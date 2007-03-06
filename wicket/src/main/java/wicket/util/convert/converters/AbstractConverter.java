/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.util.convert.converters;

import java.text.Format;
import java.text.ParsePosition;
import java.util.Locale;

import wicket.util.convert.ConversionException;
import wicket.util.convert.IConverter;

/**
 * Base class for locale aware type converters.
 * 
 * @author Eelco Hillenius
 */
public abstract class AbstractConverter implements IConverter
{
	/**
	 * Parses a value using one of the java.util.text format classes.
	 * 
	 * @param format
	 *            The format to use
	 * @param value
	 *            The object to parse
	 * @param locale
	 *            The locale to use to parse.
	 * @return The object
	 * @throws ConversionException
	 *             Thrown if parsing fails
	 */
	protected Object parse(final Format format, final Object value, Locale locale)
	{
		final ParsePosition position = new ParsePosition(0);
		final String stringValue = value.toString();
		final Object result = format.parseObject(stringValue, position);
		if (position.getIndex() != stringValue.length())
		{
			throw newConversionException("Cannot parse '" + value + "' using format " + format,
					value, locale).setFormat(format);
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
			Locale locale)
	{
		return new ConversionException(message).setSourceValue(value)
				.setTargetType(getTargetType()).setConverter(this).setLocale(locale);
	}

	/**
	 * @return The target type of this type converter
	 */
	protected abstract Class getTargetType();

	/**
	 * @see wicket.util.convert.IConverter#convertToString(java.lang.Object,
	 *      Locale)
	 */
	public String convertToString(Object value, Locale locale)
	{
		if (value == null)
		{
			return null;
		}
		return value.toString();
	}

}