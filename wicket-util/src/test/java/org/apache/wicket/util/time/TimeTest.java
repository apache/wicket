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


import java.text.ParseException;
import java.util.Calendar;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test cases for this object
 * 
 * @author Jonathan Locke
 */
public final class TimeTest extends Assert
{
	/**
	 * 
	 * @throws ParseException
	 */
	@Test
	public void test() throws ParseException
	{
		Time.now();

		// TODO Post 1.2: Bug: Eelco mentioned this test fails on his computer
		// from time to time. I have seen this and it's very weird -- Jonathan
		// Assert.assertTrue(Time.now().after(start) || Time.now().equals(start));

		final Time birthday = Time.parseDate("1966.06.01");

		assertEquals(1966, birthday.getYear());
		assertEquals(Calendar.JUNE, birthday.getMonth());
		assertEquals(1, birthday.getDayOfMonth());

		final String y2k = "2000.01.01-12.00am";

		assertEquals(y2k, Time.valueOf(y2k).toString());
	}

	/**
	 */
	@Test
	public void hour() throws Exception
	{

		Time time = Time.valueOf("2000.10.30-9.30PM");

		assertEquals(21, time.getHour());
	}
}