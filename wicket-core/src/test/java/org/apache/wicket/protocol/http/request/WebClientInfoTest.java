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
package org.apache.wicket.protocol.http.request;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.request.cycle.RequestCycle;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Tests the WebClientInfo class
 */
public class WebClientInfoTest
{
	private RequestCycle requestCycleMock;
	private ServletWebRequest webRequest;
	private HttpServletRequest servletRequest;

	/**
	 * Prepare RequestCycle to be able to extract the remote address of the client
	 */
	@Before
	public void before()
	{
		requestCycleMock = mock(RequestCycle.class);

		webRequest = mock(ServletWebRequest.class);
		when(requestCycleMock.getRequest()).thenReturn(webRequest);

		servletRequest = mock(HttpServletRequest.class);
		when(webRequest.getContainerRequest()).thenReturn(servletRequest);
	}

	/**
	 * Test X-Forwarded-For ip address extraction.
	 */
	@Test
	public void testExtractFromXForwardedForHeader()
	{
		String expected = "127.0.0.1";
		when(webRequest.getHeader("X-Forwarded-For")).thenReturn(expected);
		WebClientInfo clientInfo = new WebClientInfo(requestCycleMock, "No user agent");
		String actual = clientInfo.getRemoteAddr(requestCycleMock);
		assertThat(actual, is(equalTo(expected)));
		Mockito.verifyZeroInteractions(servletRequest);
	}

	/**
	 * Test X-Forwarded-For ip address extraction with fallback when no ip is contained.
	 *
	 * Note mgrigorov: this test could fail in network setups where unknown addresses, like "blah",
	 * will resolve to some DNS service saying "'blah' domain is free. Buy it."
	 */
	@Test
	@Ignore
	public void testExtractFromContainerRequestUnknownXForwardedFor()
	{
		String expected = "10.17.37.8";
		when(servletRequest.getRemoteAddr()).thenReturn(expected);
		when(webRequest.getHeader("X-Forwarded-For")).thenReturn("unknown");
		WebClientInfo clientInfo = new WebClientInfo(requestCycleMock, "No user agent");
		String actual = clientInfo.getRemoteAddr(requestCycleMock);
		assertThat(actual, is(equalTo(expected)));
	}

	/**
	 * Test default ip address extraction for container request.
	 */
	@Test
	public void testExtractFromContainerRequestNoXForwardedFor()
	{
		String expected = "10.17.37.8";
		when(servletRequest.getRemoteAddr()).thenReturn(expected);
		WebClientInfo clientInfo = new WebClientInfo(requestCycleMock, "No user agent");
		String actual = clientInfo.getRemoteAddr(requestCycleMock);
		assertThat(actual, is(equalTo(expected)));
	}

	/**
	 * Test X-Forwarded-For ip address extraction when proxy chain is given.
	 */
	@Test
	public void testExtractFromXForwardedForHeaderChainedIps()
	{
		String expected = "10.17.37.156";
		when(servletRequest.getRemoteAddr()).thenReturn("10.17.1.1");
		when(webRequest.getHeader("X-Forwarded-For")).thenReturn(expected + ", 10.17.37.1");
		WebClientInfo clientInfo = new WebClientInfo(requestCycleMock, "No user agent");
		String actual = clientInfo.getRemoteAddr(requestCycleMock);
		assertThat(actual, is(equalTo(expected)));
	}

	/**
	 * Test X-Forwarded-For ipv6 address extraction.
	 */
	@Test
	public void testExtractFromXForwardedForHeaderIPv6()
	{
		String expected = "2001:db8::1428:57";
		when(webRequest.getHeader("X-Forwarded-For")).thenReturn("2001:db8::1428:57");
		WebClientInfo clientInfo = new WebClientInfo(requestCycleMock, "No user agent");
		String actual = clientInfo.getRemoteAddr(requestCycleMock);
		assertThat(actual, is(equalTo(expected)));
	}

}
