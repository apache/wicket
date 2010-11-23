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

import junit.framework.TestCase;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTester;

/**
 * @author Pedro Santos
 */
public class FormWithMultipleButtonsTest extends TestCase
{
	private WicketTester tester;

	@Override
	protected void setUp() throws Exception
	{
		tester = new WicketTester();
	}

	/**
	 * Testing if the correct submit button is invoked in an form with multiple submit buttons. The
	 * browser set the clicked button input name as parameter on the HTTP request.
	 */
	public void testFindSubmittingButton()
	{
		TestPage testPage = new TestPage();
		tester.startPage(testPage);
		tester.setParameterForNextRequest("form:b2", "");
		tester.submitForm("form");
		assertFalse(testPage.b1.submitted);
		assertTrue(testPage.b2.submitted);
	}

	@Override
	protected void tearDown() throws Exception
	{
		tester.destroy();
	}

	private static class TestPage extends WebPage implements IMarkupResourceStreamProvider
	{
		Form form;
		TestButton b1;
		TestButton b2;

		public TestPage()
		{
			add(form = new Form("form"));
			form.add(b1 = new TestButton("b1"));
			form.add(b2 = new TestButton("b2"));
		}

		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream(
				"<html><body><form wicket:id=\"form\"><button wicket:id=\"b1\"></button><button wicket:id=\"b2\"></button></form></body></html>");
		}
	}

	private static class TestButton extends Button
	{
		boolean submitted;

		public TestButton(String id)
		{
			super(id);
		}

		@Override
		public void onSubmit()
		{
			submitted = true;
		}
	}
}
