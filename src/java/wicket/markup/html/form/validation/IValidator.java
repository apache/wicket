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

import wicket.markup.html.form.FormComponent;


/**
 * Interface to code that validates form components.
 *
 * @author Jonathan Locke
 */
public interface IValidator
{
    /**
     * Special value to indicate no validation error/ message.
     */
    public static final ValidationErrorMessage NO_ERROR = null;

    /**
     * An implementation of IValidator that does nothing at all.
     */
    public static final IValidator NULL = new NullValidator();

    /**
     * Validates the given input. The input corresponds to the input from the request
     * for a component.
     * @param input the input to validate.
     * @param component Component to check for
     * @return the validation message or NO_ERROR
     */
    public ValidationErrorMessage validate(
            final Serializable input, final FormComponent component);

    /**
     * Validator that does nothing.
     */
    static final class NullValidator implements IValidator
    {
        /**
         * Returns null.
         * @see wicket.markup.html.form.validation.IValidator#validate(java.io.Serializable, wicket.markup.html.form.FormComponent)
         */
        public ValidationErrorMessage validate(
                final Serializable input, final FormComponent component)
        {
            return null;
        }

        /**
         * @see java.lang.Object#toString()
         */
        public String toString()
        {
            return "[null]";
        }
    }
}

///////////////////////////////// End of File /////////////////////////////////
