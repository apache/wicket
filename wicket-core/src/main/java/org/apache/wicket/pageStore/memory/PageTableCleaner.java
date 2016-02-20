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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class that knows how to remove the nth oldest pages from {@link PageTable}
 */
public class PageTableCleaner
{
	private static final Logger LOG = LoggerFactory.getLogger(PageTableCleaner.class);

	/**
	 * Removes {@code pagesNumber} of pages from the {@link PageTable pageTable}
	 * 
	 * @param pageTable
	 *            the {@link PageTable} to clean
	 * @param pagesNumber
	 *            the number of pages to remove
	 */
	public void drop(final PageTable pageTable, final int pagesNumber)
	{
		for (int i = 0; i < pagesNumber; i++)
		{
			Integer pageIdOfTheOldest = pageTable.getOldest();
			pageTable.removePage(pageIdOfTheOldest);
			LOG.debug("Evicted page with id '{}' from the HttpSessionDataStore");
		}
	}
}
