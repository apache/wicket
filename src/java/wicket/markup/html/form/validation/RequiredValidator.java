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

import wicket.markup.html.form.FormComponent;


/**
 * Validator that ensures a component has a non-null and non-empty value.
 *
 * @author Jonathan Locke
 */
public final class RequiredValidator extends AbstractValidator
{
    /**
     * Validates the given form component.
     * @param input the input to validate
     * @param component The component to validate
     * @return Error for component or NO_ERROR if none
     */
    public ValidationErrorMessage validate(
            final String input, final FormComponent component)
    {
        final String value = (String)input;

        if ((value == null) || value.trim().equals(""))
        {
            return errorMessage(input, component);
        }

        return NO_ERROR;
    }

    /**
     * Converts this object to a String.
     * @return String representation of this object
     */
    public String toString()
    {
        return "[required]";
    }
}