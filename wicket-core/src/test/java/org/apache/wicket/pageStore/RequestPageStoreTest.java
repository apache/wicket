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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.wicket.MockPage;
import org.apache.wicket.mock.MockPageContext;
import org.apache.wicket.mock.MockPageStore;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link RequestPageStore}
 * 
 * @author svenmeier
 */
public class RequestPageStoreTest
{

	@Test
	void testAdd()
	{
		MockPageStore mockStore = new MockPageStore();
		
		MockPageContext context = new MockPageContext();

		RequestPageStore store = new RequestPageStore(mockStore);
		
		MockPage page1 = new MockPage(1);
		MockPage page2 = new MockPage(2);
		MockPage page3 = new MockPage(3);
		
		store.addPage(context, page1);
		store.addPage(context, page2);
		store.addPage(context, page3);
		
		assertTrue(mockStore.getPages().isEmpty(), "no pages delegated before detach");
		
		store.detach(context);
		
		assertEquals(3, mockStore.getPages().size(), "pages delegated on detach");
		
		mockStore.getPages().clear();
		
		assertNull(store.getPage(context, 1), "no page in request store");
		assertNull(store.getPage(context, 2), "no page in request store");
		assertNull(store.getPage(context, 3), "no page in request store");
	}
	
	@Test
	void testUntouch()
	{
		MockPageStore mockStore = new MockPageStore();
		
		MockPageContext context = new MockPageContext();

		RequestPageStore store = new RequestPageStore(mockStore);
		
		MockPage page = new MockPage(1);
		
		store.addPage(context, page);
		
		store.revertPage(context, page);
		
		assertTrue(mockStore.getPages().isEmpty(), "no page delegated before detach");
		
		store.detach(context);
		
		assertEquals(0, mockStore.getPages().size(), "untouched page not delegated on detach");
		
		assertNull(store.getPage(context, 1), "no page in request store");
	}
}
