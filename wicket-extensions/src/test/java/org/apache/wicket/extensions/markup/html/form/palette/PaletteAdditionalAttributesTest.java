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
package org.apache.wicket.extensions.markup.html.form.palette;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.wicket.extensions.markup.html.form.palette.component.AbstractOptions;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 * The attributes {@link AbstractOptions} adds to an option are escaped, the way
 * {@code RadioChoice} and {@code CheckBoxMultipleChoice} escape the attributes of the same hook.
 */
public class PaletteAdditionalAttributesTest extends WicketTestCase
{
	public static class AttributesPage extends WebPage
	{
		AttributesPage(final Map<String, String> additionalAttributes)
		{
			Form<Object> form = new Form<>("form");
			add(form);

			IChoiceRenderer<String> renderer = new IChoiceRenderer<String>()
			{
				@Override
				public Object getDisplayValue(String s)
				{
					return s;
				}

				@Override
				public String getIdValue(String s, int index)
				{
					return s;
				}
			};

			IModel<List<String>> selected = new ListModel<>(new ArrayList<>(Arrays.asList("A")));
			IModel<List<String>> all = new ListModel<>(new ArrayList<>(Arrays.asList("A", "B")));

			form.add(new Palette<String>("palette", selected, all, renderer, 10, true)
			{
				@Override
				protected Map<String, String> getAdditionalAttributesForChoices(Object choice)
				{
					return additionalAttributes;
				}

				@Override
				protected Map<String, String> getAdditionalAttributesForSelection(Object choice)
				{
					return additionalAttributes;
				}
			});
		}
	}

	@Test
	public void additionalAttributeValueIsEscaped()
	{
		tester.startPage(new AttributesPage(
			Collections.singletonMap("title", "x\" onload=\"x=1")));

		String response = tester.getLastResponseAsString();
		assertTrue("the attribute value should be escaped",
			response.contains("title=\"x&quot; onload=&quot;x=1\""));
		assertFalse("the attribute value should not be able to add another attribute",
			response.contains("onload=\"x=1\""));
	}

	/**
	 * The attribute name is escaped the same way, which is what {@code RadioChoice} and
	 * {@code CheckBoxMultipleChoice} do with the name from the same hook. Note that
	 * {@code Strings#escapeMarkup} leaves a space and an equals sign alone, so escaping a name is
	 * not by itself a defence against a name that is built from untrusted input. Attribute names
	 * are identifiers the application writes, not data.
	 */
	@Test
	public void additionalAttributeNameIsEscaped()
	{
		tester.startPage(new AttributesPage(Collections.singletonMap("a\"b", "v")));

		assertTrue("the attribute name should be escaped",
			tester.getLastResponseAsString().contains("a&quot;b=\"v\""));
	}
}
