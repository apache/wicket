/*
 * $Id$ $Revision:
 * 1.10 $ $Date$
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

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import wicket.util.convert.ConversionException;

/**
 * Converts from Object to Double.
 * 
 * @author Eelco Hillenius
 * @author Jonathan Locke
 */
public final class DoubleConverter extends DecimalConverter
{
	/**
	 * Constructor.
	 */
	public DoubleConverter()
	{
	}

	/**
	 * Constructor.
	 * 
	 * @param locale
	 *            The locale for this converter
	 */
	public DoubleConverter(final Locale locale)
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
			return new Double(((Number)value).doubleValue());
		}

		NumberFormat format = getNumberFormat();
		try
		{
			return new Double(format.parse(value.toString()).doubleValue());
		}
		catch (ParseException e)
		{
			final ConversionException conversionException = new ConversionException(
					"Cannot convert '" + value + "' to Double", e);
			if (format instanceof DecimalFormat)
			{
				String pattern = ((DecimalFormat)format).toLocalizedPattern();
				conversionException.setPattern(pattern);
			}
			throw conversionException;
		}
	}
}