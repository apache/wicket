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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

/**
 * Simple test using the WicketTester
 */
class AjaxButtonTest extends WicketTestCase
{
	/**
	 * 
	 */
	@Test
	void ajaxButtonWhenCancelButtonHasAModelValue()
	{
		// start and render the test page
		tester.startPage(HomePage.class);
		// assert rendered page class
		tester.assertRenderedPage(HomePage.class);
		// assert button type unmodified, WICKET-5993
		assertEquals("image", tester.getTagById("cancel3").getAttribute("type"));
		// assert button type unmodified for AjaxButtons, WICKET-6139
		assertEquals("submit", tester.getTagById("submit2").getAttribute("type"));
		// assert rendered label component
		tester.assertLabel("message",
			"If you see this message wicket is properly configured and running");

		// assert rendered page class
		HomePage homePage = (HomePage)tester.getLastRenderedPage();
		TestForm testForm = homePage.getForm();

		tester.getRequest()
			.getPostParameters()
			.setParameterValue(homePage.getForm().getSubmitButton().getInputName(), "x");
		tester.executeAjaxEvent(testForm.getSubmitButton(), "click");
	}
}
