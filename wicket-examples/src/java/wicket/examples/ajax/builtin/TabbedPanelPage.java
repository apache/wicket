/*
 * $Id: TabbedPanelPage.java 5860 2006-05-25 20:29:28 +0000 (Thu, 25 May 2006)
 * eelco12 $ $Revision$ $Date: 2006-05-25 20:29:28 +0000 (Thu, 25 May
 * 2006) $
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.examples.ajax.builtin;

import java.util.ArrayList;
import java.util.List;

import wicket.MarkupContainer;
import wicket.extensions.ajax.markup.html.tabs.AjaxTabbedPanel;
import wicket.extensions.markup.html.tabs.AbstractTab;
import wicket.markup.html.panel.Panel;
import wicket.model.Model;

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
		List<AbstractTab> tabs = new ArrayList<AbstractTab>();
		tabs.add(new AbstractTab(new Model<String>("first tab"))
		{
			@Override
			public Panel getPanel(MarkupContainer parent, String panelId)
			{
				return new TabPanel1(parent, panelId);
			}
		});

		tabs.add(new AbstractTab(new Model<String>("second tab"))
		{
			@Override
			public Panel getPanel(MarkupContainer parent, String panelId)
			{
				return new TabPanel2(parent, panelId);
			}
		});

		tabs.add(new AbstractTab(new Model<String>("third tab"))
		{
			@Override
			public Panel getPanel(MarkupContainer parent, String panelId)
			{
				return new TabPanel3(parent, panelId);
			}
		});

		new AjaxTabbedPanel(this, "tabs", tabs);
	}

	/**
	 * Panel representing the content panel for the first tab.
	 */
	private static class TabPanel1 extends Panel
	{
		/**
		 * Constructor
		 * 
		 * @param parent
		 *            The parent of this component The parent of this component.
		 * @param id
		 *            component id
		 */
		public TabPanel1(MarkupContainer parent, String id)
		{
			super(parent, id);
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
		 * @param parent
		 *            The parent of this component The parent of this component.
		 * @param id
		 *            component id
		 */
		public TabPanel2(MarkupContainer parent, String id)
		{
			super(parent, id);
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
		 * @param parent
		 *            The parent of this component The parent of this component.
		 * @param id
		 *            component id
		 */
		public TabPanel3(MarkupContainer parent, String id)
		{
			super(parent, id);
		}
	};
}