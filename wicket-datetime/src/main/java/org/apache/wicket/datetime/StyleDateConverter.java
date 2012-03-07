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
package org.apache.wicket.datetime;

import java.util.Locale;

import org.apache.wicket.datetime.markup.html.form.DateTextField;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;


/**
 * Date converter that uses Joda Time and can be configured to take the time zone difference between
 * clients and server into account, and that is configured for a certain date style. The pattern
 * will always be locale specific.
 * <p>
 * This converter is especially suited on a per-component base.
 * </p>
 * 
 * @see DateTextField
 * @see DateTime
 * @see DateTimeFormat
 * @see DateTimeZone
 * 
 * @author eelcohillenius
 */
public class StyleDateConverter extends DateConverter
{

	private static final long serialVersionUID = 1L;

	/**
	 * Date style to use. See {@link DateTimeFormat#forStyle(String)}.
	 */
	private final String dateStyle;

	/**
	 * Construct. The dateStyle 'S-' (which is the same as {@link DateTimeFormat#shortDate()}) will
	 * be used for constructing the date format for the current locale. </p> When
	 * applyTimeZoneDifference is true, the current time is applied on the parsed date, and the date
	 * will be corrected for the time zone difference between the server and the client. For
	 * instance, if I'm in Seattle and the server I'm working on is in Amsterdam, the server is 9
	 * hours ahead. So, if I'm inputting say 12/24 at a couple of hours before midnight, at the
	 * server it is already 12/25. If this boolean is true, it will be transformed to 12/25, while
	 * the client sees 12/24. </p>
	 * 
	 * @param applyTimeZoneDifference
	 *            whether to apply the difference in time zones between client and server
	 */
	public StyleDateConverter(boolean applyTimeZoneDifference)
	{
		this("S-", applyTimeZoneDifference);
	}

	/**
	 * Construct. The provided pattern will be used as the base format (but they will be localized
	 * for the current locale) and if null, {@link DateTimeFormat#shortDate()} will be used. </p>
	 * When applyTimeZoneDifference is true, the current time is applied on the parsed date, and the
	 * date will be corrected for the time zone difference between the server and the client. For
	 * instance, if I'm in Seattle and the server I'm working on is in Amsterdam, the server is 9
	 * hours ahead. So, if I'm inputting say 12/24 at a couple of hours before midnight, at the
	 * server it is already 12/25. If this boolean is true, it will be transformed to 12/25, while
	 * the client sees 12/24. </p>
	 * 
	 * @param dateStyle
	 *            Date style to use. The first character is the date style, and the second character
	 *            is the time style. Specify a character of 'S' for short style, 'M' for medium, 'L'
	 *            for long, and 'F' for full. A date or time may be ommitted by specifying a style
	 *            character '-'. See {@link DateTimeFormat#forStyle(String)}.
	 * @param applyTimeZoneDifference
	 *            whether to apply the difference in time zones between client and server
	 * @throws IllegalArgumentException
	 *             in case dateStyle is null
	 */
	public StyleDateConverter(String dateStyle, boolean applyTimeZoneDifference)
	{
		super(applyTimeZoneDifference);
		if (dateStyle == null)
		{
			throw new IllegalArgumentException("dateStyle must be not null");
		}
		this.dateStyle = dateStyle;
	}

	/**
	 * Gets the optional date pattern.
	 * 
	 * @return datePattern
	 */
	@Override
	public final String getDatePattern(Locale locale)
	{
		return DateTimeFormat.patternForStyle(dateStyle, locale);
	}

	/**
	 * @return formatter The formatter for the current conversion
	 */
	@Override
	protected DateTimeFormatter getFormat(Locale locale)
	{
		return DateTimeFormat.forPattern(getDatePattern(locale))
			.withLocale(locale)
			.withPivotYear(2000);
	}
}