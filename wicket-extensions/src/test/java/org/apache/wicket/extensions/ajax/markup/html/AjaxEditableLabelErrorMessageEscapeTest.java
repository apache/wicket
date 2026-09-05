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
package org.apache.wicket.extensions.ajax.markup.html;

import org.apache.wicket.behavior.AbstractAjaxBehavior;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.WicketTestCase;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link AjaxEditableLabel#onError(org.apache.wicket.ajax.AjaxRequestTarget)} writes the editor's
 * first error message into a single quoted JavaScript string literal, and a validation message may
 * carry the value the user submitted.
 *
 * @author Pedro Santos
 */
class AjaxEditableLabelErrorMessageEscapeTest extends WicketTestCase
{
	/**
	 * The backslash in front of the quote used to escape the backslash the escaper put there,
	 * leaving the quote free to close the window.status literal.
	 */
	private static final String BACKSLASH_BEFORE_QUOTE = "a\\';b";

	@Test
	void errorMessageStaysInsideItsJavaScriptStringLiteral()
	{
		AjaxEditableLabel<String> editableLabel = new AjaxEditableLabel<>("editableLabel",
			new Model<>("initial"));
		editableLabel.add(new IValidator<String>()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void validate(IValidatable<String> validatable)
			{
				validatable.error(
					new ValidationError().setMessage("rejected: " + validatable.getValue()));
			}
		});

		tester.startComponentInPage(editableLabel);

		// "click" the label to get into edit mode
		tester.executeAjaxEvent(editableLabel.get("label"), "click");

		// submit a value the validator will reject
		FormComponent<?> editor = (FormComponent<?>)editableLabel.get("editor");
		tester.getRequest().setParameter(editor.getInputName(), BACKSLASH_BEFORE_QUOTE);
		tester.getRequest().setParameter("save", "true");
		tester.getRequest().setMethod(Form.METHOD_GET);
		tester.executeBehavior((AbstractAjaxBehavior)editor.getBehaviorById(0));

		String response = tester.getLastResponseAsString();
		assertTrue(response.contains("a\\\\\\';b"), "no error message was reported");
	}
}
