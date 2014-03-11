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
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

import org.apache.wicket.page.IManageablePage;
import org.apache.wicket.serialize.ISerializer;
import org.apache.wicket.util.lang.Args;

/**
 * A page store that uses a SecondLevelPageCache with the last N used page instances
 * per session.
 *
 * <strong>Note</strong>: the size of the cache depends on the {@code cacheSize} constructor
 * parameter multiplied by the number of the active http sessions.
 *
 * It depends on the application use cases but usually a reasonable value of
 * {@code cacheSize} would be just a few pages (2-3). If the application don't expect many
 * active http sessions and the work flow involves usage of the browser/application history
 * then the {@code cacheSize} value may be increased to a bigger value.
 */
public class PerSessionPageStore extends AbstractCachingPageStore<IManageablePage>
{
	/**
	 * Constructor.
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
		super(pageSerializer, dataStore, new PagesCache(cacheSize));
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
	 * An implementation of SecondLevelPageCache that stores the last used N live page instances
	 * per http session.
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
			private long accessTime;

			private PageValue(IManageablePage page)
			{
				this(page.getPageId());
			}

			private PageValue(int pageId)
			{
				this.pageId = pageId;
				touch();
			}

			/**
			 * Updates the access time with the current time
			 */
			private void touch()
			{
				accessTime = System.nanoTime();
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
				return Long.valueOf(p1.accessTime).compareTo(p2.accessTime);
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
						PageValue sample = new PageValue(pageId);
						Iterator<Map.Entry<PageValue, IManageablePage>> iterator = pages.entrySet().iterator();
						while (iterator.hasNext())
						{
							Map.Entry<PageValue, IManageablePage> entry = iterator.next();
							if (sample.equals(entry.getKey()))
							{
								result = entry.getValue();
								iterator.remove();
								break;
							}
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
						PageValue sample = new PageValue(pageId);
						for (Map.Entry<PageValue, IManageablePage> entry : pages.entrySet())
						{
							if (sample.equals(entry.getKey()))
							{
								// touch the entry
								entry.getKey().touch();
								result = entry.getValue();
								break;
							}
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
					removePage(sessionId, pageId);

					PageValue pv = new PageValue(page);
					pages.put(pv, page);

					while (pages.size() > maxEntriesPerSession)
					{
						pages.pollFirstEntry();
					}
				}
			}
		}

		@Override
		public void destroy()
		{
			cache.clear();
		}
	}
}
