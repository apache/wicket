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
package org.apache.wicket.markup.html.form.onFormValidateModelObjects;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

public class AddressFormPanel extends Panel
{
	public AddressFormPanel(String id, IModel<ChildModel> model)
	{
		super(id, model);

		setupPanel();
	}

	private void setupPanel()
	{
		// create the form and add the fields and submit button
		Form form = new Form("childForm", getDefaultModel())
		{
			@Override
			protected void onValidateModelObjects()
			{
				super.onValidateModelObjects();
				((ChildModel)getModelObject()).setChildValidated(true);
			}
		};

		add(form);

		// add all the form fields
		form.add(new TextField("address1"));
		form.add(new TextField("address2"));
		form.add(new TextField("city"));
		form.add(new TextField("state"));
		form.add(new TextField("zip"));

		// add the child submit button
		add(new AjaxSubmitLink("childSubmitButton", form)
		{
			@Override
			public void onSubmit(AjaxRequestTarget target, Form<?> form)
			{
				((ChildModel)getPanelInstance().getDefaultModelObject()).submittedCalled(target);
			}
		});
	}

	private AddressFormPanel getPanelInstance()
	{
		return this;
	}

	public abstract static class ChildModel
	{
		String address1 = "address1";
		String address2 = "address2";
		String city = "city";
		String state = "state";
		String zip = "zip";

		boolean childValidated = false;

		public String getAddress1()
		{
			return address1;
		}

		public void setAddress1(String address1)
		{
			this.address1 = address1;
		}

		public String getAddress2()
		{
			return address2;
		}

		public void setAddress2(String address2)
		{
			this.address2 = address2;
		}

		public String getCity()
		{
			return city;
		}

		public void setCity(String city)
		{
			this.city = city;
		}

		public String getState()
		{
			return state;
		}

		public void setState(String state)
		{
			this.state = state;
		}

		public String getZip()
		{
			return zip;
		}

		public void setZip(String zip)
		{
			this.zip = zip;
		}

		public boolean isChildValidated()
		{
			return childValidated;
		}

		public void setChildValidated(boolean childValidated)
		{
			this.childValidated = childValidated;
		}

		public void submittedCalled(AjaxRequestTarget target)
		{
			// do nothing here...(this is so parent can refresh the validations
			// area)
		}

	}

}
