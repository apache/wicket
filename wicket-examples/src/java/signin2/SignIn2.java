///////////////////////////////////////////////////////////////////////////////////
//
// Created May 21, 2004
//
// Copyright 2004, Jonathan W. Locke
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

package signin2;

import com.voicetribe.wicket.PageParameters;
import com.voicetribe.wicket.RequestCycle;
import com.voicetribe.wicket.markup.html.HtmlPage;
import com.voicetribe.wicket.markup.html.panel.SignInPanel;

/**
 * Simple example of a sign in page.
 * @author Jonathan Locke
 */
public final class SignIn2 extends HtmlPage
{
    /**
     * Constructor
     * @param parameters The page parameters
     */
    public SignIn2(final PageParameters parameters)
    {
        add(new SignInPanel("signInPanel")
        {
            protected String signIn(RequestCycle cycle, String username, String password)
            {
                // Sign the user in
                if (username.equals("jonathan") && password.equals("password"))
                {
                    cycle.getSession().setProperty("user", "jonathan");
                    return null;
                }
                else
                {
                    return "Couldn't sign you in";
                }
            }
        });
    }
}

///////////////////////////////// End of File /////////////////////////////////
