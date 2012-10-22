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
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.Page;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.protocol.https.HttpsMapper.RedirectHandler;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.IRequestMapper;
import org.apache.wicket.request.Url;
import org.junit.Test;

public class HttpsMapperTest
{
	@Test
	public void getDesiredSchemeOfPageClass()
	{
		IRequestMapper delegate = mock(IRequestMapper.class);
		HttpsMapper mapper = new HttpsMapper(delegate, new HttpsConfig());

		assertThat(mapper.getDesiredSchemeFor(SecurePage.class), is(Scheme.HTTPS));
		assertThat(mapper.getDesiredSchemeFor(SecureDecendantPage.class), is(Scheme.HTTPS));
		assertThat(mapper.getDesiredSchemeFor(SecureMixinPage.class), is(Scheme.HTTPS));
		assertThat(mapper.getDesiredSchemeFor(InsecurePage.class), is(Scheme.HTTP));
	}

	@Test
	public void getDesiredSchemeOfHandler()
	{
		IRequestMapper delegate = mock(IRequestMapper.class);
		HttpsMapper mapper = new HttpsMapper(delegate, new HttpsConfig());

		IRequestHandler handler = new RenderPageRequestHandler(new PageProvider(SecurePage.class));
		assertThat(mapper.getDesiredSchemeFor(handler), is(Scheme.HTTPS));

		handler = new RenderPageRequestHandler(new PageProvider(InsecurePage.class));
		assertThat(mapper.getDesiredSchemeFor(handler), is(Scheme.HTTP));

		handler = mock(IRequestHandler.class);
		assertThat(mapper.getDesiredSchemeFor(handler), is(Scheme.ANY));
	}

	@Test
	public void getSchemeOfRequest()
	{
		IRequestMapper delegate = mock(IRequestMapper.class);
		HttpsMapper mapper = new HttpsMapper(delegate, new HttpsConfig());

		ServletWebRequest request = mock(ServletWebRequest.class);
		HttpServletRequest req = mock(HttpServletRequest.class);
		when(request.getContainerRequest()).thenReturn(req);

		when(req.getScheme()).thenReturn("https");
		assertThat(mapper.getSchemeOf(request), is(Scheme.HTTPS));

		reset(req);
		when(req.getScheme()).thenReturn("hTTps");
		assertThat(mapper.getSchemeOf(request), is(Scheme.HTTPS));

		reset(req);
		when(req.getScheme()).thenReturn("http");
		assertThat(mapper.getSchemeOf(request), is(Scheme.HTTP));

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
	public void mapHandler()
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
		assertThat(url.getProtocol(), is("https"));
		assertThat(url.getPort(), is(443));

		// render url to http page on http, ignore protocol
		handler = new RenderPageRequestHandler(new PageProvider(InsecurePage.class));
		url = new Url();
		reset(delegate);
		when(delegate.mapHandler(handler)).thenReturn(url);
		when(req.getScheme()).thenReturn("http");
		mapper.mapHandler(handler, request);
		assertThat(url.getProtocol(), is(nullValue()));

		// render url to http page on https, set protocol
		handler = new RenderPageRequestHandler(new PageProvider(InsecurePage.class));
		url = new Url();
		reset(delegate);
		when(delegate.mapHandler(handler)).thenReturn(url);
		when(req.getScheme()).thenReturn("https");
		mapper.mapHandler(handler, request);
		assertThat(url.getProtocol(), is("http"));
		assertThat(url.getPort(), is(80));
	}


	@Test
	public void mapRequest()
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
		assertThat(resolved, is(instanceOf(RedirectHandler.class)));
		assertThat(((RedirectHandler)resolved).getUrl(), is("https://localhost/ctx?foo=bar"));

		// http handler on http request, return the original handler
		handler = new RenderPageRequestHandler(new PageProvider(InsecurePage.class));
		reset(delegate);
		when(delegate.mapRequest(request)).thenReturn(handler);
		setupRequest(req, "http", "localhost", 80, "/ctx", "foo=bar");
		resolved = mapper.mapRequest(request);
		assertThat(resolved, is(sameInstance(handler)));
	}

	@Test
	public void mapRequestWithCustomPorts()
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
		assertThat(resolved, is(instanceOf(RedirectHandler.class)));
		assertThat(((RedirectHandler)resolved).getUrl(), is("https://localhost:20/ctx?foo=bar"));

		// http handler on http request, return the original handler
		handler = new RenderPageRequestHandler(new PageProvider(InsecurePage.class));
		reset(delegate);
		when(delegate.mapRequest(request)).thenReturn(handler);
		setupRequest(req, "https", "localhost", 20, "/ctx", "foo=bar");
		resolved = mapper.mapRequest(request);
		assertThat(resolved, is(instanceOf(RedirectHandler.class)));
		assertThat(((RedirectHandler)resolved).getUrl(), is("http://localhost:10/ctx?foo=bar"));
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