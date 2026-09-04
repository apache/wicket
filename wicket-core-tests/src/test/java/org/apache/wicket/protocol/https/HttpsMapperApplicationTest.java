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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.wicket.Page;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.Test;

/**
 * Test for proper switching of http and https in {@link WicketTester}
 */
class HttpsMapperApplicationTest
{
	@SuppressWarnings({ "unchecked" })
	private <T extends Page> T requestPage(WicketTester tester, Class<T> pageClass)
	{
		Page page = tester.startPage(pageClass);
		tester.assertRenderedPage(pageClass);
		assertNotNull(page);
		assertEquals(pageClass, page.getClass());
		assertEquals(page, tester.getLastRenderedPage());
		return (T)page;
	}

	@Test
	void switchDefaultToHttpsWithDefaultPorts()
	{
		WicketTester tester = new WicketTester(new HttpsMapperApplication());

		requestPage(tester, HttpsPage.class);

		assertEquals("https", tester.getLastRequest().getScheme());
	}

	@Test
	void switchDefaultToHttpsWithCustomPort()
	{
		WicketTester tester = new WicketTester(new HttpsMapperApplication(123, 456));

		requestPage(tester, HttpsPage.class);

		assertEquals("https", tester.getLastRequest().getScheme());
		assertEquals(456, tester.getLastRequest().getServerPort());
	}

	@Test
	void switchHttpsToHttpWithDefaultPorts()
	{
		WicketTester tester = new WicketTester(new HttpsMapperApplication());

		tester.getRequest().setScheme("https");
		tester.getRequest().setServerPort(443);

		requestPage(tester, HttpPage.class);

		assertEquals("http", tester.getLastRequest().getScheme());
	}

	@Test
	void switchHttpsToHttpWithCustomPorts()
	{
		WicketTester tester = new WicketTester(new HttpsMapperApplication(123, 456));

		tester.getRequest().setScheme("https");
		tester.getRequest().setServerPort(443);

		requestPage(tester, HttpPage.class);

		assertEquals("http", tester.getLastRequest().getScheme());
		assertEquals(123, tester.getLastRequest().getServerPort());
	}

	@Test
	void switchHttpToHttpsWithDefaultPorts()
	{
		WicketTester tester = new WicketTester(new HttpsMapperApplication());

		tester.getRequest().setScheme("http");

		requestPage(tester, HttpsPage.class);

		assertEquals("https", tester.getLastRequest().getScheme());
	}

	@Test
	void switchHttpToHttpsWithCustomPorts()
	{
		WicketTester tester = new WicketTester(new HttpsMapperApplication(123, 456));

		tester.getRequest().setScheme("http");
		tester.getRequest().setServerPort(123);

		requestPage(tester, HttpsPage.class);

		assertEquals("https", tester.getLastRequest().getScheme());
		assertEquals(456, tester.getLastRequest().getServerPort());
	}

	@Test
	void testProtocolSwitchForNextRequest()
	{
		WicketTester tester = new WicketTester(new HttpsMapperApplication());

		requestPage(tester, HttpsPage.class);

		assertEquals("https", tester.getLastRequest().getScheme());

		assertEquals("https", tester.getRequest().getScheme());
	}
}
