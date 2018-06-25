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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import org.apache.wicket.Application;
import org.apache.wicket.Session;
import org.apache.wicket.core.util.lang.WicketObjects;
import org.apache.wicket.page.IManageablePage;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Bytes;

/**
 * A storage of pages in memory.
 */
public class InMemoryPageStore implements IPersistentPageStore
{

	/**
	 * A registry of all page instances.
	 */
	private static final ConcurrentMap<String, InMemoryPageStore> IN_MEMORY_STORES = new ConcurrentHashMap<>();

	private static final String KEY = "wicket:InMemoryPageStore";

	private final Map<String, MemoryData> datas = new ConcurrentHashMap<>();

	private String applicationName;

	private int maxPages;

	/**
	 * @param applicationName
	 *            {@link Application#getName()}
	 * @param maxPages
	 *            max pages per session
	 */
	public InMemoryPageStore(String applicationName, int maxPages)
	{
		this.applicationName = Args.notNull(applicationName, "applicationName");
		this.maxPages = maxPages;

		IN_MEMORY_STORES.put(applicationName, this);
	}

	/**
	 * Versioning is not supported.	
	 */
	@Override
	public boolean supportsVersioning()
	{
		return false;
	}
	
	/**
	 * 
	 * 
	 * @return <code>true</code> always
	 */
	@Override
	public boolean canBeAsynchronous(IPageContext context)
	{
		// session attribute must be added here *before* any asynchronous calls
		// when session is no longer available
		getSessionAttribute(context, true);

		return true;
	}

	protected SessionAttribute getSessionAttribute(IPageContext context, boolean create)
	{
		context.bind();
		
		SessionAttribute attribute = context.getSessionAttribute(KEY);
		if (attribute == null && create)
		{
			attribute = new SessionAttribute(applicationName, context.getSessionId());
			context.setSessionAttribute(KEY, attribute);
		}
		return attribute;
	}

	@Override
	public void destroy()
	{
		datas.clear();

		IN_MEMORY_STORES.remove(applicationName);
	}

	@Override
	public IManageablePage getPage(IPageContext context, int id)
	{
		MemoryData data = getMemoryData(context, false);
		if (data == null)
		{
			return null;
		}

		return data.get(id);
	}

	@Override
	public void removePage(IPageContext context, final IManageablePage page)
	{
		MemoryData data = getMemoryData(context, false);

		if (data != null)
		{
			synchronized (data)
			{
				data.remove(page);
			}
		}
	}

	@Override
	public void removeAllPages(IPageContext context)
	{
		MemoryData data = getMemoryData(context, false);

		if (data != null)
		{
			synchronized (data)
			{
				data.removeAll();
			}
		}
	}

	@Override
	public void addPage(IPageContext context, IManageablePage page)
	{
		MemoryData data = getMemoryData(context, true);

		data.add(page, maxPages);
	}

	@Override
	public String getContextIdentifier(IPageContext context)
	{
		return context.getSessionId();
	}

	@Override
	public Set<String> getContextIdentifiers()
	{
		return datas.keySet();
	}

	@Override
	public List<IPersistedPage> getPersistentPages(String sessionIdentifier)
	{
		MemoryData data = datas.get(sessionIdentifier);
		if (data == null)
		{
			return new ArrayList<>();
		}

		synchronized (data)
		{
			return StreamSupport.stream(data.spliterator(), false)
				.map(page -> new PersistedPage(page, getSize(page)))
				.collect(Collectors.toList());
		}
	}

	@Override
	public Bytes getTotalSize()
	{
		int size = 0;

		for (MemoryData data : datas.values())
		{
			synchronized (data)
			{
				for (IManageablePage page : data)
				{
					size += getSize(page);
				}
			}
		}

		return Bytes.bytes(size);
	}

	/**
	 * Get the size of the given page.
	 */
	protected long getSize(IManageablePage page)
	{
		if (page instanceof SerializedPage) {
			return ((SerializedPage)page).getData().length;
		} else {
			return WicketObjects.sizeof(page);
		}
	}

	private MemoryData getMemoryData(IPageContext context, boolean create)
	{
		SessionAttribute attribute = getSessionAttribute(context, create);

		if (!create)
		{
			if (attribute == null) {
				return null;
			} else {
				return datas.get(attribute.identifier);
			}
		}

		MemoryData data = new MemoryData();
		MemoryData existing = datas.putIfAbsent(attribute.identifier, data);
		return existing != null ? existing : data;
	}

	private void removeMemoryData(String identifier)
	{
		datas.remove(identifier);
	}
	
	/**
	 * Data kept in memory.
	 */
	static class MemoryData implements Iterable<IManageablePage>
	{
		private LinkedHashMap<Integer, IManageablePage> pages = new LinkedHashMap<>();

		@Override
		public Iterator<IManageablePage> iterator()
		{
			return pages.values().iterator();
		}

		public synchronized void add(IManageablePage page, int maxPages)
		{
			pages.remove(page.getPageId());
			pages.put(page.getPageId(), page);

			Iterator<IManageablePage> iterator = pages.values().iterator();
			int size = pages.size();
			while (size > maxPages)
			{
				iterator.next();

				iterator.remove();
				size--;
			}
		}

		public void remove(IManageablePage page)
		{
			pages.remove(page.getPageId());
		}

		public void removeAll()
		{
			pages.clear();
		}

		public IManageablePage get(int id)
		{
			IManageablePage page = pages.get(id);

			return page;
		}
	}

	/**
	 * Attribute held in session.
	 */
	static class SessionAttribute implements Serializable, HttpSessionBindingListener
	{

		private final String applicationName;

		/**
		 * The identifier of the session, must not be equal to {@link Session#getId()}, e.g. when
		 * the container changes the id after authorization.
		 */
		public final String identifier;

		public SessionAttribute(String applicationName, String sessionIdentifier)
		{
			this.applicationName = Args.notNull(applicationName, "applicationName");
			this.identifier = Args.notNull(sessionIdentifier, "sessionIdentifier");
		}


		@Override
		public void valueBound(HttpSessionBindingEvent event)
		{
		}

		@Override
		public void valueUnbound(HttpSessionBindingEvent event)
		{
			InMemoryPageStore store = IN_MEMORY_STORES.get(applicationName);
			if (store != null)
			{
				store.removeMemoryData(identifier);
			}
		}
	}
	
	private static class PersistedPage implements IPersistedPage
	{

		private final int id;

		private final String type;

		private final long size;

		public PersistedPage(IManageablePage page, long size)
		{
			this.id = page.getPageId();
			this.type = page instanceof SerializedPage ? ((SerializedPage)page).getPageType() : page.getClass().getName();
			this.size = size;
		}

		@Override
		public int getPageId()
		{
			return id;
		}

		@Override
		public String getPageType()
		{
			return type;
		}

		@Override
		public Bytes getPageSize()
		{
			return Bytes.bytes(size);
		}
	}
}