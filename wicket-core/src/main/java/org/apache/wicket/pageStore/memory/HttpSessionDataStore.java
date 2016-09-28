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
package org.apache.wicket.pageStore.memory;

import javax.servlet.http.HttpSession;

import org.apache.wicket.Session;
import org.apache.wicket.page.IPageManagerContext;
import org.apache.wicket.pageStore.IDataStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link IDataStore} which stores the pages in the {@link HttpSession}. Uses
 * {@link IDataStoreEvictionStrategy} to keep the memory footprint reasonable.
 *
 * <p>
 * Usage:
 *
 * <pre>
 * <!--@formatter:off-->
 * MyApp#init()
 * {
 *
 * 	setPageManagerProvider(new DefaultPageManagerProvider(this)
 * 	{
 * 		protected IDataStore newDataStore() 
 * 		{ 
 * 			return  new HttpSessionDataStore(pageManagerContext, new PageNumberEvictionStrategy(20));
 * 		}
 * 	}
 * }
 * <!--@formatter:on-->
 * </pre>
 */
public class HttpSessionDataStore implements IDataStore
{
	private static final Logger LOG = LoggerFactory.getLogger(HttpSessionDataStore.class);

	/** the session attribute key. auto-prefixed with application.getSessionAttributePrefix() */
	private static final String PAGE_TABLE_KEY = "page:store:memory";

	private final IPageManagerContext pageManagerContext;

	private final IDataStoreEvictionStrategy evictionStrategy;

	/**
	 * Construct.
	 * 
	 * @param pageManagerContext
	 * @param evictionStrategy
	 */
	public HttpSessionDataStore(IPageManagerContext pageManagerContext,
		IDataStoreEvictionStrategy evictionStrategy)
	{
		this.pageManagerContext = pageManagerContext;
		this.evictionStrategy = evictionStrategy;
	}

	/**
	 * @param sessionId
	 *            Ignored. Only pages from the current http session can be read
	 * @see org.apache.wicket.pageStore.IDataStore#getData(java.lang.String, int)
	 */
	@Override
	public byte[] getData(String sessionId, int pageId)
	{
		PageTable pageTable = getPageTable(false, false);
		byte[] pageAsBytes = null;
		if (pageTable != null)
		{
			pageAsBytes = pageTable.getPage(pageId);
		}

		if (LOG.isDebugEnabled())
		{
			int bytesLength = pageAsBytes != null ? pageAsBytes.length : -1;
			LOG.debug("Loaded '{}' bytes for page with id '{}' in session '{}'",
					bytesLength, pageId, sessionId);
		}


		return pageAsBytes;
	}

	@Override
	public void removeData(String sessionId, int pageId)
	{
		PageTable pageTable = getPageTable(false, true);
		if (pageTable != null)
		{
			byte[] bytes = pageTable.removePage(pageId);

			if (LOG.isDebugEnabled() && bytes != null)
			{
				LOG.debug("Removed page '{}' in session '{}'", pageId, sessionId);
			}
		}
	}

	@Override
	public void removeData(String sessionId)
	{
		PageTable pageTable = getPageTable(false, true);
		if (pageTable != null)
		{
			pageTable.clear();
			LOG.debug("Removed all pages in session '{}'", sessionId);
		}
	}

	@Override
	public void storeData(String sessionId, int pageId, byte[] pageAsBytes)
	{
		PageTable pageTable = getPageTable(true, true);
		if (pageTable != null)
		{
			pageTable.storePage(pageId, pageAsBytes);
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Stored '{}' bytes for page '{}' in session '{}'",
						pageAsBytes.length, pageId, sessionId);
			}
			evictionStrategy.evict(pageTable);
		}
		else
		{
			LOG.error("Cannot store the data for page with id '{}' in session with id '{}'",
				pageId, sessionId);
		}
	}

	@Override
	public void destroy()
	{
		// do nothing
		// this is application lifecycle thread (WicketFilter#destroy())
		// so there is no reachable http session
	}

	@Override
	public boolean isReplicated()
	{
		return true;
	}

	private PageTable getPageTable(boolean create, boolean rewriteToSession)
	{
		PageTable pageTable = null;
		if (Session.exists())
		{
			pageTable = (PageTable)pageManagerContext.getSessionAttribute(PAGE_TABLE_KEY);
			if (pageTable == null && create)
			{
				pageTable = new PageTable();
				pageManagerContext.setSessionAttribute(PAGE_TABLE_KEY, pageTable);
			} else if (rewriteToSession) {
				pageManagerContext.setSessionAttribute(PAGE_TABLE_KEY, pageTable);
			}
		}
		return pageTable;
	}

	@Override
	public final boolean canBeAsynchronous()
	{
		// HttpSessionDataStore needs access to the current http session
		// and this is not possible in AsychronousDataStore
		return false;
	}

}
