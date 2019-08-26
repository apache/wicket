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
package org.apache.wicket.util.value;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Test for {@link AttributeMap}.
 * 
 * @author svenmeier
 */
public class AttributeMapTest
{

	@Test
	public void encoding() {
		AttributeMap map = new AttributeMap();
		
		map.putAttribute("key", "-&-ö-<->-");
		
		assertEquals(" key=\"-&amp;-ö-&lt;-&gt;-\"", map.toString());
	}
	
	@Test
	public void putString()
	{
		AttributeMap map = new AttributeMap();

		assertNull(map.putAttribute("foo", "bar"));
		assertEquals("bar", map.get("foo"));

		assertEquals("bar", map.putAttribute("foo", "baz"));
		assertEquals("baz", map.get("foo"));

		assertEquals("baz", map.putAttribute("foo", null));
		assertEquals(null, map.get("foo"));
	}

	@Test
	public void putBoolean()
	{
		AttributeMap map = new AttributeMap();

		assertFalse(map.putAttribute("foo", true));
		assertEquals("foo", map.get("foo"));

		assertTrue(map.putAttribute("foo", false));
		assertEquals(null, map.get("foo"));
	}
}
