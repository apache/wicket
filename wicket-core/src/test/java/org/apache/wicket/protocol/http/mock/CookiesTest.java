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
package org.apache.wicket.protocol.http.mock;

import javax.servlet.http.Cookie;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for Cookies helper class
 */
public class CookiesTest extends Assert
{
	@Test
	public void testIsEqual() throws Exception
	{
		Cookie c1 = new Cookie("Name", "Value");
		Cookie c2 = new Cookie("Name", "Value");

		assertTrue(Cookies.isEqual(c1, c2));

		c2.setPath("Path");
		assertFalse(Cookies.isEqual(c1, c2));

		c1.setPath("Path");
		assertTrue(Cookies.isEqual(c1, c2));

		c2.setDomain("Domain");
		assertFalse(Cookies.isEqual(c1, c2));

		c1.setDomain("Domain");
		assertTrue(Cookies.isEqual(c1, c2));
	}
}
