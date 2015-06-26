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

import org.apache.wicket.page.IManageablePage;
import org.apache.wicket.serialize.ISerializer;
import org.apache.wicket.util.lang.Args;

/**
 * An abstract {@link org.apache.wicket.pageStore.IPageStore} that uses
 * {@link org.apache.wicket.pageStore.SecondLevelPageCache} to cache the stored pages in memory
 *
 * @param <P>
 *          The type of the page to be stored
 */
public abstract class AbstractCachingPageStore<P> extends AbstractPageStore
{
	/**
	 * The cache implementation
	 */
	protected final SecondLevelPageCache<String, Integer, P> pagesCache;

	/**
	 * Constructor.
	 *
	 * @param pageSerializer
	 *          The serializer that will convert pages to/from byte[]
	 * @param dataStore
	 *          The third level page cache
	 * @param pagesCache
	 *          The cache to use as a second level store
	 */
	protected AbstractCachingPageStore(ISerializer pageSerializer, IDataStore dataStore,
	                                   SecondLevelPageCache<String, Integer, P> pagesCache)
	{
		super(pageSerializer, dataStore);

		this.pagesCache = Args.notNull(pagesCache, "pagesCache");
	}

	@SuppressWarnings("unchecked")
	@Override
	public IManageablePage getPage(final String sessionId, final int pageId)
	{
		P fromCache = pagesCache.getPage(sessionId, pageId);
		if (fromCache != null)
		{
			return convertToPage(fromCache);
		}

		byte[] data = getPageData(sessionId, pageId);
		if (data != null)
		{
			IManageablePage page = deserializePage(data);
			pagesCache.storePage(sessionId, pageId,  (P) page);
			return page;
		}
		return null;
	}

	@Override
	public void removePage(final String sessionId, final int pageId)
	{
		pagesCache.removePage(sessionId, pageId);
		removePageData(sessionId, pageId);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void storePage(final String sessionId, final IManageablePage page)
	{
		byte[] data = serializePage(page);
		if (data != null)
		{
			int pageId = page.getPageId();
			pagesCache.storePage(sessionId, pageId,  (P) page);
			storePageData(sessionId, pageId, data);
		}
	}

	@Override
	public void unbind(final String sessionId)
	{
		removePageData(sessionId);
		pagesCache.removePages(sessionId);
	}

	@Override
	public void destroy()
	{
		super.destroy();
		pagesCache.destroy();
	}
}
