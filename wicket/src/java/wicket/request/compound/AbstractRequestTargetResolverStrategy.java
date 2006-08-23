/*
 * $Id: org.eclipse.jdt.ui.prefs 5004 2006-03-17 20:47:08 -0800 (Fri, 17 Mar 2006) eelco12 $
 * $Revision: 5004 $
 * $Date: 2006-03-17 20:47:08 -0800 (Fri, 17 Mar 2006) $
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.request.compound;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Application;
import wicket.Component;
import wicket.IRedirectListener;
import wicket.IRequestTarget;
import wicket.Page;
import wicket.PageParameters;
import wicket.RequestCycle;
import wicket.RequestListenerInterface;
import wicket.Session;
import wicket.WicketRuntimeException;
import wicket.authorization.UnauthorizedActionException;
import wicket.markup.MarkupException;
import wicket.markup.html.INewBrowserWindowListener;
import wicket.protocol.http.request.WebErrorCodeResponseTarget;
import wicket.request.IRequestCodingStrategy;
import wicket.request.RequestParameters;
import wicket.request.target.component.BookmarkableListenerInterfaceRequestTarget;
import wicket.request.target.component.BookmarkablePageRequestTarget;
import wicket.request.target.component.ExpiredPageClassRequestTarget;
import wicket.request.target.component.PageRequestTarget;
import wicket.request.target.component.listener.RedirectPageRequestTarget;
import wicket.util.string.Strings;

/**
 * Base class for  target resolver strategies {@link DefaultRequestTargetResolverStrategy} 
 * and {@link PortletRequestTargetResolverStrategy}
 * 
 * @author Eelco Hillenius
 * @author Igor Vaynberg
 * @author Jonathan Locke
 */
public abstract class AbstractRequestTargetResolverStrategy implements IRequestTargetResolverStrategy
{

	/** log. */
	private static final Log log = LogFactory.getLog(AbstractRequestTargetResolverStrategy.class);

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
			// Page was expired from session, probably because backtracking
			// limit was reached
			return new ExpiredPageClassRequestTarget();
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
			if (Strings.isEmpty(pageRelativeComponentPath))
			{
				// We have an interface that is not a redirect, but no
				// component... that must be wrong
				throw new WicketRuntimeException("When trying to call " + listener
						+ ", a component must be provided");
			}
			final Component component = page.get(pageRelativeComponentPath);
			if (component == null || !component.isEnabled() || !component.isVisibleInHierarchy())
			{
				log.info("component not enabled or visible, redirecting to calling page, component: "
								+ component);
				return new RedirectPageRequestTarget(page);
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
	 * Resolves to a bookmarkable page target.
	 * 
	 * @param requestCycle
	 *            the current request cycle
	 * @param requestParameters
	 *            the request parameters object
	 * @return the bookmarkable page as a request target
	 */
	@SuppressWarnings("unchecked")
	protected IRequestTarget resolveBookmarkablePage(final RequestCycle requestCycle,
			final RequestParameters requestParameters)
	{
		String bookmarkablePageClass = requestParameters.getBookmarkablePageClass();
		Session session = requestCycle.getSession();
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
			if (requestParameters.getComponentPath() != null
					&& requestParameters.getInterfaceName() != null)
			{
				final String componentPath = requestParameters.getComponentPath();
				final Page page = session.getPage(requestParameters.getPageMapName(), componentPath,
						requestParameters.getVersionNumber());
				
				if(page != null && page.getClass() == pageClass)
				{
					return resolveListenerInterfaceTarget(requestCycle, page, componentPath,
							requestParameters.getInterfaceName(), requestParameters);
				}
				else
				{
					return new BookmarkableListenerInterfaceRequestTarget(requestParameters
							.getPageMapName(), pageClass, params, requestParameters.getComponentPath(),
							requestParameters.getInterfaceName());
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
			Class<? extends Page> homePageClass = application.getHomePage();

			PageParameters parameters = new PageParameters(requestParameters.getParameters());
			// and create a dummy target for looking up whether the home page is
			// mounted
			BookmarkablePageRequestTarget homepageTarget = null;
			homepageTarget = new BookmarkablePageRequestTarget(homePageClass, parameters);
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
}
