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
package org.apache.wicket.markup.html.form.panelBorder;

import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 * Simple test using the WicketTester
 */
public class TestHomePage_2134 extends WicketTestCase
{
	/**
	 * WICKET-2134: two forms inside a border throw a ConversionException error
	 */
	@Test
	public void testRenderMyPage()
	{
		// start and render the test page
		tester.startPage(CommonModelPage.class);

		// assert rendered page class
		tester.assertRenderedPage(CommonModelPage.class);

		FormTester formTester = tester.newFormTester("border:body:form1");
		formTester.submit();

		CommonModelPage page = (CommonModelPage)tester.getLastRenderedPage();
		assertEquals(0, page.quantity1);
		assertEquals(0, page.quantity2);
	}

	/**
	 * WICKET-2134: two forms inside a border throw a ConversionException error
	 */
	@Test
	public void testRenderMyPage2()
	{
		// start and render the test page
		tester.startPage(CommonModelPage.class);

		// assert rendered page class
		tester.assertRenderedPage(CommonModelPage.class);

		FormTester formTester = tester.newFormTester("border:body:form1");
		formTester.setValue("quantity1", "123");
		// formTester.setValue("quantity2", "44");
		formTester.submit();

		CommonModelPage page = (CommonModelPage)tester.getLastRenderedPage();
		assertEquals(123, page.quantity1);
		assertEquals(0, page.quantity2);
	}

	/**
	 * WICKET-2134: two forms inside a border throw a ConversionException error
	 */
	@Test
	public void testRenderMyPage3()
	{
		// start and render the test page
		tester.startPage(CommonModelPage.class);

		// assert rendered page class
		tester.assertRenderedPage(CommonModelPage.class);

		FormTester formTester = tester.newFormTester("border:body:form2");
		// formTester.setValue("quantity1", "123");
		formTester.setValue("quantity2", "44");
		formTester.submit();

		CommonModelPage page = (CommonModelPage)tester.getLastRenderedPage();
		assertEquals(0, page.quantity1);
		assertEquals(44, page.quantity2);
	}
}
