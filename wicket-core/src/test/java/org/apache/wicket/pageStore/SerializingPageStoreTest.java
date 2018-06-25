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

import org.apache.wicket.MockPage;
import org.apache.wicket.mock.MockPageContext;
import org.apache.wicket.mock.MockPageStore;
import org.apache.wicket.page.IManageablePage;
import org.apache.wicket.serialize.java.JavaSerializer;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link SerializingPageStore}.
 */
public class SerializingPageStoreTest
{

	@Test
	void test()
	{
		MockPageContext context = new MockPageContext();
		
		MockPageStore mockStore = new MockPageStore();
		
		IManageablePage original = new MockPage(1);
		
		SerializingPageStore store = new SerializingPageStore(mockStore, new JavaSerializer("test"));
		
		store.addPage(context, original);
		
		SerializedPage serialized = (SerializedPage)mockStore.getPage(context, 1);
		assertEquals(1, serialized.getPageId(), "page was serialized");
		
		MockPage deserialized = (MockPage)store.getPage(context, 1);
		assertEquals(1, deserialized.getPageId(), "page was deserialized");
	}
}
