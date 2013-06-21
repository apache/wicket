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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * 
 */
public class MostRecentlyUsedMapTest
{
	/**
	 * Tests that {@link MostRecentlyUsedMap} contains at most 2 entries
	 * 
	 * @see <a href="https://issues.apache.org/jira/browse/WICKET-3209">WICKET-3209</a>
	 */
	@Test
	public void max2Entries()
	{
		MostRecentlyUsedMap<String, String> map = new MostRecentlyUsedMap<>(2);
		assertEquals(0, map.size());
		map.put("1", "one");
		assertEquals(1, map.size());
		map.put("2", "two");
		assertEquals(2, map.size());
		map.put("3", "three");
		assertEquals(2, map.size());
		assertTrue(map.containsKey("2"));
		assertTrue(map.containsKey("3"));
	}
}
