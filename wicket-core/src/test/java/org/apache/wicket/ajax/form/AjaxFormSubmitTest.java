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
package org.apache.wicket.ajax.form;

import org.apache.wicket.Page;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 * @author marrink
 */
public class AjaxFormSubmitTest extends WicketTestCase
{
	@Test
	public void submit()
	{
		AjaxFormSubmitTestPage page = new AjaxFormSubmitTestPage(true);

		tester.startPage(page);
		tester.assertRenderedPage(page.getClass());
		FormTester form = tester.newFormTester("form");
		form.setValue("txt1", "txt1");
		form.setValue("txt2", "txt2");
		// mark the button as the one being pressed. there is a 'p::' infront of name because wicket
		// escapes "submit" input names as they break browsers
		tester.getRequest().getPostParameters().setParameterValue("p::submit", "x");
		tester.submitForm(form.getForm());
		tester.assertRenderedPage(page.getClass());
		assertTrue((page.getFormSubmitted() & AjaxFormSubmitTestPage.FORM_SUBMIT) > 0);
		assertFalse((page.getFormSubmitted() & AjaxFormSubmitTestPage.FORM_ERROR) > 0);
		assertTrue((page.getFormSubmitted() & AjaxFormSubmitTestPage.BUTTON_SUBMIT) > 0);
		assertFalse((page.getFormSubmitted() & AjaxFormSubmitTestPage.BUTTON_ERROR) > 0);
		assertEquals("txt1", tester.getComponentFromLastRenderedPage("form:txt1")
			.getDefaultModelObject());
		assertEquals("txt2", tester.getComponentFromLastRenderedPage("form:txt2")
			.getDefaultModelObject());
	}

	/**
	 * Test ajax form submit without default form processing.
	 *
	 * @see <a href="https://issues.apache.org/jira/browse/WICKET-1291">WICKET-1291</a>
	 */
	@Test
	public void submitNoDefProcessing()
	{
		AjaxFormSubmitTestPage page = new AjaxFormSubmitTestPage(false);

		tester.startPage(page);
		tester.assertRenderedPage(page.getClass());
		FormTester form = tester.newFormTester("form");
		form.setValue("txt1", "txt1");
		form.setValue("txt2", "txt2");
		tester.executeAjaxEvent("form:submit", "click");
		tester.assertRenderedPage(page.getClass());
		assertFalse((page.getFormSubmitted() & AjaxFormSubmitTestPage.FORM_SUBMIT) > 0);
		assertFalse((page.getFormSubmitted() & AjaxFormSubmitTestPage.FORM_ERROR) > 0);
		assertTrue((page.getFormSubmitted() & AjaxFormSubmitTestPage.BUTTON_SUBMIT) > 0);
		assertFalse((page.getFormSubmitted() & AjaxFormSubmitTestPage.BUTTON_ERROR) > 0);
		assertEquals("foo", tester.getComponentFromLastRenderedPage("form:txt1")
			.getDefaultModelObject());
		assertEquals("bar", tester.getComponentFromLastRenderedPage("form:txt2")
			.getDefaultModelObject());
	}

	/**
	 * Test ajax fallback with errors.
	 *
	 * @see <a href="https://issues.apache.org/jira/browse/WICKET-6324">WICKET-6324</a>
	 */
	@Test
	public void submitFallbackErrors()
	{
		AjaxFormSubmitTestPage page = new AjaxFormSubmitTestPage(true);

		tester.startPage(page);
		tester.assertRenderedPage(page.getClass());
		FormTester form = tester.newFormTester("form");
		form.setValue("txt1", "");
		form.setValue("txt2", "");
		// mark the button as the one being pressed. there is a 'p::' infront of name because wicket
		// escapes "submit" input names as they break browsers
		tester.getRequest().getPostParameters().setParameterValue("p::submit", "x");
		tester.submitForm(form.getForm());
		tester.assertRenderedPage(page.getClass());
		assertFalse((page.getFormSubmitted() & AjaxFormSubmitTestPage.FORM_SUBMIT) > 0);
		assertTrue((page.getFormSubmitted() & AjaxFormSubmitTestPage.FORM_ERROR) > 0);
		assertFalse((page.getFormSubmitted() & AjaxFormSubmitTestPage.BUTTON_SUBMIT) > 0);
		assertTrue((page.getFormSubmitted() & AjaxFormSubmitTestPage.BUTTON_ERROR) > 0);
	}

	/**
	 * Test that onclick handler is generated with the proper XHTML entities for special characters,
	 * notably ampersand. See WICKET-2033.
	 * 
	 * @throws Exception
	 */
	@Test
	public void eventJavaScriptEscaped() throws Exception
	{
		tester.startPage(new AjaxFormSubmitTestPage(false));
		tester.assertResultPage(AjaxFormSubmitTestPage.class,
			"AjaxFormSubmitTestPage_expected.html");
	}

}
