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

/**
 * Interpolates variables from a map into strings. Variables are denoted in the source
 * string by the syntax ${variableName}. When constructed with no String to interpolate,
 * interpolate can be called with a String argument. When constructed with a String or
 * File argument, interpolates into that String or into the contents of the given template
 * file.
 * @author Jonathan Locke
 */
public abstract class VariableInterpolator
{
    // The string to interpolate within
    protected final String string;

    /**
     * Constructor
     * @param string String to interpolate with variable values
     */
    public VariableInterpolator(final String string)
    {
        this.string = string;
    }

    /**
     * Gets a value for a variable name during interpolation
     * @param variableName The variable
     * @return The value
     */
    protected abstract String getValue(String variableName);

    /**
     * Interpolate using variables
     * @return The interpolated string
     */
    public String toString()
    {
        // Result buffer
        final StringBuffer buffer = new StringBuffer();

        // For each occurrences of "${"
        int start;
        int pos = 0;

        while ((start = string.indexOf("${", pos)) != -1)
        {
            // Append text before possible variable
            buffer.append(string.substring(pos, start));

            // Position is now where we found the "${"
            pos = start;

            // Get start and end of variable name
            final int startVariableName = start + 2;
            final int endVariableName = string.indexOf('}', startVariableName);

            // Found a close brace?
            if (endVariableName != -1)
            {
                // Get variable name inside brackets
                final String variableName = string.substring(startVariableName, endVariableName);

                // Get value of variable
                final String value = getValue(variableName);

                // If there's no value
                if (value == null)
                {
                    // Leave variable uninterpolated, allowing multiple
                    // interpolators to
                    // do their work on the same string
                    buffer.append("${" + variableName + "}");
                }
                else
                {
                    // Append variable value
                    buffer.append(value);
                }

                // Move past variable
                pos = endVariableName + 1;
            }
            else
            {
                break;
            }
        }

        // Append anything that might be left
        if (pos < string.length())
        {
            buffer.append(string.substring(pos));
        }

        // Convert result to String
        return buffer.toString();
    }
}

///////////////////////////////// End of File /////////////////////////////////
