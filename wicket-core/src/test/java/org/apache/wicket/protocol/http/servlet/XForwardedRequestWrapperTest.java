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
import static org.junit.jupiter.api.Assertions.assertFalse;

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.markup.html.basic.SimplePage;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.protocol.http.mock.MockHttpServletRequest;
import org.apache.wicket.protocol.http.mock.MockHttpServletResponse;
import org.apache.wicket.util.tester.WicketTestCase;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 
 * @author Juergen Donnerstag
 */
class XForwardedRequestWrapperTest extends WicketTestCase
{
	private MockHttpServletRequest request;
	private XForwardedRequestWrapperFactory filter;

	/**
	 * @throws Exception
	 */
	@BeforeEach
	void before() throws Exception
	{
		filter = new XForwardedRequestWrapperFactory();
		request = tester.getRequest();
	}

	/** */
	@Test
	void test1()
	{
		HttpServletRequest resp = filter.getWrapper(request);
		assertEquals("127.0.0.1", resp.getRemoteAddr());
		assertEquals(null, resp.getHeader("x-forwarded-for"));
		assertEquals(null, resp.getHeader("x-forwarded-by"));
		assertEquals(null, resp.getHeader("x-forwarded-proto"));
		assertEquals("http", resp.getScheme());
		assertFalse(resp.isSecure());
		assertEquals(80, resp.getServerPort());
	}

	/** */
	@Test
	void test2()
	{
		filter.getConfig().setProtocolHeader("x-forwarded-proto");

		HttpServletRequest resp = filter.getWrapper(request);
		assertEquals("127.0.0.1", resp.getRemoteAddr());
		assertEquals(null, resp.getHeader("x-forwarded-for"));
		assertEquals(null, resp.getHeader("x-forwarded-by"));
		assertEquals(null, resp.getHeader("x-forwarded-proto"));
		assertEquals("http", resp.getScheme());
		assertFalse(resp.isSecure());
		assertEquals(80, resp.getServerPort());
	}

	/**
	 * Sample with internal proxies
	 */
	@Test
	void test3()
	{
		filter.getConfig().setAllowedInternalProxies("192\\.168\\.0\\.10, 192\\.168\\.0\\.11");
		filter.getConfig().setRemoteIPHeader("x-forwarded-for");
		filter.getConfig().setProxiesHeader("x-forwarded-by");
		filter.getConfig().setProtocolHeader("x-forwarded-proto");

		request.setRemoteAddr("192.168.0.10");
		request.addHeader("x-forwarded-for", "140.211.11.130, 192.168.0.10");
		// request.addHeader("x-forwarded-by", null);
		request.addHeader("x-forwarded-proto", "https");
		request.setScheme("http");
		request.setSecure(false);
		request.setServerPort(80);

		HttpServletRequest resp = filter.getWrapper(request);
		assertEquals("140.211.11.130", resp.getRemoteAddr());
		assertEquals(null, resp.getHeader("x-forwarded-for"));
		assertEquals(null, resp.getHeader("x-forwarded-by"));
		assertEquals("https", resp.getHeader("x-forwarded-proto"));
		assertEquals("https", resp.getScheme());
		assertEquals(true, resp.isSecure());
		assertEquals(443, resp.getServerPort());
	}

	/**
	 * Sample with trusted proxies
	 */
	@Test
	void test4()
	{
		filter.getConfig().setAllowedInternalProxies("192\\.168\\.0\\.10, 192\\.168\\.0\\.11");
		filter.getConfig().setRemoteIPHeader("x-forwarded-for");
		filter.getConfig().setProxiesHeader("x-forwarded-by");
		filter.getConfig().setTrustedProxies("proxy1, proxy2");

		request.setRemoteAddr("192.168.0.10");
		request.addHeader("x-forwarded-for", "140.211.11.130, proxy1, proxy2");

		HttpServletRequest resp = filter.getWrapper(request);
		assertEquals("140.211.11.130", resp.getRemoteAddr());
		assertEquals(null, resp.getHeader("x-forwarded-for"));
		assertEquals("proxy1, proxy2", resp.getHeader("x-forwarded-by"));
	}

	/**
	 * Sample with internal and trusted proxies
	 */
	@Test
	void test5()
	{
		filter.getConfig().setAllowedInternalProxies("192\\.168\\.0\\.10, 192\\.168\\.0\\.11");
		filter.getConfig().setRemoteIPHeader("x-forwarded-for");
		filter.getConfig().setProxiesHeader("x-forwarded-by");
		filter.getConfig().setTrustedProxies("proxy1, proxy2");

		request.setRemoteAddr("192.168.0.10");
		request.addHeader("x-forwarded-for", "140.211.11.130, proxy1, proxy2, 192.168.0.10");

		HttpServletRequest resp = filter.getWrapper(request);
		assertEquals("140.211.11.130", resp.getRemoteAddr());
		assertEquals(null, resp.getHeader("x-forwarded-for"));
		assertEquals("proxy1, proxy2", resp.getHeader("x-forwarded-by"));
	}

	/**
	 * Sample with an untrusted proxy
	 */
	@Test
	void test6()
	{
		filter.getConfig().setAllowedInternalProxies("192\\.168\\.0\\.10, 192\\.168\\.0\\.11");
		filter.getConfig().setRemoteIPHeader("x-forwarded-for");
		filter.getConfig().setProxiesHeader("x-forwarded-by");
		filter.getConfig().setTrustedProxies("proxy1, proxy2");

		request.setRemoteAddr("192.168.0.10");
		request.addHeader("x-forwarded-for", "140.211.11.130, untrusted-proxy, proxy1");

		HttpServletRequest resp = filter.getWrapper(request);
		assertEquals("untrusted-proxy", resp.getRemoteAddr());
		assertEquals("140.211.11.130", resp.getHeader("x-forwarded-for"));
		assertEquals("proxy1", resp.getHeader("x-forwarded-by"));
	}

	private class MyApplication extends MockApplication
	{
		XForwardedRequestWrapperFactory factory;

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void init()
		{
			super.init();

			factory = new XForwardedRequestWrapperFactory();
			factory.init(getWicketFilter().getFilterConfig());

			getFilterFactoryManager().add(factory);
		}
	}

	/**
	 * @throws Exception
	 * 
	 */
	@Test
	void test7() throws Exception
	{
		MyApplication app = new MyApplication();
		tester = new WicketTester(app);

		app.factory.getConfig().setAllowedInternalProxies("192\\.168\\.0\\.10, 192\\.168\\.0\\.11");
		app.factory.getConfig().setRemoteIPHeader("x-forwarded-for");
		app.factory.getConfig().setProxiesHeader("x-forwarded-by");
		app.factory.getConfig().setTrustedProxies("proxy1, proxy2");

		request.setRemoteAddr("192.168.0.10");
		request.addHeader("x-forwarded-for", "140.211.11.130, untrusted-proxy, proxy1");

		tester.startPage(SimplePage.class);
		tester.assertRenderedPage(SimplePage.class);
		tester.assertResultPage(SimplePage.class, "SimplePageExpectedResult.html");

		MockHttpServletResponse resp = tester.getResponse();

		// @TODO should there be any header in the response ????
		// assertEquals("140.211.11.130", resp.getHeader("x-forwarded-for"));
		// assertEquals("proxy1", resp.getHeader("x-forwarded-by"));
	}
}
