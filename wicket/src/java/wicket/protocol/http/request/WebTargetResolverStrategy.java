/*
 * $Id$
 * $Revision$ $Date$
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
package wicket.protocol.http.request;

import java.lang.reflect.Method;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Application;
import wicket.ApplicationPages;
import wicket.ApplicationSettings;
import wicket.Component;
import wicket.IRequestTarget;
import wicket.Page;
import wicket.PageParameters;
import wicket.RequestCycle;
import wicket.Resource;
import wicket.Session;
import wicket.SharedResources;
import wicket.WicketRuntimeException;
import wicket.protocol.http.WebRequest;
import wicket.protocol.http.WebRequestCycle;
import wicket.request.InterfaceCallRequestTarget;
import wicket.request.ExpiredPageClassRequestTarget;
import wicket.request.PageClassRequestTarget;
import wicket.request.PageRequestTarget;
import wicket.request.RedirectPageRequestTarget;
import wicket.request.SharedResourceRequestTarget;
import wicket.request.compound.IRequestTargetResolverStrategy;
import wicket.util.string.Strings;

/**
 * TODO docme.
 * 
 * @author Eelco Hillenius
 */
public final class WebTargetResolverStrategy implements IRequestTargetResolverStrategy
{
	/** log. */
	private static Log log = LogFactory.getLog(WebTargetResolverStrategy.class);

	/**
	 * Construct.
	 */
	public WebTargetResolverStrategy()
	{
	}

	/**
	 * @see wicket.request.compound.IRequestTargetResolverStrategy#resolve(wicket.RequestCycle)
	 */
	public final IRequestTarget resolve(RequestCycle requestCycle)
	{
		final WebRequestCycle webRequestCycle = (WebRequestCycle)requestCycle;
		final WebRequest webRequest = webRequestCycle.getWebRequest();


		String componentPath = webRequest.getParameter("path");
		// See whether this request points to a rendered page
		if (componentPath != null)
		{
			if (log.isDebugEnabled())
			{
				log.debug("resolving to previously rendered page");
			}
			return resolveRenderedPage(webRequestCycle, componentPath);
		}

		// see whether this request points to a bookmarkable page
		final String bookmarkablePageParameter = webRequest.getParameter("bookmarkablePage");

		if (bookmarkablePageParameter != null)
		{
			if (log.isDebugEnabled())
			{
				log.debug("resolving to bookmarkable page");
			}
			return resolveBookmarkablePage(webRequestCycle, bookmarkablePageParameter);
		}

		String pathInfo = webRequest.getPath();
		// see whether this request points to the home page
		if (Strings.isEmpty(pathInfo) || ("/".equals(pathInfo)))
		{
			if (log.isDebugEnabled())
			{
				log.debug("resolving to home page");
			}
			return resolveHomePageTarget(webRequestCycle);
		}
		else if (pathInfo.startsWith("/resources/"))
		{
			if (log.isDebugEnabled())
			{
				log.debug("resolving to shared resource");
			}
			final String resourceKey = pathInfo.substring("/resources/".length());
			return resolveSharedResource(webRequestCycle, resourceKey);
		}

		// if we get here, we have no regconized Wicket target, and thus
		// regard this as a external (non-wicket) resource request on
		// this server

		// Get the relative URL we need for loading the resource from
		// the servlet context
		// NOTE: we NEED to put the '/' in front as otherwise some versions
		// of application servers (e.g. Jetty 5.1.x) will fail for requests
		// like '/mysubdir/myfile.css'
		final String url = '/' + webRequest.getRelativeURL();
		return new WebExternalResourceRequestTarget(url);
	}

	/**
	 * Resolves to a shared resource target.
	 * 
	 * @param webRequestCycle
	 *            the current request cycle
	 * @param resourceKey
	 *            the key of the shared resource
	 * @return the shared resource as a request target
	 */
	private IRequestTarget resolveSharedResource(final WebRequestCycle webRequestCycle,
			final String resourceKey)
	{
		final Session session = webRequestCycle.getSession();
		final Application application = session.getApplication();
		SharedResources sharedResources = application.getSharedResources();
		Resource resource = sharedResources.get(resourceKey);
		if (resource == null)
		{
			if (log.isDebugEnabled())
			{
				log.debug("Could not find resource referenced by key " + resourceKey);
			}
			return new WebErrorCodeResponseTarget(HttpServletResponse.SC_NOT_FOUND,
					"Unable to load resource " + resourceKey);
		}
		return new SharedResourceRequestTarget(resourceKey, resource);
	}

	/**
	 * Resolves to a page target that was previously rendered. Optionally
	 * resolves to a component call target, which is a specialization of a page
	 * target. If no corresponding page could be found, a expired page target
	 * will be returned.
	 * 
	 * @param webRequestCycle
	 *            the current request cycle
	 * @param componentPath
	 *            the component path
	 * @return the previously rendered page as a request target
	 */
	private IRequestTarget resolveRenderedPage(WebRequestCycle webRequestCycle,
			final String componentPath)
	{
		final WebRequest webRequest = webRequestCycle.getWebRequest();
		final String pageMapName = webRequest.getParameter("pagemap");
		// Get version number
		final String versionNumberString = webRequest.getParameter("version");
		final int versionNumber = Strings.isEmpty(versionNumberString) ? 0 : Integer
				.parseInt(versionNumberString);

		final Session session = webRequestCycle.getSession();
		final Page page = session.getPage(pageMapName, componentPath, versionNumber);

		// Does page exist?
		if (page != null)
		{
			// Assume cluster needs to be updated now, unless listener
			// invocation change this (for example, with a simple page
			// redirect)
			webRequestCycle.setUpdateCluster(true);

			// see whether this resolves to a component call or just the page
			final String interfaceName = getInterfaceName(webRequest);
			if (interfaceName != null)
			{
				if (interfaceName.equals("IRedirectListener"))
				{
					return new RedirectPageRequestTarget(page);
				}
				else
				{
					final Method listenerMethod = webRequestCycle
							.getRequestInterfaceMethod(interfaceName);
					if (listenerMethod == null)
					{
						throw new WicketRuntimeException("Attempt to access unknown interface "
								+ interfaceName);
					}
					String componentPart = Strings.afterFirstPathComponent(componentPath, ':');
					if (Strings.isEmpty(componentPart))
					{
						// we have an interface that is not redirect, but no
						// component... that must be wrong
						throw new WicketRuntimeException("when trying to call " + listenerMethod
								+ ", a component must be provided");
					}
					final Component component = page.get(componentPart);
					if (!component.isVisible())
					{
						throw new WicketRuntimeException(
								"Calling listener methods on components that are not visible is not allowed");
					}
					return new InterfaceCallRequestTarget(page, component, listenerMethod);
				}
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
	 * Gets the name of the interface to invoke.
	 * 
	 * @param webRequest
	 *            the web request object
	 * @return the name of the interface to invoke
	 */
	private String getInterfaceName(final WebRequest webRequest)
	{
		String interfaceName = webRequest.getParameter("interface");
		if (interfaceName == null)
		{
			interfaceName = "IRedirectListener";
		}
		return interfaceName;
	}

	/**
	 * Resolves to a bookmarkable page target.
	 * 
	 * @param webRequestCycle
	 *            the current request cycle
	 * @param bookmarkablePageParameter
	 *            the bookmarkable page parameter
	 * @return the bookmarkable page as a request target
	 */
	private IRequestTarget resolveBookmarkablePage(WebRequestCycle webRequestCycle,
			final String bookmarkablePageParameter)
	{
		final IRequestTarget requestTarget;
		final Session session = webRequestCycle.getSession();
		final Application application = session.getApplication();

		// first see whether we have a logical mapping
		Class pageClass = application.getPages().classForAlias(bookmarkablePageParameter);

		// nope, we don't have a logical mapping, so this should be a
		// full class name
		if (pageClass == null)
		{
			try
			{
				pageClass = session.getClassResolver().resolveClass(bookmarkablePageParameter);
			}
			catch (RuntimeException e)
			{
				return new WebErrorCodeResponseTarget(HttpServletResponse.SC_NOT_FOUND,
						"Unable to load Bookmarkable Page");
			}
		}

		try
		{
			Page newPage = session.getPageFactory().newPage(pageClass,
					new PageParameters(webRequestCycle.getRequest().getParameterMap()));

			// the response might have been set in the constructor of
			// the bookmarkable page
			if (webRequestCycle.getResponsePage() == null)
			{
				requestTarget = new PageRequestTarget(newPage);
			}
			else
			{
				requestTarget = new PageRequestTarget(webRequestCycle.getResponsePage());
			}

			// as we have a new page, we should update the cluster
			// TODO abstract this so that we can decide by looking
			// at the kind of target and we don't have to bother
			// users with it?
			webRequestCycle.setUpdateCluster(true);

			return requestTarget;
		}
		catch (RuntimeException e)
		{
			throw new WicketRuntimeException("Unable to instantiate Page class: "
					+ bookmarkablePageParameter + ". See below for details.", e);
		}
	}

	/**
	 * Resolves to a home page target.
	 * 
	 * @param webRequestCycle
	 *            the current request cycle.
	 * @return the home page as a request target
	 */
	private IRequestTarget resolveHomePageTarget(WebRequestCycle webRequestCycle)
	{
		final IRequestTarget requestTarget;
		final Session session = webRequestCycle.getSession();
		final Application application = session.getApplication();
		try
		{
			Class homePage = application.getPages().getHomePage();
			ApplicationPages.HomePageRenderStrategy homePageStrategy = application.getPages()
					.getHomePageRenderStrategy();
			if (homePageStrategy == ApplicationPages.BOOKMARK_REDIRECT)
			{
				requestTarget = new PageClassRequestTarget(homePage);
			}
			else
			{
				final PageParameters parameters = new PageParameters(webRequestCycle
						.getWebRequest().getParameterMap());
				Page newPage = session.getPageFactory().newPage(homePage, parameters);

				// check if the home page didn't set a page by itself
				if (webRequestCycle.getResponsePage() == null)
				{
					if (homePageStrategy == ApplicationPages.PAGE_REDIRECT)
					{
						// see if we have to redirect the render part by default
						// so that a homepage has the same url as a post or
						// get to that page.
						ApplicationSettings.RenderStrategy strategy = session.getApplication()
								.getSettings().getRenderStrategy();
						boolean issueRedirect = (strategy == ApplicationSettings.REDIRECT_TO_RENDER || strategy == ApplicationSettings.REDIRECT_TO_BUFFER);
						webRequestCycle.setRedirect(issueRedirect);
					}
					requestTarget = new PageRequestTarget(newPage);
				}
				else
				{
					requestTarget = new PageRequestTarget(webRequestCycle.getResponsePage());
				}
			}

			// as we have a new page, we should update the cluster
			// TODO abstract this so that we can decide by looking
			// at the kind of target and we don't have to bother
			// users with it?
			webRequestCycle.setUpdateCluster(true);

			return requestTarget;
		}
		catch (WicketRuntimeException e)
		{
			throw new WicketRuntimeException("Could not create home page", e);
		}
	}
}
