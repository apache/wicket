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
package org.apache.wicket.examples.authentication1;

import org.apache.wicket.examples.WicketExamplePage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.value.ValueMap;


/**
 * Simple example of a sign in page. Even simpler, as shown in the authentication-2 example, is
 * using the SignInPanel from the auth-role package. Beside that this simple example does not
 * support "rememberMe".
 * 
 * @author Jonathan Locke
 */
public final class SignIn extends WicketExamplePage
{
	/**
	 * Constructor
	 */
	public SignIn()
	{
		// Create feedback panel and add to page
		add(new FeedbackPanel("feedback"));

		// Add sign-in form to page
		add(new SignInForm("signInForm"));
	}

	/**
	 * Sign in form
	 */
	public final class SignInForm extends Form<Void>
	{
		private static final String USERNAME = "username";
		private static final String PASSWORD = "password";

		// El-cheapo model for form
		private final ValueMap properties = new ValueMap();

		/**
		 * Constructor
		 * 
		 * @param id
		 *            id of the form component
		 */
		public SignInForm(final String id)
		{
			super(id);

			// Attach textfield components that edit properties map model
			add(new TextField<>(USERNAME, new PropertyModel<String>(properties, USERNAME)));
			add(new PasswordTextField(PASSWORD, new PropertyModel<String>(properties, PASSWORD)));
		}

		/**
		 * @see org.apache.wicket.markup.html.form.Form#onSubmit()
		 */
		@Override
		public final void onSubmit()
		{
			// Get session info
			SignInSession session = getMySession();

			// Sign the user in
			if (session.signIn(getUsername(), getPassword()))
			{
				continueToOriginalDestination();
				setResponsePage(getApplication().getHomePage());
			}
			else
			{
				// Get the error message from the properties file associated with the Component
				String errmsg = getString("loginError", null, "Unable to sign you in");

				// Register the error message with the feedback panel
				error(errmsg);
			}
		}

		/**
		 * @return
		 */
		private String getPassword()
		{
			return properties.getString(PASSWORD);
		}

		/**
		 * @return
		 */
		private String getUsername()
		{
			return properties.getString(USERNAME);
		}

		/**
		 * @return
		 */
		private SignInSession getMySession()
		{
			return (SignInSession)getSession();
		}
	}
}
