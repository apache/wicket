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

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.lang.Args;

/**
 * Converts to {@link Calendar}.
 */
public class CalendarConverter implements IConverter<Calendar>
{
	private static final long serialVersionUID = 1L;

	private final IConverter<Date> dateConverter;

	/**
	 * Construct.
	 */
	public CalendarConverter()
	{
		this(new DateConverter());
	}

	/**
	 * Construct.
	 * 
	 * @param dateConverter
	 *            delegated converter, not null
	 */
	public CalendarConverter(IConverter<Date> dateConverter)
	{
		Args.notNull(dateConverter, "dateConverter");

		this.dateConverter = dateConverter;
	}

	/**
	 * @see org.apache.wicket.util.convert.IConverter#convertToObject(java.lang.String,
	 *      java.util.Locale)
	 */
	@Override
	public Calendar convertToObject(final String value, final Locale locale)
	{
		Date date = dateConverter.convertToObject(value, locale);
		if (date == null)
		{
			return null;
		}

		Calendar calendar = locale != null ? Calendar.getInstance(locale) : Calendar.getInstance();
		calendar.setTime(date);
		return calendar;
	}

	/**
	 * @see org.apache.wicket.util.convert.IConverter#convertToString(java.lang.Object,
	 *      java.util.Locale)
	 */
	@Override
	public String convertToString(final Calendar value, final Locale locale)
	{
		if (value == null)
		{
			return null;
		}
		return dateConverter.convertToString(value.getTime(), locale);
	}
}
