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

import java.text.NumberFormat;
import java.text.ParseException;

import wicket.util.convert.ConversionException;

/**
 * Converts from Object to Double.
 * 
 * @author Eelco Hillenius
 * @author Jonathan Locke
 */
public final class DoubleConverter extends NumberConverter
{
    /**
     * @see wicket.util.convert.ITypeConverter#convert(java.lang.Object)
     */
    public Object convert(final Object value)
    {
        if (value instanceof Number)
        {
            Number number = (Number)value;
            return new Double(number.doubleValue());
        }

        final String stringValue = value.toString();
        try
        {
            final NumberFormat numberFormat = getNumberFormat();
            if (numberFormat != null)
            {
                return new Double(numberFormat.parse(stringValue).doubleValue());
            }
            return new Double(stringValue);
        }
        catch (ParseException e)
        {
            throw new ConversionException("Cannot convert '" + stringValue + "' to Double", e);
        }
        catch (NumberFormatException e)
        {
            throw new ConversionException("Cannot convert '" + stringValue + "' to Double", e);
        }
    }
}