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

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.WicketTestCase;
import org.apache.wicket.protocol.http.filter.XForwardedWicketFilterExtension;
import org.apache.wicket.protocol.http.mock.MockHttpServletRequest;

/**
 * 
 * @author Juergen Donnerstag
 */
public class XForwardedWicketFilterExtensionTest extends WicketTestCase
{
	MockHttpServletRequest request;
	XForwardedWicketFilterExtension filter;

	@Override
	protected void setUp() throws Exception
	{
		super.setUp();

		filter = new XForwardedWicketFilterExtension();
		tester.getApplication().getWicketFilter().addFilter(filter);
		request = tester.getRequest();
	}

	/** */
	public void test1()
	{
		HttpServletRequest resp = (HttpServletRequest)filter.getRequestWrapper(request);
		assertEquals("127.0.0.1", resp.getRemoteAddr());
		assertEquals(null, resp.getHeader("x-forwarded-for"));
		assertEquals(null, resp.getHeader("x-forwarded-by"));
		assertEquals(null, resp.getHeader("x-forwarded-proto"));
		assertEquals("http", resp.getScheme());
		assertFalse(resp.isSecure());
		assertEquals(80, resp.getServerPort());
	}

	/** */
	public void test2()
	{
		filter.getConfig().setProtocolHeader("x-forwarded-proto");

		HttpServletRequest resp = (HttpServletRequest)filter.getRequestWrapper(request);
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
	public void test3()
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

		HttpServletRequest resp = (HttpServletRequest)filter.getRequestWrapper(request);
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
	public void test4()
	{
		filter.getConfig().setAllowedInternalProxies("192\\.168\\.0\\.10, 192\\.168\\.0\\.11");
		filter.getConfig().setRemoteIPHeader("x-forwarded-for");
		filter.getConfig().setProxiesHeader("x-forwarded-by");
		filter.getConfig().setTrustedProxies("proxy1, proxy2");

		request.setRemoteAddr("192.168.0.10");
		request.addHeader("x-forwarded-for", "140.211.11.130, proxy1, proxy2");

		HttpServletRequest resp = (HttpServletRequest)filter.getRequestWrapper(request);
		assertEquals("140.211.11.130", resp.getRemoteAddr());
		assertEquals(null, resp.getHeader("x-forwarded-for"));
		assertEquals("proxy1, proxy2", resp.getHeader("x-forwarded-by"));
	}

	/**
	 * Sample with internal and trusted proxies
	 */
	public void test5()
	{
		filter.getConfig().setAllowedInternalProxies("192\\.168\\.0\\.10, 192\\.168\\.0\\.11");
		filter.getConfig().setRemoteIPHeader("x-forwarded-for");
		filter.getConfig().setProxiesHeader("x-forwarded-by");
		filter.getConfig().setTrustedProxies("proxy1, proxy2");

		request.setRemoteAddr("192.168.0.10");
		request.addHeader("x-forwarded-for", "140.211.11.130, proxy1, proxy2, 192.168.0.10");

		HttpServletRequest resp = (HttpServletRequest)filter.getRequestWrapper(request);
		assertEquals("140.211.11.130", resp.getRemoteAddr());
		assertEquals(null, resp.getHeader("x-forwarded-for"));
		assertEquals("proxy1, proxy2", resp.getHeader("x-forwarded-by"));
	}

	/**
	 * Sample with an untrusted proxy
	 */
	public void test6()
	{
		filter.getConfig().setAllowedInternalProxies("192\\.168\\.0\\.10, 192\\.168\\.0\\.11");
		filter.getConfig().setRemoteIPHeader("x-forwarded-for");
		filter.getConfig().setProxiesHeader("x-forwarded-by");
		filter.getConfig().setTrustedProxies("proxy1, proxy2");

		request.setRemoteAddr("192.168.0.10");
		request.addHeader("x-forwarded-for", "140.211.11.130, untrusted-proxy, proxy1");

		HttpServletRequest resp = (HttpServletRequest)filter.getRequestWrapper(request);
		assertEquals("untrusted-proxy", resp.getRemoteAddr());
		assertEquals("140.211.11.130", resp.getHeader("x-forwarded-for"));
		assertEquals("proxy1", resp.getHeader("x-forwarded-by"));
	}
}
