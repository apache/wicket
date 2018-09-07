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
package org.apache.wicket.pageStore.memory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/***/
class PageNumberEvictionStrategyTest
{

	private final byte[] PAGE1 = new byte[] { 1 };
	private final byte[] PAGE2 = new byte[] { 2, 3 };

	/***/
	@Test
	void evict()
	{
		// evict to page table with one page only
		PageNumberEvictionStrategy strategy = new PageNumberEvictionStrategy(1);

		PageTable pageTable = new PageTable();

		pageTable.storePage(PAGE1.length, PAGE1);
		assertEquals(1, pageTable.size());
		strategy.evict(pageTable);
		assertEquals(1, pageTable.size());
		assertNotNull(pageTable.getPage(PAGE1.length));

		pageTable.storePage(PAGE2.length, PAGE2);
		assertEquals(2, pageTable.size());
		strategy.evict(pageTable);
		assertEquals(1, pageTable.size());
		assertNotNull(pageTable.getPage(PAGE2.length));
		assertNull(pageTable.getPage(PAGE1.length));
	}

	/**
	 * The number of pages must be at least '1'
	 */
	@Test
	void greaterThanZero()
	{
		assertThrows(IllegalArgumentException.class, () -> {
			new PageNumberEvictionStrategy(0);
		});
	}
}
