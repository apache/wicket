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

import java.util.TimeZone;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for ClientProperties that failed on Mac OS X Java platform.
 * 
 * @author Martijn Dashorst
 */
public class ClientPropertiesTest extends Assert
{
	/**
	 * Tests GMT-2:00
	 */
	@Test
	public void timezoneMinus2()
	{
		String utc = "-2.0";
		ClientProperties props = new ClientProperties();
		props.setUtcOffset(utc);

		assertEquals(TimeZone.getTimeZone("GMT-2:00"), props.getTimeZone());
	}

	/**
	 * Tests GMT+2:00
	 */
	@Test
	public void timezonePlus2()
	{
		String utc = "+2.0";
		ClientProperties props = new ClientProperties();
		props.setUtcOffset(utc);

		assertEquals(TimeZone.getTimeZone("GMT+2:00"), props.getTimeZone());
	}

	/**
	 * Tests GMT+11:00
	 */
	@Test
	public void timezonePlus10()
	{
		String utc = "+11.0";
		ClientProperties props = new ClientProperties();
		props.setUtcOffset(utc);

		assertEquals(TimeZone.getTimeZone("GMT+11:00"), props.getTimeZone());
	}

	/**
	 * Tests GMT+2:30
	 */
	@Test
	public void timezonePlus2andAHalf()
	{
		String utc = "+2.5";
		ClientProperties props = new ClientProperties();
		props.setUtcOffset(utc);

		assertEquals(TimeZone.getTimeZone("GMT+2:30"), props.getTimeZone());
	}

	/**
	 * Tests GMT-2:30
	 */
	@Test
	public void timezoneMinus2andAHalf()
	{
		String utc = "-2.5";
		ClientProperties props = new ClientProperties();
		props.setUtcOffset(utc);

		assertEquals(TimeZone.getTimeZone("GMT-2:30"), props.getTimeZone());
	}

	/**
	 * Tests GMT+3:00
	 */
	@Test
	public void timezonePlus3()
	{
		String utc = "3";
		ClientProperties props = new ClientProperties();
		props.setUtcOffset(utc);

		assertEquals(TimeZone.getTimeZone("GMT+3:00"), props.getTimeZone());
	}

	/**
	 * Tests GMT-3:00
	 */
	@Test
	public void timezoneMinus3()
	{
		String utc = "-3";
		ClientProperties props = new ClientProperties();
		props.setUtcOffset(utc);

		assertEquals(TimeZone.getTimeZone("GMT-3:00"), props.getTimeZone());
	}

	/**
	 * WICKET-5396.
	 */
	@Test
	public void integerToString()
	{
		ClientProperties props = new ClientProperties();

		assertFalse(props.toString().contains("browserHeight"));

		props.setBrowserHeight(666);

		assertTrue(props.toString().contains("browserHeight=666"));
	}
}
