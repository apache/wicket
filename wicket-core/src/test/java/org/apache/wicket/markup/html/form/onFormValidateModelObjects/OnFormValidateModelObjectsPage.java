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

import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.apache.wicket.markup.html.form.onFormValidateModelObjects.AddressFormPanel.ChildModel;

/**
 * Test page for <a href="https://issues.apache.org/jira/browse/WICKET-4344">WICKET-4344</a>
 */
public class OnFormValidateModelObjectsPage extends WebPage
{
	private static Map<String, ParentModel> pmMap = new HashMap<String, ParentModel>();

	public OnFormValidateModelObjectsPage(final PageParameters parameters)
	{
		super(parameters);

		CompoundPropertyModel<ParentModel> parentModel = new CompoundPropertyModel<ParentModel>(
				new LoadableDetachableModel<ParentModel>()
				{
					@Override
					protected ParentModel load()
					{
						String id = Session.get().getId();
						ParentModel pm = pmMap.get(id);
						if (pm == null)
						{
							pm = new ParentModel();
							pmMap.put(id, pm);
						}
						return pm;
					}
				});

		setDefaultModel(parentModel); // so we don't need to get it from the
										// form.

		Form parentForm = new Form("parentForm", parentModel)
		{
			@Override
			protected void onValidateModelObjects()
			{
				super.onValidateModelObjects();

				((ParentModel)getModelObject()).setParentValidated(true);
			}
		};
		parentForm.add(new TextField("name"));
		parentForm.add(new TextField("company"));
		add(parentForm);

		IModel<ChildModel> childModel = new CompoundPropertyModel(parentModel);
		parentForm.add(new AddressFormPanel("addressInfo", (IModel<ChildModel>)childModel));

		parentForm.add(new AjaxSubmitLink("parentSubmitBtn", parentForm)
		{
			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form)
			{
				super.onSubmit(target, form);
				target.add(getThePage().get("validations"));
			}
		});

		add(new AjaxLink("resetSubmits")
		{
			@Override
			public void onClick(AjaxRequestTarget target)
			{
				ParentModel pm = (ParentModel)getPage().getDefaultModelObject();
				pm.setChildValidated(false);
				pm.setParentValidated(false);

				target.add(getThePage().get("validations"));
			}
		});

		WebMarkupContainer cont = new WebMarkupContainer("validations");
		cont.setOutputMarkupPlaceholderTag(true);
		add(cont);
		cont.add(new Label("parentValidated"));
		cont.add(new Label("childValidated"));

	}

	private void handleChildSubmittedCalled(AjaxRequestTarget target)
	{
		target.add(get("validations"));
	}

	private OnFormValidateModelObjectsPage getThePage()
	{
		return this;
	}

	public class ParentModel extends AddressFormPanel.ChildModel
	{
		String name = "name";
		String company = "company";

		boolean parentValidated;

		public ParentModel()
		{
			super();
		}

		public String getName()
		{
			return name;
		}

		public void setName(String name)
		{
			this.name = name;
		}

		public String getCompany()
		{
			return company;
		}

		public void setCompany(String company)
		{
			this.company = company;
		}

		public boolean isParentValidated()
		{
			return parentValidated;
		}

		public void setParentValidated(boolean parentValid)
		{
			this.parentValidated = parentValid;
		}

		@Override
		public void submittedCalled(AjaxRequestTarget target)
		{
			handleChildSubmittedCalled(target);
		}
	}
}
