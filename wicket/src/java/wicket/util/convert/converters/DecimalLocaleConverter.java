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


import java.text.DecimalFormat;
import java.text.ParseException;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import wicket.util.convert.ConversionException;

/**
 * <p>
 * Modified {@link LocaleConverter}implementation for this framework
 * </p>
 */
public abstract class DecimalLocaleConverter extends BaseLocaleConverter
{ // TODO finalize javadoc
    protected Pattern nonDigitPattern = Pattern.compile(".*[^0-9&&[^\\,]&&[^\\.]&&[^\\-]].*");

    // ----------------------------------------------------------- Constructors

    /**
     * Create a {@link LocaleConverter}that will throw a {@link ConversionException}if a
     * conversion error occurs. The locale is the default locale for this instance of the
     * Java Virtual Machine and an unlocalized pattern is used for the convertion.
     */
    public DecimalLocaleConverter()
    {
        this(Locale.getDefault());
    }

    /**
     * Create a {@link LocaleConverter}that will throw a {@link ConversionException}if a
     * conversion error occurs. No pattern is used for the convertion.
     * @param locale The locale
     */
    public DecimalLocaleConverter(Locale locale)
    {
        this(locale, null);
    }

    /**
     * Create a {@link LocaleConverter}that will throw a {@link ConversionException}if a
     * conversion error occurs. An unlocalized pattern is used for the convertion.
     * @param locale The locale
     * @param pattern The convertion pattern
     */
    public DecimalLocaleConverter(Locale locale, String pattern)
    {
        this(locale, pattern, false);
    }

    /**
     * Create a {@link LocaleConverter}that will throw a {@link ConversionException}if a
     * conversion error occurs.
     * @param locale The locale
     * @param pattern The convertion pattern
     * @param locPattern Indicate whether the pattern is localized or not
     */
    public DecimalLocaleConverter(Locale locale, String pattern, boolean locPattern)
    {
        super(locale, pattern, locPattern);
    }

    // --------------------------------------------------------- Methods

    /**
     * Convert the specified locale-sensitive input object into an output object of the
     * specified type.
     * @param value The input object to be converted
     * @param pattern The pattern is used for the conversion
     * @return converted object
     * @throws ParseException
     * @exception ConversionException if conversion cannot be performed successfully
     */
    protected Object parse(Object value, String pattern) throws ParseException
    {
        if (value == null)
        {
            return null;
        }

        DecimalFormat formatter = getFormat(pattern);

        return formatter.parse((String) value);
    }

    /**
     * Convert the specified input object into a locale-sensitive output string
     * @param value The input object to be formatted
     * @param pattern The pattern is used for the conversion
     * @return formatted object
     * @exception IllegalArgumentException if formatting cannot be performed successfully
     */
    public String format(Object value, String pattern) throws IllegalArgumentException
    {
        if (value == null)
        {
            return null;
        }

        DecimalFormat formatter = getFormat(pattern);

        return formatter.format(value);
    }

    /**
     * get format and optionally apply pattern if given
     * @param pattern pattern or null
     * @return DecimalFormat formatter instance
     */
    protected DecimalFormat getFormat(String pattern)
    {
        DecimalFormat formatter = (DecimalFormat) DecimalFormat.getInstance(locale);

        if (pattern != null)
        {
            if (locPattern)
            {
                formatter.applyLocalizedPattern(pattern);
            }
            else
            {
                formatter.applyPattern(pattern);
            }
        }

        return formatter;
    }

    /**
     * translate value to a number optionally using the supplied pattern
     * @param value the value to convert
     * @param pattern the patter to use (optional)
     * @return Number
     * @throws ConversionException
     */
    protected Number getNumber(Object value, String pattern) throws ConversionException
    {
        if (value instanceof Number)
        {
            return (Number) value;
        }

        Number temp = null;

        try
        {
            if (pattern != null)
            {
                temp = (Number) parse(value, pattern);
            }
            else
            {
                String stringval = null;

                if (value instanceof String)
                {
                    stringval = (String) value;
                }
                else if (value instanceof String[])
                {
                    stringval = ((String[]) value)[0];
                }
                else
                {
                    stringval = String.valueOf(value);
                }

                Matcher nonDigitMatcher = nonDigitPattern.matcher(stringval);

                if (nonDigitMatcher.matches())
                {
                    throw new ConversionException(stringval + " is not a valid number");
                }

                temp = (Number) parse(value, this.pattern);
            }
        }
        catch (Exception e)
        {
            String dpat = null;

            if (pattern != null)
            {
                dpat = pattern;
            }
            else
            {
                DecimalFormat formatter = getFormat(pattern);

                dpat = formatter.toLocalizedPattern();
            }

            throw new ConversionException(e).setPattern(dpat);
        }

        return temp;
    }
}
