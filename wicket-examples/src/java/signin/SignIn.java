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
package signin;

import com.voicetribe.util.value.ValueMap;
import com.voicetribe.wicket.PageParameters;
import com.voicetribe.wicket.RequestCycle;
import com.voicetribe.wicket.markup.html.HtmlPage;
import com.voicetribe.wicket.markup.html.form.Form;
import com.voicetribe.wicket.markup.html.form.PasswordTextField;
import com.voicetribe.wicket.markup.html.form.TextField;
import com.voicetribe.wicket.markup.html.panel.FeedbackPanel;

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
        // Create feedback panel and add to page
        final FeedbackPanel feedback = new FeedbackPanel("feedback");

        add(feedback);

        // Add sign-in form to page, passing feedback panel as validation error handler
        add(new SignInForm("signInForm", feedback));
    }

    /**
     * Sign in form
     * @author Jonathan Locke
     */
    public final class SignInForm extends Form
    {
        // El-cheapo model for form
        private final ValueMap properties = new ValueMap();

        /**
         * Constructor
         * @param componentName Name of the form component
         * @param feedback The feedback panel to update
         */
        public SignInForm(final String componentName,
            final FeedbackPanel feedback)
        {
            super(componentName, feedback);

            // Attach textfield components that edit properties map model
            add(new TextField("username", properties, "username"));
            add(new PasswordTextField("password", properties, "password"));
        }

        /**
         * @see com.voicetribe.wicket.markup.html.form.Form#handleSubmit(com.voicetribe.wicket.RequestCycle)
         */
        public final void handleSubmit(final RequestCycle cycle)
        {
            // Sign the user in
            if (properties.getString("username").equals("jonathan")
                && properties.getString("password").equals("password"))
            {
                cycle.getSession().setProperty("signin.user", "jonathan");

                if (!cycle.continueToOriginalDestination())
                {
                    cycle.setPage(new Home(PageParameters.NULL));
                }
            }
            else
            {
                // Form method that will notify feedback panel
                errorMessage("Couldn't sign you in");
            }
        }
    }
}

///////////////////////////////// End of File /////////////////////////////////
