/*
 * $Id: org.eclipse.jdt.ui.prefs 5004 2006-03-17 20:47:08 -0800 (Fri, 17 Mar
 * 2006) eelco12 $ $Revision: 5004 $ $Date: 2006-03-17 20:47:08 -0800 (Fri, 17
 * Mar 2006) $
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
package wicket.examples.breadcrumb;

import wicket.extensions.breadcrumb.IBreadCrumbModel;
import wicket.extensions.breadcrumb.panel.BreadCrumbPanel;
import wicket.extensions.breadcrumb.panel.BreadCrumbPanelLink;
import wicket.extensions.breadcrumb.panel.IBreadCrumbPanelFactory;
import wicket.markup.html.form.Button;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.TextField;
import wicket.model.CompoundPropertyModel;

/**
 * Test bread crumb enabled panel.
 * 
 * @author Eelco Hillenius
 */
public class SecondPanel extends BreadCrumbPanel
{
	/** Test form. */
	private final class InputForm extends Form
	{
		/** test input string. */
		private String input;

		/**
		 * Construct.
		 * 
		 * @param id
		 *            The component id
		 */
		public InputForm(String id)
		{
			super(id);
			setModel(new CompoundPropertyModel(this));
			add(new TextField("input"));
			add(new Button("normalButton"));

			add(new Button("nextButton")
			{
				protected void onSubmit()
				{
					activate(new IBreadCrumbPanelFactory()
					{
						public BreadCrumbPanel create(String componentId,
								IBreadCrumbModel breadCrumbModel)
						{
							return new ResultPanel(componentId, breadCrumbModel, input);
						}
					});
				}
			});
		}

		/**
		 * Gets input.
		 * 
		 * @return input
		 */
		public String getInput()
		{
			return input;
		}

		/**
		 * Sets input.
		 * 
		 * @param input
		 *            input
		 */
		public void setInput(String input)
		{
			this.input = input;
		}
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 * @param breadCrumbModel
	 */
	public SecondPanel(final String id, final IBreadCrumbModel breadCrumbModel)
	{
		super(id, breadCrumbModel);

		add(new BreadCrumbPanelLink("linkToThird", this, ThirdPanel.class));
		add(new BreadCrumbPanelLink("linkToFourth", this, FourthPanel.class));

		add(new InputForm("form"));
	}

	/**
	 * @see wicket.extensions.breadcrumb.IBreadCrumbParticipant#getTitle()
	 */
	public String getTitle()
	{
		return "second";
	}
}
