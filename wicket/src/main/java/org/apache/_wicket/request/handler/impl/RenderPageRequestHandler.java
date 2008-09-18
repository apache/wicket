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
import org.apache._wicket.request.RequestHandler;
import org.apache._wicket.request.Url;
import org.apache._wicket.request.handler.PageRequestHandler;
import org.apache._wicket.request.response.BufferedWebResponse;
import org.apache._wicket.request.response.Response;
import org.apache._wicket.request.response.WebResponse;
import org.apache.wicket.Application;
import org.apache.wicket.settings.IRequestCycleSettings;

/**
 * {@link RequestHandler} that renders page instance. Depending on the <code>redirectPolicy</code>
 * flag and current request strategy the handler either just renders the page to the response, or
 * redirects to render the page. <code>REDIRECT_TO_BUFFER</code> strategy is also supported.
 * <p>
 * 
 * @author Matej Knopp
 */
public class RenderPageRequestHandler implements PageRequestHandler
{
	private final IPage page;
	private final RedirectPolicy redirectPolicy;

	/**
	 * Determines whether Wicket does a redirect when rendering a page
	 * 
	 * @author Matej Knopp
	 */
	public enum RedirectPolicy {
		/**
		 * Always redirect if current request URL is different than page URL.
		 */
		ALWAYS_REDIRECT,

		/**
		 * Never redirect - always render the page to current response.
		 */
		NEVER_REDIRECT,

		/**
		 * Redirect if necessary. The redirect will happen when all of the following conditions are
		 * met:
		 * <ul>
		 * <li>current request URL is different than page URL
		 * <li>page is not stateless
		 * <li>render strategy is either REDIRECT_TO_BUFFER or REDIRECT_TO_RENDER </li>
		 */
		AUTO_REDIRECT
	};

	/**
	 * Construct. Renders the page with a redirect if necessary.
	 * 
	 * @param page
	 */
	public RenderPageRequestHandler(IPage page)
	{
		this(page, RedirectPolicy.AUTO_REDIRECT);
	}

	/**
	 * Construct.
	 * 
	 * @param page
	 * @param redirectPolicy
	 */
	public RenderPageRequestHandler(IPage page, RedirectPolicy redirectPolicy)
	{
		if (page == null)
		{
			throw new IllegalArgumentException("Argument 'page' may not be null.");
		}
		if (redirectPolicy == null)
		{
			throw new IllegalArgumentException("Argument 'redirectPolicy' may no be null.");
		}
		this.redirectPolicy = redirectPolicy;
		this.page = page;
	}

	public IPage getPage()
	{
		return page;
	}

	/**
	 * Returns the {@link RedirectPolicy}
	 * 
	 * @return redirect policy
	 */
	public RedirectPolicy getRedirectPolicy()
	{
		return redirectPolicy;
	}

	public void detach(RequestCycle requestCycle)
	{
		page.detach();
	}

	private boolean isOnePassRender()
	{
		return Application.get().getRequestCycleSettings().getRenderStrategy() == IRequestCycleSettings.ONE_PASS_RENDER;
	}

	private boolean isRedirectToRender()
	{
		return Application.get().getRequestCycleSettings().getRenderStrategy() == IRequestCycleSettings.REDIRECT_TO_RENDER;
	}

	private boolean isRedirectToBuffer()
	{
		return Application.get().getRequestCycleSettings().getRenderStrategy() == IRequestCycleSettings.REDIRECT_TO_BUFFER;
	}

	private void renderPage()
	{
		page.renderPage();
	}

	protected BufferedWebResponse getAndRemoveBufferedResponse(Url url)
	{
		// TODO: get and remove buffered response
		return null;
	}

	/**
	 * Renders page to a {@link BufferedWebResponse}. All URLs in page will be rendered relative to
	 * <code>targetUrl</code>
	 * 
	 * @param targetUrl
	 * @param requestCycle
	 * @return BufferedWebResponse containing page body
	 */
	protected BufferedWebResponse renderPage(Url targetUrl, RequestCycle requestCycle)
	{
		// keep the original response
		final Response originalResponse = requestCycle.getResponse();

		// buffered web response for page
		BufferedWebResponse response = new BufferedWebResponse()
		{
			@Override
			public String encodeURL(String url)
			{
				return originalResponse.encodeURL(url);
			}
		};

		// keep the original base URL
		Url originalBaseUrl = requestCycle.getUrlRenderer().setBaseUrl(targetUrl);

		try
		{
			requestCycle.setResponse(response);
			page.renderPage();
			return response;
		}
		finally
		{
			// restore original response and base URL
			requestCycle.setResponse(originalResponse);
			requestCycle.getUrlRenderer().setBaseUrl(originalBaseUrl);
		}
	}

	protected void storeBufferedResponse(Url url, BufferedWebResponse response)
	{
		// TODO:
	}

	private void redirectTo(Url url, RequestCycle requestCycle)
	{
		WebResponse response = (WebResponse)requestCycle.getResponse();
		String relativeUrl = requestCycle.getUrlRenderer().renderUrl(url);
		response.sendRedirect(relativeUrl);
	}

	public void respond(RequestCycle requestCycle)
	{
		Url currentUrl = requestCycle.getRequest().getUrl();
		Url targetUrl = requestCycle.urlFor(this);

		// try to get an already rendered buffered response for current URL
		BufferedWebResponse bufferedResponse = getAndRemoveBufferedResponse(currentUrl);

		if (bufferedResponse != null)
		{
			// if there is saved response for this URL render it
			bufferedResponse.writeTo((WebResponse)requestCycle.getResponse());
		}
		else if (redirectPolicy == RedirectPolicy.NEVER_REDIRECT || isOnePassRender() ||
			targetUrl.equals(currentUrl) || (isRedirectToRender() && page.isPageStateless()))
		{
			// if the policy is never to redirect
			//   or one pass render mode is on 
			//   or the targetUrl matches current url 
			// just render the page
			renderPage();
		}
		else if (redirectPolicy == RedirectPolicy.ALWAYS_REDIRECT || isRedirectToRender())
		{			
			redirectTo(targetUrl, requestCycle);
		}
		else		
		{
			// redirect to buffer
			BufferedWebResponse response = renderPage(targetUrl, requestCycle);

			// check if the url hasn't changed after page has been rendered
			// (i.e. the stateless flag might have changed which could result in different page url)
			Url targetUrl2 = requestCycle.urlFor(this);

			if (targetUrl.getSegments().size() != targetUrl2.getSegments().size())
			{
				// the amount of segments is different - generated relative URLs will not work, we
				// need to rerender the page. This shouldn't happen, but in theory it can - with
				// RequestHandlerEncoders that produce different URLs with different amount of
				// segments for stateless and stateful pages
				response = renderPage(targetUrl2, requestCycle);
			}

			if (page.isPageStateless() && !enableRedirectForStatelessPage())
			{
				// we don't want the redirect to happen for stateless page
				// example:
				// when a normal mounted stateful page is hit at /mount/point
				// wicket renders the page to buffer and redirects to /mount/point?12
				// but for stateless page the redirect is not necessary
				// also for listener interface on stateful page we want to redirect
				// after the listener is invoked, but on stateless page the user
				// must ask for redirect explicitely
				response.writeTo((WebResponse)requestCycle.getResponse());
			}
			else
			{
				storeBufferedResponse(targetUrl2, response);

				redirectTo(targetUrl2, requestCycle);
			}
		}
	}

	/**
	 * When the page renders to buffer and it is still stateless after rendering, this flag
	 * determines whether the redirect will take place or not.
	 * <p>
	 * Normally there is no reason for a stateless page to redirect
	 * 
	 * @return boolean value
	 */
	protected boolean enableRedirectForStatelessPage()
	{
		return false;
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
}
