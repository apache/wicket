/*
 * $Id: SignIn.java 6427 2006-07-08 09:17:31 +0000 (Sat, 08 Jul 2006)
 * jdonnerstag $ $Revision$ $Date: 2006-05-26 00:57:30 +0200 (vr, 26 mei
 * 2006) $
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.examples.signin;

import wicket.MarkupContainer;
import wicket.PageParameters;
import wicket.examples.WicketExamplePage;
import wicket.markup.html.form.PasswordTextField;
import wicket.markup.html.form.StatelessForm;
import wicket.markup.html.form.TextField;
import wicket.markup.html.panel.FeedbackPanel;
import wicket.model.PropertyModel;
import wicket.util.value.ValueMap;

/**
 * Simple example of a sign in page.
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
		this(null);
	}

	/**
	 * Constructor
	 * 
	 * @param parameters
	 *            The page parameters
	 */
	public SignIn(final PageParameters parameters)
	{
		// Create feedback panel and add to page
		new FeedbackPanel(this, "feedback");

		// Add sign-in form to page, passing feedback panel as validation error
		// handler
		new SignInForm(this, "signInForm");
	}

	/**
	 * Sign in form
	 * 
	 * @author Jonathan Locke
	 */
	public final class SignInForm extends StatelessForm
	{
		// El-cheapo model for form
		private final ValueMap properties = new ValueMap();

		/**
		 * Constructor
		 * 
		 * @param parent
		 * @param id
		 *            id of the form component
		 */
		public SignInForm(final MarkupContainer parent, final String id)
		{
			super(parent, id);

			// Attach textfield components that edit properties map model
			new TextField<String>(this, "username", new PropertyModel<String>(properties,
					"username"));
			new PasswordTextField(this, "password", new PropertyModel<String>(properties,
					"password"));
		}

		/**
		 * @see wicket.markup.html.form.Form#onSubmit()
		 */
		@Override
		public final void onSubmit()
		{
			// Get session info
			SignInSession session = (SignInSession)getSession();

			// Sign the user in
			if (session.authenticate(properties.getString("username"), properties
					.getString("password")))
			{
				if (!continueToOriginalDestination())
				{
					setResponsePage(new Home(PageParameters.NULL));
				}
			}
			else
			{
				// Form method that will notify feedback panel
				// Try the component based localizer first. If not found try the
				// application localizer. Else use the default
				final String errmsg = getLocalizer().getString("loginError", this,
						"Unable to sign you in");
				error(errmsg);
			}
		}
	}
}
