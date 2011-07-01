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
package org.apache.wicket.util.date;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.apache.wicket.util.time.Time;

/**
 * print date in RFC1123 format
 * <p/>
 * Contrary to {@link java.text.SimpleDateFormat} this is thread-safe.
 * <p/> 
 * taken from the source code of jetty 7.3.0, thanks for Greg Wilkins!
 * 
 * @author Peter Ertl
 * 
 */
public final class RFC1123DateFormatter
{
	private static final String[] DAYS =
		{"Sat", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
	private static final String[] MONTHS =
		{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec", "Jan"};

	public static final TimeZone GMT = TimeZone.getTimeZone("GMT");

	private RFC1123DateFormatter()
	{
	}

	public static String formatDate(Time time)
	{
		final Calendar cal = GregorianCalendar.getInstance(GMT);
		final StringBuilder buf = new StringBuilder(32);

		cal.setTimeInMillis(time.getMilliseconds());

		int day_of_week = cal.get(Calendar.DAY_OF_WEEK);
		int day_of_month = cal.get(Calendar.DAY_OF_MONTH);
		int month = cal.get(Calendar.MONTH);
		int year = cal.get(Calendar.YEAR);
		int century = year / 100;
		year = year % 100;

		int hours = cal.get(Calendar.HOUR_OF_DAY);
		int minutes = cal.get(Calendar.MINUTE);
		int seconds = cal.get(Calendar.SECOND);

		buf.append(DAYS[day_of_week]);
		buf.append(',');
		buf.append(' ');
		appendTwoDigits(buf, day_of_month);

		buf.append(' ');
		buf.append(MONTHS[month]);
		buf.append(' ');
		appendTwoDigits(buf, century);
		appendTwoDigits(buf, year);

		buf.append(' ');
		appendTwoDigits(buf, hours);
		buf.append(':');
		appendTwoDigits(buf, minutes);
		buf.append(':');
		appendTwoDigits(buf, seconds);
		buf.append(" GMT");

		return buf.toString();
	}

	private static void appendTwoDigits(StringBuilder str, int number)
	{
		str.append((char)(number / 10 + '0'));
		str.append((char)(number % 10 + '0'));
	}
}
