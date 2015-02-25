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
package org.apache.wicket.markup.html.form.login;

import java.io.Serializable;

import org.apache.wicket.Application;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.login.InterceptTest.MySession;
import org.apache.wicket.model.Model;


/**
 * @author marrink
 */
public class MockLoginPage extends WebPage
{
	private static final long serialVersionUID = 1L;

	private Form<Void> form;

	private TextField<Serializable> textField;

	/**
	 * 
	 */
	public MockLoginPage()
	{
		super();
		add(new Label("label", "welcome please login"));
		add(form = new Form<Void>("form")
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit()
			{
				login(get("username").getDefaultModelObjectAsString());
			}
		});
		form.add(textField = new TextField<>("username", new Model<>()));
	}

	/**
	 * 
	 * @param username
	 * @return boolean
	 */
	public boolean login(String username)
	{
		((MySession)Session.get()).setUsername(username);
		continueToOriginalDestination();
		setResponsePage(Application.get().getHomePage());
		return true;
	}

	/**
	 * 
	 * @return form
	 */
	public final Form<Void> getForm()
	{
		return form;
	}

	/**
	 * 
	 * @return textfield
	 */
	public final TextField<Serializable> getTextField()
	{
		return textField;
	}
}
