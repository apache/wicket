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

import junit.framework.TestCase;

/**
 * 
 */
public class DatePickerTest extends TestCase
{
	/**
	 * FIXME WicketTestCase is available in Eclipse but not with Maven
	 */
	public void testDummy()
	{
	}

// public void test1() throws Exception
// {
// myTestExecution(DatesPage1.class, "DatesPage1_ExpectedResult.html");
// }
//
// /**
// * @throws Exception
// */
// public void test2() throws Exception
// {
// Class<? extends Page> pageClass = DatesPage2.class;
// Date date = new GregorianCalendar(2010, 10, 06, 0, 0).getTime();
// tester.getSession().setLocale(Locale.GERMAN);
// tester.startPage(pageClass);
// tester.assertRenderedPage(pageClass);
// FormTester formTester = tester.newFormTester("form");
// formTester.setValue("dateTimeField:date", "06.11.2010");
// formTester.setValue("dateTimeField:hours", "00");
// formTester.setValue("dateTimeField:minutes", "00");
// formTester.setValue("dateField:date", "06.11.2010");
// formTester.submit();
// DatesPage2 page = (DatesPage2)tester.getLastRenderedPage();
// assertEquals(date, page.dateTime);
// assertEquals(date, page.date);
// }
//
// /**
// * Use <code>-Dwicket.replace.expected.results=true</code> to automatically replace the expected
// * output file.
// *
// * @param <T>
// *
// * @param pageClass
// * @param filename
// * @throws Exception
// */
// protected <T extends Page> void myTestExecution(final Class<T> pageClass, final String filename)
// throws Exception
// {
// System.out.println("=== " + pageClass.getName() + " ===");
//
// tester.getSession().setLocale(Locale.GERMAN);
// tester.startPage(pageClass);
// tester.assertRenderedPage(pageClass);
//
// String document = tester.getLastResponseAsString();
// document = document.replaceAll("\\d\\d\\.\\d\\d\\.\\d\\d", "xx.xx.xx");
// document = document.replaceAll("\\d\\d/\\d\\d/\\d\\d\\d\\d", "xx.xx.xxxx");
// document = document.replaceAll("\\d\\d/\\d\\d\\d\\d", "xx.xxxx");
//
// DiffUtil.validatePage(document, pageClass, filename, true);
// }
}
