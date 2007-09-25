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

/**
 * @author Ate Douma
 */
public class WicketPortlet extends GenericPortlet
{
	public static final String WICKET_URL_PORTLET_PARAMETER = "_wu";
	public static final String PORTLET_RESOURCE_URL_PARAMETER = "_ru";
	public static final String PORTLET_RESOURCE_URL_ATTR = "_ru";
	public static final String WICKET_FILTER_PATH_PARAM = "wicketFilterPath";
	public static final String PARAM_SERVLET_CONTEXT_PROVIDER = "ServletContextProvider";
	public static final String PARAM_PORTLET_RESOURCE_URL_FACTORY = "PortletResourceURLFactory";
	public static final String ACTION_REQUEST = "ACTION";
	public static final String VIEW_REQUEST = "VIEW";
	public static final String RESOURCE_REQUEST = "RESOURCE";
	public static final String CUSTOM_REQUEST = "CUSTOM";
	public static final String EDIT_REQUEST = "EDIT";
	public static final String HELP_REQUEST = "HELP";
	public static final String REQUEST_TYPE_ATTR = WicketPortlet.class.getName() + ".REQUEST_TYPE";
	public static final String WICKET_URL_PORTLET_PARAMETER_ATTR = WicketPortlet.class.getName() + ".WICKET_URL_PORTLET_PARAMETER";
	public static final String CONFIG_PARAM_PREFIX = WicketPortlet.class.getName() + ".";
	public static final String RESPONSE_STATE_ATTR = WicketResponseState.class.getName();
	public static final String RESOURCE_URL_FACTORY_ATTR = PortletResourceURLFactory.class.getName();
	public static final String WICKET_PORTLET_PROPERTIES = WicketPortlet.class.getName().replace('.', '/')+".properties";
	public static final String WICKET_FILTER_PATH = WicketPortlet.class.getName() + ".FILTERPATH";
	public static final String WICKET_FILTER_QUERY = WicketPortlet.class.getName() + ".FILTERQUERY";
	
    /**
     * Name of portlet init parameter for Action page
     */
    public static final String PARAM_ACTION_PAGE = "actionPage";
    /**
     * Name of portlet  init parameterfor Custom page
     */
    public static final String PARAM_CUSTOM_PAGE = "customPage";
    /**
     * Name of portlet  init parameterfor Edit page
     */
    public static final String PARAM_EDIT_PAGE = "editPage";
    /**
     * Name of portlet  init parameter for Edit page
     */
    public static final String PARAM_HELP_PAGE = "helpPage";
    /**
     * Name of portlet  init parameter for View page
     */
    public static final String PARAM_VIEW_PAGE = "viewPage";

	private ServletContextProvider servletContextProvider;
	private PortletResourceURLFactory resourceURLFactory;
	private String wicketFilterPath;
	private String wicketFilterQuery;
	private HashMap defaultPages = new HashMap();

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
			throw new PortletException("Portlet " + config.getPortletName()
					+ " is incorrectly configured. Init parameter "
					+ PARAM_SERVLET_CONTEXT_PROVIDER + " not specified, nor as context parameter "
					+ ServletContextProvider.class.getName() + " or as property in "+WICKET_PORTLET_PROPERTIES + " in the classpath.");
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
			throw new PortletException("Portlet " + config.getPortletName()
					+ " is incorrectly configured. Init parameter "
					+ PARAM_PORTLET_RESOURCE_URL_FACTORY + " not specified, nor as context parameter "
					+ PortletResourceURLFactory.class.getName() + " or as property in "+WICKET_PORTLET_PROPERTIES + " in the classpath.");
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
		
		defaultPages.put(PARAM_VIEW_PAGE,config.getInitParameter(PARAM_VIEW_PAGE));
		defaultPages.put(PARAM_ACTION_PAGE,config.getInitParameter(PARAM_ACTION_PAGE));
		defaultPages.put(PARAM_CUSTOM_PAGE,config.getInitParameter(PARAM_CUSTOM_PAGE));
		defaultPages.put(PARAM_HELP_PAGE,config.getInitParameter(PARAM_HELP_PAGE));
		defaultPages.put(PARAM_EDIT_PAGE,config.getInitParameter(PARAM_EDIT_PAGE));
        
		validateDefaultPages(defaultPages, wicketFilterPath, wicketFilterQuery);
	}
	
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
			return wicketFilterPath.substring(0,wicketFilterPath.length()-1)+"?";
		}
	}
	
	protected String fixWicketUrl(String url, String wicketFilterPath, String wicketFilterQuery)
	{
		if (url == null)
		{
			return wicketFilterPath;
		}
		else if (!url.startsWith(wicketFilterPath))
		{
			if ((url+"/").equals(wicketFilterPath))
			{
				// hack around "old" style wicket home url's without trailing '/' which would lead to a redirect to the real home path anyway
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
	
	protected void validateDefaultPages(Map defaultPages, String wicketFilterPath, String wicketFilterQuery)
	{
		String viewPage = fixWicketUrl((String)defaultPages.get(PARAM_VIEW_PAGE), wicketFilterPath, wicketFilterQuery);
		defaultPages.put(PARAM_VIEW_PAGE, viewPage.startsWith(wicketFilterPath) ? viewPage : wicketFilterPath);

		String defaultPage = (String)defaultPages.get(PARAM_ACTION_PAGE);
		if (defaultPage == null)
		{
			defaultPages.put(PARAM_ACTION_PAGE, viewPage);
		}
		else
		{
			defaultPage = fixWicketUrl(defaultPage, wicketFilterPath, wicketFilterQuery);
			defaultPages.put(PARAM_ACTION_PAGE, defaultPage.startsWith(wicketFilterPath) ? defaultPage : viewPage);
		}
		
		defaultPage = (String)defaultPages.get(PARAM_CUSTOM_PAGE);
		if (defaultPage == null)
		{
			defaultPages.put(PARAM_CUSTOM_PAGE, viewPage);
		}
		else
		{
			defaultPage = fixWicketUrl(defaultPage, wicketFilterPath, wicketFilterQuery);
			defaultPages.put(PARAM_CUSTOM_PAGE, defaultPage.startsWith(wicketFilterPath) ? defaultPage : viewPage);
		}
		
		defaultPage = (String)defaultPages.get(PARAM_HELP_PAGE);
		if (defaultPage == null)
		{
			defaultPages.put(PARAM_HELP_PAGE, viewPage);
		}
		else
		{
			defaultPage = fixWicketUrl(defaultPage, wicketFilterPath, wicketFilterQuery);
			defaultPages.put(PARAM_HELP_PAGE, defaultPage.startsWith(wicketFilterPath) ? defaultPage : viewPage);
		}

		defaultPage = (String)defaultPages.get(PARAM_EDIT_PAGE);
		if (defaultPage == null)
		{
			defaultPages.put(PARAM_EDIT_PAGE, viewPage);
		}
		else
		{
			defaultPage = fixWicketUrl(defaultPage, wicketFilterPath, wicketFilterQuery);
			defaultPages.put(PARAM_EDIT_PAGE, defaultPage.startsWith(wicketFilterPath) ? defaultPage : viewPage);
		}		
	}

	protected Properties getWicketPortletProperties(Properties properties) throws PortletException
	{
		if (properties == null)
		{
			properties = new Properties();
		}
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(WICKET_PORTLET_PROPERTIES);
		if (is != null)
		{
			try
			{
				properties.load(is);
			}
			catch (IOException e)
			{
				throw new PortletException("Failed to load WicketPortlet.properties from classpath", e);
			}
		}
		return properties;
	}
	
	protected String getContextProviderClassNameParameter(PortletConfig config)
	{
		return config.getInitParameter(PARAM_SERVLET_CONTEXT_PROVIDER);
	}

	protected String getPortletResourceURLFactoryClassNameParameter(PortletConfig config)
	{
		return config.getInitParameter(PARAM_PORTLET_RESOURCE_URL_FACTORY);
	}

	protected ServletContextProvider getServletContextProvider()
	{
		return servletContextProvider;
	}

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
	
	protected String getWicketConfigParameter(PortletRequest request, String paramName, String defaultValue)
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
	
	protected String getWicketURL(PortletRequest request, String pageType, String defaultPage)
	{
		String wicketURL = null;
		if (request instanceof ActionRequest)
		{
			wicketURL = request.getParameter((String)request.getAttribute(WicketPortlet.WICKET_URL_PORTLET_PARAMETER_ATTR));
		}
		else
		{
			wicketURL = request.getParameter((String)request.getAttribute(WicketPortlet.WICKET_URL_PORTLET_PARAMETER_ATTR)+request.getPortletMode().toString());
		}
        if (wicketURL == null)
        {
        	wicketURL = getWicketConfigParameter(request, CONFIG_PARAM_PREFIX+pageType, defaultPage);
        }
        return wicketURL;
	}

	protected void doView(RenderRequest request, RenderResponse response) throws PortletException,
			IOException
	{
		processRequest(request, response, VIEW_REQUEST, PARAM_VIEW_PAGE);
	}

	protected void doEdit(RenderRequest request, RenderResponse response) throws PortletException,
			IOException
	{
		processRequest(request, response, EDIT_REQUEST, PARAM_EDIT_PAGE);
	}

	protected void doHelp(RenderRequest request, RenderResponse response) throws PortletException,
			IOException
	{
		processRequest(request, response, HELP_REQUEST, PARAM_HELP_PAGE);
	}

	protected void doCustom(RenderRequest request, RenderResponse response) throws PortletException,
			IOException
	{
		processRequest(request, response, CUSTOM_REQUEST, PARAM_CUSTOM_PAGE);
	}

	public void processAction(ActionRequest request, ActionResponse response)
			throws PortletException, IOException
	{
		processRequest(request, response, ACTION_REQUEST, PARAM_ACTION_PAGE);
	}

	protected void processRequest(PortletRequest request, PortletResponse response,
			String requestType, String pageType) throws PortletException, IOException
	{
		String wicketURL = null;
		String wicketFilterPath = null;
		String wicketFilterQuery = null;
		
		request.setAttribute(WICKET_URL_PORTLET_PARAMETER_ATTR, getWicketUrlPortletParameter(request));
		
		wicketURL = getWicketURL(request, pageType, getDefaultPage(pageType));
		wicketFilterPath = getWicketConfigParameter(request, WICKET_FILTER_PATH, this.wicketFilterPath);			
		wicketFilterQuery = getWicketConfigParameter(request, WICKET_FILTER_QUERY, this.wicketFilterQuery);
		
		boolean actionRequest = ACTION_REQUEST.equals(requestType);
		
		WicketResponseState responseState = new WicketResponseState();

		request.setAttribute(RESPONSE_STATE_ATTR, responseState);
		request.setAttribute(RESOURCE_URL_FACTORY_ATTR, resourceURLFactory);
		request.setAttribute(REQUEST_TYPE_ATTR, requestType);
		String portletResourceURL = request.getParameter(PORTLET_RESOURCE_URL_PARAMETER);
		if (portletResourceURL != null)
		{
			request.setAttribute(PORTLET_RESOURCE_URL_ATTR,portletResourceURL);
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
					processActionResponseState(wicketURL, wicketFilterPath, wicketFilterQuery, (ActionRequest)request, (ActionResponse)response, responseState);
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
						redirectLocation = fixWicketUrl(redirectLocation, wicketFilterPath, wicketFilterQuery);
						boolean validWicketUrl = redirectLocation.startsWith(wicketFilterPath);
						if (portletResourceURL != null)
						{
							if (validWicketUrl)
							{
								HashMap parameters = new HashMap(2);
								parameters.put((String)request.getAttribute(WicketPortlet.WICKET_URL_PORTLET_PARAMETER_ATTR)+request.getPortletMode().toString(), new String[]{redirectLocation});
								parameters.put(PORTLET_RESOURCE_URL_PARAMETER, new String[]{"true"});
								redirectLocation = resourceURLFactory.createResourceURL(getPortletConfig(), (RenderRequest)request, (RenderResponse)response, parameters);
							}
							getHttpServletResponse(this, request, response).sendRedirect(redirectLocation);
						}
						else if (validWicketUrl && ((previousURL == null || previousURL != redirectLocation)))
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
	
	protected void processActionResponseState(String wicketURL, String wicketFilterPath, String wicketFilterQuery, ActionRequest request, ActionResponse response, WicketResponseState responseState) throws PortletException, IOException
	{
		if ( responseState.getRedirectLocation() != null )
		{
			wicketURL = fixWicketUrl(responseState.getRedirectLocation(), wicketFilterPath, wicketFilterQuery);
			if (wicketURL.startsWith(wicketFilterPath))
			{
				response.setRenderParameter((String)request.getAttribute(WicketPortlet.WICKET_URL_PORTLET_PARAMETER_ATTR)+request.getPortletMode().toString(), wicketURL);
			}
			else
			{
				response.sendRedirect(responseState.getRedirectLocation());
			}
		}
	}
}
