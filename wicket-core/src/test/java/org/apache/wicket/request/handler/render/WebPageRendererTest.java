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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.wicket.core.request.handler.IPageProvider;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler.RedirectPolicy;
import org.apache.wicket.protocol.http.BufferedWebResponse;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.UrlRenderer;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

/**
 * Tests for the calculation whether or not to redirect or directly render a page
 */
class WebPageRendererTest
{

	private RenderPageRequestHandler handler;
	private RequestCycle requestCycle;
	private UrlRenderer urlRenderer;
	private WebRequest request;
	private WebResponse response;
	private IRequestablePage page;
	private IPageProvider provider;

	/**
	 * Common setup
	 */
	@BeforeEach
	void before()
	{
		provider = mock(IPageProvider.class);

		page = mock(IRequestablePage.class);
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
	 * Tests that when {@link org.apache.wicket.settings.RequestCycleSettings.RenderStrategy#ONE_PASS_RENDER}
	 * is configured there wont be a redirect issued
	 */
	@Test
	void testOnePassRender()
	{

		TestPageRenderer renderer = new TestPageRenderer(handler);
		renderer.onePassRender = true;

		when(urlRenderer.getBaseUrl()).thenReturn(Url.parse("base"));

		when(requestCycle.mapUrlFor(eq(handler))).thenReturn(Url.parse("base/a"));

		when(request.shouldPreserveClientUrl()).thenReturn(false);

		renderer.respond(requestCycle);

		verify(response).write(any(byte[].class));
		verify(response, never()).sendRedirect(anyString());
	}

	/**
	 * Tests that when {@link org.apache.wicket.settings.RequestCycleSettings.RenderStrategy#ONE_PASS_RENDER}
	 * is configured there will be a redirect issued if the protocols of the current and target urls
	 * are different
	 *
	 * https://issues.apache.org/jira/browse/WICKET-5522
	 */
	@Test
	void testOnePassRenderDifferentProtocols()
	{
		final AtomicBoolean responseBuffered = new AtomicBoolean(false);

		PageRenderer renderer = new TestPageRenderer(handler)
		{
			@Override
			protected boolean isOnePassRender()
			{
				return true;
			}

			@Override
			protected void storeBufferedResponse(Url url, BufferedWebResponse response)
			{
				responseBuffered.set(true);
			}
		};

		// uses HTTPS
		when(urlRenderer.getBaseUrl()).thenReturn(Url.parse("https://host/base"));

		when(requestCycle.mapUrlFor(eq(handler))).thenReturn(Url.parse("http://host/base/a"));

		when(request.shouldPreserveClientUrl()).thenReturn(false);

		renderer.respond(requestCycle);

		verify(response, never()).write(any(byte[].class));
		verify(response).sendRedirect(isNull());
		assertTrue(responseBuffered.get());
	}

	/**
	 * Tests that even when {@link org.apache.wicket.settings.RequestCycleSettings.RenderStrategy#ONE_PASS_RENDER}
	 * is configured but the {@link RedirectPolicy} says that it needs to redirect it will redirect.
	 */
	@Test
	void testOnePassRenderWithAlwaysRedirect()
	{

		PageRenderer renderer = new TestPageRenderer(handler)
		{
			@Override
			protected boolean isOnePassRender()
			{
				return true;
			}

			@Override
			protected RedirectPolicy getRedirectPolicy()
			{
				return RedirectPolicy.ALWAYS_REDIRECT;
			}
		};

		when(urlRenderer.getBaseUrl()).thenReturn(Url.parse("base"));

		when(requestCycle.mapUrlFor(eq(handler))).thenReturn(Url.parse("base/a"));

		when(request.shouldPreserveClientUrl()).thenReturn(false);

		renderer.respond(requestCycle);

		verify(response, never()).write(any(byte[].class));
		verify(response).sendRedirect(isNull());
	}

	/**
	 * Tests that when {@link org.apache.wicket.settings.RequestCycleSettings.RenderStrategy#ONE_PASS_RENDER}
	 * is configured but the current request is Ajax then a redirect should be issued
	 */
	@Test
	void testOnePassRenderAndAjaxRequest()
	{

		TestPageRenderer renderer = new TestPageRenderer(handler);
		renderer.onePassRender = true;
		renderer.ajax = true;
		renderer.newPageInstance = true;
		renderer.pageStateless = true;

		when(urlRenderer.getBaseUrl()).thenReturn(Url.parse("base"));

		when(requestCycle.mapUrlFor(eq(handler))).thenReturn(Url.parse("base/a"));

		renderer.respond(requestCycle);

		verify(response).sendRedirect(isNull());
		verify(response, never()).write(any(byte[].class));

	}


	/**
	 * Tests that when {@link RenderPageRequestHandler#getRedirectPolicy()} is
	 * {@link RedirectPolicy#NEVER_REDIRECT} there wont be a redirect issued
	 */
	@Test
	void testRedirectPolicyNever()
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
	 * Tests that when the fromUrl and toUrl are the same and
	 * {@link org.apache.wicket.settings.RequestCycleSettings.RenderStrategy#REDIRECT_TO_RENDER}
	 * is configured there wont be a redirect issued
	 */
	@Test
	void testSameUrlsAndRedirectToRender()
	{

		PageRenderer renderer = new TestPageRenderer(handler)
		{
			@Override
			protected boolean isRedirectToRender()
			{
				return true;
			}

		};

		Url sameUrl = Url.parse("anything");

		when(urlRenderer.getBaseUrl()).thenReturn(sameUrl);

		when(requestCycle.mapUrlFor(eq(handler))).thenReturn(sameUrl);

		when(request.shouldPreserveClientUrl()).thenReturn(false);

		renderer.respond(requestCycle);

		verify(response).write(any(byte[].class));
		verify(response, never()).sendRedirect(anyString());
	}

	/**
	 * Tests that when the fromUrl and toUrl are the same and the page is not stateless there wont
	 * be a redirect issued
	 */
	@Test
	void testSameUrlsAndStatefulPage()
	{
		when(provider.isNewPageInstance()).thenReturn(false);
		when(page.isPageStateless()).thenReturn(false);

		PageRenderer renderer = new TestPageRenderer(handler);

		Url sameUrl = Url.parse("anything");

		when(urlRenderer.getBaseUrl()).thenReturn(sameUrl);

		when(requestCycle.mapUrlFor(eq(handler))).thenReturn(sameUrl);

		when(request.shouldPreserveClientUrl()).thenReturn(false);

		renderer.respond(requestCycle);

		verify(response).write(any(byte[].class));
		verify(response, never()).sendRedirect(anyString());
	}

	/**
	 * Tests that when {@link WebRequest#shouldPreserveClientUrl()} is <code>true</code> no redirect
	 * should occur
	 */
	@Test
	void testShouldPreserveClientUrl()
	{

		TestPageRenderer renderer = new TestPageRenderer(handler);
		renderer.shouldPreserveClientUrl = true;

		when(urlRenderer.getBaseUrl()).thenReturn(Url.parse("something"));

		when(requestCycle.mapUrlFor(eq(handler))).thenReturn(Url.parse("different"));

		renderer.respond(requestCycle);

		verify(response).write(any(byte[].class));
		verify(response, never()).sendRedirect(anyString());
	}

    /**
     * Tests that when {@link WebRequest#shouldPreserveClientUrl()} is <code>true</code>
     * but {@link RenderPageRequestHandler#getRedirectPolicy()} is
     * {@link RedirectPolicy#ALWAYS_REDIRECT} a redirect must be issued
     *
     * https://issues.apache.org/jira/browse/WICKET-5486
     */
    @Test
	void testShouldPreserveClientUrlOverruledByRedirectPolicyAlwaysRedirect()
    {
        TestPageRenderer renderer = new TestPageRenderer(handler);
        renderer.shouldPreserveClientUrl = true;
        renderer.redirectPolicy = RedirectPolicy.ALWAYS_REDIRECT;

        when(urlRenderer.getBaseUrl()).thenReturn(Url.parse("something"));

        when(requestCycle.mapUrlFor(eq(handler))).thenReturn(Url.parse("different"));

        renderer.respond(requestCycle);

        verify(response, never()).write(any(byte[].class));
        verify(response).sendRedirect(isNull());
    }

	/**
	 * Tests that when {@link RenderPageRequestHandler#getRedirectPolicy()} is
	 * {@link RedirectPolicy#ALWAYS_REDIRECT} there a redirect must be issued
	 */
	@Test
	void testRedirectPolicyAlways()
	{
		TestPageRenderer renderer = new TestPageRenderer(handler);
		renderer.redirectPolicy = RedirectPolicy.ALWAYS_REDIRECT;

		when(urlRenderer.getBaseUrl()).thenReturn(Url.parse("base"));

		when(requestCycle.mapUrlFor(eq(handler))).thenReturn(Url.parse("base/a"));

		when(request.shouldPreserveClientUrl()).thenReturn(true);

		renderer.respond(requestCycle);

		verify(response, never()).write(any(byte[].class));
		verify(response).sendRedirect(isNull());
	}

	/**
	 * Tests that when the current request is Ajax then a redirect should happen
	 */
	@Test
	void testSameUrlsAndAjaxRequest()
	{
		TestPageRenderer renderer = new TestPageRenderer(handler);
		renderer.ajax = true;

		Url sameUrl = Url.parse("same");

		when(urlRenderer.getBaseUrl()).thenReturn(sameUrl);

		when(requestCycle.mapUrlFor(eq(handler))).thenReturn(sameUrl);

		renderer.respond(requestCycle);

		verify(response, never()).write(any(byte[].class));
		verify(response).sendRedirect(isNull());
	}

	/**
	 * Tests that when {@link org.apache.wicket.settings.RequestCycleSettings.RenderStrategy#REDIRECT_TO_RENDER}
	 * is configured then no matter what are the fromUrl and toUrl a redirect will happen
	 */
	@Test
	void testRedirectToRender()
	{

		TestPageRenderer renderer = new TestPageRenderer(handler);
		renderer.redirectToRender = true;

		when(provider.getPageInstance()).thenThrow(
			new AssertionFailedError("no page instance should be created"));

		when(urlRenderer.getBaseUrl()).thenReturn(Url.parse("a"));

		when(requestCycle.mapUrlFor(eq(handler))).thenReturn(Url.parse("b"));

		renderer.respond(requestCycle);

		verify(response, never()).write(any(byte[].class));
		verify(response).sendRedirect(isNull());
	}

	/**
	 * Tests that when target URL is different and session is temporary and page is stateless this
	 * is special case when page is stateless but there is no session so we can't render it to
	 * buffer
	 */
	@Test
	void testDifferentUrlsTemporarySessionAndStatelessPage()
	{
		TestPageRenderer renderer = new TestPageRenderer(handler);
		renderer.redirectToBuffer = true;
		renderer.sessionTemporary = true;
		renderer.pageStateless = true;

		when(urlRenderer.getBaseUrl()).thenReturn(Url.parse("a"));

		when(requestCycle.mapUrlFor(eq(handler))).thenReturn(Url.parse("b"));

		renderer.respond(requestCycle);

		verify(response, never()).write(any(byte[].class));
		verify(response).sendRedirect(isNull());
	}

	/**
	 * Tests that when URLs are different and we have a page class and not an instance we can
	 * redirect to the url which will instantiate the instance of us
	 */
	@Test
	void testDifferentUrlsAndNewPageInstance()
	{
		TestPageRenderer renderer = new TestPageRenderer(handler);
		renderer.redirectToBuffer = true;
		renderer.newPageInstance = true;

		when(urlRenderer.getBaseUrl()).thenReturn(Url.parse("a"));

		when(requestCycle.mapUrlFor(eq(handler))).thenReturn(Url.parse("b"));

		renderer.respond(requestCycle);

		verify(response, never()).write(any(byte[].class));
		verify(response).sendRedirect(isNull());
	}

	/**
	 * Tests that when during page render another request handler got scheduled. The handler will
	 * want to overwrite the response, so we need to let it
	 */
	@Test
	void testRedirectToBufferNoPageToRender()
	{
		final AtomicBoolean stored = new AtomicBoolean(false);

		TestPageRenderer renderer = new TestPageRenderer(handler)
		{
			@Override
			protected BufferedWebResponse renderPage(Url targetUrl, RequestCycle requestCycle)
			{
				return null;
			}

			@Override
			protected void storeBufferedResponse(Url url, BufferedWebResponse response)
			{
				stored.set(true);
			}
		};
		renderer.redirectToBuffer = true;

		// needed for earlier checks
		when(urlRenderer.getBaseUrl()).thenReturn(Url.parse("a"));
		when(requestCycle.mapUrlFor(eq(handler))).thenReturn(Url.parse("b"));

		renderer.respond(requestCycle);

		verify(response, never()).write(any(byte[].class));
		verify(response, never()).write(any(CharSequence.class));
		verify(response, never()).sendRedirect(anyString());
		assertFalse(stored.get());
	}

	/**
	 * Tests that when the page is stateless and redirect for stateless pages is disabled then the
	 * page should be written without redirect
	 */
	@Test
	void testRedirectToBufferStatelessPageAndRedirectIsDisabled()
	{
		final AtomicBoolean stored = new AtomicBoolean(false);

		TestPageRenderer renderer = new TestPageRenderer(handler)
		{
			@Override
			protected boolean enableRedirectForStatelessPage()
			{
				return false;
			}

			@Override
			protected void storeBufferedResponse(Url url, BufferedWebResponse response)
			{
				stored.set(true);
			}
		};
		renderer.redirectToBuffer = true;
		renderer.pageStateless = true;

		// needed for earlier checks
		when(urlRenderer.getBaseUrl()).thenReturn(Url.parse("a"));
		when(requestCycle.mapUrlFor(eq(handler))).thenReturn(Url.parse("b"));

		renderer.respond(requestCycle);

		verify(response).write(any(byte[].class));
		verify(response, never()).sendRedirect(anyString());
		assertFalse(stored.get());
	}

	/**
	 * Tests that when the page is stateless and redirect for stateless pages is enabled then there
	 * should be a redirect
	 */
	@Test
	void testRedirectToBufferStatelessPageAndRedirectIsEsabled()
	{
		final AtomicBoolean stored = new AtomicBoolean(false);

		PageRenderer renderer = new TestPageRenderer(handler)
		{
			@Override
			protected boolean isRedirectToBuffer()
			{
				return true;
			}

			@Override
			protected void storeBufferedResponse(Url url, BufferedWebResponse response)
			{
				stored.set(true);
			}
		};

		when(page.isPageStateless()).thenReturn(true);

		// needed for earlier checks
		when(urlRenderer.getBaseUrl()).thenReturn(Url.parse("a"));
		when(requestCycle.mapUrlFor(eq(handler))).thenReturn(Url.parse("b"));

		renderer.respond(requestCycle);

		verify(response, never()).write(any(byte[].class));
		verify(response).sendRedirect(isNull());
		assertTrue(stored.get());
	}

	/**
	 * Tests that when the page is stateful and the urls are different then there should be a
	 * redirect
	 */
	@Test
	void testRedirectToBufferStatefulPage()
	{
		final AtomicBoolean stored = new AtomicBoolean(false);

		PageRenderer renderer = new TestPageRenderer(handler)
		{
			@Override
			protected boolean isRedirectToBuffer()
			{
				return true;
			}

			@Override
			protected void storeBufferedResponse(Url url, BufferedWebResponse response)
			{
				stored.set(true);
			}
		};

		// needed for earlier checks
		when(urlRenderer.getBaseUrl()).thenReturn(Url.parse("a"));
		when(requestCycle.mapUrlFor(eq(handler))).thenReturn(Url.parse("b"));

		renderer.respond(requestCycle);

		verify(response, never()).write(any(byte[].class));
		verify(response).sendRedirect(isNull());
		assertTrue(stored.get());
	}

	@Test
	void testShouldRenderPageAndWriteResponseCondition() {

		TestPageRenderer renderer = new TestPageRenderer(handler);
		// if
		// the policy is never to redirect
		renderer.redirectPolicy = RedirectPolicy.NEVER_REDIRECT;
		renderer.ajax = false;
		renderer.onePassRender = true;
		renderer.redirectToRender = true;
		renderer.shouldPreserveClientUrl = true;
		renderer.newPageInstance = true;
		renderer.pageStateless = true;

		assertTrue(renderer.shouldRenderPageAndWriteResponse(requestCycle,
			Url.parse("test"), Url.parse("test")));

		renderer.ajax = false;
		renderer.onePassRender = false;
		renderer.redirectToRender = false;
		renderer.shouldPreserveClientUrl = false;
		renderer.newPageInstance = false;
		renderer.pageStateless = false;

		assertTrue(renderer.shouldRenderPageAndWriteResponse(requestCycle,
			Url.parse("test1"), Url.parse("test2")));

		// 	or
		//		its NOT ajax and
		//				one pass render mode is on and NOT forced to redirect
		//			or
		//				the targetUrl matches current url and page is NOT stateless and NOT a new instance
		renderer.redirectPolicy = RedirectPolicy.AUTO_REDIRECT;
		renderer.ajax = false;
		renderer.onePassRender = true;

		assertTrue(renderer.shouldRenderPageAndWriteResponse(requestCycle,
			Url.parse("test1"), Url.parse("test2")));

		renderer.redirectPolicy = RedirectPolicy.ALWAYS_REDIRECT;
		renderer.onePassRender = false;

		renderer.newPageInstance = false;
		renderer.pageStateless = false;

		assertTrue(renderer.shouldRenderPageAndWriteResponse(requestCycle,
			Url.parse("test"), Url.parse("test")));

		//	or
		//		the targetUrl matches current url and it's redirect-to-render
		renderer.redirectToRender = true;

		assertTrue(renderer.shouldRenderPageAndWriteResponse(requestCycle,
			Url.parse("test"), Url.parse("test")));

		renderer.pageStateless = true;
		renderer.newPageInstance = true;
		renderer.redirectToRender = true;

		assertTrue(renderer.shouldRenderPageAndWriteResponse(requestCycle,
			Url.parse("test"), Url.parse("test")));

		//	or
		//  	the request determines that the current url should be preserved
		//	just render the page
		renderer.shouldPreserveClientUrl = true;

		renderer.redirectPolicy = RedirectPolicy.AUTO_REDIRECT;
		renderer.ajax = false;
		renderer.onePassRender = false;
		renderer.redirectToRender = false;
		renderer.newPageInstance = true;
		renderer.pageStateless = true;

		assertTrue(renderer.shouldRenderPageAndWriteResponse(requestCycle,
			Url.parse("test1"), Url.parse("test2")));

	}

	@Test
	void testShouldNOTRenderPageAndWriteResponseCondition() {

		TestPageRenderer renderer = new TestPageRenderer(handler);

		// NOT if the policy is never to redirect
		renderer.redirectPolicy = RedirectPolicy.AUTO_REDIRECT;

		// NOT or one pass render mode is on
		// -> or one pass render mode is on and its NOT ajax and its NOT always redirect
		renderer.ajax = true;
		renderer.onePassRender = false;

		// NOT or the targetUrl matches current url and the page is not stateless
		// or the targetUrl matches current url, page is stateless but it's redirect-to-render
		// --> its NOT ajax and
		// 				the targetUrl matches current url and the page is NOT stateless and its NOT a new instance
		// 		or the targetUrl matches current url and it's redirect-to-render

		// or the request determines that the current url should be preserved
		// just render the page
		renderer.shouldPreserveClientUrl = false;

		renderer.redirectToRender = true;
		renderer.newPageInstance = true;
		renderer.pageStateless = true;

		assertFalse(renderer.shouldRenderPageAndWriteResponse(requestCycle,
			Url.parse("test1"), Url.parse("test2")));

		renderer.redirectToRender = false;
		renderer.newPageInstance = false;
		renderer.pageStateless = false;

		assertFalse(renderer.shouldRenderPageAndWriteResponse(requestCycle,
			Url.parse("test"), Url.parse("test")));
	}

	@Test
	void testShouldRenderPageAndWriteResponseVariationIntegrity() {
		int count = countVariations(new ShouldRenderPageAndWriteResponseVariations());
		assertEquals(2 * 2 * 2 * 2 * 2 * 2 * 2 * 3, count);
	}

	@Test
	void shouldRenderPageAndWriteResponseVariation() {

		String match =
						"    X       X   " +
						"    XXXX    XXXX" +
						"    X       X   " +
						"    XXXX    XXXX" +
						"                " +
						"                " +
						"                " +
						"                " +
						"XXXXXXXXXXXXXXXX" +
						"XXXXXXXXXXXXXXXX" +
						"XXXXXXXXXXXXXXXX" +
						"XXXXXXXXXXXXXXXX" +
						"                " +
						"                " +
						"                " +
						"                " +
						"    X   XXXXXXXX" +
						"    XXXXXXXXXXXX" +
						"XXXXXXXXXXXXXXXX" +
						"XXXXXXXXXXXXXXXX" +
						"                " +
						"                " +
						"                " +
						"                ";

		checkVariations(match, new ShouldRenderPageAndWriteResponseVariations());
	}

	@Test
	void testShouldRedirectToTargetUrlIntegrity() {
		int count = countVariations(new ShouldRedirectToTargetUrl());
		assertEquals(2 * 3 * 2 * 2 * 2 * 2 * 2, count);
	}

	@Test
	void testShouldRedirectToTargetUrl() {

		String match =
						"XXXXXXXXXXXXXXXX" +
						"XXXXXXXXXXXXXXXX" +
						"   XXXXX        " +
						"XXXXXXXXXXXXXXXX" +
						"   XXXXX        " +
						"XXXXXXXXXXXXXXXX" +
						"XXXXXXXXXXXXXXXX" +
						"XXXXXXXXXXXXXXXX" +
						"   XXXXXXXXXXXXX" +
						"XXXXXXXXXXXXXXXX" +
						"   XXXXXXXXXXXXX" +
						"XXXXXXXXXXXXXXXX";

		checkVariations(match, new ShouldRedirectToTargetUrl());
	}

	@Test
	void shouldRedirectToTargetUrlCondition() {

		TestPageRenderer renderer = new TestPageRenderer(handler);

		// if
		//		render policy is always-redirect

		renderer.ajax = false;
		renderer.redirectPolicy = RedirectPolicy.ALWAYS_REDIRECT;
		renderer.redirectToRender = false;
		renderer.newPageInstance = false;
		renderer.pageStateless = false;
		renderer.sessionTemporary = false;

		assertTrue(renderer.shouldRedirectToTargetUrl(requestCycle, Url.parse("test1"),
			Url.parse("test2")));

		// 	or
		//		it's redirect-to-render
		renderer.redirectPolicy = RedirectPolicy.AUTO_REDIRECT;
		renderer.redirectToRender = true;

		assertTrue(renderer.shouldRedirectToTargetUrl(requestCycle, Url.parse("test1"),
			Url.parse("test2")));

		//	or
		//		its ajax and the targetUrl matches current url
		renderer.redirectToRender = false;
		renderer.ajax = true;

		assertTrue(renderer.shouldRedirectToTargetUrl(requestCycle, Url.parse("test"),
			Url.parse("test")));

		// 	or
		//		targetUrl DONT matches current url and
		//				is new page instance
		//			or
		//				session is temporary and page is stateless
		renderer.ajax = false;
		renderer.newPageInstance = true;

		assertTrue(renderer.shouldRedirectToTargetUrl(requestCycle, Url.parse("test1"),
			Url.parse("test2")));

		renderer.newPageInstance = false;
		renderer.sessionTemporary = true;
		renderer.pageStateless = true;

		assertTrue(renderer.shouldRedirectToTargetUrl(requestCycle, Url.parse("test1"),
			Url.parse("test2")));
		// just redirect
	}

	@Test
	void shouldNOTRedirectToTargetUrlCondition() {

		TestPageRenderer renderer = new TestPageRenderer(handler);
		
		// NOT if
		//		render policy is always-redirect
		// AND NOT
		//		it's redirect-to-render
		// AND NOT
		//		its ajax and the targetUrl matches current url
		// AND NOT
		//		targetUrl DONT matches current url and
		//				is new page instance
		//			or
		//				session is temporary and page is stateless

		renderer.ajax=false;
		renderer.redirectPolicy=RedirectPolicy.AUTO_REDIRECT;
		renderer.redirectToRender=false;
		renderer.newPageInstance=false;
		renderer.pageStateless=false;
		renderer.sessionTemporary=false;

		assertFalse(renderer.shouldRedirectToTargetUrl(requestCycle, Url.parse("test1"),
				Url.parse("test2")));
	}

	private int countVariations(AbstractVariations variations) {
		int count = 0;
		while (variations.hasNextVariation()) {
			count++;
			variations.nextVariation();
		}
		return count;
	}

	private void checkVariations(String match, AbstractVariations variations) {
		int idx=0;
		while (variations.hasNextVariation()) {
			variations.nextVariation();
			assertEquals(match.charAt(idx) == 'X', variations.getResult(), variations.toString());
			idx++;
		}
	}

	void printVariations(AbstractVariations variations) {
		int idx=0;
		System.out.print("\"");
		while (variations.hasNextVariation()) {
			System.out.print(variations.getResult() ? 'X': ' ');
			variations.nextVariation();
			idx++;
			if (idx>=16) {
				System.out.print("\"+\n\"");
				idx=0;
			}
		}
		System.out.println("\";");
	}

	/**
			 * Tests that when the page is stateful and the urls are the same then the response is written
			 * directly
			 */
	@Test
	void testRedirectToBufferStatefulPageAndSameUrls()
	{
		final AtomicBoolean stored = new AtomicBoolean(false);

		PageRenderer renderer = new TestPageRenderer(handler)
		{
			@Override
			protected boolean isRedirectToBuffer()
			{
				return true;
			}

			@Override
			protected void storeBufferedResponse(Url url, BufferedWebResponse response)
			{
				stored.set(true);
			}
		};

		when(provider.isNewPageInstance()).thenReturn(true);

		Url sameUrl = Url.parse("same");

		// needed for earlier checks
		when(urlRenderer.getBaseUrl()).thenReturn(sameUrl);
		when(requestCycle.mapUrlFor(eq(handler))).thenReturn(sameUrl);

		renderer.respond(requestCycle);

		verify(response).write(any(byte[].class));
		verify(response, never()).sendRedirect(anyString());
		assertFalse(stored.get());
	}

	/**
	 * Tests that when all the conditions fail the redirect_to_buffer should be used as a fallback
	 */
	@Test
	void testRedirectToBufferIsFallback()
	{
		final AtomicBoolean stored = new AtomicBoolean(false);

		PageRenderer renderer = new TestPageRenderer(handler)
		{
			@Override
			protected boolean isRedirectToBuffer()
			{
				return false;
			}

			@Override
			protected boolean isOnePassRender()
			{
				return false;
			}

			@Override
			protected boolean isRedirectToRender()
			{
				return false;
			}

			@Override
			protected boolean isSessionTemporary()
			{
				return false;
			}

			@Override
			protected void storeBufferedResponse(Url url, BufferedWebResponse response)
			{
				stored.set(true);
			}
		};

		when(page.isPageStateless()).thenReturn(false);
		when(provider.isNewPageInstance()).thenReturn(false);

		// needed for earlier checks
		when(urlRenderer.getBaseUrl()).thenReturn(Url.parse("url1"));
		when(requestCycle.mapUrlFor(eq(handler))).thenReturn(Url.parse("url2"));

		renderer.respond(requestCycle);

		verify(response, never()).write(any(byte[].class));
		verify(response).sendRedirect(isNull());
		assertTrue(stored.get());
	}

	/**
	 * Tests that when {@link WebRequest#shouldPreserveClientUrl()} returns {@code true} and the
	 * current request is Ajax then a redirect should be issued
	 */
	@Test
	void testShouldPreserveClientUrlAndAjaxRequest()
	{
		TestPageRenderer renderer = new TestPageRenderer(handler);
		renderer.ajax = true;
		renderer.newPageInstance = true;

		when(urlRenderer.getBaseUrl()).thenReturn(Url.parse("base"));

		when(requestCycle.mapUrlFor(eq(handler))).thenReturn(Url.parse("base/a"));

		when(request.shouldPreserveClientUrl()).thenReturn(true);

		renderer.respond(requestCycle);

		verify(response).sendRedirect(isNull());
		verify(response, never()).write(any(byte[].class));

	}

	/**
	 * Tests that when {@link RedirectPolicy#NEVER_REDIRECT} is configured but the current request
	 * is Ajax then a redirect should still be issued
	 */
	@Test
	void testNeverRedirectButAjaxRequest()
	{
		TestPageRenderer renderer = new TestPageRenderer(handler);
		renderer.redirectPolicy = RedirectPolicy.NEVER_REDIRECT;
		renderer.ajax = true;
		renderer.newPageInstance = true;

		when(urlRenderer.getBaseUrl()).thenReturn(Url.parse("base"));

		when(requestCycle.mapUrlFor(eq(handler))).thenReturn(Url.parse("base/a"));

		renderer.respond(requestCycle);

		verify(response).sendRedirect(isNull());
		verify(response, never()).write(any(byte[].class));

	}
}
