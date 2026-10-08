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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

/**
 * The body of the default ("null") option of a single select choice is escaped the same way the body
 * of every other option is. The display value is looked up by an overridable method, so it is not
 * necessarily the plain text from a resource bundle that the default implementation returns.
 */
class AbstractSingleSelectChoiceEscapeTest extends WicketTestCase
{
	private static final String MARKUP = "<script>x=1</script>";

	private static final String ESCAPED = "&lt;script&gt;x=1&lt;/script&gt;";

	/**
	 * A choice whose null display values come from somewhere other than a bundle, as an application
	 * override does when it shows the field's label instead of the generic text.
	 */
	private static class NullDisplayValueChoice extends DropDownChoice<String>
	{
		private static final long serialVersionUID = 1L;

		private final String displayValue;

		/** What the hooks handed back, to show escaping did not happen inside them. */
		private String observedDisplayValue;

		private NullDisplayValueChoice(String id, String displayValue)
		{
			super(id, Model.of((String)null), Arrays.asList("A", "B"));
			this.displayValue = displayValue;
		}

		@Override
		protected String getNullValidDisplayValue()
		{
			observedDisplayValue = displayValue;
			return displayValue;
		}

		@Override
		protected String getNullKeyDisplayValue()
		{
			observedDisplayValue = displayValue;
			return displayValue;
		}

		@Override
		protected CharSequence escapeOptionHtml(String value)
		{
			return "[" + super.escapeOptionHtml(value) + "]";
		}
	}

	private static class ChoicePage extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		private final NullDisplayValueChoice choice;

		private ChoicePage(String displayValue, boolean nullValid)
		{
			Form<Void> form = new Form<>("form");
			add(form);
			choice = new NullDisplayValueChoice("dropdown", displayValue);
			choice.setNullValid(nullValid);
			form.add(choice);
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream(
				"<html><body><form wicket:id=\"form\"><select wicket:id=\"dropdown\"></select>"
					+ "</form></body></html>");
		}
	}

	/** {@code nullValid} is true, so the "nullValid" display value is rendered. */
	@Test
	void nullValidDisplayValueIsEscaped()
	{
		tester.startPage(new ChoicePage(MARKUP, true));

		String response = tester.getLastResponseAsString();
		assertTrue(response.contains(ESCAPED), "the default option body should be escaped");
		assertFalse(response.contains(MARKUP),
			"the default option body should not reach the markup as markup");
	}

	/** {@code nullValid} is false and nothing is selected, so the "null" display value is used. */
	@Test
	void nullKeyDisplayValueIsEscaped()
	{
		tester.startPage(new ChoicePage(MARKUP, false));

		String response = tester.getLastResponseAsString();
		assertTrue(response.contains(ESCAPED), "the default option body should be escaped");
		assertFalse(response.contains(MARKUP),
			"the default option body should not reach the markup as markup");
	}

	/**
	 * The escaping follows escapeModelStrings, like the other options, so an application that means
	 * to put markup in the default option can still do so.
	 */
	@Test
	void defaultOptionIsNotEscapedWhenEscapeModelStringsIsFalse()
	{
		ChoicePage page = new ChoicePage(MARKUP, true);
		page.choice.setEscapeModelStrings(false);
		tester.startPage(page);

		assertTrue(tester.getLastResponseAsString().contains(MARKUP),
			"escaping is off, so the default option body should be written as is");
	}

	/**
	 * The escape goes through {@code escapeOptionHtml}, so an application that customised the
	 * escaping of its options gets it applied to the default option too.
	 */
	@Test
	void escapeOptionHtmlIsUsedForTheDefaultOption()
	{
		tester.startPage(new ChoicePage(MARKUP, true));

		// the marker the override adds has to show up in the body of the default option itself.
		// Asserting that the method was called says nothing, it is called for every other option
		assertTrue(
			tester.getLastResponseAsString().contains("value=\"\">[" + ESCAPED + "]</option>"),
			"the default option should be escaped through escapeOptionHtml");
	}

	/**
	 * Escaping happens where the option is written, not inside the display value methods. An
	 * override that compares what super returns against a resource it looks up itself would stop
	 * matching if those methods escaped.
	 */
	@Test
	void displayValueHooksAreNotEscapedThemselves()
	{
		ChoicePage page = new ChoicePage(MARKUP, true);
		tester.startPage(page);

		assertEquals(MARKUP, page.choice.observedDisplayValue,
			"the display value hook should see and return unescaped text");
		assertTrue(tester.getLastResponseAsString().contains(ESCAPED),
			"while the rendered option body is escaped");
	}

	/** Plain text, which is what the shipped bundles hold, renders unchanged. */
	@Test
	void plainDisplayValueRendersUnchanged()
	{
		tester.startPage(new ChoicePage("Choose one", true));

		assertTrue(tester.getLastResponseAsString().contains("value=\"\">[Choose one]</option>"),
			"plain text should pass through the escaping unchanged");
	}
}
