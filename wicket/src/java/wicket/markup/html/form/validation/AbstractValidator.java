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
import java.util.HashMap;
import java.util.Map;

import wicket.Component;
import wicket.Localizer;
import wicket.RenderException;
import wicket.RequestCycle;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.FormComponent;
import wicket.model.IModel;
import wicket.model.Model;
import wicket.util.lang.Classes;

/**
 * Base class for form component validators.
 *
 * @author Jonathan Locke
 * @author Eelco Hillenius
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
     * validator is contained. The resource key must be of the form:
     * [form-name].[component-name].[validator-class]. For example, in the SignIn page's
     * SignIn.properties file, you might find an entry:
     * signInForm.password.RequiredValidator=A password is required Entries can contain
     * optional ognl variable interpolations from the component, such as:
     * editBook.name.LengthValidator='${input}' is too short a name.
     * <p>
     * Available variables for interpolation are:
     * <ul>
     *   <li>
     * 		${input}: the user's input
     *   </li>
     *   <li>
     *   	${name}: the name of the component
     *   </li>
     * </ul>
     * </p>
     * @param input the input (that caused the error)
     * @param component The component where the error occurred
     * @return The validation error message
     */
    public final ValidationErrorMessage errorMessage(
            final Serializable input, final FormComponent component)
    {
        // Resource key must be <form-name>.<component-name>.<validator-class>
        final Component parentForm = component.findParent(Form.class);
        if (parentForm != null)
        {
            final String resourceKey = parentForm.getName()
                    + "." + component.getName() + "." + Classes.name(getClass());
            return errorMessage(resourceKey, input, component);
        }
        else
        {
            throw new RenderException(
                    "Unable to find Form parent for FormComponent " + component);
        }
    }

    /**
     * Returns a formatted validation error message for a given component. The error
     * message is retrieved from a message bundle associated with the page in which this
     * validator is contained using the given resource key.
     * <p>
     * Available variables for interpolation are:
     * <ul>
     *   <li>
     * 		${input}: the user's input
     *   </li>
     *   <li>
     *   	${name}: the name of the component
     *   </li>
     * </ul>
     * </p>
     * @param resourceKey the resource key to be used for the message
     * @param input the input (that caused the error)
     * @param component The component where the error occurred
     * @return The validation error message
     */
    public final ValidationErrorMessage errorMessage(
    		final String resourceKey,
            final Serializable input, final FormComponent component)
    {
        Map resourceModel = new HashMap(2);
        resourceModel.put("input", input);
        resourceModel.put("name", component.getName());
		return errorMessage(resourceKey, resourceModel, input, component);
    }

    /**
     * Returns a formatted validation error message for a given component. The error
     * message is retrieved from a message bundle associated with the page in which this
     * validator is contained using the given resource key. The resourceModel is used
     * for variable interpolation.
     * @param resourceKey the resource key to be used for the message
     * @param resourceModel the model for variable interpolation
     * @param input the input (that caused the error)
     * @param component The component where the error occurred
     * @return The validation error message
     */
    public final ValidationErrorMessage errorMessage(
    		final String resourceKey, final Map resourceModel,
            final Serializable input, final FormComponent component)
    {
    	final IModel model;
    	if(resourceModel instanceof Serializable)
    	{
    		model = new Model((Serializable)resourceModel);
    	}
    	else
    	{
    		model = new Model(new HashMap(resourceModel));
    	}
    	return errorMessage(resourceKey, model, input, component);
    }


    /**
     * Returns a formatted validation error message for a given component. The error
     * message is retrieved from a message bundle associated with the page in which this
     * validator is contained using the given resource key. The resourceModel is used
     * for variable interpolation.
     * @param resourceKey the resource key to be used for the message
     * @param resourceModel the model for variable interpolation
     * @param input the input (that caused the error)
     * @param component The component where the error occurred
     * @return The validation error message
     */
    public final ValidationErrorMessage errorMessage(
    		final String resourceKey, final IModel resourceModel,
            final Serializable input, final FormComponent component)
    {
        // Return formatted error message
        Localizer localizer = component.getLocalizer();
		String message = localizer.getString(resourceKey, component, resourceModel);
        return new ValidationErrorMessage(input, component, message);
    }
}