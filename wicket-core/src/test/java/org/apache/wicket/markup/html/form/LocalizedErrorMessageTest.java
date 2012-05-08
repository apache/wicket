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
package org.apache.wicket.markup.html.form;

import java.util.Locale;

import org.apache.wicket.WicketTestCase;
import org.junit.Test;

/**
 * Test case for checking localized error messages.
 */
public class LocalizedErrorMessageTest extends WicketTestCase
{
	/**
	 * Test for checking if changing the session's locale to another language actually causes the
	 * feedback messages to be altered as well. Testcase for WICKET-891.
	 */
	@Test
	public void wicket891()
	{
		tester.getSession().setLocale(new Locale("nl"));

		LocalizedMessagePage page = new LocalizedMessagePage();
		tester.startPage(page);

		tester.getRequest()
			.getPostParameters()
			.setParameterValue(page.integerField.getInputName(), "foo");
		tester.submitForm(page.form);

		tester.assertErrorMessages("'Number' moet een geheel getal zijn. ");
		tester.getSession().setLocale(new Locale("us"));

		tester.clearFeedbackMessages();

		page = new LocalizedMessagePage();
		tester.startPage(page);

		tester.getRequest()
			.getPostParameters()
			.setParameterValue(page.integerField.getInputName(), "foo");

		tester.submitForm(page.form);

		tester.assertErrorMessages("'Number' is not a valid Integer.");
	}

	/**
	 * WicketTester.assertErrorMessages returns FeedbackMessages in iso-8859-1 encoding only. Hence
	 * assertErrorMessage will fail for special characters in languages like e.g. German. Testcase
	 * for WICKET-1972.
	 * 
	 */
	@Test
	public void wicket_1927()
	{
		tester.getApplication().getMarkupSettings().setDefaultMarkupEncoding("UTF-8");
		tester.getSession().setLocale(new Locale("de"));

		LocalizedMessagePage page = new LocalizedMessagePage();
		tester.startPage(page);

		tester.getRequest()
			.getPostParameters()
			.setParameterValue(page.integerField.getInputName(), "foo");

		tester.submitForm(page.form);

		tester.assertErrorMessages("'Number' ist kein g\u00FCltiger Wert f\u00FCr 'Integer'.");
		tester.getSession().setLocale(new Locale("pl"));

		tester.clearFeedbackMessages();

		page = new LocalizedMessagePage();
		tester.startPage(page);

		tester.getRequest()
			.getPostParameters()
			.setParameterValue(page.integerField.getInputName(), "foo");

		tester.submitForm(page.form);
		tester.assertErrorMessages("'Number' nie jest w\u0142a\u015Bciwym Integer.");
	}
}
