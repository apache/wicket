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
package org.apache.wicket.examples.breadcrumb;

import org.apache.wicket.extensions.breadcrumb.IBreadCrumbModel;
import org.apache.wicket.extensions.breadcrumb.panel.BreadCrumbPanel;
import org.apache.wicket.extensions.breadcrumb.panel.BreadCrumbPanelLink;
import org.apache.wicket.extensions.breadcrumb.panel.IBreadCrumbPanelFactory;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;


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
			setDefaultModel(new CompoundPropertyModel(this));
			add(new TextField("input"));
			add(new Button("normalButton"));

			add(new Button("nextButton")
			{
				@Override
				public void onSubmit()
				{
					activate(new IBreadCrumbPanelFactory()
					{
						@Override
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
	 * @see org.apache.wicket.extensions.breadcrumb.IBreadCrumbParticipant#getTitle()
	 */
	@Override
	public IModel<String> getTitle()
	{
		return Model.of("second");
	}
}
