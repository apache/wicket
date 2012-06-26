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
package org.apache.wicket.util.tester.apps_7;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;

/**
 * Homepage
 */
public class HomePage extends WebPage
{
	private static final long serialVersionUID = 1L;

	/**
	 */
	public HomePage()
	{
		this(true, 0);
	}

	/**
	 * @param enableInputField
	 * @param newPageId
	 */
	public HomePage(boolean enableInputField, int newPageId)
	{
		// Add the simplest type of label
		add(new Label("message",
			"If you see this message wicket is properly configured and running"));

		Form<?> form;
		add(form = new Form<Void>("form"));
		form.add(new TextField<String>("inputField", new Model<String>()));
		form.add(new Button("hiddenButton").setVisible(false));
		form.add(new AjaxButton("ajaxButton")
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form)
			{

			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form)
			{
			}
		});
	}
}
