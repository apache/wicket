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

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.wicket.protocol.http.BufferedWebResponse;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.UrlRenderer;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.core.request.handler.IPageProvider;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler.RedirectPolicy;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.settings.IRequestCycleSettings;
import org.junit.Assert;
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
	private IRequestablePage page;
	private IPageProvider provider;

	/**
	 * Common setup
	 */
	@Before
	public void before()
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
	 * Tests that even when {@link IRequestCycleSettings.RenderStrategy#ONE_PASS_RENDER} is
	 * configured but the {@link RedirectPolicy} says that it needs to redirect it will redirect.
	 */
	@Test
	public void testOnePassRenderWithAlwaysRedirect()
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
		verify(response).sendRedirect(anyString());
	}

	/**
	 * Tests that when {@link IRequestCycleSettings.RenderStrategy#ONE_PASS_RENDER} is configured
	 * but the current request is Ajax then a redirect should be issued
	 */
	@Test
	public void testOnePassRenderAndAjaxRequest()
	{

		when(provider.isNewPageInstance()).thenReturn(true);
		when(page.isPageStateless()).thenReturn(true);

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

		when(request.isAjax()).thenReturn(true);

		when(request.shouldPreserveClientUrl()).thenReturn(false);

		renderer.respond(requestCycle);

		verify(response).sendRedirect(anyString());
		verify(response, never()).write(any(byte[].class));

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
	 * Tests that when the fromUrl and toUrl are the same and
	 * {@link IRequestCycleSettings.RenderStrategy#REDIRECT_TO_RENDER} is configured there wont be a
	 * redirect issued
	 */
	@Test
	public void testSameUrlsAndRedirectToRender()
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
	public void testSameUrlsAndStatefulPage()
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
	public void testShouldPreserveClientUrl()
	{

		PageRenderer renderer = new TestPageRenderer(handler);

		when(urlRenderer.getBaseUrl()).thenReturn(Url.parse("something"));

		when(requestCycle.mapUrlFor(eq(handler))).thenReturn(Url.parse("different"));

		when(request.shouldPreserveClientUrl()).thenReturn(true);

		renderer.respond(requestCycle);

		verify(response).write(any(byte[].class));
		verify(response, never()).sendRedirect(anyString());
	}

	/**
	 * Tests that when there is already saved buffered response then it will be used without
	 * checking the rendering strategies or redirect policies
	 */
	@Test
	public void testGetAndRemoveBufferedResponse()
	{
		final BufferedWebResponse bufferedResponse = mock(BufferedWebResponse.class);

		PageRenderer renderer = new TestPageRenderer(handler)
		{
			@Override
			protected BufferedWebResponse getAndRemoveBufferedResponse(Url url)
			{
				return bufferedResponse;
			}

		};

		Url sameUrl = Url.parse("anything");

		when(urlRenderer.getBaseUrl()).thenReturn(sameUrl);

		when(requestCycle.mapUrlFor(eq(handler))).thenReturn(sameUrl);

		when(request.shouldPreserveClientUrl()).thenReturn(false);

		renderer.respond(requestCycle);

		verify(bufferedResponse).writeTo(response);
		verify(response, never()).write(any(byte[].class));
		verify(response, never()).sendRedirect(anyString());
	}

	/**
	 * Tests that when {@link RenderPageRequestHandler#getRedirectPolicy()} is
	 * {@link RedirectPolicy#ALWAYS_REDIRECT} there a redirect must be issued
	 */
	@Test
	public void testRedirectPolicyAlways()
	{

		PageRenderer renderer = new TestPageRenderer(handler)
		{
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
		verify(response).sendRedirect(anyString());
	}

	/**
	 * Tests that when the current request is Ajax then a redirect should happen
	 */
	@Test
	public void testSameUrlsAndAjaxRequest()
	{
		PageRenderer renderer = new TestPageRenderer(handler);

		Url sameUrl = Url.parse("same");

		when(urlRenderer.getBaseUrl()).thenReturn(sameUrl);

		when(requestCycle.mapUrlFor(eq(handler))).thenReturn(sameUrl);

		when(request.isAjax()).thenReturn(true);

		renderer.respond(requestCycle);

		verify(response, never()).write(any(byte[].class));
		verify(response).sendRedirect(anyString());
	}

	/**
	 * Tests that when {@link IRequestCycleSettings.RenderStrategy#REDIRECT_TO_RENDER} is configured
	 * then no matter what are the fromUrl and toUrl a redirect will happen
	 */
	@Test
	public void testRedirectToRender()
	{

		PageRenderer renderer = new TestPageRenderer(handler)
		{
			@Override
			protected boolean isRedirectToRender()
			{
				return true;
			}

		};

		when(urlRenderer.getBaseUrl()).thenReturn(Url.parse("a"));

		when(requestCycle.mapUrlFor(eq(handler))).thenReturn(Url.parse("b"));

		renderer.respond(requestCycle);

		verify(response, never()).write(any(byte[].class));
		verify(response).sendRedirect(anyString());
	}

	/**
	 * Tests that when target URL is different and session is temporary and page is stateless this
	 * is special case when page is stateless but there is no session so we can't render it to
	 * buffer
	 */
	@Test
	public void testDifferentUrlsTemporarySessionAndStatelessPage()
	{
		when(page.isPageStateless()).thenReturn(true);

		PageRenderer renderer = new TestPageRenderer(handler)
		{
			@Override
			protected boolean isRedirectToBuffer()
			{
				return true;
			}

			@Override
			protected boolean isSessionTemporary()
			{
				return true;
			}
		};

		when(urlRenderer.getBaseUrl()).thenReturn(Url.parse("a"));

		when(requestCycle.mapUrlFor(eq(handler))).thenReturn(Url.parse("b"));

		renderer.respond(requestCycle);

		verify(response, never()).write(any(byte[].class));
		verify(response).sendRedirect(anyString());
	}

	/**
	 * Tests that when URLs are different and we have a page class and not an instance we can
	 * redirect to the url which will instantiate the instance of us
	 */
	@Test
	public void testDifferentUrlsAndNewPageInstance()
	{
		when(provider.isNewPageInstance()).thenReturn(true);

		PageRenderer renderer = new TestPageRenderer(handler)
		{
			@Override
			protected boolean isRedirectToBuffer()
			{
				return true;
			}
		};

		when(urlRenderer.getBaseUrl()).thenReturn(Url.parse("a"));

		when(requestCycle.mapUrlFor(eq(handler))).thenReturn(Url.parse("b"));

		renderer.respond(requestCycle);

		verify(response, never()).write(any(byte[].class));
		verify(response).sendRedirect(anyString());
	}

	/**
	 * Tests that when during page render another request handler got scheduled. The handler will
	 * want to overwrite the response, so we need to let it
	 */
	@Test
	public void testRedirectToBufferNoPageToRender()
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

		// needed for earlier checks
		when(urlRenderer.getBaseUrl()).thenReturn(Url.parse("a"));
		when(requestCycle.mapUrlFor(eq(handler))).thenReturn(Url.parse("b"));

		renderer.respond(requestCycle);

		verify(response, never()).write(any(byte[].class));
		verify(response, never()).write(any(CharSequence.class));
		verify(response, never()).sendRedirect(anyString());
		Assert.assertFalse(stored.get());
	}

	/**
	 * Tests that when the page is stateless and redirect for stateless pages is disabled then the
	 * page should be written without redirect
	 */
	@Test
	public void testRedirectToBufferStatelessPageAndRedirectIsDisabled()
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

		when(page.isPageStateless()).thenReturn(true);

		// needed for earlier checks
		when(urlRenderer.getBaseUrl()).thenReturn(Url.parse("a"));
		when(requestCycle.mapUrlFor(eq(handler))).thenReturn(Url.parse("b"));

		renderer.respond(requestCycle);

		verify(response).write(any(byte[].class));
		verify(response, never()).sendRedirect(anyString());
		Assert.assertFalse(stored.get());
	}

	/**
	 * Tests that when the page is stateless and redirect for stateless pages is enabled then there
	 * should be a redirect
	 */
	@Test
	public void testRedirectToBufferStatelessPageAndRedirectIsEsabled()
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
		verify(response).sendRedirect(anyString());
		Assert.assertTrue(stored.get());
	}

	/**
	 * Tests that when the page is stateful and the urls are different then there should be a
	 * redirect
	 */
	@Test
	public void testRedirectToBufferStatefulPage()
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
		verify(response).sendRedirect(anyString());
		Assert.assertTrue(stored.get());
	}

	// or one pass render mode is on
	// or the targetUrl matches current url and the page is not stateless
	// or the targetUrl matches current url, page is stateless but it's redirect-to-render
	// or the request determines that the current url should be preserved
	// just render the page
	@Test
	public void testShouldRenderPageAndWriteResponseCondition() {

		RedirectPolicy redirectPolicy;
		boolean ajax;
		boolean onePassRender;
		boolean redirectToRender;
		boolean shouldPreserveClientUrl;
		boolean targetEqualsCurrentUrl;
		boolean newPageInstance;
		boolean pageStateless;

		// if the policy is never to redirect
		redirectPolicy=RedirectPolicy.NEVER_REDIRECT;
		ajax=true;
		onePassRender=true;
		redirectToRender=true;
		shouldPreserveClientUrl=true;
		targetEqualsCurrentUrl=true;
		newPageInstance=true;
		pageStateless=true;

		Assert.assertTrue(WebPageRenderer.shouldRenderPageAndWriteResponse(ajax, onePassRender, redirectToRender, redirectPolicy, shouldPreserveClientUrl, targetEqualsCurrentUrl, newPageInstance, pageStateless));

		ajax=false;
		onePassRender=false;
		redirectToRender=false;
		shouldPreserveClientUrl=false;
		targetEqualsCurrentUrl=false;
		newPageInstance=false;
		pageStateless=false;

		Assert.assertTrue(WebPageRenderer.shouldRenderPageAndWriteResponse(ajax, onePassRender, redirectToRender, redirectPolicy, shouldPreserveClientUrl, targetEqualsCurrentUrl, newPageInstance, pageStateless));

	}

	/**
	 * Tests that when the page is stateful and the urls are the same then the response is written
	 * directly
	 */
	@Test
	public void testRedirectToBufferStatefulPageAndSameUrls()
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
		Assert.assertFalse(stored.get());
	}

	/**
	 * Tests that when all the conditions fail the redirect_to_buffer should be used as a fallback
	 */
	@Test
	public void testRedirectToBufferIsFallback()
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
		verify(response).sendRedirect(anyString());
		Assert.assertTrue(stored.get());
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
		protected boolean isRedirectToBuffer()
		{
			return false;
		}

		@Override
		protected boolean isSessionTemporary()
		{
			return false;
		}
	}
}
