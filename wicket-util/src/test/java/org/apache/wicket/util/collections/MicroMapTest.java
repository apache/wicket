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
package org.apache.wicket.util.collections;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author almaw
 */
public class MicroMapTest extends Assert
{
	private static final Object FOO = new Object();
	private static final Object BAR = new Object();

	/**
	 * Basic test for keySet(), entrySet() and values().
	 */
	@Test
	public void microMap()
	{
		MicroMap<Object, Object> m = new MicroMap<>();
		m.put(FOO, BAR);

		// Test .keySet();
		Set<Object> s = m.keySet();
		assertEquals(1, m.size());
		assertEquals(1, s.size());

		Iterator<?> i = s.iterator();
		assertTrue(i.hasNext());
		Object key = i.next();
		assertEquals(FOO, key);
		assertFalse(i.hasNext());
		try
		{
			i.next();
			fail("Expected i.next() to fail with NoSuchElementException");
		}
		catch (NoSuchElementException e)
		{
			// Swallow this.
		}

		// Do approx the same again with the .entrySet()
		Set<Entry<Object, Object>> entrySet = m.entrySet();
		assertEquals(1, m.size());
		assertEquals(1, entrySet.size());

		i = entrySet.iterator();
		assertTrue(i.hasNext());
		@SuppressWarnings("unchecked")
		Map.Entry<Object, Object> entry = (Map.Entry<Object, Object>)i.next();
		assertEquals(FOO, entry.getKey());
		assertEquals(BAR, entry.getValue());
		assertFalse(i.hasNext());
		try
		{
			i.next();
			fail("Expected i.next() to fail with NoSuchElementException");
		}
		catch (NoSuchElementException e)
		{
			// Swallow this.
		}

		// Do approx the same again with the .values()
		Collection<Object> v = m.values();
		assertEquals(1, m.size());
		assertEquals(1, v.size());

		i = v.iterator();
		assertTrue(i.hasNext());
		Object value = i.next();
		assertEquals(BAR, value);
		assertFalse(i.hasNext());
		try
		{
			i.next();
			fail("Expected i.next() to fail with NoSuchElementException");
		}
		catch (NoSuchElementException e)
		{
			// Swallow this.
		}
	}
}
