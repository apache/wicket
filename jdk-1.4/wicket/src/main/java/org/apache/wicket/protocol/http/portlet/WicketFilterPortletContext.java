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
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.portals.bridges.common.PortletResourceURLFactory;
import org.apache.portals.bridges.util.ServletPortletSessionProxy;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.settings.IRequestCycleSettings;

/**
 * @author Ate Douma
 * @Id@
 */
public class WicketFilterPortletContext
{
	public void initFilter(FilterConfig filterConfig, WebApplication webApplication) throws ServletException
    {
        webApplication.getRequestCycleSettings().setRenderStrategy(IRequestCycleSettings.REDIRECT_TO_RENDER);
    }

    public void setupFilter(FilterConfig config, FilterRequestContext filterRequestContext, String filterPath) throws IOException, ServletException
    {
    	PortletConfig portletConfig = (PortletConfig)filterRequestContext.getRequest().getAttribute("javax.portlet.config");
        if ( portletConfig != null )
        {
        	WicketResponseState responseState = (WicketResponseState)filterRequestContext.getRequest().getAttribute(WicketPortlet.RESPONSE_STATE_ATTR);
        	filterRequestContext.setRequest(new PortletServletRequestWrapper(config.getServletContext(),filterRequestContext.getRequest(), ServletPortletSessionProxy.createProxy(filterRequestContext.getRequest())));
            if ( WicketPortlet.ACTION_REQUEST.equals(filterRequestContext.getRequest().getAttribute(WicketPortlet.REQUEST_TYPE_ATTR)))
            {
            	filterRequestContext.setResponse(new PortletActionServletResponseWrapper(filterRequestContext.getResponse(), responseState));
            }
            else
            {   
            	filterRequestContext.setResponse(new PortletRenderServletResponseWrapper(filterRequestContext.getResponse(), (RenderResponse)filterRequestContext.getRequest().getAttribute("javax.portlet.response"),responseState));
            }            
        }
        else
        {
        	filterRequestContext.setRequest(PortletRenderContext.getPortletServletRequest(config.getServletContext(),filterRequestContext.getRequest(), filterPath));
        }
    }
    
    public boolean createPortletRenderContext(WebRequest request, WebResponse response)
    {
    	HttpServletRequest servletRequest = request.getHttpServletRequest();
    	PortletConfig portletConfig = (PortletConfig)servletRequest.getAttribute("javax.portlet.config");
        if ( portletConfig != null )
        {
            if ( WicketPortlet.ACTION_REQUEST.equals(servletRequest.getAttribute(WicketPortlet.REQUEST_TYPE_ATTR)))
            {
            	new PortletRenderContext(null, null, null, null, null);
            }
            else
            {
            	PortletResourceURLFactory resourceURLFactory = (PortletResourceURLFactory)servletRequest.getAttribute(WicketPortlet.RESOURCE_URL_FACTORY_ATTR);
            	RenderRequest renderRequest = (RenderRequest)servletRequest.getAttribute("javax.portlet.request");
            	RenderResponse renderResponse = (RenderResponse)servletRequest.getAttribute("javax.portlet.response");
            	boolean isResourceRequest = "true".equals(servletRequest.getAttribute(WicketPortlet.PORTLET_RESOURCE_URL_ATTR));
            	new PortletRenderContext(portletConfig, renderRequest, renderResponse, resourceURLFactory, isResourceRequest ? null : new EmbeddedPortletHeaderResponse(response));
            }
            return true;
        }
        return false;
    }
}
