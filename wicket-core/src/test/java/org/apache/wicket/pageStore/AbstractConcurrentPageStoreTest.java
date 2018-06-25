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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.wicket.page.IManageablePage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Concurrency tests for stores.
 */
public abstract class AbstractConcurrentPageStoreTest
{
	/** Log for reporting. */
	private static final Logger log = LoggerFactory
		.getLogger(AbstractConcurrentPageStoreTest.class);

	private static final Random random = new Random();
	private static final int FILE_SIZE_MIN = 1024 * 200;
	private static final int FILE_SIZE_MAX = 1024 * 300;
	private static final int SESSION_COUNT = 50;
	private static final int FILES_COUNT = 1000;
	private static final int SLEEP_MAX = 10;
	private static final int THREAD_COUNT = 20;
	private static final int READ_MODULO = 100;

	private static final ConcurrentHashMap<String, IPageContext> contexts = new ConcurrentHashMap<>();

	private static IPageContext getContext(String sessionId)
	{
		IPageContext context = new DummyPageContext(sessionId);

		IPageContext existing = contexts.putIfAbsent(sessionId, context);
		return existing != null ? existing : context;
	}

	private static class TaskPage implements IManageablePage
	{
		private final String sessionId;

		private int id;

		private byte[] data;

		public TaskPage(String sessionId, int id)
		{
			this.sessionId = sessionId;

			this.id = id;

			int length = FILE_SIZE_MIN + random.nextInt(FILE_SIZE_MAX - FILE_SIZE_MIN);
			data = new byte[length];
			random.nextBytes(data);
		}

		public String getSessionId()
		{
			return sessionId;
		}

		public boolean check(TaskPage other)
		{
			if (other.data.length != other.data.length)
			{
				log.error("data.length != length");
				return false;
			}
			if (other.id != other.id)
			{
				log.error("data.id != id");
				return false;
			}
			if (other.sessionId != other.sessionId)
			{
				log.error("data.sessionId != sessionId");
				return false;
			}
			return true;
		}

		@Override
		public boolean isPageStateless()
		{
			return false;
		}

		@Override
		public int getPageId()
		{
			return id;
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

	private final Map<String, AtomicInteger> sessionCounter = new ConcurrentHashMap<String, AtomicInteger>();
	private final ConcurrentLinkedQueue<TaskPage> pagesToSave = new ConcurrentLinkedQueue<TaskPage>();
	private final ConcurrentLinkedQueue<TaskPage> filesToRead1 = new ConcurrentLinkedQueue<TaskPage>();
	private final ConcurrentLinkedQueue<TaskPage> filesToRead2 = new ConcurrentLinkedQueue<TaskPage>();

	private final AtomicInteger read1Count = new AtomicInteger(0);
	private final AtomicInteger read2Count = new AtomicInteger(0);
	private final AtomicInteger saveCount = new AtomicInteger(0);

	private final AtomicBoolean saveDone = new AtomicBoolean(false);
	private final AtomicBoolean read1Done = new AtomicBoolean(false);
	private final AtomicBoolean read2Done = new AtomicBoolean(false);

	private final AtomicInteger failures = new AtomicInteger();

	private final AtomicInteger bytesWritten = new AtomicInteger(0);
	private final AtomicInteger bytesRead = new AtomicInteger(0);

	private final AtomicInteger saveTime = new AtomicInteger(0);

	private RuntimeException exceptionThrownByThread;

	private String randomSessionId()
	{
		List<String> s = new ArrayList<String>(sessionCounter.keySet());
		return s.get(random.nextInt(s.size()));
	}

	private int nextSessionId(String sessionId)
	{
		AtomicInteger i = sessionCounter.get(sessionId);
		return i.incrementAndGet();
	}

	private void generateSessionsAndPages()
	{
		for (int i = 0; i < SESSION_COUNT; ++i)
		{
			sessionCounter.put(UUID.randomUUID().toString(), new AtomicInteger(0));
		}
		for (int i = 0; i < FILES_COUNT; ++i)
		{
			String session = randomSessionId();
			TaskPage file = new TaskPage(session, nextSessionId(session));
			long now = System.nanoTime();
			pagesToSave.add(file);
			long duration = System.nanoTime() - now;
			saveTime.addAndGet((int)duration);
		}
	}

	private IPageStore pageStore;

	/**
	 * Stores RuntimeException into a field.
	 */
	private abstract class ExceptionCapturingRunnable implements Runnable
	{
		@Override
		public final void run()
		{
			try
			{
				doRun();
			}
			catch (RuntimeException e)
			{
				exceptionThrownByThread = e;
			}
		}

		/**
		 * Called by {@link #run()}. Thrown RuntimeExceptions are stores into a field for later
		 * check.
		 */
		protected abstract void doRun();
	}

	// Store/Save data in store
	private class SaveRunnable extends ExceptionCapturingRunnable
	{
		@Override
		protected void doRun()
		{
			TaskPage page;

			while ((page = pagesToSave.poll()) != null || saveCount.get() < FILES_COUNT)
			{
				if (page != null)
				{
					pageStore.addPage(getContext(page.getSessionId()), page);

					if (saveCount.get() % READ_MODULO == 0)
					{
						filesToRead1.add(page);
					}
					saveCount.incrementAndGet();
					bytesWritten.addAndGet(page.data.length);
				}

				try
				{
					Thread.sleep(random.nextInt(SLEEP_MAX));
				}
				catch (InterruptedException e)
				{
					log.error(e.getMessage(), e);
				}
			}

			saveDone.set(true);
		}
	};

	// Read data from store
	private class Read1Runnable extends ExceptionCapturingRunnable
	{
		@Override
		protected void doRun()
		{
			TaskPage page;
			while ((page = filesToRead1.poll()) != null || !saveDone.get())
			{
				if (page != null)
				{
					TaskPage other = (TaskPage)pageStore.getPage(getContext(page.getSessionId()),
						page.getPageId());
					if (page.check(other) == false)
					{
						failures.incrementAndGet();
						log.error("Detected error number: " + failures.get());
					}
					filesToRead2.add(page);
					read1Count.incrementAndGet();
					bytesRead.addAndGet(other.data.length);
				}

				try
				{
					Thread.sleep(random.nextInt(SLEEP_MAX));
				}
				catch (InterruptedException e)
				{
					log.error(e.getMessage(), e);
				}
			}

			read1Done.set(true);
		}
	};

	private class Read2Runnable extends ExceptionCapturingRunnable
	{
		@Override
		protected void doRun()
		{
			TaskPage page;
			while ((page = filesToRead2.poll()) != null || !read1Done.get())
			{
				if (page != null)
				{
					TaskPage other = (TaskPage)pageStore.getPage(getContext(page.getSessionId()),
						page.getPageId());
					if (page.check(other) == false)
					{
						failures.incrementAndGet();
						log.error("Detected error number: " + failures.get());
					}
					read2Count.incrementAndGet();
					bytesRead.addAndGet(other.data.length);
				}

				try
				{
					Thread.sleep(random.nextInt(SLEEP_MAX));
				}
				catch (InterruptedException e)
				{
					log.error(e.getMessage(), e);
				}
			}

			read2Done.set(true);
		}
	}

	protected void doTestStore(IPageStore pageStore)
	{
		this.pageStore = new AsynchronousPageStore(pageStore, 100);

		generateSessionsAndPages();
		
		log.info("Starting...");
		long start = System.currentTimeMillis();

		for (int i = 0; i < THREAD_COUNT; ++i)
		{
			new Thread(new Read1Runnable()).start();
		}

		for (int i = 0; i < THREAD_COUNT; ++i)
		{
			new Thread(new Read2Runnable()).start();
		}

		for (int i = 0; i < THREAD_COUNT; ++i)
		{
			new Thread(new SaveRunnable()).start();
		}

		while (!(read1Done.get() && read2Done.get() && saveDone.get()))
		{
			try
			{
				Thread.sleep(50);
			}
			catch (InterruptedException e)
			{
				log.error(e.getMessage(), e);
			}
		}

		if (exceptionThrownByThread != null)
		{
			throw new RuntimeException("One of the worker threads failed.",
				exceptionThrownByThread);
		}

		long duration = System.currentTimeMillis() - start;

		log.info("Took: " + duration + " ms");
		log.info("Save: " + saveCount.intValue() + " files, " + bytesWritten.get() + " bytes");
		log.info("Read: " + (read1Count.get() + read2Count.get()) + " files, " + bytesRead.get()
			+ " bytes");

		log.info("Average save time (ns): " + (double)saveTime.get() / (double)saveCount.get());

		assertEquals(0, failures.get());

		for (String s : sessionCounter.keySet())
		{
			pageStore.removeAllPages(getContext(s));
		}
	}
}