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
package library;

import com.voicetribe.wicket.PageParameters;
import com.voicetribe.wicket.RequestCycle;
import com.voicetribe.wicket.markup.html.HtmlPage;
import com.voicetribe.wicket.markup.html.panel.SignInPanel;

/**
 * Simple example of a sign in page.
 * @author Jonathan Locke
 */
public final class SignIn extends HtmlPage
{
    /**
     * Constructor
     * @param parameters The page parameters
     */
    public SignIn(final PageParameters parameters)
    {
        add(new SignInPanel("signInPanel")
            {
                protected String signIn(final RequestCycle cycle,
                    final String username, final String password)
                {
                    // Sign the user in
                    final User user = Authenticator.forSession(cycle.getSession())
                                                   .authenticate(username,
                            password);

                    // If the user was signed in
                    if (user != null)
                    {
                        return null;
                    }
                    else
                    {
                        // Form method that will notify feedback panel
                        return getLocalizedString("couldNotAuthenticate");
                    }
                }
            });
    }
}

///////////////////////////////// End of File /////////////////////////////////
