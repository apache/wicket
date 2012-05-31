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

import java.security.SecureRandom;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.wicket.versioning.InMemoryPageStore;
import org.junit.Test;

/**
 * Tests for {@link AsynchronousDataStore}
 */
public class AsynchronousDataStoreTest
{
//	private static final IDataStore WRAPPED_DATA_STORE = new DiskDataStore("asyncDataStoreApp", new StoreSettings(null).getFileStoreFolder(), Bytes.kilobytes(1));
	private static final IDataStore WRAPPED_DATA_STORE = new InMemoryPageStore();

	/** the data store under test */
	private static final IDataStore DATA_STORE = new AsynchronousDataStore(WRAPPED_DATA_STORE, 100);

	/** the data for each page */
	private static final byte[] DATA = new byte[] { 1, 2, 3 };

	/** the used jsessionid's */
	private static final String[] SESSIONS = new String[] { "s1", "s2", "s3" };

	/** the ids for the stored/removed pages */
	private static final int[] PAGE_IDS = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };

	/** how many operations to execute */
	private static final int EXECUTIONS = 10000;

	/** used to wait the executions */
	private static final CountDownLatch LATCH = new CountDownLatch(EXECUTIONS);

	/** the execution types */
	private static final Runnable[] TASKS = new Runnable[] { new StoreTask(), new GetTask(),
			new RemovePageInSessionTask(), new RemoveSessionTask() };

	private static final SecureRandom RND = new SecureRandom();

	/**
	 * Executes random mutator and accessor operations on {@link AsynchronousDataStore} validating
	 * that the used data structures can be used simultaneously.
	 * 
	 * @throws Exception
	 */
	@Test
	public void randomOperations() throws Exception
	{
		ExecutorService executorService = Executors.newFixedThreadPool(50);

		for (int i = 0; i < EXECUTIONS; i++)
		{
			Runnable task = TASKS[RND.nextInt(TASKS.length)];
			executorService.submit(task);
		}
		LATCH.await();
		executorService.shutdown();
		DATA_STORE.destroy();
	}

	private static abstract class AbstractTask implements Runnable
	{
		protected abstract void r();

		@Override
		public void run()
		{
			try
			{
				r();
			}
			finally
			{
				LATCH.countDown();
			}
		}

		protected String getSessionId()
		{
			return SESSIONS[RND.nextInt(SESSIONS.length)];
		}

		protected int getPageId()
		{
			return PAGE_IDS[RND.nextInt(PAGE_IDS.length)];
		}
	}

	private static class StoreTask extends AbstractTask
	{
		@Override
		public void r()
		{
			DATA_STORE.storeData(getSessionId(), getPageId(), DATA);
		}
	}

	private static class GetTask extends AbstractTask
	{
		@Override
		public void r()
		{
			DATA_STORE.getData(getSessionId(), getPageId());
		}
	}

	private static class RemovePageInSessionTask extends AbstractTask
	{
		@Override
		public void r()
		{
			DATA_STORE.removeData(getSessionId(), getPageId());
		}
	}

	private static class RemoveSessionTask extends AbstractTask
	{
		@Override
		public void r()
		{
			DATA_STORE.removeData(getSessionId());
		}
	}
}
