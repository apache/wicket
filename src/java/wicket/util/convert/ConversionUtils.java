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
package wicket.util.convert;

import ognl.OgnlOps;

import java.util.Locale;

/**
 * Utiltiy for conversions that works with the given instance of converter registry.
 */
public final class ConversionUtils
{
    /** converter registry to use. */
    private final ConverterRegistry converterRegistry;

    /**
     * Construct.
     * @param registry the converter registry
     */
    ConversionUtils(ConverterRegistry registry)
    {
        this.converterRegistry = registry;
    }

    /**
     * Convert the given object to an object of the given type.
     * @param value the object to convert
     * @param toType the type to convert to
     * @return converted object
     * @throws ConversionException on unexpected errors
     */
    public Object convert(Object value, Class toType)
    {
        return convert(value, toType, null);
    }

    /**
     * Converts the given object to an object of the given type using the given locale.
     * @param value the object to convert
     * @param toType the type to convert to
     * @param locale the optional locale to use for conversion
     * @return the converted object
     * @throws ConversionException on unexpected errors
     */
    public Object convert(Object value, Class toType, Locale locale)
    {
        Converter converter = null;
        Object converted = null;
        try
        {
            converter = converterRegistry.lookup(toType, locale);
            if (converter != null) // we found a converter
            {
                if (!toType.isArray()) // a common case for request parameters// is that//
                                       // they send parameters as a
                // string array instead of a plain string
                {
                    if (value instanceof String[] && (((String[]) value).length == 1))
                    {
                        value = ((String[]) value)[0];
                    }
                }
                converted = converter.convert(value);
            }
            else
            // no converter was found
            {
                converted = OgnlOps.convertValue(value, toType);
            }
        }
        catch (ConversionException e)
        {
            throw e.setConverter(converter).setLocale(locale)
            	.setTargetType(toType).setTriedValue(value);
        }
        catch (Exception e)
        {
            throw new ConversionException(e).setConverter(converter).setLocale(locale)
                    .setTargetType(toType).setTriedValue(value);
        }
        return converted;
    }
}