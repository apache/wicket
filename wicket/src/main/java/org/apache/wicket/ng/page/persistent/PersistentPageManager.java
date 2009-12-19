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
package org.apache.wicket.ng.page.persistent;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.wicket.ng.page.IManageablePage;
import org.apache.wicket.pageManager.AbstractPageManager;
import org.apache.wicket.pageManager.IPageManagerContext;
import org.apache.wicket.pageManager.RequestAdapter;

public class PersistentPageManager extends AbstractPageManager
{
	private final IPageStore pageStore;
	private final String applicationName;

	public PersistentPageManager(String applicationName, IPageStore pageStore,
		IPageManagerContext context)
	{
		super(context);

		this.applicationName = applicationName;
		this.pageStore = pageStore;

		managers.put(applicationName, this);
	}

	private static Map<String, PersistentPageManager> managers = new ConcurrentHashMap<String, PersistentPageManager>();

	/**
	 * Represents entry for single session. This is stored as session attribute and caches pages
	 * between requests.
	 * 
	 * @author Matej Knopp
	 */
	private static class SessionEntry implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private final String applicationName;
		private final String sessionId;

		public SessionEntry(String applicationName, String sessionId)
		{
			this.applicationName = applicationName;
			this.sessionId = sessionId;
		}

		private IPageStore getPageStore()
		{
			PersistentPageManager manager = managers.get(applicationName);
			if (manager == null)
			{
				throw new IllegalStateException("PageManager for application " + applicationName +
					" not registered.");
			}
			return manager.pageStore;
		}

		/**
		 * Add the page to cached pages if page with same id is not already there
		 * 
		 * @param page
		 */
		private void addPage(IManageablePage page)
		{
			if (page != null)
			{
				for (IManageablePage p : pages)
				{
					if (p.getPageId() == page.getPageId())
					{
						return;
					}
				}
			}
			pages.add(page);
		}

		/**
		 * If the pages are stored in temporary state (after deserialization) this method convert
		 * them to list of "real" pages
		 */
		private void convertAfterReadObjects()
		{
			if (pages == null)
			{
				pages = new ArrayList<IManageablePage>();
			}

			for (Object o : afterReadObject)
			{
				IManageablePage page = getPageStore().convertToPage(o);
				addPage(page);
			}

			afterReadObject = null;
		}

		public synchronized IManageablePage getPage(int id)
		{
			// check if pages are in deserialized state
			if (afterReadObject != null && afterReadObject.isEmpty() == false)
			{
				convertAfterReadObjects();
			}

			// try to find page with same id
			if (pages != null)
			{
				for (IManageablePage page : pages)
				{
					if (page.getPageId() == id)
					{
						return page;
					}
				}
			}

			// not found, ask pagestore for the page
			return getPageStore().getPage(sessionId, id);
		}

		// set the list of pages to remember after the request
		public synchronized void setPages(List<IManageablePage> pages)
		{
			this.pages = new ArrayList<IManageablePage>(pages);
			afterReadObject = null;
		}

		private transient List<IManageablePage> pages;
		private transient List<Object> afterReadObject;

		private void writeObject(java.io.ObjectOutputStream s) throws IOException
		{
			s.defaultWriteObject();

			// prepare for serialization and store the pages
			List<Serializable> l = new ArrayList<Serializable>();
			for (IManageablePage p : pages)
			{
				l.add(getPageStore().prepareForSerialization(sessionId, p));
			}
			s.writeObject(l);
		}

		@SuppressWarnings("unchecked")
		private void readObject(java.io.ObjectInputStream s) throws IOException,
			ClassNotFoundException
		{
			s.defaultReadObject();

			afterReadObject = new ArrayList<Object>();

			List<Serializable> l = (List<Serializable>)s.readObject();

			// convert to temporary state after deserialization (will need to be processed
			// by convertAfterReadObject before the pages can be accessed)
			for (Serializable ser : l)
			{
				afterReadObject.add(getPageStore().restoreAfterSerialization(ser));
			}
		}
	};

	/**
	 * {@link RequestAdapter} for {@link PersistentPageManager}
	 * 
	 * @author Matej Knopp
	 */
	protected class PersitentRequestAdapter extends RequestAdapter
	{
		public PersitentRequestAdapter(IPageManagerContext context)
		{
			super(context);
		}

		@Override
		protected IManageablePage getPage(int id)
		{
			// try to get session entry for this session
			SessionEntry entry = getSessionEntry(false);

			if (entry != null)
			{
				return entry.getPage(id);
			}
			else
			{
				return null;
			}
		}

		private static final String ATTRIBUTE_NAME = "wicket:persistentPageManagerData";

		private SessionEntry getSessionEntry(boolean create)
		{
			SessionEntry entry = (SessionEntry)getSessionAttribute(ATTRIBUTE_NAME);
			if (entry == null && create)
			{
				bind();
				entry = new SessionEntry(applicationName, getSessionId());
			}
			if (entry != null)
			{
				synchronized (entry)
				{
					setSessionAttribute(ATTRIBUTE_NAME, null);
					setSessionAttribute(ATTRIBUTE_NAME, entry);
				}
			}
			return entry;
		}

		@Override
		protected void newSessionCreated()
		{
			// if the session is not temporary bind a session entry to it
			if (getSessionId() != null)
			{
				getSessionEntry(true);
			}
		}

		@Override
		protected void storeTouchedPages(List<IManageablePage> touchedPages)
		{
			if (!touchedPages.isEmpty())
			{
				SessionEntry entry = getSessionEntry(true);
				entry.setPages(touchedPages);
				for (IManageablePage page : touchedPages)
				{
					pageStore.storePage(getSessionId(), page);
				}
			}
		}
	};

	@Override
	protected RequestAdapter newRequestAdapter(IPageManagerContext context)
	{
		return new PersitentRequestAdapter(context);
	}

	@Override
	public boolean supportsVersioning()
	{
		return true;
	}

	@Override
	public void sessionExpired(String sessionId)
	{
		pageStore.unbind(sessionId);
	}

	public void destroy()
	{
		managers.remove(applicationName);
	}

}
