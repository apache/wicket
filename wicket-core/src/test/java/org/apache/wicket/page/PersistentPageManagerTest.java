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
package org.apache.wicket.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import org.apache.wicket.Application;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.ThreadContext;
import org.apache.wicket.mock.MockPageContext;
import org.apache.wicket.pageStore.IPageContext;
import org.apache.wicket.pageStore.IPageStore;
import org.apache.wicket.pageStore.InSessionPageStore;
import org.apache.wicket.pageStore.NoopPageStore;
import org.apache.wicket.serialize.java.JavaSerializer;
import org.junit.jupiter.api.Test;

/**
 * @author Pedro Santos
 */
class PersistentPageManagerTest
{
	private static final String APP_NAME = "test_app";

	/**
	 * WICKET-3470
	 * 
	 * Tests that a page already put in the session (in SessionEntry) can be serialized and later
	 * deserialized without the need of {@link IPageStore}
	 * 
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	@Test
	void serializationOutsideWicketLifecyle() throws IOException, ClassNotFoundException
	{
		// make sure no leaked threadlocals are present
		ThreadContext.detach();

		// create IPageManager (with IPageStore) and store a page instance
		final AtomicReference<Object> sessionData = new AtomicReference<Object>(null);
		
		IPageManager pageManager = createPageManager(APP_NAME, sessionData);

		// add a page
		TestPage toSerializePage = new TestPage();
		pageManager.touchPage(toSerializePage);
		pageManager.detach();

		// get the stored SessionEntry
		assertNotNull(sessionData.get());

		// destroy the manager and the store
		pageManager.destroy();

		// simulate persisting of the http sessions initiated by the web container
		byte[] serializedSessionData = new JavaSerializer(APP_NAME).serialize(sessionData.get());
		assertNotNull(serializedSessionData, "Wicket needs to be able to serialize the session entry");

		// WicketFilter is not initialized so there is no Application available yet
		assertFalse(Application.exists(), "Worker thread should be unaware of Wicket application");

		// simulate loading of the persisted http session initiated by the web container
		// when starting an application
		sessionData.set(new JavaSerializer(APP_NAME).deserialize(serializedSessionData));

		// without available IPageStore the read SessionEntry holds
		// the IManageablePage itself, not SerializedPage
		assertNotNull(sessionData.get(), "Wicket needs to be able to deserialize the session entry regardless the application availability");

		// provide new IPageStore which will read IManageablePage's or SerializedPage's
		// from the SessionEntry's
		IPageManager newPageManager = createPageManager(APP_NAME, sessionData);

		TestPage deserializedPage = (TestPage)newPageManager.getPage(toSerializePage.getPageId());
		assertNotNull(deserializedPage);
		assertEquals(toSerializePage.instanceID, deserializedPage.instanceID);

		newPageManager.destroy();
	}

	/**
	 * Create a manager that stores session data in the given atomic reference.
	 */
	private IPageManager createPageManager(String appName, AtomicReference<Object> sessionData)
	{
		IPageStore store = new InSessionPageStore(Integer.MAX_VALUE, new JavaSerializer(APP_NAME));
		
		return new PageManager(store) {
			@Override
			protected IPageContext createPageContext()
			{
				return new MockPageContext() {
					@Override
					public <T extends Serializable> T getSessionData(MetaDataKey<T> key, Supplier<T> defaultValue)
					{
						T value = (T)sessionData.get();
						if (value == null) {
							value = defaultValue.get();
							if (value != null) {
								sessionData.set(value);
							}
						}
						return value;
					}
				};
			}
		};
	}

	private static class TestPage implements IManageablePage
	{
		/** */
		private static final long serialVersionUID = 1L;
		private static int sequence;
		private int instanceID;

		private TestPage()
		{
			instanceID = sequence++;
		}

		@Override
		public boolean isPageStateless()
		{
			return false;
		}

		@Override
		public int getPageId()
		{
			return instanceID;
		}

		@Override
		public boolean setFreezePageId(boolean freeze)
		{
			return false;
		}
	}
}
