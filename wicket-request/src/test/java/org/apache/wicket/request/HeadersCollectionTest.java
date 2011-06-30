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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HeadersCollectionTest
{
	@Test
	public void testHeaderCollection()
	{
		HeaderCollection headers = new HeaderCollection();
		assertTrue(headers.isEmpty());
		
		headers.setHeader("Content-Type", "text/html");
		headers.setHeader("content-type", "text/plain");
		assertEquals("text/plain", headers.getValue("CONTENT-TYPE"));
		
		headers.removeHeader("content-TYPE");
		assertTrue(headers.isEmpty());
		
		headers.setHeader("   Content-Type    ", "    image/jpeg     ");
		headers.setHeader("Content-TYPE    ", "    image/gif     ");
		assertEquals("image/gif", headers.getValue("CONTENT-TYPE"));
		assertEquals(1, headers.getCount());
		
		headers.setHeader("X-Test", "123");
		assertEquals(2, headers.getCount());
		
		headers.removeHeader("   content-TYPE");
		assertEquals(1, headers.getCount());
	}
}
