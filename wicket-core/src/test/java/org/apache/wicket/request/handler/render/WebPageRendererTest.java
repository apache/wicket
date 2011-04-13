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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.wicket.protocol.http.BufferedWebResponse;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.UrlRenderer;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.IPageProvider;
import org.apache.wicket.request.handler.RenderPageRequestHandler;
import org.apache.wicket.request.handler.RenderPageRequestHandler.RedirectPolicy;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.settings.IRequestCycleSettings;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the calculation whether or not to redirect or directly render a page
 */
public class WebPageRendererTest
{

	private RenderPageRequestHandler handler;
	private RequestCycle requestCycle;
	private UrlRenderer urlRenderer;
	private WebRequest request;
	private WebResponse response;

	/**
	 * Common setup
	 */
	@Before
	public void before()
	{
		IPageProvider provider = mock(IPageProvider.class);

		IRequestablePage page = mock(IRequestablePage.class);
		when(provider.getPageInstance()).thenReturn(page);

		handler = new RenderPageRequestHandler(provider);

		requestCycle = mock(RequestCycle.class);
		urlRenderer = mock(UrlRenderer.class);
		when(requestCycle.getUrlRenderer()).thenReturn(urlRenderer);

		request = mock(WebRequest.class);
		when(requestCycle.getRequest()).thenReturn(request);

		response = mock(WebResponse.class);
		when(requestCycle.getResponse()).thenReturn(response);

	}

	/**
	 * Tests that when {@link IRequestCycleSettings.RenderStrategy#ONE_PASS_RENDER} is configured
	 * there wont be a redirect issued
	 */
	@Test
	public void testOnePassRender()
	{

		PageRenderer renderer = new TestPageRenderer(handler)
		{
			@Override
			protected boolean isOnePassRender()
			{
				return true;
			}
		};

		when(urlRenderer.getBaseUrl()).thenReturn(Url.parse("base"));

		when(requestCycle.mapUrlFor(eq(handler))).thenReturn(Url.parse("base/a"));

		when(request.shouldPreserveClientUrl()).thenReturn(false);

		renderer.respond(requestCycle);

		verify(response).write(any(byte[].class));
		verify(response, never()).sendRedirect(anyString());
	}

	/**
	 * Tests that when {@link RenderPageRequestHandler#getRedirectPolicy()} is
	 * {@link RedirectPolicy#NEVER_REDIRECT} there wont be a redirect issued
	 */
	@Test
	public void testRedirectPolicyNever()
	{

		PageRenderer renderer = new TestPageRenderer(handler)
		{
			@Override
			protected RedirectPolicy getRedirectPolicy()
			{
				return RedirectPolicy.NEVER_REDIRECT;
			}

		};

		when(urlRenderer.getBaseUrl()).thenReturn(Url.parse("base"));

		when(requestCycle.mapUrlFor(eq(handler))).thenReturn(Url.parse("base/a"));

		when(request.shouldPreserveClientUrl()).thenReturn(false);

		renderer.respond(requestCycle);

		verify(response).write(any(byte[].class));
		verify(response, never()).sendRedirect(anyString());
	}

	/**
	 * Configures common methods which are used by all tests
	 */
	private static class TestPageRenderer extends WebPageRenderer
	{
		public TestPageRenderer(RenderPageRequestHandler renderPageRequestHandler)
		{
			super(renderPageRequestHandler);
		}

		@Override
		protected BufferedWebResponse getAndRemoveBufferedResponse(Url url)
		{
			return null;
		}

		@Override
		protected BufferedWebResponse renderPage(Url targetUrl, RequestCycle requestCycle)
		{
			BufferedWebResponse webResponse = super.renderPage(targetUrl, requestCycle);
			webResponse.write("some response".getBytes());
			return webResponse;
		}

	}
}
