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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.lang.Exceptions;
import org.apache.wicket.util.string.StringValueConversionException;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

/**
 * Tests the <code>escape</code> attribute of {@literal <wicket:label>}, which turns off the
 * escaping of the label text the tag takes from a model or from a resource bundle.
 * 
 * @see AutoLabelEscapeMarkupTest for the escaping this attribute opts out of
 */
class AutoLabelEscapeAttributeTest extends WicketTestCase
{
	/** Label of the component whose tag says escape="false". */
	private static final String FROM_MODEL = "<em>model</em>";

	/** Bundle entry under the component's id, the default label, on a tag with escape="false". */
	private static final String FROM_DEFAULT_LABEL = "<em>default</em>";

	/** Bundle entry the key attribute names, on a tag with escape="false". */
	private static final String FROM_KEY = "<em>key</em>";

	/** Label of the component whose tag says escape="true". */
	private static final String ESCAPE_TRUE = "<em>escapeTrue</em>";

	/** Label of the component whose tag says escape="", which is not a request to stop escaping. */
	private static final String ESCAPE_BLANK = "<em>escapeBlank</em>";

	public static class LabelPage extends WebPage
	{
		public LabelPage()
		{
			Form<Void> form = new Form<>("form");
			add(form);
			form.add(new TextField<>("fromModel", Model.of("")).setLabel(Model.of(FROM_MODEL)));
			form.add(new TextField<>("fromDefaultLabel", Model.of("")));
			form.add(new TextField<>("fromKey", Model.of("")));
			form.add(new TextField<>("escapeTrue", Model.of("")).setLabel(Model.of(ESCAPE_TRUE)));
			form.add(new TextField<>("escapeBlank", Model.of("")).setLabel(Model.of(ESCAPE_BLANK)));
		}
	}

	public static class WrongEscapeValuePage extends WebPage
	{
		public WrongEscapeValuePage()
		{
			Form<Void> form = new Form<>("form");
			add(form);
			form.add(new TextField<>("field", Model.of("")).setLabel(Model.of("a label")));
		}
	}

	/** The label model, the case {@link SimpleFormComponentLabel} renders escaped. */
	@Test
	void escapeFalseWritesTheModelLabelAsMarkup()
	{
		tester.startPage(new LabelPage());

		String response = tester.getLastResponseAsString();
		assertTrue(response.contains(FROM_MODEL),
			"label from the model should be written as markup");
		assertFalse(response.contains("&lt;em&gt;model&lt;/em&gt;"),
			"label from the model should not be escaped");
	}

	/** The default label, looked up in the bundle by the component's id. */
	@Test
	void escapeFalseWritesTheDefaultLabelAsMarkup()
	{
		tester.startPage(new LabelPage());

		String response = tester.getLastResponseAsString();
		assertTrue(response.contains(FROM_DEFAULT_LABEL),
			"default label should be written as markup");
		assertFalse(response.contains("&lt;em&gt;default&lt;/em&gt;"),
			"default label should not be escaped");
	}

	/** The message key on the tag, {@literal <wicket:label key="markupKey" escape="false"/>}. */
	@Test
	void escapeFalseWritesTheMessageKeyAsMarkup()
	{
		tester.startPage(new LabelPage());

		String response = tester.getLastResponseAsString();
		assertTrue(response.contains(FROM_KEY),
			"label from a message key should be written as markup");
		assertFalse(response.contains("&lt;em&gt;key&lt;/em&gt;"),
			"label from a message key should not be escaped");
	}

	/** Spelling out the default changes nothing. */
	@Test
	void escapeTrueStillEscapes()
	{
		tester.startPage(new LabelPage());

		String response = tester.getLastResponseAsString();
		assertTrue(response.contains("&lt;em&gt;escapeTrue&lt;/em&gt;"),
			"escape=\"true\" should escape the label");
		assertFalse(response.contains(ESCAPE_TRUE),
			"escape=\"true\" should not let the label reach the markup as markup");
	}

	/**
	 * An empty value is not a request to stop escaping. This is where the attribute deliberately
	 * differs from {@literal <wicket:message>}: that tag reads it with
	 * {@link org.apache.wicket.util.value.IValueMap#getBoolean(String)}, which resolves an empty
	 * value to false, and there false means the default. Here false is the opt-out, so an empty
	 * value has to keep the escaping rather than silently drop it.
	 */
	@Test
	void blankEscapeAttributeStillEscapes()
	{
		tester.startPage(new LabelPage());

		String response = tester.getLastResponseAsString();
		assertTrue(response.contains("&lt;em&gt;escapeBlank&lt;/em&gt;"),
			"escape=\"\" should escape the label");
		assertFalse(response.contains(ESCAPE_BLANK),
			"escape=\"\" should not let the label reach the markup as markup");
	}

	/**
	 * A value that is neither true nor false is a mistake in the markup, and it is reported the way
	 * the same mistake on {@literal <wicket:message>} is. The render wraps it, so the chain is what
	 * is asserted here.
	 * 
	 * @see org.apache.wicket.markup.resolver.WicketMessageResolverTest
	 */
	@Test
	void unrecognisedEscapeValueFails()
	{
		Exception exception = assertThrows(Exception.class,
			() -> tester.startPage(new WrongEscapeValuePage()));

		StringValueConversionException cause = Exceptions.findCause(exception,
			StringValueConversionException.class);
		assertNotNull(cause, "an unrecognised escape value should fail the render");
		assertEquals("Boolean value \"yesPlease\" not recognized", cause.getMessage());
	}

	/**
	 * Escaping happens where the label is written, so the label the FormComponent keeps for its
	 * error messages is the bundle value either way.
	 */
	@Test
	void escapeFalseLeavesTheComponentLabelUnchanged()
	{
		tester.startPage(new LabelPage());

		assertEquals(FROM_KEY,
			((FormComponent<?>)tester.getComponentFromLastRenderedPage("form:fromKey")).getLabel()
				.getObject());
	}
}
