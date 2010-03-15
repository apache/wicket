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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.wicket.page.IManageablePage;

/**
 * 
 */
public class PersistentPageManager extends AbstractPageManager
{
	private static Map<String, PersistentPageManager> managers = new ConcurrentHashMap<String, PersistentPageManager>();

	private final IPageStore pageStore;

	private final String applicationName;

	/**
	 * Construct.
	 * 
	 * @param applicationName
	 * @param pageStore
	 * @param context
	 */
	public PersistentPageManager(final String applicationName, final IPageStore pageStore,
		final IPageManagerContext context)
	{
		super(context);

		this.applicationName = applicationName;
		this.pageStore = pageStore;

		if (managers.containsKey(applicationName))
		{
			throw new IllegalStateException("Manager for application with key '" + applicationName +
				"' already exists.");
		}
		managers.put(applicationName, this);
	}

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

		private transient List<IManageablePage> pages;
		private transient List<Object> afterReadObject;

		/**
		 * Construct.
		 * 
		 * @param applicationName
		 * @param sessionId
		 */
		public SessionEntry(String applicationName, String sessionId)
		{
			this.applicationName = applicationName;
			this.sessionId = sessionId;
		}

		/**
		 * 
		 * @return page store
		 */
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
		 * 
		 * @param id
		 * @return null, if not found
		 */
		private IManageablePage findPage(int id)
		{
			for (IManageablePage p : pages)
			{
				if (p.getPageId() == id)
				{
					return p;
				}
			}
			return null;
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
				if (findPage(page.getPageId()) != null)
				{
					return;
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

		/**
		 * 
		 * @param id
		 * @return manageable page
		 */
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
				IManageablePage page = findPage(id);
				if (page != null)
				{
					return page;
				}
			}

			// not found, ask pagestore for the page
			return getPageStore().getPage(sessionId, id);
		}

		/**
		 * set the list of pages to remember after the request
		 * 
		 * @param pages
		 */
		public synchronized void setPages(final List<IManageablePage> pages)
		{
			this.pages = new ArrayList<IManageablePage>(pages);
			afterReadObject = null;
		}

		/**
		 * 
		 * @param s
		 * @throws IOException
		 */
		private void writeObject(final ObjectOutputStream s) throws IOException
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

		/**
		 * 
		 * @param s
		 * @throws IOException
		 * @throws ClassNotFoundException
		 */
		@SuppressWarnings("unchecked")
		private void readObject(final ObjectInputStream s) throws IOException,
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
	}

	/**
	 * {@link RequestAdapter} for {@link PersistentPageManager}
	 * 
	 * @author Matej Knopp
	 */
	protected class PersitentRequestAdapter extends RequestAdapter
	{
		private static final String ATTRIBUTE_NAME = "wicket:persistentPageManagerData";

		private String getAttributeName()
		{
			return ATTRIBUTE_NAME + " - " + applicationName;
		}

		/**
		 * Construct.
		 * 
		 * @param context
		 */
		public PersitentRequestAdapter(IPageManagerContext context)
		{
			super(context);
		}

		/**
		 * @see org.apache.wicket.pageStore.RequestAdapter#getPage(int)
		 */
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

		/**
		 * 
		 * @param create
		 * @return Session Entry
		 */
		private SessionEntry getSessionEntry(boolean create)
		{
			SessionEntry entry = (SessionEntry)getSessionAttribute(getAttributeName());
			if (entry == null && create)
			{
				bind();
				entry = new SessionEntry(applicationName, getSessionId());
			}
			if (entry != null)
			{
				synchronized (entry)
				{
					setSessionAttribute(getAttributeName(), null);
					setSessionAttribute(getAttributeName(), entry);
				}
			}
			return entry;
		}

		/**
		 * @see org.apache.wicket.pageStore.RequestAdapter#newSessionCreated()
		 */
		@Override
		protected void newSessionCreated()
		{
			// if the session is not temporary bind a session entry to it
			if (getSessionId() != null)
			{
				getSessionEntry(true);
			}
		}

		/**
		 * @see org.apache.wicket.pageStore.RequestAdapter#storeTouchedPages(java.util.List)
		 */
		@Override
		protected void storeTouchedPages(final List<IManageablePage> touchedPages)
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
	}

	/**
	 * @see org.apache.wicket.pageStore.AbstractPageManager#newRequestAdapter(org.apache.wicket.pageStore.IPageManagerContext)
	 */
	@Override
	protected RequestAdapter newRequestAdapter(IPageManagerContext context)
	{
		return new PersitentRequestAdapter(context);
	}

	/**
	 * @see org.apache.wicket.pageStore.AbstractPageManager#supportsVersioning()
	 */
	@Override
	public boolean supportsVersioning()
	{
		return true;
	}

	/**
	 * @see org.apache.wicket.pageStore.AbstractPageManager#sessionExpired(java.lang.String)
	 */
	@Override
	public void sessionExpired(String sessionId)
	{
		pageStore.unbind(sessionId);
	}

	/**
	 * @see org.apache.wicket.pageStore.IPageManager#destroy()
	 */
	public void destroy()
	{
		managers.remove(applicationName);
	}
}
