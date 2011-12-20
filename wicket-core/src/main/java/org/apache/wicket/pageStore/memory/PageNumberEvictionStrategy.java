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


/**
 * An eviction strategy which decides whether to evict entries from the in-memory data store
 * depending on the number of stored paged per session
 */
public class PageNumberEvictionStrategy implements IDataStoreEvictionStrategy
{

	private final int pagesNumber;

	/**
	 * Construct.
	 * 
	 * @param pagesNumber
	 *            the maximum number of pages the data store can hold
	 */
	public PageNumberEvictionStrategy(int pagesNumber)
	{
		if (pagesNumber < 1)
		{
			throw new IllegalArgumentException("'pagesNumber' must be greater than 0.");
		}

		this.pagesNumber = pagesNumber;
	}

	/**
	 * 
	 * @see IDataStoreEvictionStrategy#evict(org.apache.wicket.pageStore.memory.PageTable)
	 */
	@Override
	public void evict(PageTable pageTable)
	{
		int size = pageTable.size();
		int pagesToDrop = size - pagesNumber;

		if (pagesToDrop > 0)
		{
			PageTableCleaner cleaner = new PageTableCleaner();
			cleaner.drop(pageTable, pagesToDrop);
		}
	}

}
