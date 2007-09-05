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
package org.apache.wicket.markup.html.form.submitlink;

import org.apache.wicket.WicketTestCase;
import org.apache.wicket.protocol.http.MockHttpServletResponse;
import org.apache.wicket.util.tester.FormTester;

public class TestHomePage extends WicketTestCase
{
	public void bugTestSubmitLinkByClickingLink()
	{
		tester.startPage(HomePage.class);
		tester.assertRenderedPage(HomePage.class);
		HomePage home = (HomePage)tester.getLastRenderedPage();
		System.out.println(((MockHttpServletResponse)tester.getWicketResponse()
				.getHttpServletResponse()).getDocument());
		tester.setParameterForNextRequest("form:text", "Hello");
		// FIXME clickLink should submit the form
		tester.clickLink("form:link");
		assertEquals("Hello", home.getText());
		assertTrue("Form.onSubmit() has not been called!", home.isSubmitted());
		assertTrue("SubmitLink.onSubmit() has not been called!", home.isSubmittedViaLink());
	}

	public void testSubmitLinkBySubmittingForm()
	{
		tester.startPage(HomePage.class);
		tester.assertRenderedPage(HomePage.class);
		HomePage home = (HomePage)tester.getLastRenderedPage();
		tester.setParameterForNextRequest("form:text", "Hello");
		// Pretend we clicked on "link"
		tester.getParametersForNextRequest().put("link", "");
		tester.submitForm("form");
		assertEquals("Hello", home.getText());
		assertTrue("Form.onSubmit() has not been called!", home.isSubmitted());
		assertTrue("SubmitLink.onSubmit() has not been called!", home.isSubmittedViaLink());
	}

	public void bugTestSubmitLinkByUsingFormTester()
	{
		tester.startPage(HomePage.class);
		tester.assertRenderedPage(HomePage.class);
		HomePage home = (HomePage)tester.getLastRenderedPage();
		FormTester formTester = tester.newFormTester("form");
		formTester.setValue("text", "Hello");
		// FIXME submit(String) should allow for SubmitLink
		formTester.submit("link");
		assertEquals("Hello", home.getText());
		assertTrue("Form.onSubmit() has not been called!", home.isSubmitted());
		assertTrue("SubmitLink.onSubmit() has not been called!", home.isSubmittedViaLink());
	}

}
