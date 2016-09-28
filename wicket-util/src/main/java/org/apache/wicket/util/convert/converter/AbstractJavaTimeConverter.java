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

import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.util.Locale;

import org.apache.wicket.util.string.Strings;

/**
 * A base class for all java.time.** related converters
 *
 * @param <T>
 *     the type of the Temporal that is supported by this converter
 */
public abstract class AbstractJavaTimeConverter<T extends Temporal> extends AbstractConverter<T>
{
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new instance of D out of the passed date(time) as long
	 * @param temporalAccessor
	 *      the date(time) in millis since Epoch
	 * @return a new instance of the specific type D
	 */
	protected abstract T createTemporal(TemporalAccessor temporalAccessor);

	@Override
	public T convertToObject(final String value, Locale locale)
	{
		if (Strings.isEmpty(value))
		{
			return null;
		}

		DateTimeFormatter dateTimeFormatter = getDateTimeFormatter(locale);
		TemporalAccessor temporalAccessor = dateTimeFormatter.parse(value);
		return createTemporal(temporalAccessor);
	}

	@Override
	public String convertToString(final T value, final Locale locale)
	{
		if (value == null)
		{
			return null;
		}

		final DateTimeFormatter dateTimeFormatter = getDateTimeFormatter(locale);
		if (dateTimeFormatter != null)
		{
			return dateTimeFormatter.format(value);
		}
		return value.toString();
	}

	/**
	 * @param locale
	 * @return Returns the date time format.
	 */
	public DateTimeFormatter getDateTimeFormatter(Locale locale)
	{
		if (locale == null)
		{
			locale = Locale.getDefault();
		}

		return getDateTimeFormatter().withLocale(locale);
	}

	protected abstract DateTimeFormatter getDateTimeFormatter();
}
