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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import org.apache.wicket.util.time.Duration;
import org.junit.Test;

/**
 * @see <a href="https://issues.apache.org/jira/browse/WICKET-3209">WICKET-3209</a>
 */
public class StoredResponsesMapTest
{
	/**
	 * Verifies that {@link StoredResponsesMap} will expire the oldest entry if it is older than 2
	 * seconds
	 * 
	 * @throws Exception
	 */
	@Test
	public void entriesLife2Seconds() throws Exception
	{
		StoredResponsesMap map = new StoredResponsesMap(1000, Duration.seconds(2));
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
	public void getExpiredValue() throws Exception
	{
		StoredResponsesMap map = new StoredResponsesMap(1000, Duration.milliseconds(50));
		assertEquals(0, map.size());
		map.put("1", new BufferedWebResponse(null));
		assertEquals(1, map.size());
		TimeUnit.MILLISECONDS.sleep(51);
		Object value = map.get("1");
		assertNull(value);
	}

	/**
	 * Verifies that {@link StoredResponsesMap} can have only {@link BufferedWebResponse} values
	 */
	@Test(expected = IllegalArgumentException.class)
	public void cannotPutArbitraryValue()
	{
		StoredResponsesMap map = new StoredResponsesMap(1000, Duration.days(1));
		map.put("1", new Object());
	}
}
