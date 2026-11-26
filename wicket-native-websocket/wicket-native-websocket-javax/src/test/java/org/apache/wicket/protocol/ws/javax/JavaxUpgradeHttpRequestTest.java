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
package org.apache.wicket.protocol.ws.javax;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.Session;

import org.apache.wicket.protocol.http.mock.MockHttpSession;
import org.apache.wicket.protocol.ws.api.ServletRequestCopy;
import org.apache.wicket.protocol.ws.api.WebSocketConnectionOriginFilter;
import org.junit.jupiter.api.Test;

/**
 * Tests that {@link JavaxUpgradeHttpRequest} keeps the header contract of
 * {@link HttpServletRequest}.
 */
class JavaxUpgradeHttpRequestTest
{
	@Test
	void headerNamesAreCaseInsensitive()
	{
		JavaxUpgradeHttpRequest request = upgradeRequest(
			Map.of("origin", List.of("http://example.com")));

		assertEquals("http://example.com", request.getHeader("Origin"));
		assertEquals(List.of("http://example.com"), Collections.list(request.getHeaders("ORIGIN")));
	}

	@Test
	void headerNamesDifferingOnlyInCaseAreMerged()
	{
		Map<String, List<String>> headers = new LinkedHashMap<>();
		headers.put("Origin", List.of("http://example.com"));
		headers.put("origin", List.of("http://example.org"));

		JavaxUpgradeHttpRequest request = upgradeRequest(headers);

		assertEquals(List.of("Origin"), Collections.list(request.getHeaderNames()));
		assertEquals(List.of("http://example.com", "http://example.org"),
			Collections.list(request.getHeaders("origin")));
	}

	@Test
	void absentHeaderHasNoValues()
	{
		JavaxUpgradeHttpRequest request = upgradeRequest(null);

		assertNull(request.getHeader("Origin"));
		assertNotNull(request.getHeaders("Origin"));
		assertEquals(List.of(), Collections.list(request.getHeaders("Origin")));
		assertNotNull(request.getHeaderNames());
		assertEquals(List.of(), Collections.list(request.getHeaderNames()));
	}

	@Test
	void copyOfTheUpgradeRequestPassesTheOriginFilter()
	{
		ServletRequestCopy copy = new ServletRequestCopy(
			upgradeRequest(Map.of("origin", List.of("http://example.com"))));

		WebSocketConnectionOriginFilter filter = new WebSocketConnectionOriginFilter(
			List.of("http://example.com"));
		assertNull(filter.doFilter(copy));
		assertNull(filter.doFilter(copy));
		assertEquals("http://example.com", copy.getHeader("Origin"));
	}

	private static JavaxUpgradeHttpRequest upgradeRequest(Map<String, List<String>> headers)
	{
		Map<String, Object> userProperties = new HashMap<>();
		userProperties.put(JavaxUpgradeHttpRequest.SESSION, new MockHttpSession(null));
		if (headers != null)
		{
			userProperties.put(JavaxUpgradeHttpRequest.HEADERS, headers);
		}

		Session session = mock(Session.class);
		when(session.getUserProperties()).thenReturn(userProperties);
		return new JavaxUpgradeHttpRequest(session, mock(EndpointConfig.class));
	}
}
