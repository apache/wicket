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

import wicket.Application;
import wicket.Component;
import wicket.IRedirectListener;
import wicket.IRequestTarget;
import wicket.Page;
import wicket.PageParameters;
import wicket.RequestCycle;
import wicket.RequestListenerInterface;
import wicket.RestartResponseAtInterceptPageException;
import wicket.RestartResponseException;
import wicket.Session;
import wicket.WicketRuntimeException;
import wicket.authorization.AuthorizationException;
import wicket.authorization.UnauthorizedActionException;
import wicket.markup.MarkupException;
import wicket.markup.html.INewBrowserWindowListener;
import wicket.protocol.http.portlet.pages.ExceptionErrorPortletPage;
import wicket.protocol.http.request.WebErrorCodeResponseTarget;
import wicket.request.AbstractRequestCycleProcessor;
import wicket.request.IRequestCodingStrategy;
import wicket.request.RequestParameters;
import wicket.request.target.component.BookmarkablePageRequestTarget;
import wicket.request.target.component.ExpiredPageClassRequestTarget;
import wicket.request.target.component.PageRequestTarget;
import wicket.request.target.component.listener.RedirectPageRequestTarget;
import wicket.settings.IApplicationSettings;
import wicket.settings.IApplicationSettings.UnexpectedExceptionDisplay;
import wicket.util.string.Strings;

/**
 * Shared functionality for portlet request cycle processors.
 * 
 * @author Janne Hietam&auml;ki
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
	 * @see wicket.request.compound.IRequestTargetResolverStrategy#resolve(wicket.RequestCycle,
	 *      wicket.request.RequestParameters)
	 */
	public IRequestTarget resolve(final RequestCycle requestCycle,
			final RequestParameters requestParameters)
	{

		if (requestParameters.getBookmarkablePageClass() != null)
		{
			return resolveBookmarkablePage(requestCycle, requestParameters);
		}

		final String componentPath = requestParameters.getComponentPath();
		if (componentPath != null)
		{
			final Session session = requestCycle.getSession();
			final Page<?> page = session.getPage(requestParameters.getPageMapName(), componentPath,
					requestParameters.getVersionNumber());

			// Does page exist?
			if (page != null)
			{
				// Set page on request
				requestCycle.getRequest().setPage(page);

				// see whether this resolves to a component call or just the
				// page
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
	@Override
	@SuppressWarnings("unchecked")
	public void respond(RuntimeException e, RequestCycle requestCycle)
	{
		// If application doesn't want debug info showing up for users
		final Session session = requestCycle.getSession();
		final Application application = session.getApplication();
		final IApplicationSettings settings = application.getApplicationSettings();
		final Page<?> responsePage = requestCycle.getResponsePage();

		Page<?> override = onRuntimeException(responsePage, e);
		if (override != null)
		{
			// we do not want to redirect - we want to inline the error output
			// and preserve the url so when the refresh button is pressed we
			// rerun the code that caused the error
			requestCycle.setRedirect(false);

			throw new RestartResponseException(override);
		}
		else if (e instanceof AuthorizationException)
		{
			// are authorization exceptions always thrown before the real
			// render?
			// else we need to make a page (see below) or set it hard to a
			// redirect.
			Class<? extends Page> accessDeniedPageClass = application.getApplicationSettings()
					.getAccessDeniedPage();

			throw new RestartResponseAtInterceptPageException(accessDeniedPageClass);
		}
		else if (settings.getUnexpectedExceptionDisplay() != UnexpectedExceptionDisplay.SHOW_NO_EXCEPTION_PAGE)
		{
			// we do not want to redirect - we want to inline the error output
			// and preserve the url so when the refresh button is pressed we
			// rerun the code that caused the error
			requestCycle.setRedirect(false);

			// figure out which error page to show
			Class<? extends Page> internalErrorPageClass = application.getApplicationSettings()
					.getInternalErrorPage();
			Class responseClass = responsePage != null ? responsePage.getClass() : null;

			if (responseClass != internalErrorPageClass
					&& settings.getUnexpectedExceptionDisplay() == UnexpectedExceptionDisplay.SHOW_INTERNAL_ERROR_PAGE)
			{
				throw new RestartResponseException(internalErrorPageClass);
			}
			else if (responseClass != ExceptionErrorPortletPage.class)
			{
				// Show full details
				throw new RestartResponseException(new ExceptionErrorPortletPage(e, responsePage));
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
	@Override
	protected IRequestCodingStrategy newRequestCodingStrategy()
	{
		return new PortletRequestCodingStrategy();
	}

	/**
	 * This method is called when a runtime exception is thrown, just before the
	 * actual handling of the runtime exception. This implemention passes the
	 * call through to
	 * {@link RequestCycle#onRuntimeException(Page, RuntimeException)}. Note
	 * that if you override this method or provide a whole new implementation of
	 * {@link IExceptionResponseStrategy} alltogether,
	 * {@link RequestCycle#onRuntimeException(Page, RuntimeException)} will not
	 * be supported.
	 * 
	 * @param page
	 *            Any page context where the exception was thrown
	 * @param e
	 *            The exception
	 * @return Any error page to redirect to
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected Page onRuntimeException(final Page page, final RuntimeException e)
	{
		return RequestCycle.get().onRuntimeException(page, e);
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
	@Override
	@SuppressWarnings("unchecked")
	protected IRequestTarget resolveBookmarkablePage(final RequestCycle requestCycle,
			final RequestParameters requestParameters)
	{
		String bookmarkablePageClass = requestParameters.getBookmarkablePageClass();
		Session session = requestCycle.getSession();
		Application application = session.getApplication();
		Class<? extends Page> pageClass;
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
	@Override
	@SuppressWarnings("unchecked")
	protected IRequestTarget resolveHomePageTarget(final RequestCycle requestCycle,
			final RequestParameters requestParameters)
	{
		Session session = requestCycle.getSession();
		Application application = session.getApplication();
		try
		{
			// Get the home page class
			Class<? extends Page> homePageClass = application.getHomePage();
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
	@Override
	@SuppressWarnings("unchecked")
	protected IRequestTarget resolveListenerInterfaceTarget(final RequestCycle requestCycle,
			final Page page, final String componentPath, final String interfaceName,
			final RequestParameters requestParameters)
	{
		if (interfaceName.equals(IRedirectListener.INTERFACE.getName()))
		{
			return new RedirectPageRequestTarget(page);
		}
		else if (interfaceName.equals(INewBrowserWindowListener.INTERFACE.getName()))
		{
			return INewBrowserWindowListener.INTERFACE.newRequestTarget(page, page,
					INewBrowserWindowListener.INTERFACE, requestParameters);
		}
		else
		{
			// Get the listener interface we need to call
			final RequestListenerInterface listener = RequestListenerInterface
					.forName(interfaceName);
			if (listener == null)
			{
				throw new WicketRuntimeException(
						"Attempt to access unknown request listener interface " + interfaceName);
			}

			// Get component
			final String pageRelativeComponentPath = Strings.afterFirstPathComponent(componentPath,
					Component.PATH_SEPARATOR);

			Component component = null;
			if (Strings.isEmpty(pageRelativeComponentPath))
			{
				component = page;
			}
			else
			{
				component = page.get(pageRelativeComponentPath);
			}
			if (!component.isEnableAllowed())
			{
				throw new UnauthorizedActionException(component, Component.ENABLE);
			}

			// Ask the request listener interface object to create a request
			// target
			return listener.newRequestTarget(page, component, listener, requestParameters);
		}
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
	@Override
	@SuppressWarnings("unchecked")
	protected IRequestTarget resolveRenderedPage(final RequestCycle requestCycle,
			final RequestParameters requestParameters)
	{
		final String componentPath = requestParameters.getComponentPath();
		final Session session = requestCycle.getSession();
		final Page page = session.getPage(requestParameters.getPageMapName(), componentPath,
				requestParameters.getVersionNumber());

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
}