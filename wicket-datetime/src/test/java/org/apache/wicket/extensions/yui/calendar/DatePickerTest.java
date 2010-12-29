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

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.wicket.Page;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.extensions.yui.calendar.DateTimeField.AM_PM;
import org.apache.wicket.protocol.http.request.WebClientInfo;
import org.apache.wicket.util.tester.DiffUtil;
import org.apache.wicket.util.tester.FormTester;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DateTimeZone;
import org.joda.time.MutableDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class DatePickerTest extends WicketTestCase
{
	/** log. */
	private static final Logger log = LoggerFactory.getLogger(DatePickerTest.class);

	private TimeZone defaultTz = TimeZone.getDefault();

	/**
	 * @see org.apache.wicket.WicketTestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception
	{
		TimeZone.setDefault(defaultTz);

		super.tearDown();
	}

	/**
	 * 
	 * @throws Exception
	 */
	public void test1() throws Exception
	{
		myTestExecution(DatesPage1.class, "DatesPage1_ExpectedResult.html");
	}

	/**
	 * @throws Exception
	 */
	public void test2() throws Exception
	{
		Class<? extends Page> pageClass = DatesPage2.class;
		Date date = new GregorianCalendar(2010, 10, 06, 0, 0).getTime();
		tester.getSession().setLocale(Locale.GERMAN);
		tester.startPage(pageClass);
		tester.assertRenderedPage(pageClass);
		FormTester formTester = tester.newFormTester("form");
		formTester.setValue("dateTimeField:date", "06.11.2010");
		formTester.setValue("dateTimeField:hours", "00");
		formTester.setValue("dateTimeField:minutes", "00");
		formTester.setValue("dateField:date", "06.11.2010");
		formTester.submit();
		DatesPage2 page = (DatesPage2)tester.getLastRenderedPage();

		log.error("orig: " + date.getTime() + "; date: " + page.date.getTime() + "; dateTime: " +
			page.dateTime.getTime());
		log.error("orig: " + date + "; date: " + page.date + "; dateTime: " + page.dateTime);
		assertEquals(0, date.compareTo(page.dateTime));
		assertEquals(0, date.compareTo(page.date));
	}

	/**
	 * 
	 * @throws Exception
	 */
	public void test3() throws Exception
	{
		TimeZone tzClient = TimeZone.getTimeZone("America/Los_Angeles");
		TimeZone tzServer = TimeZone.getTimeZone("Europe/Berlin");

		TimeZone.setDefault(tzServer);

		Class<? extends Page> pageClass = DatesPage2.class;
		MutableDateTime dt = new MutableDateTime(DateTimeZone.forTimeZone(tzClient));
		dt.setDateTime(2010, 11, 06, 0, 0, 0, 0);
		Date date = new Date(dt.getMillis());

		WebClientInfo clientInfo = (WebClientInfo)tester.getSession().getClientInfo();
		clientInfo.getProperties().setTimeZone(tzClient);

		tester.getSession().setLocale(Locale.GERMAN);
		tester.startPage(pageClass);
		tester.assertRenderedPage(pageClass);
		FormTester formTester = tester.newFormTester("form");
		formTester.setValue("dateTimeField:date", "06.11.2010");
		formTester.setValue("dateTimeField:hours", "00");
		formTester.setValue("dateTimeField:minutes", "00");
		formTester.setValue("dateField:date", "06.11.2010");
		formTester.submit();

		DatesPage2 page = (DatesPage2)tester.getLastRenderedPage();

		log.error("orig: " + date.getTime() + "; date: " + page.date.getTime() + "; dateTime: " +
			page.dateTime.getTime());
		log.error("orig: " + date + "; date: " + page.date + "; dateTime: " + page.dateTime);
		assertEquals(0, date.compareTo(page.dateTime));
		assertEquals(0, date.compareTo(page.date));
	}

	/**
	 * 
	 * @throws ParseException
	 */
	public void testDates1() throws ParseException
	{
		TimeZone tzClient = TimeZone.getTimeZone("America/Los_Angeles");
		TimeZone tzServer = TimeZone.getTimeZone("Europe/Berlin");

		TimeZone.setDefault(tzServer);
		Locale.setDefault(Locale.GERMAN);

		Date orig = convertDate("06.11.2010", null, null, null, false, tzClient);

		MutableDateTime dt = new MutableDateTime(DateTimeZone.forTimeZone(tzClient));
		dt.setDateTime(2010, 11, 06, 0, 0, 0, 0);
		Date date = new Date(dt.getMillis());

		log.error("actual: " + orig.getTime() + "; expected: " + date.getTime());
		log.error("actual: " + orig + "; expected: " + date);
		assertEquals(date.getTime(), orig.getTime());
	}

	/**
	 * 
	 * @throws ParseException
	 */
	public void testDates2() throws ParseException
	{
		TimeZone tzClient = TimeZone.getTimeZone("America/Los_Angeles");
		TimeZone tzServer = TimeZone.getTimeZone("Europe/Berlin");

		TimeZone.setDefault(tzServer);
		Locale.setDefault(Locale.GERMAN);

		Date orig = convertDate("06.11.2010", 0, 0, AM_PM.AM, false, tzClient);

		MutableDateTime dt = new MutableDateTime(DateTimeZone.forTimeZone(tzClient));
		dt.setDateTime(2010, 11, 06, 0, 0, 0, 0);
		Date date = new Date(dt.getMillis());

		log.error("actual: " + orig.getTime() + "; expected: " + date.getTime());
		log.error("actual: " + orig + "; expected: " + date);
		assertEquals(date.getTime(), orig.getTime());
	}

	/**
	 * 
	 * @throws ParseException
	 */
	public void testDates3() throws ParseException
	{
		TimeZone tzClient = TimeZone.getTimeZone("America/Los_Angeles");
		TimeZone tzServer = TimeZone.getTimeZone("Europe/Berlin");

		TimeZone.setDefault(tzServer);
		Locale.setDefault(Locale.GERMAN);

		Date orig = convertDate("06.11.2010", 12, 0, null, false, tzClient);

		MutableDateTime dt = new MutableDateTime(DateTimeZone.forTimeZone(tzClient));
		dt.setDateTime(2010, 11, 06, 12, 0, 0, 0);
		Date date = new Date(dt.getMillis());

		log.error("actual: " + orig.getTime() + "; expected: " + date.getTime());
		log.error("actual: " + orig + "; expected: " + date);
		assertEquals(date.getTime(), orig.getTime());
	}

	/**
	 * Simulate what DateTimeField does
	 * 
	 * @param dateStr
	 * @param hours
	 * @param minutes
	 * @param amOrPm
	 * @param use12HourFormat
	 * @param tzClient
	 * @return Date
	 * @throws ParseException
	 */
	public Date convertDate(final String dateStr, final Integer hours, final Integer minutes,
		final AM_PM amOrPm, final boolean use12HourFormat, final TimeZone tzClient)
		throws ParseException
	{
		Date dateFieldInput = (dateStr != null ? DateFormat.getDateInstance().parse(dateStr) : null);

		// Default to today, if date entry was invisible
		final MutableDateTime date;
		if (dateFieldInput != null)
		{
			date = new MutableDateTime(dateFieldInput);
		}
		else
		{
			// Current date
			date = new MutableDateTime();
		}

		// always set secs to 0
		date.setSecondOfMinute(0);

		// The AM/PM field
		if (use12HourFormat)
		{
			date.set(DateTimeFieldType.halfdayOfDay(), amOrPm == AM_PM.PM ? 1 : 0);
		}

		// The hours
		if (hours == null)
		{
			date.setHourOfDay(0);
		}
		else
		{
			date.set(DateTimeFieldType.hourOfDay(), hours % (use12HourFormat ? 12 : 24));
		}

		// The minutes
		if (minutes == null)
		{
			date.setMinuteOfHour(0);
		}
		else
		{
			date.setMinuteOfHour(minutes);
		}

		// Use the client timezone to properly calculate the millisecs
		if (tzClient != null)
		{
			date.setZoneRetainFields(DateTimeZone.forTimeZone(tzClient));
		}

		return new Date(date.getMillis());
	}

	/**
	 * Use <code>-Dwicket.replace.expected.results=true</code> to automatically replace the expected
	 * output file.
	 * 
	 * @param <T>
	 * 
	 * @param pageClass
	 * @param filename
	 * @throws Exception
	 */
	protected <T extends Page> void myTestExecution(final Class<T> pageClass, final String filename)
		throws Exception
	{
		System.out.println("=== " + pageClass.getName() + " ===");

		tester.getSession().setLocale(Locale.GERMAN);
		tester.startPage(pageClass);
		tester.assertRenderedPage(pageClass);

		String document = tester.getLastResponseAsString();
		document = document.replaceAll("\\d\\d\\.\\d\\d\\.\\d\\d", "xx.xx.xx");
		document = document.replaceAll("\\d\\d/\\d\\d/\\d\\d\\d\\d", "xx.xx.xxxx");
		document = document.replaceAll("\\d\\d/\\d\\d\\d\\d", "xx.xxxx");

		DiffUtil.validatePage(document, pageClass, filename, true);
	}
}
