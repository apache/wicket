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
import java.util.function.Supplier;

import org.apache.wicket.Application;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.settings.ExceptionSettings.ThreadDumpStrategy;
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
	private final Supplier<ConcurrentMap<Integer, PageLock>> locks = new LazyInitializer<ConcurrentMap<Integer, PageLock>>()
	{
		private static final long serialVersionUID = 1L;

		@Override
		protected ConcurrentMap<Integer, PageLock> createInstance()
		{
			return new ConcurrentHashMap<>();
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
		final RequestCycle cycle = RequestCycle.get();
		final PageLock lock = new PageLock(pageId, cycle);
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
					cycle.getStartTime(), pageId);
			}

			previous = locks.get().putIfAbsent(pageId, lock);

			if (previous == null || previous.cycle == cycle)
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
				logger.debug("{} acquired lock to page {}", cycle.getStartTime(), pageId);
			}
		}
		else
		{
			if (logger.isWarnEnabled())
			{
				logger.warn(
					"Thread '{}' failed to acquire lock to page with id '{}', attempted for {} out of allowed {}." +
							" The thread that holds the lock has name '{}'.",
					cycle.getStartTime(), pageId, start.elapsedSince(), timeout,
							previous.cycle.getStartTime());
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
//							Threads.dumpSingleThread(logger, previous.thread);
							break;
						case NO_THREADS :
						default :
							// do nothing
					}
				}
			}
			throw new CouldNotLockPageException(pageId, "" + cycle.getStartTime(), timeout);
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
		final RequestCycle cycle = RequestCycle.get();
		final Iterator<PageLock> locks = this.locks.get().values().iterator();

		final boolean isDebugEnabled = logger.isDebugEnabled();

		while (locks.hasNext())
		{
			// remove all locks held by this thread if 'pageId' is not specified
			// otherwise just the lock for this 'pageId'
			final PageLock lock = locks.next();
			if ((pageId == null || pageId == lock.pageId) && lock.cycle == cycle)
			{
				locks.remove();
				if (isDebugEnabled)
				{
					logger.debug("'{}' released lock to page with id '{}'", cycle.getStartTime(),
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
	Supplier<ConcurrentMap<Integer, PageLock>> getLocks()
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
			public void removePage(final IManageablePage page) {
				if (page != null)
				{
					try
					{
						super.removePage(page);
						untouchPage(page);
					}
					finally
					{
						unlockPage(page.getPageId());
					}
				}
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

		/** cycle that owns the lock */
		private final RequestCycle cycle;

		private volatile boolean released = false;

		/**
		 * Constructor
		 * 
		 * @param pageId
		 * @param cycle
		 */
		public PageLock(int pageId, RequestCycle cycle)
		{
			this.pageId = pageId;
			this.cycle = cycle;
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
		public RequestCycle getCycle()
		{
			return cycle;
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
						cycle.getStartTime());
				}
				return;
			}

			if (isDebugEnabled)
			{
				logger.debug("{} waiting for lock to page {} for {}",
					cycle.getStartTime(), pageId, Duration.milliseconds(remaining));
			}
			try
			{
				wait(remaining);
			}
			catch (InterruptedException e)
			{
				throw new RuntimeException(e);
			}
		}

		final synchronized void markReleased(boolean isDebugEnabled)
		{
			if (isDebugEnabled)
			{
				logger.debug("'{}' notifying blocked threads", cycle.getStartTime());
			}
			released = true;
			notifyAll();
		}
	}
}
