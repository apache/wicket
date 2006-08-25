/*
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
package wicket.protocol.http.portlet;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Application;
import wicket.IRequestTarget;
import wicket.Page;
import wicket.PageParameters;
import wicket.RequestCycle;
import wicket.Session;
import wicket.WicketRuntimeException;
import wicket.markup.MarkupException;
import wicket.protocol.http.request.WebErrorCodeResponseTarget;
import wicket.request.RequestParameters;
import wicket.request.compound.AbstractRequestTargetResolverStrategy;
import wicket.request.target.component.BookmarkablePageRequestTarget;
import wicket.request.target.component.PageRequestTarget;

/**
 * Portlet request target resolver strategy. 
 * 
 *   
 * @author Janne Hietam&auml;ki
 */

public class PortletRequestTargetResolverStrategy extends  AbstractRequestTargetResolverStrategy
{

	/** log. */
	private static final Log log = LogFactory.getLog(PortletRequestTargetResolverStrategy.class);

	/**
	 * @see wicket.request.compound.IRequestTargetResolverStrategy#resolve(wicket.RequestCycle,
	 *      wicket.request.RequestParameters)
	 */
	public final IRequestTarget resolve(final RequestCycle requestCycle,
			final RequestParameters requestParameters)
	{

		if (requestParameters.getBookmarkablePageClass() != null)
		{
			return resolveBookmarkablePage(requestCycle, requestParameters);
		}	

		final String componentPath = requestParameters.getComponentPath();
		if(componentPath!=null)
		{
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
		}
		if (requestParameters.getPath() == null && requestParameters.getComponentPath() == null)
		{
			return resolveHomePageTarget(requestCycle, requestParameters);
		}
		throw new WicketRuntimeException("Unable to resolve request target " + requestParameters);
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
		Class<? extends Page> pageClass;
		try
		{
			pageClass = (Class<? extends Page>)session.getClassResolver().resolveClass(bookmarkablePageClass);
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
}