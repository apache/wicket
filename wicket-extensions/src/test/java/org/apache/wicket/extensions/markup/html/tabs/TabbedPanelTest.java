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

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.WicketTestCase;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.junit.Test;

/**
 * Test for {@link TabbedPanel}.
 */
public class TabbedPanelTest extends WicketTestCase
{
	/**
	 */
	public class TestPage extends WebPage
	{
		private static final long serialVersionUID = 1L;

		protected TabbedPanel<ITab> tabbedPanel;

		/**
		 */
		public TestPage()
		{
			List<ITab> defaultTabs = new ArrayList<ITab>();
			defaultTabs.add(new AbstractTab(Model.of("default 1"))
			{
				private static final long serialVersionUID = 1L;

				@Override
				public WebMarkupContainer getPanel(String panelId)
				{
					return new TestPanel(panelId, "default 1");
				}
			});
			defaultTabs.add(new AbstractTab(Model.of("default 2"))
			{
				private static final long serialVersionUID = 1L;

				@Override
				public WebMarkupContainer getPanel(String panelId)
				{
					return new TestPanel(panelId, "default 2");
				}
			});
			tabbedPanel = newTabbedPanel(defaultTabs);
			add(tabbedPanel);
		}

	}

	TabbedPanel<ITab> newTabbedPanel(List<ITab> defaultTabs)
	{
		return new TabbedPanel<ITab>("tabpanel", defaultTabs);
	}

	/**
	 */
	public static class TestPanel extends Panel
	{
		private static final long serialVersionUID = 1L;

		/**
		 * @param id
		 * @param panelTestId
		 */
		public TestPanel(String id, String panelTestId)
		{
			super(id);
			this.add(new Label("label", panelTestId));
		}
	}

	/**
	 * No tabs thus no tab component rendered.
	 * 
	 * @throws Exception
	 */
	@Test
	public void renderNoTabs() throws Exception
	{
		TestPage page = new TestPage();
		page.tabbedPanel.getTabs().clear();
		tester.startPage(page);

		tester.assertContainsNot("<span wicket:id=\"title\">default 1</span>");
		tester.assertContainsNot("<span wicket:id=\"label\">default 1</span>");
		tester.assertContainsNot("<span wicket:id=\"title\">default 2</span>");
		tester.assertContainsNot("<span wicket:id=\"label\">default 2</span>");
		tester.assertContains("<!-- no panel -->");

		assertEquals(Integer.valueOf(-1), page.tabbedPanel.getDefaultModelObject());
	}

	/**
	 * Switching between tabsS.
	 * 
	 * @throws Exception
	 */
	@Test
	public void renderDefaultTabsOnly() throws Exception
	{
		TestPage page = tester.startPage(new TestPage());
		tester.assertContains("<span wicket:id=\"title\">default 1</span></a>");
		tester.assertContains("<span wicket:id=\"label\">default 1</span>");
		tester.assertContains("<span wicket:id=\"title\">default 2</span></a>");

		assertEquals(Integer.valueOf(0), page.tabbedPanel.getDefaultModelObject());

		tester.clickLink("tabpanel:tabs-container:tabs:1:link");
		tester.assertContains("<span wicket:id=\"label\">default 2</span>");

		assertEquals(Integer.valueOf(1), page.tabbedPanel.getDefaultModelObject());
	}

	/**
	 * Additional tabs are rendered.
	 * 
	 * @throws Exception
	 */
	@Test
	public void renderAdditionalTabs() throws Exception
	{
		TestPage page = tester.startPage(new TestPage());
		page.tabbedPanel.getTabs().add(new AbstractTab(Model.of("added 1"))
		{
			private static final long serialVersionUID = 1L;

			@Override
			public WebMarkupContainer getPanel(String panelId)
			{
				return new TestPanel(panelId, "added 1");
			}
		});
		// the additional tab isn't rendered yet
		tester.assertContainsNot("<span wicket:id=\"title\">added 1</span>");
		tester.assertContainsNot("<span wicket:id=\"label\">added 1</span>");

		assertEquals(Integer.valueOf(0), page.tabbedPanel.getDefaultModelObject());

		// now its title is visible, but the contents not
		tester.clickLink("tabpanel:tabs-container:tabs:1:link");
		tester.assertContains("<span wicket:id=\"title\">added 1</span>");
		tester.assertContainsNot("<span wicket:id=\"label\">added 1</span>");

		assertEquals(Integer.valueOf(1), page.tabbedPanel.getDefaultModelObject());

		// now the entire panel should be there
		tester.clickLink("tabpanel:tabs-container:tabs:2:link");
		tester.assertContains("<span wicket:id=\"title\">added 1</span>");
		tester.assertContains("<span wicket:id=\"label\">added 1</span>");

		assertEquals(Integer.valueOf(2), page.tabbedPanel.getDefaultModelObject());
	}

	/**
	 * Changing model switches tab.
	 * 
	 * @throws Exception
	 */
	@Test
	public void renderModelChange() throws Exception
	{
		TestPage page = new TestPage();

		tester.startPage(page);
		tester.assertContains("<span wicket:id=\"label\">default 1</span>");

		page.tabbedPanel.setDefaultModelObject(Integer.valueOf(1));
		tester.startPage(page);

		tester.assertContains("<span wicket:id=\"label\">default 2</span>");
	}

	/**
	 * Tab's component is aquired once only.
	 * 
	 * @throws Exception
	 */
	@Test
	public void tabComponentAquiredOnChangeOnly() throws Exception
	{

		final int[] count = new int[1];

		TestPage page = new TestPage();
		page.tabbedPanel.getTabs().clear();
		page.tabbedPanel.getTabs().add(new AbstractTab(Model.of("added 1"))
		{
			private static final long serialVersionUID = 1L;

			@Override
			public WebMarkupContainer getPanel(String panelId)
			{
				count[0]++;

				return new TestPanel(panelId, "added 1");
			}
		});

		assertEquals(0, count[0]);
		tester.startPage(page);
		assertEquals(1, count[0]);
		tester.startPage(page);
		assertEquals(1, count[0]);

		page.tabbedPanel.setSelectedTab(0);

		assertEquals(2, count[0]);
		tester.startPage(page);
		assertEquals(2, count[0]);
	}

	/**
	 * An invisible tab gets replaced by another one.
	 * 
	 * @throws Exception
	 */
	@Test
	public void invisibleTabGetsReplaced() throws Exception
	{
		final boolean[] visible = { true, true };

		TestPage page = new TestPage();
		page.tabbedPanel.getTabs().clear();
		page.tabbedPanel.getTabs().add(new AbstractTab(Model.of("added 1"))
		{
			private static final long serialVersionUID = 1L;

			@Override
			public WebMarkupContainer getPanel(String panelId)
			{
				return new TestPanel(panelId, "added 1");
			}

			@Override
			public boolean isVisible()
			{
				return visible[0];
			}
		});
		page.tabbedPanel.getTabs().add(new AbstractTab(Model.of("added 2"))
		{
			private static final long serialVersionUID = 1L;

			@Override
			public WebMarkupContainer getPanel(String panelId)
			{
				return new TestPanel(panelId, "added 2");
			}

			@Override
			public boolean isVisible()
			{
				return visible[1];
			}
		});

		page.tabbedPanel.setSelectedTab(1);

		tester.startPage(page);

		assertEquals(Integer.valueOf(1), page.tabbedPanel.getDefaultModelObject());
		tester.assertContains("<span wicket:id=\"title\">added 1</span>");
		tester.assertContains("<span wicket:id=\"title\">added 2</span>");
		tester.assertContains("<span wicket:id=\"label\">added 2</span>");

		visible[1] = false;

		tester.startPage(page);

		// now first tab is selected
		assertEquals(Integer.valueOf(0), page.tabbedPanel.getDefaultModelObject());

		tester.assertContains("<span wicket:id=\"title\">added 1</span>");
		tester.assertContainsNot("<span wicket:id=\"title\">added 2</span>");
		tester.assertContains("<span wicket:id=\"label\">added 1</span>");

		visible[0] = false;

		tester.startPage(page);

		// first tab stays selected since no other is visible
		assertEquals(Integer.valueOf(0), page.tabbedPanel.getDefaultModelObject());
		tester.assertContainsNot("<span wicket:id=\"title\">added 1</span>");
		tester.assertContainsNot("<span wicket:id=\"title\">added 2</span>");
		tester.assertContains("<!-- no panel -->");

		visible[1] = true;

		tester.startPage(page);

		// second selected again
		assertEquals(Integer.valueOf(1), page.tabbedPanel.getDefaultModelObject());
		tester.assertContainsNot("<span wicket:id=\"title\">added 1</span>");
		tester.assertContains("<span wicket:id=\"title\">added 2</span>");
		tester.assertContains("<span wicket:id=\"label\">added 2</span>");
	}
}