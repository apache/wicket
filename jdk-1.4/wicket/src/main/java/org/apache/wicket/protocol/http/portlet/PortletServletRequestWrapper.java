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
    private String contextPath;
    private String servletPath;
    private String pathInfo;
    private String requestURI;
    private String queryString;
    private HttpSession session;
    
    private static String decodePathInfo(HttpServletRequest request, String filterPath)
    {
    	String pathInfo = request.getRequestURI().substring(request.getContextPath().length()+filterPath.length());
    	return pathInfo == null || pathInfo.length() < 2 ? null : pathInfo;
    }
    
    private static String makeServletPath(String filterPath)
    {
    	return "/"+filterPath.substring(0,filterPath.length()-1);
    }
    
    protected PortletServletRequestWrapper(ServletContext context, HttpSession proxiedSession, HttpServletRequest request, String filterPath)
    {
    	super(request);
        this.context = context;
        this.session = proxiedSession;
        if ( proxiedSession == null )
        {
            this.session = request.getSession(false);
        }
    	this.servletPath = makeServletPath(filterPath);
        if ((this.contextPath = (String) request.getAttribute("javax.servlet.include.context_path")) != null)
        {
        	this.requestURI = (String) request.getAttribute("javax.servlet.include.request_uri");
        	this.queryString = (String) request.getAttribute("javax.servlet.include.query_string");
        }
        else if ((this.contextPath = (String) request.getAttribute("javax.servlet.forward.context_path")) != null)
        {
        	this.requestURI = (String) request.getAttribute("javax.servlet.forward.request_uri");
        	this.queryString = (String) request.getAttribute("javax.servlet.forward.query_string");
        }
        else
        {
        	this.contextPath = request.getContextPath();
        	this.requestURI = request.getRequestURI();
        	this.queryString = request.getQueryString();
        }
    }

    public PortletServletRequestWrapper(ServletContext context, HttpServletRequest request, HttpSession proxiedSession, String filterPath)
    {
    	this(context, proxiedSession, request, filterPath);

    	String pathInfo = this.requestURI.substring(this.contextPath.length()+filterPath.length());
    	this.pathInfo = pathInfo == null || pathInfo.length() < 2 ? null : pathInfo;
    }

    public PortletServletRequestWrapper(ServletContext context, HttpServletRequest request, HttpSession proxiedSession, String filterPath, String pathInfo)
    {
    	this(context, proxiedSession, request, filterPath);

    	this.pathInfo = pathInfo;
    	// override requestURI
        this.requestURI = this.contextPath+this.servletPath+(pathInfo!=null?pathInfo:"");
    }
    
    public String getContextPath()
    {
    	return contextPath;
    }
    
	public String getServletPath()
    {
        return servletPath;
    }

    public String getPathInfo()
    {
        return pathInfo;
    }

    public String getRequestURI()
    {
        return requestURI;
    }
    
    public String getQueryString()
    {
    	return queryString;
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
