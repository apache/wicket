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
package org.apache.wicket.request.handler.render;

import java.util.List;

import org.apache.wicket.Application;
import org.apache.wicket.Session;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler.RedirectPolicy;
import org.apache.wicket.feedback.FeedbackCollector;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.protocol.http.BufferedWebResponse;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link PageRenderer} for web applications.
 * 
 * @author Matej Knopp
 */
public class WebPageRenderer extends PageRenderer
{
	private static final Logger logger = LoggerFactory.getLogger(WebPageRenderer.class);

	/**
	 * Construct.
	 * 
	 * @param renderPageRequestHandler
	 */
	public WebPageRenderer(RenderPageRequestHandler renderPageRequestHandler)
	{
		super(renderPageRequestHandler);
	}

	private boolean isAjax(RequestCycle requestCycle)
	{
		boolean isAjax = false;

		Request request = requestCycle.getRequest();
		if (request instanceof WebRequest)
		{
			WebRequest webRequest = (WebRequest)request;
			isAjax = webRequest.isAjax();
		}

		return isAjax;
	}

	/**
	 * 
	 * @param url
	 * @param response
	 */
	protected void storeBufferedResponse(Url url, BufferedWebResponse response)
	{
		WebApplication.get().storeBufferedResponse(getSessionId(), url, response);
	}

	protected BufferedWebResponse getAndRemoveBufferedResponse(Url url)
	{
		return WebApplication.get().getAndRemoveBufferedResponse(getSessionId(), url);
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
		// get the page before checking for a scheduled request handler because
		// the page may call setResponsePage in its constructor
		IRequestablePage requestablePage = getPage();

		IRequestHandler scheduled = requestCycle.getRequestHandlerScheduledAfterCurrent();

		if (scheduled != null)
		{
			// no need to render
			return null;
		}

		// keep the original response
		final WebResponse originalResponse = (WebResponse) requestCycle.getResponse();

		// buffered web response for page
		BufferedWebResponse response = new BufferedWebResponse(originalResponse);

		// keep the original base URL
		Url originalBaseUrl = requestCycle.getUrlRenderer().setBaseUrl(targetUrl);

		try
		{
			requestCycle.setResponse(response);
			requestablePage.renderPage();

			if (scheduled == null && requestCycle.getRequestHandlerScheduledAfterCurrent() != null)
			{
				// This is a special case.
				// During page render another request handler got scheduled and will want to overwrite
				// the response, so we need to let it.
				// Just preserve the meta data headers
				originalResponse.reset(); // clear the initial actions because they are already copied into the new response's actions
				response.writeMetaData(originalResponse);
				return null;
			}
			else
			{
				return response;
			}
		}
		finally
		{
			// restore original response and base URL
			requestCycle.setResponse(originalResponse);
			requestCycle.getUrlRenderer().setBaseUrl(originalBaseUrl);
		}
	}

	/**
	 * 
	 * @param url
	 * @param requestCycle
	 */
	protected void redirectTo(Url url, RequestCycle requestCycle)
	{
		bindSessionIfNeeded();

		WebResponse response = (WebResponse)requestCycle.getResponse();
		String relativeUrl = requestCycle.getUrlRenderer().renderUrl(url);
		response.sendRedirect(relativeUrl);
	}

	/**
	 * Bind the session if there are feedback messages pending.
	 * https://issues.apache.org/jira/browse/WICKET-5165
	 */
	private void bindSessionIfNeeded()
	{
		// check for session feedback messages only
		FeedbackCollector collector = new FeedbackCollector();
		List<FeedbackMessage> feedbackMessages = collector.collect();
		if (feedbackMessages.size() > 0)
		{
			Session.get().bind();
		}
	}

	/*
	 * TODO: simplify the code below. See WICKET-3347
	 */
	@Override
	public void respond(RequestCycle requestCycle)
	{
		Url currentUrl = requestCycle.getUrlRenderer().getBaseUrl();
		Url targetUrl = requestCycle.mapUrlFor(getRenderPageRequestHandler());

		//
		// the code below is little hairy but we have to handle 3 redirect policies,
		// 3 rendering strategies and two kind of requests (ajax and normal)
		//

		// try to get an already rendered buffered response for current URL
		BufferedWebResponse bufferedResponse = getAndRemoveBufferedResponse(currentUrl);

		boolean isAjax = isAjax(requestCycle);

		boolean shouldPreserveClientUrl = ((WebRequest)requestCycle.getRequest()).shouldPreserveClientUrl();

		if (bufferedResponse != null)
		{
			logger.warn("The Buffered response should be handled by BufferedResponseRequestHandler");
			// if there is saved response for this URL render it
			bufferedResponse.writeTo((WebResponse)requestCycle.getResponse());
		}
		else {
			RedirectPolicy redirectPolicy = getRedirectPolicy();

			boolean onePassRender = isOnePassRender();
			boolean isRedirectToRender = isRedirectToRender();
			boolean redirectToBuffer = isRedirectToBuffer();

			boolean targetEqualsCurrentUrl = targetUrl.equals(currentUrl);
			boolean isNewPageInstance = getPageProvider().isNewPageInstance();
			boolean isPageStateless = getPage().isPageStateless();

			if (shouldRenderPageAndWriteResponse(isAjax, onePassRender, isRedirectToRender, redirectPolicy, shouldPreserveClientUrl, targetEqualsCurrentUrl, isNewPageInstance, isPageStateless)) //
			{
				BufferedWebResponse response = renderPage(currentUrl, requestCycle);
				if (response != null)
				{
					response.writeTo((WebResponse)requestCycle.getResponse());
				}
			}
			else {
				boolean sessionTemporary = isSessionTemporary();
				if (shouldRedirectToTargetUrl(isAjax, redirectPolicy, isRedirectToRender, targetEqualsCurrentUrl, isNewPageInstance, isPageStateless, sessionTemporary))
				{
					redirectTo(targetUrl, requestCycle);

					// note: if we had session here we would render the page to buffer and then redirect to
					// URL generated *after* page has been rendered (the statelessness may change during
					// render). this would save one redirect because now we have to render to URL generated
					// *before* page is rendered, render the page, get URL after render and if the URL is
					// different (meaning page is not stateless), save the buffer and redirect again (which
					// is pretty much what the next step does)
				}
				else
				{
					if (redirectToBuffer == false && logger.isDebugEnabled())
					{
						String details = String.format("redirect strategy: '%s', isAjax: '%s', redirect policy: '%s', " +
								"current url: '%s', target url: '%s', is new: '%s', is stateless: '%s', is temporary: '%s'",
								Application.get().getRequestCycleSettings().getRenderStrategy(),
								isAjax, redirectPolicy, currentUrl, targetUrl, isNewPageInstance,
										isPageStateless, sessionTemporary);
						logger.debug("Falling back to Redirect_To_Buffer render strategy because none of the conditions " +
								"matched. Details: " + details);
					}

					// force creation of possible stateful page to get the final target url
					getPage();

					Url renderTargetUrl = requestCycle.mapUrlFor(getRenderPageRequestHandler());

					// redirect to buffer
					BufferedWebResponse response = renderPage(renderTargetUrl, requestCycle);

					if (response == null)
					{
						return;
					}

					if (currentUrl.equals(renderTargetUrl))
					{
						// no need to redirect when both urls are exactly the same
						response.writeTo((WebResponse)requestCycle.getResponse());
					}
					// if page is still stateless after render
					else if (isPageStateless && !enableRedirectForStatelessPage())
					{
						// we don't want the redirect to happen for stateless page
						// example:
						// when a normal mounted stateful page is hit at /mount/point
						// wicket renders the page to buffer and redirects to /mount/point?12
						// but for stateless page the redirect is not necessary
						// also for listener interface on stateful page we want to redirect
						// after the listener is invoked, but on stateless page the user
						// must ask for redirect explicitly
						response.writeTo((WebResponse)requestCycle.getResponse());
					}
					else
					{
						storeBufferedResponse(renderTargetUrl, response);

						redirectTo(renderTargetUrl, requestCycle);
					}
				}
			}
		}
	}

	// if
	//		render policy is always-redirect
	// 	or
	//		it's redirect-to-render
	//	or
	//		its ajax and the targetUrl matches current url
	// 	or
	//		targetUrl DONT matches current url and
	//				is new page instance
	//			or
	//				session is temporary and page is stateless
	// just redirect

	protected boolean shouldRedirectToTargetUrl(boolean ajax, RedirectPolicy redirectPolicy, boolean redirectToRender, boolean targetEqualsCurrentUrl, boolean newPageInstance, boolean pageStateless,boolean sessionTemporary) {
		return alwaysRedirect(redirectPolicy) //
						||
						redirectToRender //
						||
						(ajax && targetEqualsCurrentUrl)
						||
						(!targetEqualsCurrentUrl //
							&&
							(newPageInstance || (sessionTemporary && pageStateless))
						);
		// if target URL is different and session is temporary and page is stateless
		// this is special case when page is stateless but there is no session so we can't
		// render it to buffer

		// alternatively if URLs are different and we have a page class and not an instance we
		// can redirect to the url which will instantiate the instance of us
	}

	// if
	// 		the policy is never to redirect
	// 	or
	//		its NOT ajax and
	//				one pass render mode is on and NOT forced to redirect
	//			or
	//				the targetUrl matches current url and page is NOT stateless and NOT a new instance
	//	or
	//		the targetUrl matches current url and it's redirect-to-render
	//	or
	//  	the request determines that the current url should be preserved
	//	just render the page
	protected boolean shouldRenderPageAndWriteResponse(boolean ajax, boolean onePassRender, boolean redirectToRender, RedirectPolicy redirectPolicy, boolean shouldPreserveClientUrl, boolean targetEqualsCurrentUrl, boolean newPageInstance, boolean pageStateless) {
		return neverRedirect(redirectPolicy)
			||
			(!ajax &&
				(
					(onePassRender && notForcedRedirect(redirectPolicy))
					||
					(targetEqualsCurrentUrl && notNewAndNotStatelessPage(newPageInstance, pageStateless)))
				)
			||
			(targetEqualsCurrentUrl && redirectToRender)
			||
			shouldPreserveClientUrl;
	}

	private static boolean notNewAndNotStatelessPage(boolean newPageInstance, boolean pageStateless) {
		return !newPageInstance && !pageStateless;
	}

	private static boolean neverRedirect(RedirectPolicy redirectPolicy) {
		return redirectPolicy == RedirectPolicy.NEVER_REDIRECT;
	}

	private static boolean alwaysRedirect(RedirectPolicy redirectPolicy) {
		return redirectPolicy == RedirectPolicy.ALWAYS_REDIRECT;
	}

	private static boolean notForcedRedirect(RedirectPolicy redirectPolicy) {
		return !alwaysRedirect(redirectPolicy);
	}

}
