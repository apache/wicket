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

import java.time.LocalTime;
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
 */
public class StyleTimeConverter extends LocalTimeConverter
{
	private static final long serialVersionUID = 1L;

	/**
	 * Date style to use. See {@link DateTimeFormatter#ofLocalizedTime(FormatStyle)}.
	 */
	private final FormatStyle timeStyle;

	/**
	 * Construct. The dateStyle 'S-' (which is the same as {@link DateTimeFormatter#ofLocalizedTime(FormatStyle)}) will
	 * be used for constructing the date format for the current locale.
	 * 
	 */
	public StyleTimeConverter()
	{
		this(FormatStyle.SHORT);
	}

	/**
	 * Construct. The provided pattern will be used as the base format (but they will be localized
	 * for the current locale) and if null, {@link DateTimeFormatter#ofLocalizedTime(FormatStyle)} will be used.
	 * 
	 * @param timeStyle
	 *            Date style to use. See {@link DateTimeFormatter#ofLocalizedTime(FormatStyle)}.
	 * @throws IllegalArgumentException
	 *             in case dateStyle is null
	 */
	public StyleTimeConverter(FormatStyle timeStyle)
	{
		super();
		this.timeStyle = timeStyle;
	}

	public StyleTimeConverter(String timeStyle)
	{
		this(parseFormatStyle(timeStyle.charAt(0)));
	}

	/**
	 * Gets the optional time pattern.
	 * 
	 * @return timePattern
	 */
	@Override
	public final String getPattern(Locale locale)
	{
		return DateTimeFormatterBuilder.getLocalizedDateTimePattern(null, timeStyle, IsoChronology.INSTANCE, locale);
	}

	/**
	 * @return formatter The formatter for the current conversion
	 */
	@Override
	public DateTimeFormatter getFormat(Locale locale)
	{
		return timeStyle == null ? null : DateTimeFormatter.ofLocalizedTime(timeStyle).withLocale(locale);
	}

	public static FormatStyle parseFormatStyle(char style)
	{
		return TimeField.parseFormatStyle(style);
	}

	@Override
	public LocalTime convertToObject(String value, DateTimeFormatter format, Locale locale) {
		if (format == null) {
			return null;
		}
		try
		{
			return LocalTime.parse(value, format);
		}
		catch (RuntimeException e)
		{
			throw newConversionException(e, locale);
		}
	}
}
