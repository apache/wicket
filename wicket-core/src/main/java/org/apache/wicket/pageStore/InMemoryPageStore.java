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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.wicket.Application;
import org.apache.wicket.core.util.lang.WicketObjects;
import org.apache.wicket.page.IManageablePage;
import org.apache.wicket.util.lang.Bytes;

/**
 * A storage of pages in memory.
 */
public class InMemoryPageStore extends AbstractPersistentPageStore
{

	private final Map<String, MemoryData> datas = new ConcurrentHashMap<>();

	private int maxPages;

	/**
	 * @param applicationName
	 *            {@link Application#getName()}
	 * @param maxPages
	 *            max pages per session
	 */
	public InMemoryPageStore(String applicationName, int maxPages)
	{
		super(applicationName);
		this.maxPages = maxPages;
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
		getSessionIdentifier(context, true);

		return true;
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
				.map(page -> new PersistedPage(page.getPageId(), page instanceof SerializedPage ? ((SerializedPage)page).getPageType() : page.getClass().getName(), getSize(page)))
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
		String identifier = getSessionIdentifier(context, create);

		if (!create)
		{
			if (identifier == null) {
				return null;
			} else {
				return datas.get(identifier);
			}
		}

		MemoryData data = new MemoryData();
		MemoryData existing = datas.putIfAbsent(identifier, data);
		return existing != null ? existing : data;
	}

	@Override
	protected void removePersistent(String identifier)
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
}