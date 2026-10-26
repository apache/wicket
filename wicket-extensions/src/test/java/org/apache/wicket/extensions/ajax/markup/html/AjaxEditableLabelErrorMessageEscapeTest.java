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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Locale;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AbstractAjaxBehavior;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.model.Model;
import org.apache.wicket.resource.loader.IStringResourceLoader;
import org.apache.wicket.util.tester.WicketTestCase;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;
import org.junit.jupiter.api.Test;

/**
 * The error message shown by {@link AjaxEditableLabel#onError} is placed into a single-quoted
 * JavaScript string ({@code window.status='...'}). A message that carries a backslash in front of
 * a quote must not be able to close that string, which requires the backslash to be escaped too.
 */
class AjaxEditableLabelErrorMessageEscapeTest extends WicketTestCase
{
	/**
	 * A validation message that begins with a backslash followed by a single quote. The user's
	 * input is routinely echoed into validation messages through the {@code ${input}} variable,
	 * so these characters are attacker controlled.
	 */
	private static final String ATTACK = "x\\';BREAKOUT";

	@Test
	void onErrorEscapesBackslashSoTheJsStringCannotBeClosed()
	{
		AjaxEditableLabel<String> label = new AjaxEditableLabel<>("label", Model.of("value"));
		label.add((IValidator<String>)validatable -> validatable
			.error(new ValidationError().setMessage(ATTACK)));

		String js = submitInvalidValue(label, "anything");

		// escaped backslash + unescaped quote (\\') would close window.status='...' and run the rest
		assertFalse(js.contains("x\\\\';BREAKOUT"), js);
		// both the backslash and the quote are escaped (\\\')
		assertTrue(js.contains("x\\\\\\';BREAKOUT"), js);
	}

	@Test
	void onErrorEscapesUserInputEchoedThroughInputVariable()
	{
		tester.getApplication()
			.getResourceSettings()
			.getStringResourceLoaders()
			.add(0, new InvalidValueMessageLoader());

		AjaxEditableLabel<String> label = new AjaxEditableLabel<>("label", Model.of("value"));
		label.add((IValidator<String>)validatable -> validatable
			.error(new ValidationError().addKey(InvalidValueMessageLoader.KEY)));

		String js = submitInvalidValue(label, "\\';alert(1)//");

		assertFalse(js.contains("Invalid value: \\\\';alert(1)//"), js);
		assertTrue(js.contains("Invalid value: \\\\\\';alert(1)//"), js);
	}

	private String submitInvalidValue(AjaxEditableLabel<String> label, String value)
	{
		tester.startComponentInPage(label);

		// click the label to switch into edit mode
		AbstractAjaxBehavior labelBehavior = (AbstractAjaxBehavior)label.get("label")
			.getBehaviors()
			.get(0);
		tester.executeBehavior(labelBehavior);

		// submit a value, which fails validation and runs onError
		FormComponent<?> editor = (FormComponent<?>)label.get("editor");
		tester.getRequest().setParameter(editor.getInputName(), value);
		tester.getRequest().setParameter("save", "true");
		tester.getRequest().setMethod(Form.METHOD_GET);
		AbstractAjaxBehavior editorBehavior = (AbstractAjaxBehavior)editor.getBehaviors().get(0);
		tester.executeBehavior(editorBehavior);

		return tester.getLastResponse().getDocument();
	}

	/**
	 * Supplies a validation message that echoes the submitted value, as an application's own
	 * properties file would.
	 */
	private static class InvalidValueMessageLoader implements IStringResourceLoader
	{
		private static final String KEY = "InvalidValue";

		@Override
		public String loadStringResource(Class<?> clazz, String key, Locale locale, String style,
			String variation)
		{
			return KEY.equals(key) ? "Invalid value: ${input}" : null;
		}

		@Override
		public String loadStringResource(Component component, String key, Locale locale,
			String style, String variation)
		{
			return loadStringResource((Class<?>)null, key, locale, style, variation);
		}
	}
}
