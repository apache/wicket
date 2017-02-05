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
package org.apache.wicket.core.request.handler;

import org.apache.wicket.Application;
import org.apache.wicket.core.request.mapper.IPageSource;
import org.apache.wicket.core.request.mapper.StalePageException;
import org.apache.wicket.page.IPageManager;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.IRequestMapper;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.io.IClusterable;
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
public class PageProvider implements IPageProvider, IClusterable
{
	private static final long serialVersionUID = 1L;

	private final Integer renderCount;

	private final Integer pageId;

	private IPageSource pageSource;

	private Class<? extends IRequestablePage> pageClass;

	private PageParameters pageParameters;

	private Provision provision = new Provision();

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

		provision = new Provision().resolve(page);
		pageId = page.getPageId();
		renderCount = page.getRenderCount();
	}

	private Provision getResolvedProvision()
	{
		if (!provision.wasResolved())

			provision.resolve(pageId, pageClass, pageParameters, renderCount);

		return provision;
	}

	/**
	 * @see IPageProvider#getPageInstance()
	 */
	@Override
	public IRequestablePage getPageInstance()
	{
		Provision resolvedProvision = getResolvedProvision();

		if (!resolvedProvision.didResolveToPage() && !resolvedProvision.doesProvideNewPage())
		{
			throw new IllegalStateException("The configured page provider can't resolve a page.");
		}
		return resolvedProvision.getPage();
	}

	/**
	 * @see IPageProvider#getPageParameters()
	 */
	@Override
	public PageParameters getPageParameters()
	{
		if (pageParameters != null)
		{
			return pageParameters;
		}
		else if (hasPageInstance())
		{
			return getPageInstance().getPageParameters();
		}
		else
		{
			return null;
		}
	}

	/**
	 * @return negates {@link PageProvider#hasPageInstance()}
	 * @deprecated use {@link PageProvider#hasPageInstance()} negation instead
	 */
	@Override
	public boolean isNewPageInstance()
	{
		return !hasPageInstance();
	}

	/**
	 * If this provider returns existing page, regardless if it was already created by PageProvider
	 * itself or is or can be found in the data store. The only guarantee is that by calling
	 * {@link PageProvider#getPageInstance()} this provider will return an existing instance and no
	 * page will be created.
	 * 
	 * @return if provides an existing page
	 */
	@Override
	public final boolean hasPageInstance()
	{
		if (provision.wasResolved() || pageId != null)
		{
			return getResolvedProvision().didResolveToPage();
		}
		else
			return false;
	}

	/**
	 * Returns whether or not the page instance held by this provider has been instantiated by the
	 * provider.
	 * 
	 * @return {@code true} iff the page instance held by this provider was instantiated by the
	 *         provider
	 */
	@Override
	public final boolean doesProvideNewPage()
	{
		if (!this.provision.wasResolved())
		{
			throw new IllegalStateException("Page instance not yet resolved");
		}
		return getResolvedProvision().doesProvideNewPage();
	}

	/**
	 * @see org.apache.wicket.core.request.handler.IPageProvider#wasExpired()
	 */
	@Override
	public boolean wasExpired()
	{
		return pageId != null && getResolvedProvision().didFailToFindStoredPage();
	}

	/**
	 * @see IPageProvider#getPageClass()
	 */
	@Override
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


	/**
	 * Detaches the page if it has been loaded (that means either
	 * {@link #PageProvider(IRequestablePage)} constructor has been used or
	 * {@link #getPageInstance()} has been called).
	 */
	@Override
	public void detach()
	{
		provision.detach();
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
		if (this.provision.wasResolved())
		{
			throw new IllegalStateException(
				"A provision was already been done. The provider can be forcefully detached or a new one needs to be used to provide using this page source.");
		}
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
	@Override
	public Integer getPageId()
	{
		return pageId;
	}

	@Override
	public Integer getRenderCount()
	{
		return renderCount;
	}

	@Override
	public String toString()
	{
		return "PageProvider{" + "renderCount=" + renderCount + ", pageId=" + pageId
			+ ", pageClass=" + pageClass + ", pageParameters=" + pageParameters + '}';
	}

	private class Provision
	{
		transient IRequestablePage page;
		boolean resolved;
		boolean failedToFindStoredPage;

		IRequestablePage getPage()
		{
			if (page == null && doesProvideNewPage())

				page = getPageSource().newPageInstance(pageClass, pageParameters);

			return page;
		}

		boolean wasResolved()
		{
			return resolved;
		}

		boolean didResolveToPage()
		{
			return page != null;
		}

		boolean doesProvideNewPage()
		{
			return (pageId == null || failedToFindStoredPage) && pageClass != null;
		}

		boolean didFailToFindStoredPage()
		{
			return failedToFindStoredPage;
		}

		Provision resolve(IRequestablePage page)
		{
			this.page = page;

			resolved = true;

			return this;
		}

		Provision resolve(Integer pageId, Class<? extends IRequestablePage> pageClass,
			PageParameters pageParameters, Integer renderCount)
		{

			if (pageId != null)
			{
				IRequestablePage stored = getPageSource().getPageInstance(pageId);
				if (stored != null && (pageClass == null || pageClass.equals(stored.getClass())))
				{

					page = stored;

					if (renderCount != null && page.getRenderCount() != renderCount)
						throw new StalePageException(page);
				}

				failedToFindStoredPage = page == null;
			}


			resolved = true;

			return this;
		}

		void detach()
		{
			if (page != null)
			{
				page.detach();
			}
		}

	}
}
