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
package org.apache.wicket.protocol.http.pagestore;

import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.pagestore.AbstractPageStore.SerializedPage;

/**
 * Cache that stores serialized pages. This is important to make sure that a single page is not
 * serialized twice or more when not necessary.
 * <p>
 * For example a page is serialized during request, but it might be also later serialized on session
 * replication. The purpose of this cache is to make sure that the data obtained from first
 * serialization is reused on second serialization.
 * 
 * @author Matej Knopp
 */
class SerializedPagesCache
{
	/**
	 * Construct.
	 * 
	 * @param size
	 */
	public SerializedPagesCache(final int size)
	{
		this.size = size;
		cache = new ArrayList(size);
	}

	private final int size;

	private final List /* SerializedPageWithSession */cache;

	SerializedPageWithSession removePage(Page page)
	{
		if (size > 0)
		{
			synchronized (cache)
			{
				for (Iterator i = cache.iterator(); i.hasNext();)
				{
					SoftReference ref = (SoftReference)i.next();
					SerializedPageWithSession entry = (SerializedPageWithSession)ref.get();
					if (entry != null && entry.page.get() == page)
					{
						i.remove();
						return entry;
					}
				}
			}
		}
		return null;
	}

	SerializedPageWithSession getPage(Page page)
	{
		SerializedPageWithSession result = null;
		if (size > 0)
		{
			synchronized (cache)
			{
				for (Iterator i = cache.iterator(); i.hasNext();)
				{
					SoftReference ref = (SoftReference)i.next();
					SerializedPageWithSession entry = (SerializedPageWithSession)ref.get();
					if (entry != null && entry.page.get() == page)
					{
						i.remove();
						result = entry;
						break;
					}
				}

				if (result != null)
				{
					cache.add(new SoftReference(result));
				}
			}
		}
		return result;
	}

	SerializedPageWithSession getPage(String sessionId, int pageId, String pageMapName,
		int version, int ajaxVersion)
	{
		if (size > 0)
		{
			synchronized (cache)
			{
				for (Iterator i = cache.iterator(); i.hasNext();)
				{
					SoftReference ref = (SoftReference)i.next();
					SerializedPageWithSession entry = (SerializedPageWithSession)ref.get();
					if (entry != null && entry.sessionId.equals(sessionId) &&
						entry.pageId == pageId && entry.pageMapName.equals(pageMapName) &&
						entry.versionNumber == version && entry.ajaxVersionNumber == ajaxVersion)
					{
						return entry;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Store the serialized page in cache
	 * 
	 * @return
	 * @param sessionId
	 * @param page
	 * @param pagesList
	 */
	SerializedPageWithSession storePage(String sessionId, Page page,
		List /* <SerializedPage> */pagesList)
	{
		SerializedPageWithSession entry = new SerializedPageWithSession(sessionId, page, pagesList);
		SoftReference ref = new SoftReference(entry);

		if (size > 0)
		{
			synchronized (cache)
			{
				removePage(page);
				cache.add(ref);
				if (cache.size() > size)
				{
					cache.remove(0);
				}
			}
		}

		return entry;
	}

	/**
	 * 
	 * @author Matej Knopp
	 */
	static class SerializedPageWithSession implements Serializable
	{
		private static final long serialVersionUID = 1L;

		// this is used for lookup on pagemap serialization. We don't have the
		// session id at that point, because it can happen outside the request
		// thread. We only have the page instance and we need to use it as a key
		final transient WeakReference /* <Page> */page;

		// list of serialized pages
		final List<SerializedPage> pages;

		final String sessionId;

		// after deserialization, we need to be able to know which page to load
		final int pageId;
		final String pageMapName;
		final int versionNumber;
		final int ajaxVersionNumber;

		SerializedPageWithSession(String sessionId, Page page, List /* <SerializablePage> */pages)
		{
			this.sessionId = sessionId;
			pageId = page.getNumericId();
			pageMapName = page.getPageMapName();
			versionNumber = page.getCurrentVersionNumber();
			ajaxVersionNumber = page.getAjaxVersionNumber();
			this.pages = new ArrayList(pages);
			this.page = new WeakReference(page);
		}

		SerializedPageWithSession(String sessionId, int pageId, String pageMapName,
			int versionNumber, int ajaxVersionNumber, List /* <SerializablePage> */pages)
		{
			this.sessionId = sessionId;
			page = new WeakReference(NO_PAGE);
			this.pageId = pageId;
			this.pageMapName = pageMapName;
			this.versionNumber = versionNumber;
			this.ajaxVersionNumber = ajaxVersionNumber;
			this.pages = pages;
		}

		static Object NO_PAGE = new Object();

		@Override
		public String toString()
		{
			return getClass().getName() + " [ pageId:" + pageId + ", pageMapName: " + pageMapName +
				", session: " + sessionId + "]";
		}
	};

}
