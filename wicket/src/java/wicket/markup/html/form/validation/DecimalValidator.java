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
 * Ensures that the form component has a numeric value. If min and max arguments are
 * given, this validator also ensures the value is in bounds.
 *
 * @author Jonathan Locke
 */
public final class DecimalValidator extends AbstractValidator
{
    /**
     * Validator that ensures int value.
     */
    public static final DecimalValidator INTEGER =
        new DecimalValidator(Integer.MIN_VALUE, Integer.MAX_VALUE);

    /**
     * Validator that ensures positive int value.
     */
    public static final DecimalValidator POSITIVE_INTEGER =
        new DecimalValidator(0, Integer.MAX_VALUE);

    /**
     * Validator that ensures long value.
     */
    public static final DecimalValidator LONG =
        new DecimalValidator(Long.MIN_VALUE, Long.MAX_VALUE);

    /**
     * Validator that ensures positive long value.
     */
    public static final DecimalValidator POSITIVE_LONG =
        new DecimalValidator(0, Long.MAX_VALUE);

    /** Lower bound on valid decimal number. */
    private final long min;

    /** Upper bound on valid decimal number. */
    private final long max;

    /**
     * Private constructor forces use of static factory method and static instances.
     * @param min Lower bound on valid decimal number
     * @param max Upper bound on valid decimal number
     */
    private DecimalValidator(final long min, final long max)
    {
        this.min = min;
        this.max = max;
    }

    /**
     * Gets a decimal validator with a given range.
     * @param min Lower bound on valid decimal number
     * @param max Upper bound on valid decimal number
     * @return Validator object
     */
    public static DecimalValidator range(final long min, final long max)
    {
        return new DecimalValidator(min, max);
    }

    /**
     * Validates the given form component.
     * Ensures that the form component has a numeric value. If min and max arguments are
     * given, this validator also ensures the value is in bounds.
     * @param input the input
     * @param component The component to validate
     * @return Error for component or NO_ERROR if none
     */
    public ValidationErrorMessage validate(
            final String input, final FormComponent component)
    {
        try
        {
            // Get long value
            final long value = Long.parseLong((String)input);

            if ((value < min) || (value > max))
            {
                return errorMessage(input, component);
            }
        }
        catch (NumberFormatException e)
        {
            return errorMessage(input, component);
        }

        return NO_ERROR;
    }
}