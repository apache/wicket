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

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import wicket.util.convert.ConversionException;

/**
 * Base class for locale aware numeric converters.
 *
 * @author Eelco Hillenius
 */
public abstract class DecimalLocaleConverter extends AbstractLocaleConverter
{
	/** non digit regex pattern. */
	protected Pattern nonDigitPattern = Pattern.compile(".*[^0-9&&[^\\,]&&[^\\.]&&[^\\-]].*");

	/**
	 * Construct.
	 */
	public DecimalLocaleConverter()
	{
		super();
	}

	/**
	 * Construct.
	 * @param pattern The convertion pattern
	 */
	public DecimalLocaleConverter(String pattern)
	{
		super(pattern);
	}

	/**
	 * Construct.
	 * @param pattern The convertion pattern
	 * @param locPattern whether the pattern is localized
	 */
	public DecimalLocaleConverter(String pattern, boolean locPattern)
	{
		super(pattern, locPattern);
	}

	/**
	 * @see wicket.util.convert.converters.AbstractConverter#toString(java.lang.Object)
	 */
	public String toString(Object value)
	{
		if (value == null)
		{
			return null;
		}
		DecimalFormat formatter = getFormat();
		return formatter.format(value);
	}

	/**
	 * Converts the specified locale-sensitive input object into an output object
	 * of the specified type.
	 * @param value The input object to be converted
	 * @param pattern The pattern is used for the conversion
	 * @return converted object
	 * @throws ParseException
	 * @exception ConversionException if conversion cannot be performed
	 *               successfully
	 */
	protected Object parse(Object value, String pattern) throws ParseException
	{
		if (value == null)
		{
			return null;
		}
		DecimalFormat formatter = getFormat();
		return formatter.parse((String)value);
	}

	/**
	 * Gets the format and optionally apply the pattern if given
	 * @return DecimalFormat formatter instance
	 */
	protected DecimalFormat getFormat()
	{
		final Locale locale = getLocale();
		final String pattern = getPattern();
		DecimalFormat formatter = (DecimalFormat)DecimalFormat.getInstance(locale);
		if (pattern != null)
		{
			if (isLocPattern())
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
	 * Translates the given value to a number optionally using the supplied
	 * pattern.
	 * @param value the value to convert
	 * @return parsed number
	 * @throws ConversionException
	 */
	protected Number getNumber(Object value) throws ConversionException
	{
		if (value instanceof Number)
		{
			return (Number)value;
		}
		Number temp = null;
		final String pattern = getPattern();
		try
		{
			if (pattern != null)
			{
				temp = (Number)parse(value, pattern);
			}
			else
			{
				String stringval = null;
				if (value instanceof String)
				{
					stringval = (String)value;
				}
				else if (value instanceof String[])
				{
					stringval = ((String[])value)[0];
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
				temp = (Number)parse(value, pattern);
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
				DecimalFormat formatter = getFormat();
				dpat = formatter.toLocalizedPattern();
			}
			throw new ConversionException(e).setPattern(dpat);
		}
		return temp;
	}
}