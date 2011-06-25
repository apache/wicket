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
package org.apache.wicket.request;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for {@link org.apache.wicket.request.Url}
 */
public class UrlTest extends Assert
{
	@Test
	public void parseRelativeUrl()
	{
		Url url = Url.parse("foo");
		checkUrl(url, null, null, null, "foo");
		assertFalse(url.isAbsolute());

		url = Url.parse("foo/bar/baz");
		checkUrl(url, null, null, null, "foo", "bar", "baz");
		assertFalse(url.isAbsolute());

		url = Url.parse("?foobar");
		checkUrl(url, null, null, null);
		assertEquals("", url.getQueryParameter("foobar").getValue());
		assertFalse(url.isAbsolute());

		url = Url.parse("foo?a=123");
		checkUrl(url, null, null, null, "foo");
		assertEquals("123", url.getQueryParameter("a").getValue());
		assertFalse(url.isAbsolute());
	
		url = Url.parse("/foo");
		checkUrl(url, null, null, null, "", "foo");
		assertTrue(url.isAbsolute());
	}

	@Test
	public void parseAbsoluteUrl()
	{
		Url url = Url.parse("ftp://myhost:8081");
		checkUrl(url, "ftp", "myhost", 8081);
		assertFalse(url.isAbsolute());
	
		url = Url.parse("gopher://myhost:8081/foo");
		checkUrl(url, "gopher", "myhost", 8081, "", "foo");
		assertTrue(url.isAbsolute());

		url = Url.parse("https://myhost/foo");
		checkUrl(url, "https", "myhost", 443, "", "foo");
		assertTrue(url.isAbsolute());

		url = Url.parse("https://myhost/foo:123");
		checkUrl(url, "https", "myhost", 443, "", "foo:123");
		assertTrue(url.isAbsolute());

		url = Url.parse("ftp://myhost/foo");
		checkUrl(url, "ftp", "myhost", 21, "", "foo");
		assertTrue(url.isAbsolute());

		url = Url.parse("FTp://myhost/foo");
		checkUrl(url, "ftp", "myhost", 21, "", "foo");
		assertTrue(url.isAbsolute());

		url = Url.parse("unknown://myhost/foo");
		checkUrl(url, "unknown", "myhost", null, "", "foo");
		assertTrue(url.isAbsolute());

	}

	private void checkUrl(Url url, String protocol, String host, Integer port, String... segments)
	{
		assertNotNull(url);
		assertEquals(protocol, url.getProtocol());
		assertEquals(host, url.getHost());
		assertEquals(port, url.getPort());
		assertEquals(Arrays.asList(segments), url.getSegments());
	}
}
