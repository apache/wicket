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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AbstractAjaxBehavior;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 * Tests that the editable labels escape their model value by default, and that
 * {@link Component#setEscapeModelStrings(boolean)} on the editable label itself is honoured
 * rather than the setting of the label component it renders with.
 */
public class AjaxEditableLabelEscapeModelStringsTest extends WicketTestCase
{
	private static final String MARKUP = "<img src=x onerror=alert(1)>";

	private static final String ESCAPED = "&lt;img src=x onerror=alert(1)&gt;";

	private static final String DOUBLE_ESCAPED = "&amp;lt;img";

	private static final List<String> CHOICES = Arrays.asList(MARKUP, "other");

	/**
	 * A display value that is not a String, so that it is rendered through an
	 * {@link org.apache.wicket.util.convert.IConverter} rather than by {@code toString()} directly.
	 */
	private static class DisplayValue
	{
		@Override
		public String toString()
		{
			return MARKUP;
		}
	}

	private String render(Component component)
	{
		tester.startComponentInPage(component);
		return tester.getLastResponse().getDocument();
	}

	/**
	 * Renders the editable label, then puts it in edit mode so that the response holds the markup
	 * of the editor instead of the label.
	 */
	private String renderEditor(AjaxEditableLabel<?> label)
	{
		tester.startComponentInPage(label);
		AbstractAjaxBehavior labelBehavior = (AbstractAjaxBehavior)label.get("label")
			.getBehaviors()
			.get(0);
		tester.executeBehavior(labelBehavior);
		return tester.getLastResponse().getDocument();
	}

	@Test
	public void choiceLabelWithRendererEscapesByDefault()
	{
		String document = render(new AjaxEditableChoiceLabel<>("label", Model.of(MARKUP), CHOICES,
			new ChoiceRenderer<String>()));

		assertTrue(document, document.contains(ESCAPED));
		assertFalse(document, document.contains(MARKUP));
	}

	@Test
	public void choiceLabelWithRendererDoesNotEscapeWhenTurnedOff()
	{
		AjaxEditableChoiceLabel<String> label = new AjaxEditableChoiceLabel<>("label",
			Model.of(MARKUP), CHOICES, new ChoiceRenderer<String>());
		label.setEscapeModelStrings(false);

		assertTrue(render(label).contains(MARKUP));
	}

	/**
	 * A renderer returning a non-String display value is rendered through a converter, which does
	 * not escape either.
	 */
	@Test
	public void choiceLabelWithConvertedDisplayValueEscapesByDefault()
	{
		IChoiceRenderer<String> renderer = new ChoiceRenderer<String>()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public Object getDisplayValue(String object)
			{
				return new DisplayValue();
			}
		};

		String document = render(
			new AjaxEditableChoiceLabel<>("label", Model.of("value"), CHOICES, renderer));

		assertTrue(document, document.contains(ESCAPED));
		assertFalse(document, document.contains(MARKUP));
	}

	/**
	 * Without a renderer the value is escaped by {@code getDefaultModelObjectAsString()}, so it
	 * must not be escaped a second time.
	 */
	@Test
	public void choiceLabelWithoutRendererEscapesExactlyOnce()
	{
		String document = render(
			new AjaxEditableChoiceLabel<>("label", Model.of(MARKUP), CHOICES));

		assertTrue(document, document.contains(ESCAPED));
		assertFalse(document, document.contains(DOUBLE_ESCAPED));
	}

	@Test
	public void choiceLabelWithoutRendererDoesNotEscapeWhenTurnedOff()
	{
		AjaxEditableChoiceLabel<String> label = new AjaxEditableChoiceLabel<>("label",
			Model.of(MARKUP), CHOICES);
		label.setEscapeModelStrings(false);

		assertTrue(render(label).contains(MARKUP));
	}

	@Test
	public void labelEscapesByDefault()
	{
		String document = render(new AjaxEditableLabel<>("label", Model.of(MARKUP)));

		assertTrue(document, document.contains(ESCAPED));
		assertFalse(document, document.contains(MARKUP));
	}

	@Test
	public void labelDoesNotEscapeWhenTurnedOff()
	{
		AjaxEditableLabel<String> label = new AjaxEditableLabel<>("label", Model.of(MARKUP));
		label.setEscapeModelStrings(false);

		assertTrue(render(label).contains(MARKUP));
	}

	@Test
	public void multiLineLabelEscapesByDefault()
	{
		String document = render(new AjaxEditableMultiLineLabel<>("label", Model.of(MARKUP)));

		assertTrue(document, document.contains(ESCAPED));
		assertFalse(document, document.contains(MARKUP));
	}

	@Test
	public void multiLineLabelDoesNotEscapeWhenTurnedOff()
	{
		AjaxEditableMultiLineLabel<String> label = new AjaxEditableMultiLineLabel<>("label",
			Model.of(MARKUP));
		label.setEscapeModelStrings(false);

		assertTrue(render(label).contains(MARKUP));
	}

	/**
	 * The setting has to be picked up even when it is changed after the label component has been
	 * created.
	 */
	@Test
	public void settingIsHonouredWhenChangedAfterLabelWasCreated()
	{
		IModel<String> model = Model.of(MARKUP);
		AjaxEditableChoiceLabel<String> label = new AjaxEditableChoiceLabel<>("label", model,
			CHOICES, new ChoiceRenderer<String>());
		// forces the internal label component to be created up front
		label.setRequired(true);
		label.setEscapeModelStrings(false);

		assertTrue(render(label).contains(MARKUP));
	}

	/**
	 * The choices rendered by the editor are display values just like the label, so they follow
	 * the same setting. Escaping one but not the other would show the same value in two different
	 * ways depending on whether the label is in edit mode.
	 */
	@Test
	public void choiceEditorEscapesOptionsByDefault()
	{
		String document = renderEditor(new AjaxEditableChoiceLabel<>("label", Model.of(MARKUP),
			CHOICES, new ChoiceRenderer<String>()));

		assertTrue(document, document.contains(ESCAPED));
		assertFalse(document, document.contains(MARKUP));
	}

	@Test
	public void choiceEditorDoesNotEscapeOptionsWhenTurnedOff()
	{
		AjaxEditableChoiceLabel<String> label = new AjaxEditableChoiceLabel<>("label",
			Model.of(MARKUP), CHOICES, new ChoiceRenderer<String>());
		label.setEscapeModelStrings(false);

		String document = renderEditor(label);

		assertTrue(document, document.contains(MARKUP));
		assertFalse(document, document.contains(ESCAPED));
	}

	/**
	 * The text editors are deliberately not covered by the setting: they carry the value as text
	 * to be edited, and a textarea body that is not escaped does not round-trip the value.
	 */
	@Test
	public void multiLineEditorEscapesEvenWhenTurnedOff()
	{
		AjaxEditableMultiLineLabel<String> label = new AjaxEditableMultiLineLabel<>("label",
			Model.of(MARKUP));
		label.setEscapeModelStrings(false);

		assertTrue(renderEditor(label).contains(ESCAPED));
	}
}
