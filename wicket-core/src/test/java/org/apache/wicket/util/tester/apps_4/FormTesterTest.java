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
package org.apache.wicket.util.tester.apps_4;

import java.util.List;

import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;


/**
 * @author Ingram Chen
 */
public class FormTesterTest extends WicketTestCase
{

	/**
	 * @throws Exception
	 */
	@Test
	public void test_1() throws Exception
	{
		tester.startPage(EmailPage.class);

		assertEquals(EmailPage.class, tester.getLastRenderedPage().getClass());
		EmailPage page = (EmailPage)tester.getLastRenderedPage();

		FormTester formTester = tester.newFormTester("form");

		formTester.setValue("email", "a");
		formTester.submit();

		assertEquals(EmailPage.class, tester.getLastRenderedPage().getClass());
		page = (EmailPage)tester.getLastRenderedPage();

		assertNull(page.getEmail());
		assertTrue(page.get("form:email").hasFeedbackMessage());

		final List<FeedbackMessage> messages = page.get("form:email")
			.getFeedbackMessages()
			.toList();

		assertEquals(1, messages.size());
		assertEquals("wrong email address pattern for email", messages.get(0)
			.getMessage()
			.toString());
	}
}
