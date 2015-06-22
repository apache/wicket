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
import org.apache.wicket.datetime.StyleDateConverter;
import org.apache.wicket.extensions.yui.calendar.DateTimeField.AM_PM;
import org.apache.wicket.protocol.http.request.WebClientInfo;
import org.apache.wicket.util.tester.DiffUtil;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTestCase;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DateTimeZone;
import org.joda.time.MutableDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.After;
import org.junit.Test;
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
	 * @see org.apache.wicket.util.tester.WicketTestCase#tearDown()
	 */
	@Override
	@After
	public void commonAfter()
	{
		TimeZone.setDefault(defaultTz);
		DateTimeZone.setDefault(DateTimeZone.forTimeZone(defaultTz));

		super.commonAfter();
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void test1() throws Exception
	{
		log.debug("=========== test1() =================");
		myTestExecution(DatesPage1.class, "DatesPage1_ExpectedResult.html");
	}

	/**
	 * Tests conversion of input for DateTimeField and DateField.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testDateFieldInput() throws Exception
	{
		log.debug("=========== testDateFieldInput() =================");
		Class<? extends Page> pageClass = DatesPage2.class;
		Date date = new GregorianCalendar(2010, 10, 6, 0, 0).getTime();
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

		log.debug("orig: " + date.getTime() + "; date: " + page.date.getTime() + "; dateTime: " +
			page.dateTime.getTime());
		log.debug("orig: " + date + "; date: " + page.date + "; dateTime: " + page.dateTime);
		assertEquals(0, date.compareTo(page.dateTime));
		assertEquals(0, date.compareTo(page.date));
	}

	/**
	 * Tests conversion of input for DateTimeField and DateField when the client and server are in
	 * different time zones.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testDateFieldInputTimezone() throws Exception
	{
		log.debug("=========== testDateFieldInputTimezone() =================");
		TimeZone tzClient = TimeZone.getTimeZone("America/Los_Angeles");
		TimeZone tzServer = TimeZone.getTimeZone("Europe/Berlin");

		TimeZone.setDefault(tzServer);
		DateTimeZone.setDefault(DateTimeZone.forTimeZone(tzServer));

		Class<? extends Page> pageClass = DatesPage2.class;
		MutableDateTime dt = new MutableDateTime(DateTimeZone.forTimeZone(tzClient));
		dt.setDateTime(2010, 11, 6, 0, 0, 0, 0);
		Date date = new Date(dt.getMillis());

		WebClientInfo clientInfo = (WebClientInfo)tester.getSession().getClientInfo();
		clientInfo.getProperties().setTimeZone(tzClient);

		tester.getSession().setLocale(Locale.GERMANY);
		tester.startPage(pageClass);
		tester.assertRenderedPage(pageClass);
		FormTester formTester = tester.newFormTester("form");
		formTester.setValue("dateTimeField:date", "06.11.2010");
		formTester.setValue("dateTimeField:hours", "00");
		formTester.setValue("dateTimeField:minutes", "00");
		formTester.setValue("dateField:date", "06.11.2010");
		formTester.submit();

		DatesPage2 page = (DatesPage2)tester.getLastRenderedPage();

		log.debug("orig: " + date.getTime() + "; date: " + page.date.getTime() + "; dateTime: " +
			page.dateTime.getTime());
		log.debug("orig: " + date + "; date: " + page.date + "; dateTime: " + page.dateTime);
		assertEquals(0, date.compareTo(page.dateTime));
		assertEquals(0, date.compareTo(page.date));
	}

	/**
	 * Tests joda & jvm default time zone handling
	 */
	@Test
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
	 * Test date conversion with the server's time zone having a different current date than the
	 * client time zone.
	 * 
	 * @throws ParseException
	 */
	@Test
	public void testDifferentDateTimeZoneConversion() throws ParseException
	{
		log.debug("=========== testDifferentDateTimeZoneConversion() =================");
		TimeZone origJvmDef = TimeZone.getDefault();
		DateTimeZone origJodaDef = DateTimeZone.getDefault();
		TimeZone tzClient = TimeZone.getTimeZone("GMT+14");
		TimeZone tzServer = TimeZone.getTimeZone("GMT-12");

		TimeZone.setDefault(tzServer);
		DateTimeZone.setDefault(DateTimeZone.forTimeZone(tzServer));

		Class<? extends Page> pageClass = DatesPage2.class;
		MutableDateTime dt = new MutableDateTime(DateTimeZone.forTimeZone(tzClient));
		dt.setDateTime(2010, 11, 6, 0, 0, 0, 0);
		Date date = new Date(dt.getMillis());

		WebClientInfo clientInfo = (WebClientInfo)tester.getSession().getClientInfo();
		clientInfo.getProperties().setTimeZone(tzClient);

		tester.getSession().setLocale(Locale.GERMANY);
		tester.startPage(pageClass);
		tester.assertRenderedPage(pageClass);
		FormTester formTester = tester.newFormTester("form");
		formTester.setValue("dateTimeField:date", "06.11.2010");
		formTester.setValue("dateTimeField:hours", "00");
		formTester.setValue("dateTimeField:minutes", "00");
		formTester.setValue("dateField:date", "06.11.2010");
		formTester.submit();

		DatesPage2 page = (DatesPage2)tester.getLastRenderedPage();

		log.debug("orig: " + date.getTime() + "; date: " + page.date.getTime() + "; dateTime: " +
			page.dateTime.getTime());
		log.debug("orig: " + date + "; date: " + page.date + "; dateTime: " + page.dateTime);
		assertEquals(0, date.compareTo(page.dateTime));
		assertEquals(0, date.compareTo(page.date));

		TimeZone.setDefault(origJvmDef);
		DateTimeZone.setDefault(origJodaDef);
	}

	/**
	 * Test date conversion with the server's time zone having a different current date than the
	 * client time zone using a Locale with am/pm style time.
	 */
	@Test
	public void testDifferentDateTimeZoneConversionAMPM()
	{
		TimeZone origJvmDef = TimeZone.getDefault();
		DateTimeZone origJodaDef = DateTimeZone.getDefault();
		TimeZone tzClient = TimeZone.getTimeZone("GMT+14");
		TimeZone tzServer = TimeZone.getTimeZone("GMT-12");

		TimeZone.setDefault(tzServer);
		DateTimeZone.setDefault(DateTimeZone.forTimeZone(tzServer));

		Class<? extends Page> pageClass = DatesPage2.class;
		MutableDateTime dt = new MutableDateTime(DateTimeZone.forTimeZone(tzClient));
		dt.setDateTime(2010, 11, 6, 22, 0, 0, 0);
		Date date = new Date(dt.getMillis());

		WebClientInfo clientInfo = (WebClientInfo)tester.getSession().getClientInfo();
		clientInfo.getProperties().setTimeZone(tzClient);

		tester.getSession().setLocale(Locale.US);
		tester.startPage(pageClass);
		tester.assertRenderedPage(pageClass);
		FormTester formTester = tester.newFormTester("form");
		formTester.setValue("dateTimeField:date", "11/06/2010");
		formTester.setValue("dateTimeField:hours", "10");
		formTester.setValue("dateTimeField:minutes", "00");
		formTester.setValue("dateTimeField:amOrPmChoice", "1");
		formTester.submit();

		DatesPage2 page = (DatesPage2)tester.getLastRenderedPage();

		log.debug("orig: " + date.getTime() + "; dateTime: " + page.dateTime.getTime());
		log.debug("orig: " + date + "; dateTime: " + page.dateTime);
		assertEquals(0, date.compareTo(page.dateTime));

		TimeZone.setDefault(origJvmDef);
		DateTimeZone.setDefault(origJodaDef);
	}

	/**
	 * Test time conversion for TimeField. The day, month, year of the TimeField model should not be
	 * changed. The hours and minutes should be converted to the server's time zone based on the
	 * day, month and year of the Date model.
	 */
	@Test
	public void testTimeFieldDST()
	{
		TimeZone origJvmDef = TimeZone.getDefault();
		DateTimeZone origJodaDef = DateTimeZone.getDefault();
		TimeZone tzClient = TimeZone.getTimeZone("Canada/Eastern");
		TimeZone tzServer = TimeZone.getTimeZone("GMT");

		TimeZone.setDefault(tzServer);
		DateTimeZone.setDefault(DateTimeZone.forTimeZone(tzServer));
		WebClientInfo clientInfo = (WebClientInfo)tester.getSession().getClientInfo();
		clientInfo.getProperties().setTimeZone(tzClient);
		tester.getSession().setLocale(Locale.GERMAN);

		// Test with standard time (in client time zone)
		MutableDateTime dt = new MutableDateTime(DateTimeZone.forTimeZone(tzClient));
		dt.setDateTime(2010, 1, 15, 0, 0, 0, 0);
		Date date = new Date(dt.getMillis());
		DatesPage2 testPage = new DatesPage2();
		testPage.time = date;
		tester.startPage(testPage);
		FormTester formTester = tester.newFormTester("form");
		formTester.setValue("timeField:hours", "00");
		formTester.setValue("timeField:minutes", "00");
		formTester.submit();
		assertEquals(date, testPage.time);

		// Test with daylight savings time (in client time zone)
		dt = new MutableDateTime(DateTimeZone.forTimeZone(tzClient));
		dt.setDateTime(2010, 7, 15, 0, 0, 0, 0);
		date = new Date(dt.getMillis());
		testPage = new DatesPage2();
		testPage.time = date;
		tester.startPage(testPage);
		formTester = tester.newFormTester("form");
		formTester.setValue("timeField:hours", "00");
		formTester.setValue("timeField:minutes", "00");
		formTester.submit();
		assertEquals(date, testPage.time);

		TimeZone.setDefault(origJvmDef);
		DateTimeZone.setDefault(origJodaDef);
	}

	/**
	 * Test StyleDateConverter with the server's time zone having a different current date than the
	 * client time zone.
	 * 
	 * @throws ParseException
	 */
	@Test
	public void testStyleDateConverterTimeZoneDifference() throws ParseException
	{
		TimeZone origJvmDef = TimeZone.getDefault();
		DateTimeZone origJodaDef = DateTimeZone.getDefault();

		TimeZone tzClient = TimeZone.getTimeZone("GMT+14");
		TimeZone tzServer = TimeZone.getTimeZone("GMT-12");

		TimeZone.setDefault(tzServer);
		DateTimeZone.setDefault(DateTimeZone.forTimeZone(tzServer));

		WebClientInfo clientInfo = (WebClientInfo)tester.getSession().getClientInfo();
		clientInfo.getProperties().setTimeZone(tzClient);

		StyleDateConverter converter = new StyleDateConverter(true);

		Calendar cal = Calendar.getInstance(tzClient);
		cal.set(2011, 10, 5, 0, 0, 0);
		cal.set(Calendar.MILLISECOND, 0);

		Date dateRef = cal.getTime();
		Date date = converter.convertToObject("05.11.2011", Locale.GERMANY);
		log.debug("ref: " + dateRef.getTime() + "; converted: " + date.getTime());
		log.debug("ref: " + dateRef + "; date: " + date);
		assertEquals(dateRef, date);

		TimeZone.setDefault(origJvmDef);
		DateTimeZone.setDefault(origJodaDef);
	}

	/**
	 * Validates the "value" tags of the &ltinput&gt fields for DateTimeField, DateField and
	 * TimeField when they are given Date models containing Date instances.
	 */
	@Test
	public void testDateFieldsWithDateModels()
	{
		TimeZone origJvmDef = TimeZone.getDefault();
		DateTimeZone origJodaDef = DateTimeZone.getDefault();

		TimeZone tzClient = TimeZone.getTimeZone("GMT-12");
		TimeZone tzServer = TimeZone.getTimeZone("GMT+14");

		TimeZone.setDefault(tzServer);
		DateTimeZone.setDefault(DateTimeZone.forTimeZone(tzServer));
		WebClientInfo clientInfo = (WebClientInfo)tester.getSession().getClientInfo();
		clientInfo.getProperties().setTimeZone(tzClient);

		Calendar cal = Calendar.getInstance(tzServer);
		cal.set(2011, 5, 15, 10, 30, 0);
		cal.set(Calendar.MILLISECOND, 0);
		Date date = cal.getTime();

		DateFormat format = DateFormat.getDateInstance(DateFormat.SHORT, Locale.GERMAN);
		format.setTimeZone(tzClient);
		String dateRefString = format.format(date);
		cal.setTimeZone(tzClient);
		String hoursRefString = Integer.toString(cal.get(Calendar.HOUR_OF_DAY));
		String minutesRefString = Integer.toString(cal.get(Calendar.MINUTE));

		DatesPage2 testPage = new DatesPage2();
		testPage.dateTime = date;
		testPage.date = date;
		testPage.time = date;
		tester.getSession().setLocale(Locale.GERMAN);
		tester.startPage(testPage);

		String dateTimeFieldDateValue = tester.getTagByWicketId("dateTimeField")
			.getChild("wicket:id", "date")
			.getAttribute("value");
		assertEquals(dateRefString, dateTimeFieldDateValue);
		String dateTimeFieldHoursValue = tester.getTagByWicketId("dateTimeField")
			.getChild("wicket:id", "hours")
			.getAttribute("value");
		assertEquals(hoursRefString, dateTimeFieldHoursValue);
		String dateTimeFieldMinutesValue = tester.getTagByWicketId("dateTimeField")
			.getChild("wicket:id", "minutes")
			.getAttribute("value");
		assertEquals(minutesRefString, dateTimeFieldMinutesValue);
		String dateFieldValue = tester.getTagByWicketId("dateField")
			.getChild("wicket:id", "date")
			.getAttribute("value");
		assertEquals(dateRefString, dateFieldValue);
		String timeFieldHoursValue = tester.getTagByWicketId("timeField")
			.getChild("wicket:id", "hours")
			.getAttribute("value");
		assertEquals(hoursRefString, timeFieldHoursValue);
		String timeFieldMinutesValue = tester.getTagByWicketId("timeField")
			.getChild("wicket:id", "minutes")
			.getAttribute("value");
		assertEquals(minutesRefString, timeFieldMinutesValue);

		TimeZone.setDefault(origJvmDef);
		DateTimeZone.setDefault(origJodaDef);
	}

	/**
	 * 
	 * @throws ParseException
	 */
	@Test
	public void testDates1() throws ParseException
	{
		log.debug("=========== testDates1() =================");
		TimeZone tzClient = TimeZone.getTimeZone("America/Los_Angeles");
		TimeZone tzServer = TimeZone.getTimeZone("Europe/Berlin");

		TimeZone.setDefault(tzServer);
		DateTimeZone.setDefault(DateTimeZone.forTimeZone(tzServer));
		Locale.setDefault(Locale.GERMANY);

// Date orig = convertDate("06.11.2010", null, null, null, false, tzClient);
// Date origJoda = convertDateJoda("06.11.2010", null, null, null, false, tzClient);
		Date orig3 = convertDateNew("06.11.2010", null, null, null, false, tzClient);

		MutableDateTime dt = new MutableDateTime(DateTimeZone.forTimeZone(tzClient));
		dt.setDateTime(2010, 11, 6, 0, 0, 0, 0);
		Date date = new Date(dt.getMillis());

		log.debug(/* "actual: " + orig.getTime() + "; joda: " + origJoda.getTime() + */"; origNew: " +
			orig3.getTime() + "; expected: " + date.getTime());
		log.debug(/* "actual: " + orig + "; joda: " + origJoda + */"; origNew: " + orig3 +
			"; expected: " + date);
		assertEquals(date.getTime(), orig3.getTime());
// assertEquals(date.getTime(), orig.getTime());
// assertEquals(origJoda.getTime(), orig.getTime());
	}

	/**
	 * 
	 * @throws ParseException
	 */
	@Test
	public void testDates2() throws ParseException
	{
		log.debug("=========== testDates2() =================");
		TimeZone tzClient = TimeZone.getTimeZone("America/Los_Angeles");
		TimeZone tzServer = TimeZone.getTimeZone("Europe/Berlin");

		TimeZone.setDefault(tzServer);
		DateTimeZone.setDefault(DateTimeZone.forTimeZone(tzServer));
		Locale.setDefault(Locale.GERMANY);

// Date orig = convertDate("06.11.2010", 0, 0, AM_PM.AM, false, tzClient);
// Date origJoda = convertDateJoda("06.11.2010", 0, 0, AM_PM.AM, false, tzClient);
		Date orig3 = convertDateNew("06.11.2010", 0, 0, AM_PM.AM, false, tzClient);

		MutableDateTime dt = new MutableDateTime(DateTimeZone.forTimeZone(tzClient));
		dt.setDateTime(2010, 11, 6, 0, 0, 0, 0);
		Date date = new Date(dt.getMillis());

		log.debug(/* "actual: " + orig.getTime() + "; joda: " + origJoda.getTime() + */"; origNew: " +
			orig3.getTime() + "; expected: " + date.getTime());
		log.debug(/* "actual: " + orig + "; joda: " + origJoda + */"; origNew: " + orig3 +
			"; expected: " + date);
		assertEquals(date.getTime(), orig3.getTime());
// assertEquals(date.getTime(), orig.getTime());
// assertEquals(origJoda.getTime(), orig.getTime());
	}

	/**
	 * 
	 * @throws ParseException
	 */
	@Test
	public void testDates3() throws ParseException
	{
		log.debug("=========== testDates3() =================");
		TimeZone tzClient = TimeZone.getTimeZone("America/Los_Angeles");
		TimeZone tzServer = TimeZone.getTimeZone("Europe/Berlin");

		TimeZone.setDefault(tzServer);
		DateTimeZone.setDefault(DateTimeZone.forTimeZone(tzServer));
		Locale.setDefault(Locale.GERMANY);

// Date orig = convertDate("06.11.2010", 12, 0, null, false, tzClient);
// Date origJoda = convertDateJoda("06.11.2010", 12, 0, null, false, tzClient);
		Date orig3 = convertDateNew("06.11.2010", 12, 0, null, false, tzClient);

		MutableDateTime dt = new MutableDateTime(DateTimeZone.forTimeZone(tzClient));
		dt.setDateTime(2010, 11, 6, 12, 0, 0, 0);
		Date date = new Date(dt.getMillis());

		log.debug(/* "actual: " + orig.getTime() + "; joda: " + origJoda.getTime() + */"; origNew: " +
			orig3.getTime() + "; expected: " + date.getTime());
		log.debug(/* "actual: " + orig + "; joda: " + origJoda + */"; origNew: " + orig3 +
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
		log.debug(">>> convertDate()");
		Date dateFieldInput = (dateStr != null ? DateFormat.getDateInstance().parse(dateStr) : null);

		// Default to today, if date entry was invisible
		final MutableDateTime date;
		if (dateFieldInput != null)
		{
			log.debug("1. dateFieldInput: " + dateFieldInput.getTime() + "  " + dateFieldInput);
			date = new MutableDateTime(dateFieldInput);
		}
		else
		{
			log.debug("1. dateFieldInput: null");
			// Current date
			date = new MutableDateTime();
		}
		log.debug("2. mutable date: " + date.getMillis() + "  " + date);

		// always set secs to 0
		date.setSecondOfMinute(0);
		log.debug("3. secs = 0: " + date.getMillis() + "  " + date);

		// The AM/PM field
		if (use12HourFormat)
		{
			date.set(DateTimeFieldType.halfdayOfDay(), amOrPm == AM_PM.PM ? 1 : 0);
		}
		log.debug("4. AM/PM: " + date.getMillis() + "  " + date);

		// The hours
		if (hours == null)
		{
			date.setHourOfDay(0);
		}
		else
		{
			date.set(DateTimeFieldType.hourOfDay(), hours % (use12HourFormat ? 12 : 24));
		}
		log.debug("5. hours: " + date.getMillis() + "  " + date);

		// The minutes
		if (minutes == null)
		{
			date.setMinuteOfHour(0);
		}
		else
		{
			date.setMinuteOfHour(minutes);
		}
		log.debug("6. minutes: " + date.getMillis() + "  " + date);

		// Use the client timezone to properly calculate the millisecs
		if (tzClient != null)
		{
			date.setZoneRetainFields(DateTimeZone.forTimeZone(tzClient));
			log.debug("7. zone: " + date.getMillis() + "  " + date);
		}

		Date rtn = new Date(date.getMillis());
		log.debug("8. final date: " + rtn.getTime() + "  " + rtn);
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
	private Date convertDateNew(final String dateStr, final Integer hours, final Integer minutes,
		final AM_PM amOrPm, final boolean use12HourFormat, final TimeZone tzClient)
		throws ParseException
	{
		log.debug(">>> convertDateNew()");
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
		log.debug("1. date: " + date.getMillis() + "  " + date);

		// Adjust for halfday if needed
		int halfday;
		if (use12HourFormat)
		{
			halfday = (amOrPm == AM_PM.PM ? 1 : 0);
			date.set(DateTimeFieldType.halfdayOfDay(), halfday);
			date.set(DateTimeFieldType.hourOfDay(), iHours % 12);
		}
		log.debug("2. halfday adjustments: " + date.getMillis() + "  " + date);

		Date rtn = new Date(date.getMillis());
		log.debug("3. final date: " + rtn.getTime() + "  " + rtn);
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
	private Date convertDateJoda(final String dateStr, final Integer hours, final Integer minutes,
		final AM_PM amOrPm, final boolean use12HourFormat, final TimeZone tzClient)
		throws ParseException
	{
		log.debug(">>> convertDateJoda()");

		DateTimeFormatter fmt = DateTimeFormat.shortDate();
		// fmt.withZone(timeZone).parseDateTime("10/1/06 5:00 AM");
		MutableDateTime date = (dateStr != null ? fmt.parseMutableDateTime(dateStr)
			: new MutableDateTime());

		log.debug("1. mutable date: " + date.getMillis() + "  " + date);

		// always set secs to 0
		date.setSecondOfMinute(0);
		log.debug("2. secs = 0: " + date.getMillis() + "  " + date);

		// The AM/PM field
		if (use12HourFormat)
		{
			date.set(DateTimeFieldType.halfdayOfDay(), amOrPm == AM_PM.PM ? 1 : 0);
		}
		log.debug("3. AM/PM: " + date.getMillis() + "  " + date);

		// The hours
		if (hours == null)
		{
			date.setHourOfDay(0);
		}
		else
		{
			date.set(DateTimeFieldType.hourOfDay(), hours % (use12HourFormat ? 12 : 24));
		}
		log.debug("4. hours: " + date.getMillis() + "  " + date);

		// The minutes
		if (minutes == null)
		{
			date.setMinuteOfHour(0);
		}
		else
		{
			date.setMinuteOfHour(minutes);
		}
		log.debug("5. minutes: " + date.getMillis() + "  " + date);

		// Use the client timezone to properly calculate the millisecs
		if (tzClient != null)
		{
			date.setZoneRetainFields(DateTimeZone.forTimeZone(tzClient));
		}
		log.debug("6. timezone: " + date.getMillis() + "  " + date);

		Date rtn = new Date(date.getMillis());
		log.debug("7. final date: " + rtn.getTime() + "  " + rtn);
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

		tester.getSession().setLocale(Locale.ITALIAN);
		tester.startPage(pageClass);
		tester.assertRenderedPage(pageClass);

		String document = tester.getLastResponseAsString();
		document = document.replaceAll("\\d\\d\\.\\d\\d\\.\\d\\d", "xx.xx.xx");
		document = document.replaceAll("\\d\\d/\\d\\d/\\d\\d\\d\\d", "xx/xx/xxxx");
		document = document.replaceAll("\\d\\d/\\d\\d/\\d\\d", "xx/xx/xx");
		document = document.replaceAll("\\d\\d/\\d\\d\\d\\d", "xx/xxxx");

		DiffUtil.validatePage(document, pageClass, filename, true);
	}
}
