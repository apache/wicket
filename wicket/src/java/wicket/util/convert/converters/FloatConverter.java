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

import java.text.ParseException;
import java.util.Locale;

import wicket.util.convert.ConversionException;

/**
 * Converts from Object to Float.
 * 
 * @author Eelco Hillenius
 * @author Jonathan Locke
 */
public final class FloatConverter extends DecimalConverter
{
    /**
     * Constructor
     */
    public FloatConverter()
    {
    }
    
    /**
     * Constructor
     * @param locale The locale for this converter
     */
    public FloatConverter(final Locale locale)
    {
        super(locale);
    }

    /**
     * @see wicket.util.convert.ITypeConverter#convert(java.lang.Object)
     */
    public Object convert(final Object value)
    {
        if (value instanceof Number)
        {
            return new Float(((Number)value).floatValue());
        }

        try
        {
            final Number number = getNumberFormat().parse(value.toString());
            if (number.doubleValue() > Float.MAX_VALUE || 
                number.doubleValue() < Float.MIN_VALUE)
            {
                throw new ConversionException("Float value out of range");
            }
            return new Float(number.floatValue());
        }
        catch (ParseException e)
        {
            throw new ConversionException("Cannot convert '" + value + "' to Float", e);
        }
    }
}
