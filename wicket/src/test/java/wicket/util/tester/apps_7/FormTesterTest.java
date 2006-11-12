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
package wicket.util.tester.apps_7;

import wicket.WicketTestCase;
import wicket.util.diff.DiffUtil;
import wicket.util.tester.FormTester;
import wicket.util.tester.WicketTester;

/**
 * 
 */
public class FormTesterTest extends WicketTestCase
{
	/**
	 * Construct.
	 * 
	 * @param name
	 */
	public FormTesterTest(String name)
	{
		super(name);
	}

	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
	}

	/**
	 * @throws Exception
	 */
	public void test_1() throws Exception
	{
		tester = new WicketTester(EmailPage.class);
		tester.setupRequestAndResponse();
		tester.processRequestCycle();

		String document = tester.getServletResponse().getDocument();
		assertTrue(DiffUtil.validatePage(document, this.getClass(), "ExpectedResult-1.html"));

		assertEquals(EmailPage.class, tester.getLastRenderedPage().getClass());
		EmailPage page = (EmailPage)tester.getLastRenderedPage();

		FormTester formTester = tester.newFormTester("form");

		formTester.setValue("email", "a");
		formTester.submit();

		page = (EmailPage)tester.getLastRenderedPage();
		assertEquals(EmailPage.class, page.getClass());

		document = tester.getServletResponse().getDocument();
		assertTrue(DiffUtil.validatePage(document, this.getClass(), "ExpectedResult-2.html"));
	}
}
