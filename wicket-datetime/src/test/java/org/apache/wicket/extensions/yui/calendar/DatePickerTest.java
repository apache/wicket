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
import java.util.Calendar;
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
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
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
		DateTimeZone.setDefault(DateTimeZone.forTimeZone(defaultTz));

		super.tearDown();
	}

	/**
	 * 
	 * @throws Exception
	 */
	public void test1() throws Exception
	{
		log.error("=========== test1() =================");
		myTestExecution(DatesPage1.class, "DatesPage1_ExpectedResult.html");
	}

	/**
	 * @throws Exception
	 */
	public void test2() throws Exception
	{
		log.error("=========== test2() =================");
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
		log.error("=========== test3() =================");
		TimeZone tzClient = TimeZone.getTimeZone("America/Los_Angeles");
		TimeZone tzServer = TimeZone.getTimeZone("Europe/Berlin");

		TimeZone.setDefault(tzServer);
		DateTimeZone.setDefault(DateTimeZone.forTimeZone(tzServer));

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
	 * Tests joda & jvm default time zone handling
	 */
	public void testJodaTimeDefaultTimeZone()
	{
		TimeZone origJvmDef = TimeZone.getDefault();
		DateTimeZone origJodaDef = DateTimeZone.getDefault();

		// lets find a timezone different from current default
		String newId = null;
		for (String id : TimeZone.getAvailableIDs())
		{
			if (!id.equals(origJvmDef.getID()))
			{
				newId = id;
				break;
			}
		}

		assertNotNull(newId);

		TimeZone.setDefault(TimeZone.getTimeZone(newId));

		TimeZone newJvmDef = TimeZone.getDefault();
		DateTimeZone newJodaDef = DateTimeZone.getDefault();

		// if this fails we are under security manager
		// and we have no right to set default timezone
		assertNotSame(origJvmDef, newJvmDef);

		// this should be true because joda caches the
		// default timezone and even for the first
		// lookup it uses a System property if possible
		// for more info see org.joda.time.DateTimeZone.getDefault()
		assertSame(origJodaDef, newJodaDef);
	}

	/**
	 * 
	 * @throws ParseException
	 */
	public void testDates1() throws ParseException
	{
		log.error("=========== testDates1() =================");
		TimeZone tzClient = TimeZone.getTimeZone("America/Los_Angeles");
		TimeZone tzServer = TimeZone.getTimeZone("Europe/Berlin");

		TimeZone.setDefault(tzServer);
		DateTimeZone.setDefault(DateTimeZone.forTimeZone(tzServer));
		Locale.setDefault(Locale.GERMAN);

// Date orig = convertDate("06.11.2010", null, null, null, false, tzClient);
// Date origJoda = convertDateJoda("06.11.2010", null, null, null, false, tzClient);
		Date orig3 = convertDateNew("06.11.2010", null, null, null, false, tzClient);

		MutableDateTime dt = new MutableDateTime(DateTimeZone.forTimeZone(tzClient));
		dt.setDateTime(2010, 11, 06, 0, 0, 0, 0);
		Date date = new Date(dt.getMillis());

		log.error(/* "actual: " + orig.getTime() + "; joda: " + origJoda.getTime() + */"; origNew: " +
			orig3.getTime() + "; expected: " + date.getTime());
		log.error(/* "actual: " + orig + "; joda: " + origJoda + */"; origNew: " + orig3 +
			"; expected: " + date);
		assertEquals(date.getTime(), orig3.getTime());
// assertEquals(date.getTime(), orig.getTime());
// assertEquals(origJoda.getTime(), orig.getTime());
	}

	/**
	 * 
	 * @throws ParseException
	 */
	public void testDates2() throws ParseException
	{
		log.error("=========== testDates2() =================");
		TimeZone tzClient = TimeZone.getTimeZone("America/Los_Angeles");
		TimeZone tzServer = TimeZone.getTimeZone("Europe/Berlin");

		TimeZone.setDefault(tzServer);
		DateTimeZone.setDefault(DateTimeZone.forTimeZone(tzServer));
		Locale.setDefault(Locale.GERMAN);

// Date orig = convertDate("06.11.2010", 0, 0, AM_PM.AM, false, tzClient);
// Date origJoda = convertDateJoda("06.11.2010", 0, 0, AM_PM.AM, false, tzClient);
		Date orig3 = convertDateNew("06.11.2010", 0, 0, AM_PM.AM, false, tzClient);

		MutableDateTime dt = new MutableDateTime(DateTimeZone.forTimeZone(tzClient));
		dt.setDateTime(2010, 11, 06, 0, 0, 0, 0);
		Date date = new Date(dt.getMillis());

		log.error(/* "actual: " + orig.getTime() + "; joda: " + origJoda.getTime() + */"; origNew: " +
			orig3.getTime() + "; expected: " + date.getTime());
		log.error(/* "actual: " + orig + "; joda: " + origJoda + */"; origNew: " + orig3 +
			"; expected: " + date);
		assertEquals(date.getTime(), orig3.getTime());
// assertEquals(date.getTime(), orig.getTime());
// assertEquals(origJoda.getTime(), orig.getTime());
	}

	/**
	 * 
	 * @throws ParseException
	 */
	public void testDates3() throws ParseException
	{
		log.error("=========== testDates3() =================");
		TimeZone tzClient = TimeZone.getTimeZone("America/Los_Angeles");
		TimeZone tzServer = TimeZone.getTimeZone("Europe/Berlin");

		TimeZone.setDefault(tzServer);
		DateTimeZone.setDefault(DateTimeZone.forTimeZone(tzServer));
		Locale.setDefault(Locale.GERMAN);

// Date orig = convertDate("06.11.2010", 12, 0, null, false, tzClient);
// Date origJoda = convertDateJoda("06.11.2010", 12, 0, null, false, tzClient);
		Date orig3 = convertDateNew("06.11.2010", 12, 0, null, false, tzClient);

		MutableDateTime dt = new MutableDateTime(DateTimeZone.forTimeZone(tzClient));
		dt.setDateTime(2010, 11, 06, 12, 0, 0, 0);
		Date date = new Date(dt.getMillis());

		log.error(/* "actual: " + orig.getTime() + "; joda: " + origJoda.getTime() + */"; origNew: " +
			orig3.getTime() + "; expected: " + date.getTime());
		log.error(/* "actual: " + orig + "; joda: " + origJoda + */"; origNew: " + orig3 +
			"; expected: " + date);
		assertEquals(date.getTime(), orig3.getTime());
// assertEquals(date.getTime(), orig.getTime());
// assertEquals(origJoda.getTime(), orig.getTime());
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
		log.error(">>> convertDate()");
		Date dateFieldInput = (dateStr != null ? DateFormat.getDateInstance().parse(dateStr) : null);

		// Default to today, if date entry was invisible
		final MutableDateTime date;
		if (dateFieldInput != null)
		{
			log.error("1. dateFieldInput: " + dateFieldInput.getTime() + "  " + dateFieldInput);
			date = new MutableDateTime(dateFieldInput);
		}
		else
		{
			log.error("1. dateFieldInput: null");
			// Current date
			date = new MutableDateTime();
		}
		log.error("2. mutable date: " + date.getMillis() + "  " + date);

		// always set secs to 0
		date.setSecondOfMinute(0);
		log.error("3. secs = 0: " + date.getMillis() + "  " + date);

		// The AM/PM field
		if (use12HourFormat)
		{
			date.set(DateTimeFieldType.halfdayOfDay(), amOrPm == AM_PM.PM ? 1 : 0);
		}
		log.error("4. AM/PM: " + date.getMillis() + "  " + date);

		// The hours
		if (hours == null)
		{
			date.setHourOfDay(0);
		}
		else
		{
			date.set(DateTimeFieldType.hourOfDay(), hours % (use12HourFormat ? 12 : 24));
		}
		log.error("5. hours: " + date.getMillis() + "  " + date);

		// The minutes
		if (minutes == null)
		{
			date.setMinuteOfHour(0);
		}
		else
		{
			date.setMinuteOfHour(minutes);
		}
		log.error("6. minutes: " + date.getMillis() + "  " + date);

		// Use the client timezone to properly calculate the millisecs
		if (tzClient != null)
		{
			date.setZoneRetainFields(DateTimeZone.forTimeZone(tzClient));
			log.error("7. zone: " + date.getMillis() + "  " + date);
		}

		Date rtn = new Date(date.getMillis());
		log.error("8. final date: " + rtn.getTime() + "  " + rtn);
		return rtn;
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
	public Date convertDateNew(final String dateStr, final Integer hours, final Integer minutes,
		final AM_PM amOrPm, final boolean use12HourFormat, final TimeZone tzClient)
		throws ParseException
	{
		log.error(">>> convertDateNew()");
		// This is what I get from field.getConvertedInput()
		Date dateFieldInput = (dateStr != null ? DateFormat.getDateInstance().parse(dateStr) : null);

		// Default with "now"
		if (dateFieldInput == null)
		{
			dateFieldInput = new Date();
		}

		// Get year, month and day ignoring any timezone of the Date object
		Calendar cal = Calendar.getInstance();
		cal.setTime(dateFieldInput);
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;
		int day = cal.get(Calendar.DAY_OF_MONTH);
		int iHours = (hours == null ? 0 : hours % 24);
		int iMins = (minutes == null ? 0 : minutes);

		// Use the input to create a date object with proper timezone
		MutableDateTime date = new MutableDateTime(year, month, day, iHours, iMins, 0, 0,
			DateTimeZone.forTimeZone(tzClient));

		// Use the input to create a date object. Ignore the timezone provided by dateFieldInput and
		// use tzClient instead. No re-calculation will happen. It should be the same as above.
// MutableDateTime date = new MutableDateTime(dateFieldInput,
// DateTimeZone.forTimeZone(tzClient));
		log.error("1. date: " + date.getMillis() + "  " + date);

		// Adjust for halfday if needed
		int halfday = 0;
		if (use12HourFormat)
		{
			halfday = (amOrPm == AM_PM.PM ? 1 : 0);
			date.set(DateTimeFieldType.halfdayOfDay(), halfday);
			date.set(DateTimeFieldType.hourOfDay(), hours % 12);
		}
		log.error("2. halfday adjustments: " + date.getMillis() + "  " + date);

		Date rtn = new Date(date.getMillis());
		log.error("3. final date: " + rtn.getTime() + "  " + rtn);
		return rtn;
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
	public Date convertDateJoda(final String dateStr, final Integer hours, final Integer minutes,
		final AM_PM amOrPm, final boolean use12HourFormat, final TimeZone tzClient)
		throws ParseException
	{
		log.error(">>> convertDateJoda()");

		DateTimeFormatter fmt = DateTimeFormat.shortDate();
		// fmt.withZone(timeZone).parseDateTime("10/1/06 5:00 AM");
		MutableDateTime date = (dateStr != null ? fmt.parseMutableDateTime(dateStr)
			: new MutableDateTime());

		log.error("1. mutable date: " + date.getMillis() + "  " + date);

		// always set secs to 0
		date.setSecondOfMinute(0);
		log.error("2. secs = 0: " + date.getMillis() + "  " + date);

		// The AM/PM field
		if (use12HourFormat)
		{
			date.set(DateTimeFieldType.halfdayOfDay(), amOrPm == AM_PM.PM ? 1 : 0);
		}
		log.error("3. AM/PM: " + date.getMillis() + "  " + date);

		// The hours
		if (hours == null)
		{
			date.setHourOfDay(0);
		}
		else
		{
			date.set(DateTimeFieldType.hourOfDay(), hours % (use12HourFormat ? 12 : 24));
		}
		log.error("4. hours: " + date.getMillis() + "  " + date);

		// The minutes
		if (minutes == null)
		{
			date.setMinuteOfHour(0);
		}
		else
		{
			date.setMinuteOfHour(minutes);
		}
		log.error("5. minutes: " + date.getMillis() + "  " + date);

		// Use the client timezone to properly calculate the millisecs
		if (tzClient != null)
		{
			date.setZoneRetainFields(DateTimeZone.forTimeZone(tzClient));
		}
		log.error("6. timezone: " + date.getMillis() + "  " + date);

		Date rtn = new Date(date.getMillis());
		log.error("7. final date: " + rtn.getTime() + "  " + rtn);
		return rtn;
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