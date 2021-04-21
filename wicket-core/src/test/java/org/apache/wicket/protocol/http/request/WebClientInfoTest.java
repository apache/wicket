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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.request.cycle.RequestCycle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests the WebClientInfo class
 */
class WebClientInfoTest
{
	private RequestCycle requestCycleMock;
	private ServletWebRequest webRequest;
	private HttpServletRequest servletRequest;

	/**
	 * Prepare RequestCycle to be able to extract the remote address of the client
	 */
	@BeforeEach
	void before()
	{
		requestCycleMock = mock(RequestCycle.class);

		webRequest = mock(ServletWebRequest.class);
		when(requestCycleMock.getRequest()).thenReturn(webRequest);

		servletRequest = mock(HttpServletRequest.class);
		when(webRequest.getContainerRequest()).thenReturn(servletRequest);
	}

	/**
	 * Test default ip address extraction for container request.
	 */
	@Test
	void testExtractFromContainerRequestNoXForwardedFor()
	{
		String expected = "10.17.37.8";
		String invalid = "10.17.9.55";
		when(servletRequest.getRemoteAddr()).thenReturn(expected);
		when(webRequest.getHeader("X-Forwarded-For")).thenReturn(invalid);
		WebClientInfo clientInfo = new WebClientInfo(requestCycleMock, "No user agent");
		String actual = clientInfo.getRemoteAddr(requestCycleMock);
		assertEquals(expected, actual);
	}
}
