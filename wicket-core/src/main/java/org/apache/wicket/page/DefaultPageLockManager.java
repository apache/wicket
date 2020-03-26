package org.apache.wicket.page;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

import org.apache.wicket.Application;
import org.apache.wicket.settings.ExceptionSettings;
import org.apache.wicket.util.LazyInitializer;
import org.apache.wicket.util.lang.Threads;
import org.apache.wicket.util.time.Durations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default {@link IPageLockManager} that that holds a map of locks in the current session.
 */
public class DefaultPageLockManager implements IPageLockManager, Serializable {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(DefaultPageLockManager.class);

	/** map of which pages are owned by which threads */
	private final Supplier<ConcurrentMap<Integer, PageAccessSynchronizer.PageLock>> locks = new LazyInitializer<ConcurrentMap<Integer, PageAccessSynchronizer.PageLock>>()
	{
		private static final long serialVersionUID = 1L;

		@Override
		protected ConcurrentMap<Integer, PageAccessSynchronizer.PageLock> createInstance()
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
	public DefaultPageLockManager(Duration timeout)
	{
		this.timeout = timeout;
	}

	private static long remaining(Instant start, Duration timeout)
	{
		Duration elapsedTime = Durations.elapsedSince(start);
		return Math.max(0, timeout.minus(elapsedTime).toMillis());
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

	@Override
	public void lockPage(int pageId) throws CouldNotLockPageException
	{
		final Thread thread = Thread.currentThread();
		final PageAccessSynchronizer.PageLock lock = new PageAccessSynchronizer.PageLock(pageId, thread);
		final Instant start = Instant.now();

		boolean locked = false;

		final boolean isDebugEnabled = logger.isDebugEnabled();

		PageAccessSynchronizer.PageLock previous = null;

		Duration timeout = getTimeout(pageId);

		while (!locked && Durations.elapsedSince(start).compareTo(timeout) < 0)
		{
			if (isDebugEnabled)
			{
				logger.debug("'{}' attempting to acquire lock to page with id '{}'",
						thread.getName(), pageId);
			}

			previous = locks.get().putIfAbsent(pageId, lock);

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
						"Thread '{}' failed to acquire lock to page with id '{}', attempted for {} out of allowed {}." +
								" The thread that holds the lock has name '{}'.",
						thread.getName(), pageId, Duration.between(start, Instant.now()), timeout,
						previous.getThread().getName());
				if (Application.exists())
				{
					ExceptionSettings.ThreadDumpStrategy strategy = Application.get()
							.getExceptionSettings()
							.getThreadDumpStrategy();
					switch (strategy)
					{
						case ALL_THREADS :
							Threads.dumpAllThreads(logger);
							break;
						case THREAD_HOLDING_LOCK :
							Threads.dumpSingleThread(logger, previous.getThread());
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

	@Override
	public void unlockAllPages()
	{
		internalUnlockPages(null);
	}

	@Override
	public void unlockPage(int pageId)
	{
		internalUnlockPages(pageId);
	}

	private void internalUnlockPages(final Integer pageId)
	{
		final Thread thread = Thread.currentThread();
		final Iterator<PageAccessSynchronizer.PageLock> locks = this.locks.get().values().iterator();

		final boolean isDebugEnabled = logger.isDebugEnabled();

		while (locks.hasNext())
		{
			// remove all locks held by this thread if 'pageId' is not specified
			// otherwise just the lock for this 'pageId'
			final PageAccessSynchronizer.PageLock lock = locks.next();
			if ((pageId == null || pageId == lock.getPageId()) && lock.getThread() == thread)
			{
				locks.remove();
				if (isDebugEnabled)
				{
					logger.debug("'{}' released lock to page with id '{}'", thread.getName(),
							lock.getPageId());
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
	Supplier<ConcurrentMap<Integer, PageAccessSynchronizer.PageLock>> getLocks()
	{
		return locks;
	}
}
