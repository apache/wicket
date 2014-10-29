/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.page;

import java.io.Serializable;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.wicket.Application;
import org.apache.wicket.settings.IExceptionSettings.ThreadDumpStrategy;
import org.apache.wicket.util.IProvider;
import org.apache.wicket.util.LazyInitializer;
import org.apache.wicket.util.lang.Threads;
import org.apache.wicket.util.time.Duration;
import org.apache.wicket.util.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Synchronizes access to page instances from multiple threads
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public class PageAccessSynchronizer implements Serializable
{
	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(PageAccessSynchronizer.class);

	/** map of which pages are owned by which threads */
	private final IProvider<ConcurrentMap<Integer, PageLock>> locks = new LazyInitializer<ConcurrentMap<Integer, PageLock>>()
	{
		private static final long serialVersionUID = 1L;

		@Override
		protected ConcurrentMap<Integer, PageLock> createInstance()
		{
			return new ConcurrentHashMap<Integer, PageLock>();
		}
	};

	/** timeout value for acquiring a page lock */
	private final Duration timeout;

	/**
	 * Constructor
	 * 
	 * @param timeout
	 *            timeout value for acquiring a page lock
	 */
	public PageAccessSynchronizer(Duration timeout)
	{
		this.timeout = timeout;
	}

	private static long remaining(Time start, Duration timeout)
	{
		return Math.max(0, timeout.subtract(start.elapsedSince()).getMilliseconds());
	}

	/**
	 * @param pageId
	 *            the id of the page to be locked
	 * @return the duration for acquiring a page lock
	 */
	public Duration getTimeout(int pageId)
	{
		return timeout;
	}

	/**
	 * Acquire a lock to a page
	 * 
	 * @param pageId
	 *            page id
	 * @throws CouldNotLockPageException
	 *             if lock could not be acquired
	 */
	public void lockPage(int pageId) throws CouldNotLockPageException
	{
		final Thread thread = Thread.currentThread();
		final PageLock lock = new PageLock(pageId, thread);
		final Time start = Time.now();

		boolean locked = false;

		final boolean isDebugEnabled = logger.isDebugEnabled();

		PageLock previous = null;

		Duration timeout = getTimeout(pageId);

		while (!locked && start.elapsedSince().lessThan(timeout))
		{
			if (isDebugEnabled)
			{
				logger.debug("'{}' attempting to acquire lock to page with id '{}'",
					thread.getName(), pageId);
			}

			previous = locks.get().putIfAbsent(pageId, lock);

			if (previous == null || previous.thread == thread)
			{
				// first thread to acquire lock or lock is already owned by this thread
				locked = true;
			}
			else
			{
				// wait for a lock to become available
				long remaining = remaining(start, timeout);
				if (remaining > 0)
				{
					previous.waitForRelease(remaining, isDebugEnabled);
				}
			}
		}
		if (locked)
		{
			if (isDebugEnabled)
			{
				logger.debug("{} acquired lock to page {}", thread.getName(), pageId);
			}
		}
		else
		{
			if (logger.isWarnEnabled())
			{
				logger.warn(
					"Thread '{}' failed to acquire lock to page with id '{}', attempted for {} out of allowed {}. The thread that holds the lock has name '{}'.",
					new Object[] { thread.getName(), pageId, start.elapsedSince(), timeout,
							previous.thread.getName() });
				if (Application.exists())
				{
					ThreadDumpStrategy strategy = Application.get()
						.getExceptionSettings()
						.getThreadDumpStrategy();
					switch (strategy)
					{
						case ALL_THREADS :
							Threads.dumpAllThreads(logger);
							break;
						case THREAD_HOLDING_LOCK :
							Threads.dumpSingleThread(logger, previous.thread);
							break;
						case NO_THREADS :
						default :
							// do nothing
					}
				}
			}
			throw new CouldNotLockPageException(pageId, thread.getName(), timeout);
		}
	}

	/**
	 * Unlocks all pages locked by this thread
	 */
	public void unlockAllPages()
	{
		internalUnlockPages(null);
	}

	/**
	 * Unlocks a single page locked by the current thread.
	 * 
	 * @param pageId
	 *            the id of the page which should be unlocked.
	 */
	public void unlockPage(int pageId)
	{
		internalUnlockPages(pageId);
	}

	private void internalUnlockPages(final Integer pageId)
	{
		final Thread thread = Thread.currentThread();
		final Iterator<PageLock> locks = this.locks.get().values().iterator();

		final boolean isDebugEnabled = logger.isDebugEnabled();

		while (locks.hasNext())
		{
			// remove all locks held by this thread if 'pageId' is not specified
			// otherwise just the lock for this 'pageId'
			final PageLock lock = locks.next();
			if ((pageId == null || pageId == lock.pageId) && lock.thread == thread)
			{
				locks.remove();
				if (isDebugEnabled)
				{
					logger.debug("'{}' released lock to page with id '{}'", thread.getName(),
						lock.pageId);
				}
				// if any locks were removed notify threads waiting for a lock
				lock.markReleased(isDebugEnabled);
				if (pageId != null)
				{
					// unlock just the page with the specified id
					break;
				}
			}
		}
	}

	/*
	 * used by tests
	 */
	IProvider<ConcurrentMap<Integer, PageLock>> getLocks()
	{
		return locks;
	}

	/**
	 * Wraps a page manager with this synchronizer
	 * 
	 * @param pagemanager
	 * @return wrapped page manager
	 */
	public IPageManager adapt(IPageManager pagemanager)
	{
		return new PageManagerDecorator(pagemanager)
		{
			@Override
			public IManageablePage getPage(int pageId)
			{
				IManageablePage page = null;
				try
				{
					lockPage(pageId);
					page = super.getPage(pageId);
				}
				finally
				{
					if (page == null)
					{
						unlockPage(pageId);
					}
				}
				return page;
			}

			@Override
			public void touchPage(IManageablePage page)
			{
				lockPage(page.getPageId());
				super.touchPage(page);
			}

			@Override
			public void commitRequest()
			{
				try
				{
					super.commitRequest();
				}
				finally
				{
					unlockAllPages();
				}
			}
		};
	}

	/**
	 * Thread's lock on a page
	 * 
	 * @author igor
	 */
	public static class PageLock
	{
		/** page id */
		private final int pageId;

		/** thread that owns the lock */
		private final Thread thread;

		private volatile boolean released = false;

		/**
		 * Constructor
		 * 
		 * @param pageId
		 * @param thread
		 */
		public PageLock(int pageId, Thread thread)
		{
			this.pageId = pageId;
			this.thread = thread;
		}

		/**
		 * @return page id of locked page
		 */
		public int getPageId()
		{
			return pageId;
		}

		/**
		 * @return thread that owns the lock
		 */
		public Thread getThread()
		{
			return thread;
		}

		final synchronized void waitForRelease(long remaining, boolean isDebugEnabled)
		{
			if (released)
			{
				// the thread holding the lock released it before we were able to wait for the
				// release
				if (isDebugEnabled)
				{
					logger.debug(
						"lock for page with id {} no longer locked by {}, falling through", pageId,
						thread.getName());
				}
				return;
			}

			if (isDebugEnabled)
			{
				logger.debug("{} waiting for lock to page {} for {}",
					new Object[] { thread.getName(), pageId, Duration.milliseconds(remaining) });
			}
			try
			{
				wait(remaining);
			}
			catch (InterruptedException e)
			{
				// TODO better exception
				throw new RuntimeException(e);
			}
		}

		final synchronized void markReleased(boolean isDebugEnabled)
		{
			if (isDebugEnabled)
			{
				logger.debug("'{}' notifying blocked threads", thread.getName());
			}
			released = true;
			notifyAll();
		}
	}
}
