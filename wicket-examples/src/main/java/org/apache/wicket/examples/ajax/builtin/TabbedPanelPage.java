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

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.extensions.ajax.markup.html.tabs.AjaxTabbedPanel;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.extensions.markup.html.tabs.TabsStyle;
import org.apache.wicket.extensions.markup.html.tabs.TabsStyleBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.EnumChoiceRenderer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LambdaModel;
import org.apache.wicket.model.Model;


/**
 * Tabbed panel demo.
 * 
 * @author ivaynberg
 */
public class TabbedPanelPage extends BasePage
{
	private static final long serialVersionUID = 1L;

	private TabsStyle style = TabsStyle.TABS;

	/**
	 * Constructor
	 */
	public TabbedPanelPage()
	{
		// create a list of ITab objects used to feed the tabbed panel
		List<ITab> tabs = new ArrayList<>();
		tabs.add(new AbstractTab(new Model<>("first tab"))
		{
			@Override
			public Panel getPanel(String panelId)
			{
				return new TabPanel1(panelId);
			}
		});

		tabs.add(new AbstractTab(new Model<>("second tab"))
		{
			@Override
			public Panel getPanel(String panelId)
			{
				return new TabPanel2(panelId);
			}
		});

		tabs.add(new AbstractTab(new Model<>("third tab"))
		{
			@Override
			public Panel getPanel(String panelId)
			{
				return new TabPanel3(panelId);
			}
		});

		WebMarkupContainer themed = newThemedContainer("themed");
		add(themed);
		add(newThemeChoice("theme", themed));

		AjaxTabbedPanel<ITab> tabbedPanel = new AjaxTabbedPanel<>("tabs", tabs);
		tabbedPanel.add(new TabsStyleBehavior(() -> style));
		tabbedPanel.add(AttributeModifier.append("class", () -> style == null ? "tabpanel" : null));
		themed.add(tabbedPanel);

		DropDownChoice<TabsStyle> styleChoice = new DropDownChoice<>("style",
			LambdaModel.of(() -> style, chosen -> style = chosen), List.of(TabsStyle.values()),
			new EnumChoiceRenderer<>(this));
		styleChoice.setNullValid(true);
		styleChoice.add(new AjaxFormComponentUpdatingBehavior("change")
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target)
			{
				target.add(themed);
			}
		});
		add(styleChoice);
	}

	/**
	 * Panel representing the content panel for the first tab.
	 */
	private static class TabPanel1 extends Panel
	{
		/**
		 * Constructor
		 * 
		 * @param id
		 *            component id
		 */
		public TabPanel1(String id)
		{
			super(id);
		}
	}

	/**
	 * Panel representing the content panel for the second tab.
	 */
	private static class TabPanel2 extends Panel
	{
		/**
		 * Constructor
		 * 
		 * @param id
		 *            component id
		 */
		public TabPanel2(String id)
		{
			super(id);
		}
	}

	/**
	 * Panel representing the content panel for the third tab.
	 */
	private static class TabPanel3 extends Panel
	{
		/**
		 * Constructor
		 * 
		 * @param id
		 *            component id
		 */
		public TabPanel3(String id)
		{
			super(id);
		}
	}
}
