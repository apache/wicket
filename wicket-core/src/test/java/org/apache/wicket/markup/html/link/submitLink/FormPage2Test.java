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
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 * 
 */
public class FormPage2Test extends WicketTestCase
{
	/**
	 * 
	 */
	@Test
	public void submitlinkIsSubmitted()
	{
		tester.startPage(FormPage2.class);

		FormPage2 page = (FormPage2)tester.getLastRenderedPage();

		Assert.assertFalse(page.isSubmitLinkSubmitted());
		Assert.assertFalse(page.isFormSubmitted());

		tester.clickLink("form:link", false);
		page = (FormPage2)tester.getLastRenderedPage();

		Assert.assertTrue(page.isSubmitLinkSubmitted());
		Assert.assertTrue(page.isFormSubmitted());
	}

	/**
	 * 
	 */
	@Test
	public void formIsSubmitted()
	{
		tester.startPage(FormPage2.class);

		FormPage2 page = (FormPage2)tester.getLastRenderedPage();

		Assert.assertFalse(page.isSubmitLinkSubmitted());
		Assert.assertFalse(page.isFormSubmitted());

		FormTester formTester = tester.newFormTester("form");
		formTester.submit();

		page = (FormPage2)tester.getLastRenderedPage();

		Assert.assertTrue(page.isFormSubmitted());
		Assert.assertFalse(page.isSubmitLinkSubmitted());
	}

	/**
	 * 
	 */
	@Test
	public void formAndLinkAreSubmitted()
	{
		tester.startPage(FormPage2.class);

		FormPage2 page = (FormPage2)tester.getLastRenderedPage();

		Assert.assertFalse(page.isSubmitLinkSubmitted());
		Assert.assertFalse(page.isFormSubmitted());

		FormTester formTester = tester.newFormTester("form");
		formTester.submitLink("link", false);

		page = (FormPage2)tester.getLastRenderedPage();

		Assert.assertTrue(page.isFormSubmitted());
		Assert.assertTrue(page.isSubmitLinkSubmitted());
	}
}
