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
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.concurrent.CountDownLatch;

import org.apache.wicket.mock.MockPageContext;
import org.apache.wicket.mock.MockPageStore;
import org.apache.wicket.pageStore.AsynchronousPageStore;
import org.apache.wicket.pageStore.IPageContext;
import org.apache.wicket.pageStore.IPageStore;
import org.apache.wicket.pageStore.SerializedPage;
import org.apache.wicket.util.WicketTestTag;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * https://issues.apache.org/jira/browse/WICKET-6629
 */
@Tag(WicketTestTag.SLOW)
class AsyncPageStoreManagerTest
{
	private CountDownLatch added = new CountDownLatch(1);
	private CountDownLatch allowAdd = new CountDownLatch(1);
	
	/*
	 * Symptoms:
	 * 1) Out of memory on DiskDataStore.sessionEntryMap, map contains thousands of
	 * DiskDataStore$SessionEntry's; The vm only contains a few hundred PageStoreManager$SessionEntry's
	 * (as expected for the currently active sessions)
	 * 2) Slowly growing disk usage, the Wicket filestore contains thousands of directories, up to the point
	 * where ls becomes unusable.
	 * 
	 * Problem (as far as I can tell):
	 * The PageSavingRunnable of the AsynchronousPageStore saves entries to the DiskDataStore after the
	 * corresponding session has been invalidated and its DiskDataStore has been cleaned. Because the session is
	 * destroyed according to the container and Wicket session handling there is nothing triggering a cleanup for
	 * this session anymore.
	 */
	@Test
	void invalidateSessionBeforeSave() throws InterruptedException
	{
		MockPageStore testStore = new MockPageStore() {
			
			@Override
			public void addPage(IPageContext context, IManageablePage page)
			{
				super.addPage(context, page);

				added.countDown();
				
				try
				{
					allowAdd.await();
				}
				catch (InterruptedException interrupted)
				{
				}
			}
		};
		
		IPageStore asyncPageStore = new AsynchronousPageStore(testStore, 100);
		
		IPageContext contextA = new MockPageContext("A");

		// add a page to the session
		asyncPageStore.addPage(contextA, new SerializedPage(1, new byte[0]));

		// wait for page being added
		added.await();

		assertEquals(1, testStore.getPage(contextA, 1).getPageId());

		// add a second page to this session
		asyncPageStore.addPage(contextA, new SerializedPage(2, new byte[0]));

		// Session is invalidated
		asyncPageStore.removeAllPages(contextA);

		// indeed empty
		assertNull(testStore.getPage(contextA, 1));
		assertNull(testStore.getPage(contextA, 2));

		// allow next page add
		allowAdd.countDown();
		
		// wait for page being added
		added = new CountDownLatch(1);
		asyncPageStore.addPage(new MockPageContext("B"), new SerializedPage(3, new byte[0]));
		added.await();

		// Session has been invalidated. The pageStore should not contain any pages for this
		// session, because they will never be cleaned!
		assertNull(testStore.getPage(contextA, 1));
		assertNull(testStore.getPage(contextA, 2));
		
		//destroy page manager to clean static variables 
		testStore.destroy();
	}
}