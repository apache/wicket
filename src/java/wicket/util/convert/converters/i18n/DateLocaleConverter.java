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
package wicket.util.convert.converters.i18n;


import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.Locale;

import wicket.util.convert.ConversionException;

/**
 * Converts to and from Date objects using the current locale and optionally
 * a pattern for it conversion.
 *
 * @author Eelco Hillenius
 */
public final class DateLocaleConverter extends AbstractLocaleConverter
{
	/** whether to use lenient parsing. */
	private boolean lenient = false;

	/** de date style to use. */
	private int dateStyle = DateFormat.SHORT;

	/**
	 * Construct.
	 */
	public DateLocaleConverter()
	{
	}

	/**
	 * Construct. An unlocalized pattern is used for the convertion.
	 * @param locale The locale
	 * @param pattern The convertion pattern
	 */
	public DateLocaleConverter(Locale locale, String pattern)
	{
		super(pattern, false);
	}

	/**
	 * Gets whether date formatting is lenient.
	 * @return true if the <code>DateFormat</code> used for formatting is
	 *         lenient
	 * @see java.text.DateFormat#isLenient
	 */
	public boolean isLenient()
	{
		return lenient;
	}

	/**
	 * Specify whether or not date-time parsing should be lenient.
	 * @param lenient true if the <code>DateFormat</code> used for formatting
	 *           should be lenient
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
	 * @param dateStyle the date style (one of the constants of
	 *           {@link DateFormat})
	 */
	public void setDateStyle(int dateStyle)
	{
		this.dateStyle = dateStyle;
	}

	/**
	 * @see wicket.util.convert.IConverter#convert(java.lang.Object, java.lang.Class)
	 */
	public Object convert(Object value, Class c)
	{
		if (value == null)
		{
			return null;
		}
		if(c == CONVERT_TO_DEFAULT_TYPE || Date.class.isAssignableFrom(c))
		{
			return parse(value);
		}
		else if(String.class.isAssignableFrom(c))
		{
			return toString(value);
		}
		throw new ConversionException(this +
				" cannot handle conversions of type " + c);
	}

	/**
	 * @see wicket.util.convert.converters.AbstractConverter#toString(java.lang.Object)
	 */
	public String toString(Object value)
	{
		return format(value);
	}

	/**
	 * Converts the specified locale-sensitive input object into an output object
	 * of the specified type.
	 * @param value The input object to be converted
	 * @return converted object
	 * @exception ConversionException if conversion cannot be performed
	 *               successfully
	 */
	protected Object parse(Object value) throws ConversionException
	{
		final String pattern = getPattern();
		DateFormat formatter = getFormat();
		try
		{
			return formatter.parse((String)value);
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
				dpat = ((SimpleDateFormat)formatter).toLocalizedPattern();
			}

			throw new ConversionException(e).setPattern(dpat);
		}
	}

	/**
	 * Formats the given value with pattern or using the default pattern.
	 * @param value the value to format
	 * @return the formatted value
	 */
	public String format(Object value)
	{
		DateFormat format = getFormat();
		Date date = null;

		if (value instanceof Date)
		{
			date = (Date)value;
		}
		else
		{
			date = (Date)convert(value, Date.class);
		}

		return format.format(date);
	}

	/**
	 * Gets the date format.
	 * @return the date format object for the given pattern and locale
	 */
	private DateFormat getFormat()
	{
		DateFormat fmt = null;
		final String pattern = getPattern();
		final Locale locale = getLocale();
		if (pattern == null)
		{
			fmt = DateFormat.getDateInstance(dateStyle, locale);
			fmt.setLenient(lenient);
		}
		else
		{
			SimpleDateFormat simpleFmt = new SimpleDateFormat(pattern, locale);
			simpleFmt.setLenient(lenient);
			if (isLocPattern())
			{
				simpleFmt.applyLocalizedPattern(pattern);
			}
			else
			{
				simpleFmt.applyPattern(pattern);
			}
			fmt = simpleFmt;
		}
		return fmt;
	}
}
