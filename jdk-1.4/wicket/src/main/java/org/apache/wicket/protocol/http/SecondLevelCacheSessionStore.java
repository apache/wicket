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

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.IPageMap;
import org.apache.wicket.Page;
import org.apache.wicket.PageMap;
import org.apache.wicket.Request;
import org.apache.wicket.Session;
import org.apache.wicket.session.pagemap.IPageMapEntry;
import org.apache.wicket.version.IPageVersionManager;
import org.apache.wicket.version.undo.Change;


/**
 * FIXME document me!
 * 
 * @author jcompagner
 */
public class SecondLevelCacheSessionStore extends HttpSessionStore
{
	/**
	 * This interface is used by the SecondLevelCacheSessionStore so that pages
	 * can be stored to a persistent layer. Implemenation should store the page
	 * that it gets under the id and versionnumber. So that every page version
	 * can be reconstructed when asked for.
	 * 
	 * @see FilePageStore as default implementation.
	 */
	public static interface IPageStore
	{

		/**
		 * 
		 */
		void destroy();

		/**
		 * Restores a page version from the persistent layer
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
		 * This method is called when the page is accessed. A IPageStore
		 * implemenation can block until a save of that page version is done. So
		 * that a specifiek page version is always restoreable.
		 * 
		 * @param sessionId
		 * @param page
		 */
		void pageAccessed(String sessionId, Page page);

		/**
		 * Removes a page from the persistent layer.
		 * 
		 * @param sessionId
		 * @param page
		 */
		void removePage(String sessionId, Page page);

		/**
		 * Stores the page to a persistent layer. The page should be stored
		 * under the id and the version number.
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

	}

	private static final class SecondLevelCachePageMap extends PageMap
	{
		private static final long serialVersionUID = 1L;

		private Page lastPage = null;

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

		public Page get(int id, int versionNumber)
		{
			String sessionId = getSession().getId();
			if (lastPage != null && lastPage.getNumericId() == id)
			{
				Page page = lastPage.getVersion(versionNumber);
				if (page != null)
				{
					// ask the page store if it is ready saving the page.
					getStore().pageAccessed(sessionId, page);
					return page;
				}
			}
			if (sessionId != null)
			{
				// this is really a page request for a default page. (so without
				// an ajax version)
				return getStore().getPage(sessionId, getName(), id, versionNumber, 0);
			}
			return null;
		}

		public void put(Page page)
		{
			if (!page.isPageStateless())
			{
				String sessionId = getSession().getId();
				if (sessionId != null)
				{
					if (lastPage != page && page.getCurrentVersionNumber() == 0)
					{
						// we have to save a new page directly to the file store
						// so that this version is also recoverable.
						getStore().storePage(sessionId, page);
					}
					lastPage = page;
					dirty();
				}
			}
		}

		public void removeEntry(IPageMapEntry entry)
		{
			String sessionId = getSession().getId();
			if (sessionId != null)
			{
				getStore().removePage(sessionId, entry.getPage());
			}
		}

		private IPageStore getStore()
		{
			return ((SecondLevelCacheSessionStore)Application.get().getSessionStore()).getStore();
		}
	}

	private static final class SecondLevelCachePageVersionManager implements IPageVersionManager
	{
		private static final long serialVersionUID = 1L;

		private short currentVersionNumber;

		private short currentAjaxVersionNumber;

		private short lastAjaxVersionNumber;

		private Page page;

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
				return;

			versionStarted = true;
			if (!mergeVersion)
			{
				currentVersionNumber++;
				lastAjaxVersionNumber = currentAjaxVersionNumber;
				currentAjaxVersionNumber = 0;
			}
			else
			{
				currentAjaxVersionNumber++;
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
				IPageStore store = ((SecondLevelCacheSessionStore)Application.get()
						.getSessionStore()).getStore();
				store.storePage(sessionId, page);
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
					return store.getPage(sessionId, page.getPageMap().getName(), page
							.getNumericId(), versionNumber, ajaxNumber - numberOfVersions);
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
						log
								.error("trying to rollback to many versions, jumping over 2 page versions is not supported yet.");
						return null;
					}
					return store.getPage(sessionId, page.getPageMap().getName(), page
							.getNumericId(), versionNumber, ajaxNumber);
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

	private IPageStore pageStore;

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

		// turn automatic multiwindow support off by default, as we don't really
		// need to be afraid to run out of history with this implementation.
		// note that the session store is created before Application#init is
		// called, so if users set this setting explicitly, it'll be overriden
		// (and that's exactly what we want: provide a better default, but not
		// forcing people to do away with this feature).
		Application.get().getPageSettings().setAutomaticMultiWindowSupport(false);
	}

	/**
	 * @see org.apache.wicket.protocol.http.HttpSessionStore#createPageMap(java.lang.String,
	 *      org.apache.wicket.Session)
	 */
	public IPageMap createPageMap(String name, Session session)
	{
		return new SecondLevelCachePageMap(name, session);
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
