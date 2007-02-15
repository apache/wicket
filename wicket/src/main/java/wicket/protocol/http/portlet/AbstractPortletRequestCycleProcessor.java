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
package wicket.protocol.http.portlet;


import javax.servlet.http.HttpServletResponse;

import wicket.AccessStackPageMap;
import wicket.Application;
import wicket.Component;
import wicket.IPageFactory;
import wicket.IPageMap;
import wicket.IRedirectListener;
import wicket.IRequestTarget;
import wicket.Page;
import wicket.PageParameters;
import wicket.RequestCycle;
import wicket.RequestListenerInterface;
import wicket.RestartResponseAtInterceptPageException;
import wicket.Session;
import wicket.WicketRuntimeException;
import wicket.AccessStackPageMap.Access;
import wicket.authorization.AuthorizationException;
import wicket.authorization.UnauthorizedActionException;
import wicket.markup.MarkupException;
import wicket.protocol.http.portlet.pages.ExceptionErrorPortletPage;
import wicket.protocol.http.request.WebErrorCodeResponseTarget;
import wicket.request.AbstractRequestCycleProcessor;
import wicket.request.IRequestCodingStrategy;
import wicket.request.RequestParameters;
import wicket.request.target.basic.EmptyRequestTarget;
import wicket.request.target.component.BookmarkablePageRequestTarget;
import wicket.request.target.component.ExpiredPageClassRequestTarget;
import wicket.request.target.component.IPageRequestTarget;
import wicket.request.target.component.PageRequestTarget;
import wicket.request.target.component.listener.RedirectPageRequestTarget;
import wicket.request.target.resource.SharedResourceRequestTarget;
import wicket.settings.IExceptionSettings;
import wicket.util.string.Strings;

/**
 * Shared functionality for portlet request cycle processors.
 * 
 * @author Janne Hietam&auml;ki
 * 
 */
public abstract class AbstractPortletRequestCycleProcessor extends AbstractRequestCycleProcessor
{
	/**
	 * Construct.
	 */
	public AbstractPortletRequestCycleProcessor()
	{
		super();
	}

	/**
	 * @see wicket.request.AbstractRequestCycleProcessor#resolve(wicket.RequestCycle,
	 *      wicket.request.RequestParameters)
	 */
	public final IRequestTarget resolve(final RequestCycle requestCycle,
			final RequestParameters requestParameters)
	{
		if (requestParameters.getBookmarkablePageClass() != null)
		{
			return resolveBookmarkablePage(requestCycle, requestParameters);
		}

		// See whether this request points to a rendered page
		final String path = requestParameters.getPath();

		if (requestParameters.getComponentPath() != null)
		{
			// marks whether or not we will be processing this request
			boolean processRequest = true;
			synchronized (requestCycle.getSession())
			{
				// we need to check if this request has been flagged as
				// process-only-if-path-is-active and if so make sure this
				// condition
				// is met


				if (requestParameters.isOnlyProcessIfPathActive())
				{
					// this request has indeed been flagged as
					// process-only-if-path-is-active

					Session session = Session.get();
					IPageMap pageMap = session.pageMapForName(requestParameters.getPageMapName(),
							false);
					if (pageMap == null)
					{
						// requested pagemap no longer exists - ignore this
						// request
						processRequest = false;
					}
					else if (pageMap instanceof AccessStackPageMap)
					{
						AccessStackPageMap accessStackPm = (AccessStackPageMap)pageMap;
						if (accessStackPm.getAccessStack().size() > 0)
						{
							final Access access = (Access)accessStackPm.getAccessStack().peek();

							final int pageId = Integer
									.parseInt(Strings.firstPathComponent(requestParameters
											.getComponentPath(), Component.PATH_SEPARATOR));

							if (pageId != access.getId())
							{
								// the page is no longer the active page
								// - ignore this request
								processRequest = false;
							}
							else
							{
								final int version = requestParameters.getVersionNumber();
								if (version != Page.LATEST_VERSION
										&& version != access.getVersion())
								{
									// version is no longer the active version -
									// ignore this request
									processRequest = false;
								}
							}
						}
					}
					else
					{
						// TODO also this should work.. also forward port to
						// 2.0!!!
					}
				}
			}
			if (processRequest)
			{
				return resolveRenderedPage(requestCycle, requestParameters);
			}
			else
			{
				return EmptyRequestTarget.getInstance();
			}
		}
		// see whether this request points to a shared resource
		else if (requestParameters.getResourceKey() != null)
		{
			return resolveSharedResource(requestCycle, requestParameters);
		}

		if (requestParameters.getPath() == null && requestParameters.getComponentPath() == null)
		{
			return resolveHomePageTarget(requestCycle, requestParameters);
		}
		throw new WicketRuntimeException("Unable to resolve request target " + requestParameters);
	}

	/**
	 * @see wicket.request.IRequestCycleProcessor#respond(java.lang.RuntimeException,
	 *      wicket.RequestCycle)
	 */
	public final void respond(RuntimeException e, RequestCycle requestCycle)
	{
		// If application doesn't want debug info showing up for users
		final Session session = requestCycle.getSession();
		final Application application = session.getApplication();
		final IExceptionSettings settings = application.getExceptionSettings();
		final Page responsePage = requestCycle.getResponsePage();

		Page override = onRuntimeException(responsePage, e);
		if (override != null)
		{
			requestCycle.setResponsePage(override);
		}
		else if (e instanceof AuthorizationException)
		{
			// are authorization exceptions always thrown before the real
			// render?
			// else we need to make a page (see below) or set it hard to a
			// redirect.
			Class accessDeniedPageClass = application.getApplicationSettings()
					.getAccessDeniedPage();

			throw new RestartResponseAtInterceptPageException(accessDeniedPageClass);
		}
		else if (settings.getUnexpectedExceptionDisplay() != IExceptionSettings.SHOW_NO_EXCEPTION_PAGE)
		{
			Class internalErrorPageClass = application.getApplicationSettings()
					.getInternalErrorPage();
			Class responseClass = responsePage != null ? responsePage.getClass() : null;

			if (responseClass != internalErrorPageClass
					&& settings.getUnexpectedExceptionDisplay() == IExceptionSettings.SHOW_INTERNAL_ERROR_PAGE)
			{
				// Show internal error page
				final IPageFactory pageFactory;
				IRequestTarget requestTarget = requestCycle.getRequestTarget();
				if (requestTarget instanceof IPageRequestTarget)
				{
					pageFactory = session.getPageFactory(((IPageRequestTarget)requestTarget)
							.getPage());
				}
				else
				{
					pageFactory = session.getPageFactory();
				}
				requestCycle.setResponsePage(pageFactory.newPage(internalErrorPageClass));
			}
			else if (responseClass != ExceptionErrorPortletPage.class)
			{
				// Show full details
				requestCycle.setResponsePage(new ExceptionErrorPortletPage(e, responsePage));
			}
			else
			{
				// give up while we're ahead!
				throw new WicketRuntimeException("Internal Error: Could not render error page "
						+ internalErrorPageClass, e);
			}
		}
	}

	/**
	 * @see wicket.request.AbstractRequestCycleProcessor#newRequestCodingStrategy()
	 */
	protected IRequestCodingStrategy newRequestCodingStrategy()
	{
		return new PortletRequestCodingStrategy();
	}

	/**
	 * Resolves to a bookmarkable page target.
	 * 
	 * @param requestCycle
	 *            the current request cycle
	 * @param requestParameters
	 *            the request parameters object
	 * @return the bookmarkable page as a request target
	 */
	protected IRequestTarget resolveBookmarkablePage(final RequestCycle requestCycle,
			final RequestParameters requestParameters)
	{
		String bookmarkablePageClass = requestParameters.getBookmarkablePageClass();
		Session session = requestCycle.getSession();
		Application application = session.getApplication();
		Class pageClass;
		try
		{
			pageClass = session.getClassResolver().resolveClass(bookmarkablePageClass);
		}
		catch (RuntimeException e)
		{
			return new WebErrorCodeResponseTarget(HttpServletResponse.SC_NOT_FOUND,
					"Unable to load Bookmarkable Page");
		}

		try
		{
			PageParameters params = new PageParameters(requestParameters.getParameters());
			return new BookmarkablePageRequestTarget(requestParameters.getPageMapName(), pageClass,
					params);
		}
		catch (RuntimeException e)
		{
			throw new WicketRuntimeException("Unable to instantiate Page class: "
					+ bookmarkablePageClass + ". See below for details.", e);
		}
	}

	/**
	 * Resolves to a home page target.
	 * 
	 * @param requestCycle
	 *            the current request cycle.
	 * @param requestParameters
	 *            the request parameters object
	 * @return the home page as a request target
	 */
	protected IRequestTarget resolveHomePageTarget(final RequestCycle requestCycle,
			final RequestParameters requestParameters)
	{
		Session session = requestCycle.getSession();
		Application application = session.getApplication();
		try
		{
			// Get the home page class
			Class homePageClass = application.getHomePage();
			PageParameters parameters = new PageParameters(requestParameters.getParameters());
			BookmarkablePageRequestTarget homepageTarget = new BookmarkablePageRequestTarget(
					homePageClass, parameters);

			return homepageTarget;
		}
		catch (MarkupException e)
		{
			// Markup exception should pass without modification. They show
			// a nice error page
			throw e;
		}
		catch (WicketRuntimeException e)
		{
			throw new WicketRuntimeException("Could not create home page", e);
		}
	}

	/**
	 * Resolves the RequestTarget for the given interface. This method can be
	 * overriden if some special interface needs to resolve to its own target.
	 * 
	 * @param requestCycle
	 *            The current RequestCycle object
	 * @param page
	 *            The page object which holds the component for which this
	 *            interface is called on.
	 * @param componentPath
	 *            The component path for looking up the component in the page.
	 * @param interfaceName
	 *            The interface to resolve.
	 * @param requestParameters
	 * @return The RequestTarget that was resolved
	 */
	protected IRequestTarget resolveListenerInterfaceTarget(final RequestCycle requestCycle,
			final Page page, final String componentPath, final String interfaceName,
			final RequestParameters requestParameters)
	{

		if (interfaceName.equals(IRedirectListener.INTERFACE.getName()))
		{
			return new RedirectPageRequestTarget(page);
		}

		// Get the listener interface we need to call
		final RequestListenerInterface listener = RequestListenerInterface.forName(interfaceName);
		if (listener == null)
		{
			throw new WicketRuntimeException(
					"Attempt to access unknown request listener interface " + interfaceName);
		}

		// Get component
		final String pageRelativeComponentPath = Strings.afterFirstPathComponent(componentPath,
				Component.PATH_SEPARATOR);
		if (Strings.isEmpty(pageRelativeComponentPath))
		{
			// We have an interface that is not a redirect, but no
			// component... that must be wrong
			throw new WicketRuntimeException("When trying to call " + listener
					+ ", a component must be provided");
		}
		final Component component = page.get(pageRelativeComponentPath);
		if (!component.isEnableAllowed())
		{
			throw new UnauthorizedActionException(component, Component.ENABLE);
		}

		// Ask the request listener interface object to create a request
		// target
		return listener.newRequestTarget(page, component, listener, requestParameters);
	}

	/**
	 * Resolves to a page target that was previously rendered. Optionally
	 * resolves to a component call target, which is a specialization of a page
	 * target. If no corresponding page could be found, a expired page target
	 * will be returned.
	 * 
	 * @param requestCycle
	 *            the current request cycle
	 * @param requestParameters
	 *            the request parameters object
	 * @return the previously rendered page as a request target
	 */
	protected IRequestTarget resolveRenderedPage(final RequestCycle requestCycle,
			final RequestParameters requestParameters)
	{
		final String componentPath = requestParameters.getComponentPath();
		final Session session = requestCycle.getSession();

		final PortletPage page = (PortletPage)session.getPage(requestParameters.getPageMapName(),
				componentPath, requestParameters.getVersionNumber());

		// Does page exist?
		if (page != null)
		{
			// Set page on request
			requestCycle.getRequest().setPage(page);

			// see whether this resolves to a component call or just the page
			final String interfaceName = requestParameters.getInterfaceName();
			if (interfaceName != null)
			{
				return resolveListenerInterfaceTarget(requestCycle, page, componentPath,
						interfaceName, requestParameters);
			}
			else
			{
				return new PageRequestTarget(page);
			}
		}
		else
		{
			// Page was expired from session, probably because backtracking
			// limit was reached
			return new ExpiredPageClassRequestTarget();
		}
	}

	/**
	 * Resolves to a shared resource target.
	 * 
	 * @param requestCycle
	 *            the current request cycle
	 * @param requestParameters
	 *            the request parameters object
	 * @return the shared resource as a request target
	 */
	protected IRequestTarget resolveSharedResource(final RequestCycle requestCycle,
			final RequestParameters requestParameters)
	{
		String resourceKey = requestParameters.getResourceKey();
		return new SharedResourceRequestTarget(requestParameters);
	}
}