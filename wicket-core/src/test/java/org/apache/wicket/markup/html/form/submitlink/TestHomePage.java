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
import org.apache.wicket.util.tester.FormTester;
import org.junit.Test;

/**
 */
public class TestHomePage extends WicketTestCase
{
	/**
	 * testSubmitLinkByClickingLink()
	 */
	@Test
	public void testSubmitLinkByClickingLink()
	{
		tester.startPage(HomePage.class);
		tester.assertRenderedPage(HomePage.class);
		HomePage home = (HomePage)tester.getLastRenderedPage();
		tester.getRequest().getPostParameters().setParameterValue("text", "Hello");
		tester.clickLink("form:link");
		assertEquals("Hello", home.getText());
		assertTrue("Form.onSubmit() has not been called!", home.isSubmitted());
		assertTrue("SubmitLink.onSubmit() has not been called!",
			home.isSubmittedViaLinkBefore());
		assertTrue("SubmitLink.onAfterSubmit() has not been called!",
			home.isSubmittedViaLinkAfter());

	}

	/**
	 * testSubmitLinkBySubmittingForm()
	 */
	@Test
	public void testSubmitLinkBySubmittingForm()
	{
		tester.startPage(HomePage.class);
		tester.assertRenderedPage(HomePage.class);
		HomePage home = (HomePage)tester.getLastRenderedPage();
		tester.getRequest().getPostParameters().setParameterValue("text", "Hello");
		// Pretend we clicked on "link"
		tester.getRequest().getPostParameters().setParameterValue("link", "");
		tester.submitForm("form");
		assertEquals("Hello", home.getText());
		assertTrue("Form.onSubmit() has not been called!", home.isSubmitted());
		assertTrue("SubmitLink.onSubmit() has not been called!",
			home.isSubmittedViaLinkBefore());
		assertTrue("SubmitLink.onAfterSubmit() has not been called!",
			home.isSubmittedViaLinkAfter());
	}

	/**
	 * testSubmitLinkByUsingFormTester()
	 */
	@Test
	public void testSubmitLinkByUsingFormTester()
	{
		tester.startPage(HomePage.class);
		tester.assertRenderedPage(HomePage.class);
		HomePage home = (HomePage)tester.getLastRenderedPage();
		FormTester formTester = tester.newFormTester("form");
		formTester.setValue("text", "Hello");
		formTester.submit("link");
		assertEquals("Hello", home.getText());
		assertTrue("Form.onSubmit() has not been called!", home.isSubmitted());
		assertTrue("SubmitLink.onSubmit() has not been called!",
			home.isSubmittedViaLinkBefore());
		assertTrue("SubmitLink.onAfterSubmit() has not been called!",
			home.isSubmittedViaLinkAfter());
	}

}
