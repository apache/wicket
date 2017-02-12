package org.apache.wicket.pageStore;

import java.io.Serializable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.wicket.page.IManageablePage;
import org.apache.wicket.util.lang.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Facade for {@link IPageStore} that does the actual saving in worker thread.
 * <p>
 * Creates an {@link Entry} for each double (sessionId, page) and puts it in {@link #entries} queue
 * if there is room. Acts as producer.<br/>
 * Later {@link PageSavingRunnable} reads in blocking manner from {@link #entries} and saves each
 * entry. Acts as consumer.
 * </p>
 * It starts only one instance of {@link PageSavingRunnable} because all we need is to make the page
 * storing asynchronous. We don't want to write concurrently in the wrapped {@link IPageStore},
 * though it may happen in the extreme case when the queue is full. These cases should be avoided.
 * 
 * Based on AsynchronousDataStore (@author Matej Knopp).
 * 
 * @author manuelbarzi
 */
public class AsyncPageStore implements IPageStore
{

	/** Log for reporting. */
	private static final Logger log = LoggerFactory.getLogger(AsyncPageStore.class);

	/**
	 * The time to wait when adding an {@link Entry} into the entries. In millis.
	 */
	private static final long OFFER_WAIT = 30L;

	/**
	 * The time to wait for an entry to save with the wrapped {@link IPageStore} . In millis.
	 */
	private static final long POLL_WAIT = 1000L;

	/**
	 * The page saving thread.
	 */
	private final Thread pageSavingThread;

	/**
	 * The wrapped {@link IPageStore} that actually stores that pages
	 */
	private final IPageStore pageStore;

	/**
	 * The queue where the entries which have to be saved are temporary stored
	 */
	private final BlockingQueue<Entry> entries;

	/**
	 * A map 'sessionId:::pageId' -> {@link Entry}. Used for fast retrieval of {@link Entry}s which
	 * are not yet stored by the wrapped {@link IPageStore}
	 */
	private final ConcurrentMap<String, Entry> entryMap;

	/**
	 * Construct.
	 * 
	 * @param pageStore
	 *            the wrapped {@link IPageStore} that actually saved the page
	 * @param capacity
	 *            the capacity of the queue that delays the saving
	 */
	public AsyncPageStore(final IPageStore pageStore, final int capacity)
	{
		this.pageStore = pageStore;
		entries = new LinkedBlockingQueue<Entry>(capacity);
		entryMap = new ConcurrentHashMap<String, Entry>();

		PageSavingRunnable savingRunnable = new PageSavingRunnable(pageStore, entries, entryMap);
		pageSavingThread = new Thread(savingRunnable, "AsyncPageStore-PageSavingThread");
		pageSavingThread.setDaemon(true);
		pageSavingThread.start();
	}

	/**
	 * Little helper
	 * 
	 * @param sessionId
	 * @param pageId
	 * @return Entry
	 */
	private Entry getEntry(final String sessionId, final int pageId)
	{
		return entryMap.get(getKey(sessionId, pageId));
	}

	/**
	 * 
	 * @param pageId
	 * @param sessionId
	 * @return generated key
	 */
	private static String getKey(final String sessionId, final int pageId)
	{
		return pageId + ":::" + sessionId;
	}

	/**
	 * 
	 * @param entry
	 * @return generated key
	 */
	private static String getKey(final Entry entry)
	{
		return getKey(entry.sessionId, entry.page.getPageId());
	}

	/**
	 * The structure used for an entry in the queue
	 */
	private static class Entry
	{
		private final String sessionId;
		private final IManageablePage page;

		public Entry(final String sessionId, final IManageablePage page)
		{
			this.sessionId = Args.notNull(sessionId, "sessionId");
			this.page = Args.notNull(page, "page");
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + page.getPageId();
			result = prime * result + ((sessionId == null) ? 0 : sessionId.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Entry other = (Entry)obj;
			if (page.getPageId() != other.page.getPageId())
				return false;
			if (sessionId == null)
			{
				if (other.sessionId != null)
					return false;
			}
			else if (!sessionId.equals(other.sessionId))
				return false;
			return true;
		}

		@Override
		public String toString()
		{
			return "Entry [sessionId=" + sessionId + ", pageId=" + page.getPageId() + "]";
		}

	}

	/**
	 * The thread that acts as consumer of {@link Entry}ies
	 */
	private static class PageSavingRunnable implements Runnable
	{
		private static final Logger log = LoggerFactory.getLogger(PageSavingRunnable.class);

		private final BlockingQueue<Entry> entries;

		private final ConcurrentMap<String, Entry> entryMap;

		private final IPageStore pageStore;

		private PageSavingRunnable(IPageStore pageStore, BlockingQueue<Entry> entries,
			ConcurrentMap<String, Entry> entryMap)
		{
			this.pageStore = pageStore;
			this.entries = entries;
			this.entryMap = entryMap;
		}

		@Override
		public void run()
		{
			while (!Thread.interrupted())
			{
				Entry entry = null;
				try
				{
					entry = entries.poll(POLL_WAIT, TimeUnit.MILLISECONDS);
				}
				catch (InterruptedException e)
				{
					Thread.currentThread().interrupt();
				}

				if (entry != null)
				{
					log.debug("Saving asynchronously: {}...", entry);
					pageStore.storePage(entry.sessionId, entry.page);
					entryMap.remove(getKey(entry));
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.wicket.pageStore.IPageStore#destroy()
	 */
	@Override
	public void destroy()
	{
		if (pageSavingThread.isAlive())
		{
			pageSavingThread.interrupt();
			try
			{
				pageSavingThread.join();
			}
			catch (InterruptedException e)
			{
				log.error(e.getMessage(), e);
			}
		}

		pageStore.destroy();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.wicket.pageStore.IPageStore#getPage(java.lang.String, int)
	 */
	@Override
	public IManageablePage getPage(String sessionId, int pageId)
	{
		Entry entry = getEntry(sessionId, pageId);
		if (entry != null)
		{
			log.debug(
				"Returning the page of a non-stored entry with session id '{}' and page id '{}'",
				sessionId, pageId);
			return entry.page;
		}
		IManageablePage page = pageStore.getPage(sessionId, pageId);

		log.debug("Returning the page of a stored entry with session id '{}' and page id '{}'",
			sessionId, pageId);

		return page;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.wicket.pageStore.IPageStore#removePage(java.lang.String, int)
	 */
	@Override
	public void removePage(String sessionId, int pageId)
	{
		String key = getKey(sessionId, pageId);
		if (key != null)
		{
			Entry entry = entryMap.remove(key);
			if (entry != null)
			{
				entries.remove(entry);
			}
		}

		pageStore.removePage(sessionId, pageId);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.wicket.pageStore.IPageStore#storePage(java.lang.String,
	 * org.apache.wicket.page.IManageablePage)
	 */
	@Override
	public void storePage(String sessionId, IManageablePage page)
	{
		Entry entry = new Entry(sessionId, page);
		String key = getKey(entry);
		entryMap.put(key, entry);

		try
		{
			if (entries.offer(entry, OFFER_WAIT, TimeUnit.MILLISECONDS))
			{
				log.debug("Offered for storing asynchronously page with id '{}' in session '{}'",
					page.getPageId(), sessionId);
			}
			else
			{
				log.debug("Storing synchronously page with id '{}' in session '{}'",
					page.getPageId(), sessionId);
				entryMap.remove(key);
				pageStore.storePage(sessionId, page);
			}
		}
		catch (InterruptedException e)
		{
			log.error(e.getMessage(), e);
			entryMap.remove(key);
			pageStore.storePage(sessionId, page);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.wicket.pageStore.IPageStore#unbind(java.lang.String)
	 */
	@Override
	public void unbind(String sessionId)
	{
		pageStore.unbind(sessionId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.wicket.pageStore.IPageStore#prepareForSerialization(java.lang. String,
	 * java.io.Serializable)
	 */
	@Override
	public Serializable prepareForSerialization(String sessionId, Serializable page)
	{
		return pageStore.prepareForSerialization(sessionId, page);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.wicket.pageStore.IPageStore#restoreAfterSerialization(java.io. Serializable)
	 */
	@Override
	public Object restoreAfterSerialization(Serializable serializable)
	{
		return pageStore.restoreAfterSerialization(serializable);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.wicket.pageStore.IPageStore#convertToPage(java.lang.Object)
	 */
	@Override
	public IManageablePage convertToPage(Object page)
	{
		return pageStore.convertToPage(page);
	}

}
