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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.core.util.lang.WicketObjects;
import org.junit.Test;

/***/
public class MemorySizeEvictionStrategyTest
{
	final byte[] PAGE1 = new byte[] { 1 };
	final byte[] PAGE2 = new byte[] { 2, 3 };

	/***/
	@Test
	public void evict()
	{
		PageTable pageTable = new PageTable();

		long sizeOfEmptyPageTable = WicketObjects.sizeof(pageTable);

		// evict to empty page table
		MemorySizeEvictionStrategy strategy = new MemorySizeEvictionStrategy(
			Bytes.bytes(sizeOfEmptyPageTable));
		pageTable.storePage(PAGE1.length, PAGE1);
		assertEquals(1, pageTable.size());
		strategy.evict(pageTable);
		assertEquals(0, pageTable.size());
		long currentSize = WicketObjects.sizeof(pageTable);
		assertTrue("Current size: |" + currentSize + "|, strategy size: |" + sizeOfEmptyPageTable +
			"|", currentSize <= sizeOfEmptyPageTable);

		// evict to page table with size: empty + PAGE2
		pageTable.storePage(PAGE2.length, PAGE2);
		long sizeOfWithPage2 = WicketObjects.sizeof(pageTable);
		strategy = new MemorySizeEvictionStrategy(Bytes.bytes(sizeOfWithPage2));
		pageTable.storePage(PAGE1.length, PAGE1);
		assertEquals(2, pageTable.size());
		strategy.evict(pageTable);
		// the following assertion depends on the fact that PAGE2 has
		// bigger size than PAGE1
		assertEquals(1, pageTable.size());
		currentSize = WicketObjects.sizeof(pageTable);
		assertTrue("Current size: |" + currentSize + "|, strategy size: |" + sizeOfWithPage2 + "|",
			currentSize <= sizeOfWithPage2);
	}
}
