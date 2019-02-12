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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.wicket.Application;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.core.util.lang.WicketObjects;
import org.apache.wicket.page.IManageablePage;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.lang.Classes;

/**
 * A storage of pages in memory.
 */
public class InMemoryPageStore extends AbstractPersistentPageStore implements IPersistentPageStore
{

	private final Map<String, MemoryData> datas = new ConcurrentHashMap<>();

	private final Supplier<MemoryData> dataCreator;

	/**
	 * Keep {@code maxPages} for each session.
	 *    
	 * @param applicationName
	 *            {@link Application#getName()}
	 * @param maxPages
	 *            max pages per session
	 */
	public InMemoryPageStore(String applicationName, int maxPages)
	{
		this(applicationName, () -> new CountLimitedData(maxPages));
	}

	/**
	 * Keep page up to {@code maxBytes} for each session.
	 * <p>
	 * All pages added to this store <em>must</em> be {@code SerializedPage}s. You can achieve this by letting
	 * a {@link SerializingPageStore} delegate to this store.
	 * 
	 * @param applicationName
	 *            {@link Application#getName()}
	 * @param maxBytes
	 *            maximum bytes to keep in session
	 */
	public InMemoryPageStore(String applicationName, Bytes maxBytes)
	{
		this(applicationName, () -> new SizeLimitedData(maxBytes));
	}

	InMemoryPageStore(String applicationName, Supplier<MemoryData> dataCreator)
	{
		super(applicationName);
		
		this.dataCreator = dataCreator;
	}

	/**
	 * Versioning is not supported.	
	 */
	@Override
	public boolean supportsVersioning()
	{
		return false;
	}
	
	@Override
	protected IManageablePage getPersistedPage(String sessionIdentifier, int id)
	{
		MemoryData data = getMemoryData(sessionIdentifier, false);
		if (data != null)
		{
			return data.get(id);
		}

		return null;
	}

	@Override
	protected void removePersistedPage(String sessionIdentifier, IManageablePage page)
	{
		MemoryData data = getMemoryData(sessionIdentifier, false);
		if (data != null)
		{
			synchronized (data)
			{
				data.remove(page.getPageId());
			}
		}
	}

	@Override
	protected void removeAllPersistedPages(String sessionIdentifier)
	{
		datas.remove(sessionIdentifier);
	}

	@Override
	protected void addPersistedPage(String sessionIdentifier, IManageablePage page)
	{
		MemoryData data = getMemoryData(sessionIdentifier, true);

		data.add(page);
	}

	@Override
	public Set<String> getSessionIdentifiers()
	{
		return datas.keySet();
	}

	@Override
	public List<IPersistedPage> getPersistedPages(String sessionIdentifier)
	{
		MemoryData data = datas.get(sessionIdentifier);
		if (data == null)
		{
			return new ArrayList<>();
		}

		synchronized (data)
		{
			return StreamSupport.stream(data.spliterator(), false)
				.map(page -> {
					String pageType = page instanceof SerializedPage ? ((SerializedPage)page).getPageType() : Classes.name(page.getClass());
					
					return new PersistedPage(page.getPageId(), pageType, getSize(page));
				})
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
		if (page instanceof SerializedPage)
		{
			return ((SerializedPage)page).getData().length;
		}
		else
		{
			return WicketObjects.sizeof(page);
		}
	}

	private MemoryData getMemoryData(String sessionIdentifier, boolean create)
	{
		if (!create)
		{
			return datas.get(sessionIdentifier);
		}

		MemoryData data = dataCreator.get();
		MemoryData existing = datas.putIfAbsent(sessionIdentifier, data);
		return existing != null ? existing : data;
	}

	/**
	 * Data kept in memory.
	 */
	static abstract class MemoryData implements Iterable<IManageablePage>
	{
		/**
		 * Kept in list instead of map, since non-serialized pages might change their id during a request.
		 */
		List<IManageablePage> pages = new LinkedList<>();

		@Override
		public Iterator<IManageablePage> iterator()
		{
			return pages.iterator();
		}

		public synchronized void add(IManageablePage page)
		{
			remove(page.getPageId());
			
			pages.add(page);
		}

		public synchronized IManageablePage remove(int pageId)
		{
			Iterator<IManageablePage> iterator = pages.iterator();
			while (iterator.hasNext())
			{
				IManageablePage page = iterator.next();
				
				if (page.getPageId() == pageId)
				{
					iterator.remove();
					return page;
				}
			}
			return null;
		}

		public synchronized IManageablePage get(int pageId)
		{
			Iterator<IManageablePage> iterator = pages.iterator();
			while (iterator.hasNext())
			{
				IManageablePage page = iterator.next();
				
				if (page.getPageId() == pageId)
				{
					return page;
				}
			}
			
			return null;
		}
		
		protected void removeOldest() {
			IManageablePage page = pages.iterator().next();
			
			remove(page.getPageId());
		}
	}
	
	/**
	 * Limit pages by count.
	 */
	static class CountLimitedData extends MemoryData
	{

		private int maxPages;

		public CountLimitedData(int maxPages)
		{
			this.maxPages = Args.withinRange(1, Integer.MAX_VALUE, maxPages, "maxPages");
		}
	
		@Override
		public synchronized void add(IManageablePage page)
		{
			super.add(page);
			
			while (pages.size() > maxPages)
			{
				removeOldest();
			}
		}
	}
	
	/**
	 * Limit pages by size.
	 */
	static class SizeLimitedData extends MemoryData
	{
		
		private Bytes maxBytes;
		
		private long size;

		public SizeLimitedData(Bytes maxBytes)
		{
			Args.notNull(maxBytes, "maxBytes");
			
			this.maxBytes = Args.withinRange(Bytes.bytes(1), Bytes.MAX, maxBytes, "maxBytes");
		}
		
		@Override
		public synchronized void add(IManageablePage page)
		{
			if (page instanceof SerializedPage == false)
			{
				throw new WicketRuntimeException("InMemoryPageStore limited by size works with serialized pages only");
			}
			
			super.add(page);
			
			size += ((SerializedPage) page).getData().length;

			while (size > maxBytes.bytes())
			{
				removeOldest();
			}
		}
		
		@Override
		public synchronized IManageablePage remove(int pageId)
		{
			SerializedPage page = (SerializedPage) super.remove(pageId);
			if (page != null)
			{
				size -= ((SerializedPage) page).getData().length;
			}
			return page;
		}
	}
}