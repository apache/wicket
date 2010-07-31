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
package org.apache.wicket.request.http.handler;

import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.request.IRequestCycle;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.http.WebResponse;

/**
 * A request handler that redirects to the given url.
 * 
 * the url should be one of the following:
 * <ul>
 * <li>Fully qualified "http://foo.com/bar"</li>
 * <li>Relative to the Wicket filter/servlet, e.g. "?wicket:interface=foo", "mounted_page"</li>
 * <li>Absolute within your web application's <strong>context root</strong>, e.g. "/foo.html"</li>
 * </ul>
 * 
 * @author igor.vaynberg
 * @author jcompagner
 */
public class RedirectRequestHandler implements IRequestHandler
{

	private final String redirectUrl;
	private final int status;

	/**
	 * @param redirectUrl
	 *            URL to redirect to.
	 */
	public RedirectRequestHandler(String redirectUrl)
	{
		this(redirectUrl, HttpServletResponse.SC_MOVED_TEMPORARILY);
	}

	/**
	 * @param redirectUrl
	 *            URL to redirect to.
	 * @param status
	 *            301 (Moved permanently) or 302 (Moved temporarily)
	 */
	public RedirectRequestHandler(String redirectUrl, int status)
	{
		if (status != HttpServletResponse.SC_MOVED_PERMANENTLY &&
			status != HttpServletResponse.SC_MOVED_TEMPORARILY)
		{
			throw new IllegalStateException("Status must be either 301 or 302, but was: " + status);
		}
		this.redirectUrl = redirectUrl;
		this.status = status;
	}

	/**
	 * @see org.apache.wicket.request.IRequestHandler#detach(org.apache.wicket.request.cycle.RequestCycle)
	 */
	public void detach(IRequestCycle requestCycle)
	{
	}

	/**
	 * FIXME javadoc - what's special about this implementation?
	 * 
	 * @see org.apache.wicket.request.IRequestHandler#respond(org.apache.wicket.request.cycle.RequestCycle)
	 */
	public void respond(IRequestCycle requestCycle)
	{
		final String location;

		if (redirectUrl.startsWith("/"))
		{
			// context-absolute url
			location = requestCycle.getUrlRenderer().renderContextPathRelativeUrl(redirectUrl,
				requestCycle.getRequest());
		}
		else if (redirectUrl.contains("://"))
		{
			// absolute url
			location = redirectUrl;
		}
		else
		{
			// relative url, servlet container will translate to absolute per as
			// per the servlet spec
			location = redirectUrl;
		}

		WebResponse response = (WebResponse)requestCycle.getResponse();

		if (status == HttpServletResponse.SC_MOVED_TEMPORARILY)
		{
			response.sendRedirect(location);
		}
		else
		{
			response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
			response.setHeader("Location", location);
		}
	}
}
