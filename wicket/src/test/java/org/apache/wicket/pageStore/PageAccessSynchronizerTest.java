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

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.wicket.util.time.Duration;
import org.apache.wicket.util.time.Time;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.rules.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PageAccessSynchronizerTest
{
	private static final Logger logger = LoggerFactory.getLogger(PageAccessSynchronizer.class);

	@Rule
	public MethodRule globalTimeout = new Timeout((int)Duration.seconds(30).getMilliseconds());

	@Test
	public void testReentrant() throws Exception
	{
		final PageAccessSynchronizer sync = new PageAccessSynchronizer(Duration.seconds(5));
		final Duration hold = Duration.seconds(1);
		sync.lockPage(0);
		sync.lockPage(0);
	}

	@Test
	public void testBlocking() throws Exception
	{
		final PageAccessSynchronizer sync = new PageAccessSynchronizer(Duration.seconds(5));
		final Duration hold = Duration.seconds(1);
		final Time t1locks[] = new Time[1];
		final Time t2locks[] = new Time[1];

		class T1 extends Thread
		{
			@Override
			public void run()
			{
				sync.lockPage(1);
				t1locks[0] = Time.now();
				hold.sleep();
				sync.unlockAllPages();
			}
		}

		class T2 extends Thread
		{
			@Override
			public void run()
			{
				sync.lockPage(1);
				t2locks[0] = Time.now();
				sync.unlockAllPages();
			}
		}

		T1 t1 = new T1();
		t1.setName("t1");
		T2 t2 = new T2();
		t2.setName("t2");
		t1.start();
		Duration.milliseconds(100).sleep();
		t2.start();

		t1.join();
		t2.join();

		assertTrue(!t2locks[0].before(t1locks[0].add(hold)));
	}

	public void runContentionTest(final int pages, final int workers, final Duration duration)
		throws Exception
	{
		final PageAccessSynchronizer sync = new PageAccessSynchronizer(Duration.seconds(1));

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
				Time start = Time.now();

				while (start.elapsedSince().lessThan(duration) && error[0] == null)
				{
					logger.info("{} elapsed: {}, duration: {}", new Object[] {
							Thread.currentThread().getName(), start.elapsedSince(), duration });
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

		for (int i = 0; i < bots.length; i++)
		{
			bots[i].join();
		}

		monitor.stop = true;
		monitor.join();

		assertNull(error[0], error[0]);
		assertTrue(hits.get() >= counts.length);
	}

	@Test
	public void testConcurrency() throws Exception
	{
		runContentionTest(20, 10, Duration.seconds(10));
	}

	@Test
	public void testContention() throws Exception
	{
		runContentionTest(10, 20, Duration.seconds(10));
	}
}
