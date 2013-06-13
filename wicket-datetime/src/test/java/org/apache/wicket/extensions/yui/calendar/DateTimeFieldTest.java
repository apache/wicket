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
package org.apache.wicket.extensions.yui.calendar;

import java.util.TimeZone;

import org.apache.wicket.WicketTestCase;
import org.apache.wicket.model.Model;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for DateTimeField
 */
public class DateTimeFieldTest extends WicketTestCase
{
	/**
	 * https://issues.apache.org/jira/browse/WICKET-5204
	 */
	@Test
	public void testTimeZones()
	{
		DateTimeZone defaultTimeZone = DateTimeZone.getDefault();

		try
		{
			// The server is using UTC as it's default timezone
			DateTimeZone.setDefault(DateTimeZone.forID("UTC"));

			final String clientTimezone = "America/Toronto";

			DateTime jan01_10am = new DateTime(2013, 01, 01, 10, 0, 0,
				DateTimeZone.forID(clientTimezone));

			DateTimeField dateTimeField = new DateTimeField("foo", Model.of(jan01_10am.toDate()))
			{
				@Override
				protected TimeZone getClientTimeZone()
				{
					return TimeZone.getTimeZone(clientTimezone);
				}
			};

			tester.startComponentInPage(dateTimeField);

			Assert.assertEquals("The hour of day is incorrect!", jan01_10am.getHourOfDay(),
				dateTimeField.getHours().intValue());
		}
		finally
		{
			DateTimeZone.setDefault(defaultTimeZone);
		}
	}

}
