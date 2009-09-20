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
	// Disabled until we fixed the issue that WicketTestCase or WicketTester is not available in
	// other projects. They are in src/test/java. Maven will create a compile error since in Maven
	// the test folder is not exported, which is what we want. We don't want the test cases to be
	// part of the Wicket distribution jar. In Eclipse it works since all source folders are
	// exported and you can not disable specific ones.

	public void testDummy()
	{
	}

// WicketTester tester = new WicketTester();
//
// /**
// * @throws Exception
// */
// public void testRenderHomePage() throws Exception
// {
// myTestExecution(DatesPage.class, "DatesPage_1_ExpectedResult.html");
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
// tester.startPage(pageClass);
// tester.assertRenderedPage(pageClass);
//
// String document = tester.getServletResponse().getDocument();
// document = document.replaceAll("\\d\\d\\.\\d\\d\\.\\d\\d", "xx.xx.xx");
// document = document.replaceAll("\\d\\d/\\d\\d/\\d\\d\\d\\d", "xx.xx.xx");
//
// DiffUtil.validatePage(document, pageClass, filename, true);
// }
}
