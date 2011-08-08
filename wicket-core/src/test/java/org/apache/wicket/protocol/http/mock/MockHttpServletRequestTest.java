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

import org.apache.wicket.WicketTestCase;
import org.apache.wicket.request.Url;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Test;

/**
 * test features of {@link MockHttpServletRequest}
 */
public class MockHttpServletRequestTest extends WicketTestCase
{
	@Test
	public void setAbsoluteUrlWithHost()
	{
		WicketTester tester = new WicketTester();
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
		assertEquals(new Integer(1234), url.getPort());
		assertEquals("/foo/bar/baz.html", url.getPath());
		assertEquals("?a=123&b=456", url.getQueryString());

		String pathInfo = request.getPathInfo();
		assertEquals("/foo/bar/baz.html", pathInfo);
	}

	@Test
	public void setAbsoluteUrlWithoutHost()
	{
		WicketTester tester = new WicketTester();
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
		assertEquals(new Integer(80), url.getPort());
		assertEquals("/foo/bar/baz.html", url.getPath());
		assertEquals("?a=123&b=456", url.getQueryString());

		String pathInfo = request.getPathInfo();
		assertEquals("/foo/bar/baz.html", pathInfo);
	}

	@Test
	public void setRelativeUrl()
	{
		WicketTester tester = new WicketTester();
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
		assertEquals(new Integer(80), url.getPort());
		assertEquals(request.getContextPath() + request.getServletPath() + "/foo/bar/baz.html", url.getPath());
		assertEquals("?a=123&b=456", url.getQueryString());

		String pathInfo = request.getPathInfo();
		assertEquals("/foo/bar/baz.html", pathInfo);
	}
}
