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

import wicket.examples.WicketExamplePage;

/**
 * Base class to check access to a page. If user is not logged in,
 * redirect to the log-in page.
 *  
 * @author Jonathan Locke
 */
public class AuthenticatedWebPage extends WicketExamplePage
{
    /**
     * Get downcast session object
     * 
     * @return The session
     */
    public SignIn2Session getSignIn2Session()
    {
        return (SignIn2Session)getSession();
    }
    
    /**
     * @see wicket.Page#checkAccess()
     */
    protected boolean checkAccess()
    {
        // Is a user signed into this cycle's session?
        boolean signedIn = getSignIn2Session().isSignedIn();

        // If nobody is signed in
        if (!signedIn)
        {
            // Redirect request to SignIn page
            redirectToInterceptPage(newPage(SignIn2.class));
        }

        // Return true if someone is signed in and access is okay
        return signedIn;
    }
}


