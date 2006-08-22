/*
 * $Id: AbstractNumberConverter.java 5791 2006-05-20 00:32:57 +0000 (Sat, 20 May
 * 2006) joco01 $ $Revision$ $Date: 2006-05-20 00:32:57 +0000 (Sat, 20
 * May 2006) $
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.util.convert.converters;

import java.text.NumberFormat;
import java.util.Locale;

import wicket.util.convert.ConversionException;

/**
 * Base class for all number converters.
 * 
 * @author Jonathan Locke
 */
public abstract class AbstractNumberConverter extends AbstractConverter
{
	/**
	 * @param locale
	 * @return Returns the numberFormat.
	 */
	public abstract NumberFormat getNumberFormat(Locale locale);


	/**
	 * Parses a value as a String and returns a Number.
	 * 
	 * @param value
	 *            The object to parse (after converting with toString())
	 * @param min
	 *            The minimum allowed value
	 * @param max
	 *            The maximum allowed value
	 * @param locale
	 * @return The number
	 * @throws ConversionException
	 *             if value is unparsable or out of range
	 */
	protected Number parse(Object value, final double min, final double max, Locale locale)
	{
		if (locale == null)
		{
			locale = Locale.getDefault();
		}

		final NumberFormat numberFormat = getNumberFormat(locale);

		if (value instanceof String)
		{
			// Convert spaces to no-break space (U+00A0) to fix problems with
			// broser conversions.
			// Space is not valid thousands-separator, but no-br space is.
			String v = (String)value;
			value = v.replace(' ', '\u00A0');
		}

		final Number number = (Number)parse(numberFormat, value, locale);

		if (number == null)
		{
			return null;
		}

		if (number.doubleValue() < min)
		{
			throw newConversionException("Value cannot be less than " + min, value, locale)
					.setFormat(numberFormat);
		}

		if (number.doubleValue() > max)
		{
			throw newConversionException("Value cannot be greater than " + max, value, locale)
					.setFormat(numberFormat);
		}

		return number;
	}

	/**
	 * @see wicket.util.convert.IConverter#convertToString(java.lang.Object,
	 *      Locale)
	 */
	@Override
	public String convertToString(final Object value, Locale locale)
	{
		NumberFormat fmt = getNumberFormat(locale);
		if (fmt != null)
		{
			return fmt.format(value);
		}
		return value.toString();
	}

}
