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

import wicket.examples.util.NavigationPanel;
import wicket.markup.html.HtmlPage;
import wicket.markup.html.panel.SignInPanel;

/**
 * Abstract base class for a typical login page
 * 
 * @author Juergen Donnerstag
 */
public abstract class SignInPage extends HtmlPage
{
	private final SignInPanel signInPanel;

    /**
     * 
     * Constructor
     * @param signInPanel Sign in panel to use
     */
    protected SignInPage(SignInPanel signInPanel)
    {
    	super();
    	add(new NavigationPanel("mainNavigation", "Signin example"));
    	this.signInPanel = signInPanel;
        add(signInPanel);
    }
   
	/**
	 * @see wicket.Page#checkAccess()
	 */
	protected boolean checkAccess() 
    {
        // Log the user in
        if (null == signInPanel.signIn(signInPanel.getUsername(), signInPanel.getPassword()))
        {
        	// Login successful
            if (getRequestCycle().continueToOriginalDestination())
            {
            	// Page successfully redirected. No need to render page.
                return HtmlPage.ACCESS_DENIED;
            }
        }
        
        return HtmlPage.ACCESS_ALLOWED;
    }
}

