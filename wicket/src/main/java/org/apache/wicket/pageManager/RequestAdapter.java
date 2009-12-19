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
package org.apache.wicket.pageManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ng.page.IManageablePage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Request scoped helper class for {@link IPageManager}.
 * 
 * @author Matej Knopp
 */
public abstract class RequestAdapter
{
	private static final Logger log = LoggerFactory.getLogger(RequestAdapter.class);

	private final IPageManagerContext context;

	private final List<IManageablePage> touchedPages = new ArrayList<IManageablePage>();

	private final List<IManageablePage> pages = new ArrayList<IManageablePage>();

	/**
	 * Construct.
	 * 
	 * @param context
	 *            The page manager context
	 */
	public RequestAdapter(final IPageManagerContext context)
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
	protected abstract IManageablePage getPage(int id);

	/**
	 * Store the list of pages.
	 * 
	 * @param touchedPages
	 */
	protected abstract void storeTouchedPages(List<IManageablePage> touchedPages);

	/**
	 * Notification on new session being created.
	 */
	protected abstract void newSessionCreated();

	/**
	 * Bind the session
	 * 
	 * @see IPageManagerContext#bind()
	 */
	protected void bind()
	{
		context.bind();
	}

	/**
	 * @see IPageManagerContext#setSessionAttribute(String, Serializable)
	 * 
	 * @param key
	 * @param value
	 */
	public void setSessionAttribute(String key, Serializable value)
	{
		context.setSessionAttribute(key, value);
	}

	/**
	 * @see IPageManagerContext#getSessionAttribute(String)
	 * 
	 * @param key
	 * @return the session attribute
	 */
	public Serializable getSessionAttribute(final String key)
	{
		return context.getSessionAttribute(key);
	}

	/**
	 * @see IPageManagerContext#getSessionId()
	 * 
	 * @return session id
	 */
	public String getSessionId()
	{
		return context.getSessionId();
	}

	/**
	 * 
	 * @param id
	 * @return null, if not found
	 */
	private IManageablePage findPage(final int id)
	{
		for (IManageablePage page : pages)
		{
			if (page.getPageId() == id)
			{
				return page;
			}
		}
		return null;
	}

	/**
	 * 
	 * @param page
	 */
	protected void touch(final IManageablePage page)
	{
		if (findPage(page.getPageId()) == null)
		{
			pages.add(page);
		}

		for (IManageablePage p : touchedPages)
		{
			if (p.getPageId() == page.getPageId())
			{
				return;
			}
		}
		touchedPages.add(page);
	}

	/**
	 * 
	 */
	protected void commitRequest()
	{
		for (IManageablePage page : pages)
		{
			try
			{
				page.detach();
			}
			catch (Exception e)
			{
				log.error("Error detaching page", e);
			}
		}

		// store pages that are not stateless
		if (touchedPages.isEmpty() == false)
		{
			List<IManageablePage> statefulPages = new ArrayList<IManageablePage>(
				touchedPages.size());
			for (IManageablePage page : touchedPages)
			{
				if (!page.isPageStateless())
				{
					statefulPages.add(page);
				}
			}

			if (statefulPages.isEmpty() == false)
			{
				storeTouchedPages(statefulPages);
			}
		}
	}
}