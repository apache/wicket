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

/**
 * Test case for checking localized error messages.
 */
public class LocalizedErrorMessageTest extends WicketTestCase
{
	/**
	 * Test for checking if changing the session's locale to another language actually causes the
	 * feedback messages to be altered as well. Testcase for WICKET-891.
	 */
	public void testWICKET_891()
	{
		tester.setupRequestAndResponse();

		tester.getWicketSession().setLocale(new Locale("nl"));

		LocalizedMessagePage page = new LocalizedMessagePage();
		tester.startPage(page);
		tester.processRequestCycle();
		tester.setupRequestAndResponse();

		tester.getServletRequest().setRequestToComponent(page.form);
		tester.getServletRequest().setParameter(page.integerField.getInputName(), "foo");

		page.form.onFormSubmitted();

		tester.assertErrorMessages(new String[] { "'foo' is geen valide type van Integer." });
		tester.getWicketSession().setLocale(new Locale("us"));

		tester.getWicketSession().cleanupFeedbackMessages();

		tester.setupRequestAndResponse();

		page = new LocalizedMessagePage();
		tester.startPage(page);
		tester.processRequestCycle();
		tester.setupRequestAndResponse();

		tester.getServletRequest().setRequestToComponent(page.form);
		tester.getServletRequest().setParameter(page.integerField.getInputName(), "foo");

		page.form.onFormSubmitted();

		// strange: I get two Dutch messages, where I expect only one English message
		tester.assertErrorMessages(new String[] { "'foo' is not a valid Integer." });
	}
}
