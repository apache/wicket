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
package org.apache.wicket.ajax.markup.html.form;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.Model;

/**
 * Homepage
 */
public class HomePage extends WebPage
{
	private static final long serialVersionUID = 1L;
	private final TestForm form;

	/**
	 *
	 */
	public HomePage()
	{
		add(new Label("message",
			"If you see this message wicket is properly configured and running"));
		add(form = new TestForm("form"));
	}

	/**
	 * @return the form
	 */
	public TestForm getForm()
	{
		return form;
	}
}

class TestForm extends Form<Void>
{
	private static final long serialVersionUID = 1L;
	private final Button submitButton;

	public TestForm(String id)
	{
		super(id);
		add((new Button("cancel", Model.of("I am not empty label"))
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmitBeforeForm()
			{
				throw new IllegalStateException("CANCEL button hit!");
			}
		}).setDefaultFormProcessing(false));

		add((submitButton = new AjaxButton("submit")
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmitBeforeForm(AjaxRequestTarget target, Form<?> form)
			{
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form)
			{
			}
		}).setDefaultFormProcessing(false));
	}

	@Override
	protected void onSubmit()
	{
		throw new IllegalStateException("Submit not pressed via ajax!");
	}

	/**
	 * @return the submitButton
	 */
	public Button getSubmitButton()
	{
		return submitButton;
	}
}
