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

import org.apache.wicket.util.IProvider;
import org.apache.wicket.util.LazyInitializer;
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
	private IProvider<ConcurrentMap<Integer, PageLock>> locks = new LazyInitializer<ConcurrentMap<Integer, PageLock>>()
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

		while (!locked && start.elapsedSince().lessThan(timeout))
		{
			if (isDebugEnabled)
			{
				logger.debug("'{}' attempting to acquire lock to page with id '{}'",
					thread.getName(), pageId);
			}

			PageLock previous = locks.get().putIfAbsent(pageId, lock);
			if (previous == null || previous.getThread() == thread)
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
					synchronized (previous)
					{
						if (isDebugEnabled)
						{
							logger.debug("{} waiting for lock to page {} for {}", new Object[] {
									thread.getName(), pageId, Duration.milliseconds(remaining) });
						}
						try
						{
							previous.wait(remaining);
						}
						catch (InterruptedException e)
						{
							// TODO better exception
							throw new RuntimeException(e);
						}
					}
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
					"{} failed to acquire lock to page {}, attempted for {} out of allowed {}",
					new Object[] { thread.getName(), pageId, start.elapsedSince(), timeout });
			}
			throw new CouldNotLockPageException(pageId, thread.getName(), timeout);
		}
	}

	/**
	 * Unlocks all pages locked by this thread
	 */
	public void unlockAllPages()
	{
		final Thread thread = Thread.currentThread();
		final Iterator<PageLock> locks = this.locks.get().values().iterator();

		final boolean isDebugEnabled = logger.isDebugEnabled();

		while (locks.hasNext())
		{
			// remove all locks held by this thread
			final PageLock lock = locks.next();
			if (lock.getThread() == thread)
			{
				locks.remove();
				if (isDebugEnabled)
				{
					logger.debug("{} released lock to page {}", thread.getName(), lock.getPageId());
				}
				// if any locks were removed notify threads waiting for a lock
				synchronized (lock)
				{
					if (isDebugEnabled)
					{
						logger.debug("{} notifying blocked threads", thread.getName());
					}
					lock.notifyAll();
				}
			}
		}
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
			public IManageablePage getPage(int id)
			{
				lockPage(id);
				return super.getPage(id);
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
	}
}
