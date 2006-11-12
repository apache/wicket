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
package wicket.markup.html.form.login;

import wicket.Application;
import wicket.Session;
import wicket.markup.html.WebPage;
import wicket.markup.html.basic.Label;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.TextField;
import wicket.markup.html.form.login.InterceptTest.MySession;
import wicket.model.Model;

/**
 * Login test page.
 * 
 * @author Maurice Marrink
 */
public class MockLoginPage extends WebPage<Object>
{
	private static final long serialVersionUID = 1L;

	private Form form;

	private TextField textField;

	/**
	 * Construct.
	 */
	public MockLoginPage()
	{
		super();
		new Label(this, "label", "welcome please login");
		form = new Form(this, "form")
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit()
			{
				login(get("username").getModelObjectAsString().toString());
			}
		};
		textField = new TextField<String>(form, "username", new Model<String>());
	}

	/**
	 * @return form
	 */
	public final Form getForm()
	{
		return form;
	}

	/**
	 * @return textfield
	 */
	public final TextField getTextField()
	{
		return textField;
	}

	/**
	 * Logs in with the provided user name.
	 * 
	 * @param username
	 * @return boolean
	 */
	public boolean login(String username)
	{
		((MySession)Session.get()).setUsername(username);
		if (!continueToOriginalDestination())
		{
			setResponsePage(Application.get().getHomePage());
		}
		return true;
	}
}
