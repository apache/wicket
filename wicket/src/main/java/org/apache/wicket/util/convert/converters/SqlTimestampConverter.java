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
package org.apache.wicket.util.convert.converters;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

import org.apache.wicket.util.convert.ConversionException;

/**
 * Converts to {@link Timestamp}.
 * 
 * @author eelcohillenius
 */
public class SqlTimestampConverter extends AbstractConverter<Timestamp>
{
	private static final long serialVersionUID = 1L;

	/** @see org.apache.wicket.util.convert.converters.DateConverter#convertToObject(java.lang.String,java.util.Locale) */
	public Timestamp convertToObject(String value, Locale locale)
	{
		if (value == null)
			return null;
		if (locale == null)
			locale = Locale.getDefault();
		DateFormat format = DateFormat.getTimeInstance(DateFormat.SHORT, locale);
		try
		{
			Date date = format.parse(value);
			return new Timestamp(date.getTime());
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
	public String convertToString(final Timestamp value, Locale locale)
	{
		if (value == null)
			return null;
		if (locale == null)
			locale = Locale.getDefault();
		Timestamp timestamp = value;
		DateFormat format = DateFormat.getTimeInstance(DateFormat.SHORT, locale);
		return format.format(timestamp);
	}

	@Override
	protected Class<Timestamp> getTargetType()
	{
		return Timestamp.class;
	}
}
