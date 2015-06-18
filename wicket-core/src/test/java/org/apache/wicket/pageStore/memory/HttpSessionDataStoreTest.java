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

import static org.junit.Assert.assertArrayEquals;

import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 */
public class HttpSessionDataStoreTest extends WicketTestCase
{
	final String sessionId = "anything";

	final int pageId = 1;

	final byte[] PAGE1 = new byte[] { 1 };
	final byte[] PAGE2 = new byte[] { 2 };

	HttpSessionDataStore store;

	/**
	 * before()
	 */
	@Before
	public void before()
	{
		store = new HttpSessionDataStore(new DummyPageManagerContext(), new NoopEvictionStrategy());
	}

	/**
	 * after()
	 */
	@After
	public void after()
	{
		store.destroy();
	}

	/**
	 * storePage()
	 */
	@Test
	public void storePage()
	{
		assertNull(store.getData(sessionId, pageId));

		store.storeData(sessionId, pageId, PAGE1);
		assertArrayEquals(PAGE1, store.getData(sessionId, pageId));
	}

	/**
	 * removePage1()
	 */
	@Test
	public void removePage1()
	{
		assertNull(store.getData(sessionId, pageId));

		store.storeData(sessionId, pageId, PAGE1);

		assertNotNull(store.getData(sessionId, pageId));

		store.removeData(sessionId, pageId);

		assertNull(store.getData(sessionId, pageId));
	}

	/**
	 * removePage2()
	 */
	@Test
	public void removePage2()
	{
		assertNull(store.getData(sessionId, pageId));

		store.storeData(sessionId, pageId, PAGE1);

		assertNotNull(store.getData(sessionId, pageId));

		store.removeData(sessionId);

		assertNull(store.getData(sessionId, pageId));
	}


	private static final class NoopEvictionStrategy implements IDataStoreEvictionStrategy
	{

		@Override
		public void evict(PageTable pageTable)
		{
		}
	}
}
