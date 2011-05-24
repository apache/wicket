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

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;

/**
 * <a href="https://issues.apache.org/jira/browse/WICKET-3711">WICKET-3711</a>
 */
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
	 * Submit via SubmitLink.
	 * <p>
	 * This should work the same as regular submit
	 */
	public void testSubmitLink()
	{
		tester.startPage(TestPage.class);

		FormTester form = tester.newFormTester("form");
		form.setValue("text", "some test text");
		form.submitLink("submit", false);
		assertEquals("some test text", tester.getComponentFromLastRenderedPage("form:text")
			.getDefaultModelObjectAsString());
	}

	/**
	 * Submit the form
	 */
	public void testRegularSubmit()
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

	/**
	 * A test page for {@link FormTesterSubmitLinkTest}
	 */
	public static class TestPage extends WebPage
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 */
		public TestPage()
		{
			Form<?> form = new Form<Void>("form");
			add(form);
			form.add(new TextField<String>("text", Model.of(""), String.class));
			form.add(new SubmitLink("submit"));
		}
	}

}
