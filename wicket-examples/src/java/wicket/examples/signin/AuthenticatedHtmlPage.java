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
package wicket.examples.signin;

import wicket.markup.html.HtmlPage;

/**
 *
 * @author Jonathan Locke
 */
public class AuthenticatedHtmlPage extends HtmlPage
{
    /**
     * @see wicket.Page#checkAccess()
     */
    protected boolean checkAccess()
    {
        // Is a user signed into this cycle's session?
        boolean signedIn = getSession().getProperty("wicket.examples.signin.user") != null;

        // If nobody is signed in
        if (!signedIn)
        {
            // Redirect request to SignIn page
            getRequestCycle().redirectToInterceptPage(SignIn.class);
        }

        // Return true if someone is signed in and access is okay
        return signedIn;
    }
}
