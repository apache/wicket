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


import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.Locale;

import wicket.util.convert.ConversionException;

/**
 * Standard {@link LocaleConverter} implementation that converts an incoming
 * locale-sensitive String into a <code>java.util.Date</code> object, throwing a
 * {@link ConversionException} if a conversion error occurs.
 */
public class DateLocaleConverter extends BaseLocaleConverter
{
	/** whether to use lenient parsing. */
    private boolean lenient = false;

    /** de date style to use. */
    private int dateStyle = DateFormat.SHORT;

    /**
     * Construct. The locale is the default locale for this instance of the
     * Java Virtual Machine and an unlocalized pattern is used for the convertion.
     */
    public DateLocaleConverter()
    {
        this(Locale.getDefault());
    }

    /**
     * Construct. An unlocalized pattern is used for the convertion.
     * @param locale The locale
     */
    public DateLocaleConverter(Locale locale)
    {
        this(locale, null);
    }

    /**
     * Construct. An unlocalized pattern is used for the convertion.
     * @param locale The locale
     * @param pattern The convertion pattern
     */
    public DateLocaleConverter(Locale locale, String pattern)
    {
        super(locale, pattern, false);
    }

    /**
     * Gets whether date formatting is lenient.
     * @return true if the <code>DateFormat</code> used for formatting is lenient
     * @see java.text.DateFormat#isLenient
     */
    public boolean isLenient()
    {
        return lenient;
    }

    /**
     * Specify whether or not date-time parsing should be lenient.
     * @param lenient true if the <code>DateFormat</code> used for
     * formatting should be lenient
     * @see java.text.DateFormat#setLenient
     */
    public void setLenient(boolean lenient)
    {
        this.lenient = lenient;
    }

    /**
     * Gets the date style.
     * @return int date style (one of the constants of {@link DateFormat})
     */
    public int getDateStyle()
    {
        return dateStyle;
    }

    /**
     * Sets the date style.
     * @param dateStyle the date style (one of the constants of {@link DateFormat})
     */
    public void setDateStyle(int dateStyle)
    {
        this.dateStyle = dateStyle;
    }

    /**
     * Converts the specified locale-sensitive input object into an output object of the
     * specified type.
     * @param value The input object to be converted
     * @param pattern The pattern is used for the convertion
     * @return converted object
     * @exception ConversionException if conversion cannot be performed successfully
     */
    protected Object parse(Object value, String pattern) throws ConversionException
    {
        DateFormat formatter = getFormat(pattern, locale);

        try
        {
            return formatter.parse((String) value);
        }
        catch (ParseException e)
        {
            String dpat = null;

            if (pattern != null)
            {
                dpat = pattern;
            }
            else if (formatter instanceof SimpleDateFormat)
            {
                dpat = ((SimpleDateFormat) formatter).toLocalizedPattern();
            }

            throw new ConversionException(e).setPattern(dpat);
        }
    }

    /**
     * Formats the given value with pattern or using the default pattern.
     * @param value the value to format
     * @param pattern the pattern to use
     * @return the formatted value
     * @see wicket.util.convert.Formatter#format(java.lang.Object, java.lang.String)
     */
    public String format(Object value, String pattern)
    {
        DateFormat format = getFormat(pattern, locale);
        Date date = null;

        if (value instanceof Date)
        {
            date = (Date) value;
        }
        else
        {
            date = (Date) convert(value);
        }

        return format.format(date);
    }

    /**
     * Gets the date format.
     * @param pattern the pattern to use for formatting
     * @param locale the locale
     * @return the date format object for the given pattern and locale
     */
    private DateFormat getFormat(String pattern, Locale locale)
    {
        DateFormat format = null;

        if (pattern == null)
        {
            format = DateFormat.getDateInstance(dateStyle, locale);
            format.setLenient(lenient);
        }
        else
        {
            SimpleDateFormat _format = new SimpleDateFormat(pattern, locale);

            _format.setLenient(lenient);

            if (locPattern)
            {
                _format.applyLocalizedPattern(pattern);
            }
            else
            {
                _format.applyPattern(pattern);
            }

            format = _format;
        }

        return format;
    }

    /**
     * Converts the specified locale-sensitive input object into an output object of the
     * specified type.
     * @param value The input object to be converted
     * @param pattern The pattern is used for the convertion
     * @return converted object
     * @exception ConversionException if conversion cannot be performed successfully
     */
    public Object convert(Object value, String pattern)
    {
        if (value == null)
        {
            return null;
        }

        return parse(value, pattern);
    }
}
