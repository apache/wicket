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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.time.Duration;
import java.time.Instant;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.wicket.MockPage;
import org.apache.wicket.core.util.lang.WicketObjects;
import org.apache.wicket.mock.MockPageManager;
import org.apache.wicket.page.PageAccessSynchronizer.PageLock;
import org.apache.wicket.util.WicketTestTag;
import org.apache.wicket.util.time.Durations;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
@Tag(WicketTestTag.SLOW)
class PageAccessSynchronizerTest
{
	private static final Logger logger = LoggerFactory.getLogger(PageAccessSynchronizerTest.class);

	// TODO had a 30 second timeout rule, Junit 5 atm does not support timeouts (see discussion at
	// https://github.com/junit-team/junit5/issues/80)

	/**
	 * @throws Exception
	 */
	@Test
	void testReentrant() throws Exception
	{
		final PageAccessSynchronizer sync = new PageAccessSynchronizer(Duration.ofSeconds(5));
		sync.lockPage(0);
		sync.lockPage(0);
	}

	/**
	 * @throws Exception
	 */
	@Test
	void testBlocking() throws Exception
	{
		final PageAccessSynchronizer sync = new PageAccessSynchronizer(Duration.ofSeconds(5));
		final Duration hold = Duration.ofSeconds(1);
		final Instant t1locks[] = new Instant[1];
		final Instant t2locks[] = new Instant[1];

		class T1 extends Thread
		{
			@Override
			public void run()
			{
				sync.lockPage(1);
				t1locks[0] = Instant.now();
                try
                {
                    Thread.sleep(hold.toMillis());
                }
                catch (InterruptedException e)
                {
                    throw new RuntimeException(e);
                }
				sync.unlockAllPages();
			}
		}

		class T2 extends Thread
		{
			@Override
			public void run()
			{
				sync.lockPage(1);
				t2locks[0] = Instant.now();
				sync.unlockAllPages();
			}
		}

		T1 t1 = new T1();
		t1.setName("t1");
		T2 t2 = new T2();
		t2.setName("t2");
		t1.start();
		TimeUnit.MILLISECONDS.sleep(100);
		t2.start();

		t1.join();
		t2.join();

		assertTrue(!t2locks[0].isBefore(t1locks[0].plus(hold)));
	}

	/**
	 * @param pages
	 * @param workers
	 * @param duration
	 * @throws Exception
	 */
	private void runContentionTest(final int pages, final int workers, final Duration duration)
		throws Exception
	{
		final PageAccessSynchronizer sync = new PageAccessSynchronizer(Duration.ofSeconds(1));

		final AtomicInteger[] counts = new AtomicInteger[pages];
		for (int i = 0; i < counts.length; i++)
		{
			counts[i] = new AtomicInteger();
		}

		final AtomicInteger hits = new AtomicInteger();

		final String[] error = new String[1];

		class Worker extends Thread
		{
			@Override
			public void run()
			{
				Random random = new Random();
				Instant start = Instant.now();

				while (Durations.elapsedSince(start).compareTo(duration) < 0 && error[0] == null)
				{
					logger.info("{} elapsed: {}, duration: {}", new Object[] {
							Thread.currentThread().getName(), Durations.elapsedSince(start), duration });
					int page1 = random.nextInt(counts.length);
					int page2 = random.nextInt(counts.length);
					int count = 0;
					while (page2 == page1 && count < 100)
					{
						page2 = random.nextInt(counts.length);
						count++;
					}
					if (page2 == page1)
					{
						throw new RuntimeException("orly?");
					}
					try
					{
						sync.lockPage(page1);
						sync.lockPage(page2);
						// have locks, increment the count

						counts[page1].incrementAndGet();
						counts[page2].incrementAndGet();
						hits.incrementAndGet();

						// hold the lock for some time
						try
						{
							Thread.sleep(50);
						}
						catch (InterruptedException e)
						{
							error[0] = "Worker :" + Thread.currentThread().getName() +
								" interrupted";
						}

						// decrement the counts
						counts[page1].decrementAndGet();
						counts[page2].decrementAndGet();

						// release lock
					}
					catch (CouldNotLockPageException e)
					{
						// ignore
					}
					finally
					{
						sync.unlockAllPages();
					}
				}
			}
		}

		class Monitor extends Thread
		{
			volatile boolean stop = false;

			@Override
			public void run()
			{
				while (!stop && error[0] == null)
				{
					for (int i = 0; i < counts.length; i++)
					{
						int count = counts[i].get();

						if (count < 0 || count > 1)
						{
							error[0] = "Detected count of: " + count + " for page: " + i;
							return;
						}
					}
					try
					{
						Thread.sleep(1);
					}
					catch (InterruptedException e)
					{
						error[0] = "Monitor thread interrupted";
					}
				}
			}
		}

		Monitor monitor = new Monitor();
		monitor.setName("monitor");
		monitor.start();

		Worker[] bots = new Worker[workers];
		for (int i = 0; i < bots.length; i++)
		{
			bots[i] = new Worker();
			bots[i].setName("worker " + i);
			bots[i].start();
		}

		for (Worker bot : bots)
		{
			bot.join();
		}

		monitor.stop = true;
		monitor.join();

		assertNull(error[0], error[0]);
		assertTrue(hits.get() >= counts.length);
	}

	/**
	 * @throws Exception
	 */
	@Test
	void testConcurrency() throws Exception
	{
		runContentionTest(20, 10, Duration.ofSeconds(10));
	}

	/**
	 * @throws Exception
	 */
	@Test
	void testContention() throws Exception
	{
		runContentionTest(10, 20, Duration.ofSeconds(10));
	}

	/**
	 * @throws Exception
	 */
	@Test
	void testSerialization() throws Exception
	{
		// a simple worker that acquires a lock on page 5
		class Locker extends Thread
		{
			private final PageAccessSynchronizer sync;

			Locker(PageAccessSynchronizer sync)
			{
				this.sync = sync;
			}

			@Override
			public void run()
			{
				sync.lockPage(5);
			}
		}

		// set up a synchronizer and lock page 5 with locker1
		final Duration timeout = Duration.ofSeconds(30);
		final PageAccessSynchronizer sync = new PageAccessSynchronizer(timeout);
		Locker locker1 = new Locker(sync);

		final long start = System.currentTimeMillis();
		locker1.run();

		// make sure we can serialize the synchronizer

		final PageAccessSynchronizer sync2 = WicketObjects.cloneObject(sync);
		assertTrue(sync != sync2);

		// make sure the clone does not retain locks by attempting to lock page locked by locker1 in
		// locker2
		Locker locker2 = new Locker(sync2);
		locker2.run();
		assertTrue(Duration.ofMillis(System.currentTimeMillis() - start).compareTo(timeout) < 0);
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-4009
	 */
	@Test
	void unlockIfNoSuchPage()
	{
		PageAccessSynchronizer synchronizer = new PageAccessSynchronizer(Duration.ofSeconds(2));
		IPageManager pageManager = new MockPageManager();
		IPageManager synchronizedPageManager = synchronizer.adapt(pageManager);
		synchronizedPageManager.getPage(0);
		ConcurrentMap<Integer, PageLock> locks = synchronizer.getLocks().get();
		PageLock pageLock = locks.get(Integer.valueOf(0));
		assertNull(pageLock);

		int pageId = 1;
		IManageablePage page = new MockPage(pageId);
		synchronizedPageManager.addPage(page);
		synchronizedPageManager.getPage(pageId);
		PageLock pageLock2 = locks.get(Integer.valueOf(pageId));
		assertNotNull(pageLock2);
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-5316
	 * 
	 * @throws Exception
	 */
	@Test
	void failToReleaseUnderLoad() throws Exception
	{
		final Duration duration = Duration.ofSeconds(20); /* seconds */
		final ConcurrentLinkedQueue<Exception> errors = new ConcurrentLinkedQueue<Exception>();
		final long endTime = System.currentTimeMillis() + duration.toMillis();

		// set the synchronizer timeout one second longer than the test runs to prevent
		// starvation to become an issue
		final PageAccessSynchronizer sync = new PageAccessSynchronizer(
			duration.plus(Duration.ofSeconds(1)));

		final CountDownLatch latch = new CountDownLatch(100);
		for (int count = 0; count < 100; count++)
		{
			new Thread()
			{
				@Override
				public void run()
				{
					try
					{
						while (System.currentTimeMillis() < endTime)
						{
							try
							{
								logger.debug(Thread.currentThread().getName() + " locking");
								sync.lockPage(0);
								Thread.sleep(1);
								logger.debug(Thread.currentThread().getName() + " locked");
								sync.unlockAllPages();
								logger.debug(Thread.currentThread().getName() + " unlocked");
								Thread.sleep(5);
							}
							catch (InterruptedException e)
							{
								throw new RuntimeException(e);
							}
						}
					}
					catch (Exception e)
					{
						logger.error(e.getMessage(), e);
						errors.add(e);
					}
					finally
					{
						latch.countDown();
					}
				}
			}.start();
		}
		latch.await();
		if (!errors.isEmpty())
		{
			logger.error("Number of lock errors that occurred: {}", errors.size());
			throw errors.remove();
		}
	}
}
