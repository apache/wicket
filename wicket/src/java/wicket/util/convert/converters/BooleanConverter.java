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
package wicket.util.convert.converters;

import wicket.util.convert.ConversionException;
import wicket.util.convert.Converter;

/**
 * <p>
 * {@link Converter}implementation that converts an incoming String into a
 * <code>java.lang.Boolean</code> object, throwing a {@link ConversionException}if a
 * conversion error occurs.
 * </p>
 */
public final class BooleanConverter implements Converter
{
    /**
     * Create a {@link Converter}that will throw a {@link ConversionException}if a
     * conversion error occurs.
     */
    public BooleanConverter()
    {
    }

    /**
     * Convert the specified input object into an output object of the specified type.
     * @param type Data type to which this value should be converted
     * @param value The input value to be converted
     * @return converted object
     * @exception ConversionException if conversion cannot be performed successfully
     */
    public Object convert(Class type, Object value)
    {
        if (value == null)
        {
            return null;
        }

        if (value instanceof Boolean)
        {
            return (value);
        }

        try
        {
            String stringValue = value.toString();

            if (stringValue.equalsIgnoreCase("yes")
                    || stringValue.equalsIgnoreCase("y") || stringValue.equalsIgnoreCase("true")
                    || stringValue.equalsIgnoreCase("on") || stringValue.equalsIgnoreCase("1"))
            {
                return (Boolean.TRUE);
            }
            else if (stringValue.equalsIgnoreCase("no")
                    || stringValue.equalsIgnoreCase("n") || stringValue.equalsIgnoreCase("false")
                    || stringValue.equalsIgnoreCase("off") || stringValue.equalsIgnoreCase("0"))
            {
                return (Boolean.FALSE);
            }
            else
            {
                throw new ConversionException(stringValue);
            }
        }
        catch (ClassCastException e)
        {
            throw new ConversionException(e);
        }
    }
}
