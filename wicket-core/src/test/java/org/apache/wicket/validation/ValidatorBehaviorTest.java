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
package org.apache.wicket.validation;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.feedback.FeedbackCollector;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.FormTester;

/**
 * Tests validator work as validators and behaviors
 * 
 * @author igor
 */
public class ValidatorBehaviorTest extends WicketTestCase
{

	/**
	 * Tests validators are treated as behaviors
	 */
	public void testActAsBehavior()
	{
		TestPage page = new TestPage();

		tester.startPage(page);
		assertFalse(tester.getLastResponseAsString().contains("foo=\"bar\""));

		MaxLenValidator max = new MaxLenValidator();
		page.name.add(max);
		tester.startPage(page);
		tester.assertContains("foo=\"bar\"");

		page.name.remove(max);
		tester.startPage(page);
		assertFalse(tester.getLastResponseAsString().contains("foo=\"bar\""));
	}

	/**
	 * Tests validators are treated as validators
	 */
	public void testActAsValidator()
	{
		TestPage page = new TestPage();

		tester.startPage(page);

		FormTester ft = tester.newFormTester("form");
		ft.setValue("name", "999999999");
		ft.submit();
		assertEquals(0, new FeedbackCollector(page).collect().size());

		MaxLenValidator max = new MaxLenValidator();
		page.name.add(max);

		ft = tester.newFormTester("form");
		ft.setValue("name", "999999999");
		ft.submit();
		assertEquals(1, new FeedbackCollector(page).collect().size());
		// WICKET-5115 variables in default message are not substituted (was the case in 1.5.x)
		assertEquals("MAX ${len}", new FeedbackCollector(page).collect()
			.get(0)
			.getMessage()
			.toString());

		ft = tester.newFormTester("form");
		ft.setValue("name", "22");
		ft.submit();
		assertEquals(0, new FeedbackCollector(page).collect().size());

		MinLenValidator min = new MinLenValidator();
		page.name.add(min);

		ft = tester.newFormTester("form");
		ft.setValue("name", "22");
		ft.submit();
		assertEquals(1, new FeedbackCollector(page).collect().size());
		assertEquals("MINIMUM", new FeedbackCollector(page).collect()
			.get(0)
			.getMessage()
			.toString());

		ft = tester.newFormTester("form");
		ft.setValue("name", "7777777");
		ft.submit();
		assertEquals(0, new FeedbackCollector(page).collect().size());

		page.name.remove(min);

		ft = tester.newFormTester("form");
		ft.setValue("name", "22");
		ft.submit();
		assertEquals(0, new FeedbackCollector(page).collect().size());

		page.name.remove(max);

		ft = tester.newFormTester("form");
		ft.setValue("name", "999999999");
		ft.submit();
		assertEquals(0, new FeedbackCollector(page).collect().size());
	}

	/**
	 */
	public static class MaxLenValidator extends Behavior implements IValidator<String>
	{
		private static final long serialVersionUID = 1L;
		private final int len = 8;

		@Override
		public void validate(IValidatable<String> validatable)
		{
			String value = validatable.getValue();
			if (value.length() > len)
			{
				ValidationError error = new ValidationError();
				error.setVariable("len", len);
				error.setMessage("MAX ${len}");
				validatable.error(error);
			}
		}

		@Override
		public void onComponentTag(Component component, ComponentTag tag)
		{
			tag.put("foo", "bar");
		}
	}

	/**
	 */
	public static class MinLenValidator extends Behavior implements IValidator<String>
	{
		private static final long serialVersionUID = 1L;
		private int len = 5;

		@Override
		public void validate(IValidatable<String> validatable)
		{
			String value = validatable.getValue();
			if (value.length() < len)
			{
				ValidationError error = new ValidationError();
				error.setMessage("MINIMUM");
				validatable.error(error);
			}

		}

	}

	/**
	 */
	public static class TestPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;
		private TextField<String> name;

		/**
		 * Construct.
		 */
		public TestPage()
		{
			Form<Void> form = new Form<Void>("form");
			add(form);
			name = new TextField<String>("name", Model.of(""));
			form.add(name);
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream(
				"<form wicket:id='form'><input wicket:id='name' type='text'/></form>");
		}
	}
}
