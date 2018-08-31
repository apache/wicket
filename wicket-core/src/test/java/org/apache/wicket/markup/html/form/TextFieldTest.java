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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.HTML5Attributes;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTestCase;
import org.apache.wicket.validation.validator.StringValidator;
import org.junit.jupiter.api.Test;

/**
 * @author Pedro Santos
 */
class TextFieldTest extends WicketTestCase
{
	/** */
	@Test
	void emptyInputConvertedToNull()
	{
		TestPage testPage = new TestPage();
		testPage.textField.setType(String.class);
		testPage.textField.setConvertEmptyInputStringToNull(true);
		tester.startPage(testPage);
		FormTester formTester = tester.newFormTester(testPage.form.getId());
		formTester.setValue(testPage.textField.getId(), "");
		formTester.submit();
		assertEquals(null, testPage.textField.getDefaultModelObject());
	}

	/**
	 * Asserting that the value attribute on tag input is escaped once by default
	 */
	@Test
	void valueAttribute()
	{
		TestPage testPage = new TestPage();
		String text = "some text & another text";
		testPage.textModel.setObject(text);
		tester.startPage(testPage);
		assertTrue(tester.getLastResponseAsString().contains(Strings.escapeMarkup(text)));
	}

	/**
	 * Assert that null input is not validated.
	 */
	@Test
	void nullIsNotValidated()
	{
		TestPage testPage = new TestPage();
		testPage.textField.setType(String.class);
		testPage.textField.setRequired(false);
		testPage.textField.add(StringValidator.minimumLength(2));
		tester.startPage(testPage);
		FormTester formTester = tester.newFormTester(testPage.form.getId());
		formTester.setValue(testPage.textField.getId(), "");
		formTester.submit();
		assertEquals(null, testPage.textField.getDefaultModelObject());
		assertTrue(testPage.textField.isValid());
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-5289
	 */
	@Test
	void requiredAttribute()
	{
		TestPage testPage = new TestPage();
		testPage.textField.setOutputMarkupId(true);
		testPage.textField.setType(String.class);
		testPage.textField.setRequired(true);
		testPage.textField.add(new HTML5Attributes());
		tester.startPage(testPage);

		TagTester tagTester = tester.getTagById(testPage.textField.getMarkupId());
		String required = tagTester.getAttribute("required");
		assertEquals("required", required);
	}

	/** */
	public static class TestPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;
		Form<Void> form;
		TextField<String> textField;
		IModel<String> textModel = Model.of((String)null);

		/** */
		TestPage()
		{
			add(form = new Form<>("form"));
			form.add(textField = new TextField<>("text", textModel));
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream("<html><body>"
				+ "<form wicket:id=\"form\"><input wicket:id=\"text\" /></form></body></html>");
		}
	}
}
