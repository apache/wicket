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
import org.apache.wicket.markup.html.WebMarkupContainer;
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
 *
 * @author Pedro Santos
 */
public class FormVisitorParticipantTest extends WicketTestCase
{
	TestFormPage page;
	static int SEQUENCE;

	@BeforeEach
	public void initialize()
	{
		page = new TestFormPage();
		tester.startPage(page);
	}

	@Test
	public void validateInnerForm()
	{
		tester.newFormTester("outerForm").submit();

		assertTrue(page.innerForm.onValidateCalled);
	}

	@Test
	public void dontValidateInnerForm()
	{
		page.outerForm.processChildren = false;
		tester.newFormTester("outerForm").submit();

		assertFalse(page.innerForm.onValidateCalled);
	}

	@Test
	public void callInnerFormOnError()
	{
		page.innerField.add(new AlwaysFail());
		tester.newFormTester("outerForm").submit();

		assertTrue(page.innerForm.onErrorCalled);
	}

	@Test
	public void dontCallInnerFormOnErrorIfNotProcessChildren()
	{
		page.innerField.add(new AlwaysFail());
		page.outerForm.processChildren = false;
		tester.newFormTester("outerForm").submit();

		assertFalse(page.innerForm.onErrorCalled);
	}

	@Test
	public void dontCallInnerFormOnErrorIfNotEnabled()
	{
		page.innerField.add(new AlwaysFail());
		page.innerForm.setEnabled(false);
		tester.newFormTester("outerForm").submit();

		assertFalse(page.innerForm.onErrorCalled);
	}

	@Test
	public void submitInnerForm()
	{
		tester.newFormTester("outerForm").submit();

		assertTrue(page.innerForm.onSubmitCalled);
	}

	@Test
	public void dontSubmitInnerForm()
	{
		page.outerForm.processChildren = false;
		tester.newFormTester("outerForm").submit();

		assertFalse(page.innerForm.onSubmitCalled);
	}

	@Test
	public void dontSetInnerFormSubmittedFlag()
	{
		page.outerForm.processChildren = false;
		tester.newFormTester("outerForm").submit();

		assertFalse(page.innerForm.isSubmittedFlagged);
	}

	@Test
	public void validateInnerFormField()
	{
		AlwaysFail validator = new AlwaysFail();
		page.innerField.add(validator);
		tester.newFormTester("outerForm").submit();

		assertTrue(validator.validatedCalled);
	}

	@Test
	public void dontValidateInnerFormFieldIfNotProcessChildren()
	{
		AlwaysFail validator = new AlwaysFail();
		page.innerField.add(validator);
		page.outerForm.processChildren = false;
		tester.newFormTester("outerForm").submit();

		assertFalse(validator.validatedCalled);
	}

	@Test
	public void dontValidateInnerFormFieldIfNotEnabled()
	{
		AlwaysFail validator = new AlwaysFail();
		page.innerField.add(validator);
		page.innerForm.setEnabled(false);
		tester.newFormTester("outerForm").submit();

		assertFalse(validator.validatedCalled);
	}

	@Test
	public void validateFormValidator()
	{
		FormValidator validator = new FormValidator(page.innerField);
		page.innerForm.add(validator);
		tester.newFormTester("outerForm").submit();

		assertTrue(validator.validatedCalled);
	}

	@Test
	public void dontValidateFormValidatorIfNotProcessChildren()
	{
		FormValidator validator = new FormValidator(page.innerField);
		page.innerForm.add(validator);
		page.outerForm.processChildren = false;
		tester.newFormTester("outerForm").submit();

		assertFalse(validator.validatedCalled);
	}

	@Test
	public void dontValidateFormValidatorIfNotProcessInnerFormChildren()
	{
		FormValidator validator = new FormValidator(page.innerField);
		page.innerForm.add(validator);
		page.innerForm.processChildren = false;
		tester.newFormTester("outerForm").submit();

		assertFalse(validator.validatedCalled);
	}

	@Test
	public void dontValidateFormValidatorIfInnerFormNotEnabled()
	{
		FormValidator validator = new FormValidator(page.innerField);
		page.innerForm.add(validator);
		page.innerForm.setEnabled(false);
		tester.newFormTester("outerForm").submit();

		assertFalse(validator.validatedCalled);
	}

	@Test
	public void dontValidateFormValidatorIfInnerContainerNotEnabled()
	{
		FormValidator validator = new FormValidator(page.innerField);
		page.innerForm.add(validator);
		page.innerContainer.setEnabled(false);
		tester.newFormTester("outerForm").submit();

		assertFalse(validator.validatedCalled);
	}

	@Test
	public void validateInnerFormComponent()
	{
		tester.newFormTester("outerForm").submit();

		assertTrue(page.innerField.onValidCalled);
	}

	@Test
	public void dontValidateInnerFormComponent()
	{
		page.outerForm.processChildren = false;
		tester.newFormTester("outerForm").submit();

		assertFalse(page.innerField.onValidCalled);
	}

	@Test
	public void validateFormComponentsInPostorder()
	{
		tester.newFormTester("outerForm").submit();

		assertTrue(page.innerField.onValidCallOrder < page.outerField.onValidCallOrder);
	}

	@Test
	public void updateInnerFormComponentModel()
	{
		tester.newFormTester("outerForm").submit();

		assertTrue(page.innerField.updateModelCalled);
	}

	@Test
	public void dontUpdateInnerFormComponentModel()
	{
		page.outerForm.processChildren = false;
		tester.newFormTester("outerForm").submit();

		assertFalse(page.innerField.updateModelCalled);
	}

	@Test
	public void updateFormComponentModelsInPostorder()
	{
		tester.newFormTester("outerForm").submit();

		assertTrue(page.innerField.updateModelOrder < page.outerField.updateModelOrder);
	}

	@Test
	public void callInnerFormOnValidateModelObjects()
	{
		tester.newFormTester("outerForm").submit();

		assertTrue(page.innerForm.onValidateModelObjectsCalled);
	}

	@Test
	public void dontCallInnerFormOnValidateModelObjects()
	{
		page.outerForm.processChildren = false;
		tester.newFormTester("outerForm").submit();

		assertFalse(page.innerForm.onValidateModelObjectsCalled);
	}

	@Test
	public void callFormOnValidateModelObjectsInPostorder()
	{
		tester.newFormTester("outerForm").submit();

		assertTrue(
			page.innerForm.onValidateModelObjectsCallOrder < page.outerForm.onValidateModelObjectsCallOrder);
	}

	public static class TestFormPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private TestForm outerForm;
		private TestForm innerForm;
		private TestField outerField;
		private TestField innerField;
		private WebMarkupContainer innerContainer;

		public TestFormPage()
		{
			add(outerForm = new TestForm("outerForm"));
			outerForm.add(innerForm = new TestForm("innerForm"));
			outerForm.add(outerField = new TestField("outerField"));
			innerForm.add(innerContainer = new WebMarkupContainer("innerContainer"));
			innerContainer.add(innerField = new TestField("innerField"));
		}

		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream("" //
				+ "<html><body>" //
				+ "  <form wicket:id=\"outerForm\">" //
				+ "    <input wicket:id=\"outerField\" />" //
				+ "    <form wicket:id=\"innerForm\">" //
				+ "      <div wicket:id=\"innerContainer\"><input wicket:id=\"innerField\" /></div>" //
				+ "    </form>" //
				+ "  </form>"//
				+ "</body></html>");
		}
	}

	static class TestForm extends Form<Void> implements IFormVisitorParticipant
	{
		boolean processChildren = true;
		boolean onValidateCalled;
		boolean onErrorCalled;
		boolean onSubmitCalled;
		boolean isSubmittedFlagged;
		boolean onValidateModelObjectsCalled;
		int onValidateModelObjectsCallOrder;

		public TestForm(String id)
		{
			super(id);
		}

		@Override
		protected void onSubmit()
		{
			onSubmitCalled = true;
		}

		@Override
		protected void onConfigure()
		{
			super.onConfigure();
			isSubmittedFlagged = isSubmitted();
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
		protected void onValidateModelObjects()
		{
			onValidateModelObjectsCalled = true;
			onValidateModelObjectsCallOrder = SEQUENCE++;
		}

		@Override
		protected void onError()
		{
			onErrorCalled = true;
		}
	}

	static class TestField extends TextField<String> implements IFormModelUpdateListener
	{

		boolean onValidCalled;
		boolean updateModelCalled;
		int updateModelOrder;
		int onValidCallOrder;

		public TestField(String id)
		{
			super(id, Model.of((String)null));
		}

		@Override
		protected void onValid()
		{
			onValidCalled = true;
			onValidCallOrder = SEQUENCE++;
		}

		@Override
		public void updateModel()
		{
			super.updateModel();
			updateModelCalled = true;
			updateModelOrder = SEQUENCE++;
		}
	}

	static class FormValidator implements IFormValidator
	{
		private FormComponent<?>[] dependencies;
		private boolean validatedCalled;

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
			validatedCalled = true;
		}

	}

	static class AlwaysFail implements IValidator<String>, INullAcceptingValidator<String>
	{
		boolean validatedCalled;

		@Override
		public void validate(IValidatable<String> validatable)
		{
			validatable.error(new ValidationError("foo"));
			validatedCalled = true;
		}
	}

}
