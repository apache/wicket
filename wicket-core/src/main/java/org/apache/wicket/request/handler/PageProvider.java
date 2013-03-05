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
package org.apache.wicket.request.handler;

import org.apache.wicket.Application;
import org.apache.wicket.page.IPageManager;
import org.apache.wicket.pageStore.IPageStore;
import org.apache.wicket.protocol.http.PageExpiredException;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.IRequestMapper;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.mapper.IPageSource;
import org.apache.wicket.request.mapper.StalePageException;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.lang.Args;

/**
 * Provides page instance for request handlers. Each of the constructors has just enough information
 * to get existing or create new page instance. Requesting or creating page instance is deferred
 * until {@link #getPageInstance()} is called.
 * <p>
 * Purpose of this class is to reduce complexity of both {@link IRequestMapper}s and
 * {@link IRequestHandler}s. {@link IRequestMapper} examines the URL, gathers all relevant
 * information about the page in the URL (combination of page id, page class, page parameters and
 * render count), creates {@link PageProvider} object and creates a {@link IRequestHandler} instance
 * that can use the {@link PageProvider} to access the page.
 * <p>
 * Apart from simplifying {@link IRequestMapper}s and {@link IRequestHandler}s {@link PageProvider}
 * also helps performance because creating or obtaining page from {@link IPageManager} is delayed
 * until the {@link IRequestHandler} actually requires the page.
 * 
 * @author Matej Knopp
 */
public class PageProvider implements IPageProvider, IIntrospectablePageProvider
{
	private final Integer renderCount;

	private final Integer pageId;

	private IPageSource pageSource;

	private IRequestablePage pageInstance;
	private boolean pageInstanceIsFresh;

	private Class<? extends IRequestablePage> pageClass;

	private PageParameters pageParameters;

	/**
	 * Creates a new page provider object. Upon calling of {@link #getPageInstance()} this provider
	 * will return page instance with specified id.
	 * 
	 * @param pageId
	 * @param renderCount
	 *            optional argument
	 */
	public PageProvider(final Integer pageId, final Integer renderCount)
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
	public PageProvider(final Integer pageId, final Class<? extends IRequestablePage> pageClass,
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
	public PageProvider(final Integer pageId, final Class<? extends IRequestablePage> pageClass,
		final PageParameters pageParameters, final Integer renderCount)
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
	public PageProvider(final Class<? extends IRequestablePage> pageClass,
		final PageParameters pageParameters)
	{
		setPageClass(pageClass);
		if (pageParameters != null)
		{
			setPageParameters(pageParameters);
		}
		pageId = null;
		renderCount = null;
	}

	/**
	 * Creates a new page provider object. Upon calling of {@link #getPageInstance()} this provider
	 * will return new instance of page with specified class.
	 * 
	 * @param pageClass
	 */
	public PageProvider(Class<? extends IRequestablePage> pageClass)
	{
		this(pageClass, null);
	}

	/**
	 * Creates a new page provider object. Upon calling of {@link #getPageInstance()} this provider
	 * will return the given page instance.
	 * 
	 * @param page
	 */
	public PageProvider(IRequestablePage page)
	{
		Args.notNull(page, "page");

		pageInstance = page;
		pageId = page.getPageId();
		renderCount = page.getRenderCount();
	}

	/**
	 * @see org.apache.wicket.request.handler.IPageProvider#getPageInstance()
	 */
	public IRequestablePage getPageInstance()
	{
		if (pageInstance == null)
		{
			resolvePageInstance(pageId, pageClass, pageParameters, renderCount);

			if (pageInstance == null)
			{
				throw new PageExpiredException("Page with id '" + pageId + "' has expired.");
			}
		}
		return pageInstance;
	}

	/**
	 * @see org.apache.wicket.request.handler.IPageProvider#getPageParameters()
	 */
	public PageParameters getPageParameters()
	{
		if (pageParameters != null)
		{
			return pageParameters;
		}
		else if (isNewPageInstance() == false)
		{
			return pageInstance.getPageParameters();
		}
		else
		{
			return null;
		}
	}

	/**
	 * The page instance is new only if there is no cached instance or the data stores doesn't have
	 * a page with that id with the same {@linkplain #pageClass}.
	 * 
	 * @see org.apache.wicket.request.handler.IPageProvider#isNewPageInstance()
	 */
	public boolean isNewPageInstance()
	{
		boolean isNew = pageInstance == null;
		if (isNew && pageId != null)
		{
			IRequestablePage storedPageInstance = getStoredPage(pageId);
			if (storedPageInstance != null)
			{
				pageInstance = storedPageInstance;
				isNew = false;
			}
		}

		return isNew;
	}

	/**
	 * @see org.apache.wicket.request.handler.IPageProvider#getPageClass()
	 */
	public Class<? extends IRequestablePage> getPageClass()
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

	protected IPageSource getPageSource()
	{
		if (pageSource != null)
		{
			return pageSource;
		}
		if (Application.exists())
		{
			return Application.get().getMapperContext();
		}
		else
		{
			throw new IllegalStateException(
				"No application is bound to current thread. Call setPageSource() to manually assign pageSource to this provider.");
		}
	}

	private void resolvePageInstance(Integer pageId, Class<? extends IRequestablePage> pageClass,
		PageParameters pageParameters, Integer renderCount)
	{
		IRequestablePage page = null;

		boolean freshCreated = false;

		if (pageId != null)
		{
			page = getStoredPage(pageId);
		}

		if (page == null)
		{
			if (pageClass != null)
			{
				page = getPageSource().newPageInstance(pageClass, pageParameters);
				freshCreated = true;
			}
		}

		if (page != null && !freshCreated)
		{
			if (renderCount != null && page.getRenderCount() != renderCount)
			{
				throw new StalePageException(page);
			}
		}

		pageInstanceIsFresh = freshCreated;
		pageInstance = page;
	}

	/**
	 * Looks up a page by id from the {@link IPageStore}. <br/>
	 * If {@linkplain #pageClass} is specified then compares it against the stored instance class
	 * and returns the found instance only if they match.
	 * 
	 * @param pageId
	 *            the id of the page to look for.
	 * @return the found page instance by id.
	 */
	private IRequestablePage getStoredPage(final int pageId)
	{
		IRequestablePage storedPageInstance = getPageSource().getPageInstance(pageId);
		if (storedPageInstance != null)
		{
			if (pageClass == null || pageClass.equals(storedPageInstance.getClass()))
			{
				pageInstance = storedPageInstance;
				pageInstanceIsFresh = false;
				if (renderCount != null && pageInstance.getRenderCount() != renderCount)
				{
					throw new StalePageException(pageInstance);
				}
			}
			else
			{
				// the found page class doesn't match the requested one
				storedPageInstance = null;
			}
		}
		return storedPageInstance;
	}

	/**
	 * Detaches the page if it has been loaded (that means either
	 * {@link #PageProvider(IRequestablePage)} constructor has been used or
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
	 * If the {@link PageProvider} is used outside request thread (thread that does not have
	 * application instance assigned) it is necessary to specify a {@link IPageSource} instance so
	 * that {@link PageProvider} knows how to get a page instance.
	 * 
	 * @param pageSource
	 */
	public void setPageSource(IPageSource pageSource)
	{
		this.pageSource = pageSource;
	}

	/**
	 * 
	 * @param pageClass
	 */
	private void setPageClass(Class<? extends IRequestablePage> pageClass)
	{
		Args.notNull(pageClass, "pageClass");

		this.pageClass = pageClass;
	}

	/**
	 * 
	 * @param pageParameters
	 */
	protected void setPageParameters(PageParameters pageParameters)
	{
		this.pageParameters = pageParameters;
	}

	/**
	 * 
	 * @return page id
	 */
	public Integer getPageId()
	{
		return pageId;
	}

	public Integer getRenderCount()
	{
		return renderCount;
	}

	/**
	 * Checks whether or not the provider has a page instance. This page instance might have been
	 * passed to this page provider directly or it may have been instantiated or retrieved from the
	 * page store.
	 * 
	 * @return {@code true} iff page instance has been created or retrieved
	 */
	public final boolean hasPageInstance()
	{
		if (pageInstance == null && pageId != null)
		{
			// attempt to load a stored page instance from the page store
			getStoredPage(pageId);
		}
		return pageInstance != null;
	}

	/**
	 * Returns whether or not the page instance held by this provider has been instantiated by the
	 * provider.
	 * 
	 * @throws IllegalStateException
	 *             if this method is called and the provider does not yet have a page instance, ie
	 *             if {@link #getPageInstance()} has never been called on this provider
	 * @return {@code true} iff the page instance held by this provider was instantiated by the
	 *         provider
	 */
	public final boolean isPageInstanceFresh()
	{
		if (!hasPageInstance())
		{
			throw new IllegalStateException("Page instance not yet resolved");
		}
		return pageInstanceIsFresh;
	}
}
