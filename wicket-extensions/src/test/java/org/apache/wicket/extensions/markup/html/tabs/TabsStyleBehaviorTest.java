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
package org.apache.wicket.extensions.markup.html.tabs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

class TabsStyleBehaviorTest extends WicketTestCase
{
	@Test
	void theLookIsACssClassAndAStyleSheet()
	{
		tester.startPage(new TabsPage(Model.of(TabsStyle.PILLS)));

		TagTester tabs = tester.getTagByWicketId("tabs");
		assertEquals("wicket-tabs wicket-tabs-pills", tabs.getAttribute("class"));
		assertTrue(tester.getLastResponseAsString().contains("TabsStyleBehavior/wicket-tabs"));
	}

	@Test
	void theLookFollowsTheModel()
	{
		Model<TabsStyle> style = Model.of(TabsStyle.TABS);
		TabsPage page = new TabsPage(style);
		tester.startPage(page);

		style.setObject(TabsStyle.UNDERLINE);
		tester.startPage(page);

		assertEquals("wicket-tabs wicket-tabs-underline",
			tester.getTagByWicketId("tabs").getAttribute("class"));
	}

	@Test
	void withoutALookThePanelGetsNoClass()
	{
		tester.startPage(new TabsPage(Model.of((TabsStyle)null)));

		assertFalse(tester.getTagByWicketId("tabs").hasAttribute("class"));
	}

	public static class TabsPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		TabsPage(Model<TabsStyle> style)
		{
			ITab tab = new AbstractTab(Model.of("One"))
			{
				private static final long serialVersionUID = 1L;

				@Override
				public WebMarkupContainer getPanel(String panelId)
				{
					return new EmptyPanel(panelId);
				}
			};
			add(new TabbedPanel<>("tabs", List.of(tab)).add(new TabsStyleBehavior(style)));
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream(
				"<html><head></head><body><div wicket:id=\"tabs\"></div></body></html>");
		}
	}
}
