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
import wicket.util.string.StringList;

/**
 * Validates that a form component's value is of a certain min/max length.
 * Validators are constructed by calling the min, max and range static
 * factory methods.  For example, LengthValidator.min(6) would return a
 * validator valid only when the input of the component it is attached
 * to is at least 6 characters long.  Likewise, LengthValidator.range(3, 5)
 * would only validate a component containing between 3 and 5 characters
 * (inclusive).
 *
 * @author Jonathan Locke
 */
public class LengthValidator extends AbstractValidator
{
    /** True if minimum bound should be checked. */
    private final boolean checkMin;

    /** True if maximum bound should be checked. */
    private final boolean checkMax;

    /** Lower bound on valid length. */
    private final int min;

    /** Upper bound on valid length. */
    private final int max;

    /**
     * Private constructor forces use of static factory method and static instances.
     * @param checkMin True if minimum bound should be checked
     * @param min Lower bound on valid length
     * @param checkMax True if maximum bound should be checked
     * @param max Upper bound on valid length
     */
    private LengthValidator(final boolean checkMin, final int min,
    		final boolean checkMax, final int max)
    {
        this.min = min;
        this.max = max;
        this.checkMin = checkMin;
        this.checkMax = checkMax;
    }

    /**
     * Gets a length validator object that requires a minimum number of characters.
     * @param min Minimum number of characters
     * @return Validator object
     */
    public final static LengthValidator min(final int min)
    {
        return new LengthValidator(true, min, false, 0);
    }

    /**
     * Gets a length validator object that requires a maximum number of characters.
     * @param max Maximum number of characters
     * @return Validator object
     */
    public final static LengthValidator max(final int max)
    {
        return new LengthValidator(false, 0, true, max);
    }

    /**
     * Gets a length validator object that requires a minimum and maximum number of
     * characters.
     * @param min Minimum number of characters
     * @param max Maximum number of characters
     * @return Validator object
     */
    public final static LengthValidator range(final int min, final int max)
    {
        return new LengthValidator(true, min, true, max);
    }

    /**
     * Validates the given form component.
     * Validates that a form component's value is of a certain minimum 
     * and/or maximum length.
     * @param component The component to validate
     * @return Error for component or NO_ERROR if none
     */
    public final ValidationErrorMessage validate(final FormComponent component)
    {
        // Get component value
        final String value = component.getRequestString();
        
        // Check length
        if ((checkMin && value.length() < min) || 
            (checkMax && value.length() > max))
        {
            return errorMessage(value, component);
        }

        return NO_ERROR;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        final StringList list = new StringList();

        if (checkMin)
        {
            list.add("min = " + min);
        }

        if (checkMax)
        {
            list.add("max = " + max);
        }

        return list.toString();
    }
}


