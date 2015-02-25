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

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.authentication.IAuthenticationStrategy;
import org.apache.wicket.authentication.strategy.DefaultAuthenticationStrategy;
import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;

/**
 * Reusable user sign in panel with username and password as well as support for persistence of the
 * both. When the SignInPanel's form is submitted, the method signIn(String, String) is called,
 * passing the username and password submitted. The signIn() method should authenticate the user's
 * session.
 * 
 * @see {@link IAuthenticationStrategy}
 * @see {@link org.apache.wicket.settings.SecuritySettings#getAuthenticationStrategy()}
 * @see {@link DefaultAuthenticationStrategy}
 *
 * @author Jonathan Locke
 * @author Juergen Donnerstag
 * @author Eelco Hillenius
 */
public class SignInPanel extends Panel
{
	private static final long serialVersionUID = 1L;

	private static final String SIGN_IN_FORM = "signInForm";

	/** True if the panel should display a remember-me checkbox */
	private boolean includeRememberMe = true;

	/** True if the user should be remembered via form persistence (cookies) */
	private boolean rememberMe = true;

	/** password. */
	private String password;

	/** user name. */
	private String username;

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
	 * Try to sign-in with remembered credentials.
	 * 
	 * @see #setRememberMe(boolean)
	 */
	@Override
	protected void onConfigure()
	{
		// logged in already?
		if (isSignedIn() == false)
		{
			IAuthenticationStrategy authenticationStrategy = getApplication().getSecuritySettings()
				.getAuthenticationStrategy();
			// get username and password from persistence store
			String[] data = authenticationStrategy.load();

			if ((data != null) && (data.length > 1))
			{
				// try to sign in the user
				if (signIn(data[0], data[1]))
				{
					username = data[0];
					password = data[1];

					onSignInRemembered();
				}
				else
				{
					// the loaded credentials are wrong. erase them.
					authenticationStrategy.remove();
				}
			}
		}

		super.onConfigure();
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
	 * Get model object of the rememberMe checkbox
	 * 
	 * @return True if user should be remembered in the future
	 */
	public boolean getRememberMe()
	{
		return rememberMe;
	}

	/**
	 * @param rememberMe
	 *            If true, rememberMe will be enabled (username and password will be persisted
	 *            somewhere)
	 */
	public void setRememberMe(final boolean rememberMe)
	{
		this.rememberMe = rememberMe;
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
	 * @return true, if signed in
	 */
	private boolean isSignedIn()
	{
		return AuthenticatedWebSession.get().isSignedIn();
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
	 * Called when sign-in was remembered.
	 * <p>
	 * By default tries to continue to the original destination or switches to the application's
	 * home page.
	 * <p>
	 * Note: This method will be called during rendering of this panel, thus a
	 * {@link RestartResponseException} has to be used to switch to a different page.
	 * 
	 * @see #onConfigure()
	 */
	protected void onSignInRemembered()
	{
		// logon successful. Continue to the original destination
		continueToOriginalDestination();

		// Ups, no original destination. Go to the home page
		throw new RestartResponseException(getApplication().getHomePage());
	}

	/**
	 * Sign in form.
	 */
	public final class SignInForm extends StatelessForm<SignInPanel>
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

			setModel(new CompoundPropertyModel<>(SignInPanel.this));

			// Attach textfields for username and password
			add(new TextField<>("username").setRequired(true));
			add(new PasswordTextField("password"));

			// container for remember me checkbox
			WebMarkupContainer rememberMeContainer = new WebMarkupContainer("rememberMeContainer");
			add(rememberMeContainer);

			// Add rememberMe checkbox
			rememberMeContainer.add(new CheckBox("rememberMe"));

			// Show remember me checkbox?
			rememberMeContainer.setVisible(includeRememberMe);
		}

		/**
		 * @see org.apache.wicket.markup.html.form.Form#onSubmit()
		 */
		@Override
		public final void onSubmit()
		{
			IAuthenticationStrategy strategy = getApplication().getSecuritySettings()
				.getAuthenticationStrategy();

			if (signIn(getUsername(), getPassword()))
			{
				if (rememberMe == true)
				{
					strategy.save(username, password);
				}
				else
				{
					strategy.remove();
				}

				onSignInSucceeded();
			}
			else
			{
				onSignInFailed();
				strategy.remove();
			}
		}
	}
}
