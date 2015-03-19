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
package org.apache.wicket.protocol.http.servlet;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.string.Strings;

/**
 * Represents additional error parameters present in a {@link ServletRequest} when the servlet
 * container is handling an error or a forward to an error page mapped by {@code error-page} element
 * in {@code web.xml}.
 * 
 * See documentation for the following request attributes for the values stored in this object:
 * <ul>
 * <li>javax.servlet.error.status_code</li>
 * <li>javax.servlet.error.message</li>
 * <li>javax.servlet.error.request_uri</li>
 * <li>javax.servlet.error.servlet_name</li>
 * <li>javax.servlet.error.exception_type</li>
 * <li>javax.servlet.error.exception</li>
 * </ul>
 * 
 */
public class ForwardAttributes
{
	// javax.servlet.forward.request_uri
	private final String requestUri;

	// javax.servlet.forward.servlet_path
	private final String servletPath;

	// javax.servlet.forward.context_path
	private final String contextPath;

	// javax.servlet.forward.query_string
	private final String queryString;

	/**
	 * Constructor.
	 * 
	 * @param requestUri
	 * @param servletPath
	 * @param contextPath
	 * @param queryString
	 */
	private ForwardAttributes(String requestUri, String servletPath, String contextPath,
		String queryString)
	{
		this.requestUri = requestUri;
		this.servletPath = servletPath;
		this.contextPath = contextPath;
		this.queryString = queryString;
	}

	/**
	 * Gets requestUri.
	 * 
	 * @return requestUri
	 */
	public String getRequestUri()
	{
		return requestUri;
	}

	/**
	 * Gets servletPath.
	 * 
	 * @return servletPath
	 */
	public String getServletPath()
	{
		return servletPath;
	}

	/**
	 * Gets contextPath.
	 * 
	 * @return contextPath
	 */
	public String getContextPath()
	{
		return contextPath;
	}

	/**
	 * Gets the query string.
	 * 
	 * @return the query string
	 */
	public String getQueryString()
	{
		return queryString;
	}

	/**
	 * Factory for creating instances of this class.
	 * 
	 * @param request
	 * @return instance of request contains forward attributes or {@code null} if it does not.
	 */
	public static ForwardAttributes of(HttpServletRequest request, String filterPrefix)
	{
		Args.notNull(request, "request");

		final String requestUri = DispatchedRequestUtils.getRequestUri(request, "javax.servlet.forward.request_uri", filterPrefix);
		final String servletPath = (String)request.getAttribute("javax.servlet.forward.servlet_path");
		final String contextPath = (String)request.getAttribute("javax.servlet.forward.context_path");
		final String queryString = (String)request.getAttribute("javax.servlet.forward.query_string");

		if (!Strings.isEmpty(requestUri) || !Strings.isEmpty(servletPath) ||
			!Strings.isEmpty(contextPath) || !Strings.isEmpty(queryString))
		{
			return new ForwardAttributes(requestUri, servletPath, contextPath, queryString);
		}
		return null;
	}

	@Override
	public String toString()
	{
		return "ForwardAttributes [requestUri=" + requestUri + ", servletPath=" + servletPath +
			", contextPath=" + contextPath + ", queryString=" + queryString + "]";
	}
}
