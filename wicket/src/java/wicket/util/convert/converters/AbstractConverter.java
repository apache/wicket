/*
 * $Id$ $Revision:
 * 1.9 $ $Date$
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

import java.text.Format;
import java.text.ParsePosition;
import java.util.Locale;

import wicket.util.convert.ConversionException;
import wicket.util.convert.ITypeConverter;

/**
 * Base class for locale aware type converters.
 * 
 * @author Eelco Hillenius
 */
public abstract class AbstractConverter implements ITypeConverter
{
	/**
	 * Parses a value using one of the java.util.text format classes.
	 * 
	 * @param format
	 *            The format to use
	 * @param value
	 *            The object to parse
	 * @return The object
	 * @throws ConversionException
	 *             Thrown if parsing fails
	 */
	protected Object parse(final Format format, final Object value)
	{
		final ParsePosition position = new ParsePosition(0);
		final String stringValue = value.toString();
		final Object result = format.parseObject(stringValue, position);
		if (position.getIndex() != stringValue.length())
		{
			throw newConversionException("Cannot parse '" + value + "' using format " + format,
					value, null).setFormat(format);
		}
		return result;
	}

	/**
	 * Creates a conversion exception for throwing
	 * 
	 * @param message
	 *            The message
	 * @param value
	 *            The value that didn't convert
	 * @param locale
	 *            The locale
	 * @return The ConversionException
	 */
	protected ConversionException newConversionException(final String message, final Object value,
			final Locale locale)
	{
		return new ConversionException(message).setSourceValue(value)
				.setTargetType(getTargetType()).setTypeConverter(this).setLocale(locale);
	}

	/**
	 * @return The target type of this type converter
	 */
	protected abstract Class getTargetType();
}