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

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.wicket.Page;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.protocol.http.request.WebClientInfo;
import org.apache.wicket.util.tester.DiffUtil;
import org.apache.wicket.util.tester.FormTester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class DatePickerTest extends WicketTestCase
{
	/** log. */
	private static final Logger log = LoggerFactory.getLogger(DatePickerTest.class);

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

		log.error("orig: " + date + "; date: " + page.date + "; dateTime: " + page.dateTime);
		assertEquals(date.toString(), page.dateTime.toString());
		assertEquals(date.toString(), page.date.toString());
	}

	/**
	 * 
	 * @throws Exception
	 */
	public void test3() throws Exception
	{
		TimeZone tzClient = TimeZone.getTimeZone("America/Los_Angeles");
		TimeZone tzServer = TimeZone.getTimeZone("Europe/Berlin");

		System.setProperty("user.timezone", tzServer.getDisplayName());

		Class<? extends Page> pageClass = DatesPage2.class;
		GregorianCalendar gc = new GregorianCalendar(tzClient);
		gc.set(2010, 10, 06, 0, 0, 0);
		Date date = gc.getTime();

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
		formTester.setValue("dateField:hours", "00");
		formTester.setValue("dateField:minutes", "00");
		formTester.submit();

		DatesPage2 page = (DatesPage2)tester.getLastRenderedPage();

		log.error("orig: " + date + "; date: " + page.date + "; dateTime: " + page.dateTime);
		assertEquals(date.toString(), page.dateTime.toString());
		assertEquals(date.toString(), page.date.toString());
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
