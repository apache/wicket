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
package org.apache.wicket.ajax.markup.html.form;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.wicket.Session;
import org.apache.wicket.ajax.StatelessPage;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.page.XmlPartialPageUpdate;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StatelessAjaxSubmitLinkTest
{
	private WicketTester tester;

	@BeforeEach
	void setUp()
	{
		tester = new WicketTester(new MockApplication());
	}

	@AfterEach
	void teardown()
	{
		// things must stay stateless
		assertTrue(Session.get().isTemporary());
	}

	@Test
	void testSubmitForm() throws Exception
	{
		tester.startPage(StatelessPage.class);

		FormTester formTester = tester.newFormTester("inputForm");
		formTester.setValue("name", "myname");
		formTester.setValue("surname", "mysurname");

		tester.executeAjaxEvent("inputForm:submit", "click");

		String response = tester.getLastResponseAsString();

		boolean isAjaxResponse = response.contains(XmlPartialPageUpdate.START_ROOT_ELEMENT) &&
			response.contains(XmlPartialPageUpdate.END_ROOT_ELEMENT);

		assertTrue(isAjaxResponse);

		boolean formAjaxSubmit = response.contains(StatelessPage.FORM_SUBMIT) &&
			response.contains(StatelessPage.AJAX_SUBMIT);

		assertTrue(formAjaxSubmit);
	}
}
