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

import java.util.Locale;

/**
 * Localized long converter.
 */
public class LongLocaleConverter extends DecimalLocaleConverter
{
    /**
     * Construct. The locale is the default locale for this instance of the
     * Java Virtual Machine and an unlocalized pattern is used for the convertion.
     */
    public LongLocaleConverter()
    {
        this(Locale.getDefault());
    }

    /**
     * Construct. An unlocalized pattern is used for the convertion.
     * @param locale The locale
     */
    public LongLocaleConverter(Locale locale)
    {
        this(locale, null);
    }

    /**
     * Construct. An unlocalized pattern is used for the convertion.
     * @param locale The locale
     * @param pattern The convertion pattern
     */
    public LongLocaleConverter(Locale locale, String pattern)
    {
        this(locale, pattern, false);
    }

    /**
     * Construct.
     * @param locale The locale
     * @param pattern The convertion pattern
     * @param locPattern Indicate whether the pattern is localized or not
     */
    public LongLocaleConverter(Locale locale, String pattern, boolean locPattern)
    {
        super(locale, pattern, locPattern);
    }

    /**
     * Converts the specified locale-sensitive input object into an output object of the
     * specified type.
     * @param value The input object to be converted
     * @param pattern The pattern is used for the convertion
     * @return converted object
     * @exception wicket.util.convert.ConversionException if conversion cannot be performed successfully
     */
    public Object convert(Object value, String pattern)
    {
        if (value == null)
        {
            return null;
        }

        Number temp = getNumber(value, pattern);

        return (temp instanceof Long) ? (Long) temp : new Long(temp.longValue());
    }
}
