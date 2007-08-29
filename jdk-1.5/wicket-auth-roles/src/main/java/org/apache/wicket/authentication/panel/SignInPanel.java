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
package org.apache.wicket.authentication.panel;

import org.apache.wicket.PageParameters;
import org.apache.wicket.authentication.AuthenticatedWebSession;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.value.ValueMap;


/**
 * Reusable user sign in panel with username and password as well as support for
 * cookie persistence of the both. When the SignInPanel's form is submitted, the
 * method signIn(String, String) is called, passing the username and password
 * submitted. The signIn() method should authenticate the user's session. The
 * default implementation calls AuthenticatedWebSession.get().signIn().
 * 
 * @author Jonathan Locke
 * @author Juergen Donnerstag
 * @author Eelco Hillenius
 */
public class SignInPanel extends Panel
{
	private static final long serialVersionUID = 1L;

	/** True if the panel should display a remember-me checkbox */
	private boolean includeRememberMe = true;

	/** Field for password. */
	private PasswordTextField password;

	/** True if the user should be remembered via form persistence (cookies) */
	private boolean rememberMe = true;

	/** Field for user name. */
	private TextField username;

	/**
	 * Sign in form.
	 */
	public final class SignInForm extends Form
	{
		private static final long serialVersionUID = 1L;

		/** El-cheapo model for form. */
		private final ValueMap properties = new ValueMap();

		/**
		 * Constructor.
		 * 
		 * @param id
		 *            id of the form component
		 */
		public SignInForm(final String id)
		{
			super(id);

			// Attach textfield components that edit properties map
			// in lieu of a formal beans model
			add(username = new TextField("username", new PropertyModel(properties, "username")));
			add(password = new PasswordTextField("password", new PropertyModel(properties,
					"password")));

			// MarkupContainer row for remember me checkbox
			final WebMarkupContainer rememberMeRow = new WebMarkupContainer("rememberMeRow");
			add(rememberMeRow);

			// Add rememberMe checkbox
			rememberMeRow.add(new CheckBox("rememberMe", new PropertyModel(SignInPanel.this,
					"rememberMe")));

			// Make form values persistent
			setPersistent(rememberMe);

			// Show remember me checkbox?
			rememberMeRow.setVisible(includeRememberMe);
		}

		/**
		 * @see org.apache.wicket.markup.html.form.Form#onSubmit()
		 */
		public final void onSubmit()
		{
			if (signIn(getUsername(), getPassword()))
			{
			    onSignInSucceeded();
			}
			else
			{
			    onSignInFailed();
			}
		}
	}

	/**
	 * @see org.apache.wicket.Component#Component(String)
	 */
	public SignInPanel(final String id)
	{
		this(id, true);
	}

	/**
	 * @param id
	 *            See Component constructor
	 * @param includeRememberMe
	 *            True if form should include a remember-me checkbox
	 * @see org.apache.wicket.Component#Component(String)
	 */
	public SignInPanel(final String id, final boolean includeRememberMe)
	{
		super(id);

		this.includeRememberMe = includeRememberMe;

		// Create feedback panel and add to page
		final FeedbackPanel feedback = new FeedbackPanel("feedback");
		add(feedback);

		// Add sign-in form to page, passing feedback panel as
		// validation error handler
		add(new SignInForm("signInForm"));
	}

	/**
	 * Removes persisted form data for the signin panel (forget me)
	 */
	public final void forgetMe()
	{
		// Remove persisted user data. Search for child component
		// of type SignInForm and remove its related persistence values.
		getPage().removePersistedFormData(SignInPanel.SignInForm.class, true);
	}

	/**
	 * Convenience method to access the password.
	 * 
	 * @return The password
	 */
	public String getPassword()
	{
		return password.getInput();
	}

	/**
	 * Get model object of the rememberMe checkbox
	 * 
	 * @return True if user should be remembered in the future
	 */
	public boolean getRememberMe()
	{
		return rememberMe;
	}

	/**
	 * Convenience method to access the username.
	 * 
	 * @return The user name
	 */
	public String getUsername()
	{
		return username.getModelObjectAsString();
	}

	/**
	 * Convenience method set persistence for username and password.
	 * 
	 * @param enable
	 *            Whether the fields should be persistent
	 */
	public void setPersistent(final boolean enable)
	{
		username.setPersistent(enable);
	}

	/**
	 * Set model object for rememberMe checkbox
	 * 
	 * @param rememberMe
	 */
	public void setRememberMe(final boolean rememberMe)
	{
		this.rememberMe = rememberMe;
		this.setPersistent(rememberMe);
	}

	/**
	 * Sign in user if possible.
	 * 
	 * @param username
	 *            The username
	 * @param password
	 *            The password
	 * @return True if signin was successful
	 */
	public boolean signIn(String username, String password)
	{
		return AuthenticatedWebSession.get().signIn(username, password);
	}

	protected void onSignInFailed()
	{
		// Try the component based localizer first. If not found try the
		// application localizer. Else use the default
		error(getLocalizer().getString("signInFailed", this, "Sign in failed"));
	}

	protected void onSignInSucceeded()
	{
		// If login has been called because the user was not yet
		// logged in, than continue to the original destination,
		// otherwise to the Home page
		if (!continueToOriginalDestination())
		{
			setResponsePage(getApplication().getSessionSettings().getPageFactory().newPage(
					getApplication().getHomePage(), (PageParameters)null));
		}
	}

}
