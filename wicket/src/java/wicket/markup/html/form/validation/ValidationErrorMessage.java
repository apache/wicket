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

import wicket.Component;
import wicket.FeedbackMessage;
import wicket.markup.html.form.FormComponent;

/**
 * A specialized version of a {@link wicket.FeedbackMessage}
 * that represents a message about the failure of a validation.
 * 
 * @see wicket.markup.html.form.validation.IValidator
 * @see wicket.FeedbackMessage
 * @see wicket.markup.html.form.validation.IValidationErrorHandler
 * @author Eelco Hillenius
 */
public final class ValidationErrorMessage extends FeedbackMessage
{
    /** Constant for representing an empty message (same as null). */
    public static final ValidationErrorMessage NO_MESSAGE = null;

    /**
     * Construct using fields.
     * @param reporter the message reporter
     * @param message the actual message
     */
    public ValidationErrorMessage(final Component reporter, final String message)
    {
        super(reporter, message, ERROR);
    }
    
    /**
     * Get the input that caused this validation error message.
     * @return The input, or null if none is available.
     */
    public String getInput()
    {
        final Component reporter = getReporter();
        if (reporter instanceof FormComponent)
        {
            return ((FormComponent)getReporter()).getStringValue();
        }
        return null;
    }
}

