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
import java.util.regex.Pattern;

import wicket.markup.html.form.FormComponent;
import wicket.util.parse.metapattern.MetaPattern;

/**
 * Validates component with Java regexp.
 * @author Jonathan Locke
 */
public final class PatternValidator extends AbstractValidator
{
    /** The regexp pattern. */
    private final Pattern pattern;

    /**
     * Constructor.
     * @param pattern Regular expression pattern
     */
    public PatternValidator(final String pattern)
    {
        this(Pattern.compile(pattern));
    }

    /**
     * Constructor.
     * @param pattern Regular expression pattern
     * @param flags Compile flags for java.util.regex.Pattern
     */
    public PatternValidator(final String pattern, final int flags)
    {
        this(Pattern.compile(pattern, flags));
    }

    /**
     * Constructor.
     * @param pattern Java regex pattern
     */
    public PatternValidator(final Pattern pattern)
    {
        this.pattern = pattern;
    }

    /**
     * Constructor.
     * @param pattern Meta regex pattern
     */
    public PatternValidator(final MetaPattern pattern)
    {
        this(pattern.pattern());
    }

    /**
     * Validates the given form component.
     * @param input the input to validate
     * @param component The component to validate
     * @return Error for component or NO_ERROR if none
     */
    public ValidationErrorMessage validate(
            final Serializable input, final FormComponent component)
    {
        final String value = (String)input;

        if (!pattern.matcher(value).matches())
        {
            return errorMessage(input, component);
        }

        return NO_ERROR;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return "[pattern = " + pattern + "]";
    }
}

///////////////////////////////// End of File /////////////////////////////////
