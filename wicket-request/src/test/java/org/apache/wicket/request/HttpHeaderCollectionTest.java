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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import org.apache.wicket.util.time.Time;
import org.junit.Test;

public class HttpHeaderCollectionTest
{
	@Test
	public void testHeaderCollection()
	{
		HttpHeaderCollection headers = new HttpHeaderCollection();
		assertTrue(headers.isEmpty());

		headers.addHeader("X-Test", "foo");
		headers.addHeader("X-Test", "bar");
		assertArrayEquals(new String[]{"foo", "bar"}, headers.getHeaderValues("X-Test"));

		headers.removeHeader("x-test");
		assertTrue(headers.isEmpty());

		headers.addHeader("   X-Image    ", "    jpeg     ");
		headers.addHeader("X-Image    ", "    gif     ");
		assertArrayEquals(new String[]{"jpeg", "gif"}, headers.getHeaderValues("X-IMAGE"));
		assertEquals(1, headers.getCount());

		headers.addHeader("X-Test", "123");
		assertEquals(2, headers.getCount());

		headers.removeHeader(" x-tesT ");
		assertEquals(1, headers.getCount());

		// empty header values are valid (RFC2616)
		headers.setHeader("foobaz", "");
		assertEquals("", headers.getHeader("foobaz"));

		// empty header values are valid (RFC2616)
		headers.setHeader("foobaz", "     ");
		assertEquals("", headers.getHeader("foobaz"));
		
	}

	@Test
	public void getHeaderNames()
	{
		final HttpHeaderCollection headers = new HttpHeaderCollection();

		headers.addHeader("key1", "a");
		headers.addHeader("Key1", "b");
		headers.addHeader("key2", "c");

		Set<String> names = headers.getHeaderNames();
		assertTrue(names.contains("key1"));
		assertFalse(names.contains("Key1"));
		assertTrue(names.contains("key2"));
	}

	@Test
	public void dateValues()
	{
		final HttpHeaderCollection headers = new HttpHeaderCollection();

		final Time time1 = Time.millis(1000000);
		final Time time2 = Time.millis(2000000);

		headers.setDateHeader("date", time1);
		headers.addDateHeader("date", time2);
		headers.addHeader("date", "not-a-date");

		assertEquals(time1, headers.getDateHeader("date"));
		assertEquals("Thu, 01 Jan 1970 00:16:40 GMT", headers.getHeader("date"));

		// a change of the locale or timezone must not affect the date format
		final Locale defaultLocale = Locale.getDefault();
		final TimeZone defaultLocaleefaultTimezone = TimeZone.getDefault();

		try
		{
			final String expected = "Thu, 01 Jan 1970 00:16:40 GMT";

			Locale.setDefault(Locale.CHINESE);
			TimeZone.setDefault(TimeZone.getTimeZone("CET"));
			assertEquals(expected, headers.getHeader("date"));

			Locale.setDefault(Locale.US);
			TimeZone.setDefault(TimeZone.getTimeZone("EST"));
			assertEquals(expected, headers.getHeader("date"));
		}
		finally
		{
			Locale.setDefault(defaultLocale);
			TimeZone.setDefault(defaultLocaleefaultTimezone);
		}

		assertArrayEquals(new String[]{"Thu, 01 Jan 1970 00:16:40 GMT", "Thu, 01 Jan 1970 00:33:20 GMT", "not-a-date"},
		                  headers.getHeaderValues("date"));
		
		headers.setHeader("date", "foobar");
		try
		{
			Time date = headers.getDateHeader("date");
			fail();
		}
		catch (IllegalStateException e)
		{
			// ok
		}
	}
}
