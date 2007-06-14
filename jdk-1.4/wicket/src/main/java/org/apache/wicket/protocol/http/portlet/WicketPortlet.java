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
	public static final String WICKET_URL_PORTLET_PARAMETER = "wicketUrl";
	public static final String PORTLET_RESOURCE_URL_PARAMETER = "resourceUrl";
	public static final String WICKET_FILTER_PATH_PARAM = "wicketFilterPath";
	public static final String PARAM_SERVLET_CONTEXT_PROVIDER = "ServletContextProvider";
	public static final String PARAM_PORTLET_RESOURCE_URL_FACTORY = "PortletResourceURLFactory";
	public static final String ACTION_REQUEST = "ACTION";
	public static final String VIEW_REQUEST = "VIEW";
	public static final String RESOURCE_REQUEST = "RESOURCE";
	public static final String CUSTOM_REQUEST = "CUSTOM";
	public static final String EDIT_REQUEST = "EDIT";
	public static final String HELP_REQUEST = "HELP";
	public static final String REQUEST_TYPE_ATTR = WicketPortlet.class.getName() + "REQUEST_TYPE";
	public static final String RESPONSE_STATE_ATTR = WicketResponseState.class.getName();
	public static final String RESOURCE_URL_FACTORY_ATTR = PortletResourceURLFactory.class.getName();
	public static final String PORTLET_RESOURCE_URL_ATTR = "resourceUrl";

	private ServletContextProvider servletContextProvider;
	private PortletResourceURLFactory resourceURLFactory;
	private String wicketFilterPath;

	public void init(PortletConfig config) throws PortletException
	{
		super.init(config);
		String contextProviderClassName = getContextProviderClassNameParameter(config);
		if (contextProviderClassName == null)
		{
			contextProviderClassName = config.getPortletContext().getInitParameter(
					ServletContextProvider.class.getName());
		}
		if (contextProviderClassName == null)
		{
			throw new PortletException("Portlet " + config.getPortletName()
					+ " is incorrectly configured. Init parameter "
					+ PARAM_SERVLET_CONTEXT_PROVIDER + " not specified, nor context parameter "
					+ ServletContextProvider.class.getName() + ".");
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
			throw new PortletException("Portlet " + config.getPortletName()
					+ " is incorrectly configured. Init parameter "
					+ PARAM_PORTLET_RESOURCE_URL_FACTORY + " not specified, nor context parameter "
					+ PortletResourceURLFactory.class.getName() + ".");
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
		
		wicketFilterPath = config.getInitParameter(WICKET_FILTER_PATH_PARAM);
		if (wicketFilterPath == null || wicketFilterPath.length() == 0)
		{
			wicketFilterPath = "/";
		}
		else
		{
			if (!wicketFilterPath.startsWith("/"))
			{
				wicketFilterPath = "/" + wicketFilterPath;
			}
			if (wicketFilterPath.endsWith("*"))
			{
				wicketFilterPath = wicketFilterPath.substring(0, wicketFilterPath.length() - 1);
			}
			if (!wicketFilterPath.endsWith("/"))
			{
				wicketFilterPath += "/";
			}
		}
	}

	public void processAction(ActionRequest request, ActionResponse response)
			throws PortletException, IOException
	{
		processRequest(request, response, ACTION_REQUEST);
	}

	protected void doView(RenderRequest request, RenderResponse response) throws PortletException,
			IOException
	{
		processRequest(request, response, VIEW_REQUEST);
	}

	public void destroy()
	{
		resourceURLFactory = null;
		servletContextProvider = null;
		super.destroy();
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

	protected void processRequest(PortletRequest request, PortletResponse response,
			String requestType) throws PortletException, IOException
	{
		String wicketURL = request.getParameter(WICKET_URL_PORTLET_PARAMETER);

		if (wicketURL == null)
		{
			wicketURL = wicketFilterPath;
		}

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
					processActionResponseState(wicketURL, (ActionRequest)request, (ActionResponse)response, responseState);
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
					if (responseState.getRedirectLocation() != null && responseState.getRedirectLocation().startsWith(wicketFilterPath)
							&& ((previousURL == null || previousURL != responseState.getRedirectLocation())))
					{
						previousURL = wicketURL;
						wicketURL = responseState.getRedirectLocation();
						((RenderResponse)response).reset();
						responseState.reset();
						continue;
					}
				}
				break;
			}
		}
	}
	
	protected void processActionResponseState(String wicketURL, ActionRequest request, ActionResponse response, WicketResponseState responseState) throws PortletException, IOException
	{
		if ( responseState.getRedirectLocation() != null )
		{
			wicketURL = responseState.getRedirectLocation();
			if (wicketURL.startsWith(request.getContextPath()+wicketFilterPath))
			{
				response.setRenderParameter(WICKET_URL_PORTLET_PARAMETER, wicketURL.substring(request.getContextPath().length()));
			}
			else
			{
				response.sendRedirect(responseState.getRedirectLocation());
			}
		}
	}
}
