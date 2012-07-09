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
package org.apache.wicket.page.persistent.disk;

import java.security.SecureRandom;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.wicket.pageStore.PageWindowManager;
import org.apache.wicket.pageStore.PageWindowManager.PageWindow;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Matej Knopp
 */
public class PageWindowManagerTest extends Assert
{
	/**
	 * https://issues.apache.org/jira/browse/WICKET-4572
	 */
	@Test
	public void removeObsoleteIndices()
	{
		int page0id = 0,
			page1id = 1,
			page2id = 2;
		int maxSize = 10;

		PageWindowManager manager = new PageWindowManager(maxSize);

		// Add few pages.
		// All of them fully occupy the max space in the pageWindowManager.
		// So adding N+1st page removes the Nth page.
		manager.createPageWindow(page0id, maxSize);
		PageWindow page0Window = manager.getPageWindow(page0id);
		assertWindow(page0Window, page0id, page0Window.getFilePartOffset(), page0Window.getFilePartSize());

		manager.createPageWindow(page1id, maxSize);
		PageWindow page1Window = manager.getPageWindow(page1id);
		assertWindow(page1Window, page1id, page1Window.getFilePartOffset(), page1Window.getFilePartSize());

		// Try to get a page which has been lost with the adding of page1
		assertNull("Page0 must be lost when Page1 has been added.", manager.getPageWindow(page0id));

		manager.createPageWindow(page2id, maxSize);
		PageWindow page2Window = manager.getPageWindow(page2id);
		assertWindow(page2Window, page2id, page2Window.getFilePartOffset(), page2Window.getFilePartSize());

		// Try to get a page which has been lost with the adding of page2
		assertNull("Page1 must be lost when Page2 has been added.", manager.getPageWindow(page1id));
	}

	/**
	 * 
	 */
	@Test
	public void addRemove()
	{
		PageWindowManager manager = new PageWindowManager(300);
		PageWindow window;

		window = manager.createPageWindow(1, 50);
		assertWindow(window, 1, 0, 50);

		window = manager.createPageWindow(2, 40);
		assertWindow(window, 2, 50, 40);

		assertEquals(manager.getTotalSize(), 90);

		window = manager.createPageWindow(2, 30);
		assertWindow(window, 2, 50, 30);
		assertEquals(manager.getTotalSize(), 80);

		manager.removePage(2);
		assertEquals(manager.getTotalSize(), 50);

		window = manager.createPageWindow(3, 30);
		assertWindow(window, 3, 50, 30);
		assertEquals(manager.getTotalSize(), 80);
	}

	/**
	 * 
	 */
	@Test
	public void pageWindowCycle()
	{
		PageWindowManager manager = new PageWindowManager(100);
		PageWindow window;

		window = manager.createPageWindow(1, 30);

		window = manager.createPageWindow(2, 30);

		window = manager.createPageWindow(3, 30);

		assertWindow(window, 3, 60, 30);

		window = manager.createPageWindow(4, 30);

		assertWindow(window, 4, 90, 30);

		// should start at the beginging

		window = manager.createPageWindow(5, 20);

		assertWindow(window, 5, 0, 20);

		assertNull(manager.getPageWindow(1));

		window = manager.getPageWindow(2);
		assertWindow(window, 2, 30, 30);

		window = manager.createPageWindow(6, 10);

		assertWindow(window, 6, 20, 10);

		window = manager.getPageWindow(2);
		assertWindow(window, 2, 30, 30);

		window = manager.createPageWindow(6, 30);
		assertWindow(window, 6, 20, 30);

		assertNull(manager.getPageWindow(2));
		assertNotNull(manager.getPageWindow(3));

		window = manager.createPageWindow(6, 60);
		assertWindow(window, 6, 20, 60);

		assertNull(manager.getPageWindow(3));

		window = manager.createPageWindow(7, 20);
		assertWindow(window, 7, 80, 20);

		assertNotNull(manager.getPageWindow(7));

		// should start at the beginning again

		window = manager.createPageWindow(8, 10);
		assertWindow(window, 8, 0, 10);

		assertNull(manager.getPageWindow(5));
		assertNotNull(manager.getPageWindow(6));

		window = manager.createPageWindow(9, 20);
		assertWindow(window, 9, 10, 20);

		assertNull(manager.getPageWindow(6));
		assertNotNull(manager.getPageWindow(7));

		window = manager.createPageWindow(10, 20);
		assertWindow(window, 10, 30, 20);

		assertNull(manager.getPageWindow(6));
		assertNotNull(manager.getPageWindow(7));

		// make sure when replacing a page that's not last the old "instance" is
		// not valid anymore

		manager.createPageWindow(8, 10);

		window = manager.getPageWindow(8);
		assertWindow(window, 8, 50, 10);
	}


	private void assertWindow(PageWindow window, int pageId, int filePartOffset, int filePartSize)
	{
		assertTrue(window.getPageId() == pageId && window.getFilePartOffset() == filePartOffset &&
			window.getFilePartSize() == filePartSize);
	}

	/** how many operations to execute */
	private static final int EXECUTIONS = 10000;

	/** used to wait the executions */
	private static final CountDownLatch LATCH = new CountDownLatch(EXECUTIONS);

	private final PageWindowManager pageWindowManager = new PageWindowManager(1000L);

	/** the execution types */
	private final Runnable[] TASKS = new Runnable[]
	{
		new CreatePageWindowTask(pageWindowManager),
		new GetPageWindowTask(pageWindowManager),
		new RemovePageInSessionTask(pageWindowManager)
	};

	private static final SecureRandom RND = new SecureRandom();

	/**
	 * Executes random mutator and accessor operations on {@link org.apache.wicket.pageStore.AsynchronousDataStore} validating
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
	}

	private static abstract class AbstractTask implements Runnable
	{
		/** the ids for the stored/removed pages */
		private static final int[] PAGE_IDS = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };

		protected final PageWindowManager pageWindowManager;

		private AbstractTask(PageWindowManager pageWindowManager)
		{
			this.pageWindowManager = pageWindowManager;
		}

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

		protected int getPageId()
		{
			return PAGE_IDS[RND.nextInt(PAGE_IDS.length)];
		}
	}

	private static class CreatePageWindowTask extends AbstractTask
	{
		private CreatePageWindowTask(PageWindowManager pageWindowManager)
		{
			super(pageWindowManager);
		}

		@Override
		public void r()
		{
			pageWindowManager.createPageWindow(getPageId(), 1000);
		}
	}

	private static class GetPageWindowTask extends AbstractTask
	{
		private GetPageWindowTask(PageWindowManager pageWindowManager)
		{
			super(pageWindowManager);
		}

		@Override
		public void r()
		{
			pageWindowManager.getPageWindow(getPageId());
		}
	}

	private static class RemovePageInSessionTask extends AbstractTask
	{
		private RemovePageInSessionTask(PageWindowManager pageWindowManager)
		{
			super(pageWindowManager);
		}

		@Override
		public void r()
		{
			pageWindowManager.removePage(getPageId());
		}
	}
}
