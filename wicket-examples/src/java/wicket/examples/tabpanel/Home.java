/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.examples.tabPanel;

import wicket.PageParameters;
import wicket.examples.util.NavigationPanel;
import wicket.markup.html.HtmlPage;
import wicket.markup.html.basic.Label;
import wicket.markup.html.form.Form;
import wicket.markup.html.panel.Panel;
import wicket.model.IModel;

/**
 * Everybody's favorite example.
 * @author Jonathan Locke
 * @author Marrink
 */
public class Home extends HtmlPage
{
	private TabPanel tab;

	/**
	 * Constructor
	 * @param parameters Page parameters
	 */
	public Home(final PageParameters parameters)
	{
		add(new NavigationPanel("mainNavigation", "TabPanel example"));

		tab = new TabPanel("tabPanel");
		add(tab);
		// all components added to the TabPanel MUST be named tab.
		// By default the first tab is selected.
		tab.addTab("tab 1", new LabelPanel("tab", "Some text on a Label inside a Panel."));
		// any component will work as long as it is compatible with the html element in
		// TabPanel.html
		// in this case it is a <div id="wicket-tab"/>
		tab.addTab("tab 2", new Label("tab", "Some text on a plain Label."));
		// this allows us to select and or disabled a tab
		tab.addTab("tab 3 (a disabled tab)", false, false, new Label("tab", "3333"));
		tab.addTab("tab 4", new LabelPanel("tab",
				"This tab can be disabled or enabled on the next tab."));
		tab.addTab("tab 5", new FormPanel("tab", new IModel()
		{

			public Object getObject()
			{
				// returns index of tab before selected tab.
				return new Integer(tab.indexOf(tab.getSelectedTab()) - 1);
			}

			public void setObject(Object object)
			{
				// not important
			}
		}));
		//And thats not all, Instead of adding components you can add your own implentation of TabPanelModel instead.
		//this way you get more control over selected/enabled/disabled/labels/style, and who knows what you will think off.
		//TODO make example with custom TabPanelModel, maybe a dynamic label or something
	}

	/**
	 * Simple Panel to display a Label.
	 * @author Marrink
	 */
	private class LabelPanel extends Panel
	{
		/**
		 * Construct.
		 * @param componentName
		 * @param label
		 */
		public LabelPanel(String componentName, String label)
		{
			super(componentName);
			add(new Label("my_label", label));
		}
	}

	/**
	 * Simple Panel with Form that disables another Tab.
	 * @author Marrink
	 */
	private class FormPanel extends Panel
	{

		/**
		 * Construct.
		 * @param componentName
		 */
		public FormPanel(String componentName, IModel model)
		{
			super(componentName);
			setModel(model);
			add(new Form("my_form", model, null)
			{

				public void handleSubmit()
				{
					tab.toggleTab(((Integer) this.getModelObject()).intValue());
				}
			});
			add(new Label("my_label",
					"This button will toggle between enabling and disabling the previous tab."));
			// button to submit form is in html file
		}

	}
}