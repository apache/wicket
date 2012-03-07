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

import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.core.util.lang.WicketObjects;

/**
 * An eviction strategy that keeps the data store size up to configured bytes
 */
public class MemorySizeEvictionStrategy implements IDataStoreEvictionStrategy
{

	private final Bytes maxBytes;

	/**
	 * Construct.
	 * 
	 * @param maxBytes
	 *            the maximum size of the data store
	 */
	public MemorySizeEvictionStrategy(Bytes maxBytes)
	{
		Args.notNull(maxBytes, "maxBytes");

		this.maxBytes = maxBytes;
	}

	/**
	 * 
	 * @see IDataStoreEvictionStrategy#evict(org.apache.wicket.pageStore.memory.PageTable)
	 */
	@Override
	public void evict(PageTable pageTable)
	{

		long storeCurrentSize = WicketObjects.sizeof(pageTable);

		if (storeCurrentSize > maxBytes.bytes())
		{
			PageTableCleaner cleaner = new PageTableCleaner();
			cleaner.drop(pageTable, 1);

			// recurse until enough space is cleaned
			evict(pageTable);
		}
	}

}
