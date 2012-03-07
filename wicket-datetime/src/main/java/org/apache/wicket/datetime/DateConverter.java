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

import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.request.WebClientInfo;
import org.apache.wicket.core.request.ClientInfo;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.string.Strings;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;


/**
 * Base class for Joda Time based date converters. It contains the logic to parse and format,
 * optionally taking the time zone difference between clients and the server into account.
 * <p>
 * Converters of this class are best suited for per-component use.
 * </p>
 * 
 * @author eelcohillenius
 */
public abstract class DateConverter implements IConverter<Date>
{
	private static final long serialVersionUID = 1L;

	/**
	 * Whether to apply the time zone difference when interpreting dates.
	 */
	private final boolean applyTimeZoneDifference;

	/**
	 * Optional component to use for determining the locale.
	 */
	private Component component = null;

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

	/**
	 * @see org.apache.wicket.util.convert.IConverter#convertToObject(java.lang.String,
	 *      java.util.Locale)
	 */
	public Date convertToObject(String value, Locale locale)
	{
		if (Strings.isEmpty(value))
		{
			return null;
		}

		DateTimeFormatter format = getFormat(locale);
		if (format == null)
		{
			throw new IllegalStateException("format must be not null");
		}

		if (applyTimeZoneDifference)
		{
			TimeZone zone = getClientTimeZone();
			DateTime dateTime = null;

			// set time zone for client
			format = format.withZone(getTimeZone());

			try
			{
				// parse date retaining the time of the submission
				dateTime = format.parseDateTime(value);
			}
			catch (RuntimeException e)
			{
				throw new ConversionException(e);
			}
			// apply the server time zone to the parsed value
			if (zone != null)
			{
				dateTime = dateTime.withZoneRetainFields(DateTimeZone.forTimeZone(zone));
			}

			return dateTime.toDate();
		}
		else
		{
			try
			{
				DateTime date = format.parseDateTime(value);
				return date.toDate();
			}
			catch (RuntimeException e)
			{
				throw new ConversionException(e);
			}
		}
	}

	/**
	 * @see org.apache.wicket.util.convert.IConverter#convertToString(java.lang.Object,
	 *      java.util.Locale)
	 */
	public String convertToString(Date value, Locale locale)
	{
		DateTime dt = new DateTime((value).getTime(), getTimeZone());
		DateTimeFormatter format = getFormat(locale);

		if (applyTimeZoneDifference)
		{
			TimeZone zone = getClientTimeZone();
			if (zone != null)
			{
				// apply time zone to formatter
				format = format.withZone(DateTimeZone.forTimeZone(zone));
			}
		}
		return format.print(dt);
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
	 * @return optional component to use for determining the locale.
	 */
	public final Component getComponent()
	{
		return component;
	}

	/**
	 * @param locale
	 * @return Gets the pattern that is used for printing and parsing
	 */
	public abstract String getDatePattern(Locale locale);

	/**
	 * Sets component for getting the locale
	 * 
	 * @param component
	 *            optional component to use for determining the locale.
	 */
	public final void setComponent(Component component)
	{
		this.component = component;
	}

	/**
	 * Gets the client's time zone.
	 * 
	 * @return The client's time zone or null
	 */
	protected TimeZone getClientTimeZone()
	{
		ClientInfo info = Session.get().getClientInfo();
		if (info instanceof WebClientInfo)
		{
			return ((WebClientInfo)info).getProperties().getTimeZone();
		}
		return null;
	}

	/**
	 * @param locale
	 * 
	 * @return formatter The formatter for the current conversion
	 */
	protected abstract DateTimeFormatter getFormat(Locale locale);

	/**
	 * Gets the locale to use.
	 * 
	 * @return the locale from either the component if that is set, or from the session
	 */
	protected Locale getLocale()
	{
		Component c = getComponent();
		return (c != null) ? c.getLocale() : Session.get().getLocale();
	}

	/**
	 * Gets the server time zone. Override this method if you want to fix to a certain time zone,
	 * regardless of what actual time zone the server is in.
	 * 
	 * @return The server time zone
	 */
	protected DateTimeZone getTimeZone()
	{
		return DateTimeZone.getDefault();
	}
}
