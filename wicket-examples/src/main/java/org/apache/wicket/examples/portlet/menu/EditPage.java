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
package org.apache.wicket.examples.portlet.menu;

import java.util.List;

import javax.portlet.ActionResponse;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;

import org.apache.wicket.RequestContext;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.portlet.PortletRequestContext;

/**
 * @author Ate Douma
 */
public class EditPage extends WebPage<Void>
{
	private static final IChoiceRenderer<ExampleApplication> exampleChoiceRenderer = new IChoiceRenderer<ExampleApplication>()
	{
		/**
		 * @see org.apache.wicket.markup.html.form.IChoiceRenderer#getDisplayValue(java.lang.Object)
		 */
		public Object getDisplayValue(ExampleApplication object)
		{
			return (object).getDisplayName();
		}

		/**
		 * @see org.apache.wicket.markup.html.form.IChoiceRenderer#getIdValue(java.lang.Object, int)
		 */
		public String getIdValue(ExampleApplication object, int index)
		{
			return Integer.toString(index);
		}
	};

	private final DropDownChoice<ExampleApplication> ddc;

	/**
	 * Construct.
	 */
	public EditPage()
	{
		Form<?> form = new Form<Void>("form")
		{
			/**
			 * @see org.apache.wicket.markup.html.form.Form#onSubmit()
			 */
			@Override
			protected void onSubmit()
			{
				ExampleApplication selected = ddc.getModelObject();
				PortletRequestContext prc = (PortletRequestContext)RequestContext.get();
				PortletPreferences prefs = prc.getPortletRequest().getPreferences();
				prc.getPortletRequest().getPortletSession().setAttribute(
					WicketExamplesMenuPortlet.EXAMPLE_APPLICATION_ATTR, selected);
				try
				{
					((ActionResponse)prc.getPortletResponse()).setPortletMode(PortletMode.VIEW);
					prefs.setValue(WicketExamplesMenuPortlet.EXAMPLE_APPLICATION_PREF,
						selected.getFilterPath());
					prefs.store();
				}
				catch (Exception pe)
				{
					throw new RuntimeException(pe);
				}
			}
		};
		List<ExampleApplication> examples = WicketExamplesMenuApplication.getExamples();
		ddc = new DropDownChoice<ExampleApplication>("examples", examples, exampleChoiceRenderer);
		ddc.setNullValid(false);
		PortletRequestContext prc = (PortletRequestContext)RequestContext.get();
		String eaFilterPath = prc.getPortletRequest().getPreferences().getValue(
			WicketExamplesMenuPortlet.EXAMPLE_APPLICATION_PREF, null);
		Model<ExampleApplication> selected = new Model<ExampleApplication>(examples.get(0));
		if (eaFilterPath != null)
		{
			for (int i = 0, size = examples.size(); i < size; i++)
			{
				if ((examples.get(i)).getFilterPath().equals(eaFilterPath))
				{
					selected.setObject(examples.get(i));
					break;
				}
			}
		}
		ddc.setModel(selected);
		form.add(ddc);
		form.add(new Button<Void>("setButton"));
		add(form);
	}

}
