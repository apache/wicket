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
package org.apache.wicket.ajax.form;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;

/**
 * Homepage
 */
public class AjaxFormSubmitBehaviorTestPage extends WebPage
{
	private static final long serialVersionUID = 1L;
	private final TestForm form;

	/**
	 * Construct.
	 */
	public AjaxFormSubmitBehaviorTestPage()
	{
		add(form = new TestForm("form"));
	}

	/**
	 * @return form
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
	private final TextField<String> textField;
	private boolean submitedByAjaxBehavior;

	public TestForm(String id)
	{
		super(id);
		textField = new TextField<String>("textField", new Model<String>());
		textField.add(new AjaxFormSubmitBehavior("onchange")
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onError(AjaxRequestTarget target)
			{
			}

			@Override
			protected void onSubmitBeforeForm(AjaxRequestTarget target)
			{
				submitedByAjaxBehavior = true;
			}
		});
		add(textField);
		add(new SubmitLink("ajaxSubmitLink")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmitBeforeForm()
			{
				throw new IllegalStateException("Submit link hit!");
			}
		});

		add((submitButton = new Button("submit")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmitBeforeForm()
			{
				throw new IllegalStateException("Submit button hit!");
			}
		}));
	}


	/**
	 * @return the textField
	 */
	public TextField<String> getTextField()
	{
		return textField;
	}


	/**
	 * @return the submitedByAjaxBehavior
	 */
	public boolean isSubmitedByAjaxBehavior()
	{
		return submitedByAjaxBehavior;
	}

}
