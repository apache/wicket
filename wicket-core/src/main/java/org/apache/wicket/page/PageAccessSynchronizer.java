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

import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.time.Duration;
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

	/** lock manager responsible for locking and unlocking page instances */
	private final IPageLockManager pageLockManager;

	/**
	 * Constructor
	 * 
	 * @param timeout
	 *            timeout value for acquiring a page lock
	 */
	public PageAccessSynchronizer(Duration timeout)
	{
		this(new DefaultPageLockManager(timeout));
	}

	/**
	 * Constructor
	 *
	 * @param pageLockManager the lock manager
	 */
	public PageAccessSynchronizer(IPageLockManager pageLockManager)
	{
		this.pageLockManager = Args.notNull(pageLockManager, "pageLockManager");
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
		pageLockManager.lockPage(pageId);
	}

	/**
	 * Unlocks all pages locked by this thread
	 */
	public void unlockAllPages()
	{
		pageLockManager.unlockAllPages();
	}

	/**
	 * Unlocks a single page locked by the current thread.
	 * 
	 * @param pageId
	 *            the id of the page which should be unlocked.
	 */
	public void unlockPage(int pageId)
	{
		pageLockManager.unlockPage(pageId);
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

		public final synchronized void waitForRelease(long remaining, boolean isDebugEnabled)
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
					thread.getName(), pageId, Duration.milliseconds(remaining));
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

		public final synchronized void markReleased(boolean isDebugEnabled)
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
