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
package org.apache.wicket.authroles.authentication.panel;

import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;

/**
 * Reusable sign in panel with a username and a password field. When the panel's form is submitted,
 * the submitted values are passed to {@link AuthenticatedWebSession#signIn(String, String)}, which
 * authenticates the user's session.
 * <p>
 * The credentials live no longer than the session: nothing is written to the client, so a user signs
 * in again once the session has ended. Wicket offers no supported way to persist credentials on the
 * client and get an automatic sign in on a later visit &mdash; anything stored there authenticates
 * the user by itself, and the framework cannot make that safe. An application that needs a
 * persistent login has to implement one, and should do so with a random, revocable, per-device token
 * rather than with the password; {@link AuthenticatedWebSession#signIn(boolean)} exists so that such
 * a token can sign a session in without being passed to
 * {@link AuthenticatedWebSession#authenticate(String, String)}.
 * 
 * @author Jonathan Locke
 * @author Juergen Donnerstag
 * @author Eelco Hillenius
 */
public class UsernamePasswordPanel extends Panel
{
	private static final long serialVersionUID = 1L;

	private static final String SIGN_IN_FORM = "signInForm";

	/** password. */
	private String password;

	/** user name. */
	private String username;

	/**
	 * @see org.apache.wicket.Component#Component(String)
	 */
	public UsernamePasswordPanel(final String id)
	{
		super(id);

		// Create feedback panel and add to page
		add(new FeedbackPanel("feedback"));

		// Add sign-in form to page, passing feedback panel as
		// validation error handler
		add(new SignInForm(SIGN_IN_FORM));
	}

	/**
	 * 
	 * @return signin form
	 */
	protected SignInForm getForm()
	{
		return (SignInForm)get(SIGN_IN_FORM);
	}

	/**
	 * Convenience method to access the password.
	 * 
	 * @return The password
	 */
	public String getPassword()
	{
		return password;
	}

	/**
	 * Set the password
	 * 
	 * @param password
	 */
	public void setPassword(final String password)
	{
		this.password = password;
	}

	/**
	 * Convenience method to access the username.
	 * 
	 * @return The user name
	 */
	public String getUsername()
	{
		return username;
	}

	/**
	 * Set the username
	 * 
	 * @param username
	 */
	public void setUsername(final String username)
	{
		this.username = username;
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
	private boolean signIn(String username, String password)
	{
		return AuthenticatedWebSession.get().signIn(username, password);
	}

	/**
	 * Called when sign in failed
	 */
	protected void onSignInFailed()
	{
		// Try the component based localizer first. If not found try the
		// application localizer. Else use the default
		error(getLocalizer().getString("signInFailed", this, "Sign in failed"));
	}

	/**
	 * Called when sign in was successful
	 */
	protected void onSignInSucceeded()
	{
		// If login has been called because the user was not yet logged in, than continue to the
		// original destination, otherwise to the Home page
		continueToOriginalDestination();
		setResponsePage(getApplication().getHomePage());
	}

	/**
	 * Sign in form.
	 */
	public final class SignInForm extends StatelessForm<UsernamePasswordPanel>
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Constructor.
		 * 
		 * @param id
		 *            id of the form component
		 */
		public SignInForm(final String id)
		{
			super(id);

			setModel(new CompoundPropertyModel<>(UsernamePasswordPanel.this));

			// Attach textfields for username and password
			add(new TextField<>("username").setRequired(true));
			add(new PasswordTextField("password"));
		}

		@Override
		public void onSubmit()
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
}
