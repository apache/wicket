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
package org.apache.wicket.util.convert.converter;

import java.text.NumberFormat;
import java.util.Locale;

import org.apache.wicket.util.convert.ConversionException;


/**
 * Base class for all number converters.
 * 
 * @author Jonathan Locke
 * @param <N>
 */
public abstract class AbstractNumberConverter<N extends Number> extends AbstractConverter<N>
{
	private static final long serialVersionUID = 1L;

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
	protected N parse(Object value, final double min, final double max, Locale locale)
	{
		if (locale == null)
		{
			locale = Locale.getDefault();
		}

		if (value == null)
		{
			return null;
		}
		else if (value instanceof String)
		{
			// Convert spaces to no-break space (U+00A0) as required by Java formats:
			// http://bugs.sun.com/view_bug.do?bug_id=4510618
			value = ((String)value).replaceAll("(\\d+)\\s(?=\\d)", "$1\u00A0");
		}

		final NumberFormat numberFormat = getNumberFormat(locale);
		final N number = parse(numberFormat, value, locale);

		if (number == null)
		{
			return null;
		}

		if (number.doubleValue() < min)
		{
			throw newConversionException("Value cannot be less than " + min, value, locale).setFormat(
				numberFormat);
		}

		if (number.doubleValue() > max)
		{
			throw newConversionException("Value cannot be greater than " + max, value, locale).setFormat(
				numberFormat);
		}

		return number;
	}

	@Override
	public String convertToString(final N value, final Locale locale)
	{
		NumberFormat fmt = getNumberFormat(locale);
		if (fmt != null)
		{
			return fmt.format(value);
		}
		return value.toString();
	}
}
