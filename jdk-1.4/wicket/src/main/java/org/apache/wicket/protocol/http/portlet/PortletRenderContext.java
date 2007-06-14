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

import java.util.HashMap;

import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletURL;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.portals.bridges.common.PortletResourceURLFactory;
import org.apache.portals.bridges.util.PortletWindowUtils;
import org.apache.portals.bridges.util.ServletPortletSessionProxy;
import org.apache.wicket.RenderContext;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.protocol.http.WebRequest;

/**
 * @author Ate Douma
 */
public class PortletRenderContext extends RenderContext
{
	private final PortletConfig portletConfig;
	private final RenderRequest renderRequest;
	private final RenderResponse renderResponse;
	private final PortletResourceURLFactory resourceURLFactory;
	private final IHeaderResponse headerResponse;
	private String portletWindowId;
	private String[] lastEncodedUrl = new String[2];

	private static final String SERVLET_RESOURCE_URL_PORTLET_WINDOW_ID_PREFIX = "/ps:";
	
    public static HttpServletRequest getPortletServletRequest(ServletContext context, HttpServletRequest request, String filterPath)
    {
    	String pathInfo = request.getRequestURI().substring(request.getContextPath().length()+filterPath.length());
    	if (pathInfo != null && pathInfo.startsWith(SERVLET_RESOURCE_URL_PORTLET_WINDOW_ID_PREFIX))
    	{
    		String portletWindowId = null;
    		int nextPath = pathInfo.indexOf('/',1);
    		if (nextPath > -1)
    		{
    			portletWindowId = pathInfo.substring(SERVLET_RESOURCE_URL_PORTLET_WINDOW_ID_PREFIX.length(),nextPath);
    			pathInfo = pathInfo.substring(nextPath);
    		}
    		else
    		{
    			portletWindowId = pathInfo.substring(SERVLET_RESOURCE_URL_PORTLET_WINDOW_ID_PREFIX.length());
    			pathInfo = null;
    		}
    		return new PortletServletRequestWrapper(context,request,ServletPortletSessionProxy.createProxy(request, portletWindowId), "/"+filterPath.substring(0,filterPath.length()-1), pathInfo);
    	}
    	return request;
    }
    
	public PortletRenderContext(final PortletConfig portletConfig, final RenderRequest renderRequest, final RenderResponse renderResponse, final PortletResourceURLFactory resourceURLFactory, final IHeaderResponse headerResponse)
	{
		super();
		this.portletConfig = portletConfig;
		this.renderRequest = renderRequest;
		this.renderResponse = renderResponse;
		this.resourceURLFactory = resourceURLFactory;
		this.headerResponse = headerResponse;
	}

	public String getLastEncodedPath(String url)
	{
		if (url != null && lastEncodedUrl != null && url.equals(lastEncodedUrl[0]))
		{
			return lastEncodedUrl[1];
		}
		return null;
	}
	
	protected String saveLastEncodedUrl(String url, String path)
	{
		lastEncodedUrl[0] = url;
		lastEncodedUrl[1] = path;
		return url;
	}
	
	/**
	 * @see org.apache.wicket.RenderContext#encodeActionURL(java.lang.CharSequence)
	 */
	public CharSequence encodeActionURL(CharSequence path)
	{
		if ( path != null )
		{
			path = getQualifiedPath(path);
			if (renderResponse != null)
			{
				PortletURL url = renderResponse.createActionURL();
				url.setParameter(WicketPortlet.WICKET_URL_PORTLET_PARAMETER, path.toString());
				path = saveLastEncodedUrl(url.toString(), path.toString());
			}
		}
		return path;
	}

	/**
	 * @see org.apache.wicket.RenderContext#encodeMarkupId(java.lang.String)
	 */
	public String encodeMarkupId(String markupId)
	{
		if ( markupId != null )
		{
			markupId = getNamespace() + "_" + markupId;
		}
		return markupId;
	}

	/**
	 * @see org.apache.wicket.RenderContext#encodeRenderURL(java.lang.CharSequence)
	 */
	public CharSequence encodeRenderURL(CharSequence path)
	{
		if ( path != null )
		{			
			path = getQualifiedPath(path);
			if (renderResponse != null)
			{
				PortletURL url = renderResponse.createRenderURL();
				url.setParameter(WicketPortlet.WICKET_URL_PORTLET_PARAMETER, path.toString());
				path = saveLastEncodedUrl(url.toString(), path.toString());
				path = url.toString();
			}
		}
		return path;
	}

	/**
	 * @see org.apache.wicket.RenderContext#encodeResourceURL(java.lang.CharSequence)
	 */
	public CharSequence encodeResourceURL(CharSequence path)
	{
		if ( path != null )
		{
			path = getQualifiedPath(path);
			if (renderResponse != null)
			{
				try
				{
					HashMap parameters = new HashMap(1);
					parameters.put(WicketPortlet.WICKET_URL_PORTLET_PARAMETER, new String[]{path.toString()});
					parameters.put(WicketPortlet.PORTLET_RESOURCE_URL_PARAMETER, new String[]{"true"});
					path = saveLastEncodedUrl(resourceURLFactory.createResourceURL(portletConfig, renderRequest, renderResponse, parameters), path.toString());
				}
				catch (PortletException e)
				{
					throw new RuntimeException(e);
				}
			}
		}
		return path;
	}

	/**
	 * @see org.apache.wicket.RenderContext#encodeSharedResourceURL(java.lang.CharSequence)
	 */
	public CharSequence encodeSharedResourceURL(CharSequence path)
	{
		if ( path != null )
		{
			String url = (SERVLET_RESOURCE_URL_PORTLET_WINDOW_ID_PREFIX.substring(1) + getPortletWindowId() + "/" + path);
			return saveLastEncodedUrl(url,url);
		}
		return null;
	}

	/**
	 * @see org.apache.wicket.RenderContext#getHeaderResponse()
	 */
	public IHeaderResponse getHeaderResponse()
	{
		return headerResponse;
	}

	/**
	 * @see org.apache.wicket.RenderContext#getNamespace()
	 */
	public CharSequence getNamespace()
	{
		return renderResponse != null ? renderResponse.getNamespace() : "";
	}
	
	/**
	 * @see org.apache.wicket.RenderContext#isEmbedded()
	 */
	public boolean isEmbedded()
	{
		return true;
	}
	
	protected String getQualifiedPath(CharSequence path)
	{
		HttpServletRequest request = ((WebRequest)RequestCycle.get().getRequest()).getHttpServletRequest();
		if (renderRequest == null)
		{
			return request.getContextPath() + request.getServletPath() + path;
		}
		return request.getServletPath() + path;
	}
	
	protected String getPortletWindowId()
	{
		if (portletWindowId == null)
		{
	    	portletWindowId = PortletWindowUtils.getPortletWindowId(renderRequest.getPortletSession());
		}
		return portletWindowId;
	}
}
