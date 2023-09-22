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
import static org.junit.jupiter.api.Assertions.fail;

import java.io.Serializable;
import java.util.function.Supplier;

import org.apache.wicket.MetaDataKey;
import org.apache.wicket.mock.MockPageContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public abstract class AbstractPageStoreTest
{
	protected final String sessionId = "1234567890";
	protected final int pageId = 123;
	
	/**
	 * Maximum entries in store.
	 */
	protected int maxEntries = 1;
	
	/**
	 * Data for stored pages.
	 */
	protected byte[] pageData = new byte[1];
	
	protected IPageStore pageStore = null;

	@BeforeEach
	public void before()
	{
		pageStore = createPageStore( maxEntries);
	}

	protected abstract IPageStore createPageStore(int maxEntries);

	@AfterEach
	public void after()
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
		IPageContext context = new MockPageContext(sessionId);
		
		pageStore.addPage(context, new SerializedPage(pageId, pageData));

		assertNotNull(pageStore.getPage(context, pageId));
	}

	/**
	 * Assert that storing a page twice won't keep two entries
	 */
	@Test
	void storePage2()
	{
		pageStore.destroy();
		
		IPageContext context = new MockPageContext(sessionId);
		int maxEntries = 10;

		pageStore = createPageStore(maxEntries);

		pageStore.addPage(context, new SerializedPage(pageId, pageData));
		pageStore.addPage(context, new SerializedPage(pageId, pageData));

		assertNotNull(pageStore.getPage(context, pageId));

		pageStore.removePage(context, new SerializedPage(pageId, pageData));

		assertNull(pageStore.getPage(context, pageId));
	}

	@Test
	void removePage()
	{
		IPageContext context = new MockPageContext(sessionId);
		
		pageStore.addPage(context, new SerializedPage(pageId, pageData));

		assertNotNull(pageStore.getPage(context, pageId));

		pageStore.removePage(context, new SerializedPage(pageId, pageData));

		assertNull(pageStore.getPage(context, pageId));
		
		int pageId2 = 234;
		pageStore.removePage(context, new SerializedPage(pageId2, pageData));
	}

	@Test
	void removeAllPagesDoesNotBindSession()
	{
		IPageContext context = new MockPageContext(sessionId) {
			@Override
			public <T extends Serializable> T getSessionAttribute(String key, Supplier<T> value) {
				if (value != null) {
					fail();
				}
				
				return null;
			}
			
			@Override
			public <T extends Serializable> T getSessionData(MetaDataKey<T> key, Supplier<T> value) {
				if (value != null) {
					return fail();
				}
				
				return null;
			}
		};
		
		pageStore.removeAllPages(context);
	}

	@Test
	void removePageDoesNotBindSession()
	{
		IPageContext context = new MockPageContext(sessionId) {
			@Override
			public <T extends Serializable> T getSessionAttribute(String key, Supplier<T> value) {
				if (value != null) {
					fail();
				}
				
				return null;
			}
			
			@Override
			public <T extends Serializable> T getSessionData(MetaDataKey<T> key, Supplier<T> value) {
				if (value != null) {
					return fail();
				}
				
				return null;
			}
		};
		
		pageStore.removePage(context, new SerializedPage(0, pageData));
	}

	@Test
	void removeAllPages()
	{
		IPageContext context = new MockPageContext(sessionId);
		
		pageStore.addPage(context, new SerializedPage(pageId, pageData));

		assertNotNull(pageStore.getPage(context, pageId));

		pageStore.removeAllPages(context);

		assertNull(pageStore.getPage(context, pageId));
	}

	/**
	 * Verify that at most {@code maxEntries} per session can be put in the store
	 */
	@Test
	void maxSizeSameSession()
	{
		IPageContext context = new MockPageContext(sessionId);
		
		pageStore.addPage(context, new SerializedPage(pageId, pageData));

		assertNotNull(pageStore.getPage(context, pageId));

		int pageId2 = 234;
		pageStore.addPage(context, new SerializedPage(pageId2, pageData));
		assertNull(pageStore.getPage(context, pageId));
		assertNotNull(pageStore.getPage(context, pageId2));
	}

	/**
	 * Verify that it is OK to store more pages than {@code maxEntries}
	 * if they are in different sessions
	 */
	@Test
	void maxSizeDifferentSessions()
	{
		IPageContext context = new MockPageContext(sessionId);
		IPageContext context2 = new MockPageContext("0987654321");

		pageStore.addPage(context, new SerializedPage(pageId, pageData));

		assertNotNull(pageStore.getPage(context, pageId));

		pageStore.addPage(context2, new SerializedPage(pageId, pageData));

		assertNotNull(pageStore.getPage(context, pageId));
		assertNotNull(pageStore.getPage(context2, pageId));
	}
}
