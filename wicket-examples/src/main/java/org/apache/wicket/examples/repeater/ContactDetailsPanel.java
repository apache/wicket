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
package org.apache.wicket.examples.repeater;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;

/**
 * Shows the data of a contact, and nothing while there is none.
 */
public class ContactDetailsPanel extends GenericPanel<Contact>
{
	private static final long serialVersionUID = 1L;

	/**
	 * @param id
	 *            component id
	 * @param model
	 *            the contact to show
	 */
	public ContactDetailsPanel(String id, IModel<Contact> model)
	{
		super(id, model);
		setOutputMarkupPlaceholderTag(true);

		add(new Label("id", model.map(Contact::getId)));
		add(new Label("firstName", model.map(Contact::getFirstName)));
		add(new Label("lastName", model.map(Contact::getLastName)));
		add(new Label("homePhone", model.map(Contact::getHomePhone)));
		add(new Label("cellPhone", model.map(Contact::getCellPhone)));
		add(new Label("bornDate", model.map(Contact::getBornDate)));
	}

	@Override
	protected void onConfigure()
	{
		super.onConfigure();
		setVisible(getModelObject() != null);
	}
}
