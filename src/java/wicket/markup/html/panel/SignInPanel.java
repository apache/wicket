/*
 * $Id$ $Revision:
 * 1.11 $ $Date$
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
package wicket.markup.html.panel;

import wicket.Page;
import wicket.PageParameters;
import wicket.RequestCycle;
import wicket.markup.html.WebContainer;
import wicket.markup.html.form.CheckBox;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.PasswordTextField;
import wicket.markup.html.form.TextField;
import wicket.markup.html.form.validation.IValidationFeedback;
import wicket.util.value.ValueMap;

/**
 * Reusable user sign in panel with username and password as well as support for
 * cookie persistence of the both. When the SignInPanel's form is submitted, the
 * abstract method signIn(String, String) is called, passing the username and
 * password submitted. The signIn() method should sign the user in and return
 * null if no error ocurred, or a descriptive String in the event that the sign
 * in fails.
 * 
 * @author Jonathan Locke
 * @author Juergen Donnerstag
 * @author Eelco Hillenius
 */
public abstract class SignInPanel extends Panel
{
	/** Field for user name. */
	private TextField username;

	/** Field for password. */
	private PasswordTextField password;
    
    /** True if the user should be remembered via form persistence (cookies) */
    private boolean rememberMe = true;
    
    /** True if the panel should display a remember-me checkbox */
    private boolean includeRememberMe = true;

    /**
     * @see wicket.Component#Component(String)
     */
    public SignInPanel(final String componentName)
    {
    	this(componentName, true);
    }

    /**
     * @param componentName See Component constructor
     * @param includeRememberMe True if form should include a remember-me checkbox
     * @see wicket.Component#Component(String)
     */
    public SignInPanel(final String componentName, final boolean includeRememberMe)
	{
		super(componentName);
        
        this.includeRememberMe = includeRememberMe;

		// Create feedback panel and add to page
		final FeedbackPanel feedback = new FeedbackPanel("feedback");
		add(feedback);

		// Add sign-in form to page, passing feedback panel as
		// validation error handler
		add(new SignInForm("signInForm", feedback));
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
	 * Convenience method to access the password.
	 * 
	 * @return The password
	 */
	public String getPassword()
	{
		return password.getModelObjectAsString();
	}

	/**
	 * Convenience method set persistence for username and password.
	 * 
	 * @param enable
	 *            Whether the fields should be persistent
	 */
	public void setPersistent(boolean enable)
	{
		username.setPersistent(enable);
		password.setPersistent(enable);
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
	public abstract boolean signIn(final String username, final String password);

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
     * Set model object for rememberMe checkbox
     * 
     * @param rememberMe
     */
    public void setRememberMe(boolean rememberMe)
    {
        this.rememberMe = rememberMe;
        this.setPersistent(rememberMe);
    }

	/**
	 * Sign in form.
	 */
	public final class SignInForm extends Form
	{
		/** Serial Version ID. */
		private static final long serialVersionUID = 303695648327317416L;

		/** El-cheapo model for form. */
		private final ValueMap properties = new ValueMap();

		/**
		 * Constructor.
		 * 
		 * @param componentName
		 *            Name of the form component
		 * @param feedback
		 *            The feedback panel to update
		 */
		public SignInForm(final String componentName, final IValidationFeedback feedback)
		{
			super(componentName, feedback);

			// Attach textfield components that edit properties map
			// in lieu of a formal beans model
			add(username = new TextField("username", properties, "username"));
			add(password = new PasswordTextField("password", properties, "password"));
            
            // Container row for remember me checkbox
            WebContainer rememberMeRow = new WebContainer("rememberMeRow");
            add(rememberMeRow);

            // Add rememberMe checkbox
            rememberMeRow.add(new CheckBox("rememberMe", SignInPanel.this, "rememberMe"));

            // Make form values persistent
            setPersistent(rememberMe);   

            // Show remember me checkbox?
            rememberMeRow.setVisible(includeRememberMe);
		}

		/**
		 * @see wicket.markup.html.form.Form#handleSubmit()
		 */
		public final void handleSubmit()
		{
			if (signIn(getUsername(), getPassword()))
			{
				// Get active request cycle
				final RequestCycle cycle = getRequestCycle();

				// If login has been called because the user was not yet
				// logged in, than continue to the original destination,
				// otherwise to the Home page
				if (cycle.continueToOriginalDestination())
				{
					// HTTP redirect response has been committed. No more data
					// shall be written to the response.
					cycle.setPage((Page)null);
				}
				else
				{
					cycle.setPage(getApplicationSettings().getDefaultPageFactory().newPage(
							getApplicationPages().getHomePage(), (PageParameters)null));
				}
			}
            else
            {
                // TODO this should be localized
            	error("Unable to sign you in");
            }
		}
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
}

