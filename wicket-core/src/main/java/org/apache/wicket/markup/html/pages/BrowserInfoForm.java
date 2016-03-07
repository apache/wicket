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

import org.apache.wicket.model.IModel;
import org.apache.wicket.protocol.http.ClientProperties;

/**
 * Form for posting JavaScript properties.
 */
public class BrowserInfoForm extends AbstractBrowserInfoForm<ClientProperties>
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            component id
	 */
	public BrowserInfoForm(String id, IModel<ClientProperties> properties)
	{
		super(id, properties);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void afterSubmit(ClientProperties properties)
	{
		getModelObject().setJavaScriptEnabled(true);

		continueToOriginalDestination();

		// switch to home page if no original destination was intercepted
		setResponsePage(getApplication().getHomePage());
	}
}
