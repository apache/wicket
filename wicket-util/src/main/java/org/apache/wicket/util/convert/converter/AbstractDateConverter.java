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

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.wicket.util.string.Strings;

/**
 * A base class for all Date related converters
 *
 * @param <D>
 *     the type of the Date that is supported by this converter
 */
public abstract class AbstractDateConverter<D extends Date> extends AbstractConverter<D>
{
	/**
	 * Creates a new instance of D out of the passed date(time) as long
	 * @param date
	 *      the date(time) in millis since Epoch
	 * @return a new instance of the specific type D
	 */
	protected abstract D createDateLike(long date);

	@Override
	public D convertToObject(final String value, Locale locale)
	{
		if (Strings.isEmpty(value))
		{
			return null;
		}

		DateFormat format = getDateFormat(locale);
		Date date = parse(format, value, locale);
		return createDateLike(date.getTime());
	}

	@Override
	public String convertToString(final D value, final Locale locale)
	{
		if (value == null)
		{
			return null;
		}

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

		// return a clone because DateFormat.getDateInstance uses a pool
		return (DateFormat) DateFormat.getDateInstance(DateFormat.SHORT, locale).clone();
	}
}
