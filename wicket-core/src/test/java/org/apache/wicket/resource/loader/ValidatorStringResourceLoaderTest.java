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
package org.apache.wicket.resource.loader;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.validation.IFormValidator;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.lang.Objects;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;
import org.apache.wicket.validation.validator.AbstractValidator;
import org.junit.Test;

/**
 * Tests for ValidatorStringResourceLoader
 *
 * @since 1.5.5
 */
public class ValidatorStringResourceLoaderTest extends WicketTestCase
{
	/**
	 * Tests that resource bundle is properly loaded for implementations of
	 * IValidator need to be wrapped in ValidatorAdapter
	 *
	 * https://issues.apache.org/jira/browse/WICKET-4379
	 */
	@Test
	public void interfaceValidator()
	{
		tester.startPage(new ValidatorLoaderPage(new InterfaceValidator()));
		FormTester formTester = tester.newFormTester("form");
		formTester.setValue("passwd", "anything");
		formTester.submit();

		tester.assertErrorMessages("Interface error loaded OK");
	}

	/**
	 * Tests that resource bundle is properly loaded for implementations of
	 * AbstractValidator
	 *
	 * https://issues.apache.org/jira/browse/WICKET-4379
	 */
	@Test
	public void classValidator()
	{
		tester.startPage(new ValidatorLoaderPage(new ClassValidator()));
		FormTester formTester = tester.newFormTester("form");
		formTester.setValue("passwd", "anything");
		formTester.submit();

		tester.assertErrorMessages("Class error loaded OK");
	}
	
	@Test
	public void formValidator()
	{
		tester.startPage(new FormValidatorPage());
		FormTester formTester = tester.newFormTester("form");
		formTester.setValue("field1", "value1");
		formTester.setValue("field2", "value2");
		formTester.submit();
		tester.assertErrorMessages("Form Validator loaded OK");
	}
	
	private static class ValidatorLoaderPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private ValidatorLoaderPage(IValidator<String> validator)
		{
			Form<Void> form = new Form<Void>("form");
			add(form);

			PasswordTextField passwordTextField = new PasswordTextField("passwd", Model.of(""));
			form.add(passwordTextField);
			passwordTextField.add(validator);
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container, Class<?> containerClass)
		{
			return new StringResourceStream("<html><body><form wicket:id='form'><input type='password' wicket:id='passwd' /></form></body></html>");
		}
	}

	private static class ClassValidator extends AbstractValidator<String>
	{
		@Override
		protected void onValidate(IValidatable<String> validatable)
		{
			ValidationError error = new ValidationError();
			error.addKey("testError");
			validatable.error(error);
		}
	}

	private static class InterfaceValidator implements IValidator<String>
	{
		@Override
		public void validate(IValidatable<String> validatable)
		{
			ValidationError error = new ValidationError();
			error.addKey("testError");
			validatable.error(error);
		}
	}
	
	private static class FormValidatorPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private FormValidatorPage()
		{
			FormValidatorEntity entity = new FormValidatorEntity();
			CompoundPropertyModel<FormValidatorEntity> model = new CompoundPropertyModel<FormValidatorEntity>(entity);
			Form<FormValidatorEntity> form = new Form<FormValidatorEntity>("form", model);
			add(form);

			TextField<String> field1 = new TextField<String>("field1");
			TextField<String> field2 = new TextField<String>("field2");
			form.add(field1, field2);
			
			form.add(new FormValidator(field1, field2));
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container, Class<?> containerClass)
		{
			return new StringResourceStream("<html><body><form wicket:id='form'><input wicket:id='field1'/><input wicket:id='field2'/></form></body></html>");
		}
	}
	
	private static class FormValidator implements IFormValidator
	{
		private final FormComponent<?> fc1;
		private final FormComponent<?> fc2;

		private FormValidator(FormComponent<?> fc1, FormComponent<?> fc2)
		{
			this.fc1 = fc1;
			this.fc2 = fc2;
		}
		
		@Override
		public FormComponent<?>[] getDependentFormComponents()
		{
			return new FormComponent<?>[] {fc1, fc2};
		}

		@Override
		public void validate(Form<?> form)
		{
			if (Objects.equal(fc1.getRawInput(), fc2.getRawInput()) == false)
			{
				form.error(form.getString("formValidatorFailed"));
			}
		}
	}
	
	private static class FormValidatorEntity
	{
		private String field1;
		private String field2;
	}
}
