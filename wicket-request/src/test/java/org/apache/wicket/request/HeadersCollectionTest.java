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

import org.junit.Test;

import static org.junit.Assert.*;

public class HeadersCollectionTest
{
	@Test
	public void testHeaderCollection()
	{
		HeaderCollection headers = new HeaderCollection();
		assertTrue(headers.isEmpty());

		headers.addHeader("X-Test", "foo");
		headers.addHeader("X-Test", "bar");
		assertArrayEquals(new String[]{"foo", "bar"}, headers.getValues("X-Test"));

		headers.removeHeaderValues("x-test");
		assertTrue(headers.isEmpty());

		headers.addHeader("   X-Image    ", "    jpeg     ");
		headers.addHeader("X-Image    ", "    gif     ");
		assertArrayEquals(new String[]{"jpeg", "gif"}, headers.getValues("X-IMAGE"));
		assertEquals(1, headers.getCount());

		headers.addHeader("X-Test", "123");
		assertEquals(2, headers.getCount());

		headers.removeHeaderValues(" x-tesT ");
		assertEquals(1, headers.getCount());
	}
}
