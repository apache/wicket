/*
 * $Id$
 * $Revision$ $Date$
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
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
public abstract class NumberConverter extends AbstractConverter
{
	/** The number format */
	protected NumberFormat numberFormat;

	/**
	 * Constructor
	 */
	public NumberConverter()
	{
	}

	/**
	 * Constructor
	 * 
	 * @param locale
	 *            The locale for this converter
	 */
	public NumberConverter(final Locale locale)
	{
		super(locale);
	}

	/**
	 * @return Returns the numberFormat.
	 */
	public NumberFormat getNumberFormat()
	{
		if (numberFormat == null)
		{
			numberFormat = NumberFormat.getInstance(getLocale());
		}
		return numberFormat;
	}

	/**
	 * @param numberFormat
	 *            The numberFormat to set.
	 */
	public final void setNumberFormat(final NumberFormat numberFormat)
	{
		this.numberFormat = numberFormat;
	}

	/**
	 * Parses a value as a String and returns a Number.
	 * 
	 * @param value
	 *            The object to parse (after converting with toString())
	 * @param min
	 *            The minimum allowed value
	 * @param max
	 *            The maximum allowed value
	 * @return The number
	 * @throws ConversionException
	 *             if value is unparsable or out of range
	 */
	protected Number parse(final Object value, final double min, final double max)
	{
		final Number number = (Number)parse(getNumberFormat(), value);
		if (number.doubleValue() < min)
		{
			throw newConversionException("Value cannot be less than " + min, value).setFormat(
					numberFormat);
		}
		if (number.doubleValue() > max)
		{
			throw newConversionException("Value cannot be greater than " + max, value).setFormat(
					numberFormat);
		}
		return number;
	}
}