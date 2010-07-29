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
package org.apache.wicket.pageStore;

import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.wicket.page.IManageablePage;
import org.apache.wicket.util.IProvider;
import org.apache.wicket.util.ValueProvider;
import org.apache.wicket.util.lang.Checks;
import org.apache.wicket.util.time.Duration;
import org.apache.wicket.util.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Synchronizes access to page instances from multiple threads
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public class PageAccessSynchronizer
{
	private static final Logger logger = LoggerFactory.getLogger(PageAccessSynchronizer.class);

	/** map of which pages are owned by which threads */
	private final ConcurrentHashMap<Integer, PageLock> locks = new ConcurrentHashMap<Integer, PageLock>();

	/** used to synchronize various code points */
	private final Object semaphore = new Object();

	/** timeout value for acquiring a page lock */
	private final IProvider<Duration> timeout;

	/**
	 * Constructor
	 * 
	 * @param timeout
	 *            timeout value for acquiring a page lock
	 */
	public PageAccessSynchronizer(Duration timeout)
	{
		this(ValueProvider.of(timeout));
	}

	/**
	 * Constructor
	 * 
	 * @param timeout
	 *            timeout value for acquiring a page lock
	 */
	public PageAccessSynchronizer(IProvider<Duration> timeout)
	{
		Checks.argumentNotNull(timeout, "timeout");
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
		final Duration timeout = this.timeout.get();
		final Thread thread = Thread.currentThread();
		final PageLock lock = new PageLock(pageId, thread);
		final Time start = Time.now();

		boolean locked = false;

		while (!locked && start.elapsedSince().lessThan(timeout))
		{
			logger.debug("{} attempting to acquire lock to page {}", thread.getName(), pageId);

			PageLock previous = locks.putIfAbsent(pageId, lock);
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
					synchronized (semaphore)
					{
						if (logger.isDebugEnabled())
						{
							logger.debug("{} waiting for lock to page {} for {}", new Object[] {
									thread.getName(), pageId, Duration.milliseconds(remaining) });
						}
						try
						{
							semaphore.wait(remaining);
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
			logger.debug("{} acquired lock to page {}", thread.getName(), pageId);
		}
		else
		{
			logger.warn("{} failed to acquire lock to page {}, attempted for {} out of allowed {}",
				new Object[] { thread.getName(), pageId, start.elapsedSince(), timeout });
			throw new CouldNotLockPageException(pageId, thread.getName(), timeout);
		}
	}

	/**
	 * Unlocks all pages locked by this thread
	 */
	public void unlockAllPages()
	{
		final Thread thread = Thread.currentThread();
		final Iterator<PageLock> locks = this.locks.values().iterator();

		boolean changed = false;
		while (locks.hasNext())
		{
			// remove all locks held by this thread
			final PageLock lock = locks.next();
			if (lock.getThread() == thread)
			{
				locks.remove();
				logger.debug("{} released lock to page {}", thread.getName(), lock.getPageId());
				changed = true;
			}
		}

		if (changed)
		{
			// if any locks were removed notify threads waiting for a lock
			synchronized (semaphore)
			{
				logger.debug("{} notifying blocked threads", thread.getName());
				semaphore.notifyAll();
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
				IManageablePage page = super.getPage(id);
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

		/** timestamp when lock was created */
		private final Date created;

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
			created = new Date();
		}

		/**
		 * @return page id of locked page
		 */
		public int getPageId()
		{
			return pageId;
		}

		/**
		 * @return timestamp lock was created
		 */
		public Date getCreated()
		{
			return created;
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
