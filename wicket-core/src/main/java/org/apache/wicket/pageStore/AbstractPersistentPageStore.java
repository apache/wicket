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

import java.io.File;
import java.io.Serializable;
import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import org.apache.wicket.Session;
import org.apache.wicket.page.IManageablePage;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for stores that keep an identifier in the session only, while holding the actual pages
 * in a secondary persistent storage.
 * 
 * @see #getSessionIdentifier(IPageContext, boolean)
 */
public abstract class AbstractPersistentPageStore implements IPersistentPageStore
{
	private static final String KEY = "wicket:persistentPageStore";

	private static final Logger log = LoggerFactory.getLogger(AbstractPersistentPageStore.class);

	/**
	 * A cache that holds all persistent page stores, the key is the application name suffixed with the page store implementation class.
	 */
	private static final ConcurrentMap<String, AbstractPersistentPageStore> STORES = new ConcurrentHashMap<>();

	private final String storeKey;

	protected AbstractPersistentPageStore(String applicationName)
	{
		this.storeKey = Args.notNull(applicationName, "applicationName") + ":" + getClass().getSimpleName();

		if (STORES.containsKey(storeKey))
		{
			throw new IllegalStateException(
				"Store with key '" + storeKey + "' already exists.");
		}
		STORES.put(storeKey, this);
	}

	@Override
	public void destroy()
	{
		STORES.remove(storeKey);
	}

	@Override
	public boolean canBeAsynchronous(IPageContext context)
	{
		// session attribute must be added here *before* any asynchronous calls
		// when session is no longer available
		getSessionIdentifier(context, true);

		return true;
	}

	@Override
	public IManageablePage getPage(IPageContext context, int id)
	{
		String identifier = getSessionIdentifier(context, false);
		if (identifier == null)
		{
			return null;
		}

		return getPersistedPage(identifier, id);
	}

	protected abstract IManageablePage getPersistedPage(String identifier, int id);
	
	@Override
	public void removePage(IPageContext context, IManageablePage page)
	{
		String identifier = getSessionIdentifier(context, false);
		if (identifier == null)
		{
			return;
		}
		
		removePersistedPage(identifier, page);
	}

	protected abstract void removePersistedPage(String identifier, IManageablePage page);

	@Override
	public void removeAllPages(IPageContext context)
	{
		String identifier = getSessionIdentifier(context, false);
		if (identifier == null)
		{
			return;
		}
		
		removeAllPersistedPages(identifier);
	}

	protected abstract void removeAllPersistedPages(String identifier);

	@Override
	public void addPage(IPageContext context, IManageablePage page)
	{
		String identifier = getSessionIdentifier(context, true);
		
		addPersistedPage(identifier, page);
	}

	protected abstract void addPersistedPage(String identifier, IManageablePage page);

	/**
	 * Get the distinct and stable identifier for the given context.
	 * 
	 * @param context the context to identify
	 * @param create should a new identifier be created if not there already
	 */
	private String getSessionIdentifier(IPageContext context, boolean create)
	{
		SessionAttribute attribute = context.getSessionAttribute(KEY);
		if (attribute == null && create)
		{
			attribute = new SessionAttribute(storeKey, createSessionIdentifier(context));
			context.setSessionAttribute(KEY, attribute);
		}
		
		if (attribute == null) {
			return null;
		}
		return attribute.identifier;
	}

	/**
	 * Create an identifier for the given context.
	 * <p>
	 * Default implementation uses {@link IPageContext#getSessionId()}.
	 * 
	 * @param context context
	 * @return identifier for the sseion
	 */
	protected String createSessionIdentifier(IPageContext context) {
		return context.getSessionId();
	}

	/**
	 * Attribute held in session.
	 */
	private static class SessionAttribute implements Serializable, HttpSessionBindingListener
	{

		private final String storeKey;

		/**
		 * The identifier of the session, must not be equal to {@link Session#getId()}, e.g. when
		 * the container changes the id after authorization.
		 */
		public final String identifier;

		public SessionAttribute(String storeKey, String sessionIdentifier)
		{
			this.storeKey = Args.notNull(storeKey, "storeKey");
			this.identifier = Args.notNull(sessionIdentifier, "sessionIdentifier");
		}


		@Override
		public void valueBound(HttpSessionBindingEvent event)
		{
		}

		@Override
		public void valueUnbound(HttpSessionBindingEvent event)
		{
			AbstractPersistentPageStore store = STORES.get(storeKey);
			if (store == null)
			{
				log.warn(
					"Cannot remove data '{}' because disk store '{}' is no longer present.", identifier, storeKey);
			}
			else
			{
				store.removeAllPersistedPages(identifier);
			}
		}
	}

	public class LastModifiedComparator implements Comparator<File>
	{

		@Override
		public int compare(File f1, File f2)
		{
			return (int)(f2.lastModified() - f1.lastModified());
		}

	}

	@Override
	public String getSessionIdentifier(IPageContext context)
	{
		return getSessionIdentifier(context, true);
	}

	protected static class PersistedPage implements IPersistedPage
	{
		private int pageId;

		private String pageType;

		private long pageSize;

		public PersistedPage(int pageId, String pageType, long pageSize)
		{
			this.pageId = pageId;
			this.pageType = pageType;
			this.pageSize = pageSize;
		}

		@Override
		public int getPageId()
		{
			return pageId;
		}

		@Override
		public Bytes getPageSize()
		{
			return Bytes.bytes(pageSize);
		}

		@Override
		public String getPageType()
		{
			return pageType;
		}

	}
}