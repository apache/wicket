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

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.validation.IFormValidator;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTestCase;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitFilter;
import org.apache.wicket.util.visit.IVisitor;
import org.apache.wicket.util.visit.Visits;
import org.apache.wicket.validation.INullAcceptingValidator;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * <a href="https://issues.apache.org/jira/browse/WICKET-3899">WICKET-3899</a>
 *
 * @author Pedro Santos
 */
public class FormVisitTest extends WicketTestCase
{
	TestFormPage page;
	FormValidator formValidator = new FormValidator();
	AlwaysFail alwaysFail = new AlwaysFail();
	static int sequence;

	@BeforeEach
	public void initialize()
	{
		page = new TestFormPage();
		tester.startPage(page);
	}

	@Test
	public void processForms()
	{
		page.outerForm.add(formValidator);
		tester.newFormTester("outerForm").submit();

		assertTrue(page.outerForm.onValidateCalled);
		assertTrue(page.outerForm.onSubmitCalled);
		assertTrue(page.outerForm.isSubmittedFlagged);
		assertTrue(page.outerForm.onValidateModelObjectsCalled);
		assertTrue(page.outerField.onValidCalled);
		assertTrue(page.outerField.updateModelCalled);
		assertTrue(formValidator.validatedCalled);
		assertTrue(page.innerForm.onValidateCalled);
		assertTrue(page.innerForm.onSubmitCalled);
		assertTrue(page.innerForm.isSubmittedFlagged);
		assertTrue(page.innerForm.onValidateModelObjectsCalled);
		assertTrue(page.innerField.onValidCalled);
		assertTrue(page.innerField.updateModelCalled);
	}

	@Test
	public void processOuterFormOnly()
	{
		page.outerForm.add(formValidator);
		page.innerForm.wantSubmitOnParentFormSubmit = false;
		tester.newFormTester("outerForm").submit();

		assertTrue(page.outerForm.onValidateCalled);
		assertTrue(page.outerForm.onSubmitCalled);
		assertTrue(page.outerForm.isSubmittedFlagged);
		assertTrue(page.outerForm.onValidateModelObjectsCalled);
		assertTrue(page.outerField.onValidCalled);
		assertTrue(page.outerField.updateModelCalled);
		assertTrue(formValidator.validatedCalled);
		assertFalse(page.innerForm.onValidateCalled);
		assertFalse(page.innerForm.onSubmitCalled);
		assertFalse(page.innerForm.isSubmittedFlagged);
		assertFalse(page.innerForm.onValidateModelObjectsCalled);
		assertFalse(page.innerField.onValidCalled);
		assertFalse(page.innerField.updateModelCalled);
	}

	@Test
	@Disabled
	public void processInnerFormOnly()
	{
		page.outerForm.add(formValidator);
		tester.newFormTester("outerForm:outerContainer:innerForm").submit("innerContainer:innerFormSubmitButton");

		assertFalse(page.outerForm.onValidateCalled);
		assertFalse(page.outerForm.onSubmitCalled);
		assertFalse(page.outerForm.isSubmittedFlagged);
		assertFalse(page.outerForm.onValidateModelObjectsCalled);
		assertFalse(page.outerField.onValidCalled);
		assertFalse(page.outerField.updateModelCalled);
		assertFalse(formValidator.validatedCalled);
		assertTrue(page.innerForm.onValidateCalled);
		assertTrue(page.innerForm.onSubmitCalled);
		assertTrue(page.innerForm.isSubmittedFlagged);
		assertTrue(page.innerForm.onValidateModelObjectsCalled);
		assertTrue(page.innerField.onValidCalled);
		assertTrue(page.innerField.updateModelCalled);
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
		page.innerForm.wantSubmitOnParentFormSubmit = false;
		tester.newFormTester("outerForm").submit();
		assertFalse(page.innerForm.onValidateCalled);
	}

	@Test
	public void callInnerFormOnError()
	{
		page.innerField.add(alwaysFail);
		tester.newFormTester("outerForm").submit();

		assertTrue(page.innerForm.onErrorCalled);
	}

	@Test
	public void dontCallInnerFormOnErrorIfNotProcessChildren()
	{
		page.innerField.add(alwaysFail);
		page.innerForm.wantSubmitOnParentFormSubmit = false;
		tester.newFormTester("outerForm").submit();

		assertFalse(page.innerForm.onErrorCalled);
	}

	@Test
	public void dontCallInnerFormOnErrorIfNotEnabled()
	{
		page.innerField.add(alwaysFail);
		page.innerForm.setEnabled(false);
		tester.newFormTester("outerForm").submit();

		assertFalse(page.innerForm.onErrorCalled);
	}

	@Test
	public void dontCallOuterFormOnErrorTwice()
	{
		page.outerField.add(alwaysFail);
		tester.newFormTester("outerForm").submit();

		assertEquals(1, page.outerForm.numberOfOnErrorCalls);
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
		page.innerForm.wantSubmitOnParentFormSubmit = false;
		tester.newFormTester("outerForm").submit();

		assertFalse(page.innerForm.onSubmitCalled);
	}

	@Test
	public void setInnerFormSubmittedFlag()
	{
		tester.newFormTester("outerForm").submit();

		assertTrue(page.innerForm.isSubmittedFlagged);
	}

	@Test
	public void dontSetInnerFormSubmittedFlag()
	{
		page.innerForm.wantSubmitOnParentFormSubmit = false;
		tester.newFormTester("outerForm").submit();

		assertFalse(page.innerForm.isSubmittedFlagged);
	}

	@Test
	public void validateInnerFormField()
	{
		page.innerField.add(alwaysFail);
		tester.newFormTester("outerForm").submit();

		assertTrue(alwaysFail.validatedCalled);
	}

	@Test
	public void dontValidateInnerFormFieldIfNotProcessChildren()
	{
		page.innerField.add(alwaysFail);
		page.innerForm.wantSubmitOnParentFormSubmit = false;
		tester.newFormTester("outerForm").submit();

		assertFalse(alwaysFail.validatedCalled);
	}

	@Test
	public void dontValidateInnerFormFieldIfNotEnabled()
	{
		page.innerField.add(alwaysFail);
		page.innerForm.setEnabled(false);
		tester.newFormTester("outerForm").submit();

		assertFalse(alwaysFail.validatedCalled);
	}

	@Test
	public void validateFormValidator()
	{
		page.innerForm.add(formValidator);
		tester.newFormTester("outerForm").submit();

		assertTrue(formValidator.validatedCalled);
	}

	@Test
	public void dontValidateFormValidatorIfFormNotEnabled()
	{
		page.innerForm.add(formValidator);
		page.innerForm.setEnabled(false);
		tester.newFormTester("outerForm").submit();

		assertFalse(formValidator.validatedCalled);
	}

	@Test
	public void dontValidateFormValidatorIfFormNotProcessed()
	{
		page.innerForm.add(formValidator);
		page.innerForm.wantSubmitOnParentFormSubmit = false;
		tester.newFormTester("outerForm").submit();

		assertFalse(formValidator.validatedCalled);
	}

	@Test
	public void dontValidateFormValidatorIfDependentNotEnabled()
	{
		page.innerForm.add(formValidator.setDependency(page.innerField));
		page.innerContainer.setEnabled(false);
		tester.newFormTester("outerForm").submit();

		assertFalse(formValidator.validatedCalled);
	}

	@Test
	public void dontValidateFormValidatorIfDependentNotProcessed()
	{
		page.outerForm.add(formValidator.setDependency(page.innerField));
		page.innerForm.wantSubmitOnParentFormSubmit = false;
		tester.newFormTester("outerForm").submit();

		assertFalse(formValidator.validatedCalled);
	}

	@Test
	public void validateFormValidatorIfDependentIsParticipant()
	{
		page.outerForm.add(formValidator.setDependency(page.innerField));
		// currently this flag affects just how form components are
		// visited inside their form, the test just reflects this expectation
		page.outerContainer.processChildren = false;
		tester.newFormTester("outerForm").submit();

		assertTrue(formValidator.validatedCalled);
	}

	@Test
	public void dontValidateFormValidatorIfDependentNorParticipant()
	{
		page.outerForm.add(formValidator.setDependency(page.innerField));
		page.innerContainer.processChildren = false;
		tester.newFormTester("outerForm").submit();

		assertFalse(formValidator.validatedCalled);
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
		page.innerForm.wantSubmitOnParentFormSubmit = false;
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
		page.innerForm.wantSubmitOnParentFormSubmit = false;
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
		page.innerForm.wantSubmitOnParentFormSubmit = false;
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
		TestForm outerForm;
		TestForm innerForm;
		TestField outerField;
		TestField innerField;
		Button innerFormSubmitButton;
		Container outerContainer;
		Container innerContainer;

		public TestFormPage()
		{
			add(outerForm = new TestForm("outerForm"));
			outerForm.add(outerContainer = new Container("outerContainer"));
			outerContainer.add(outerField = new TestField("outerField"));
			outerContainer.add(innerForm = new TestForm("innerForm"));
			innerForm.add(innerContainer = new Container("innerContainer"));
			innerContainer.add(innerField = new TestField("innerField"));
			innerContainer.add(innerFormSubmitButton = new Button("innerFormSubmitButton"));
		}

		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream("" //
				+ "<html><body>" //
				+ "  <form wicket:id=\"outerForm\">" //
				+ "    <div wicket:id=\"outerContainer\">" //
				+ "      <input wicket:id=\"outerField\" />" //
				+ "        <form wicket:id=\"innerForm\">" //
				+ "          <div wicket:id=\"innerContainer\"><input wicket:id=\"innerField\" /><button wicket:id=\"innerFormSubmitButton\" type=\"submit\"></button></div>" //
				+ "        </form>" //
				+ "    </div>" //
				+ "  </form>"//
				+ "</body></html>");
		}
	}

	static class TestForm extends Form<Void>
	{
		boolean wantSubmitOnParentFormSubmit = true;
		boolean onValidateCalled;
		boolean onErrorCalled;
		boolean onSubmitCalled;
		boolean isSubmittedFlagged;
		boolean onValidateModelObjectsCalled;
		int onValidateModelObjectsCallOrder;
		int numberOfOnErrorCalls;

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
		protected boolean wantSubmitOnParentFormSubmit()
		{
			return wantSubmitOnParentFormSubmit;
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
			onValidateModelObjectsCallOrder = sequence++;
		}

		@Override
		protected void onError()
		{
			onErrorCalled = true;
			numberOfOnErrorCalls++;
		}
	}

	static class Container extends WebMarkupContainer implements IFormVisitorParticipant
	{
		boolean processChildren = true;

		public Container(final String id)
		{
			super(id);
		}

		@Override
		public boolean processChildren()
		{
			return processChildren;
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
			onValidCallOrder = sequence++;
		}

		@Override
		public void updateModel()
		{
			super.updateModel();
			updateModelCalled = true;
			updateModelOrder = sequence++;
		}
	}

	static class FormValidator implements IFormValidator
	{
		FormComponent<?>[] dependencies;
		boolean validatedCalled;

		public FormComponent<?>[] getDependentFormComponents()
		{
			return dependencies;
		}

		public void validate(Form<?> form)
		{
			validatedCalled = true;
		}

		public FormValidator setDependency(FormComponent component)
		{
			this.dependencies = new FormComponent<?>[] { component };
			return this;
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
