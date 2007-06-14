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

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;

/**
 * @author Ate Douma
 */
public class PortletServletRequestWrapper extends HttpServletRequestWrapper
{
    private ServletContext context;
    private boolean included;
    private boolean pathOverride;
    private String servletPath;
    private String pathInfo;
    private String requestURI;
    private HttpSession session;
    
    public PortletServletRequestWrapper(ServletContext context, HttpServletRequest request, HttpSession proxiedSession)
    {
        super(request);
        this.context = context;
        session = proxiedSession;
        if ( proxiedSession == null )
        {
            session = request.getSession(false);
        }
        this.included = request.getAttribute("javax.servlet.include.context_path") != null;
    }

    public PortletServletRequestWrapper(ServletContext context, HttpServletRequest request, HttpSession proxiedSession, String servletPath, String pathInfo)
    {
        this(context, request, proxiedSession);
        this.servletPath = servletPath;
        this.pathInfo = pathInfo;
        this.requestURI = request.getContextPath()+servletPath+(pathInfo!=null?pathInfo:"");
        this.pathOverride = true;
    }

    public String getPathInfo()
    {
        return pathOverride ? pathInfo : included ? (String) super.getAttribute("javax.servlet.include.path_info") : super.getPathInfo();
    }

    public String getContextPath()
    {
        return included ? (String) super.getAttribute("javax.servlet.include.context_path") : super.getContextPath();
    }

    public String getRequestURI()
    {
        return pathOverride ? requestURI : included ? (String) super.getAttribute("javax.servlet.include.request_uri") : super.getRequestURI();
    }
    
	public String getServletPath()
    {
        return pathOverride ? servletPath : included ? (String) super.getAttribute("javax.servlet.include.servlet_path") : super.getServletPath();
    }

    public String getQueryString()
    {
        return included ? (String) super.getAttribute("javax.servlet.include.query_string") : super.getQueryString();
    }

    public HttpSession getSession()
    {
        return getSession(true);
    }
    
    public HttpSession getSession(boolean create)
    {
        return session != null ? session : super.getSession(create);
    }
    
    public Object getAttribute(String name)
    {
    	// TODO: check if these can possibly be set/handled
    	// nullifying these for now to prevent Wicket ServletWebRequest.getRelativePathPrefixToWicketHandler() going the wrong route
    	if ("javax.servlet.error.request_uri".equals(name) || "javax.servlet.forward.servlet_path".equals(name))
    	{
    		return null;
    	}
    	return super.getAttribute(name);
    }
}
