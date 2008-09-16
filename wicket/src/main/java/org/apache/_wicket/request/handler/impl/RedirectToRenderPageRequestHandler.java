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
package org.apache._wicket.request.handler.impl;

import org.apache._wicket.IPage;
import org.apache._wicket.PageParameters;
import org.apache._wicket.RequestCycle;
import org.apache._wicket.request.Url;
import org.apache._wicket.request.handler.PageRequestHandler;
import org.apache._wicket.request.response.WebResponse;

/**
 * Simple request handler that does a redirect to page URL and renders the page.
 * 
 * @author Matej Knopp
 */
public class RedirectToRenderPageRequestHandler implements PageRequestHandler
{
	private final IPage page;

	/**
	 * Construct.
	 * 
	 * @param page
	 */
	public RedirectToRenderPageRequestHandler(IPage page)
	{
		if (page == null)
		{
			throw new IllegalArgumentException("Argument 'page' may not be null.");
		}
		this.page = page;
	}

	public IPage getPage()
	{
		return page;
	}

	public Class<? extends IPage> getPageClass()
	{
		return page.getClass();
	}

	public String getPageMapName()
	{
		return page.getPageMapName();
	}

	public PageParameters getPageParameters()
	{
		return page.getPageParameters();
	}

	public void detach(RequestCycle requestCycle)
	{
		page.detach();
	}

	private void redirectTo(Url url, RequestCycle requestCycle)
	{
		WebResponse response = (WebResponse)requestCycle.getResponse();
		String relativeUrl = requestCycle.getUrlRenderer().renderUrl(url);
		response.sendRedirect(relativeUrl);
	}

	public void respond(RequestCycle requestCycle)
	{
		RenderPageRequestHandler handler = new RenderPageRequestHandler(page);
		Url url = requestCycle.urlFor(handler);
		if (requestCycle.getRequest().getUrl().equals(url))
		{
			// the URLs match - RenderPageRequestTarget will render the page immediately
			requestCycle.replaceAllRequestHandlers(handler);
		}
		else
		{
			redirectTo(url, requestCycle);
		}
	}
}
