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
package wicket.util.string.interpolator;

import ognl.Ognl;
import ognl.OgnlException;

/**
 * Interpolates values produced by ognl expressions into strings
 * @author Jonathan Locke
 */
public final class OgnlVariableInterpolator extends VariableInterpolator
{ // TODO finalize javadoc
    // Map of variables
    private final Object model;

    /**
     * Private constructor to force use of static interpolate method
     * @param string The string to interpolate into
     * @param model The root model to apply ognl expressions to
     */
    private OgnlVariableInterpolator(final String string, final Object model)
    {
        super(string);
        this.model = model;
    }

    /**
     * Interpolates string using OGNL
     * @param string String containing OGNL expressions like ${ognl}
     * @param object The object for OGNL to reflect on
     * @return The interpolated string
     */
    public static String interpolate(final String string, final Object object)
    {
        // If there's any reason to go to the expense of ognl
        if (string.indexOf("${") != -1)
        {
            // do ognl variable interpolation
            return new OgnlVariableInterpolator(string, object).toString();
        }

        // Return simple string
        return string;
    }

    /**
     * Gets a value for a variable name during interpolation
     * @param variableName The variable
     * @return The value
     */
    protected String getValue(final String variableName)
    {
        try
        {
            return Ognl.getValue(variableName, model).toString();
        }
        catch (OgnlException e)
        {
            throw new RuntimeException("Unable to get value of variable '"
                    + variableName + "' in " + string);
        }
    }
}

///////////////////////////////// End of File /////////////////////////////////
