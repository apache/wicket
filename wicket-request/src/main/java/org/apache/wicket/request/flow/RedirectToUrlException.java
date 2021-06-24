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
package org.apache.wicket.request.flow;

import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.request.RequestHandlerExecutor.ReplaceHandlerException;
import org.apache.wicket.request.http.handler.RedirectRequestHandler;

/**
 * Causes Wicket to interrupt current request processing and send a redirect to the given url.
 * 
 * Use this if you want to redirect to an external or none Wicket url. If you want to redirect to a
 * page use the <em>org.apache.wicket.RestartResponseException</em>
 *
 * Also see org.apache.wicket.RestartResponseAtInterceptPageException
 */
public class RedirectToUrlException extends ReplaceHandlerException
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @param redirectUrl
	 *            URL to redirect to.
	 */
	public RedirectToUrlException(final String redirectUrl)
	{
		this(redirectUrl, HttpServletResponse.SC_MOVED_TEMPORARILY);
	}

	/**
	 * Construct.
	 * 
	 * @param redirectUrl
	 *            URL to redirect to.
	 * @param statusCode
	 *            301 (Moved permanently) or 302 (Moved temporarily)
	 */
	public RedirectToUrlException(final String redirectUrl, final int statusCode)
	{
		super(new RedirectRequestHandler(redirectUrl, statusCode), true);
	}


	/**
	 * Construct.
	 *
	 * @param redirectUrl
	 *            URL to redirect to.
	 * @param statusCode
	 *            301 (Moved permanently) or 302 (Moved temporarily)
	 * @param mode
	 *            The way to made the redirect - via sendRedirect() or directly via setStatus()+setHeader("Location", ...)
	 */
	public RedirectToUrlException(final String redirectUrl, final int statusCode, RedirectRequestHandler.Mode mode)
	{
		super(new RedirectRequestHandler(redirectUrl, statusCode).mode(mode), true);
	}
}
