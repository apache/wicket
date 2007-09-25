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
package org.apache.wicket.request.target.basic;

import org.apache.wicket.IRequestTarget;
import org.apache.wicket.RequestContext;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.Response;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.protocol.http.portlet.PortletRequestContext;

/**
 * A RequestTarget that will sent a redirect url to the browser. Use this if you 
 * want to direct the browser to some external URL, like Google etc, immediantly. 
 * or if you want to redirect to a Wicket page.
 * 
 * If you want to redirect with a delay the {@link RedirectPage} will do a meta
 * tag redirect with a delay.
 * 
 * @author jcompagner
 */
public class RedirectRequestTarget implements IRequestTarget
{

	private final String redirectUrl;

	/**
	 * Your URL should be one of the following:
	 * <ul>
	 * <li>Fully qualified "http://foo.com/bar"</li>
	 * <li>Relative to the Wicket filter/servlet, e.g. "?wicket:interface=foo", "mounted_page"</li>
	 * <li>Absolute within your web application's context root, e.g. "/foo.html"</li>
	 * </ul>
	 * 
	 * @param redirectUrl URL to redirect to.
	 */
	public RedirectRequestTarget(String redirectUrl)
	{
		this.redirectUrl = redirectUrl;

	}

	/**
	 * @see org.apache.wicket.IRequestTarget#detach(org.apache.wicket.RequestCycle)
	 */
	public void detach(RequestCycle requestCycle)
	{
	}

	/**
	 * @see org.apache.wicket.IRequestTarget#respond(org.apache.wicket.RequestCycle)
	 */
	public void respond(RequestCycle requestCycle)
	{
		Response response = requestCycle.getResponse();
		response.reset();
		if (redirectUrl.startsWith("/"))
		{
			RequestContext rc = RequestContext.get();
			String continueTo = null;
			if (rc.isPortletRequest() && ((PortletRequestContext)rc).isEmbedded())
			{
				response.redirect(redirectUrl);	
			}
			else
			{
				response.redirect(RequestCycle.get().getRequest().getRelativePathPrefixToContextRoot() + redirectUrl.substring(1));	
			}
		}
		else if (redirectUrl.startsWith("http://") || redirectUrl.startsWith("https://"))
		{
			response.redirect(redirectUrl);
		}
		else
		{
			response.redirect(RequestCycle.get().getRequest().getRelativePathPrefixToWicketHandler() + redirectUrl);
		}
	}

}
