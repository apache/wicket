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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import org.apache.wicket.Application;
import org.apache.wicket.ThreadContext;
import org.apache.wicket.pageStore.DefaultPageStore;
import org.apache.wicket.pageStore.IDataStore;
import org.apache.wicket.pageStore.IPageStore;
import org.apache.wicket.pageStore.memory.DummyPageManagerContext;
import org.apache.wicket.serialize.java.JavaSerializer;
import org.apache.wicket.versioning.InMemoryPageStore;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Pedro Santos
 */
public class PersistentPageManagerTest
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
	public void serializationOutsideWicketLifecyle() throws IOException, ClassNotFoundException
	{
		// make sure no leaked threadlocals are present
		ThreadContext.detach();

		// create IPageManager (with IPageStore) and store a page instance
		IPageManager pageManager = newPersistentPageManager(APP_NAME);
		TestPage toSerializePage = new TestPage();
		pageManager.touchPage(toSerializePage);
		pageManager.commitRequest();

		// get the stored SessionEntry
		Serializable sessionEntry = pageManager.getContext().getSessionAttribute(null);

		// destroy the manager and the store
		pageManager.destroy();

		// simulate persisting of the http sessions initiated by the web container
		byte[] serializedSessionEntry = new JavaSerializer(APP_NAME).serialize(sessionEntry);
		assertNotNull("Wicket needs to be able to serialize the session entry",
			serializedSessionEntry);

		// simulate loading of the persisted http session initiated by the web container
		// when starting an application
		ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(
			serializedSessionEntry));

		// WicketFilter is not initialized so there is no Application available yet
		Assert.assertFalse("Worker thread should be unaware of Wicket application",
			Application.exists());

		assertEquals(APP_NAME, in.readObject());

		// without available IPageStore the read SessionEntry holds
		// the IManageablePage itself, not SerializedPage
		Serializable loadedSessionEntry = (Serializable)in.readObject();
		assertNotNull(
			"Wicket needs to be able to deserialize the session entry regardless the application availability",
			loadedSessionEntry);

		// provide new IPageStore which will read IManageablePage's or SerializedPage's
		// from the SessionEntry's
		IPageManager newPageManager = newPersistentPageManager(APP_NAME);
		newPageManager.getContext().setSessionAttribute(null, loadedSessionEntry);

		TestPage deserializedPage = (TestPage)newPageManager.getPage(toSerializePage.getPageId());
		assertNotNull(deserializedPage);
		assertEquals(toSerializePage.instanceID, deserializedPage.instanceID);

		newPageManager.destroy();
	}

	private PageStoreManager newPersistentPageManager(String appName)
	{
		IDataStore dataStore = new InMemoryPageStore();
		IPageStore pageStore = new DefaultPageStore(new JavaSerializer(appName), dataStore, 4);
		IPageManagerContext pageManagerContext = new DummyPageManagerContext();
		return new PageStoreManager(appName, pageStore, pageManagerContext);
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
		public void detach()
		{
		}

		@Override
		public boolean setFreezePageId(boolean freeze)
		{
			return false;
		}
	}
}
