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
package org.apache.wicket.protocol.http.portlet;

import java.io.IOException;

import javax.portlet.PortletConfig;
import javax.portlet.PortletRequest;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WicketFilter;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.settings.IRequestCycleSettings;
import org.apache.wicket.settings.IRequestCycleSettings.RenderStrategy;

/**
 * Handles Portlet specific filtering requirements.
 * <p/>
 * 
 * The WicketFilterPortletContext first checks if it is a Portlet based request or a direct browser
 * (servlet) request. If the request is a direct browser request it simply delegates the request to
 * the {@link WicketFilter} and "normal" Wicket web application handling continues. This allows
 * deploying the same Wicket application (with the same web.xml) as a normal web application too (as
 * long as you don't use portlet specific features within your application).
 * </p>
 * 
 * If the request is dispatched from the WicketPortlet (easily determined from request attributes),
 * the WicketPortletFilter wraps the servlet request and response objects with specialized portlet
 * environment versions. Furthermore, the Servlet Session object will be wrapped to provide an
 * isolated PORTLET_SCOPED session to Wicket to support multiple windows of the same portlet. And
 * the RenderStrategy {@link IRequestCycleSettings#REDIRECT_TO_RENDER} will have to be enforced when
 * invoked from portlet processAction, otherwise {@link IRequestCycleSettings#ONE_PASS_RENDER}.
 * Thereafter, the {@link WicketFilterPortletContext} can let the standard {@link WicketFilter}
 * handle the request as if it were a "normal" web based request.
 * <p/>
 * 
 * @see WicketFilter
 * @author Ate Douma
 */
public class WicketFilterPortletContext
{
	/**
	 * The unique, reserved string used to prefix the portlet's "window id" in the URL.
	 */
	private static final String SERVLET_RESOURCE_URL_PORTLET_WINDOW_ID_PREFIX = "/ps:";

	private static final char[] slashReplacers = { '!', '@', '$', '-', '_', '|', ',', '.', '9',
			'8', '7', '6', '5', '4', '3', '2', '1', 'z', 'y', 'x', 'w', 'v', 'u', 't', 's', 'r',
			'q', 'p', 'o', 'm', 'n', 'l', 'k', 'j', 'i', 'h', 'g', 'f', 'e', 'd', 'c', 'b', 'a' };

	/**
	 * Overrides render strategy and adds the {@link PortletInvalidMarkupFilter} filter.
	 * 
	 * @see PortletInvalidMarkupFilter
	 * @param webApplication
	 */
	public void initFilter(FilterConfig filterConfig, WebApplication webApplication)
		throws ServletException
	{
		// override render strategy to REDIRECT_TO_REDNER
		webApplication.getRequestCycleSettings().setRenderStrategy(
			RenderStrategy.REDIRECT_TO_RENDER);
		// Add response filter to remove extra HTML such as <body> etc as they are not appropriate
		// for portlet environments
		webApplication.getRequestCycleSettings()
			.addResponseFilter(new PortletInvalidMarkupFilter());
	}

	/**
	 * Sets up the filter to process a given request cycle. Potentially wraps the request and
	 * response objects with portlet specific wrappers.
	 * 
	 * <p>
	 * Also sets up the session proxy using Apache Portals Bridge to ensure portlet session
	 * isolation. This is an option feature of Portal 2.0 spec so we just use portal bridges instead
	 * as it guarantees us support.
	 * 
	 * @see org.apache.portals.bridges.util.ServletPortletSessionProxy
	 * @param config
	 *            filter configuration
	 * @param filterRequestContext
	 * @param filterPath
	 * @return true if we are in a portlet environment
	 * @throws IOException
	 * @throws ServletException
	 */
	public boolean setupFilter(FilterConfig config, FilterRequestContext filterRequestContext,
		String filterPath) throws IOException, ServletException
	{
		boolean inPortletContext = false;
		PortletConfig portletConfig = (PortletConfig)filterRequestContext.getRequest()
			.getAttribute("javax.portlet.config");
		if (portletConfig != null)
		{
			inPortletContext = true;
			PortletRequest portletRequest = (PortletRequest)filterRequestContext.getRequest()
				.getAttribute("javax.portlet.request");
			WicketResponseState responseState = (WicketResponseState)filterRequestContext.getRequest()
				.getAttribute(WicketPortlet.RESPONSE_STATE_ATTR);
			filterRequestContext.setRequest(new PortletServletRequestWrapper(
				config.getServletContext(), filterRequestContext.getRequest(),
				ServletPortletSessionProxy.createProxy(filterRequestContext.getRequest(),
					portletRequest.getWindowID()), filterPath));
			filterRequestContext.setResponse(new PortletServletResponseWrapper(
				filterRequestContext.getResponse(), responseState));
		}
		else
		{
			ServletContext context = config.getServletContext();
			HttpServletRequest request = filterRequestContext.getRequest();
			String pathInfo = request.getRequestURI().substring(
				request.getContextPath().length() + filterPath.length());
			String portletWindowId = decodePortletWindowId(pathInfo);
			if (portletWindowId != null)
			{
				HttpSession proxiedSession = ServletPortletSessionProxy.createProxy(request,
					portletWindowId);
				pathInfo = stripWindowIdFromPathInfo(pathInfo);
				filterRequestContext.setRequest(new PortletServletRequestWrapper(context, request,
					proxiedSession, filterPath, pathInfo));
			}
		}
		return inPortletContext;
	}

	/**
	 * Factory method which will delegate to
	 * {@link #newPortletRequestContext(ServletWebRequest, WebResponse)} to create the
	 * {@link PortletRequestContext} if the request is in a portlet context.
	 * 
	 * @param request
	 * @param response
	 * @return true if running in a portlet context.
	 */
	public boolean createPortletRequestContext(ServletWebRequest request, WebResponse response)
	{
		if (request.getHttpServletRequest().getAttribute("javax.portlet.config") != null)
		{
			newPortletRequestContext(request, response);
			return true;
		}
		return false;
	}

	/**
	 * @see WicketFilterPortletContext#SERVLET_RESOURCE_URL_PORTLET_WINDOW_ID_PREFIX
	 * @return the unique, reserved string used to prefix the portlet's "window id" in the URL.
	 */
	public String getServletResourceUrlPortletWindowIdPrefix()
	{
		return SERVLET_RESOURCE_URL_PORTLET_WINDOW_ID_PREFIX;
	}

	/**
	 * FIXME javadoc
	 * 
	 * Try to extract the portlet's window id from the request url.
	 * 
	 * @param pathInfo
	 *            the url relative to the servlet context and filter path
	 * @return the window id, or null if it couldn't be decoded, with no leading forward slash
	 */
	public String decodePortletWindowId(String pathInfo)
	{
		String portletWindowId = null;
		// the path info should start with the window id prefix
		if (pathInfo != null && pathInfo.startsWith(getServletResourceUrlPortletWindowIdPrefix()))
		{
			int nextPath = pathInfo.indexOf('/', 1);
			if (nextPath > -1)
			{
				portletWindowId = pathInfo.substring(
					getServletResourceUrlPortletWindowIdPrefix().length(), nextPath);
			}
			else
			{
				portletWindowId = pathInfo.substring(getServletResourceUrlPortletWindowIdPrefix().length());
			}

			if (portletWindowId.length() > 2 && portletWindowId.charAt(0) == ':')
			{
				// Support for JBoss Portal which provides portletWindowIds containing a '/'
				// character which cannot be used within a path parameter.
				// slash encoder is provided as prefix of the real portletWindowId
				char slashEncoder = portletWindowId.charAt(2);
				portletWindowId = portletWindowId.substring(2);
				if (slashEncoder != ':')
				{
					portletWindowId = portletWindowId.replace(slashEncoder, '/');
				}
			}
		}
		else
		// pathInfo was empty or didn't start with the window id prefix
		{
			// ignore - returns null
		}
		return portletWindowId;
	}


	/**
	 * FIXME javadoc
	 * 
	 * <p>
	 * If the pathInfo contains the portlet window id namespace prefix, remove it.
	 * 
	 * @param pathInfo
	 * @return
	 */
	public String stripWindowIdFromPathInfo(String pathInfo)
	{
		if (pathInfo != null && pathInfo.startsWith(getServletResourceUrlPortletWindowIdPrefix()))
		{
			int nextPath = pathInfo.indexOf('/', 1);
			pathInfo = nextPath > -1 ? pathInfo.substring(nextPath) : null;
		}
		return pathInfo;
	}

	/**
	 * Encodes the given path portlet window id.
	 * 
	 * @param windowId
	 * @param path
	 * @return
	 */
	public String encodeWindowIdInPath(String windowId, CharSequence path)
	{
		if (windowId != null && windowId.length() > 0)
		{
			if (windowId.indexOf('/') > -1)
			{
				// Support for JBoss Portal which provides portletWindowIds containing a '/'
				// character which cannot be used within a path parameter.
				// Trying to find a replacer and encoding it as a prefix before the thereby
				// "encoded" windowId
				boolean replaced = false;
				for (char replacer : slashReplacers)
				{
					if (windowId.indexOf(replacer) == -1)
					{
						windowId = ":" + replacer + windowId.replace('/', replacer);
						replaced = true;
						break;
					}
				}
				if (!replaced)
				{
					throw new RuntimeException(
						"PortletRequest.getWindowId() contains a '/' character for which no valid and unique replacer could be determined: " +
							windowId);
				}
			}
			else if (windowId.charAt(0) == ':')
			{
				windowId = "::" + windowId;
			}
		}
		return (getServletResourceUrlPortletWindowIdPrefix().substring(1) + windowId + "/" + path);
	}

	/**
	 * Factory method to create the {@link PortletRequestContext}.
	 * 
	 * @see #createPortletRequestContext(WebRequest, WebResponse)
	 * @see PortletRequestContext
	 * @param request
	 * @param response
	 */
	protected void newPortletRequestContext(ServletWebRequest request, WebResponse response)
	{
		new PortletRequestContext(this, request, response);
	}
}