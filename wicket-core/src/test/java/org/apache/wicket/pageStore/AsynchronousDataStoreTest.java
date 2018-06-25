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

import org.apache.wicket.MockPage;
import org.apache.wicket.page.IManageablePage;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link AsynchronousPageStore}
 */
public class AsynchronousDataStoreTest
{
	private static final IPageStore WRAPPED_PAGE_STORE = new InMemoryPageStore("test", Integer.MAX_VALUE);

	/** the data store under test */
	private static final IPageStore ASYNC_PAGE_STORE = new AsynchronousPageStore(WRAPPED_PAGE_STORE, 100);

	/** the used jsessionid's */
	private static final IPageContext[] CONTEXT = new IPageContext[] { createContext("s1"), createContext("s2"), createContext("s3")};

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
	 * Executes random mutator and accessor operations on {@link AsynchronousPageStore} validating
	 * that the used data structures can be used simultaneously.
	 * 
	 * @throws Exception
	 */
	@Test
	void randomOperations() throws Exception
	{
		ExecutorService executorService = Executors.newFixedThreadPool(50);

		for (int i = 0; i < EXECUTIONS; i++)
		{
			Runnable task = TASKS[RND.nextInt(TASKS.length)];
			executorService.submit(task);
		}
		LATCH.await();
		executorService.shutdown();
		ASYNC_PAGE_STORE.destroy();
	}

	private static IPageContext createContext(String sessionId)
	{
		return new DummyPageContext(sessionId);
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

		protected IPageContext getPageContext()
		{
			return CONTEXT[RND.nextInt(CONTEXT.length)];
		}

		protected int getPageId()
		{
			return PAGE_IDS[RND.nextInt(PAGE_IDS.length)];
		}
		
		protected IManageablePage getPage()
		{
			return new MockPage(getPageId());
		}
	}

	private static class StoreTask extends AbstractTask
	{
		@Override
		public void r()
		{
			ASYNC_PAGE_STORE.addPage(getPageContext(), getPage());
		}
	}

	private static class GetTask extends AbstractTask
	{
		@Override
		public void r()
		{
			ASYNC_PAGE_STORE.getPage(getPageContext(), getPageId());
		}
	}

	private static class RemovePageInSessionTask extends AbstractTask
	{
		@Override
		public void r()
		{
			ASYNC_PAGE_STORE.removePage(getPageContext(), getPage());
		}
	}

	private static class RemoveSessionTask extends AbstractTask
	{
		@Override
		public void r()
		{
			ASYNC_PAGE_STORE.removeAllPages(getPageContext());
		}
	}
}
