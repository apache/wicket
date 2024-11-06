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

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.validation.IFormValidator;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTestCase;
import org.apache.wicket.validation.INullAcceptingValidator;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * <a href="https://issues.apache.org/jira/browse/WICKET-3899">WICKET-3899</a>
 * @author Pedro Santos
 */
public class FormVisitorParticipantTest extends WicketTestCase
{
	private TestFormPage page;

	@BeforeEach
	public void initialize()
	{
		page = new TestFormPage();
	}

	@Test
	public void validateInnerForm()
	{
		tester.startPage(page);
		tester.newFormTester("outerForm").submit();

		assertTrue(page.innerForm.onValidateCalled);
	}

	@Test
	public void dontValidateInnerForm()
	{
		page.outerForm.processChildren = false;
		tester.startPage(page);
		tester.newFormTester("outerForm").submit();

		assertFalse(page.innerForm.onValidateCalled);
	}

	@Test
	public void callInnerFormOnError()
	{
		page.innerField.add(new AlwaysFail());
		tester.startPage(page);
		tester.newFormTester("outerForm").submit();

		assertTrue(page.innerForm.onErrorCalled);
	}

	@Test
	public void dontCallInnerFormOnError()
	{
		page.innerField.add(new AlwaysFail());
		page.outerForm.processChildren = false;
		tester.startPage(page);
		tester.newFormTester("outerForm").submit();

		assertFalse(page.innerForm.onErrorCalled);
	}

	@Test
	public void submitInnerForm()
	{
		tester.startPage(page);
		tester.newFormTester("outerForm").submit();

		assertTrue(page.innerForm.onSubmit);
	}

	@Test
	public void dontSubmitInnerForm()
	{
		page.outerForm.processChildren = false;
		tester.startPage(page);
		tester.newFormTester("outerForm").submit();

		assertFalse(page.innerForm.onSubmit);
	}

	public static class TestFormPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private OuterForm outerForm;
		private InnerForm innerForm;
		private TextField<String> innerField;

		public TestFormPage()
		{
			add(outerForm = new OuterForm("outerForm"));
			outerForm.add(innerForm = new InnerForm("innerForm"));
			innerForm.add(innerField = new TextField<String>("innerField", Model.of((String)null)));
		}

		class OuterForm extends Form<Void> implements IFormVisitorParticipant
		{
			boolean processChildren = true;

			public OuterForm(String id)
			{
				super(id);
			}

			@Override
			public boolean processChildren()
			{
				return processChildren;
			}
		}

		class InnerForm extends Form<Void> implements IFormVisitorParticipant
		{
			boolean processChildren = true;
			boolean onValidateCalled;
			private boolean onErrorCalled;
			private boolean onSubmit;

			public InnerForm(String id)
			{
				super(id);
			}

			@Override
			protected void onSubmit()
			{
				onSubmit = true;
			}

			@Override
			public boolean processChildren()
			{
				return processChildren;
			}

			@Override
			protected void onValidate()
			{
				onValidateCalled = true;
			}

			@Override
			protected void onError()
			{
				onErrorCalled = true;
			}
		}

		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream("" //
				+ "<html><body>" //
				+ "  <form wicket:id=\"outerForm\">" //
				+ "    <form wicket:id=\"innerForm\"><input wicket:id=\"innerField\" /></form>" //
				+ "  </form>"//
				+ "</body></html>");
		}
	}

	private class FormValidator implements IFormValidator
	{
		private FormComponent<?>[] dependencies;
		private boolean validated;

		public FormValidator(FormComponent<?>... dependencies)
		{
			this.dependencies = dependencies;
		}

		public FormComponent<?>[] getDependentFormComponents()
		{
			return dependencies;
		}

		public void validate(Form<?> form)
		{
			validated = true;
		}

	}

	private class AlwaysFail implements IValidator<String>, INullAcceptingValidator<String>
	{
		boolean validated;

		@Override
		public void validate(IValidatable<String> validatable)
		{
			validatable.error(new ValidationError("foo"));
			validated = true;
		}
	}

}