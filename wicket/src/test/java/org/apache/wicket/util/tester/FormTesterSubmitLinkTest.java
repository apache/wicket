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
package org.apache.wicket.util.tester;

import junit.framework.TestCase;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;

public class FormTesterSubmitLinkTest extends TestCase
{
	private WicketTester tester;

	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		tester = new WicketTester();
	}

	/**
	 * this should work the same as regular submit
	 */
	public void testSubmitLink() throws Exception
	{
		tester.startPage(TestPage.class);

		FormTester form = tester.newFormTester("form");
		form.setValue("text", "some test text");
		form.submitLink("submit", false);
		assertEquals("some test text", tester.getComponentFromLastRenderedPage("form:text")
			.getDefaultModelObjectAsString());
	}

	public void testAjaxSubmitLink() throws Exception
	{
		tester.startPage(TestPage.class);

		FormTester form = tester.newFormTester("form2");
		form.setValue("text", "some test text");
		tester.clickLink("form2:submit", true);
		assertEquals("some test text", tester.getComponentFromLastRenderedPage("form2:text")
			.getDefaultModelObjectAsString());
	}

	public void testAjaxSubmitLinkMustNotLosePreviousInput() throws Exception
	{
		tester.startPage(TestPage.class);

		FormTester form = tester.newFormTester("form2");
		form.setValue("text", "some test text");
		tester.clickLink("form2:submit", true);
		// click again for no-change resubmit. should still contain the same values now.
		tester.clickLink("form2:submit", true);
		assertEquals("some test text", tester.getComponentFromLastRenderedPage("form2:text")
			.getDefaultModelObjectAsString());
	}

	public void testRegularSubmit() throws Exception
	{
		tester.startPage(TestPage.class);

		FormTester form = tester.newFormTester("form");
		form.setValue("text", "some test text");
		form.submit();
		assertEquals("some test text", tester.getComponentFromLastRenderedPage("form:text")
			.getDefaultModelObjectAsString());
	}

	@Override
	protected void tearDown() throws Exception
	{
		tester.destroy();
		super.tearDown();
	}

	public static class TestPage extends WebPage
	{
		public TestPage()
		{
			Form form = new Form("form");
			add(form);
			form.add(new TextField<String>("text", Model.of(""), String.class));
			form.add(new SubmitLink("submit"));
			Form form2 = new Form("form2");
			add(form2);
			form2.add(new TextField<String>("text", Model.of(""), String.class));
			form2.add(new AjaxSubmitLink("submit")
			{
				@Override
				protected void onSubmit(AjaxRequestTarget target, Form<?> form)
				{
				}
			});
		}
	}

}
