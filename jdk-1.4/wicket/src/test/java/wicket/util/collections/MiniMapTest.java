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
package wicket.util.collections;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import junit.framework.TestCase;

/**
 * @author almaw
 */
public class MiniMapTest extends TestCase
{
	private static final Object FOO = new Object();
	private static final Object BAR = new Object();

	/**
	 * Basic test for keySet(), entrySet() and values().
	 */
	public void testMiniMap()
	{
		MiniMap m = new MiniMap(3);
		m.put(FOO, BAR);

		// Test .keySet();
		Set s = m.keySet();
		assertEquals(1, m.size());

		Iterator i = s.iterator();
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
		s = m.entrySet();
		assertEquals(1, m.size());

		i = s.iterator();
		assertTrue(i.hasNext());
		Map.Entry entry = (Map.Entry)i.next();
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
		Collection v = m.values();
		assertEquals(1, m.size());

		i = v.iterator();
		assertTrue(i.hasNext());
		Object value = i.next();
		assertEquals(BAR, value);
		assertFalse(i.hasNext());
		try
		{
			Object wibble = i.next();
			wibble = i.next();
			wibble = i.next();
			wibble = i.next();
			wibble = i.next();
			fail("Expected i.next() to fail with NoSuchElementException");
		}
		catch (NoSuchElementException e)
		{
			// Swallow this.
		}
	}
}
