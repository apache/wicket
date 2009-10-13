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
package org.apache.wicket.ng.request.handler.impl.render;

import org.apache.wicket.ng.Application;
import org.apache.wicket.ng.ThreadContext;
import org.apache.wicket.ng.protocol.http.WebApplication;
import org.apache.wicket.ng.request.Url;
import org.apache.wicket.ng.request.component.RequestablePage;
import org.apache.wicket.ng.request.cycle.RequestCycle;
import org.apache.wicket.ng.request.handler.impl.RenderPageRequestHandler;
import org.apache.wicket.ng.request.handler.impl.RenderPageRequestHandler.RedirectPolicy;
import org.apache.wicket.ng.request.response.BufferedWebResponse;
import org.apache.wicket.ng.request.response.Response;
import org.apache.wicket.ng.request.response.WebResponse;
import org.apache.wicket.ng.settings.RequestCycleSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebRenderPageRequestHandlerDelegate extends RenderPageRequestHandlerDelegate
{

	public WebRenderPageRequestHandlerDelegate(RenderPageRequestHandler renderPageRequestHandler)
	{
		super(renderPageRequestHandler);
	}

	public RequestablePage getPage()
	{
		return getPageProvider().getPageInstance();
	}

	private boolean isOnePassRender()
	{
		return Application.get().getRequestCycleSettings().getRenderStrategy() == RequestCycleSettings.RenderStrategy.ONE_PASS_RENDER;
	}

	private boolean isRedirectToRender()
	{
		return Application.get().getRequestCycleSettings().getRenderStrategy() == RequestCycleSettings.RenderStrategy.REDIRECT_TO_RENDER;
	}

	private boolean isRedirectToBuffer()
	{
		return Application.get().getRequestCycleSettings().getRenderStrategy() == RequestCycleSettings.RenderStrategy.REDIRECT_TO_BUFFER;
	}

	private void renderPage()
	{
		getPage().renderPage();
	}

	protected String getSessionId()
	{
		return ThreadContext.getSession().getId();
	}

	protected boolean isSessionTemporary()
	{
		return ThreadContext.getSession().isTemporary();
	}

	protected BufferedWebResponse getAndRemoveBufferedResponse(Url url)
	{
		return WebApplication.get().getAndRemoveBufferedResponse(getSessionId(), url);
	}

	protected void storeBufferedResponse(Url url, BufferedWebResponse response)
	{
		WebApplication.get().storeBufferedResponse(getSessionId(), url, response);
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
			getPage().renderPage();
			return response;
		}
		finally
		{
			// restore original response and base URL
			requestCycle.setResponse(originalResponse);
			requestCycle.getUrlRenderer().setBaseUrl(originalBaseUrl);
		}
	}

	private void redirectTo(Url url, RequestCycle requestCycle)
	{
		WebResponse response = (WebResponse)requestCycle.getResponse();
		String relativeUrl = requestCycle.getUrlRenderer().renderUrl(url);
		response.sendRedirect(relativeUrl);
	}

	@Override
	public void respond(RequestCycle requestCycle)
	{
		Url currentUrl = requestCycle.getRequest().getUrl();
		Url targetUrl = requestCycle.urlFor(getRenderPageRequestHandler());

		//
		// the code below is little hairy but we have to handle 3 redirect policies
		// and 3 rendering strategies
		//

		// try to get an already rendered buffered response for current URL
		BufferedWebResponse bufferedResponse = getAndRemoveBufferedResponse(currentUrl);

		if (bufferedResponse != null)
		{
			logger.warn("The Buffered response should be handled by BufferedResponseRequestHandler");
			// if there is saved response for this URL render it
			bufferedResponse.writeTo((WebResponse)requestCycle.getResponse());
		}
		else if (getRedirectPolicy() == RedirectPolicy.NEVER_REDIRECT ||
			isOnePassRender() // 
			||
			(targetUrl.equals(currentUrl) && !getPage().isPageStateless()) //
			||
			(targetUrl.equals(currentUrl) && isRedirectToRender() && getPage().isPageStateless()))
		{
			// if the policy is never to redirect
			// or one pass render mode is on
			// or the targetUrl matches current url and the page is not stateless
			// or the targetUrl matches current url, page is stateless but it's redirect-to-render
			// just render the page
			renderPage();
		}
		else if (!targetUrl.equals(currentUrl) //
			&&
			(getRedirectPolicy() == RedirectPolicy.ALWAYS_REDIRECT || isRedirectToRender()))
		{
			// if target URL is different
			// and render policy is always-redirect or it's redirect-to-render
			redirectTo(targetUrl, requestCycle);
		}
		else if (!targetUrl.equals(currentUrl) //
			&&
			isSessionTemporary() && getPage().isPageStateless())
		{
			// if target URL is different and session is temporary and page is stateless
			// this is special case when page is stateless but there is no session so we can't
			// render it to buffer

			// note: if we had session here we would render the page to buffer and then redirect to
			// URL generated
			// *after* page has been rendered (the statelessness may change during render). this
			// would save one redirect
			// because now we have to render to URL generated *before* page is rendered, render the
			// page, get URL
			// after render and if the URL is different (meaning page is not stateless), save the
			// buffer and redirect again
			// (which is pretty much what the next step does)
			redirectTo(targetUrl, requestCycle);
		}
		else if (isRedirectToBuffer())
		{
			// redirect to buffer
			BufferedWebResponse response = renderPage(targetUrl, requestCycle);

			// check if the url hasn't changed after page has been rendered
			// (i.e. the stateless flag might have changed which could result in different page url)
			Url targetUrl2 = requestCycle.urlFor(getRenderPageRequestHandler());

			if (targetUrl.getSegments().equals(targetUrl2.getSegments()) == false)
			{
				// the amount of segments is different - generated relative URLs will not work, we
				// need to rerender the page. This shouldn't happen, but in theory it can - with
				// RequestHandlerEncoders that produce different URLs with different amount of
				// segments for stateless and stateful pages
				response = renderPage(targetUrl2, requestCycle);
			}

			// if page is still stateless after render
			if (getPage().isPageStateless() && !enableRedirectForStatelessPage())
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
		else
		{
			throw new IllegalStateException("Unknown RenderStrategy.");
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

	private static Logger logger = LoggerFactory.getLogger(WebRenderPageRequestHandlerDelegate.class);
}
