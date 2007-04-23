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
package org.apache.wicket.request;

import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.IRedirectListener;
import org.apache.wicket.IRequestTarget;
import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.RequestListenerInterface;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.Session;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.authorization.AuthorizationException;
import org.apache.wicket.authorization.UnauthorizedActionException;
import org.apache.wicket.markup.MarkupException;
import org.apache.wicket.markup.html.INewBrowserWindowListener;
import org.apache.wicket.markup.html.pages.ExceptionErrorPage;
import org.apache.wicket.protocol.http.PageExpiredException;
import org.apache.wicket.protocol.http.request.WebErrorCodeResponseTarget;
import org.apache.wicket.protocol.http.request.WebExternalResourceRequestTarget;
import org.apache.wicket.request.target.IEventProcessor;
import org.apache.wicket.request.target.component.BookmarkableListenerInterfaceRequestTarget;
import org.apache.wicket.request.target.component.BookmarkablePageRequestTarget;
import org.apache.wicket.request.target.component.PageRequestTarget;
import org.apache.wicket.request.target.component.listener.RedirectPageRequestTarget;
import org.apache.wicket.request.target.resource.SharedResourceRequestTarget;
import org.apache.wicket.settings.IExceptionSettings;
import org.apache.wicket.util.string.Strings;


/**
 * Default abstract implementation of {@link IRequestCycleProcessor}.
 * 
 * @author eelcohillenius
 */
public abstract class AbstractRequestCycleProcessor implements IRequestCycleProcessor
{
	/** request coding strategy to use. */
	private IRequestCodingStrategy requestCodingStrategy;

	/**
	 * Construct.
	 */
	public AbstractRequestCycleProcessor()
	{
	}

	/**
	 * @see org.apache.wicket.request.IRequestCycleProcessor#getRequestCodingStrategy()
	 */
	public IRequestCodingStrategy getRequestCodingStrategy()
	{
		if (requestCodingStrategy == null)
		{
			requestCodingStrategy = newRequestCodingStrategy();
		}
		return requestCodingStrategy;
	}

	/**
	 * @see org.apache.wicket.request.IRequestCycleProcessor#processEvents(org.apache.wicket.RequestCycle)
	 */
	public void processEvents(RequestCycle requestCycle)
	{
		IRequestTarget target = requestCycle.getRequestTarget();

		if (target instanceof IEventProcessor)
		{
			Application.get().logEventTarget(target);
			((IEventProcessor)target).processEvents(requestCycle);
		}
	}

	/**
	 * @see org.apache.wicket.request.IRequestCycleProcessor#respond(org.apache.wicket.RequestCycle)
	 */
	public void respond(RequestCycle requestCycle)
	{
		IRequestTarget requestTarget = requestCycle.getRequestTarget();
		if (requestTarget != null)
		{
			Application.get().logResponseTarget(requestTarget);
			requestTarget.respond(requestCycle);
		}
	}

	/**
	 * @see org.apache.wicket.request.IRequestCycleProcessor#respond(java.lang.RuntimeException,
	 *      org.apache.wicket.RequestCycle)
	 */
	public void respond(RuntimeException e, RequestCycle requestCycle)
	{
		// If application doesn't want debug info showing up for users
		final Application application = Application.get();
		final IExceptionSettings settings = application.getExceptionSettings();
		final Page responsePage = requestCycle.getResponsePage();

		Page override = onRuntimeException(responsePage, e);
		if (override != null)
		{
			throw new RestartResponseException(override);
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
		else if (e instanceof PageExpiredException)
		{
			Class pageExpiredErrorPageClass = application.getApplicationSettings()
					.getPageExpiredErrorPage();
			throw new RestartResponseException(pageExpiredErrorPageClass);
		}
		else if (settings.getUnexpectedExceptionDisplay() != IExceptionSettings.SHOW_NO_EXCEPTION_PAGE)
		{
			// we do not want to redirect - we want to inline the error output
			// and preserve the url so when the refresh button is pressed we
			// rerun the code that caused the error
			requestCycle.setRedirect(false);

			// figure out which error page to show
			Class internalErrorPageClass = application.getApplicationSettings()
					.getInternalErrorPage();
			Class responseClass = responsePage != null ? responsePage.getClass() : null;

			if (responseClass != internalErrorPageClass
					&& settings.getUnexpectedExceptionDisplay() == IExceptionSettings.SHOW_INTERNAL_ERROR_PAGE)
			{
				throw new RestartResponseException(internalErrorPageClass);
			}
			else if (responseClass != ExceptionErrorPage.class)
			{
				// Show full details
				throw new RestartResponseException(new ExceptionErrorPage(e, responsePage));
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
	 * Creates a new request coding strategy instance. this is (typically)
	 * called once at the first time {@link #getRequestCodingStrategy()} is
	 * called.
	 * 
	 * @return a new request coding strategy
	 */
	protected abstract IRequestCodingStrategy newRequestCodingStrategy();

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
	protected IRequestTarget resolveBookmarkablePage(final RequestCycle requestCycle,
			final RequestParameters requestParameters)
	{
		String bookmarkablePageClass = requestParameters.getBookmarkablePageClass();
		Session session = requestCycle.getSession();
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
			if (requestParameters.getComponentPath() != null
					&& requestParameters.getInterfaceName() != null)
			{
				final String componentPath = requestParameters.getComponentPath();
				final Page page = session.getPage(requestParameters.getPageMapName(),
						componentPath, requestParameters.getVersionNumber());

				if (page != null && page.getClass() == pageClass)
				{
					return resolveListenerInterfaceTarget(requestCycle, page, componentPath,
							requestParameters.getInterfaceName(), requestParameters);
				}
				else
				{
					return new BookmarkableListenerInterfaceRequestTarget(requestParameters
							.getPageMapName(), pageClass, params, requestParameters
							.getComponentPath(), requestParameters.getInterfaceName());
				}
			}
			else
			{
				return new BookmarkablePageRequestTarget(requestParameters.getPageMapName(),
						pageClass, params);
			}
		}
		catch (RuntimeException e)
		{
			throw new WicketRuntimeException("Unable to instantiate Page class: "
					+ bookmarkablePageClass + ". See below for details.", e);
		}
	}

	/**
	 * Resolves to an external resource.
	 * 
	 * @param requestCycle
	 *            The current request cycle
	 * @return The external resource request target
	 */
	protected IRequestTarget resolveExternalResource(RequestCycle requestCycle)
	{
		// Get the relative URL we need for loading the resource from
		// the servlet context
		// NOTE: we NEED to put the '/' in front as otherwise some versions
		// of application servers (e.g. Jetty 5.1.x) will fail for requests
		// like '/mysubdir/myfile.css'
		final String url = '/' + requestCycle.getRequest().getRelativeURL();
		return new WebExternalResourceRequestTarget(url);
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
			// and create a dummy target for looking up whether the home page is
			// mounted
			BookmarkablePageRequestTarget homepageTarget = new BookmarkablePageRequestTarget(
					homePageClass, parameters);
			IRequestCodingStrategy requestCodingStrategy = requestCycle.getProcessor()
					.getRequestCodingStrategy();
			CharSequence path = requestCodingStrategy.pathForTarget(homepageTarget);

			if (path != null)
			{
				// The home page was mounted at the given path.
				// Issue a redirect to that path
				requestCycle.setRedirect(true);
			}

			// else the home page was not mounted; render it now so
			// that we will keep a clean path
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
		if (page == null)
		{
			throw new IllegalArgumentException("page must not be null");
		}
		if (interfaceName == null)
		{
			throw new IllegalArgumentException("interfaceName must not be null");
		}

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
			Component component = null;
			final String pageRelativeComponentPath = Strings.afterFirstPathComponent(componentPath,
					Component.PATH_SEPARATOR);
			if (Strings.isEmpty(pageRelativeComponentPath))
			{
				component = page;
			}
			else
			{
				component = page.get(pageRelativeComponentPath);
			}

			if (component == null)
			{
				throw new WicketRuntimeException("component " + pageRelativeComponentPath
						+ " not found on page " + page.getClass().getName() + "[id = "
						+ page.getNumericId() + "], listener interface = " + listener);
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
			// Page could not be located in the session
			throw new PageExpiredException("Cannot find the rendered page in session [pagemap="
					+ requestParameters.getPageMapName() + ",componentPath=" + componentPath
					+ ",versionNumber=" + requestParameters.getVersionNumber() + "]");
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
