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

import java.lang.ref.SoftReference;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

import org.apache.wicket.page.IManageablePage;
import org.apache.wicket.serialize.ISerializer;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Objects;
import org.apache.wicket.util.time.Time;

/**
 *
 */
public class PerSessionPageStore extends AbstractPageStore
{
	private final SecondLevelPageCache<String, Integer, IManageablePage> pagesCache;

	/**
	 * Construct.
	 *
	 * @param pageSerializer
	 *            the {@link org.apache.wicket.serialize.ISerializer} that will be used to convert pages from/to byte arrays
	 * @param dataStore
	 *            the {@link org.apache.wicket.pageStore.IDataStore} that actually stores the pages
	 * @param cacheSize
	 *            the number of pages to cache in memory before passing them to
	 *            {@link org.apache.wicket.pageStore.IDataStore#storeData(String, int, byte[])}
	 */
	public PerSessionPageStore(final ISerializer pageSerializer, final IDataStore dataStore,
	                           final int cacheSize)
	{
		super(pageSerializer, dataStore);
		this.pagesCache = new PagesCache(cacheSize);
	}

	@Override
	public IManageablePage getPage(final String sessionId, final int id)
	{
		IManageablePage fromCache = pagesCache.getPage(sessionId, id);
		if (fromCache != null)
		{
			return fromCache;
		}

		byte[] data = getPageData(sessionId, id);
		if (data != null)
		{
			return deserializePage(data);
		}
		return null;
	}

	@Override
	public void removePage(final String sessionId, final int id)
	{
		pagesCache.removePage(sessionId, id);
		removePageData(sessionId, id);
	}

	@Override
	public void storePage(final String sessionId, final IManageablePage page)
	{
		byte[] data = serializePage(page);
		if (data != null)
		{
			pagesCache.storePage(sessionId, page.getPageId(), page);
			storePageData(sessionId, page.getPageId(), data);
		}
	}

	@Override
	public void unbind(final String sessionId)
	{
		removePageData(sessionId);
		pagesCache.removePages(sessionId);
	}

	@Override
	public IManageablePage convertToPage(final Object object)
	{
		if (object == null)
		{
			return null;
		}
		else if (object instanceof IManageablePage)
		{
			return (IManageablePage)object;
		}

		String type = object.getClass().getName();
		throw new IllegalArgumentException("Unknown object type: " + type);
	}

	/**
	 */
	protected static class PagesCache implements SecondLevelPageCache<String, Integer, IManageablePage>
	{
		/**
		 * Helper class used to compare the page entries in the cache by their
		 * access time
		 */
		private static class PageValue
		{
			/**
			 * The id of the cached page
			 */
			private final int pageId;

			/**
			 * The last time this page has been used/accessed.
			 */
			private Time accessTime;

			private PageValue(IManageablePage page)
			{
				this(page.getPageId());
			}

			private PageValue(int pageId)
			{
				this.pageId = pageId;
				this.accessTime = Time.now();
			}

			@Override
			public boolean equals(Object o)
			{
				if (this == o) return true;
				if (o == null || getClass() != o.getClass()) return false;

				PageValue pageValue = (PageValue) o;

				return pageId == pageValue.pageId;

			}

			@Override
			public int hashCode()
			{
				return pageId;
			}
		}

		private static class PageComparator implements Comparator<PageValue>
		{
			@Override
			public int compare(PageValue p1, PageValue p2)
			{
				return Objects.compareWithConversion(p1.accessTime, p2.accessTime);
			}
		}

		private final int maxEntriesPerSession;

		private final ConcurrentSkipListMap<String, SoftReference<ConcurrentSkipListMap<PageValue, IManageablePage>>> cache;

		/**
		 * Constructor.
		 *
		 * @param maxEntriesPerSession
		 *          The number of cache entries per session
		 */
		public PagesCache(final int maxEntriesPerSession)
		{
			this.maxEntriesPerSession = maxEntriesPerSession;
			cache = new ConcurrentSkipListMap<>();
		}

		/**
		 *
		 * @param sessionId
		 *          The id of the http session
		 * @param pageId
		 *          The id of the page to remove from the cache
		 * @return the removed {@link org.apache.wicket.page.IManageablePage} or <code>null</code> - otherwise
		 */
		@Override
		public IManageablePage removePage(final String sessionId, final Integer pageId)
		{
			IManageablePage result = null;

			if (maxEntriesPerSession > 0)
			{
				Args.notNull(sessionId, "sessionId");
				Args.notNull(pageId, "pageId");

				SoftReference<ConcurrentSkipListMap<PageValue, IManageablePage>> pagesPerSession = cache.get(sessionId);
				if (pagesPerSession != null)
				{
					ConcurrentMap<PageValue, IManageablePage> pages = pagesPerSession.get();
					if (pages != null)
					{
						PageValue pv = new PageValue(pageId);
						IManageablePage page = pages.remove(pv);
						if (page != null)
						{
							result = page;
						}
					}
				}
			}

			return result;
		}

		/**
		 * Removes all {@link org.apache.wicket.page.IManageablePage}s for the session
		 * with <code>sessionId</code> from the cache.
		 *
		 * @param sessionId
		 *          The id of the expired http session
		 */
		@Override
		public void removePages(String sessionId)
		{
			Args.notNull(sessionId, "sessionId");

			if (maxEntriesPerSession > 0)
			{
				cache.remove(sessionId);
			}
		}

		/**
		 * Returns a {@link org.apache.wicket.page.IManageablePage} by looking it up by <code>sessionId</code> and
		 * <code>pageId</code>. If there is a match then it is <i>touched</i>, i.e. it is moved at
		 * the top of the cache.
		 * 
		 * @param sessionId
		 *          The id of the http session
		 * @param pageId
		 *          The id of the page to find
		 * @return the found serialized page or <code>null</code> when not found
		 */
		@Override
		public IManageablePage getPage(String sessionId, Integer pageId)
		{
			IManageablePage result = null;

			if (maxEntriesPerSession > 0)
			{
				Args.notNull(sessionId, "sessionId");
				Args.notNull(pageId, "pageId");

				SoftReference<ConcurrentSkipListMap<PageValue, IManageablePage>> pagesPerSession = cache.get(sessionId);
				if (pagesPerSession != null)
				{
					ConcurrentSkipListMap<PageValue, IManageablePage> pages = pagesPerSession.get();
					if (pages != null)
					{
						PageValue pv = new PageValue(pageId);
						Map.Entry<PageValue, IManageablePage> entry = pages.ceilingEntry(pv);

						if (entry != null)
						{
							// touch the entry
							entry.getKey().accessTime = Time.now();
							result = entry.getValue();
						}
					}
				}
			}
			return result;
		}

		/**
		 * Store the serialized page in cache
		 * 
		 * @param page
		 *      the data to serialize (page id, session id, bytes)
		 */
		@Override
		public void storePage(String sessionId, Integer pageId, IManageablePage page)
		{
			if (maxEntriesPerSession > 0)
			{
				Args.notNull(sessionId, "sessionId");
				Args.notNull(pageId, "pageId");

				SoftReference<ConcurrentSkipListMap<PageValue, IManageablePage>> pagesPerSession = cache.get(sessionId);
				if (pagesPerSession == null)
				{
					ConcurrentSkipListMap<PageValue, IManageablePage> pages = new ConcurrentSkipListMap<>(new PageComparator());
					pagesPerSession = new SoftReference<>(pages);
					SoftReference<ConcurrentSkipListMap<PageValue, IManageablePage>> old = cache.putIfAbsent(sessionId, pagesPerSession);
					if (old != null)
					{
						pagesPerSession = old;
					}
				}

				ConcurrentSkipListMap<PageValue, IManageablePage> pages = pagesPerSession.get();
				if (pages == null)
				{
					pages = new ConcurrentSkipListMap<>();
					pagesPerSession = new SoftReference<>(pages);
					SoftReference<ConcurrentSkipListMap<PageValue, IManageablePage>> old = cache.putIfAbsent(sessionId, pagesPerSession);
					if (old != null)
					{
						pages = old.get();
					}
				}

				if (pages != null)
				{
					while (pages.size() > maxEntriesPerSession)
					{
						pages.pollFirstEntry();
					}

					PageValue pv = new PageValue(page);
					pages.put(pv, page);
				}
			}
		}
	}
}
