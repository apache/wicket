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
package org.apache.wicket.protocol.ws.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.wicket.protocol.http.mock.MockHttpServletRequest;
import org.apache.wicket.protocol.http.mock.MockHttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests that {@link ServletRequestCopy} keeps the header contract of {@link HttpServletRequest}.
 */
class ServletRequestCopyTest
{
	private MockHttpServletRequest original;

	@BeforeEach
	void before()
	{
		original = new MockHttpServletRequest(null, new MockHttpSession(null), null,
			Locale.GERMANY);
	}

	@Test
	void headerNamesAreCaseInsensitive()
	{
		original.addHeader("origin", "http://example.com");

		ServletRequestCopy copy = new ServletRequestCopy(original);

		assertEquals("http://example.com", copy.getHeader("Origin"));
		assertEquals(List.of("http://example.com"), Collections.list(copy.getHeaders("ORIGIN")));
	}

	@Test
	void headersCanBeReadRepeatedly()
	{
		original.addHeader("Accept-Encoding", "gzip");
		original.addHeader("Accept-Encoding", "br");
		original.addHeader("Max-Forwards", "10");

		ServletRequestCopy copy = new ServletRequestCopy(original);

		assertEquals("gzip", copy.getHeader("Accept-Encoding"));
		assertEquals("gzip", copy.getHeader("Accept-Encoding"));
		assertEquals(List.of("gzip", "br"), Collections.list(copy.getHeaders("Accept-Encoding")));
		assertEquals(List.of("gzip", "br"), Collections.list(copy.getHeaders("Accept-Encoding")));
		assertEquals(10, copy.getIntHeader("Max-Forwards"));
		assertEquals(10, copy.getIntHeader("Max-Forwards"));
	}

	@Test
	void absentHeaderHasNoValues()
	{
		ServletRequestCopy copy = new ServletRequestCopy(original);

		assertNull(copy.getHeader("X-Absent"));
		assertNotNull(copy.getHeaders("X-Absent"));
		assertEquals(List.of(), Collections.list(copy.getHeaders("X-Absent")));
		assertEquals(-1, copy.getIntHeader("X-Absent"));
	}

	@Test
	void headerReportedUnderSeveralCasesIsCopiedOnce()
	{
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getSession(true)).thenReturn(new MockHttpSession(null));
		when(request.getHeaderNames())
			.thenReturn(Collections.enumeration(List.of("Origin", "origin")));
		when(request.getHeaders(anyString()))
			.thenAnswer(invocation -> Collections.enumeration(List.of("http://example.com")));

		ServletRequestCopy copy = new ServletRequestCopy(request);

		assertEquals(List.of("Origin"), Collections.list(copy.getHeaderNames()));
		assertEquals(List.of("http://example.com"), Collections.list(copy.getHeaders("origin")));
	}

	@Test
	void localesCanBeReadRepeatedly()
	{
		HttpServletRequest copy = new ServletRequestCopy(original);

		List<Locale> expected = List.of(Locale.GERMANY, Locale.GERMAN);
		assertEquals(expected, Collections.list(copy.getLocales()));
		assertEquals(expected, Collections.list(copy.getLocales()));
	}

	@Test
	void missingLocalesAreCopiedAsNone()
	{
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getSession(true)).thenReturn(new MockHttpSession(null));

		HttpServletRequest copy = new ServletRequestCopy(request);

		assertEquals(List.of(), Collections.list(copy.getLocales()));
	}
}
