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
package org.apache.wicket.stateless.pages;

import org.apache.wicket.Application;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.value.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 * @author marrink
 *
 */
public class UsernamePasswordSignInPanel extends Panel
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(UsernamePasswordSignInPanel.class);
	/**
	 * Constructor.
	 */
	public UsernamePasswordSignInPanel(final String id)
	{
		super(id);

		add(new FeedbackPanel("feedback"));
		add(new Label("naam"));
		add(new SignInForm("signInForm"));
	}

	/**
	 * 
	 * @param username
	 * @param password
	 * @return
	 */
	public boolean signIn(String username, String password)
	{
		return true;
	}

	/**
	 * Sign in form.
	 */
	public final class SignInForm extends StatelessForm
	{
		/** Voor serializatie. */
		private static final long serialVersionUID = 1L;

		/** Moeten de inlog waarden bewaard blijven? */
		private boolean rememberMe = true;

		/**
		 * Constructor.
		 * @param id id of the form component
		 */
		public SignInForm(final String id)
		{
			super(id, new CompoundPropertyModel(new ValueMap()));

			// only save username, not passwords
			add(new TextField("username").setPersistent(rememberMe));
			add(new PasswordTextField("password"));
			// MarkupContainer row for remember me checkbox
			WebMarkupContainer rememberMeRow = new WebMarkupContainer("rememberMeRow");
			add(rememberMeRow);

			// Add rememberMe checkbox
			rememberMeRow.add(new CheckBox("rememberMe", new PropertyModel(this, "rememberMe")));
		}

		/**
		 * @see wicket.markup.html.form.Form#onSubmit()
		 */
		public final void onSubmit()
		{
			if (!rememberMe)
			{
				// Verwijder de persistente waarden van het formulier
				getPage().removePersistedFormData(SignInForm.class, true);
			}

			ValueMap values = (ValueMap) getModelObject();
			String username = values.getString("username");
			String password = values.getString("password");

			if (signIn(username, password))
			{
				if (!getPage().continueToOriginalDestination())
				{
					setResponsePage(Application.get().getHomePage());
				}
			}
			else
			{
				// Try the component based localizer first. If not found try the
				// application localizer. Else use the default
				error(getLocalizer().getString("exception.login", this,
						"Illegal username password combo"));
			}
		}

		/**
		 * Geeft terug of de waarden van het formulier bewaard moeten worden of niet.
		 */
		public boolean getRememberMe()
		{
			return rememberMe;
		}

		/**
		 * Zet of de waarden van het formulier bewaard moeten worden of niet.
		 */
		public void setRememberMe(boolean rememberMe)
		{
			this.rememberMe = rememberMe;
			((FormComponent) get("username")).setPersistent(rememberMe);
		}
	}
}
