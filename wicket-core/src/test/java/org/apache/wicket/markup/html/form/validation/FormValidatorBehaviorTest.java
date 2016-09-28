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
package org.apache.wicket.markup.html.form.validation;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.feedback.FeedbackCollector;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 */
public class FormValidatorBehaviorTest extends WicketTestCase
{
	/**
	 * Tests validators are treated as behaviors
	 */
	@Test
	public void actAsBehavior()
	{
		TestPage page = new TestPage();

		tester.startPage(page);

		assertFalse(tester.getPreviousRequests().contains("foo=\"bar\""));

		MaxLenValidator validator = new MaxLenValidator(page.name);
		page.form.add(validator);
		tester.startPage(page);

		tester.assertContains("foo=\"bar\"");

		page.form.remove(validator);

		assertFalse(tester.getPreviousRequests().contains("foo=\"bar\""));
	}

	/**
	 * Tests validators are treated as validators
	 */
	@Test
	public void actAsValidator()
	{
		TestPage page = new TestPage();

		tester.startPage(page);

		FormTester ft = tester.newFormTester("form");
		ft.setValue("name", "999999999");
		ft.submit();
		assertEquals(0, new FeedbackCollector(page).collect().size());

		MaxLenValidator max = new MaxLenValidator(page.name);
		page.form.add(max);

		ft = tester.newFormTester("form");
		ft.setValue("name", "999999999");
		ft.submit();
		assertEquals(1, new FeedbackCollector(page).collect().size());
		assertEquals("MAX", new FeedbackCollector(page).collect()
			.get(0)
			.getMessage()
			.toString());

		ft = tester.newFormTester("form");
		ft.setValue("name", "22");
		ft.submit();
		assertEquals(0, new FeedbackCollector(page).collect().size());

		MinLenValidator min = new MinLenValidator(page.name);
		page.form.add(min);

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

		page.form.remove(min);

		ft = tester.newFormTester("form");
		ft.setValue("name", "22");
		ft.submit();
		assertEquals(0, new FeedbackCollector(page).collect().size());

		page.form.remove(max);

		ft = tester.newFormTester("form");
		ft.setValue("name", "999999999");
		ft.submit();
		assertEquals(0, new FeedbackCollector(page).collect().size());

	}

	/**
	 */
	public static class MaxLenValidator extends Behavior implements IFormValidator
	{
		private static final long serialVersionUID = 1L;
		private final int len = 8;
		private final TextField<String> field;

		/**
		 * Construct.
		 * 
		 * @param field
		 */
		public MaxLenValidator(TextField<String> field)
		{
			this.field = field;
		}

		@Override
		public void onComponentTag(Component component, ComponentTag tag)
		{
			tag.put("foo", "bar");
		}

		@Override
		public FormComponent<?>[] getDependentFormComponents()
		{
			return new FormComponent[] { field };
		}

		@Override
		public void validate(Form<?> form)
		{
			String value = field.getConvertedInput();
			if (value.length() > len)
			{
				form.error("MAX");
			}
		}
	}

	/**
	 */
	public static class MinLenValidator implements IFormValidator
	{
		private static final long serialVersionUID = 1L;
		private int len = 5;
		private final TextField<String> field;

		/**
		 * Construct.
		 * 
		 * @param field
		 */
		public MinLenValidator(TextField<String> field)
		{
			this.field = field;
		}

		@Override
		public FormComponent<?>[] getDependentFormComponents()
		{
			return new FormComponent[] { field };
		}

		@Override
		public void validate(Form<?> form)
		{
			String value = field.getConvertedInput();
			if (value.length() < len)
			{
				form.error("MINIMUM");
			}
		}
	}

	/**
	 */
	public static class TestPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;
		/**	 */
		public TextField<String> name;
		/**	 */
		public Form<Void> form;

		/**
		 * Construct.
		 */
		public TestPage()
		{
			form = new Form<Void>("form");
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
