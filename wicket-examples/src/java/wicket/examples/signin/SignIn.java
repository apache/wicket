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

import wicket.PageParameters;
import wicket.RequestCycle;
import wicket.examples.WicketExamplePage;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.PasswordTextField;
import wicket.markup.html.form.TextField;
import wicket.markup.html.panel.FeedbackPanel;
import wicket.util.value.ValueMap;

/**
 * Simple example of a sign in page.
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
         * @see wicket.markup.html.form.Form#onSubmit()
         */
        public final void onSubmit()
        {
            // Get session info
            SignInSession session = (SignInSession)getSession();
                    
            // Sign the user in
            if (session.authenticate(properties.getString("username"),  
                    properties.getString("password")))
            {
                final RequestCycle cycle = getRequestCycle();
                if (!cycle.continueToOriginalDestination())
                {
                    cycle.setPage(new Home(PageParameters.NULL));
                }
            }
            else
            {
                // Form method that will notify feedback panel
                error("Couldn't sign you in");
            }
        }
    }
}


