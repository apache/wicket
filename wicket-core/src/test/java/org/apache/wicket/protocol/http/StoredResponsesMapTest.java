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
package org.apache.wicket.protocol.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.wicket.util.WicketTestTag;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * @see <a href="https://issues.apache.org/jira/browse/WICKET-3209">WICKET-3209</a>
 */
@Tag(WicketTestTag.SLOW)
class StoredResponsesMapTest
{
	/**
	 * Verifies that {@link StoredResponsesMap} will expire the oldest entry if it is older than 2
	 * seconds
	 * 
	 * @throws Exception
	 */
	@Test
	void entriesLife2Seconds() throws Exception
	{
		StoredResponsesMap map = new StoredResponsesMap(1000, Duration.ofSeconds(2));
		assertEquals(0, map.size());
		map.put("1", new BufferedWebResponse(null));
		assertEquals(1, map.size());
		TimeUnit.SECONDS.sleep(3);
		map.put("2", new BufferedWebResponse(null));
		assertEquals(1, map.size());
		assertTrue(map.containsKey("2"));
	}

	/**
	 * Verifies that getting a value which is expired will return <code>null</code>.
	 * 
	 * @throws Exception
	 */
	@Test
	void getExpiredValue() throws Exception
	{
		Instant start = Instant.now();
		Duration timeout = Duration.ofMillis(50);
		StoredResponsesMap map = new StoredResponsesMap(1000, timeout);
		assertEquals(0, map.size());
		map.put("1", new BufferedWebResponse(null));
		assertEquals(1, map.size());
		TimeUnit.MILLISECONDS.sleep(timeout.toMillis() * 2); // sleep for twice longer than the timeout
		Duration elapsedTime = Duration.between(start, Instant.now());
		elapsedTime = elapsedTime.truncatedTo(ChronoUnit.MILLIS);
		
		assertTrue(elapsedTime.compareTo(timeout) > 0, "The timeout has passed.");
		Object value = map.get("1");
		assertNull(value);
	}

	/**
	 * Verifies that {@link StoredResponsesMap} can have only {@link BufferedWebResponse} values
	 */
	@Test
	void cannotPutArbitraryValue()
	{
		StoredResponsesMap map = new StoredResponsesMap(1000, Duration.ofDays(1));
		assertThrows(IllegalArgumentException.class, () -> {
			map.put("1", new Object());
		});

	}

	/**
	 * <a href="https://issues.apache.org/jira/browse/WICKET-3736">WICKET-3736</a>
	 * 
	 * Tries to simulate heavy load on the {@link StoredResponsesMap} by putting many entries and
	 * removing randomly them.
	 *
	 * @throws InterruptedException
	 */
	@Test
	void heavyLoad() throws InterruptedException
	{
		final int numberOfThreads = 100;
		final int iterations = 1000;
		final CountDownLatch startLatch = new CountDownLatch(numberOfThreads);
		final CountDownLatch endLatch = new CountDownLatch(numberOfThreads);
		final SecureRandom rnd = new SecureRandom();
		final StoredResponsesMap map = new StoredResponsesMap(1000, Duration.ofSeconds(60));
		final List<String> keys = new CopyOnWriteArrayList<String>();

		final Runnable r = new Runnable()
		{
			@Override
			public void run()
			{
				startLatch.countDown();
				try
				{
					// wait all threads before starting the test
					startLatch.await();
				}
				catch (InterruptedException e)
				{
					throw new RuntimeException(e);
				}

				for (int i = 0; i < iterations; i++)
				{
					String key = "abc" + (rnd.nextDouble() * iterations);
					keys.add(key);
					map.put(key, new BufferedWebResponse(null));

					int randomMax = keys.size() - 1;
					int toRemove = randomMax == 0 ? 0 : rnd.nextInt(randomMax);
					String key2 = keys.get(toRemove);
					map.remove(key2);
				}
				endLatch.countDown();
			}
		};

		for (int t = 0; t < numberOfThreads; t++)
		{
			new Thread(r).start();
		}
		endLatch.await();
	}
}
