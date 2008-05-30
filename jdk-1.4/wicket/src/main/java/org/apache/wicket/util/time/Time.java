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
package org.apache.wicket.util.time;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * An immutable <code>Time</code> class that represents a specific point in time. The underlying
 * representation is a <code>long</code> value which holds a number of milliseconds since January
 * 1, 1970, 0:00 GMT. To represent a duration of time, such as "6 seconds", use the
 * <code>Duration</code> class. To represent a time period with a start and end time, use the
 * <code>TimeFrame</code> class. To represent a time of day, use the <code>TimeOfDay</code>
 * class.
 * 
 * @author Jonathan Locke
 * @since 1.2.6
 */
public final class Time extends AbstractTime
{
	private static final long serialVersionUID = 1L;

	/** the beginning of UNIX time: January 1, 1970, 0:00 GMT. */
	public static final Time START_OF_UNIX_TIME = milliseconds(0);

	/** parser in 'yyyy.MM.dd' format. */
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");

	/** parser in 'yyyy.MM.dd-h.mma' format. */
	private static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy.MM.dd-h.mma");

	/**
	 * Retrieves a <code>Time</code> instance based on the given milliseconds.
	 * 
	 * @param time
	 *            the time value in milliseconds since START_OF_UNIX_TIME
	 * @return the given <code>Time</code>
	 */
	public static Time milliseconds(final long time)
	{
		return new Time(time);
	}

	/**
	 * Retrieves a <code>Time</code> instance based on the current time.
	 * 
	 * @return the current <code>Time</code>
	 */
	public static Time now()
	{
		return new Time(System.currentTimeMillis());
	}

	/**
	 * Retrieves a <code>Time</code> instance by parsing 'yyyy.MM.dd' format.
	 * 
	 * @param calendar
	 *            the <code>Calendar</code> to use when parsing date <code>String</code>
	 * @param string
	 *            the <code>String</code> to parse
	 * @return the time
	 * @throws ParseException
	 */
	public static Time parseDate(final Calendar calendar, final String string)
		throws ParseException
	{
		synchronized (dateFormat)
		{
			synchronized (calendar)
			{
				dateFormat.setCalendar(calendar);

				return valueOf(dateFormat.parse(string));
			}
		}
	}

	/**
	 * Retrieves a <code>Time</code> instance by parsing 'yyyy.MM.dd' format using a local time
	 * <code>Calendar</code>.
	 * 
	 * @param string
	 *            the <code>String</code> to parse
	 * @return the time
	 * @throws ParseException
	 */
	public static Time parseDate(final String string) throws ParseException
	{
		return parseDate(localtime, string);
	}

	/**
	 * Retrieves a <code>Time</code> instance by parsing 'yyyy.MM.dd-h.mma' format.
	 * 
	 * @param calendar
	 *            the <code>Calendar</code> to use when parsing the <code>String</code>
	 * @param string
	 *            the <code>String</code> to parse
	 * @return an immutable UNIX <code>Time</code> value
	 * @throws ParseException
	 */
	public static Time valueOf(final Calendar calendar, final String string) throws ParseException
	{
		synchronized (dateTimeFormat)
		{
			synchronized (calendar)
			{
				dateTimeFormat.setCalendar(calendar);

				return valueOf(dateTimeFormat.parse(string));
			}
		}
	}

	/**
	 * Retrieves a <code>Time</code> instance based on the given <code>Calendar</code> and
	 * {@link TimeOfDay} objects.
	 * 
	 * @param calendar
	 *            the <code>Calendar</code> to use
	 * @param timeOfDay
	 *            the time of day
	 * @return a <code>Time</code> value for the time of day today
	 */
	public static Time valueOf(final Calendar calendar, final TimeOfDay timeOfDay)
	{
		synchronized (calendar)
		{
			// Set time to midnight today
			calendar.setTimeInMillis(System.currentTimeMillis());
			calendar.set(Calendar.HOUR, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0); // WICKET-1670

			// Add time of day milliseconds to midnight
			return valueOf(calendar.getTimeInMillis() + timeOfDay.getMilliseconds());
		}
	}

	/**
	 * Retrieves a <code>Time</code> instance based on the given <code>Date</code> object.
	 * 
	 * @param date
	 *            a <code>java.util.Date</code> object
	 * @return a corresponding immutable <code>Time</code> object
	 */
	public static Time valueOf(final Date date)
	{
		return new Time(date.getTime());
	}

	/**
	 * Retrieves a <code>Time</code> instance based on the given milliseconds.
	 * 
	 * @param time
	 *            the <code>Time</code> value in milliseconds since START_OF_UNIX_TIME
	 * @return a corresponding immutable <code>Time</code> object
	 */
	public static Time valueOf(final long time)
	{
		return new Time(time);
	}

	/**
	 * Retrieves a <code>Time</code> instance by parsing 'yyyy.MM.dd-h.mma' format.
	 * 
	 * @param string
	 *            the <code>String</code> to parse
	 * @return the <code>Time</code> instance
	 * @throws ParseException
	 */
	public static Time valueOf(final String string) throws ParseException
	{
		return valueOf(localtime, string);
	}

	/**
	 * Retrieves a <code>Time</code> instance by parsing 'pattern' format.
	 * 
	 * @param string
	 *            input
	 * @param pattern
	 *            the pattern to parse
	 * @return a <code>Time</code> instance that resulted from parsing the given
	 *         <code>String</code>
	 * @throws ParseException
	 */
	public static Time valueOf(final String string, final String pattern) throws ParseException
	{
		final SimpleDateFormat dateTimeFormat = new SimpleDateFormat(pattern);
		dateTimeFormat.setCalendar(localtime);
		return valueOf(dateTimeFormat.parse(string));
	}

	/**
	 * Retrieves a <code>Time</code> instance based on the given {@link TimeOfDay} object.
	 * 
	 * @param timeOfDay
	 *            the time of day in local time
	 * @return a <code>Time</code> value for the time of day today
	 */
	public static Time valueOf(final TimeOfDay timeOfDay)
	{
		return valueOf(localtime, timeOfDay);
	}

	/**
	 * Private constructor forces use of static factory methods.
	 * 
	 * @param time
	 *            the <code>Time</code> value in milliseconds since START_OF_UNIX_TIME
	 */
	private Time(final long time)
	{
		super(time);
	}

	/**
	 * Adds the given <code>Duration</code> to this <code>Time</code> object, moving the time
	 * into the future.
	 * 
	 * @param duration
	 *            the <code>Duration</code> to add
	 * @return this <code>Time</code> + <code>Duration</code>
	 */
	public Time add(final Duration duration)
	{
		return milliseconds(getMilliseconds() + duration.getMilliseconds());
	}

	/**
	 * Calculates the amount of time that has elapsed since this <code>Time</code> value.
	 * 
	 * @return the amount of time that has elapsed since this <code>Time</code> value
	 * @throws IllegalStateException
	 *             thrown if this <code>Time</code> value is in the future
	 */
	public Duration elapsedSince()
	{
		final Time now = now();
		if (this.greaterThan(now))
		{
			throw new IllegalStateException("This time is in the future");
		}
		return now.subtract(this);
	}

	/**
	 * Retrieves the <code>Duration</code> from now to this <code>Time</code> value. If this
	 * <code>Time</code> value is in the past, then the <code>Duration</code> returned will be
	 * negative. Otherwise, it will be the number of milliseconds from now to this <code>Time</code>.
	 * 
	 * @return the <code>Duration</code> from now to this <code>Time</code> value
	 */
	public Duration fromNow()
	{
		return subtract(now());
	}

	/**
	 * Retrieves the value of a field from the given <code>Calendar</code>.
	 * 
	 * @param calendar
	 *            the <code>Calendar</code> to use
	 * @param field
	 *            the <code>Calendar</code> field to get
	 * @return the field's value for this point in time on the given <code>Calendar</code>
	 */
	public int get(final Calendar calendar, final int field)
	{
		synchronized (calendar)
		{
			calendar.setTimeInMillis(getMilliseconds());

			return calendar.get(field);
		}
	}

	/**
	 * Retrieves the value of a field.
	 * 
	 * @param field
	 *            the <code>Calendar</code> field to get
	 * @return the field's value (in local time)
	 */
	public int get(final int field)
	{
		return get(localtime, field);
	}

	/**
	 * Retrieves the day of month field of the current <code>Calendar</code>.
	 * 
	 * @return the day of month field value
	 */
	public int getDayOfMonth()
	{
		return getDayOfMonth(localtime);
	}

	/**
	 * Retrieves the day of month field of the given <code>Calendar</code>.
	 * 
	 * @param calendar
	 *            the <code>Calendar</code> to get the field value from
	 * @return the day of month field value
	 */
	public int getDayOfMonth(final Calendar calendar)
	{
		return get(calendar, Calendar.DAY_OF_MONTH);
	}

	/**
	 * Retrieves the hour field of the current <code>Calendar</code>.
	 * 
	 * @return the hour field value
	 */
	public int getHour()
	{
		return getHour(localtime);
	}

	/**
	 * Retrieves the hour field of the given <code>Calendar</code>.
	 * 
	 * @param calendar
	 *            the <code>Calendar</code> to get the field value from
	 * @return the hour field value
	 */
	public int getHour(final Calendar calendar)
	{
		return get(calendar, Calendar.HOUR);
	}

	/**
	 * Retrieves the minute field of the current <code>Calendar</code>.
	 * 
	 * @return the minute field value
	 */
	public int getMinute()
	{
		return getMinute(localtime);
	}

	/**
	 * Retrieves the minute field of the given <code>Calendar</code>.
	 * 
	 * @param calendar
	 *            the <code>Calendar</code> from which to get the field value
	 * @return the minute field value
	 */
	public int getMinute(final Calendar calendar)
	{
		return get(calendar, Calendar.MINUTE);
	}

	/**
	 * Retrieves the month field of the current <code>Calendar</code>.
	 * 
	 * @return the month field value
	 */
	public int getMonth()
	{
		return getMonth(localtime);
	}

	/**
	 * Retrieves the month field of the given <code>Calendar</code>.
	 * 
	 * @param calendar
	 *            the <code>Calendar</code> from which to get the field value
	 * @return the month field value
	 */
	public int getMonth(final Calendar calendar)
	{
		return get(calendar, Calendar.MONTH);
	}

	/**
	 * Retrieves the seconds field of the current <code>Calendar</code>.
	 * 
	 * @return the seconds field value
	 */
	public int getSecond()
	{
		return getSecond(localtime);
	}

	/**
	 * Retrieves the seconds field of the given <code>Calendar</code>.
	 * 
	 * @param calendar
	 *            the <code>Calendar</code> from which to get the field value
	 * @return the seconds field value
	 */
	public int getSecond(final Calendar calendar)
	{
		return get(calendar, Calendar.SECOND);
	}

	/**
	 * Retrieves the year field of the current <code>Calendar</code>.
	 * 
	 * @return the year field value
	 */
	public int getYear()
	{
		return getYear(localtime);
	}

	/**
	 * Retrieves the year field of the given <code>Calendar</code>.
	 * 
	 * @param calendar
	 *            the <code>Calendar</code> from which to get the field value
	 * @return the year field value
	 */
	public int getYear(final Calendar calendar)
	{
		return get(calendar, Calendar.YEAR);
	}

	/**
	 * Subtracts the given <code>Duration</code> from this <code>Time</code> object, moving the
	 * time into the past.
	 * 
	 * @param duration
	 *            the <code>Duration</code> to subtract
	 * @return this duration of time
	 */
	public Time subtract(final Duration duration)
	{
		return milliseconds(getMilliseconds() - duration.getMilliseconds());
	}

	/**
	 * Subtract time from this and returns the difference as a <code>Duration</code> object.
	 * 
	 * @param that
	 *            the time to subtract
	 * @return the <code>Duration</code> between this and that time
	 */
	public Duration subtract(final Time that)
	{
		return Duration.milliseconds(getMilliseconds() - that.getMilliseconds());
	}

	/**
	 * Retrieves a <code>Date</code> object for this <code>Time</code> object. A new
	 * <code>Date</code> object is always returned rather than attempting to cache a date since
	 * <code>Date</code> is mutable.
	 * 
	 * @return this immutable <code>Time</code> object as a mutable <code>java.util.Date</code>
	 *         object
	 */
	public Date toDate()
	{
		return new Date(getMilliseconds());
	}

	/**
	 * Converts this <code>Time</code> value to a date <code>String</code> using the date
	 * formatter 'yyyy.MM.dd'.
	 * 
	 * @return the date string
	 */
	public String toDateString()
	{
		return toDateString(localtime);
	}

	/**
	 * Converts this <code>Time</code> value to a date <code>String</code> using the formatter
	 * 'yyyy.MM.dd'.
	 * 
	 * @param calendar
	 *            the <code>Calendar</code> to use in the conversion
	 * @return the date <code>String</code>
	 */
	public String toDateString(final Calendar calendar)
	{
		synchronized (dateFormat)
		{
			synchronized (calendar)
			{
				dateFormat.setCalendar(calendar);

				return dateFormat.format(new Date(getMilliseconds())).toLowerCase();
			}
		}
	}

	/**
	 * Converts this <code>Time</code> value to a <code>String</code> suitable for use in a file
	 * system name.
	 * 
	 * @return this <code>Time</code> value as a formatted <code>String</code>
	 */
	public String toString()
	{
		return toDateString() + "-" + toTimeString();
	}

	/**
	 * Converts this <code>Time</code> object to a <code>String</code> using the given
	 * <code>Calendar</code> and format.
	 * 
	 * @param calendar
	 *            the <code>Calendar</code> to use in the conversion
	 * @param format
	 *            the format to use
	 * @return this <code>Time</code> value as a formatted <code>String</code>
	 */
	public String toString(final Calendar calendar, final String format)
	{
		final SimpleDateFormat dateTimeFormat = new SimpleDateFormat(format);
		dateTimeFormat.setCalendar(calendar == null ? localtime : calendar);
		return dateTimeFormat.format(new Date(getMilliseconds()));
	}

	/**
	 * Converts this <code>Time</code> value to a <code>String</code> using the given format.
	 * 
	 * @param format
	 *            the format to use
	 * @return this <code>Time</code> value as a formatted string
	 */
	public String toString(final String format)
	{
		return toString(null, format);
	}
}
