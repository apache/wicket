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
package org.apache.wicket.protocol.http.mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.Enumeration;
import java.util.Locale;
import jakarta.servlet.http.HttpSession;

import org.apache.wicket.Session;
import org.apache.wicket.request.Url;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * test features of {@link MockHttpServletRequest}
 */
class MockHttpServletRequestTest extends WicketTestCase
{
	@Test
	void setAbsoluteUrlWithHost()
	{
		MockHttpServletRequest request = tester.getRequest();
		assertEquals("http", request.getScheme());
		assertEquals("localhost", request.getServerName());
		assertEquals(80, request.getServerPort());

		request.setURL("https://myhost.mydomain.org:1234/foo/bar/baz.html?a=123&b=456");
		assertEquals("https", request.getScheme());
		assertEquals("myhost.mydomain.org", request.getServerName());
		assertEquals(1234, request.getServerPort());

		Url url = request.getUrl();
		assertEquals("https", url.getProtocol());
		assertEquals("myhost.mydomain.org", url.getHost());
		assertEquals(Integer.valueOf(1234), url.getPort());
		assertEquals("/foo/bar/baz.html", url.getPath());
		assertEquals("a=123&b=456", url.getQueryString());

		String pathInfo = request.getPathInfo();
		assertEquals("/foo/bar/baz.html", pathInfo);
	}

	@Test
	void setAbsoluteUrlWithoutHost()
	{
		MockHttpServletRequest request = tester.getRequest();
		assertEquals("http", request.getScheme());
		assertEquals("localhost", request.getServerName());
		assertEquals(80, request.getServerPort());

		request.setURL("/foo/bar/baz.html?a=123&b=456");
		assertEquals("http", request.getScheme());
		assertEquals("localhost", request.getServerName());
		assertEquals(80, request.getServerPort());

		Url url = request.getUrl();
		assertEquals("http", url.getProtocol());
		assertEquals("localhost", url.getHost());
		assertEquals(Integer.valueOf(80), url.getPort());
		assertEquals("/foo/bar/baz.html", url.getPath());
		assertEquals("a=123&b=456", url.getQueryString());

		String pathInfo = request.getPathInfo();
		assertEquals("/foo/bar/baz.html", pathInfo);
	}

	@Test
	void setRelativeUrl()
	{
		MockHttpServletRequest request = tester.getRequest();
		assertEquals("http", request.getScheme());
		assertEquals("localhost", request.getServerName());
		assertEquals(80, request.getServerPort());

		request.setURL("foo/bar/baz.html?a=123&b=456");
		assertEquals("http", request.getScheme());
		assertEquals("localhost", request.getServerName());
		assertEquals(80, request.getServerPort());

		Url url = request.getUrl();
		assertEquals("http", url.getProtocol());
		assertEquals("localhost", url.getHost());
		assertEquals(Integer.valueOf(80), url.getPort());
		assertEquals(request.getContextPath() + request.getServletPath() + "/foo/bar/baz.html", url.getPath());
		assertEquals("a=123&b=456", url.getQueryString());

		String pathInfo = request.getPathInfo();
		assertEquals("/foo/bar/baz.html", pathInfo);
	}

    /**
     * WICKET-4664 - no query string returns null as per HttpServletRequest
     */
    @Test
	void testNoQueryString_returnsNull()
    {
        MockHttpServletRequest request = tester.getRequest();
        request.setURL("my/servlet/without/query/param");
        
        Url url = request.getUrl();
        assertNull(url.getQueryString());
    }
	
	@Test
	void getSessionFromNonMockHttpSession()
	{
		HttpSession httpSession = Mockito.mock(HttpSession.class);
		MockHttpServletRequest request = new MockHttpServletRequest(null, httpSession, null);
		assertNull(request.getSession(true), "MockHttpServletRequest knows how to work only with MockHttpSession");
		assertNull(request.getSession(false), "MockHttpServletRequest knows how to work only with MockHttpSession");
	}

	@Test
	void getSessionFalseFromMockHttpSession()
	{
		HttpSession httpSession = new MockHttpSession(null);
		MockHttpServletRequest request = new MockHttpServletRequest(null, httpSession, null);
		assertNull(request.getSession(false), "HttpSession should not be created!");
	}

	@Test
	void getSessionDefaultFromMockHttpSession()
	{
		HttpSession httpSession = new MockHttpSession(null);
		MockHttpServletRequest request = new MockHttpServletRequest(null, httpSession, null);
		assertSame(httpSession, request.getSession(), "HttpSession should be created!");
	}

	@Test
	void getSessionTrueFromMockHttpSession()
	{
		HttpSession httpSession = new MockHttpSession(null);
		MockHttpServletRequest request = new MockHttpServletRequest(null, httpSession, null);
		assertSame(httpSession, request.getSession(true), "HttpSession should be created!");
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-4481
	 */
	@Test
	void setHeader()
	{
		HttpSession httpSession = new MockHttpSession(null);
		MockHttpServletRequest request = new MockHttpServletRequest(null, httpSession, null);
		String headerName = "headerName";
		request.setHeader(headerName, "headerValue");
		Enumeration<String> headers = request.getHeaders(headerName);
		assertEquals("headerValue", headers.nextElement());
		assertFalse(headers.hasMoreElements());
		request.addHeader(headerName, "headerValue2");
		headers = request.getHeaders(headerName);
		assertEquals("headerValue", headers.nextElement());
		assertEquals("headerValue2", headers.nextElement());
		assertFalse(headers.hasMoreElements());
		request.setHeader(headerName, "completelyNewValue");
		headers = request.getHeaders(headerName);
		assertEquals("completelyNewValue", headers.nextElement());
		assertFalse(headers.hasMoreElements());
	}

	@Test
	void setLocale() {
		Session session = tester.getSession();
		session.setLocale(Locale.US);
		tester.getRequest().setLocale(Locale.CANADA_FRENCH);

		session.invalidateNow();

		assertEquals(Locale.CANADA_FRENCH, tester.getSession().getLocale());
	}
}
