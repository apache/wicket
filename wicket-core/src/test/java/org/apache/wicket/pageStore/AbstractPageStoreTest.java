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

import org.apache.wicket.MetaDataKey;
import org.apache.wicket.MockPage;
import org.apache.wicket.serialize.ISerializer;
import org.apache.wicket.serialize.java.JavaSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public abstract class AbstractPageStoreTest
{
	protected final String sessionId = "1234567890";
	protected final int pageId = 123;
	protected final ISerializer serializer = new JavaSerializer(getClass().getName());
	protected int maxEntries = 1;
	protected IPageStore pageStore = null;

	@BeforeEach
	public void before()
	{
		pageStore = createPageStore(serializer, maxEntries);
	}

	protected abstract IPageStore createPageStore(ISerializer serializer, int maxEntries);

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
		IPageContext context = new DummyPageContext(sessionId);
		
		pageStore.addPage(context, new MockPage(pageId));

		assertNotNull(pageStore.getPage(context, pageId));
	}

	/**
	 * Assert that storing a page twice won't keep two entries
	 */
	@Test
	void storePage2()
	{
		IPageContext context = new DummyPageContext(sessionId);
		int maxEntries = 10;

		pageStore = createPageStore(serializer, maxEntries);

		pageStore.addPage(context, new MockPage(pageId));
		pageStore.addPage(context, new MockPage(pageId));

		assertNotNull(pageStore.getPage(context, pageId));

		pageStore.removePage(context, new MockPage(pageId));

		assertNull(pageStore.getPage(context, pageId));
	}

	@Test
	void removePage()
	{
		IPageContext context = new DummyPageContext(sessionId);
		
		pageStore.addPage(context, new MockPage(pageId));

		assertNotNull(pageStore.getPage(context, pageId));

		pageStore.removePage(context, new MockPage(pageId));

		assertNull(pageStore.getPage(context, pageId));
	}

	@Test
	void removeAllPagesDoesNotBindSession()
	{
		IPageContext context = new DummyPageContext(sessionId) {
			@Override
			public <T extends Serializable> void setSessionAttribute(String key, T value) {
				fail();
			}
			
			@Override
			public <T extends Serializable> T setSessionData(MetaDataKey<T> key, T value) {
				return fail();
			}
		};
		
		pageStore.removeAllPages(context);
	}

	@Test
	void removePageDoesNotBindSession()
	{
		IPageContext context = new DummyPageContext(sessionId) {
			@Override
			public <T extends Serializable> void setSessionAttribute(String key, T value) {
				fail();
			}
			
			@Override
			public <T extends Serializable> T setSessionData(MetaDataKey<T> key, T value) {
				return fail();
			}
		};
		
		pageStore.removePage(context, new MockPage());
	}

	@Test
	void removeAllPages()
	{
		IPageContext context = new DummyPageContext(sessionId);
		
		pageStore.addPage(context, new MockPage(pageId));

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
		IPageContext context = new DummyPageContext(sessionId);
		
		pageStore.addPage(context, new MockPage(pageId));

		assertNotNull(pageStore.getPage(context, pageId));

		int pageId2 = 234;
		pageStore.addPage(context, new MockPage(pageId2));
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
		IPageContext context = new DummyPageContext(sessionId);
		IPageContext context2 = new DummyPageContext("0987654321");

		pageStore.addPage(context, new MockPage(pageId));

		assertNotNull(pageStore.getPage(context, pageId));

		pageStore.addPage(context2, new MockPage(pageId));

		assertNotNull(pageStore.getPage(context, pageId));
		assertNotNull(pageStore.getPage(context2, pageId));
	}
}
