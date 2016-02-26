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
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.FormatStyle;
import java.util.Locale;

/**
 * Date converter that uses javax.time and can be configured to take the time zone difference between
 * clients and server into account, and that is configured for a certain date style. The pattern
 * will always be locale specific.
 * <p>
 * This converter is especially suited on a per-component base.
 * </p>
 * 
 * @see org.apache.wicket.extensions.markup.html.form.DateTextField
 * @see java.time.LocalDate
 * @see DateTimeFormatter
 * 
 * @author eelcohillenius
 */
public class StyleDateConverter extends LocalDateConverter
{
	private static final long serialVersionUID = 1L;

	/**
	 * Date style to use. See {@link DateTimeFormatter#ofLocalizedDate(FormatStyle)}.
	 */
	private final FormatStyle dateStyle;

	/**
	 * Construct. The dateStyle 'S-' (which is the same as {@link DateTimeFormatter#ofLocalizedDate(FormatStyle)}) will
	 * be used for constructing the date format for the current locale.
	 * 
	 */
	public StyleDateConverter()
	{
		this(FormatStyle.SHORT);
	}

	/**
	 * Construct. The provided pattern will be used as the base format (but they will be localized
	 * for the current locale) and if null, {@link DateTimeFormatter#ofLocalizedDate(FormatStyle)} will be used.
	 * 
	 * @param dateStyle
	 *            Date style to use. See {@link DateTimeFormatter#ofLocalizedDate(FormatStyle)}.
	 * @throws IllegalArgumentException
	 *             in case dateStyle is null
	 */
	public StyleDateConverter(FormatStyle dateStyle)
	{
		super();
		this.dateStyle = dateStyle;
	}

	public StyleDateConverter(String dateStyle)
	{
		this(parseFormatStyle(dateStyle.charAt(0)));
	}

	/**
	 * Gets the optional date pattern.
	 * 
	 * @return datePattern
	 */
	@Override
	public final String getPattern(Locale locale)
	{
		return DateTimeFormatterBuilder.getLocalizedDateTimePattern(dateStyle, null, IsoChronology.INSTANCE, locale);
	}

	/**
	 * @return formatter The formatter for the current conversion
	 */
	@Override
	public DateTimeFormatter getFormat(Locale locale)
	{
		return dateStyle == null ? null : DateTimeFormatter.ofLocalizedDate(dateStyle).withLocale(locale);
	}

	public static FormatStyle parseFormatStyle(char style)
	{
		return DateField.parseFormatStyle(style);
	}

	@Override
	public LocalDate convertToObject(String value, DateTimeFormatter format, Locale locale) {
		if (format == null) {
			return null;
		}
		try
		{
			return LocalDate.parse(value, format);
		}
		catch (RuntimeException e)
		{
			throw newConversionException(e, locale);
		}
	}
}
