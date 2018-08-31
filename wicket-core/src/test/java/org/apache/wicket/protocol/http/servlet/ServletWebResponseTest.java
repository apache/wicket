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
package org.apache.wicket.protocol.http.servlet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.ThreadContext;
import org.apache.wicket.protocol.http.mock.MockHttpServletRequest;
import org.apache.wicket.protocol.http.mock.MockHttpServletResponse;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.UrlRenderer;
import org.apache.wicket.request.cycle.RequestCycle;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

/**
 * Tests for {@link ServletWebResponse}
 */
class ServletWebResponseTest
{

	/**
	 * https://issues.apache.org/jira/browse/WICKET-3885
	 * 
	 * Redirects in Ajax requests should produce &lt;ajax-response&gt; with relative url
	 * 
	 * @throws IOException
	 */
	@Test
	void sendRedirectAjax() throws IOException
	{
		final String url = "./relative/path";

		ServletWebRequest webRequest = mock(ServletWebRequest.class);
		when(webRequest.isAjax()).thenReturn(Boolean.TRUE);
		Url baseUrl = Url.parse("./baseUrl");
		baseUrl.setProtocol("http");
		baseUrl.setHost("someHost");
		baseUrl.setPort(80);
		when(webRequest.getClientUrl()).thenReturn(baseUrl);

		HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
		when(webRequest.getContainerRequest()).thenReturn(httpServletRequest);
		when(httpServletRequest.getCharacterEncoding()).thenReturn("UTF-8");

		HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
		when(httpServletResponse.encodeRedirectURL(ArgumentMatchers.anyString())).thenReturn(url);
		StringWriter writer = new StringWriter();
		when(httpServletResponse.getWriter()).thenReturn(new PrintWriter(writer));

		ServletWebResponse webResponse = new ServletWebResponse(webRequest, httpServletResponse);
		webResponse.sendRedirect(url);

		verify(httpServletResponse).setHeader("Ajax-Location", url);
		verify(httpServletResponse).setContentType("text/xml;charset=UTF-8");
		assertEquals(
			"<ajax-response><redirect><![CDATA[./relative/path]]></redirect></ajax-response>",
			writer.toString());
		assertTrue(webResponse.isRedirect());

	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-3885
	 * 
	 * Redirects in normal (non-Ajax) requests should call HttpServletResponse's sendRedirect()
	 * which cares to make the url absolute
	 * 
	 * https://issues.apache.org/jira/browse/WICKET-4260
	 * 
	 * Redirect to relative url should be stripped of leading dot
	 * 
	 * @throws IOException
	 */
	@Test
	void sendRedirect() throws IOException
	{
		final String url = "./relative/path";

		ServletWebRequest webRequest = mock(ServletWebRequest.class);
		when(webRequest.isAjax()).thenReturn(Boolean.FALSE);
		Url baseUrl = Url.parse("./baseUrl");
		baseUrl.setProtocol("http");
		baseUrl.setHost("someHost");
		baseUrl.setPort(80);
		when(webRequest.getClientUrl()).thenReturn(baseUrl);

		HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
		when(httpServletResponse.encodeRedirectURL(ArgumentMatchers.anyString())).thenReturn(url);

		ServletWebResponse webResponse = new ServletWebResponse(webRequest, httpServletResponse);
		webResponse.sendRedirect(url);

		verify(httpServletResponse).sendRedirect("./relative/path");
		assertTrue(webResponse.isRedirect());
	}

	/**
	 * WICKET-4934 DownloadLink uses wrong encoding for spaces/non-ASCII characters
	 */
	@Test
	void setDispositionHeader()
	{
		ServletWebRequest webRequest = mock(ServletWebRequest.class);
		MockHttpServletRequest httpRequest = mock(MockHttpServletRequest.class);
		HttpServletResponse httpResponse = new MockHttpServletResponse(httpRequest);
		ServletWebResponse response = new ServletWebResponse(webRequest, httpResponse);

		response.setInlineHeader("name with spaces and;,");
		String header = httpResponse.getHeader("Content-Disposition");
		assertEquals(
			"inline; filename=\"name%20with%20spaces%20and%3B%2C\"; filename*=UTF-8''name%20with%20spaces%20and%3B%2C",
			header);

		// says: "name with bulgarian"
		response.setInlineHeader("name with български");
		header = httpResponse.getHeader("Content-Disposition");
		assertEquals(
			"inline; filename=\"name%20with%20%D0%B1%D1%8A%D0%BB%D0%B3%D0%B0%D1%80%D1%81%D0%BA%D0%B8\"; filename*=UTF-8''name%20with%20%D0%B1%D1%8A%D0%BB%D0%B3%D0%B0%D1%80%D1%81%D0%BA%D0%B8",
			header);

		response.setAttachmentHeader("name with spaces");
		header = httpResponse.getHeader("Content-Disposition");
		assertEquals(
			"attachment; filename=\"name%20with%20spaces\"; filename*=UTF-8''name%20with%20spaces",
			header);

		// says: "name with bulgarian"
		response.setAttachmentHeader("name with български");
		header = httpResponse.getHeader("Content-Disposition");
		assertEquals(
			"attachment; filename=\"name%20with%20%D0%B1%D1%8A%D0%BB%D0%B3%D0%B0%D1%80%D1%81%D0%BA%D0%B8\"; filename*=UTF-8''name%20with%20%D0%B1%D1%8A%D0%BB%D0%B3%D0%B0%D1%80%D1%81%D0%BA%D0%B8",
			header);
	}

	/**
	 * WICKET-5582 absolute URLs stay absolute after encoding
	 */
	@Test
	void encodeAbsoluteUrl()
	{
		final String url = "http://localhost:8080/path";

		ServletWebRequest webRequest = mock(ServletWebRequest.class);
		when(webRequest.isAjax()).thenReturn(Boolean.FALSE);
		Url baseUrl = Url.parse("./baseUrl");
		baseUrl.setProtocol("http");
		baseUrl.setHost("someHost");
		baseUrl.setPort(80);
		when(webRequest.getClientUrl()).thenReturn(baseUrl);

		UrlRenderer renderer = new UrlRenderer(webRequest);

		RequestCycle requestCycle = mock(RequestCycle.class);
		ThreadContext.setRequestCycle(requestCycle);
		when(requestCycle.getUrlRenderer()).thenReturn(renderer);

		HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
		when(httpServletResponse.encodeURL(ArgumentMatchers.eq(url))).thenReturn(url + ";foo");

		ServletWebResponse webResponse = new ServletWebResponse(webRequest, httpServletResponse);
		assertEquals(url + ";foo", webResponse.encodeURL(url));
	}

	/**
	 * WICKET-5582 absolute URLs stay absolute after encoding
	 */
	@Test
	void encodeRedirectAbsoluteUrl()
	{
		final String url = "http://localhost:8080/path";

		ServletWebRequest webRequest = mock(ServletWebRequest.class);
		when(webRequest.isAjax()).thenReturn(Boolean.FALSE);
		Url baseUrl = Url.parse("./baseUrl");
		baseUrl.setProtocol("http");
		baseUrl.setHost("someHost");
		baseUrl.setPort(80);
		when(webRequest.getClientUrl()).thenReturn(baseUrl);

		UrlRenderer renderer = new UrlRenderer(webRequest);

		RequestCycle requestCycle = mock(RequestCycle.class);
		ThreadContext.setRequestCycle(requestCycle);
		when(requestCycle.getUrlRenderer()).thenReturn(renderer);

		HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
		when(httpServletResponse.encodeRedirectURL(ArgumentMatchers.eq(url))).thenReturn(url + ";foo");

		ServletWebResponse webResponse = new ServletWebResponse(webRequest, httpServletResponse);
		assertEquals(url + ";foo", webResponse.encodeRedirectURL(url));
	}
}
