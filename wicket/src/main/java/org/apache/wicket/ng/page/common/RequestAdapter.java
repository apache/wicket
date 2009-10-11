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
package org.apache.wicket.ng.page.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ng.page.ManageablePage;
import org.apache.wicket.ng.page.PageManager;
import org.apache.wicket.ng.page.PageManagerContext;

/**
 * Request scoped helper class for {@link PageManager}.
 * 
 * @author Matej Knopp
 */
public abstract class RequestAdapter
{
	private final PageManagerContext context;

	public RequestAdapter(PageManagerContext context)
	{
		this.context = context;
	}

	/**
	 * Returns the page with specified id. The page is then cached by {@link RequestAdapter} during
	 * the rest of request processing.
	 * 
	 * @param id
	 * @return page instance or <code>null</code> if the page does not exist.
	 */
	protected abstract ManageablePage getPage(int id);

	/**
	 * Store the list of pages.
	 * 
	 * @param touchedPages
	 */
	protected abstract void storeTouchedPages(List<ManageablePage> touchedPages);

	/**
	 * Notification on new session being created.
	 */
	protected abstract void newSessionCreated();

	/**
	 * Bind the session
	 */
	protected void bind()
	{
		context.bind();
	}

	public void setSessionAttribute(String key, Serializable value)
	{
		context.setSessionAttribute(key, value);
	}

	public Serializable getSessionAttribute(String key)
	{
		return context.getSessionAttribute(key);
	}

	public String getSessionId()
	{
		return context.getSessionId();
	}

	protected final ManageablePage getPageInternal(int id)
	{
		ManageablePage page = findPage(id);
		if (page == null)
		{
			page = getPage(id);
		}
		if (page != null)
		{
			pages.add(page);
		}
		return page;
	}

	private ManageablePage findPage(int id)
	{
		for (ManageablePage page : pages)
		{
			if (page.getPageId() == id)
			{
				return page;
			}
		}
		return null;
	}

	protected void touch(ManageablePage page)
	{
		if (findPage(page.getPageId()) == null)
		{
			pages.add(page);
		}
		for (ManageablePage p : touchedPages)
		{
			if (p.getPageId() == page.getPageId())
			{
				return;
			}
		}
		touchedPages.add(page);
	}

	protected void commitRequest()
	{
		for (ManageablePage page : pages)
		{
			try
			{
				page.detach();
			}
			catch (Exception e)
			{
				AbstractPageManager.logger.error("Error detaching page", e);
			}
		}

		// store pages that are not stateless
		List<ManageablePage> statefulPages = new ArrayList<ManageablePage>(touchedPages.size());
		for (ManageablePage page : touchedPages)
		{
			if (!page.isPageStateless())
			{
				statefulPages.add(page);
			}
		}
		storeTouchedPages(statefulPages);
	}

	List<ManageablePage> touchedPages = new ArrayList<ManageablePage>();
	List<ManageablePage> pages = new ArrayList<ManageablePage>();
}