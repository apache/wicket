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
package org.apache.wicket.markup.html.pages;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.protocol.http.ClientProperties;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

/**
 * Form for posting JavaScript properties.
 */
public class BrowserInfoForm extends GenericPanel<ClientProperties>
{
	private static final long serialVersionUID = 1L;
	
	public static final ResourceReference JS = new JavaScriptResourceReference(BrowserInfoForm.class, "wicket-browser-info.js");

	/**
	 * The special form that submits the client/browser info
	 */
	private final Form<ClientProperties> form;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            component id
	 */
	public BrowserInfoForm(String id, IModel<ClientProperties> properties)
	{
		super(id, properties);

		this.form = createForm("postback", properties);
		form.setOutputMarkupId(true);
		add(form);
	}

	/**
	 * Creates the form
	 *
	 * @param componentId
	 *      the id for the Form component
	 * @return the Form that will submit the data
	 */
	protected Form<ClientProperties> createForm(String componentId, IModel<ClientProperties> properties)
	{
		Form<ClientProperties> form = new Form<ClientProperties>(componentId, properties)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit()
			{
				getModelObject().read(getRequest().getPostParameters());

				afterSubmit();
			}
		};
		return form;
	}

	protected void afterSubmit()
	{
	}

	@Override
	public void renderHead(IHeaderResponse response)
	{
		super.renderHead(response);

		response.render(JavaScriptHeaderItem.forReference(JS));
	}

	/**
	 * @return The markup id of the form that submits the client info
	 */
	public String getFormMarkupId()
	{
		return form.getMarkupId();
	}
}