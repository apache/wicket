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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.authorization.Action;
import org.apache.wicket.authorization.IAuthorizationStrategy;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTestCase;
import org.apache.wicket.validation.INullAcceptingValidator;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;
import org.junit.jupiter.api.Test;

/**
 * A form submitting component that is not visible or not enabled in its hierarchy must never be
 * invoked, no matter that the client names it in the request. This is the only place where the
 * {@link Action#RENDER} and {@link Action#ENABLE} decisions of an
 * {@link IAuthorizationStrategy} are enforced for a submit button, so a component the application
 * has restricted must not reach {@link IFormSubmitter#onSubmit()},
 * {@link IFormSubmitter#onAfterSubmit()} or {@link IFormSubmitter#onError()}.
 */
class FormSubmitterNotAllowedTest extends WicketTestCase
{
	/**
	 * Denies {@code action} for the component with the given id, and allows everything else.
	 */
	private void deny(final Action action, final String componentId)
	{
		tester.getApplication()
			.getSecuritySettings()
			.setAuthorizationStrategy(new IAuthorizationStrategy.AllowAllAuthorizationStrategy()
			{
				@Override
				public boolean isActionAuthorized(Component component, Action a)
				{
					return !(action.equals(a) && componentId.equals(component.getId()));
				}
			});
	}

	/**
	 * Names {@code component} as the submitting component of the next request, the way a browser
	 * does for the button that was clicked.
	 */
	private void nameAsSubmitter(FormComponent<?> component)
	{
		tester.getRequest()
			.getPostParameters()
			.setParameterValue(component.getInputName(), "x");
	}

	/**
	 * A button the application restricted with {@link Action#ENABLE} renders disabled, so its
	 * input name is in the markup the client received. Naming it anyway must not invoke it.
	 */
	@Test
	void enableDeniedSubmitterIsNotInvoked()
	{
		TestPage page = new TestPage();
		deny(Component.ENABLE, "restricted");
		tester.startPage(page);
		tester.assertRenderedPage(TestPage.class);
		assertTrue(tester.getLastResponseAsString().contains("disabled=\"disabled\""));

		nameAsSubmitter(page.restricted);
		WicketRuntimeException e = assertThrows(WicketRuntimeException.class,
			() -> tester.submitForm(page.form));

		assertTrue(e.getMessage().contains("is not enabled"), e.getMessage());
		assertFalse(page.restrictedSubmitted);
		assertFalse(page.formSubmitted);
	}

	/**
	 * A button the application restricted with {@link Action#RENDER} is absent from the markup, so
	 * its input name has to be guessed. Guessing it correctly must not invoke it.
	 */
	@Test
	void renderDeniedSubmitterIsNotInvoked()
	{
		TestPage page = new TestPage();
		deny(Component.RENDER, "restricted");
		tester.startPage(page);
		tester.assertRenderedPage(TestPage.class);
		assertFalse(tester.getLastResponseAsString().contains("wicket:id=\"restricted\""));

		nameAsSubmitter(page.restricted);
		WicketRuntimeException e = assertThrows(WicketRuntimeException.class,
			() -> tester.submitForm(page.form));

		assertTrue(e.getMessage().contains("is not visible"), e.getMessage());
		assertFalse(page.restrictedSubmitted);
		assertFalse(page.formSubmitted);
	}

	/**
	 * The same guard protects a plainly invisible submitter, with no authorization strategy
	 * involved at all.
	 */
	@Test
	void invisibleSubmitterIsNotInvoked()
	{
		TestPage page = new TestPage();
		tester.startPage(page);
		page.restricted.setVisible(false);

		nameAsSubmitter(page.restricted);
		WicketRuntimeException e = assertThrows(WicketRuntimeException.class,
			() -> tester.submitForm(page.form));

		assertTrue(e.getMessage().contains("is not visible"), e.getMessage());
		assertFalse(page.restrictedSubmitted);
		assertFalse(page.formSubmitted);
	}

	/**
	 * The same guard protects a plainly disabled submitter, with no authorization strategy
	 * involved at all.
	 */
	@Test
	void disabledSubmitterIsNotInvoked()
	{
		TestPage page = new TestPage();
		tester.startPage(page);
		page.restricted.setEnabled(false);

		nameAsSubmitter(page.restricted);
		WicketRuntimeException e = assertThrows(WicketRuntimeException.class,
			() -> tester.submitForm(page.form));

		assertTrue(e.getMessage().contains("is not enabled"), e.getMessage());
		assertFalse(page.restrictedSubmitted);
		assertFalse(page.formSubmitted);
	}

	/**
	 * A submitter that does not want the form processed reaches onSubmit() directly, bypassing
	 * validation, so it needs the guard just as much.
	 */
	@Test
	void disabledSubmitterWithoutDefaultFormProcessingIsNotInvoked()
	{
		TestPage page = new TestPage();
		tester.startPage(page);
		page.restricted.setDefaultFormProcessing(false);
		page.restricted.setEnabled(false);

		nameAsSubmitter(page.restricted);
		WicketRuntimeException e = assertThrows(WicketRuntimeException.class,
			() -> tester.submitForm(page.form));

		assertTrue(e.getMessage().contains("is not enabled"), e.getMessage());
		assertFalse(page.restrictedSubmitted);
	}

	/**
	 * onError() is a callback on the submitting component too, so a submitter that fails the guard
	 * must not receive it when validation fails.
	 */
	@Test
	void disabledSubmitterDoesNotReceiveOnError()
	{
		TestPage page = new TestPage();
		page.text.add(new AlwaysFail());
		tester.startPage(page);
		page.restricted.setEnabled(false);

		tester.getRequest().getPostParameters().setParameterValue(page.text.getInputName(), "x");
		nameAsSubmitter(page.restricted);
		WicketRuntimeException e = assertThrows(WicketRuntimeException.class,
			() -> tester.submitForm(page.form));

		assertTrue(e.getMessage().contains("is not enabled"), e.getMessage());
		assertFalse(page.restrictedErrored);
		assertFalse(page.restrictedSubmitted);
	}

	/**
	 * Control: restricting one button leaves the others alone.
	 */
	@Test
	void allowedSubmitterIsStillInvoked()
	{
		TestPage page = new TestPage();
		deny(Component.ENABLE, "restricted");
		tester.startPage(page);

		tester.newFormTester("form").submit("allowed");

		assertTrue(page.allowedSubmitted);
		assertTrue(page.formSubmitted);
		assertFalse(page.restrictedSubmitted);
	}

	/**
	 * Control: a submission that names no submitting component is unaffected.
	 */
	@Test
	void submissionWithoutSubmitterIsStillProcessed()
	{
		TestPage page = new TestPage();
		tester.startPage(page);

		tester.newFormTester("form").submit();

		assertTrue(page.formSubmitted);
		assertFalse(page.restrictedSubmitted);
		assertFalse(page.allowedSubmitted);
	}

	/**
	 * Control: an Ajax submit is invoked as a behaviour on the button itself, which
	 * {@code ListenerRequestHandler} has already authorized, and must keep working.
	 */
	@Test
	void ajaxSubmitterIsStillInvoked()
	{
		TestPage page = new TestPage();
		tester.startPage(page);

		tester.executeAjaxEvent(page.ajax, "click");

		assertTrue(page.ajaxSubmitted);
		assertTrue(page.formSubmitted);
	}

	/**
	 * An {@link AjaxButton} is a submitting component like any other, so a plain form post naming
	 * a disabled one must be rejected as well.
	 */
	@Test
	void disabledAjaxSubmitterIsNotInvokedByPlainSubmit()
	{
		TestPage page = new TestPage();
		tester.startPage(page);
		page.ajax.setEnabled(false);

		nameAsSubmitter(page.ajax);
		WicketRuntimeException e = assertThrows(WicketRuntimeException.class,
			() -> tester.submitForm(page.form));

		assertTrue(e.getMessage().contains("is not enabled"), e.getMessage());
		assertFalse(page.formSubmitted);
	}

	/**
	 * The submitter of a nested form is resolved from the root form, so the guard has to hold
	 * there too.
	 */
	@Test
	void disabledSubmitterOfNestedFormIsNotInvoked()
	{
		NestedTestPage page = new NestedTestPage();
		tester.startPage(page);
		page.innerSubmit.setEnabled(false);

		nameAsSubmitter(page.innerSubmit);
		WicketRuntimeException e = assertThrows(WicketRuntimeException.class,
			() -> tester.submitForm(page.outerForm));

		assertTrue(e.getMessage().contains("is not enabled"), e.getMessage());
		assertFalse(page.innerSubmitted);
		assertFalse(page.innerFormSubmitted);
		assertFalse(page.outerFormSubmitted);
	}

	/**
	 * Whether the submitting component can be resolved before or after the multipart body has been
	 * parsed is a property of the servlet container, so the guard must not depend on it.
	 */
	@Test
	void disabledSubmitterOfMultiPartFormIsNotInvoked()
	{
		MultiPartTestPage page = new MultiPartTestPage();
		tester.startPage(page);
		assertTrue(page.form.isMultiPart());
		page.restricted.setEnabled(false);

		WicketRuntimeException e = assertThrows(WicketRuntimeException.class,
			() -> tester.newFormTester("form").submit("restricted"));

		assertTrue(e.getMessage().contains("is not enabled"), e.getMessage());
		assertFalse(page.restrictedSubmitted);
		assertFalse(page.formSubmitted);
	}

	/** */
	public static class TestPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		Form<Void> form;
		TextField<String> text;
		Button restricted;
		Button allowed;
		AjaxButton ajax;

		boolean formSubmitted;
		boolean restrictedSubmitted;
		boolean restrictedErrored;
		boolean allowedSubmitted;
		boolean ajaxSubmitted;

		TestPage()
		{
			add(form = new Form<Void>("form")
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected void onSubmit()
				{
					formSubmitted = true;
				}
			});
			form.add(text = new TextField<>("text", Model.of("")));
			form.add(restricted = new Button("restricted")
			{
				private static final long serialVersionUID = 1L;

				@Override
				public void onSubmit()
				{
					restrictedSubmitted = true;
				}

				@Override
				public void onError()
				{
					restrictedErrored = true;
				}
			});
			form.add(allowed = new Button("allowed")
			{
				private static final long serialVersionUID = 1L;

				@Override
				public void onSubmit()
				{
					allowedSubmitted = true;
				}
			});
			form.add(ajax = new AjaxButton("ajax")
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected void onSubmit(AjaxRequestTarget target)
				{
					ajaxSubmitted = true;
				}
			});
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream("<html><body>" //
				+ "<form wicket:id=\"form\">" //
				+ "<input type=\"text\" wicket:id=\"text\"/>" //
				+ "<button wicket:id=\"restricted\"></button>" //
				+ "<button wicket:id=\"allowed\"></button>" //
				+ "<button wicket:id=\"ajax\"></button>" //
				+ "</form></body></html>");
		}
	}

	/** */
	public static class NestedTestPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		Form<Void> outerForm;
		Form<Void> innerForm;
		Button innerSubmit;

		boolean outerFormSubmitted;
		boolean innerFormSubmitted;
		boolean innerSubmitted;

		NestedTestPage()
		{
			add(outerForm = new Form<Void>("outerForm")
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected void onSubmit()
				{
					outerFormSubmitted = true;
				}
			});
			outerForm.add(innerForm = new Form<Void>("innerForm")
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected void onSubmit()
				{
					innerFormSubmitted = true;
				}
			});
			innerForm.add(innerSubmit = new Button("innerSubmit")
			{
				private static final long serialVersionUID = 1L;

				@Override
				public void onSubmit()
				{
					innerSubmitted = true;
				}
			});
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream("<html><body>" //
				+ "<form wicket:id=\"outerForm\">" //
				+ "<form wicket:id=\"innerForm\">" //
				+ "<button wicket:id=\"innerSubmit\"></button>" //
				+ "</form></form></body></html>");
		}
	}

	/** */
	public static class MultiPartTestPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		Form<Void> form;
		Button restricted;

		boolean formSubmitted;
		boolean restrictedSubmitted;

		MultiPartTestPage()
		{
			add(form = new Form<Void>("form")
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected void onSubmit()
				{
					formSubmitted = true;
				}
			});
			form.add(new FileUploadField("upload"));
			form.add(restricted = new Button("restricted")
			{
				private static final long serialVersionUID = 1L;

				@Override
				public void onSubmit()
				{
					restrictedSubmitted = true;
				}
			});
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream("<html><body>" //
				+ "<form wicket:id=\"form\">" //
				+ "<input type=\"file\" wicket:id=\"upload\"/>" //
				+ "<button wicket:id=\"restricted\"></button>" //
				+ "</form></body></html>");
		}
	}

	private static class AlwaysFail implements IValidator<String>, INullAcceptingValidator<String>
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void validate(IValidatable<String> validatable)
		{
			validatable.error(new ValidationError("always fails"));
		}
	}
}
