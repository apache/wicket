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

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

/**
 * Converts to {@link Timestamp}.
 * 
 * @author eelcohillenius
 */
public class SqlTimestampConverter extends AbstractConverter<Timestamp>
{
	private static final long serialVersionUID = 1L;

	private final int dateFormat;
	private final int timeFormat;

	/**
	 * Construct.
	 */
	public SqlTimestampConverter()
	{
		dateFormat = DateFormat.SHORT;
		timeFormat = DateFormat.SHORT;
	}

	/**
	 * Construct.
	 * 
	 * @param dateFormat
	 *            See java.text.DateFormat for details. Defaults to DateFormat.SHORT
	 */
	public SqlTimestampConverter(final int dateFormat)
	{
		this.dateFormat = dateFormat;
		timeFormat = DateFormat.SHORT;
	}

	/**
	 * Construct.
	 * 
	 * @param dateFormat
	 *            See java.text.DateFormat for details. Defaults to DateFormat.SHORT * @param
	 *            timeFormat See java.text.DateFormat for details. Defaults to DateFormat.SHORT
	 * @param timeFormat
	 */
	public SqlTimestampConverter(final int dateFormat, final int timeFormat)
	{
		this.dateFormat = dateFormat;
		this.timeFormat = timeFormat;
	}


	/**
	 * 
	 * @see org.apache.wicket.util.convert.IConverter#convertToObject(java.lang.String,
	 *      java.util.Locale)
	 */
	@Override
	public Timestamp convertToObject(final String value, Locale locale)
	{
		if (value == null)
		{
			return null;
		}

		if (locale == null)
		{
			locale = Locale.getDefault();
		}

		DateFormat format = (DateFormat) DateFormat.getDateTimeInstance(dateFormat, timeFormat, locale).clone();
		try
		{
			Date date = format.parse(value);
			return new Timestamp(date.getTime());
		}
		catch (ParseException e)
		{
			throw newConversionException("Cannot parse '" + value + "' using format " + format,
				value, locale);
		}
	}

	/**
	 * 
	 * @see org.apache.wicket.util.convert.converter.AbstractConverter#convertToString(java.lang.Object,
	 *      java.util.Locale)
	 */
	@Override
	public String convertToString(final Timestamp timestamp, Locale locale)
	{
		if (timestamp == null)
		{
			return null;
		}

		if (locale == null)
		{
			locale = Locale.getDefault();
		}

		DateFormat format = (DateFormat) DateFormat.getDateTimeInstance(dateFormat, timeFormat, locale).clone();
		return format.format(timestamp);
	}

	/**
	 * 
	 * @see org.apache.wicket.util.convert.converter.AbstractConverter#getTargetType()
	 */
	@Override
	protected Class<Timestamp> getTargetType()
	{
		return Timestamp.class;
	}
}
