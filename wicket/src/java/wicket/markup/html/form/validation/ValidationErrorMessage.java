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
import wicket.FeebackMessage;


/**
 * A specialized version of a {@link wicket.FeebackMessage}
 * that represents a message about the failure of a validation.
 * @see wicket.markup.html.form.validation.IValidator
 * @see wicket.FeebackMessage
 * @see wicket.markup.html.form.validation.IValidationErrorHandler
 *
 * @author Eelco Hillenius
 */
public final class ValidationErrorMessage extends FeebackMessage
{ // TODO finalize javadoc
    /** the input (that caused the error). */
    private final Serializable input;

    /**
     * Construct using fields.
     * @param input the input (the cause of the validation error)
     * @param reporter the message reporter
     * @param message the actual message
     */
    public ValidationErrorMessage(Serializable input, Component reporter, String message)
    {
        super(reporter, message, ERROR);
        this.input = input;
    }

    /**
     * Gets input (that caused the error).
     * @return the input.
     */
    public Serializable getInput()
    {
        return input;
    }

    
}
