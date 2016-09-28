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
package org.apache.wicket.protocol.http;

import java.util.List;

import javax.servlet.http.Cookie;

import org.apache.wicket.protocol.http.mock.MockHttpServletResponse;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 */
public class ModifyCookiePageTest extends WicketTestCase
{
	/**
	 * testSetCookieWithinLinkListener()
	 */
	@Test
	public void testSetCookieWithinLinkListener()
	{
		// render page
		tester.startPage(ModifyCookiePage.class);
		tester.assertRenderedPage(ModifyCookiePage.class);

		// click link that creates a cookie with in the link listener
		tester.clickLink(ModifyCookiePage.CREATE_COOKIE_ID);

		// check page is rendered
		tester.assertRenderedPage(ModifyCookiePage.class);

		// get response
		MockHttpServletResponse response = tester.getLastResponse();
		assertNotNull(response);

		// check that one cookie was set
		List<Cookie> cookies = response.getCookies();
		assertEquals(1, cookies.size());

		// check that cookie contains proper values
		Cookie cookie = cookies.get(0);
		assertEquals(ModifyCookiePage.COOKIE_NAME, cookie.getName());
		assertEquals(ModifyCookiePage.COOKIE_VALUE, cookie.getValue());
	}
}