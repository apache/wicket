/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.examples.signin2;

import wicket.PageParameters;
import wicket.RequestCycle;
import wicket.markup.html.panel.SignInPanel;

/**
 * Simple example of a sign in page. It extends SignInPage, a
 * base class which provide standard functionality for 
 * typical log-in pages
 * 
 * @author Jonathan Locke
 */
public final class SignIn2 extends SignInPage
{
    /**
     * Constructor
     * @param parameters The page parameters
     */
    public SignIn2(final PageParameters parameters)
    {
        super(new SignIn2Panel("signInPanel")
        {
            public String signIn(String username, String password)
            {
                // Sign the user in
                if (username.equals("jonathan") && password.equals("password"))
                {
                    // Successfully signed in.
                    getSession().setProperty("wicket.examples.signin2.user", "jonathan");
                    
                    // Depending on user's choice, remember me or not
                    this.setPersistent(this.getRememberMe());
                    return null;
                }

                // error
                return "Couldn't sign you in";
            }
        });
    }
	
	/**
	 * 
	 */
	public void logout()
	{
        // Invalidate session
        RequestCycle.get().getSession().invalidate();
		
		// Remove persisted user data. Search for child component
        // of type SignInForm and remove its related persistence values.
		removePersistedFormData(SignInPanel.SignInForm.class, true);
	}
}

