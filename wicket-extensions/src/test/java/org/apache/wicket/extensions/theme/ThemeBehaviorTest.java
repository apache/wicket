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
package org.apache.wicket.extensions.theme;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

class ThemeBehaviorTest extends WicketTestCase
{
	@Test
	void addsTheClassOfTheThemeAndItsStyleSheet()
	{
		tester.startPage(new ThemedPage(Model.of(Theme.RED)));

		assertEquals("box wicket-theme wicket-theme-red",
			tester.getTagByWicketId("themed").getAttribute("class"));
		tester.assertContains("wicket-theme.*\\.css");
	}

	@Test
	void switchesWithItsModel()
	{
		IModel<Theme> theme = Model.of(Theme.BLUE);
		tester.startPage(new ThemedPage(theme));

		theme.setObject(Theme.GREEN);
		tester.startPage(tester.getLastRenderedPage());

		assertEquals("box wicket-theme wicket-theme-green",
			tester.getTagByWicketId("themed").getAttribute("class"));
	}

	@Test
	void noThemeWithoutAModelObject()
	{
		tester.startPage(new ThemedPage(Model.of((Theme)null)));

		assertEquals("box", tester.getTagByWicketId("themed").getAttribute("class"));
	}

	@Test
	void everyThemeHasItsClassInTheStyleSheet() throws Exception
	{
		String css = new String(Theme.class.getResourceAsStream("wicket-theme.css").readAllBytes(),
			"UTF-8");
		for (Theme theme : Theme.values())
		{
			assertTrue(css.contains("." + theme.getCssClass() + " {"), theme.name());
		}
	}

	public static class ThemedPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		ThemedPage(IModel<Theme> theme)
		{
			add(new WebMarkupContainer("themed").add(new ThemeBehavior(theme)));
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream("<html><head></head><body>" +
				"<div wicket:id=\"themed\" class=\"box\"></div></body></html>");
		}
	}
}
