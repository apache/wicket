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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.Serializable;
import java.util.function.Supplier;

import javax.servlet.http.HttpSessionBindingListener;

import org.apache.wicket.mock.MockPageContext;
import org.apache.wicket.page.IManageablePage;
import org.junit.jupiter.api.Test;

/**
 * Test vor {@link AbstractPersistentPageStore}.
 */
class AbstractPersistentPageStoreTest
{

	/**
	 * WICKET-6990
	 */
	@Test
	void rebindingAttributeDoesNotRemoveAllPages()
	{
		var store = new AbstractPersistentPageStore("fooBar")
		{
			
			@Override
			public boolean supportsVersioning()
			{
				return false;
			}
			
			@Override
			protected void removePersistedPage(String sessionIdentifier, IManageablePage page)
			{
			}
			
			@Override
			protected void removeAllPersistedPages(String sessionIdentifier)
			{
				fail("unexpected removal of all pages while rebinding attribute");
			}
			
			@Override
			protected IManageablePage getPersistedPage(String sessionIdentifier, int id)
			{
				return null;
			}
			
			@Override
			protected void addPersistedPage(String sessionIdentifier, IManageablePage page)
			{
				
			}
		};
		
		var context = new MockPageContext() {
			@Override
			public <T extends Serializable> T getSessionAttribute(String key,
				Supplier<T> defaultValue)
			{
				T attribute = super.getSessionAttribute(key, defaultValue);
				
				// simulate container unbinding when attribute is set again
				((HttpSessionBindingListener)attribute).valueUnbound(null);
				
				return attribute;
			}
		};
		assertTrue(store.canBeAsynchronous(context));
		
	}

}
