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

import org.apache.wicket.extensions.ajax.markup.html.tabs.AjaxTabbedPanel;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;


/**
 * Tabbed panel demo.
 * 
 * @author ivaynberg
 */
public class TabbedPanelPage extends BasePage
{
	/**
	 * Constructor
	 */
	public TabbedPanelPage()
	{
		// create a list of ITab objects used to feed the tabbed panel
		List<ITab> tabs = new ArrayList<>();
		tabs.add(new AbstractTab(new Model<String>("first tab"))
		{
			@Override
			public Panel getPanel(String panelId)
			{
				return new TabPanel1(panelId);
			}
		});

		tabs.add(new AbstractTab(new Model<String>("second tab"))
		{
			@Override
			public Panel getPanel(String panelId)
			{
				return new TabPanel2(panelId);
			}
		});

		tabs.add(new AbstractTab(new Model<String>("third tab"))
		{
			@Override
			public Panel getPanel(String panelId)
			{
				return new TabPanel3(panelId);
			}
		});

		add(new AjaxTabbedPanel("tabs", tabs));
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
	};

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
	};

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
	};
}