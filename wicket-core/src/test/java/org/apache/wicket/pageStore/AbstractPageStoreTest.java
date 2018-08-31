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
package org.apache.wicket.pageStore;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.apache.wicket.MockPage;
import org.apache.wicket.serialize.ISerializer;
import org.apache.wicket.serialize.java.JavaSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

abstract class AbstractPageStoreTest
{
	final String sessionId = "1234567890";
	final int pageId = 123;
	private final ISerializer serializer = new JavaSerializer(getClass().getName());
	private final IDataStore dataStore = new NoopDataStore();
	private int maxEntries = 1;
	IPageStore pageStore = null;

	@BeforeEach
	void before()
	{
		pageStore = createPageStore(serializer, dataStore, maxEntries);
	}

	abstract IPageStore createPageStore(ISerializer serializer, IDataStore dataStore, int maxEntries);

	@AfterEach
	void after()
	{
		if (pageStore != null)
		{
			pageStore.destroy();
			pageStore = null;
		}
	}

	/**
	 * Assert that a stored page is available to be read
	 */
	@Test
	void storePage()
	{
		pageStore.storePage(sessionId, new MockPage(pageId));

		assertNotNull(pageStore.getPage(sessionId, pageId));
	}

	/**
	 * Assert that storing a page twice won't keep two entries
	 */
	@Test
	void storePage2()
	{
		int maxEntries = 10;

		pageStore = createPageStore(serializer, dataStore, maxEntries);

		pageStore.storePage(sessionId, new MockPage(pageId));
		pageStore.storePage(sessionId, new MockPage(pageId));

		assertNotNull(pageStore.getPage(sessionId, pageId));

		pageStore.removePage(sessionId, pageId);

		assertNull(pageStore.getPage(sessionId, pageId));
	}

	@Test
	void removePage()
	{
		pageStore.storePage(sessionId, new MockPage(pageId));

		assertNotNull(pageStore.getPage(sessionId, pageId));

		pageStore.removePage(sessionId, pageId);

		assertNull(pageStore.getPage(sessionId, pageId));
	}

	/**
	 * Verify that at most {@code maxEntries} per session can be put in the cache
	 */
	@Test
	void maxSizeSameSession()
	{
		pageStore.storePage(sessionId, new MockPage(pageId));

		assertNotNull(pageStore.getPage(sessionId, pageId));

		int pageId2 = 234;
		pageStore.storePage(sessionId, new MockPage(pageId2));
		assertNull(pageStore.getPage(sessionId, pageId));
		assertNotNull(pageStore.getPage(sessionId, pageId2));
	}

	/**
	 * Verify that it is OK to store more pages than {@code maxEntries}
	 * if they are in different sessions
	 */
	@Test
	void maxSizeDifferentSessions()
	{
		String sessionId2 = "0987654321";

		pageStore.storePage(sessionId, new MockPage(pageId));

		assertNotNull(pageStore.getPage(sessionId, pageId));

		pageStore.storePage(sessionId2, new MockPage(pageId));

		assertNull(pageStore.getPage(sessionId, pageId));
		assertNotNull(pageStore.getPage(sessionId2, pageId));
	}
}
