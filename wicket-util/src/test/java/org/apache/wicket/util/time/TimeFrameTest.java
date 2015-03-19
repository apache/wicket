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

import org.junit.Assert;
import org.junit.Test;

/**
 * @author v857829
 */
public class TimeFrameTest extends Assert
{

	private final TimeOfDay three = TimeOfDay.time(3, 0, TimeOfDay.PM);
	private final TimeOfDay four = TimeOfDay.time(4, 0, TimeOfDay.PM);
	private final TimeOfDay five = TimeOfDay.time(5, 0, TimeOfDay.PM);

	/**
	 * Test method for
	 * {@link org.apache.wicket.util.time.TimeFrame#eachDay(org.apache.wicket.util.time.TimeOfDay, org.apache.wicket.util.time.TimeOfDay)}
	 * .
	 */
	@Test
	public void eachDay()
	{
		ITimeFrameSource test = TimeFrame.eachDay(three, five);
		Assert.assertTrue(test.getTimeFrame().contains(Time.valueOf(four)));
	}

	/**
	 * Test method for
	 * {@link org.apache.wicket.util.time.TimeFrame#valueOf(org.apache.wicket.util.time.Time, org.apache.wicket.util.time.Duration)}
	 * .
	 */
	@Test
	public void valueOfTimeDuration()
	{
		TimeFrame test = TimeFrame.valueOf(Time.valueOf(three), Duration.minutes(60));
		Assert.assertEquals(test.getStart(), Time.valueOf(three));
		Assert.assertEquals(test.getEnd(), Time.valueOf(four));
	}

	/**
	 * Test method for
	 * {@link org.apache.wicket.util.time.TimeFrame#contains(org.apache.wicket.util.time.Time)}.
	 */
	@Test
	public void contains()
	{
		TimeFrame test = TimeFrame.valueOf(Time.valueOf(three), Duration.minutes(70));
		Assert.assertTrue(test.contains(Time.valueOf(four)));
	}

	/**
	 * Test method for
	 * {@link org.apache.wicket.util.time.TimeFrame#overlaps(org.apache.wicket.util.time.TimeFrame)}
	 * .
	 */
	@Test
	public void overlaps()
	{
		TimeFrame test = TimeFrame.valueOf(Time.valueOf(three), Duration.minutes(70));
		TimeFrame test2 = TimeFrame.valueOf(Time.valueOf(four), Duration.minutes(50));
		TimeFrame test3 = TimeFrame.valueOf(Time.valueOf(four), Time.valueOf(five));
		TimeFrame test4 = TimeFrame.valueOf(Time.valueOf(three), Duration.minutes(20));
		Assert.assertTrue(test.overlaps(test2));
		Assert.assertTrue(test3.overlaps(test2));
		Assert.assertTrue(test.overlaps(test3));
		Assert.assertFalse(test4.overlaps(test3));
	}

	/**
	 * Test method for {@link org.apache.wicket.util.time.TimeFrame#equals(java.lang.Object)}.
	 */
	@Test
	public void testEquals()
	{
		TimeFrame test = TimeFrame.valueOf(Time.valueOf(three), Duration.minutes(60));
		TimeFrame test2 = TimeFrame.valueOf(Time.valueOf(three), Time.valueOf(four));
		TimeFrame test3 = TimeFrame.valueOf(Time.valueOf(three), Duration.minutes(59));
		Assert.assertEquals(test, test2);
		Assert.assertNotSame(test2, test3);
	}

}
