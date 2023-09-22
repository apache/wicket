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
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import jakarta.servlet.http.Cookie;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link MockHttpServletResponse}
 */
class MockHttpServletResponseTest
{
	private MockHttpServletResponse response;

	/**
	 * Prepare
	 */
	@BeforeEach
	void before()
	{
		response = new MockHttpServletResponse(null);
	}

	/**
	 * Clean up
	 */
	@AfterEach
	void after()
	{
		response = null;
	}

	/**
	 * Add a cookie
	 */
	@Test
	void addCookie()
	{
		Cookie cookie = new Cookie("name", "value");
		response.addCookie(cookie);

		List<Cookie> cookies = response.getCookies();
		assertEquals(1, cookies.size());
		assertEquals("name", cookies.get(0).getName());
		assertEquals("value", cookies.get(0).getValue());
		assertNull(cookies.get(0).getComment());
		assertNull(cookies.get(0).getDomain());
		assertEquals(-1, cookies.get(0).getMaxAge());
		assertNull(cookies.get(0).getPath());
		assertEquals(false, cookies.get(0).getSecure());
		assertEquals(0, cookies.get(0).getVersion());
	}

	/**
	 * Add a duplicate cookie. <br/>
	 * https://issues.apache.org/jira/browse/WICKET-4292
	 */
	@Test
	void addDuplicateCookie()
	{
		Cookie cookie1 = new Cookie("name", "value");
		response.addCookie(cookie1);
		assertEquals(1, response.getCookies().size());

		Cookie cookie2 = new Cookie("name", "value");
		response.addCookie(cookie2);
		assertEquals(1, response.getCookies().size());

		Cookie cookie3 = new Cookie("name", "value");
		cookie3.setPath("/");
		response.addCookie(cookie3);
		assertEquals(2, response.getCookies().size());

		Cookie cookie4 = new Cookie("name", "value");
		cookie4.setPath("/");
		response.addCookie(cookie4);
		assertEquals(2, response.getCookies().size());

		Cookie cookie5 = new Cookie("name", "value");
		cookie5.setPath("/");
		cookie5.setDomain("example.com");
		response.addCookie(cookie5);
		assertEquals(3, response.getCookies().size());

		Cookie cookie6 = new Cookie("name", "value");
		cookie6.setPath("/");
		cookie6.setDomain("example.com");
		response.addCookie(cookie6);
		assertEquals(3, response.getCookies().size());
	}
}
