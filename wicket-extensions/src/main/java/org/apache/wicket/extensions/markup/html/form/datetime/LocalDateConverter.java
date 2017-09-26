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
package org.apache.wicket.extensions.markup.html.form.datetime;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.string.Strings;


/**
 * Base class for Joda Time based date converters. It contains the logic to parse and format,
 * optionally taking the time zone difference between clients and the server into account.
 * <p>
 * Converters of this class are best suited for per-component use.
 * </p>
 * 
 * @author eelcohillenius
 */
public abstract class LocalDateConverter implements IDateConverter<LocalDate>
{
	private static final long serialVersionUID = 1L;

	public LocalDate convertToObject(String value, DateTimeFormatter format, Locale locale) {
		try
		{
			// parse date retaining the time of the submission
			return LocalDate.parse(value, format);
		}
		catch (RuntimeException e)
		{
			throw newConversionException(e, locale);
		}
	}

	@Override
	public LocalDate convertToObject(String value, Locale locale)
	{
		if (Strings.isEmpty(value))
		{
			return null;
		}

		DateTimeFormatter format = getFormat(locale);
		Args.notNull(format, "format");

		return convertToObject(value, format, locale);
	}

	/**
	 * Creates a ConversionException and sets additional context information to it.
	 *
	 * @param cause
	 *            - {@link RuntimeException} cause
	 * @param locale
	 *            - {@link Locale} used to set 'format' variable with localized pattern
	 * @return {@link ConversionException}
	 */
	ConversionException newConversionException(RuntimeException cause, Locale locale)
	{
		return new ConversionException(cause)
				.setVariable("format", getPattern(locale));
	}

	@Override
	public String convertToString(LocalDate dateTime, Locale locale)
	{
		DateTimeFormatter format = getFormat(locale);
		return format.format(dateTime);
	}

	/**
	 * @param locale
	 *            The locale used to convert the value
	 * @return Gets the pattern that is used for printing and parsing
	 */
	public abstract String getPattern(Locale locale);

	/**
	 * @param locale
	 *            The locale used to convert the value
	 * 
	 * @return formatter The formatter for the current conversion
	 */
	public abstract DateTimeFormatter getFormat(Locale locale);
}
