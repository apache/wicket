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
 * Converts from Object to Short.
 * 
 * @author Eelco Hillenius
 * @author Jonathan Locke
 */
public final class ShortConverter extends NumberConverter
{
    /**
     * Constructor
     */
    public ShortConverter()
    {
    }
    
    /**
     * Constructor
     * @param locale The locale for this converter
     */
    public ShortConverter(final Locale locale)
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
            return new Short(((Number)value).shortValue());
        }

        try
        {
            final Number number = getNumberFormat().parse(value.toString());
            if (number.doubleValue() > Short.MAX_VALUE || 
                number.doubleValue() < Short.MIN_VALUE)
            {
                throw new ConversionException("Short value out of range");
            }
            return new Short(number.shortValue());
        }
        catch (ParseException e)
        {
            throw new ConversionException("Cannot convert '" + value + "' to Short", e);
        }
    }
}
