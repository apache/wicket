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

import java.sql.Date;
import java.text.DateFormat;
import java.text.Format;
import java.text.ParsePosition;
import java.util.Locale;

import org.apache.wicket.util.string.Strings;

/**
 * Converts to {@link java.sql.Date}.
 */
public class SqlDateConverter extends AbstractConverter<Date>
{
	private static final long serialVersionUID = 1L;

	/**
	 * @see org.apache.wicket.util.convert.IConverter#convertToObject(java.lang.String,Locale)
	 */
	public Date convertToObject(final String value, final Locale locale)
	{
		if ((value == null) || Strings.isEmpty(value))
		{
			return null;
		}
		else
		{
			java.util.Date date = parseDate(getDateFormat(locale), value, locale);
			return new Date(date.getTime());
		}
	}

	/**
	 * @see org.apache.wicket.util.convert.converter.DateConverter#convertToObject(java.lang.String,
	 *      java.util.Locale)
	 */
	@Override
	public String convertToString(final Date value, final Locale locale)
	{
		final DateFormat dateFormat = getDateFormat(locale);
		if (dateFormat != null)
		{
			return dateFormat.format(value);
		}
		return value.toString();
	}


	/**
	 * @param locale
	 * @return Returns the date format.
	 */
	public DateFormat getDateFormat(Locale locale)
	{
		if (locale == null)
		{
			locale = Locale.getDefault();
		}

		return DateFormat.getDateInstance(DateFormat.SHORT, locale);
	}

	@Override
	protected Class<Date> getTargetType()
	{
		return Date.class;
	}

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
	 * @throws org.apache.wicket.util.convert.ConversionException
	 *             Thrown if parsing fails
	 */
	@SuppressWarnings("unchecked")
	private java.util.Date parseDate(final Format format, final Object value, final Locale locale)
	{
		final ParsePosition position = new ParsePosition(0);
		final String stringValue = value.toString();
		final java.util.Date result = (java.util.Date)format.parseObject(stringValue, position);

		if (position.getIndex() != stringValue.length())
		{
			throw newConversionException("Cannot parse '" + value + "' using format " + format,
					value, locale).setFormat(format);
		}
		return result;
	}
}
