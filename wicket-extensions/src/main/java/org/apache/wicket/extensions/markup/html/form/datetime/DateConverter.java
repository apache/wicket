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

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.wicket.Session;
import org.apache.wicket.core.request.ClientInfo;
import org.apache.wicket.protocol.http.request.WebClientInfo;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;
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
public abstract class DateConverter implements IConverter<ZonedDateTime>
{
	private static final long serialVersionUID = 1L;

	/**
	 * Whether to apply the time zone difference when interpreting dates.
	 */
	private final boolean applyTimeZoneDifference;

	/**
	 * Construct. </p> When applyTimeZoneDifference is true, the current time is applied on the
	 * parsed date, and the date will be corrected for the time zone difference between the server
	 * and the client. For instance, if I'm in Seattle and the server I'm working on is in
	 * Amsterdam, the server is 9 hours ahead. So, if I'm inputting say 12/24 at a couple of hours
	 * before midnight, at the server it is already 12/25. If this boolean is true, it will be
	 * transformed to 12/25, while the client sees 12/24. </p>
	 * 
	 * @param applyTimeZoneDifference
	 *            whether to apply the difference in time zones between client and server
	 */
	public DateConverter(boolean applyTimeZoneDifference)
	{
		this.applyTimeZoneDifference = applyTimeZoneDifference;
	}

	protected ZonedDateTime convertToObject(String value, DateTimeFormatter format, Locale locale) {
		try
		{
			// parse date retaining the time of the submission
			return ZonedDateTime.parse(value, format);
		}
		catch (RuntimeException e)
		{
			throw newConversionException(e, locale);
		}
	}

	@Override
	public ZonedDateTime convertToObject(String value, Locale locale)
	{
		if (Strings.isEmpty(value))
		{
			return null;
		}

		DateTimeFormatter format = getFormat(locale);
		Args.notNull(format, "format");

		if (applyTimeZoneDifference)
		{
			ZoneId zoneId = getClientTimeZone();

			// set time zone for client
			format = format.withZone(getTimeZone());

			ZonedDateTime dateTime = convertToObject(value, format, locale);
			// apply the server time zone to the parsed value
			if (zoneId != null)
			{
				dateTime = dateTime.withZoneSameInstant(zoneId);
			}

			return dateTime;
		}
		else
		{
			return convertToObject(value, format, locale);
		}
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
				.setVariable("format", getDatePattern(locale));
	}

	@Override
	public String convertToString(ZonedDateTime dateTime, Locale locale)
	{
		DateTimeFormatter format = getFormat(locale);

		if (applyTimeZoneDifference)
		{
			ZoneId zoneId = getClientTimeZone();
			if (zoneId != null)
			{
				// apply time zone to formatter
				format = format.withZone(zoneId);
			}
		}
		return format.format(dateTime);
	}

	/**
	 * Gets whether to apply the time zone difference when interpreting dates.
	 * 
	 * </p> When true, the current time is applied on the parsed date, and the date will be
	 * corrected for the time zone difference between the server and the client. For instance, if
	 * I'm in Seattle and the server I'm working on is in Amsterdam, the server is 9 hours ahead.
	 * So, if I'm inputting say 12/24 at a couple of hours before midnight, at the server it is
	 * already 12/25. If this boolean is true, it will be transformed to 12/25, while the client
	 * sees 12/24. </p>
	 * 
	 * @return whether to apply the difference in time zones between client and server
	 */
	public final boolean getApplyTimeZoneDifference()
	{
		return applyTimeZoneDifference;
	}

	/**
	 * @param locale
	 *            The locale used to convert the value
	 * @return Gets the pattern that is used for printing and parsing
	 */
	public abstract String getDatePattern(Locale locale);

	/**
	 * Gets the client's time zone.
	 * 
	 * @return The client's time zone or null
	 */
	protected ZoneId getClientTimeZone()
	{
		ClientInfo info = Session.get().getClientInfo();
		if (info instanceof WebClientInfo)
		{
			TimeZone timeZone = ((WebClientInfo) info).getProperties().getTimeZone();
			return timeZone.toZoneId();
		}
		return null;
	}

	/**
	 * @param locale
	 *            The locale used to convert the value
	 * 
	 * @return formatter The formatter for the current conversion
	 */
	protected abstract DateTimeFormatter getFormat(Locale locale);

	/**
	 * Gets the server time zone. Override this method if you want to fix to a certain time zone,
	 * regardless of what actual time zone the server is in.
	 * 
	 * @return The server time zone
	 */
	protected ZoneId getTimeZone()
	{
		return ZoneId.systemDefault();
	}
}
