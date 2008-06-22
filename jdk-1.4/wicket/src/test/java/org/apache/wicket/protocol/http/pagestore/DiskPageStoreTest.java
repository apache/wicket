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
package org.apache.wicket.protocol.http.pagestore;

import junit.framework.TestCase;

import org.apache.wicket.util.tester.WicketTester;

/**
 * @author frankbille
 */
public class DiskPageStoreTest extends TestCase
{
	/**
	 * The stop method on the PageSavingThread didn't wait until the thread was actually finished
	 * until it continued the execution to the main thread. This can in some situations lead to the
	 * main thread being terminated before the PageSavingThread.
	 * 
	 * This test doesn't do anything but making sure that destroying the {@link DiskPageStore} also
	 * stops the PageSavingThread.
	 */
	public void testStoppingPageSavingThread()
	{
		new WicketTester();

		DiskPageStore store = new DiskPageStore()
		{
			// Make sure that it's asynchronous, because that's what we are testing here.
			protected boolean isSynchronous()
			{
				return false;
			}
		};

		// Assume that the PageSavingThread is in the same ThreadGroup as the current thread.
		int activeThreadsBefore = Thread.activeCount();

		store.destroy();

		int activeThreadsAfter = Thread.activeCount();

		// it can be that we are a bit to fast in this test.
		// if it is not down by 1 yet sleep for 2 seconds to give it time to kill itself.
		if (activeThreadsAfter != (activeThreadsBefore - 1))
		{
			try
			{
				Thread.sleep(2000);
			}
			catch (InterruptedException e)
			{
				// ignore
			}
			activeThreadsAfter = Thread.activeCount();
		}

		assertEquals(activeThreadsBefore - 1, activeThreadsAfter);
	}
}
