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

import wicket.util.convert.Formatter;

/**
 * base class for localized converters.
 */
public abstract class BaseLocaleConverter implements LocaleConverter, Formatter
{
    // ----------------------------------------------------- Instance Variables

    /** The locale specified to our Constructor, by default - system locale. */
    protected Locale locale = Locale.getDefault();

    /** The default pattern specified to our Constructor, if any. */
    protected String pattern = null;

    /** The flag indicating whether the given pattern string is localized or not. */
    protected boolean locPattern = false;

    // ----------------------------------------------------------- Constructors

    /**
     * Create a {@link LocaleConverter}that will throw a {@link wicket.util.convert.ConversionException}if a
     * conversion error occurs. An unlocalized pattern is used for the convertion.
     * @param locale The locale
     * @param pattern The convertion pattern
     */
    protected BaseLocaleConverter(Locale locale, String pattern)
    {
        this(locale, pattern, false);
    }

    /**
     * Create a {@link LocaleConverter}that will throw a {@link wicket.util.convert.ConversionException}if
     * an conversion error occurs.
     * @param locale The locale
     * @param pattern The convertion pattern
     * @param locPattern Indicate whether the pattern is localized or not
     */
    protected BaseLocaleConverter(Locale locale, String pattern, boolean locPattern)
    {
        if (locale != null)
        {
            this.locale = locale;
        }

        this.pattern = pattern;
        this.locPattern = locPattern;
    }

    // --------------------------------------------------------- Methods

    /**
     * Convert the specified locale-sensitive input object into an output object of the
     * specified type.
     * @param value The input object to be converted
     * @param pattern The pattern is used for the convertion
     * @return parsed object
     * @throws ParseException
     * @exception wicket.util.convert.ConversionException if conversion cannot be performed successfully
     */
    abstract protected Object parse(Object value, String pattern) throws ParseException;

    /**
     * Convert the specified locale-sensitive input object into an output object. The
     * default pattern is used for the convertion.
     * @param value The input object to be converted
     * @return converted object
     * @exception wicket.util.convert.ConversionException if conversion cannot be performed successfully
     */
    public Object convert(Object value)
    {
        return convert(value, null);
    }

    /**
     * Convert the specified locale-sensitive input object into an output object.
     * @param value The input object to be converted
     * @param pattern The pattern is used for the convertion
     * @return converted object
     * @exception wicket.util.convert.ConversionException if conversion cannot be performed successfully
     */
    public Object convert(Object value, String pattern)
    {
        return convert(null, value, pattern);
    }

    /**
     * Convert the specified locale-sensitive input object into an output object of the
     * specified type. The default pattern is used for the convertion.
     * @param type Data type to which this value should be converted
     * @param value The input object to be converted
     * @return converted object
     * @exception wicket.util.convert.ConversionException if conversion cannot be performed successfully
     */
    public Object convert(Class type, Object value)
    {
        return convert(type, value, null);
    }

    /**
     * get the locale
     * @return Locale
     */
    public Locale getLocale()
    {
        return locale;
    }

    /**
     * get the pattern
     * @return String
     */
    public String getPattern()
    {
        return pattern;
    }

    /**
     * set the locale
     * @param locale
     */
    public void setLocale(Locale locale)
    {
        this.locale = locale;
    }

    /**
     * set the pattern
     * @param string
     */
    public void setPattern(String string)
    {
        pattern = string;
    }
}
