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
package org.apache.wicket.util.time;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.text.NumberFormat;
import java.util.Locale;

import org.apache.wicket.util.string.StringValueConversionException;
import org.junit.Test;

/**
 * Test cases for this object
 * 
 * @author Jonathan Locke
 */
public final class DurationTest
{
	/**
	 * @throws StringValueConversionException
	 */
	@Test
	public void values() throws StringValueConversionException
	{
		assertEquals(Duration.milliseconds(3000), Duration.seconds(3));
		assertEquals(Duration.seconds(120), Duration.minutes(2));
		assertEquals(Duration.minutes(1440), Duration.hours(24));
		assertEquals(Duration.hours(48), Duration.days(2));
		assertEquals(Duration.minutes(90), Duration.valueOf("90 minutes"));
		assertEquals(Duration.days(9), Duration.valueOf("9 days"));
		assertEquals(Duration.hours(1), Duration.valueOf("1 hour"));
		assertTrue(9 == Duration.days(9).days());
		assertTrue(11 == Duration.hours(11).hours());
		assertTrue(21 == Duration.minutes(21).minutes());
		assertTrue(51 == Duration.seconds(51).seconds());
	}

	/** */
	@Test
	public void operations()
	{
		assertTrue(Duration.milliseconds(3001).greaterThan(Duration.seconds(3)));
		assertTrue(Duration.milliseconds(3001).greaterThanOrEqual(Duration.seconds(3)));
		assertTrue(Duration.milliseconds(3000).greaterThanOrEqual(Duration.seconds(3)));
		assertTrue(Duration.milliseconds(2999).lessThan(Duration.seconds(3)));
		assertTrue(Duration.milliseconds(2999).lessThanOrEqual(Duration.seconds(3)));
		assertTrue(Duration.milliseconds(3000).lessThanOrEqual(Duration.seconds(3)));
		assertEquals(-1, Duration.milliseconds(2999).compareTo(Duration.seconds(3)));
		assertEquals(1, Duration.milliseconds(3001).compareTo(Duration.seconds(3)));
		assertEquals(0, Duration.milliseconds(3000).compareTo(Duration.seconds(3)));
		assertEquals(Duration.minutes(10), Duration.minutes(4).add(Duration.minutes(6)));
		assertEquals(Duration.ONE_HOUR, Duration.minutes(90).subtract(Duration.minutes(30)));

		String value = NumberFormat.getNumberInstance().format(1.5);

		assertEquals(value + " minutes", Duration.seconds(90).toString());
		assertEquals("12 hours", Duration.days(0.5).toString());
	}

	/** */
	@Test
	public void testSleep()
	{
		assertTrue(Duration.seconds(0.5).lessThan(Duration.benchmark(new Runnable()
		{
			public void run()
			{
				Duration.seconds(1.5).sleep();
			}
		})));

		assertTrue(Duration.seconds(1).greaterThan(Duration.benchmark(new Runnable()
		{
			public void run()
			{
				Duration.hours(-1).sleep();
			}
		})));
	}

	/**
	 * @throws StringValueConversionException
	 */
	@Test
	public void locale() throws StringValueConversionException
	{
		assertEquals(Duration.minutes(90), Duration.valueOf("90 minutes"));
		assertEquals(Duration.hours(1.5), Duration.valueOf("1.5 hour", Locale.US));
		assertEquals(Duration.hours(1.5), Duration.valueOf("1,5 hour", Locale.GERMAN));
		assertEquals("1.5 hours", Duration.hours(1.5).toString(Locale.US));
		assertEquals("1,5 hours", Duration.hours(1.5).toString(Locale.GERMAN));
	}


	/**
	 * Check if toString(Locale locale) respects the appropriate value and format (in English).
	 */
	@Test
	public void toStringValues()
	{
		assertEquals("1 day", Duration.days(1).toString(Locale.ENGLISH));
		assertEquals("5 days", Duration.days(5).toString(Locale.ENGLISH));

		assertEquals("1 hour", Duration.hours(1).toString(Locale.ENGLISH));
		assertEquals("23 hours", Duration.hours(23).toString(Locale.ENGLISH));

		assertEquals("1 minute", Duration.minutes(1).toString(Locale.ENGLISH));
		assertEquals("2 minutes", Duration.minutes(2).toString(Locale.ENGLISH));

		assertEquals("1 second", Duration.seconds(1).toString(Locale.ENGLISH));
		assertEquals("2 seconds", Duration.seconds(2).toString(Locale.ENGLISH));

		assertEquals("1 millisecond", Duration.milliseconds(1).toString(Locale.ENGLISH));
		assertEquals("955 milliseconds", Duration.milliseconds(955).toString(Locale.ENGLISH));
	}
}
