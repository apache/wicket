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

import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

import org.apache.wicket.util.convert.ConversionException;

/**
 * Converts to {@link Time}.
 */
public class SqlTimeConverter extends AbstractConverter<Time>
{

	private static final long serialVersionUID = 1L;

	/** @see org.apache.wicket.util.convert.converter.DateConverter#convertToObject(java.lang.String,java.util.Locale) */
	@Override
	public Time convertToObject(final String value, Locale locale)
	{
		if (value == null)
		{
			return null;
		}
		if (locale == null)
		{
			locale = Locale.getDefault();
		}
		DateFormat format = DateFormat.getTimeInstance(DateFormat.SHORT, locale);
		try
		{
			Date date = format.parse(value);
			return new Time(date.getTime());
		}
		catch (ParseException e)
		{
			throw new ConversionException("Cannot parse '" + value + "' using format " + format).setSourceValue(
				value)
				.setTargetType(getTargetType())
				.setConverter(this)
				.setLocale(locale);
		}
	}

	@Override
	public String convertToString(final Time time, Locale locale)
	{
		if (time == null)
		{
			return null;
		}
		if (locale == null)
		{
			locale = Locale.getDefault();
		}
		DateFormat format = DateFormat.getTimeInstance(DateFormat.SHORT, locale);
		return format.format(time);
	}

	@Override
	protected Class<Time> getTargetType()
	{
		return Time.class;
	}
}
