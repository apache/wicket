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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.protocol.http.mock.MockHttpServletResponse;
import org.apache.wicket.util.time.Time;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Matchers;

/**
 * Tests for {@link ServletWebResponse}
 */
public class ServletWebResponseTest extends Assert
{

	/**
	 * https://issues.apache.org/jira/browse/WICKET-3885
	 * 
	 * Redirects in Ajax requests should produce &lt;ajax-response&gt; with relative url
	 * 
	 * @throws IOException
	 */
	@Test
	public void sendRedirectAjax() throws IOException
	{
		final String url = "relative/path";

		ServletWebRequest webRequest = mock(ServletWebRequest.class);
		when(webRequest.isAjax()).thenReturn(Boolean.TRUE);

		HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
		when(webRequest.getContainerRequest()).thenReturn(httpServletRequest);
		when(httpServletRequest.getCharacterEncoding()).thenReturn("UTF-8");

		HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
		when(httpServletResponse.encodeRedirectURL(Matchers.eq(url))).thenReturn(url);
		StringWriter writer = new StringWriter();
		when(httpServletResponse.getWriter()).thenReturn(new PrintWriter(writer));

		ServletWebResponse webResponse = new ServletWebResponse(webRequest, httpServletResponse);
		webResponse.sendRedirect(url);

		verify(httpServletResponse).addHeader("Ajax-Location", url);
		verify(httpServletResponse).setContentType("text/xml;charset=UTF-8");
		assertEquals(
			"<ajax-response><redirect><![CDATA[relative/path]]></redirect></ajax-response>",
			writer.toString());
		assertTrue(webResponse.isRedirect());

	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-3885
	 * 
	 * Redirects in normal (non-Ajax) requests should call HttpServletResponse's sendRedirect()
	 * which cares to make the url absolute
	 * 
	 * @throws IOException
	 */
	@Test
	public void sendRedirect() throws IOException
	{
		final String url = "relative/path";

		ServletWebRequest webRequest = mock(ServletWebRequest.class);
		when(webRequest.isAjax()).thenReturn(Boolean.FALSE);

		HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
		when(httpServletResponse.encodeRedirectURL(Matchers.eq(url))).thenReturn(url);

		ServletWebResponse webResponse = new ServletWebResponse(webRequest, httpServletResponse);
		webResponse.sendRedirect(url);

		verify(httpServletResponse).sendRedirect(url);
		assertTrue(webResponse.isRedirect());

	}

	/**
	 * Verifies that response headers' name and/or values doesn't contain malicious characters
	 * 
	 * https://issues.apache.org/jira/browse/WICKET-4196
	 * 
	 * @throws IOException
	 */
	@Test
	public void sanitizeHeaders() throws IOException
	{
		final String badInput = "something\n\rbad\n\r\n";
		final String badUrl = "bad\n\rurl\r\n";

		ServletWebRequest webRequest = mock(ServletWebRequest.class);
		when(webRequest.isAjax()).thenReturn(Boolean.FALSE);

		MockHttpServletResponse httpServletResponse = new MockHttpServletResponse(null);

		ServletWebResponse webResponse = new ServletWebResponse(webRequest, httpServletResponse);

		webResponse.addHeader(badInput, "someValue");
		assertNull(httpServletResponse.getHeader(badInput));
		assertEquals(httpServletResponse.getHeader(webResponse.sanitize(badInput)), "someValue");

		webResponse.addHeader("someName", badInput);
		assertEquals(httpServletResponse.getHeader("someName"), "something  bad   ");

		webResponse.setHeader(badInput, badInput);
		assertNull(httpServletResponse.getHeader(badInput));
		assertEquals(httpServletResponse.getHeader(webResponse.sanitize(badInput)),
			"something  bad   ");

		Time now = Time.now();
		webResponse.setDateHeader(badInput, now);
		assertNull(httpServletResponse.getHeader(badInput));
		String dateHeaderValue = httpServletResponse.getHeader(webResponse.sanitize(badInput));
		assertNotNull(dateHeaderValue);
		assertEquals(-1, dateHeaderValue.indexOf('\n'));
		assertEquals(-1, dateHeaderValue.indexOf('\r'));

		webResponse.sendRedirect(badUrl);
		assertEquals(httpServletResponse.getRedirectLocation(), "bad  url  ");
	}
}
