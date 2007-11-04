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
package org.apache.wicket.protocol.http;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.IPageMap;
import org.apache.wicket.Page;
import org.apache.wicket.PageMap;
import org.apache.wicket.Request;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.Session;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.protocol.http.pagestore.DiskPageStore;
import org.apache.wicket.session.pagemap.IPageMapEntry;
import org.apache.wicket.util.collections.IntHashMap;
import org.apache.wicket.version.IPageVersionManager;
import org.apache.wicket.version.undo.Change;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * FIXME document me!
 * 
 * @author jcompagner
 */
public class SecondLevelCacheSessionStore extends HttpSessionStore
{
	private static Logger log = LoggerFactory.getLogger(SecondLevelCacheSessionStore.class);

	/**
	 * This interface is used by the SecondLevelCacheSessionStore so that pages can be stored to a
	 * persistent layer. Implementation should store the page that it gets under the id and version
	 * number. So that every page version can be reconstructed when asked for.
	 * 
	 * @see DiskPageStore as default implementation.
	 */
	public static interface IPageStore
	{

		/**
		 * Destroy the store.
		 */
		void destroy();

		/**
		 * Restores a page version from the persistent layer.
		 * <p>
		 * Note that the versionNumber and ajaxVersionNumber parameters may be -1.
		 * <ul>
		 * <li>If ajaxVersionNumber is -1 and versionNumber is specified, the page store must
		 * return the page with highest ajax version.
		 * <li>If both versionNumber and ajaxVersioNumber are -1, the pagestore must return last
		 * touched (saved) page version with given id.
		 * </ul>
		 * 
		 * @param sessionId
		 * @param pagemap
		 * @param id
		 * @param versionNumber
		 * @param ajaxVersionNumber
		 * @return The page
		 */
		Page getPage(String sessionId, String pagemap, int id, int versionNumber,
			int ajaxVersionNumber);

		/**
		 * This method is called when the page is accessed. A IPageStore implementation can block
		 * until a save of that page version is done. So that a specific page version is always
		 * restore able.
		 * 
		 * @param sessionId
		 * @param page
		 */
		void pageAccessed(String sessionId, Page page);

		/**
		 * Removes a page from the persistent layer.
		 * 
		 * @param sessionId
		 *            The session of the page that must be removed
		 * @param pagemap
		 *            The pagemap of the page that must be removed
		 * @param id
		 *            The id of the page.
		 */
		void removePage(String sessionId, String pagemap, int id);

		/**
		 * Stores the page to a persistent layer. The page should be stored under the id and the
		 * version number.
		 * 
		 * @param sessionId
		 * @param page
		 */
		void storePage(String sessionId, Page page);

		/**
		 * The pagestore should cleanup all the pages for that sessionid.
		 * 
		 * @param sessionId
		 */
		void unbind(String sessionId);

		/**
		 * Returns whether the PageStore contains given page.
		 * 
		 * @param sessionId
		 * @param pageMapName
		 * @param pageId
		 * @param pageVersion
		 * @return boolean If the page was found
		 */
		boolean containsPage(String sessionId, String pageMapName, int pageId, int pageVersion);
	}

	protected boolean isPageStoreClustered()
	{
		return pageStore instanceof IClusteredPageStore;
	}

	/**
	 * Marker interface for PageStores that support replication of serialized pages across cluster,
	 * which means that the lastPage attribute of {@link SecondLevelCachePageMap} does not have to
	 * be serialized;
	 * 
	 * @author Matej Knopp
	 */
	public static interface IClusteredPageStore extends IPageStore
	{

	};

	/**
	 * Some PageStores might want to preprocess page before serialization. For example if the
	 * PageStore serializes page, it might keep the serialized page during the request. So when the
	 * pagemap gets serialized (for session replication) in the request thread, the pagestore can
	 * provide the already serialized data.
	 * 
	 * @author Matej Knopp
	 */
	public static interface ISerializationAwarePageStore extends IPageStore
	{
		/**
		 * Process the page before the it gets serialized
		 * 
		 * @param page
		 * @return The Page itself or a SerializedContainer for that page
		 */
		public Serializable prepareForSerialization(Page page);

		/**
		 * This method should restore the given object to the original page.
		 * 
		 * @param serializable
		 * @return Page
		 */
		public Page restoreAfterSerialization(Serializable serializable);
	};

	/**
	 * Page map implementation for this session store.
	 */
	private static final class SecondLevelCachePageMap extends PageMap
	{
		private static final long serialVersionUID = 1L;

		private transient Page lastPage = null;

		// whether the last page instance should be serialized together with the
		// pagemap
		private final boolean serializeLastPage;

		// when the last page is deserialized, it's actually placed here
		// then on first demand, it's postprocessed (if the session store
		// implements
		// ISerializationAwareSessionStore) and set as lastPage
		private Serializable lastPageDeserialized;

		private transient SecondLevelCacheSessionStore sessionStore;

		private IPageStore getPageStore()
		{
			if (sessionStore == null)
			{
				Application app = Application.exists() ? Application.get() : null;
				if (app != null)
				{
					sessionStore = (SecondLevelCacheSessionStore)app.getSessionStore();
				}
			}
			if (sessionStore != null)
			{
				return sessionStore.getStore();
			}
			else
			{
				return null;
			}
		}

		private Page getLastPage()
		{
			if (lastPage == null && lastPageDeserialized != null)
			{
				IPageStore store = getPageStore();
				// initialize lastPage if necessary (we intentionally delay this
				// until the first demand)
				if (store instanceof ISerializationAwarePageStore)
				{
					lastPage = ((ISerializationAwarePageStore)store).restoreAfterSerialization(lastPageDeserialized);
				}
				else if (lastPageDeserialized instanceof Page)
				{
					lastPage = (Page)lastPageDeserialized;
				}
				lastPageDeserialized = null;
			}
			return lastPage;
		}

		private void setLastPage(Page lastPage)
		{
			this.lastPage = lastPage;
			lastPageDeserialized = null;
		}

		/**
		 * Construct.
		 * 
		 * @param sessionStore
		 * @param name
		 */
		private SecondLevelCachePageMap(SecondLevelCacheSessionStore sessionStore, String name)
		{
			super(name);
			this.sessionStore = sessionStore;
			serializeLastPage = sessionStore.isPageStoreClustered() == false;
		}

		public boolean containsPage(int id, int versionNumber)
		{
			Page lastPage = this.lastPage;
			if (lastPage != null && lastPage.getNumericId() == id &&
				lastPage.getCurrentVersionNumber() == versionNumber)
			{
				return true;
			}
			else
			{
				return getStore().containsPage(getSession().getId(), getName(), id, versionNumber);
			}
		}

		/**
		 * @see org.apache.wicket.PageMap#get(int, int)
		 */
		public Page get(int id, int versionNumber)
		{
			HashMap pageMaps = (HashMap)usedPages.get();
			if (pageMaps == null)
			{
				pageMaps = new HashMap();
				usedPages.set(pageMaps);
			}
			IntHashMap pages = (IntHashMap)pageMaps.get(getName());
			if (pages == null)
			{
				pages = new IntHashMap();
				pageMaps.put(getName(), pages);
			}

			// for now i only get by id.
			// does it really make any sense that there are multiply instances
			// of the
			// same page are alive in one session??
			Page page = (Page)pages.get(id);
			if (page != null)
			{
				return page;
			}

			String sessionId = getSession().getId();
			if (getLastPage() != null && getLastPage().getNumericId() == id)
			{
				page = versionNumber != -1 ? getLastPage().getVersion(versionNumber)
					: getLastPage();
				if (page != null)
				{
					// ask the page store if it is ready saving the page.
					getStore().pageAccessed(sessionId, page);
					pages.put(id, page);
					return page;
				}
			}
			if (sessionId != null)
			{
				setLastPage(null);
				page = getStore().getPage(sessionId, getName(), id, versionNumber, -1);
				pages.put(id, page);
				return page;

			}
			return null;
		}

		/**
		 * @see org.apache.wicket.PageMap#put(org.apache.wicket.Page)
		 */
		public void put(Page page)
		{
			if (!page.isPageStateless())
			{
				Session session = getSession();
				String sessionId = session.getId();
				if (sessionId != null && !session.isSessionInvalidated())
				{
					getStore().storePage(sessionId, page);
					setLastPage(page);
					dirty();
				}
			}
		}

		/**
		 * @see org.apache.wicket.PageMap#clear()
		 */
		public void clear()
		{
			super.clear();
			String sessionId = getSession().getId();
			if (sessionId != null)
			{
				getStore().removePage(sessionId, getName(), -1);
			}
		}

		/**
		 * @see org.apache.wicket.PageMap#removeEntry(org.apache.wicket.session.pagemap.IPageMapEntry)
		 */
		public void removeEntry(IPageMapEntry entry)
		{
			String sessionId = getSession().getId();
			if (sessionId != null)
			{
				getStore().removePage(sessionId, getName(), entry.getNumericId());
			}
		}

		private IPageStore getStore()
		{
			return ((SecondLevelCacheSessionStore)Application.get().getSessionStore()).getStore();
		}

		private void writeObject(java.io.ObjectOutputStream s) throws IOException
		{

			s.defaultWriteObject();

			// if the pagestore is not clustered, we need to serialize the
			// lastPage instance
			if (serializeLastPage)
			{
				Serializable page = lastPage;

				IPageStore store = getPageStore();
				if (page != null && store instanceof ISerializationAwarePageStore)
				{
					page = ((ISerializationAwarePageStore)store).prepareForSerialization(lastPage);
				}

				try
				{
					s.writeObject(page);
				}
				catch (Exception e)
				{
					throw new WicketRuntimeException("Failed to serialize " + page.toString(), e);
				}
			}
		}

		private void readObject(java.io.ObjectInputStream s) throws IOException,
			ClassNotFoundException
		{
			s.defaultReadObject();

			// if the pagestore is not clustered, we need to read the lastPage
			// instance
			if (serializeLastPage)
			{
				Serializable page = (Serializable)s.readObject();
				if (page != null)
				{
					lastPageDeserialized = page;
				}
			}
		}
	}

	/**
	 * version manager for this session store.
	 */
	private static final class SecondLevelCachePageVersionManager implements IPageVersionManager
	{
		private static final long serialVersionUID = 1L;

		private short currentVersionNumber;

		private short currentAjaxVersionNumber;

		private short lastAjaxVersionNumber;

		private final Page page;

		private transient boolean versionStarted;

		/**
		 * Construct.
		 * 
		 * @param page
		 */
		public SecondLevelCachePageVersionManager(Page page)
		{
			this.page = page;
		}

		/**
		 * @see org.apache.wicket.version.IPageVersionManager#beginVersion(boolean)
		 */
		public void beginVersion(boolean mergeVersion)
		{
			// this is an hack.. when object is read in. It must ignore the
			// first version bump.
			if (versionStarted)
			{
				return;
			}

			versionStarted = true;
			if (!mergeVersion)
			{
				// TODO: Skip existing versions! (Ask pagestore for last
				// version)
				currentVersionNumber++;
				lastAjaxVersionNumber = currentAjaxVersionNumber;
				currentAjaxVersionNumber = 0;
			}
			else
			{
				if (RequestCycle.get().getRequest() instanceof WebRequest &&
					((WebRequest)RequestCycle.get().getRequest()).isAjax())
				{
					currentAjaxVersionNumber++;
				}
			}
		}

		/**
		 * @see org.apache.wicket.version.IPageVersionManager#componentAdded(org.apache.wicket.Component)
		 */
		public void componentAdded(Component component)
		{
		}

		/**
		 * @see org.apache.wicket.version.IPageVersionManager#componentModelChanging(org.apache.wicket.Component)
		 */
		public void componentModelChanging(Component component)
		{
		}

		/**
		 * @see org.apache.wicket.version.IPageVersionManager#componentRemoved(org.apache.wicket.Component)
		 */
		public void componentRemoved(Component component)
		{
		}

		/**
		 * @see org.apache.wicket.version.IPageVersionManager#componentStateChanging(org.apache.wicket.version.undo.Change)
		 */
		public void componentStateChanging(Change change)
		{
		}

		/**
		 * @see org.apache.wicket.version.IPageVersionManager#endVersion(boolean)
		 */
		public void endVersion(boolean mergeVersion)
		{
			versionStarted = false;
			String sessionId = page.getSession().getId();
			if (sessionId != null)
			{
				page.getSession().touch(page);
			}
		}

		/**
		 * @see org.apache.wicket.version.IPageVersionManager#expireOldestVersion()
		 */
		public void expireOldestVersion()
		{
		}

		/**
		 * @see org.apache.wicket.version.IPageVersionManager#getAjaxVersionNumber()
		 */
		public int getAjaxVersionNumber()
		{
			return currentAjaxVersionNumber;
		}

		/**
		 * @see org.apache.wicket.version.IPageVersionManager#getCurrentVersionNumber()
		 */
		public int getCurrentVersionNumber()
		{
			return currentVersionNumber;
		}

		/**
		 * @see org.apache.wicket.version.IPageVersionManager#getVersion(int)
		 */
		public Page getVersion(int versionNumber)
		{
			if (currentVersionNumber == versionNumber)
			{
				return page;
			}
			return null;
		}

		/**
		 * @see org.apache.wicket.version.IPageVersionManager#getVersions()
		 */
		public int getVersions()
		{
			return 0;
		}

		/**
		 * @see org.apache.wicket.version.IPageVersionManager#ignoreVersionMerge()
		 */
		public void ignoreVersionMerge()
		{
			currentVersionNumber++;
			lastAjaxVersionNumber = currentAjaxVersionNumber;
			currentAjaxVersionNumber = 0;
		}

		/**
		 * @see org.apache.wicket.version.IPageVersionManager#rollbackPage(int)
		 */
		public Page rollbackPage(int numberOfVersions)
		{
			String sessionId = page.getSession().getId();
			if (sessionId != null)
			{
				int versionNumber = currentVersionNumber;
				int ajaxNumber = currentAjaxVersionNumber;
				if (versionStarted)
				{
					versionNumber--;
					ajaxNumber--;
				}

				IPageStore store = ((SecondLevelCacheSessionStore)Application.get()
					.getSessionStore()).getStore();
				// if the number of versions to rollback can be done inside the
				// current page version.
				if (ajaxNumber >= numberOfVersions)
				{
					return store.getPage(sessionId, page.getPageMapName(), page.getNumericId(),
						versionNumber, ajaxNumber - numberOfVersions);
				}
				else
				{
					// else go one page version down.
					versionNumber--;
					// then calculate the previous ajax version by looking at
					// the last ajax number of the previous version.
					ajaxNumber = lastAjaxVersionNumber - (numberOfVersions - ajaxNumber);
					if (ajaxNumber < 0)
					{
						// currently it is not supported to jump over 2
						// pages....
						log.error("trying to rollback to many versions, jumping over 2 page versions is not supported yet.");
						return null;
					}
					return store.getPage(sessionId, page.getPageMapName(), page.getNumericId(),
						versionNumber, ajaxNumber);
				}
			}

			return null;
		}

		private void readObject(java.io.ObjectInputStream s) throws IOException,
			ClassNotFoundException
		{
			s.defaultReadObject();
			// this is an hack.. when object is read in. It must ignore the
			// first version bump.

			// (matej_k) for now, I'm commenting it out. It causes serious
			// trouble with back
			// button, where new versions are not created as they should be
			// johan promised to look at it soon

			// versionStarted = true;
		}
	}

	static final ThreadLocal usedPages = new ThreadLocal();

	/**
	 * @return
	 */
	public static ThreadLocal getUsedPages()
	{
		return usedPages;
	}

	private final IPageStore pageStore;

	/**
	 * Construct.
	 * 
	 * @param application
	 *            The application for this store
	 * 
	 * @param pageStore
	 *            Page store for keeping page versions
	 */
	public SecondLevelCacheSessionStore(Application application, final IPageStore pageStore)
	{
		super(application);

		this.pageStore = pageStore;

		// turn automatic multi window support off by default, as we don't
		// really
		// need to be afraid to run out of history with this implementation.
		// note that the session store is created before Application#init is
		// called, so if users set this setting explicitly, it'll be overridden
		// (and that's exactly what we want: provide a better default, but not
		// forcing people to do away with this feature).
		Application.get().getPageSettings().setAutomaticMultiWindowSupport(false);
	}

	/**
	 * @see org.apache.wicket.protocol.http.HttpSessionStore#createPageMap(java.lang.String,
	 *      org.apache.wicket.Session)
	 */
	public IPageMap createPageMap(String name)
	{
		return new SecondLevelCachePageMap(this, name);
	}

	/**
	 * @see org.apache.wicket.protocol.http.AbstractHttpSessionStore#onEndRequest(org.apache.wicket.Request)
	 */
	public void onEndRequest(Request request)
	{
		super.onEndRequest(request);
		usedPages.set(null);
	}

	/**
	 * @see org.apache.wicket.protocol.http.AbstractHttpSessionStore#destroy()
	 */
	public void destroy()
	{
		super.destroy();
		getStore().destroy();
	}

	/**
	 * @return The store to use
	 */
	public IPageStore getStore()
	{
		return pageStore;
	}

	/**
	 * @see org.apache.wicket.protocol.http.HttpSessionStore#newVersionManager(org.apache.wicket.Page)
	 */
	public IPageVersionManager newVersionManager(Page page)
	{
		return new SecondLevelCachePageVersionManager(page);
	}

	/**
	 * @see org.apache.wicket.session.ISessionStore#setAttribute(org.apache.wicket.Request,
	 *      java.lang.String, java.lang.Object)
	 */
	public void setAttribute(Request request, String name, Object value)
	{
		// ignore all pages, they are stored through the pagemap
		if (!(value instanceof Page))
		{
			super.setAttribute(request, name, value);
		}
	}

	/**
	 * @see org.apache.wicket.protocol.http.AbstractHttpSessionStore#onUnbind(java.lang.String)
	 */
	protected void onUnbind(String sessionId)
	{
		getStore().unbind(sessionId);
	}
}
