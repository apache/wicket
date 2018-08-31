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
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.value.IValueMap;
import org.apache.wicket.util.value.ValueMap;


/**
 * 
 * @author marrink
 */
public class UsernamePasswordSignInPanel extends Panel
{
	/** */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 * 
	 * @param id
	 */
	public UsernamePasswordSignInPanel(final String id)
	{
		super(id);

		add(new FeedbackPanel("feedback"));
		add(new Label("naam"));
		add(new SignInForm("signInForm", this));
	}

	/**
	 * 
	 * @param username
	 * @param password
	 * @return whether signin is successful
	 */
    private boolean signIn(String username, String password)
	{
		return username.equals(password);
	}

	/**
	 * Sign in form.
	 */
	public static final class SignInForm extends StatelessForm<IValueMap>
	{
		/** For serialisation. */
		private static final long serialVersionUID = 1L;

		/** Should the login values preserved? */
		private boolean rememberMe = true;

		private final UsernamePasswordSignInPanel panel;

		/**
		 * Constructor.
		 * 
		 * @param id
		 *            id of the form component
		 * @param panel
		 */
        SignInForm(final String id, UsernamePasswordSignInPanel panel)
		{
			super(id, new CompoundPropertyModel<IValueMap>(new ValueMap()));
			this.panel = panel;

			// only save username, not passwords
			add(new TextField<String>("username"));
			add(new PasswordTextField("password"));
			// MarkupContainer row for remember me checkbox
			WebMarkupContainer rememberMeRow = new WebMarkupContainer("rememberMeRow");
			add(rememberMeRow);

			// Add rememberMe checkbox
			rememberMeRow.add(new CheckBox("rememberMe", new PropertyModel<Boolean>(this,
				"rememberMe")));
		}

		@Override
		public final void onSubmit()
		{
			if (!rememberMe)
			{
				// Verwijder de persistente waarden van het formulier
				// getPage().removePersistedFormData(SignInForm.class, true);
			}

			ValueMap values = (ValueMap)getDefaultModelObject();
			String username = values.getString("username");
			String password = values.getString("password");

			if (panel.signIn(username, password))
			{
				continueToOriginalDestination();
				setResponsePage(Application.get().getHomePage());
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
		 * @return whether the values of the form should be kept or not.
		 */
		public boolean getRememberMe()
		{
			return rememberMe;
		}

		/**
		 * Set whether the values of the form should be kept or not.
		 * 
		 * @param rememberMe
		 */
		public void setRememberMe(boolean rememberMe)
		{
			this.rememberMe = rememberMe;
			// ((FormComponent)get("username")).setPersistent(rememberMe);
		}
	}
}
