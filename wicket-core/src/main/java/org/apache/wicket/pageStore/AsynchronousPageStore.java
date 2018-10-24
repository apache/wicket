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

import java.io.Serializable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

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
public class AsynchronousPageStore implements IPageStore
{

	/** Log for reporting. */
	private static final Logger log = LoggerFactory.getLogger(AsynchronousPageStore.class);

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
	private final IPageStore delegate;

	/**
	 * The queue where the entries which have to be saved are temporary stored
	 */
	private final BlockingQueue<Entry> entries;

	/**
	 * A map 'sessionId:::pageId' -> {@link Entry}. Used for fast retrieval of {@link Entry}s which
	 * are not yet stored by the wrapped {@link IPageStore}
	 */
	private final ConcurrentMap<String, Entry> entryMap;

	private AtomicBoolean operates = new AtomicBoolean(true);

	/**
	 * Construct.
	 *
	 * @param delegate
	 *            the wrapped {@link IPageStore} that actually saved the page
	 * @param capacity
	 *            the capacity of the queue that delays the saving
	 */
	public AsynchronousPageStore(final IPageStore delegate, final int capacity)
	{
		this.delegate = Args.notNull(delegate, "delegate");
		entries = new LinkedBlockingQueue<>(capacity);
		entryMap = new ConcurrentHashMap<>();

		pageSavingThread = new Thread(new PageSavingRunnable(), "Wicket-AsyncPageStore-PageSavingThread");
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
			result = prime * result + sessionId.hashCode();
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
			if (!sessionId.equals(other.sessionId))
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
	private class PageSavingRunnable implements Runnable
	{
		@Override
		public void run()
		{
			while (operates.get())
			{
				Entry entry = null;
				try
				{
					entry = entries.poll(POLL_WAIT, TimeUnit.MILLISECONDS);
				}
				catch (InterruptedException e)
				{
					log.debug("PageSavingRunnable:: Interrupted...");
				}

				if (entry != null && operates.get())
				{
					log.debug("PageSavingRunnable:: Saving asynchronously: {}...", entry);
					delegate.storePage(entry.sessionId, entry.page);
					entryMap.remove(getKey(entry));
				}
			}
		}
	}

	@Override
	public void destroy()
	{
		operates.compareAndSet(true, false);
		if (pageSavingThread.isAlive())
		{
			try
			{
				pageSavingThread.join();
			}
			catch (InterruptedException e)
			{
				log.error(e.getMessage(), e);
			}
		}
		delegate.destroy();
	}

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
		IManageablePage page = delegate.getPage(sessionId, pageId);

		log.debug("Returning the page of a stored entry with session id '{}' and page id '{}'",
			sessionId, pageId);

		return page;
	}

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

		delegate.removePage(sessionId, pageId);
	}

	@Override
	public void storePage(String sessionId, IManageablePage page)
	{
		if (!operates.get())
		{
			return;
		}
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
				delegate.storePage(sessionId, page);
			}
		}
		catch (InterruptedException e)
		{
			log.error(e.getMessage(), e);
			if (operates.get())
			{
				entryMap.remove(key);
				delegate.storePage(sessionId, page);
			}
		}
	}

	@Override
	public void unbind(String sessionId)
	{
		delegate.unbind(sessionId);
	}

	@Override
	public Serializable prepareForSerialization(String sessionId, Serializable page)
	{
		return delegate.prepareForSerialization(sessionId, page);
	}

	@Override
	public Object restoreAfterSerialization(Serializable serializable)
	{
		return delegate.restoreAfterSerialization(serializable);
	}

	@Override
	public IManageablePage convertToPage(Object page)
	{
		return delegate.convertToPage(page);
	}

	@Override
	public boolean canBeAsynchronous()
	{
		// should not wrap in another AsynchronousPageStore
		return false;
	}
}
