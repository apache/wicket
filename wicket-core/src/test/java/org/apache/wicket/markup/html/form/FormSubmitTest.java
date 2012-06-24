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
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.core.request.handler.ListenerInvocationNotAllowedException;
import org.apache.wicket.markup.html.form.NestedFormsPage.NestableForm;
import org.apache.wicket.util.tester.FormTester;
import org.junit.Before;
import org.junit.Test;

/**
 * Please see <a href="http://cwiki.apache.org/WICKET/nested-forms.html">"Nested Forms"</a> for more
 * details on nested forms
 * 
 * @author Gerolf Seitz
 */
public class FormSubmitTest extends WicketTestCase
{
	private NestedFormsPage page;
	private NestableForm outerForm;
	private NestableForm middleForm;
	private NestableForm innerForm;

	/**
	 *
	 */
	@Before
	public void before()
	{
		tester.startPage(new NestedFormsPage());
		page = (NestedFormsPage)tester.getLastRenderedPage();
		outerForm = (NestableForm)page.get("outerForm");
		middleForm = (NestableForm)page.get("outerForm:middleForm");
		innerForm = (NestableForm)page.get("outerForm:middleForm:innerForm");
	}

	/**
	 *
	 */
	@Test
	public void allFormsEnabledSubmitOuterForm()
	{
		assertEnabledState(true, true, true);

		FormTester formTester = tester.newFormTester("outerForm");
		formTester.submit("submit");

		assertOnSubmitCalled(true, true, true);
		assertOnErrorCalled(false, false, false);
		assertSubmitOrder(innerForm, middleForm, outerForm);
	}

	private void assertSubmitOrder(NestableForm... forms)
	{
		assertEquals("not submitted in expected order!", joinIds(forms), page.submitOrder);
	}

	private String joinIds(Component[] comps)
	{
		String result = "";
		for (Component component : comps)
		{
			result += component.getId();
		}
		return result;
	}

	private void assertErrorOrder(NestableForm... forms)
	{
		assertEquals("not onError'd in expected order!", joinIds(forms), page.errorOrder);
	}

	/**
	 *
	 */
	@Test
	public void allFormsEnabledSubmitMiddleForm()
	{
		assertEnabledState(true, true, true);

		FormTester formTester = tester.newFormTester("outerForm:middleForm");
		formTester.submit("submit");

		assertOnSubmitCalled(false, true, true);
		assertOnErrorCalled(false, false, false);
		assertSubmitOrder(innerForm, middleForm);
	}

	/**
	 *
	 */
	@Test
	public void allFormsEnabledSubmitInnerForm()
	{
		assertEnabledState(true, true, true);

		FormTester formTester = tester.newFormTester("outerForm:middleForm:innerForm");
		formTester.submit("submit");

		assertOnSubmitCalled(false, false, true);
		assertOnErrorCalled(false, false, false);
		assertSubmitOrder(innerForm);
	}

	/**
	 *
	 */
	@Test
	public void middleFormDisabledSubmitOuterForm()
	{
		// disable middle form
		middleForm.setEnabled(false);
		assertEnabledState(true, false, true);

		// submit outer form
		FormTester formTester = tester.newFormTester("outerForm");
		formTester.submit("submit");

		assertOnSubmitCalled(true, false, false);
		assertOnErrorCalled(false, false, false);
		assertSubmitOrder(outerForm);
	}

	/**
	 *
	 */
	@Test
	public void innerFormDisabledSubmitOuterForm()
	{
		// disable middle form
		innerForm.setEnabled(false);
		assertEnabledState(true, true, false);

		// submit outer form
		FormTester formTester = tester.newFormTester("outerForm");
		formTester.submit("submit");

		assertOnSubmitCalled(true, true, false);
		assertOnErrorCalled(false, false, false);
		assertSubmitOrder(middleForm, outerForm);
	}

	/**
	 *
	 */
	@Test
	public void submitDisabledOuterForm()
	{
		outerForm.setEnabled(false);
		assertEnabledState(false, true, true);

		FormTester formTester = tester.newFormTester("outerForm");
		try
		{
			formTester.submit("submit");
			fail("Executing the listener on disabled component is not allowed.");
		}
		catch (ListenerInvocationNotAllowedException expected)
		{
			;
		}
		assertOnSubmitCalled(false, false, false);
		assertOnErrorCalled(false, false, false);
	}

	/**
	 *
	 */
	@Test
	public void errorOnInnerFormSubmitOuterForm()
	{
		assertEnabledState(true, true, true);

		causeValidationErrorAndSubmit("outerForm", "middleForm:innerForm:first");

		assertOnSubmitCalled(false, false, false);
		assertOnErrorCalled(true, true, true);
		assertErrorOrder(innerForm, middleForm, outerForm);
	}

	/**
	 *
	 */
	@Test
	public void errorOnMiddleFormSubmitOuterForm()
	{
		assertEnabledState(true, true, true);

		causeValidationErrorAndSubmit("outerForm", "middleForm:first");

		assertOnSubmitCalled(false, false, false);
		assertOnErrorCalled(true, true, false);
		assertErrorOrder(middleForm, outerForm);
	}

	/**
	 *
	 */
	@Test
	public void errorOnMiddleFormSubmitMiddleForm()
	{
		assertEnabledState(true, true, true);

		causeValidationErrorAndSubmit("outerForm:middleForm", "first");

		assertOnSubmitCalled(false, false, false);
		assertOnErrorCalled(false, true, false);
		assertErrorOrder(middleForm);
	}

	/**
	 *
	 */
	@Test
	public void errorOnInnerFormSubmitMiddleForm()
	{
		assertEnabledState(true, true, true);

		causeValidationErrorAndSubmit("outerForm:middleForm", "innerForm:first");

		assertOnSubmitCalled(false, false, false);
		assertOnErrorCalled(false, true, true);
		assertErrorOrder(innerForm, middleForm);
	}

	/**
	 *
	 */
	@Test
	public void middleFormDisabledErrorOnOuterFormSubmitOuterForm()
	{
		middleForm.setEnabled(false);
		assertEnabledState(true, false, true);

		causeValidationErrorAndSubmit("outerForm", "first");

		assertOnSubmitCalled(false, false, false);
		assertOnErrorCalled(true, false, false);
		assertErrorOrder(outerForm);
	}

	/**
	 *
	 */
	@Test
	public void errorOnInnerFormDisabledMiddleFormSubmitOuterForm()
	{
		middleForm.setEnabled(false);
		assertEnabledState(true, false, true);

		causeValidationErrorAndSubmit("outerForm", "middleForm:innerForm:first");

		assertOnSubmitCalled(true, false, false);
		assertOnErrorCalled(false, false, false);
	}

	/**
	 * 
	 * @param isOuterFormEnabled
	 * @param isMiddleFormEnabled
	 * @param isInnerFormEnabled
	 */
	private void assertEnabledState(boolean isOuterFormEnabled, boolean isMiddleFormEnabled,
		boolean isInnerFormEnabled)
	{
		assertEquals(isOuterFormEnabled, outerForm.isEnabled());
		assertEquals(isMiddleFormEnabled, middleForm.isEnabled());
		assertEquals(isInnerFormEnabled, innerForm.isEnabled());
	}

	/**
	 * 
	 * @param isOuterFormOnErrorCalled
	 * @param isMiddleFormOnErrorCalled
	 * @param isInnerFormOnErrorCalled
	 */
	private void assertOnErrorCalled(boolean isOuterFormOnErrorCalled,
		boolean isMiddleFormOnErrorCalled, boolean isInnerFormOnErrorCalled)
	{
		assertEquals(isOuterFormOnErrorCalled, outerForm.onErrorCalled);
		assertEquals(isMiddleFormOnErrorCalled, middleForm.onErrorCalled);
		assertEquals(isInnerFormOnErrorCalled, innerForm.onErrorCalled);
	}

	/**
	 * 
	 * @param isOuterFormOnSubmitCalled
	 * @param isMiddleFormOnSubmitCalled
	 * @param isInnerFormOnSubmitCalled
	 */
	private void assertOnSubmitCalled(boolean isOuterFormOnSubmitCalled,
		boolean isMiddleFormOnSubmitCalled, boolean isInnerFormOnSubmitCalled)
	{
		assertEquals(isOuterFormOnSubmitCalled, outerForm.onSubmitCalled);
		assertEquals(isMiddleFormOnSubmitCalled, middleForm.onSubmitCalled);
		assertEquals(isInnerFormOnSubmitCalled, innerForm.onSubmitCalled);
	}

	/**
	 * @param formToBeSubmitted
	 *            absolute path of the form to be submitted
	 * @param componentToGetError
	 *            relative path to <code>formToBeSumitted</code> of the component to be changed
	 * @return a {@link FormTester} instance
	 */
	private FormTester causeValidationErrorAndSubmit(String formToBeSubmitted,
		String componentToGetError)
	{
		FormTester formTester;
		formTester = tester.newFormTester(formToBeSubmitted);
		formTester.setValue(componentToGetError, "");
		formTester.submit("submit");
		return formTester;
	}
}
