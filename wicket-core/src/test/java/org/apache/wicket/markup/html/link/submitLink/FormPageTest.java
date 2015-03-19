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
package org.apache.wicket.markup.html.link.submitLink;

import org.junit.Assert;

import org.apache.wicket.WicketTestCase;
import org.apache.wicket.util.tester.FormTester;
import org.junit.Test;

/**
 * 
 */
public class FormPageTest extends WicketTestCase
{
	/**
	 * 
	 */
	@Test
	public void submitlinkIsSubmitted()
	{
		tester.startPage(FormPage.class);

		FormPage page = (FormPage)tester.getLastRenderedPage();

		Assert.assertFalse(page.isSubmitLinkSubmitted());
		Assert.assertFalse(page.isFormSubmitted());

		tester.clickLink("link", false);
		page = (FormPage)tester.getLastRenderedPage();

		Assert.assertTrue(page.isSubmitLinkSubmitted());
		Assert.assertTrue(page.isFormSubmitted());
	}

	/**
	 * 
	 */
	@Test
	public void formIsSubmitted()
	{
		tester.startPage(FormPage.class);

		FormPage page = (FormPage)tester.getLastRenderedPage();

		Assert.assertFalse(page.isSubmitLinkSubmitted());
		Assert.assertFalse(page.isFormSubmitted());

		FormTester formTester = tester.newFormTester("form");
		formTester.submit();

		page = (FormPage)tester.getLastRenderedPage();

		Assert.assertTrue(page.isFormSubmitted());
		Assert.assertFalse(page.isSubmitLinkSubmitted());
	}

	/**
	 * 
	 */
	@Test
	public void formAndLinkAreSubmitted()
	{
		tester.startPage(FormPage.class);

		FormPage page = (FormPage)tester.getLastRenderedPage();

		Assert.assertFalse(page.isSubmitLinkSubmitted());
		Assert.assertFalse(page.isFormSubmitted());

		FormTester formTester = tester.newFormTester("form");
		formTester.submitLink("link", true);

		page = (FormPage)tester.getLastRenderedPage();

		Assert.assertTrue(page.isFormSubmitted());
		Assert.assertTrue(page.isSubmitLinkSubmitted());
	}
}
