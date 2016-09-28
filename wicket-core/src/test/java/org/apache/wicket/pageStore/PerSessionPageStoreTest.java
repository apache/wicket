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

import org.apache.wicket.MockPage;
import org.apache.wicket.serialize.ISerializer;
import org.junit.Test;

/**
 * Tests for PerSessionPageStore
 */
public class PerSessionPageStoreTest extends AbstractPageStoreTest
{
	@Override
	protected IPageStore createPageStore(ISerializer serializer, IDataStore dataStore, int maxEntries)
	{
		return new PerSessionPageStore(serializer, dataStore, maxEntries);
	}

	/**
	 * Verify that it is OK to store more pages than {@code maxEntries}
	 * if they are in different sessions
	 */
	@Test
	@Override
	public void maxSizeDifferentSessions()
	{
		String sessionId2 = "0987654321";

		pageStore.storePage(sessionId, new MockPage(pageId));

		assertNotNull(pageStore.getPage(sessionId, pageId));

		pageStore.storePage(sessionId2, new MockPage(pageId));

		assertNotNull(pageStore.getPage(sessionId, pageId));
		assertNotNull(pageStore.getPage(sessionId2, pageId));
	}
}
