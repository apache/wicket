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
package wicket.datetime.util;

import java.util.Date;
import java.util.TimeZone;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.MutableDateTime;
import org.joda.time.format.DateTimeFormatter;

import wicket.Session;
import wicket.protocol.http.request.WebClientInfo;
import wicket.request.ClientInfo;
import wicket.util.convert.SimpleConverterAdapter;

/**
 * Base class for Joda Time based date converters. It contains the logic to
 * parse and format, optionally taking the time zone difference between clients
 * and the server into account.
 * <p>
 * Converters of this class are best suited for per-component use.
 * </p>
 * 
 * @author eelcohillenius
 */
public abstract class DateConverter extends SimpleConverterAdapter {

	/**
	 * Whether to apply the time zone difference when interpreting dates.
	 */
	private final boolean applyTimeZoneDifference;

	/**
	 * Construct.
	 * </p>
	 * When applyTimeZoneDifference is true, the current time is applied on the
	 * parsed date, and the date will be corrected for the time zone difference
	 * between the server and the client. For instance, if I'm in Seattle and
	 * the server I'm working on is in Amsterdam, the server is 9 hours ahead.
	 * So, if I'm inputting say 12/24 at a couple of hours before midnight, at
	 * the server it is already 12/25. If this boolean is true, it will be
	 * transformed to 12/25, while the client sees 12/24.
	 * </p>
	 * 
	 * @param applyTimeZoneDifference
	 *            whether to apply the difference in time zones between client
	 *            and server
	 */
	public DateConverter(boolean applyTimeZoneDifference) {
		this.applyTimeZoneDifference = applyTimeZoneDifference;
	}

	/**
	 * Gets whether to apply the time zone difference when interpreting dates.
	 * 
	 * </p>
	 * When true, the current time is applied on the parsed date, and the date
	 * will be corrected for the time zone difference between the server and the
	 * client. For instance, if I'm in Seattle and the server I'm working on is
	 * in Amsterdam, the server is 9 hours ahead. So, if I'm inputting say 12/24
	 * at a couple of hours before midnight, at the server it is already 12/25.
	 * If this boolean is true, it will be transformed to 12/25, while the
	 * client sees 12/24.
	 * </p>
	 * 
	 * @return whether to apply the difference in time zones between client and
	 *         server
	 */
	public final boolean getApplyTimeZoneDifference() {
		return applyTimeZoneDifference;
	}

	/**
	 * @return Gets the pattern that is used for printing and parsing
	 */
	public abstract String getDatePattern();

	/**
	 * @see wicket.util.convert.SimpleConverterAdapter#toObject(java.lang.String)
	 */
	public final Object toObject(String value) {

		DateTimeFormatter format = getFormat();

		if (applyTimeZoneDifference) {
			TimeZone zone = getClientTimeZone();
			// instantiate now/ current time
			MutableDateTime dt = new MutableDateTime();
			if (zone != null) {
				// set time zone for client
				format = format.withZone(DateTimeZone.forTimeZone(zone));
				dt.setZone(DateTimeZone.forTimeZone(zone));
			}
			// parse date retaining the time of the submission
			format.parseInto(dt, value, 0);
			// apply the server time zone to the parsed value
			dt.setZone(DateTimeZone.forTimeZone(TimeZone.getDefault()));
			return dt.toDate();
		} else {
			return format.parseDateTime(value).toDate();
		}
	}

	/**
	 * @see wicket.util.convert.SimpleConverterAdapter#toString(java.lang.Object)
	 */
	public final String toString(Object value) {

		DateTime dt = new DateTime(((Date) value).getTime());
		DateTimeFormatter format = getFormat();

		if (applyTimeZoneDifference) {
			TimeZone zone = getClientTimeZone();
			if (zone != null) {
				// apply time zone to formatter
				format = format.withZone(DateTimeZone.forTimeZone(zone));
			}
		}
		return format.print(dt);
	}

	/**
	 * Gets the client's time zone.
	 * 
	 * @return The client's time zone or null
	 */
	protected TimeZone getClientTimeZone() {
		ClientInfo info = Session.get().getClientInfo();
		if (info instanceof WebClientInfo) {
			return ((WebClientInfo) info).getProperties().getTimeZone();
		}
		return null;
	}

	/**
	 * @return formatter The formatter for the current conversion
	 */
	protected abstract DateTimeFormatter getFormat();
}
