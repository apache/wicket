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
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletRequest;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletResponse;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.portals.bridges.common.PortletResourceURLFactory;
import org.apache.portals.bridges.common.ServletContextProvider;
import org.apache.wicket.Resource;
import org.apache.wicket.protocol.http.WicketFilter;

/**
 * Adapter between the Portlet world requests and the internal Wicket engine. I.e. simulates the
 * web/servlet environment for Wicket, while it's actually running as a Portlet.
 * 
 * <p>
 * It receives a portlet request and dispatches to a a Wicket filter; wrapping the servlet context,
 * request and response objects; intercepts response writing (especially urls and redirects) and
 * rewrites and adapts the output to accommodate the portlet requirements.
 * 
 * <p>
 * The WicketPortlet is configured (using an initParameter) against a specific filter path, e.g.
 * Wicket WebApplication. The WicketPortlet maintains a parameter for the current Wicket page url,
 * based against the filter path (e.g. fully qualified to the context path). When a request (action,
 * render or direct resource/ajax call) is received by the WicketPortlet, it dispatches it to Wicket
 * as a servlet request using the provided Wicket page url parameter.
 * 
 * @see WicketFilter
 * @author Ate Douma
 */
public class WicketPortlet extends GenericPortlet
{
	/**
	 * FIXME javadoc
	 * 
	 * <p>
	 * The prefix for the parameter name for storing Wicket URLs.
	 */
	public static final String WICKET_URL_PORTLET_PARAMETER = "_wu";

	/**
	 * FIXME javadoc
	 * 
	 * <p>
	 * The prefix for the parameter name for storing Wicket Resource URLs.
	 * 
	 * @see Resource
	 */
	public static final String PORTLET_RESOURCE_URL_PARAMETER = "_ru";

	/**
	 * FIXME javadoc
	 * 
	 * <p>
	 * The attribute name for storing if the request is a Resource request or not.
	 * 
	 * @see Resource
	 */
	public static final String PORTLET_RESOURCE_URL_ATTR = "_ru";

	/**
	 * FIXME javadoc
	 */
	public static final String WICKET_FILTER_PATH_PARAM = "wicketFilterPath";

	/**
	 * FIXME javadoc
	 * 
	 * <p>
	 * Parameter name to store the {@link ServletContextProvider}.
	 */
	public static final String PARAM_SERVLET_CONTEXT_PROVIDER = "ServletContextProvider";

	/**
	 * FIXME javadoc
	 * 
	 * <p>
	 * Parameter name to store the {@link PortletResourceURLFactory}.
	 */
	public static final String PARAM_PORTLET_RESOURCE_URL_FACTORY = "PortletResourceURLFactory";
	/** Portal Action request */
	public static final String ACTION_REQUEST = "ACTION";
	/** Portal Resource request */
	public static final String RESOURCE_REQUEST = "RESOURCE";

	/** Portal Custom request */
	public static final String CUSTOM_REQUEST = "CUSTOM";
	/** Portal View request - i.e. doView */
	public static final String VIEW_REQUEST = "VIEW";
	/** Portal Edit request - i.e. doEdit */
	public static final String EDIT_REQUEST = "EDIT";
	/** Portal Help request - i.e. doHelp */
	public static final String HELP_REQUEST = "HELP";
	/**
	 * Marker used as key to store the type of request as a request attribute - i.e. resource /
	 * action / event request etc .
	 */
	public static final String REQUEST_TYPE_ATTR = WicketPortlet.class.getName() + ".REQUEST_TYPE";
	/**
	 * FIXME javadoc
	 * <p>
	 * The parameter name to store the URL to the Wicket porlet.
	 */
	public static final String WICKET_URL_PORTLET_PARAMETER_ATTR = WicketPortlet.class.getName() +
		".WICKET_URL_PORTLET_PARAMETER";
	public static final String CONFIG_PARAM_PREFIX = WicketPortlet.class.getName() + ".";
	/** Marker used as key for the WicketResponseState object stored as a request attribute. */
	public static final String RESPONSE_STATE_ATTR = WicketResponseState.class.getName();
	public static final String RESOURCE_URL_FACTORY_ATTR = PortletResourceURLFactory.class.getName();
	/** FIXME javadoc */
	public static final String WICKET_PORTLET_PROPERTIES = WicketPortlet.class.getName().replace(
		'.', '/') +
		".properties";
	public static final String WICKET_FILTER_PATH = WicketPortlet.class.getName() + ".FILTERPATH";
	public static final String WICKET_FILTER_QUERY = WicketPortlet.class.getName() + ".FILTERQUERY";

	/**
	 * Name of portlet init parameter for Action page
	 */
	public static final String PARAM_ACTION_PAGE = "actionPage";
	/**
	 * Name of portlet init parameter for Custom page
	 */
	public static final String PARAM_CUSTOM_PAGE = "customPage";
	/**
	 * Name of portlet init parameter for Edit page
	 */
	public static final String PARAM_EDIT_PAGE = "editPage";
	/**
	 * Name of portlet init parameter for Edit page
	 */
	public static final String PARAM_HELP_PAGE = "helpPage";
	/**
	 * Name of portlet init parameter for View page
	 */
	public static final String PARAM_VIEW_PAGE = "viewPage";

	private ServletContextProvider servletContextProvider;
	/**
	 * {@link PortletResourceURLFactory}, used to generate {@link Resource} URLs for Portal 168
	 * support.
	 */
	private PortletResourceURLFactory resourceURLFactory;
	private String wicketFilterPath;
	private String wicketFilterQuery;
	private final HashMap defaultPages = new HashMap();

	@Override
	public void init(PortletConfig config) throws PortletException
	{
		super.init(config);
		Properties wicketPortletProperties = null;
		String contextProviderClassName = getContextProviderClassNameParameter(config);
		if (contextProviderClassName == null)
		{
			contextProviderClassName = config.getPortletContext().getInitParameter(
				ServletContextProvider.class.getName());
		}
		if (contextProviderClassName == null)
		{
			wicketPortletProperties = getWicketPortletProperties(wicketPortletProperties);
			contextProviderClassName = wicketPortletProperties.getProperty(ServletContextProvider.class.getName());
		}
		if (contextProviderClassName == null)
		{
			throw new PortletException("Portlet " + config.getPortletName() +
				" is incorrectly configured. Init parameter " + PARAM_SERVLET_CONTEXT_PROVIDER +
				" not specified, nor as context parameter " +
				ServletContextProvider.class.getName() + " or as property in " +
				WICKET_PORTLET_PROPERTIES + " in the classpath.");
		}
		try
		{
			Class clazz = Class.forName(contextProviderClassName);
			servletContextProvider = (ServletContextProvider)clazz.newInstance();
		}
		catch (Exception e)
		{
			if (e instanceof PortletException)
			{
				throw (PortletException)e;
			}
			throw new PortletException("Initialization failure", e);
		}

		String resourceURLFactoryClassName = getPortletResourceURLFactoryClassNameParameter(config);
		if (resourceURLFactoryClassName == null)
		{
			resourceURLFactoryClassName = config.getPortletContext().getInitParameter(
				PortletResourceURLFactory.class.getName());
		}
		if (resourceURLFactoryClassName == null)
		{
			wicketPortletProperties = getWicketPortletProperties(wicketPortletProperties);
			resourceURLFactoryClassName = wicketPortletProperties.getProperty(PortletResourceURLFactory.class.getName());
		}
		if (resourceURLFactoryClassName == null)
		{
			throw new PortletException("Portlet " + config.getPortletName() +
				" is incorrectly configured. Init parameter " + PARAM_PORTLET_RESOURCE_URL_FACTORY +
				" not specified, nor as context parameter " +
				PortletResourceURLFactory.class.getName() + " or as property in " +
				WICKET_PORTLET_PROPERTIES + " in the classpath.");
		}
		try
		{
			Class clazz = Class.forName(resourceURLFactoryClassName);
			resourceURLFactory = (PortletResourceURLFactory)clazz.newInstance();
		}
		catch (Exception e)
		{
			if (e instanceof PortletException)
			{
				throw (PortletException)e;
			}
			throw new PortletException("Initialization failure", e);
		}

		wicketFilterPath = buildWicketFilterPath(config.getInitParameter(WICKET_FILTER_PATH_PARAM));
		wicketFilterQuery = buildWicketFilterQuery(wicketFilterPath);

		defaultPages.put(PARAM_VIEW_PAGE, config.getInitParameter(PARAM_VIEW_PAGE));
		defaultPages.put(PARAM_ACTION_PAGE, config.getInitParameter(PARAM_ACTION_PAGE));
		defaultPages.put(PARAM_CUSTOM_PAGE, config.getInitParameter(PARAM_CUSTOM_PAGE));
		defaultPages.put(PARAM_HELP_PAGE, config.getInitParameter(PARAM_HELP_PAGE));
		defaultPages.put(PARAM_EDIT_PAGE, config.getInitParameter(PARAM_EDIT_PAGE));

		validateDefaultPages(defaultPages, wicketFilterPath, wicketFilterQuery);
	}

	@Override
	public void destroy()
	{
		resourceURLFactory = null;
		servletContextProvider = null;
		super.destroy();
	}

	protected String getDefaultPage(String pageType)
	{
		return (String)defaultPages.get(pageType);
	}

	protected String buildWicketFilterPath(String filterPath)
	{
		if (filterPath == null || filterPath.length() == 0)
		{
			filterPath = "/";
		}
		else
		{
			if (!filterPath.startsWith("/"))
			{
				filterPath = "/" + filterPath;
			}
			if (filterPath.endsWith("*"))
			{
				filterPath = filterPath.substring(0, filterPath.length() - 1);
			}
			if (!filterPath.endsWith("/"))
			{
				filterPath += "/";
			}
		}
		return filterPath;
	}

	protected String buildWicketFilterQuery(String wicketFilterPath)
	{
		if (wicketFilterPath.equals("/"))
		{
			return "?";
		}
		else
		{
			return wicketFilterPath.substring(0, wicketFilterPath.length() - 1) + "?";
		}
	}

	/**
	 * FIXME javadoc
	 * 
	 * <p>
	 * Corrects the incoming URL if the old home page style, or if it's missing the filter path
	 * prefix.
	 * 
	 * @param url
	 *            the URL to fix
	 * @param wicketFilterPath
	 * @param wicketFilterQuery
	 * @return the corrected URL
	 */
	protected String fixWicketUrl(String url, String wicketFilterPath, String wicketFilterQuery)
	{
		if (url == null)
		{
			return wicketFilterPath;
		}
		else if (!url.startsWith(wicketFilterPath))
		{
			if ((url + "/").equals(wicketFilterPath))
			{
				// hack around "old" style wicket home url's without trailing '/' which would lead
				// to a redirect to the real home path anyway
				url = wicketFilterPath;
			}
			else if (url.startsWith(wicketFilterQuery))
			{
				// correct url: path?query -> path/?query
				url = wicketFilterPath + "?" + url.substring(wicketFilterQuery.length());
			}
		}
		return url;
	}

	/**
	 * FIXME javadoc
	 * 
	 * <p>
	 * Registers the default pages and their URLs for the different {@link PortletMode}s. Also
	 * corrects and slightly incorrect URLs (see {@link #fixWicketUrl(String, String, String)}).
	 * 
	 * <p>
	 * If no specific page was specified for a given portlet mode (VIEW, EDIT etc) then the page for
	 * that mode is set to be the same page as that of the VIEW mode.
	 * 
	 * @see PortletMode
	 * @see #fixWicketUrl(String, String, String)
	 * @param defaultPages
	 * @param wicketFilterPath
	 * @param wicketFilterQuery
	 */
	protected void validateDefaultPages(Map defaultPages, String wicketFilterPath,
		String wicketFilterQuery)
	{
		String viewPage = fixWicketUrl((String)defaultPages.get(PARAM_VIEW_PAGE), wicketFilterPath,
			wicketFilterQuery);
		defaultPages.put(PARAM_VIEW_PAGE, viewPage.startsWith(wicketFilterPath) ? viewPage
			: wicketFilterPath);

		String defaultPage = (String)defaultPages.get(PARAM_ACTION_PAGE);
		if (defaultPage == null)
		{
			defaultPages.put(PARAM_ACTION_PAGE, viewPage);
		}
		else
		{
			defaultPage = fixWicketUrl(defaultPage, wicketFilterPath, wicketFilterQuery);
			defaultPages.put(PARAM_ACTION_PAGE, defaultPage.startsWith(wicketFilterPath)
				? defaultPage : viewPage);
		}

		defaultPage = (String)defaultPages.get(PARAM_CUSTOM_PAGE);
		if (defaultPage == null)
		{
			defaultPages.put(PARAM_CUSTOM_PAGE, viewPage);
		}
		else
		{
			defaultPage = fixWicketUrl(defaultPage, wicketFilterPath, wicketFilterQuery);
			defaultPages.put(PARAM_CUSTOM_PAGE, defaultPage.startsWith(wicketFilterPath)
				? defaultPage : viewPage);
		}

		defaultPage = (String)defaultPages.get(PARAM_HELP_PAGE);
		if (defaultPage == null)
		{
			defaultPages.put(PARAM_HELP_PAGE, viewPage);
		}
		else
		{
			defaultPage = fixWicketUrl(defaultPage, wicketFilterPath, wicketFilterQuery);
			defaultPages.put(PARAM_HELP_PAGE, defaultPage.startsWith(wicketFilterPath)
				? defaultPage : viewPage);
		}

		defaultPage = (String)defaultPages.get(PARAM_EDIT_PAGE);
		if (defaultPage == null)
		{
			defaultPages.put(PARAM_EDIT_PAGE, viewPage);
		}
		else
		{
			defaultPage = fixWicketUrl(defaultPage, wicketFilterPath, wicketFilterQuery);
			defaultPages.put(PARAM_EDIT_PAGE, defaultPage.startsWith(wicketFilterPath)
				? defaultPage : viewPage);
		}
	}

	/**
	 * Loads the Wicket Portlet properties file off the class path.
	 * 
	 * FIXME javadoc - check properties
	 * 
	 * @param properties
	 *            appends the portlet properties to
	 * @return Wicket portlet properties. Returns an empty or unchanged properties object if Wicket
	 *         Portlet properties could not be found
	 * @throws PortletException
	 *             if loading the properties fails
	 */
	protected Properties getWicketPortletProperties(Properties properties) throws PortletException
	{
		if (properties == null)
		{
			properties = new Properties();
		}
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(
			WICKET_PORTLET_PROPERTIES);
		if (is != null)
		{
			try
			{
				properties.load(is);
			}
			catch (IOException e)
			{
				throw new PortletException(
					"Failed to load WicketPortlet.properties from classpath", e);
			}
		}
		return properties;
	}

	/**
	 * Gets the Context Provider ClassName Parameter from the config object.
	 * 
	 * @param config
	 * @return Context Provider ClassName
	 */
	protected String getContextProviderClassNameParameter(PortletConfig config)
	{
		return config.getInitParameter(PARAM_SERVLET_CONTEXT_PROVIDER);
	}

	/**
	 * Gets the portlet resource url factory class name from the config object.
	 * 
	 * @param config
	 * @return portlet resource url factory class name
	 */
	protected String getPortletResourceURLFactoryClassNameParameter(PortletConfig config)
	{
		return config.getInitParameter(PARAM_PORTLET_RESOURCE_URL_FACTORY);
	}

	/**
	 * 
	 * @return servlet context provider
	 */
	protected ServletContextProvider getServletContextProvider()
	{
		return servletContextProvider;
	}

	/**
	 * 
	 * @param portlet
	 * @param request
	 * @param response
	 * @return
	 */
	protected ServletContext getServletContext(GenericPortlet portlet, PortletRequest request,
		PortletResponse response)
	{
		return getServletContextProvider().getServletContext(portlet);
	}

	protected HttpServletRequest getHttpServletRequest(GenericPortlet portlet,
		PortletRequest request, PortletResponse response)
	{
		return getServletContextProvider().getHttpServletRequest(portlet, request);
	}

	protected HttpServletResponse getHttpServletResponse(GenericPortlet portlet,
		PortletRequest request, PortletResponse response)
	{
		return getServletContextProvider().getHttpServletResponse(portlet, response);
	}

	protected String getWicketConfigParameter(PortletRequest request, String paramName,
		String defaultValue)
	{
		return defaultValue;
	}

	protected String getWicketUrlPortletParameter(PortletRequest request)
	{
		return WICKET_URL_PORTLET_PARAMETER;
	}

	protected String getWicketFilterPath()
	{
		return wicketFilterPath;
	}

	/**
	 * Retrieves the Wicket URL from the request object, or if none exists returns the default URL.
	 * 
	 * @see PortletRequestContext#getLastEncodedPath()
	 * @param request
	 * @param pageType
	 * @param defaultPage
	 * @return the Wicket URL from within the specified request
	 */
	protected String getWicketURL(PortletRequest request, String pageType, String defaultPage)
	{
		String wicketURL = null;
		if (request instanceof ActionRequest)
		{
			wicketURL = request.getParameter((String)request.getAttribute(WicketPortlet.WICKET_URL_PORTLET_PARAMETER_ATTR));
		}
		else
		{
			wicketURL = request.getParameter((String)request.getAttribute(WicketPortlet.WICKET_URL_PORTLET_PARAMETER_ATTR) +
				request.getPortletMode().toString());
		}
		if (wicketURL == null)
		{
			wicketURL = getWicketConfigParameter(request, CONFIG_PARAM_PREFIX + pageType,
				defaultPage);
		}
		return wicketURL;
	}

	@Override
	protected void doView(RenderRequest request, RenderResponse response) throws PortletException,
		IOException
	{
		processRequest(request, response, VIEW_REQUEST, PARAM_VIEW_PAGE);
	}

	@Override
	protected void doEdit(RenderRequest request, RenderResponse response) throws PortletException,
		IOException
	{
		processRequest(request, response, EDIT_REQUEST, PARAM_EDIT_PAGE);
	}

	@Override
	protected void doHelp(RenderRequest request, RenderResponse response) throws PortletException,
		IOException
	{
		processRequest(request, response, HELP_REQUEST, PARAM_HELP_PAGE);
	}

	protected void doCustom(RenderRequest request, RenderResponse response)
		throws PortletException, IOException
	{
		processRequest(request, response, CUSTOM_REQUEST, PARAM_CUSTOM_PAGE);
	}

	@Override
	public void processAction(ActionRequest request, ActionResponse response)
		throws PortletException, IOException
	{
		processRequest(request, response, ACTION_REQUEST, PARAM_ACTION_PAGE);
	}

	/**
	 * Consumes and processes all portlet requests. All the doX methods delegate to this method,
	 * including processAction and serveResource.
	 * 
	 * @param request
	 * @param response
	 * @param requestType
	 * @param pageType
	 * @throws PortletException
	 * @throws IOException
	 */
	protected void processRequest(PortletRequest request, PortletResponse response,
		String requestType, String pageType) throws PortletException, IOException
	{
		String wicketURL = null;
		String wicketFilterPath = null;
		String wicketFilterQuery = null;

		request.setAttribute(WICKET_URL_PORTLET_PARAMETER_ATTR,
			getWicketUrlPortletParameter(request));

		wicketURL = getWicketURL(request, pageType, getDefaultPage(pageType));
		wicketFilterPath = getWicketConfigParameter(request, WICKET_FILTER_PATH,
			this.wicketFilterPath);
		wicketFilterQuery = getWicketConfigParameter(request, WICKET_FILTER_QUERY,
			this.wicketFilterQuery);

		boolean actionRequest = ACTION_REQUEST.equals(requestType);

		WicketResponseState responseState = new WicketResponseState();

		request.setAttribute(RESPONSE_STATE_ATTR, responseState);
		request.setAttribute(RESOURCE_URL_FACTORY_ATTR, resourceURLFactory);
		request.setAttribute(REQUEST_TYPE_ATTR, requestType);
		String portletResourceURL = request.getParameter(PORTLET_RESOURCE_URL_PARAMETER);
		if (portletResourceURL != null)
		{
			request.setAttribute(PORTLET_RESOURCE_URL_ATTR, portletResourceURL);
		}

		if (actionRequest)
		{
			ServletContext servletContext = getServletContext(this, request, response);
			HttpServletRequest req = getHttpServletRequest(this, request, response);
			HttpServletResponse res = getHttpServletResponse(this, request, response);
			RequestDispatcher rd = servletContext.getRequestDispatcher(wicketURL);

			if (rd != null)
			{
				// http://issues.apache.org/jira/browse/PB-2:
				// provide servlet access to the Portlet components even from
				// an actionRequest in extension to the JSR-168 requirement
				// PLT.16.3.2 which (currently) only covers renderRequest
				// servlet inclusion.
				if (req.getAttribute("javax.portlet.config") == null)
				{
					req.setAttribute("javax.portlet.config", getPortletConfig());
				}
				if (req.getAttribute("javax.portlet.request") == null)
				{
					req.setAttribute("javax.portlet.request", request);
				}
				if (req.getAttribute("javax.portlet.response") == null)
				{
					req.setAttribute("javax.portlet.response", response);
				}
				try
				{
					rd.include(req, res);
					processActionResponseState(wicketURL, wicketFilterPath, wicketFilterQuery,
						(ActionRequest)request, (ActionResponse)response, responseState);
				}
				catch (ServletException e)
				{
					throw new PortletException(e);
				}
			}
		}
		else
		{
			PortletRequestDispatcher rd = null;
			String previousURL = null;
			while (true)
			{
				rd = getPortletContext().getRequestDispatcher(wicketURL);
				if (rd != null)
				{
					rd.include((RenderRequest)request, (RenderResponse)response);
					String redirectLocation = responseState.getRedirectLocation();
					if (redirectLocation != null)
					{
						redirectLocation = fixWicketUrl(redirectLocation, wicketFilterPath,
							wicketFilterQuery);
						boolean validWicketUrl = redirectLocation.startsWith(wicketFilterPath);
						if (portletResourceURL != null)
						{
							if (validWicketUrl)
							{
								HashMap parameters = new HashMap(2);
								parameters.put(
									(String)request.getAttribute(WicketPortlet.WICKET_URL_PORTLET_PARAMETER_ATTR) +
										request.getPortletMode().toString(),
									new String[] { redirectLocation });
								parameters.put(PORTLET_RESOURCE_URL_PARAMETER,
									new String[] { "true" });
								redirectLocation = resourceURLFactory.createResourceURL(
									getPortletConfig(), (RenderRequest)request,
									(RenderResponse)response, parameters);
							}
							getHttpServletResponse(this, request, response).sendRedirect(
								redirectLocation);
						}
						else if (validWicketUrl &&
							((previousURL == null || previousURL != redirectLocation)))
						{
							previousURL = wicketURL;
							wicketURL = redirectLocation;
							((RenderResponse)response).reset();
							responseState.reset();
							continue;
						}
						else
						{
							// TODO: unhandled/unsupport RenderResponse redirect
						}
					}
				}
				break;
			}
		}
	}

	/**
	 * Handles redirects set from processing the action. Checks the response state after the action
	 * has been processed by Wicket for the presence of a redirect URL, and if present, portletifies
	 * the URL. If the URL is a redirect to within the scope of this portlet, leaves it to be
	 * handled in a subsequent render call, or if not, sends the redirect to the client.
	 * 
	 * @param wicketURL
	 * @param wicketFilterPath
	 * @param wicketFilterQuery
	 * @param request
	 * @param response
	 * @param responseState
	 * @throws PortletException
	 * @throws IOException
	 */
	protected void processActionResponseState(String wicketURL, String wicketFilterPath,
		String wicketFilterQuery, ActionRequest request, ActionResponse response,
		WicketResponseState responseState) throws PortletException, IOException
	{
		if (responseState.getRedirectLocation() != null)
		{
			wicketURL = fixWicketUrl(responseState.getRedirectLocation(), wicketFilterPath,
				wicketFilterQuery);
			if (wicketURL.startsWith(wicketFilterPath))
			{
				response.setRenderParameter(
					(String)request.getAttribute(WicketPortlet.WICKET_URL_PORTLET_PARAMETER_ATTR) +
						request.getPortletMode().toString(), wicketURL);
			}
			else
			{
				response.sendRedirect(responseState.getRedirectLocation());
			}
		}
	}
}
