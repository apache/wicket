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

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

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

	/** The date format to use */
	private final ConcurrentHashMap<Locale, NumberFormat> numberFormats = new ConcurrentHashMap<>();

	/**
	 * @param locale
	 *            The locale
	 * @return Returns the numberFormat.
	 */
	public NumberFormat getNumberFormat(final Locale locale)
	{
		NumberFormat numberFormat = numberFormats.get(locale);
		if (numberFormat == null)
		{
			numberFormat = newNumberFormat(locale);

			if (numberFormat instanceof DecimalFormat)
			{
				// always try to parse BigDecimals
				((DecimalFormat)numberFormat).setParseBigDecimal(true);
			}

			NumberFormat tmpNumberFormat = numberFormats.putIfAbsent(locale, numberFormat);
			if (tmpNumberFormat != null)
			{
				numberFormat = tmpNumberFormat;
			}
		}
		// return a clone because NumberFormat.get..Instance use a pool
		return (NumberFormat)numberFormat.clone();
	}

	/**
	 * Creates a new {@link NumberFormat} for the given locale. The instance is later cached and is
	 * accessible through {@link #getNumberFormat(Locale)}
	 *
	 * @param locale
	 * @return number format
	 */
	protected abstract NumberFormat newNumberFormat(final Locale locale);

	/**
	 * Parses a value as a String and returns a Number.
	 * 
	 * @param value
	 *            The object to parse (after converting with toString())
	 * @param min
	 *            The minimum allowed value or {@code null} if none
	 * @param max
	 *            The maximum allowed value or {@code null} if none
	 * @param locale
	 * @return The number
	 * @throws ConversionException
	 *             if value is unparsable or out of range
	 */
	protected BigDecimal parse(Object value, final BigDecimal min, final BigDecimal max, Locale locale)
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

		BigDecimal bigDecimal;
		if (number instanceof BigDecimal)
		{
			bigDecimal = (BigDecimal)number;
		}
		else
		{
			// should occur rarely, see #getNumberFormat(Locale)
			bigDecimal = new BigDecimal(number.toString());
		}

		if (min != null && bigDecimal.compareTo(min) < 0)
		{
			throw newConversionException("Value cannot be less than " + min, value, locale)
					.setFormat(numberFormat);
		}

		if (max != null && bigDecimal.compareTo(max) > 0)
		{
			throw newConversionException("Value cannot be greater than " + max, value, locale)
					.setFormat(numberFormat);
		}

		return bigDecimal;
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
