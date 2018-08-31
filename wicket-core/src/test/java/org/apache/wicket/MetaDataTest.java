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
package org.apache.wicket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

/**
 * Some tests for meta data.
 */
class MetaDataTest
{
	private static final MetaDataKey<String> KEY1 = new MetaDataKey<String>()
	{
		private static final long serialVersionUID = 1L;
	};

	private static final MetaDataKey<String> KEY2 = new MetaDataKey<String>()
	{
		private static final long serialVersionUID = 1L;
	};

	private static final MetaDataKey<String> KEY3 = new MetaDataKey<String>()
	{
		private static final long serialVersionUID = 1L;
	};

	private static final MetaDataKey<String> KEY4 = new MetaDataKey<String>()
	{
		private static final long serialVersionUID = 1L;
	};

	/**
	 * Test bounds and basic operations.
	 */
	@Test
	void metaDataKey()
	{
		MetaDataEntry<?>[] md = KEY1.set(null, "1");
		assertNotNull(md);
		assertEquals(1, md.length);
		md = KEY1.set(md, null);
		assertNull(md);
		md = KEY1.set(md, "1");
		md = KEY2.set(md, "2");
		md = KEY3.set(md, "3");
		md = KEY4.set(md, "4");
		assertEquals(4, md.length);
		md = KEY3.set(md, null);
		assertEquals(3, md.length);
		assertEquals("1", KEY1.get(md));
		assertEquals("2", KEY2.get(md));
		assertEquals(null, KEY3.get(md));
		assertEquals("4", KEY4.get(md));
		md = KEY4.set(md, null);
		assertEquals(2, md.length);
		assertEquals("1", KEY1.get(md));
		assertEquals("2", KEY2.get(md));
		assertEquals(null, KEY3.get(md));
		assertEquals(null, KEY4.get(md));
		md = KEY1.set(md, null);
		assertEquals(1, md.length);
		assertEquals(null, KEY1.get(md));
		assertEquals("2", KEY2.get(md));
		assertEquals(null, KEY3.get(md));
		assertEquals(null, KEY4.get(md));
		md = KEY2.set(md, null);
		assertNull(md);
	}
}
