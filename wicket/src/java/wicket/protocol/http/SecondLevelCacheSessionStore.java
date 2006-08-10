/*
 * $Id: org.eclipse.jdt.ui.prefs 5004 2006-03-17 20:47:08 -0800 (Fri, 17 Mar
 * 2006) eelco12 $ $Revision: 5004 $ $Date: 2006-03-17 20:47:08 -0800 (Fri, 17
 * Mar 2006) $
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.protocol.http;

import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import wicket.Application;
import wicket.Page;
import wicket.PageMap;
import wicket.Request;
import wicket.Session;
import wicket.session.pagemap.IPageMapEntry;

/**
 * FIXME document me!
 * 
 * @author jcompagner
 */
public class SecondLevelCacheSessionStore extends HttpSessionStore
{
	private static final class SecondLevelCachePageMap extends PageMap
	{
		private static final long serialVersionUID = 1L;

		Page lastPage = null;

		/**
		 * Construct.
		 * 
		 * @param name
		 * @param session
		 */
		private SecondLevelCachePageMap(String name, Session session)
		{
			super(name, session);
		}

		@Override
		protected void removeEntry(IPageMapEntry entry)
		{
			String sessionId = getSession().getId();
			if (sessionId != null)
			{
				getStore().removePage(sessionId, entry.getPage());
			}
		}

		@Override
		protected void put(Page page)
		{
			if (!page.isPageStateless())
			{
				String sessionId = getSession().getId();
				if (sessionId != null)
				{
					if (lastPage != page)
					{
						lastPage = page;
						dirty();
					}
					getStore().storePage(sessionId, page);
				}
			}
		}

		@Override
		protected Page get(int id, int versionNumber)
		{
			if (lastPage != null && lastPage.getNumericId() == id)
			{
				Page page = lastPage.getVersion(versionNumber);
				if (page != null)
				{
					return page;
				}
			}
			String sessionId = getSession().getId();
			if (sessionId != null)
			{
				return getStore().getPage(sessionId, id, versionNumber);
			}
			return null;
		}

		private IPageStore getStore()
		{
			return ((SecondLevelCacheSessionStore)Application.get().getSessionStore()).getStore();
		}
	}

	/**
	 * FIXME document me!
	 */
	public interface IPageStore
	{

		/**
		 * @param sessionId
		 * @param page
		 */
		void storePage(String sessionId, Page page);

		/**
		 * @param sessionId
		 * @param id
		 * @param versionNumber
		 * @return The page
		 */
		Page getPage(String sessionId, int id, int versionNumber);

		/**
		 * @param sessionId
		 * @param page
		 */
		void removePage(String sessionId, Page page);

		/**
		 * @param sessionId
		 */
		void unbind(String sessionId);

	}

	private final IPageStore cachingStore;

	/**
	 * Construct.
	 * 
	 * @param pageStore
	 */
	public SecondLevelCacheSessionStore(final IPageStore pageStore)
	{
		this.cachingStore = new IPageStore()
		{
			Map<String, SoftReference<Map<String, SoftReference<Page>>>> sessionMap = new ConcurrentHashMap<String, SoftReference<Map<String, SoftReference<Page>>>>();

			public void unbind(String sessionId)
			{
				sessionMap.remove(sessionId);
				pageStore.unbind(sessionId);
			}

			public void removePage(String sessionId, Page page)
			{
				SoftReference sr = sessionMap.get(sessionId);
				if (sr != null)
				{
					Map map = (Map)sr.get();
					if (map != null)
					{
						map.remove(page.getId());
					}
				}
				pageStore.removePage(sessionId, page);
			}

			public Page getPage(String sessionId, int id, int versionNumber)
			{
				SoftReference sr = sessionMap.get(sessionId);
				if (sr != null)
				{
					Map map = (Map)sr.get();
					if (map != null)
					{
						SoftReference sr2 = (SoftReference)map.get(Integer.toString(id));
						if (sr2 != null)
						{
							Page page = (Page)sr2.get();
							if (page != null)
							{
								page = page.getVersion(versionNumber);
							}
							if (page != null)
							{
								return page;
							}
						}
					}
				}
				return pageStore.getPage(sessionId, id, versionNumber);
			}

			public void storePage(String sessionId, Page page)
			{
				Map<String, SoftReference<Page>> pageMap = null;
				SoftReference<Map<String, SoftReference<Page>>> sr = sessionMap.get(sessionId);
				if (sr == null || (pageMap = sr.get()) == null)
				{
					pageMap = new ConcurrentHashMap<String, SoftReference<Page>>();
					sessionMap.put(sessionId, new SoftReference<Map<String, SoftReference<Page>>>(
							pageMap));
				}
				pageMap.put(page.getId(), new SoftReference<Page>(page));
				pageStore.storePage(sessionId, page);
			}
		};
	}

	/**
	 * @see wicket.session.ISessionStore#setAttribute(wicket.Request,
	 *      java.lang.String, java.lang.Object)
	 */
	@Override
	public final void setAttribute(Request request, String name, Object value)
	{
		// ignore all pages, they are stored through the pagemap
		if (!(value instanceof Page))
		{
			super.setAttribute(request, name, value);
		}
	}

	/**
	 * @see wicket.protocol.http.AbstractHttpSessionStore#onUnbind(java.lang.String)
	 */
	@Override
	protected void onUnbind(String sessionId)
	{
		getStore().unbind(sessionId);
	}

	/**
	 * @return The store to use
	 */
	public IPageStore getStore()
	{
		return cachingStore;
	}

	/**
	 * @see wicket.protocol.http.HttpSessionStore#createPageMap(java.lang.String,
	 *      wicket.Session)
	 */
	@Override
	public PageMap createPageMap(String name, Session session)
	{
		return new SecondLevelCachePageMap(name, session);
	}
}
