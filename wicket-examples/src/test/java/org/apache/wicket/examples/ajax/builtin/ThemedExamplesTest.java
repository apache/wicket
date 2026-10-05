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
package org.apache.wicket.examples.ajax.builtin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.examples.ajax.builtin.modal.ModalDialogPage;
import org.apache.wicket.extensions.markup.html.tabs.TabsStyle;
import org.apache.wicket.extensions.theme.Theme;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;

/**
 * The ajax examples of the components of wicket-extensions show them in a theme the user chooses.
 */
class ThemedExamplesTest extends WicketTestCase
{
	@ParameterizedTest
	@ValueSource(classes = { ModalDialogPage.class, AutoCompletePage.class, TabbedPanelPage.class,
			FileUploadPage.class, ProgressBarPage.class, CollapsiblePage.class,
			ThemeEditorPage.class })
	void theThemeCanBeChosen(Class<? extends Page> page)
	{
		tester.startPage(page);
		String themed = page == ThemeEditorPage.class ? "preview" : "themed";
		assertTrue(classOf(themed).contains(Theme.DEFAULT.getCssClass()), page.getSimpleName());

		choose("theme", Theme.DARK.ordinal());

		tester.assertComponentOnAjaxResponse(themed);
		tester.assertComponentOnAjaxResponse("explanation");
		assertTrue(classOf(themed).contains(Theme.DARK.getCssClass()), page.getSimpleName());
	}

	@Test
	void theTabsCanBeStyledOrLeftToTheExamplesCss()
	{
		tester.startPage(TabbedPanelPage.class);
		assertEquals("wicket-tabs wicket-tabs-tabs", classOf("themed:tabs"));

		choose("style", TabsStyle.PILLS.ordinal());
		assertEquals("wicket-tabs wicket-tabs-pills", classOf("themed:tabs"));

		choose("style", -1);
		assertEquals("tabpanel", classOf("themed:tabs"));
	}

	@Test
	void theThemeEditorListsEveryProperty()
	{
		tester.startPage(ThemeEditorPage.class);

		String response = tester.getLastResponseAsString();
		for (String property : ThemeEditorPage.PROPERTIES)
		{
			List<TagTester> fields = TagTester.createTagsByAttribute(response,
				"data-theme-property", "--wicket-theme-" + property, false);
			assertEquals(1, fields.size(), property);
		}
		assertEquals("color", TagTester.createTagByAttribute(response, "data-theme-property",
			"--wicket-theme-primary").getAttribute("type"));
		tester.assertContains("Wicket.ThemeEditor.init");
	}

	private String classOf(String path)
	{
		return tester.getTagById(tester.getComponentFromLastRenderedPage(path).getMarkupId())
			.getAttribute("class");
	}

	@SuppressWarnings("unchecked")
	private void choose(String path, int index)
	{
		DropDownChoice<Object> choice = (DropDownChoice<Object>)tester
			.getComponentFromLastRenderedPage(path);
		tester.getRequest().getPostParameters().setParameterValue(choice.getInputName(),
			index < 0 ? "" : choice.getChoiceRenderer().getIdValue(
				choice.getChoices().get(index), index));
		tester.executeAjaxEvent(choice, "change");
	}
}
