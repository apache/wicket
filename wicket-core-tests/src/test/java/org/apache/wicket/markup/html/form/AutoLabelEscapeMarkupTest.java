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

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

/**
 * Tests that the text {@literal <wicket:label>} takes from a model or from a resource bundle is
 * escaped, while the markup it takes from its own tag body is not.
 */
class AutoLabelEscapeMarkupTest extends WicketTestCase
{
	/** The label the tag body is used for, which has neither a model nor a bundle entry. */
	private static final String BODY_MARKUP = "<em>emphasis</em>";

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static class LabelPage extends WebPage
	{
		LabelPage(String labelFromModel)
		{
			Form form = new Form("form");
			add(form);
			form.add(new TextField("fromModel", Model.of("")).setLabel(Model.of(labelFromModel)));
			form.add(new TextField("fromDefaultLabel", Model.of("")));
			form.add(new TextField("fromKey", Model.of("")));
			form.add(new TextField("fromBody", Model.of("")));
		}
	}

	/**
	 * The label model is the case reported: it is the same model
	 * {@link SimpleFormComponentLabel} renders escaped.
	 * <p>
	 * The labeled component here is a {@link TextField}, which clears escapeModelStrings in its
	 * constructor so that its value attribute is not encoded twice. Reading the flag from the
	 * labeled component instead of from the label would leave this case unescaped, so this test is
	 * what keeps that mistake out.
	 */
	@Test
	void labelFromModelIsEscaped()
	{
		tester.startPage(new LabelPage("<script>x=0</script>"));

		String response = tester.getLastResponseAsString();
		assertTrue(response.contains("&lt;script&gt;x=0&lt;/script&gt;"),
			"label from the model should be escaped");
		assertFalse(response.contains("<script>x=0</script>"),
			"label from the model should not reach the markup as markup");
	}

	/** The default label, looked up in the bundle by the component's id. */
	@Test
	void labelFromDefaultLabelIsEscaped()
	{
		tester.startPage(new LabelPage(""));

		String response = tester.getLastResponseAsString();
		assertTrue(response.contains("&lt;script&gt;x=1&lt;/script&gt;"),
			"default label should be escaped");
		assertFalse(response.contains("<script>x=1</script>"),
			"default label should not reach the markup as markup");
	}

	/** The message key on the tag, {@literal <wicket:label key="markupKey"/>}. */
	@Test
	void labelFromMessageKeyIsEscaped()
	{
		tester.startPage(new LabelPage(""));

		String response = tester.getLastResponseAsString();
		assertTrue(response.contains("&lt;script&gt;x=2&lt;/script&gt;"),
			"label from a message key should be escaped");
		assertFalse(response.contains("<script>x=2</script>"),
			"label from a message key should not reach the markup as markup");
	}

	/**
	 * The tag body is markup the label has just rendered itself, so it is written as is. This is
	 * the documented way to put markup in a label, and escaping it would double encode whatever
	 * nested components and {@literal <wicket:message>} produced.
	 */
	@Test
	void labelFromTagBodyIsRenderedAsMarkup()
	{
		tester.startPage(new LabelPage(""));

		String response = tester.getLastResponseAsString();
		assertTrue(response.contains(BODY_MARKUP),
			"the tag body should be rendered as markup");
		assertFalse(response.contains("&lt;em&gt;emphasis&lt;/em&gt;"),
			"the tag body should not be escaped");
	}

	/**
	 * Escaping happens where the label is written, not in the model, so the label the
	 * FormComponent keeps for its error messages is still the value the application supplied.
	 */
	@Test
	void labelModelIsNotEscapedItself()
	{
		String label = "<script>x=0</script>";
		tester.startPage(new LabelPage(label));

		assertEquals(label,
			((FormComponent<?>)tester.getComponentFromLastRenderedPage("form:fromModel")).getLabel()
				.getObject());
	}

	/** The tag body is handed to the FormComponent as its label, as it always was. */
	@Test
	void tagBodyBecomesTheComponentLabel()
	{
		tester.startPage(new LabelPage(""));

		assertEquals(BODY_MARKUP,
			((FormComponent<?>)tester.getComponentFromLastRenderedPage("form:fromBody")).getLabel()
				.getObject());
	}
}
