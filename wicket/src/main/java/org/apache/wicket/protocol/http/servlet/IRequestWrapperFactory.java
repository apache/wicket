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

import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.Application;

/**
 * Added to WicketFilter and allow to preprocess the request if needed. E.g. to replace the request
 * object with a wrapper that manages XForwarded headers properly.
 * 
 * @author Juergen Donnerstag
 */
public interface IRequestWrapperFactory
{
	/**
	 * @param request
	 * @return Either the original request or a wrapper
	 */
	HttpServletRequest getWrapper(final HttpServletRequest request);

	/**
	 * Servlets and Filters are treated essentially the same with Wicket. This is the entry point
	 * for both of them and allows to initialize the object.
	 * 
	 * @see #init(FilterConfig)
	 * 
	 * @param application
	 * @param isServlet
	 *            True if Servlet, false of Filter
	 * @param filterConfig
	 */
	void init(Application application, boolean isServlet, FilterConfig filterConfig);
}