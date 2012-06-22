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

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.junit.Test;

/**
 * <a href="https://issues.apache.org/jira/browse/WICKET-3711">WICKET-3711</a>
 */
public class FormTesterSubmitLinkTest extends WicketTestCase
{
	/**
	 * Submit via SubmitLink.
	 * <p>
	 * This should work the same as regular submit
	 */
	@Test
	public void submitLink()
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
	@Test
	public void regularSubmit()
	{
		tester.startPage(TestPage.class);

		FormTester form = tester.newFormTester("form");
		form.setValue("text", "some test text");
		form.submit();
		assertEquals("some test text", tester.getComponentFromLastRenderedPage("form:text")
			.getDefaultModelObjectAsString());
	}

	@Test
	public void radioComponentValueEncoding()
	{

		class TestPage extends WebPage implements IMarkupResourceStreamProvider
		{
			private static final long serialVersionUID = 1L;

			private String value;
			private boolean submitted;

			public TestPage()
			{
				Form<Void> form = new Form<Void>("form");
				add(form);

				RadioGroup<String> group = new RadioGroup<String>("group",
					new PropertyModel<String>(this, "value"));
				form.add(group);

				value = "a";

				group.add(new Radio<String>("a", Model.of("a")));
				group.add(new Radio<String>("b", Model.of("b")));

				form.add(new AjaxSubmitLink("submit")
				{
					@Override
					protected void onSubmitBeforeForm(AjaxRequestTarget target, Form<?> form)
					{
						submitted = true;
					}

					@Override
					protected void onError(AjaxRequestTarget target, Form<?> form)
					{
					}
				});
			}


			@Override
			public IResourceStream getMarkupResourceStream(MarkupContainer container,
				Class<?> containerClass)
			{
				return new StringResourceStream(
					"<html><body><form wicket:id='form'><div wicket:id='group'><input type='radio' wicket:id='a'/><input type='radio' wicket:id='b'/></div><input wicket:id='submit' type='submit'/></form></body></html>");
			}
		}

		TestPage page = new TestPage();
		WicketTester tester = new WicketTester();
		tester.startPage(page);

		// clicking an ajax submit link will force the form to be ajax-serialized, current values of
		// form components copied into request. this will check that the value of radio is correctly
		// serialized.

		tester.clickLink("form:submit");
		assertTrue(page.submitted);
		assertEquals("a", page.value);
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
