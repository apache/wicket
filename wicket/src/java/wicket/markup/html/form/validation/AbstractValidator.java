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
package wicket.markup.html.form.validation;

import java.io.Serializable;

import wicket.Component;
import wicket.RenderException;
import wicket.RequestCycle;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.FormComponent;
import wicket.util.lang.Classes;


/**
 * Base class for form component validators.
 * @author Jonathan Locke
 */
public abstract class AbstractValidator implements IValidator
{
    /**
     * Implemented by subclass to validate an html form component.
     * @param input the input to validate
     * @param component The component to validate
     * @return Validation message or null if okay
     */
    public abstract ValidationErrorMessage validate(
            final Serializable input, FormComponent component);

    /**
     * Gets the input for the given current component.
     * @param component the component to get the input for
     * @return the input for the given current component
     */
    public final String getInput(final FormComponent component)
    {
        return component.getRequestString(RequestCycle.get());
    }

    /**
     * Returns a formatted validation error message for a given component. The error
     * message is retrieved from a message bundle associated with the page in which this
     * validator is contained. The property name must be of the form:
     * [form-name].[component-name].[validator-class]. For example, in the SignIn page's
     * SignIn.properties file, you might find an entry:
     * signInForm.password.RequiredValidator=A password is required Entries can contain
     * optional ognl variable interpolations from the component, such as:
     * editBook.name.LengthValidator='${model.name}' is too short a name.
     * @param input the input (that caused the error)
     * @param component The component where the error occurred
     * @return The validation error message
     */
    public final ValidationErrorMessage errorMessage(
            final Serializable input, final FormComponent component)
    {
        // Property name must be <form-name>.<component-name>.<validator-class>
        final Component parentForm = component.findParent(Form.class);

        if (parentForm != null)
        {
            final String propertyName = parentForm.getName()
                    + "." + component.getName() + "." + Classes.name(getClass());

            // Return formatted error message
            String message = component.getLocalizer().getString(propertyName, component);
            return new ValidationErrorMessage(input, component, message);
        }
        else
        {
            throw new RenderException(
                    "Unable to find Form parent for FormComponent " + component);
        }
    }
}

///////////////////////////////// End of File /////////////////////////////////
