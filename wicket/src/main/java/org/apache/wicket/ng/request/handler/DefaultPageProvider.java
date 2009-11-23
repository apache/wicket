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
package org.apache.wicket.ng.request.handler;

import org.apache.wicket.ng.Application;
import org.apache.wicket.ng.Page;
import org.apache.wicket.ng.page.PageManager;
import org.apache.wicket.ng.request.RequestHandler;
import org.apache.wicket.ng.request.RequestMapper;
import org.apache.wicket.ng.request.component.PageExpiredException;
import org.apache.wicket.ng.request.component.PageParameters;
import org.apache.wicket.ng.request.component.RequestablePage;
import org.apache.wicket.ng.request.mapper.PageSource;
import org.apache.wicket.ng.request.mapper.StalePageException;
import org.apache.wicket.util.lang.Checks;

/**
 * Provides page instance for request handlers. Each of the constructors has just enough information
 * to get existing or create new page instance. Requesting or creating page instance is deferred
 * until {@link #getPageInstance()} is called.
 * <p>
 * Purpose of this class is to reduce complexity of both {@link RequestMapper}s and
 * {@link RequestHandler}s. {@link RequestMapper} examines the URL, gathers all relevant information
 * about the page in the URL (combination of page id, page class, page parameters and render count),
 * creates {@link DefaultPageProvider} object and creates a {@link RequestHandler} instance that can
 * use the {@link DefaultPageProvider} to access the page.
 * <p>
 * Apart from simplifying {@link RequestMapper}s and {@link RequestHandler}s
 * {@link DefaultPageProvider} also helps performance because creating or obtaining page from
 * {@link PageManager} is delayed until the {@link RequestHandler} actually requires the page.
 * 
 * @author Matej Knopp
 */
public class DefaultPageProvider implements PageProvider
{
	private Integer renderCount;
	private PageSource pageSource;
	private RequestablePage pageInstance;
	private Class<? extends RequestablePage> pageClass;
	private Integer pageId;
	private PageParameters pageParameters;

	/**
	 * Creates a new page provider object. Upon calling of {@link #getPageInstance()} this provider
	 * will return page instance with specified id.
	 * 
	 * @param pageId
	 * @param renderCount
	 *            optional argument
	 */
	public DefaultPageProvider(int pageId, Integer renderCount)
	{
		this.pageId = pageId;
		this.renderCount = renderCount;
	}

	/**
	 * Creates a new page provider object. Upon calling of {@link #getPageInstance()} this provider
	 * will return page instance with specified id if it exists and it's class matches pageClass. If
	 * none of these is true new page instance will be created.
	 * 
	 * @param pageId
	 * @param pageClass
	 * @param renderCount
	 *            optional argument
	 */
	public DefaultPageProvider(int pageId, Class<? extends RequestablePage> pageClass,
		Integer renderCount)
	{
		this(pageId, pageClass, new PageParameters(), renderCount);
	}

	/**
	 * Creates a new page provider object. Upon calling of {@link #getPageInstance()} this provider
	 * will return page instance with specified id if it exists and it's class matches pageClass. If
	 * none of these is true new page instance will be created.
	 * 
	 * @param pageId
	 * @param pageClass
	 * @param pageParameters
	 * @param renderCount
	 *            optional argument
	 */
	public DefaultPageProvider(int pageId, Class<? extends RequestablePage> pageClass,
		PageParameters pageParameters, Integer renderCount)
	{
		this.pageId = pageId;
		setPageClass(pageClass);
		setPageParameters(pageParameters);
		this.renderCount = renderCount;
	}

	/**
	 * Creates a new page provider object. Upon calling of {@link #getPageInstance()} this provider
	 * will return new instance of page with specified class.
	 * 
	 * @param pageClass
	 * @param pageParameters
	 */
	public DefaultPageProvider(Class<? extends RequestablePage> pageClass,
		PageParameters pageParameters)
	{
		setPageClass(pageClass);
		if (pageParameters != null)
		{
			setPageParameters(pageParameters);
		}
	}

	/**
	 * Creates a new page provider object. Upon calling of {@link #getPageInstance()} this provider
	 * will return new instance of page with specified class.
	 * 
	 * @param pageClass
	 */
	public DefaultPageProvider(Class<? extends RequestablePage> pageClass)
	{
		this(pageClass, new PageParameters());
	}

	/**
	 * Creates a new page provider object. Upon calling of {@link #getPageInstance()} this provider
	 * will return the given page instance.
	 * 
	 * @param page
	 */
	public DefaultPageProvider(RequestablePage page)
	{
		Checks.argumentNotNull(page, "page");

		pageInstance = page;
	}

	/**
	 * @see org.apache.wicket.ng.request.handler.PageProvider#getPageInstance()
	 */
	public RequestablePage getPageInstance()
	{
		if (pageInstance == null)
		{
			pageInstance = getPageInstance(pageId, pageClass, pageParameters, renderCount);
		}
		if (pageInstance == null)
		{
			throw new PageExpiredException("Page expired.");
		}
		touchPageInstance();
		return pageInstance;
	}

	/**
	 * @see org.apache.wicket.ng.request.handler.PageProvider#getPageParameters()
	 */
	public PageParameters getPageParameters()
	{
		if (pageParameters != null)
		{
			return pageParameters;
		}
		else
		{
			return getPageInstance().getPageParameters();
		}
	}

	/**
	 * @see org.apache.wicket.ng.request.handler.PageProvider#getPageClass()
	 */
	public Class<? extends RequestablePage> getPageClass()
	{
		if (pageClass != null)
		{
			return pageClass;
		}
		else
		{
			return getPageInstance().getClass();
		}
	}

	protected boolean prepareForRenderNewPage()
	{
		return false;
	}

	protected PageSource getPageSource()
	{
		if (pageSource != null)
		{
			return pageSource;
		}
		if (Application.exists())
		{
			return Application.get().getEncoderContext();
		}
		else
		{
			throw new IllegalStateException(
				"No application is bound to current thread. Call setPageSource() to manually assign pageSource to this provider.");
		}
	}

	private RequestablePage getPageInstance(Integer pageId,
		Class<? extends RequestablePage> pageClass, PageParameters pageParameters,
		Integer renderCount)
	{
		RequestablePage page = null;

		boolean freshCreated = false;

		if (pageId != null)
		{
			page = getPageSource().getPageInstance(pageId);
			if (page != null && pageClass != null && page.getClass().equals(pageClass) == false)
			{
				page = null;
			}
			else if (page != null && pageParameters != null)
			{
				page.getPageParameters().assign(pageParameters);
			}
		}
		if (page == null)
		{
			if (pageClass != null)
			{
				page = getPageSource().newPageInstance(pageClass, pageParameters);
				freshCreated = true;
				if (prepareForRenderNewPage() && page instanceof Page)
				{
					((Page)page).prepareForRender(false);
				}
			}
		}

		if (page != null && !freshCreated)
		{
			if (renderCount != null && page.getRenderCount() != renderCount)
			{
				throw new StalePageException(page);
			}
		}

		return page;
	}

	/**
	 * Detaches the page if it has been loaded (that means either
	 * {@link #PageProvider(RequestablePage)} constructor has been used or
	 * {@link #getPageInstance()} has been called).
	 */
	public void detach()
	{
		if (pageInstance != null)
		{
			pageInstance.detach();
		}
	}

	/**
	 * If the {@link DefaultPageProvider} is used outside request thread (thread that does not have
	 * application instance assigned) it is necessary to specify a {@link PageSource} instance so
	 * that {@link DefaultPageProvider} knows how to get a page instance.
	 * 
	 * @param pageSource
	 */
	public void setPageSource(PageSource pageSource)
	{
		this.pageSource = pageSource;
	}

	private void setPageClass(Class<? extends RequestablePage> pageClass)
	{
		Checks.argumentNotNull(pageClass, "pageClass");

		this.pageClass = pageClass;
	}

	private void setPageParameters(PageParameters pageParameters)
	{
		Checks.argumentNotNull(pageParameters, "pageParameters");

		this.pageParameters = pageParameters;
	}

	private void touchPageInstance()
	{
		// If there is application accessible from current thread touch
		// the page instance
		if (Application.exists())
		{
			Application.get().getPageManager().touchPage(pageInstance);
		}
	}
}
