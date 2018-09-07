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
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

/***/
class PageTableTest
{

	private final byte[] data = new byte[] { 1 };

	/***/
	@Test
	void getOldest()
	{
		PageTable pageTable = new PageTable();

		assertNull(pageTable.getOldest());

		pageTable.storePage(1, data);
		// index: 1
		assertEquals(Integer.valueOf(1), pageTable.getOldest());

		pageTable.storePage(2, data);
		// index: 2, 1
		assertEquals(Integer.valueOf(1), pageTable.getOldest());

		pageTable.storePage(3, data);
		// index: 3, 2, 1
		assertEquals(Integer.valueOf(1), pageTable.getOldest());

		pageTable.getPage(1);
		// index: 1, 3, 2
		assertEquals(Integer.valueOf(2), pageTable.getOldest());

		pageTable.removePage(2);
		// index: 1, 3
		assertEquals(Integer.valueOf(3), pageTable.getOldest());
	}
}
