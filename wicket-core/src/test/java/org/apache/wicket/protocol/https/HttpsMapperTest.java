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
package org.apache.wicket.protocol.https;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.wicket.Page;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.protocol.https.HttpsMapper.RedirectHandler;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.IRequestMapper;
import org.apache.wicket.request.Url;
import org.junit.jupiter.api.Test;

class HttpsMapperTest
{
	@Test
	void getDesiredSchemeOfPageClass()
	{
		IRequestMapper delegate = mock(IRequestMapper.class);
		HttpsMapper mapper = new HttpsMapper(delegate, new HttpsConfig());

		assertEquals(Scheme.HTTPS, mapper.getDesiredSchemeFor(SecurePage.class));
		assertEquals(Scheme.HTTPS, mapper.getDesiredSchemeFor(SecureDecendantPage.class));
		assertEquals(Scheme.HTTPS, mapper.getDesiredSchemeFor(SecureMixinPage.class));
		assertEquals(Scheme.HTTP, mapper.getDesiredSchemeFor(InsecurePage.class));
	}

	@Test
	void getDesiredSchemeOfHandler()
	{
		IRequestMapper delegate = mock(IRequestMapper.class);
		HttpsMapper mapper = new HttpsMapper(delegate, new HttpsConfig());

		IRequestHandler handler = new RenderPageRequestHandler(new PageProvider(SecurePage.class));
		assertEquals(Scheme.HTTPS, mapper.getDesiredSchemeFor(handler));

		handler = new RenderPageRequestHandler(new PageProvider(InsecurePage.class));
		assertEquals(Scheme.HTTP, mapper.getDesiredSchemeFor(handler));

		handler = mock(IRequestHandler.class);
		assertEquals(Scheme.ANY, mapper.getDesiredSchemeFor(handler));
	}

	@Test
	void getSchemeOfRequest()
	{
		IRequestMapper delegate = mock(IRequestMapper.class);
		HttpsMapper mapper = new HttpsMapper(delegate, new HttpsConfig());

		ServletWebRequest request = mock(ServletWebRequest.class);
		HttpServletRequest req = mock(HttpServletRequest.class);
		when(request.getContainerRequest()).thenReturn(req);

		when(req.getScheme()).thenReturn("https");
		assertEquals(Scheme.HTTPS, mapper.getSchemeOf(request));

		reset(req);
		when(req.getScheme()).thenReturn("hTTps");
		assertEquals(Scheme.HTTPS, mapper.getSchemeOf(request));

		reset(req);
		when(req.getScheme()).thenReturn("http");
		assertEquals(Scheme.HTTP, mapper.getSchemeOf(request));

		try
		{
			reset(req);
			when(req.getScheme()).thenReturn("ftp");
			mapper.getSchemeOf(request);
			assertThat("expected error", false);
		}
		catch (IllegalStateException e)
		{ // expected

		}
	}

	@Test
	void mapHandler()
	{
		IRequestMapper delegate = mock(IRequestMapper.class);
		HttpsMapper mapper = new HttpsMapper(delegate, new HttpsConfig());

		ServletWebRequest request = mock(ServletWebRequest.class);
		HttpServletRequest req = mock(HttpServletRequest.class);
		when(request.getContainerRequest()).thenReturn(req);

		// rendering url to https page on http, set protocol
		IRequestHandler handler = new RenderPageRequestHandler(new PageProvider(SecurePage.class));
		Url url = new Url();
		when(delegate.mapHandler(handler)).thenReturn(url);
		when(req.getScheme()).thenReturn("http");
		mapper.mapHandler(handler, request);
		assertEquals("https", url.getProtocol());
		assertEquals(Integer.valueOf(443), url.getPort());

		// render url to http page on http, ignore protocol
		handler = new RenderPageRequestHandler(new PageProvider(InsecurePage.class));
		url = new Url();
		reset(delegate);
		when(delegate.mapHandler(handler)).thenReturn(url);
		when(req.getScheme()).thenReturn("http");
		mapper.mapHandler(handler, request);
		assertNull(url.getProtocol());

		// render url to http page on https, set protocol
		handler = new RenderPageRequestHandler(new PageProvider(InsecurePage.class));
		url = new Url();
		reset(delegate);
		when(delegate.mapHandler(handler)).thenReturn(url);
		when(req.getScheme()).thenReturn("https");
		mapper.mapHandler(handler, request);
		assertEquals("http", url.getProtocol());
		assertEquals(Integer.valueOf(80), url.getPort());
	}


	@Test
	void mapRequest()
	{
		IRequestMapper delegate = mock(IRequestMapper.class);
		HttpsMapper mapper = new HttpsMapper(delegate, new HttpsConfig());

		ServletWebRequest request = mock(ServletWebRequest.class);
		HttpServletRequest req = mock(HttpServletRequest.class);
		when(request.getContainerRequest()).thenReturn(req);

		// https handler on http request, redirect to https
		setupRequest(req, "http", "localhost", 80, "/ctx", "foo=bar");
		IRequestHandler handler = new RenderPageRequestHandler(new PageProvider(SecurePage.class));
		when(delegate.mapRequest(request)).thenReturn(handler);
		IRequestHandler resolved = mapper.mapRequest(request);
		assertThat(resolved, instanceOf(RedirectHandler.class));
		assertEquals("https://localhost/ctx?foo=bar", ((RedirectHandler)resolved).getUrl());

		// http handler on http request, return the original handler
		handler = new RenderPageRequestHandler(new PageProvider(InsecurePage.class));
		reset(delegate);
		when(delegate.mapRequest(request)).thenReturn(handler);
		setupRequest(req, "http", "localhost", 80, "/ctx", "foo=bar");
		resolved = mapper.mapRequest(request);
		assertSame(handler, resolved);
	}

	@Test
	void mapRequestWithCustomPorts()
	{
		IRequestMapper delegate = mock(IRequestMapper.class);
		HttpsMapper mapper = new HttpsMapper(delegate, new HttpsConfig(10, 20));

		ServletWebRequest request = mock(ServletWebRequest.class);
		HttpServletRequest req = mock(HttpServletRequest.class);
		when(request.getContainerRequest()).thenReturn(req);

		// https handler on http request, redirect to https
		setupRequest(req, "http", "localhost", 10, "/ctx", "foo=bar");
		IRequestHandler handler = new RenderPageRequestHandler(new PageProvider(SecurePage.class));
		when(delegate.mapRequest(request)).thenReturn(handler);
		IRequestHandler resolved = mapper.mapRequest(request);
		assertThat(resolved, instanceOf(RedirectHandler.class));
		assertEquals("https://localhost:20/ctx?foo=bar", ((RedirectHandler)resolved).getUrl());

		// http handler on http request, return the original handler
		handler = new RenderPageRequestHandler(new PageProvider(InsecurePage.class));
		reset(delegate);
		when(delegate.mapRequest(request)).thenReturn(handler);
		setupRequest(req, "https", "localhost", 20, "/ctx", "foo=bar");
		resolved = mapper.mapRequest(request);
		assertThat(resolved, instanceOf(RedirectHandler.class));
		assertEquals("http://localhost:10/ctx?foo=bar", ((RedirectHandler)resolved).getUrl());
	}

	private static void setupRequest(HttpServletRequest mock, String scheme, String host, int port,
		String uri, String query)
	{
		reset(mock);
		when(mock.getScheme()).thenReturn(scheme);
		when(mock.getServerName()).thenReturn(host);
		when(mock.getServerPort()).thenReturn(port);
		when(mock.getRequestURI()).thenReturn(uri);
		when(mock.getQueryString()).thenReturn(query);
	}


	@RequireHttps
	private static class SecurePage extends Page
	{

	}

	private static class InsecurePage extends Page
	{

	}

	@RequireHttps
	private static interface Secured
	{

	}

	private static class SecureDecendantPage extends SecurePage
	{

	}

	private static class SecureMixinPage extends Page implements Secured
	{

	}
}
