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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import junit.framework.TestCase;

import org.apache.wicket.pageStore.DefaultPageStore;
import org.apache.wicket.pageStore.IDataStore;
import org.apache.wicket.pageStore.IPageStore;
import org.apache.wicket.pageStore.memory.DummyPageManagerContext;
import org.apache.wicket.util.lang.WicketObjects;
import org.apache.wicket.versioning.InMemoryPageStore;

/**
 * @author Pedro Santos
 */
public class PersistentPageManagerTest extends TestCase
{
	/**
	 * WICKET-3470
	 * 
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public void testSerializationOutsideWicketLifecyle() throws IOException, ClassNotFoundException
	{
		IPageManager pageManager = newPersistentPageManager("test_app");
		TestPage toSerializePage = new TestPage();
		pageManager.touchPage(toSerializePage);
		pageManager.commitRequest();
		pageManager.destroy();
		// serializing the Wicket piece in servlet session
		Serializable sessionEntry = pageManager.getContext().getSessionAttribute(null);
		byte[] serializedSessionEntry = WicketObjects.objectToByteArray(sessionEntry);
		assertNotNull("Wicket needs to be able to serialize the session entry",
			serializedSessionEntry);

		// testing if it is possible to deserialize the session entry
		IPageManager newPageManager = newPersistentPageManager("test_app");
		ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(
			serializedSessionEntry));
		assertNull("Worker thread should be unaware of Wicket application", in.readObject());
		Serializable loadedSessionEntry = (Serializable)in.readObject();
		assertNotNull(
			"Wicket needs to be able to deserialize the session entry regardless the application availability",
			loadedSessionEntry);

		newPageManager.getContext().setSessionAttribute(null, loadedSessionEntry);


		TestPage deserializedPage = (TestPage)newPageManager.getPage(toSerializePage.getPageId());
		assertNotNull(deserializedPage);
		assertEquals(toSerializePage.instanceID, deserializedPage.instanceID);

		newPageManager.destroy();
	}

	private PersistentPageManager newPersistentPageManager(String appName)
	{
		IDataStore dataStore = new InMemoryPageStore();
		IPageStore pageStore = new DefaultPageStore(appName, dataStore, 4);
		IPageManagerContext pageManagerContext = new DummyPageManagerContext();
		return new PersistentPageManager(appName, pageStore, pageManagerContext);
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

		public boolean isPageStateless()
		{
			return false;
		}

		public int getPageId()
		{
			return instanceID;
		}

		public void detach()
		{
		}

		public boolean setFreezePageId(boolean freeze)
		{
			return false;
		}
	}
}
