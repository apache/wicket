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
package wicket.util.time;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * An immutable Time class that represents a specific point in time. The
 * underlying representation is a long value which holds a number of
 * milliseconds since January 1, 1970, 0:00 GMT. To represent a time duration,
 * such as "6 seconds", use the Duration class. To represent a time period with
 * a start and end time, use the TimeFrame class. To represent a time of day,
 * use the TimeOfDay class.
 * 
 * @author Jonathan Locke
 */
public final class Time extends AbstractTime
{
	private static final long serialVersionUID = 1L;

	/** The beginning of UNIX time: January 1, 1970, 0:00 GMT. */
	public static final Time START_OF_UNIX_TIME = milliseconds(0);

	/** Parser in 'yyyy.MM.dd' format. */
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");

	/** Parser in 'yyyy.MM.dd-h.mma' format. */
	private static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy.MM.dd-h.mma");

	/**
	 * Gets a Time instance based on the given miliseconds.
	 * 
	 * @param time
	 *            The time in milliseconds since START_OF_UNIX_TIME
	 * @return The given time
	 */
	public static Time milliseconds(final long time)
	{
		return new Time(time);
	}

	/**
	 * Gets a Time instance based on the current time.
	 * 
	 * @return The current time
	 */
	public static Time now()
	{
		return new Time(System.currentTimeMillis());
	}

	/**
	 * Gets time by parsing 'yyyy.MM.dd' format.
	 * 
	 * @param calendar
	 *            The calendar to use when parsing date string
	 * @param string
	 *            The string
	 * @return The time
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
	 * Gets time by parsing 'yyyy.MM.dd' format using a localtime calendar.
	 * 
	 * @param string
	 *            The string
	 * @return The time
	 * @throws ParseException
	 */
	public static Time parseDate(final String string) throws ParseException
	{
		return parseDate(localtime, string);
	}

	/**
	 * Gets time by parsing yyyy.MM.dd-h.mma format.
	 * 
	 * @param calendar
	 *            The calendar to use when parsing the string
	 * @param string
	 *            The string
	 * @return An immutable UNIX time value
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
	 * Gets a Time instance based on the given calendar and {@link TimeOfDay}
	 * objects.
	 * 
	 * @param calendar
	 *            The calendar to use
	 * @param timeOfDay
	 *            The time of day
	 * @return A time value for the time of day today
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

			// Add time of day milliseconds to midnight
			return valueOf(calendar.getTimeInMillis() + timeOfDay.getMilliseconds());
		}
	}

	/**
	 * Gets a Time instance based on the given date object.
	 * 
	 * @param date
	 *            A java.util.Date object
	 * @return A corresponding immutable Time object
	 */
	public static Time valueOf(final Date date)
	{
		return new Time(date.getTime());
	}

	/**
	 * Gets a Time instance based on the given miliseconds.
	 * 
	 * @param time
	 *            The time in milliseconds since START_OF_UNIX_TIME
	 * @return A corresponding immutable Time object
	 */
	public static Time valueOf(final long time)
	{
		return new Time(time);
	}

	/**
	 * Gets time by parsing yyyy.MM.dd-h.mma format.
	 * 
	 * @param string
	 *            The string
	 * @return The time
	 * @throws ParseException
	 */
	public static Time valueOf(final String string) throws ParseException
	{
		return valueOf(localtime, string);
	}

	/**
	 * Gets time by parsing 'pattern' format.
	 * 
	 * @param string
	 *            input
	 * @param pattern
	 *            pattern to use
	 * @return Time instance that resulted from parsing the given string
	 * @throws ParseException
	 */
	public static Time valueOf(final String string, final String pattern) throws ParseException
	{
		final SimpleDateFormat dateTimeFormat = new SimpleDateFormat(pattern);
		dateTimeFormat.setCalendar(localtime);
		return valueOf(dateTimeFormat.parse(string));
	}

	/**
	 * Gets a Time instance based on the given {@link TimeOfDay}object.
	 * 
	 * @param timeOfDay
	 *            The time of day in localtime
	 * @return A time value for the time of day today
	 */
	public static Time valueOf(final TimeOfDay timeOfDay)
	{
		return valueOf(localtime, timeOfDay);
	}

	/**
	 * Private constructor forces use of static factory methods.
	 * 
	 * @param time
	 *            The time in milliseconds since START_OF_UNIX_TIME
	 */
	private Time(final long time)
	{
		super(time);
	}

	/**
	 * Adds the given duration to this time object, moving the time into the
	 * future.
	 * 
	 * @param duration
	 *            The duration to add
	 * @return This time + duration
	 */
	public Time add(final Duration duration)
	{
		return milliseconds(getMilliseconds() + duration.getMilliseconds());
	}

	/**
	 * @return Amount of time that has elapsed since this time
	 * @throws IllegalStateException Thrown if this time is in the future
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
	 * Gets the duration from now to this time value. If this time value is in
	 * the past, then the Duration returned will be negative. Otherwise, it will
	 * be the number of milliseconds from now to this Time.
	 * 
	 * @return The duration from now to this time value
	 */
	public Duration fromNow()
	{
		return subtract(now());
	}

	/**
	 * Gets the value of a field from the given calendar.
	 * 
	 * @param calendar
	 *            The calendar to use
	 * @param field
	 *            The calendar field to get
	 * @return The field's value for this point in time on the given calendar
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
	 * Gets the value of a field.
	 * 
	 * @param field
	 *            The calendar field to get
	 * @return The field's value (in localtime)
	 */
	public int get(final int field)
	{
		return get(localtime, field);
	}

	/**
	 * Gets the day of month field of the current calendar.
	 * 
	 * @return the field value
	 */
	public int getDayOfMonth()
	{
		return getDayOfMonth(localtime);
	}

	/**
	 * Gets the day of month field of the given calendar.
	 * 
	 * @param calendar
	 *            the calendar to get the field value from
	 * @return the field value
	 */
	public int getDayOfMonth(final Calendar calendar)
	{
		return get(calendar, Calendar.DAY_OF_MONTH);
	}

	/**
	 * Gets the hour field of the current calendar.
	 * 
	 * @return the field value
	 */
	public int getHour()
	{
		return getHour(localtime);
	}

	/**
	 * Gets the hour field of the given calendar.
	 * 
	 * @param calendar
	 *            the calendar to get the field value from
	 * @return the field value
	 */
	public int getHour(final Calendar calendar)
	{
		return get(calendar, Calendar.HOUR);
	}

	/**
	 * Gets the minute field of the current calendar.
	 * 
	 * @return the field value
	 */
	public int getMinute()
	{
		return getMinute(localtime);
	}

	/**
	 * Gets the minute field of the given calendar.
	 * 
	 * @param calendar
	 *            the calendar to get the field value from
	 * @return the field value
	 */
	public int getMinute(final Calendar calendar)
	{
		return get(calendar, Calendar.MINUTE);
	}

	/**
	 * Gets the month field of the current calendar.
	 * 
	 * @return the field value
	 */
	public int getMonth()
	{
		return getMonth(localtime);
	}

	/**
	 * Gets the month field of the given calendar.
	 * 
	 * @param calendar
	 *            the calendar to get the field value from
	 * @return the field value
	 */
	public int getMonth(final Calendar calendar)
	{
		return get(calendar, Calendar.MONTH);
	}

	/**
	 * Gets the second field of the current calendar.
	 * 
	 * @return the field value
	 */
	public int getSecond()
	{
		return getSecond(localtime);
	}

	/**
	 * Gets the second field of the given calendar.
	 * 
	 * @param calendar
	 *            the calendar to get the field value from
	 * @return the field value
	 */
	public int getSecond(final Calendar calendar)
	{
		return get(calendar, Calendar.SECOND);
	}

	/**
	 * Gets the year field of the current calendar.
	 * 
	 * @return the field value
	 */
	public int getYear()
	{
		return getYear(localtime);
	}

	/**
	 * Gets the year field of the given calendar.
	 * 
	 * @param calendar
	 *            the calendar to get the field value from
	 * @return the field value
	 */
	public int getYear(final Calendar calendar)
	{
		return get(calendar, Calendar.YEAR);
	}

	/**
	 * Adds the given duration to this time object, moving the time into the
	 * future.
	 * 
	 * @param duration
	 *            The duration to add
	 * @return This time - duration
	 */
	public Time subtract(final Duration duration)
	{
		return milliseconds(getMilliseconds() - duration.getMilliseconds());
	}

	/**
	 * Subtract time from this and returns the difference as a duration object.
	 * 
	 * @param that
	 *            The time to subtract
	 * @return The duration between this and that time
	 */
	public Duration subtract(final Time that)
	{
		return Duration.milliseconds(this.getMilliseconds() - that.getMilliseconds());
	}

	/**
	 * Gets a Date object for this time object. A new Date object is always
	 * returned rather than attempting to cache a date since Date is mutable.
	 * 
	 * @return This immutable time object as a mutable java.util.Date object
	 */
	public Date toDate()
	{
		return new Date(getMilliseconds());
	}

	/**
	 * Converts this time to a date string using the date formatter
	 * 'yyyy.MM.dd'.
	 * 
	 * @return The date string
	 */
	public String toDateString()
	{
		return toDateString(localtime);
	}

	/**
	 * Converts this time to a date string using the formatter 'yyyy.MM.dd'.
	 * 
	 * @param calendar
	 *            The calendar to use in the conversion
	 * @return The date string
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
	 * Converts this time to a string suitable for use in a filesystem name.
	 * 
	 * @return This time as a formatted string
	 */
	public String toString()
	{
		return toDateString() + "-" + toTimeString();
	}

	/**
	 * Converts this time to a string using the given calendar and format.
	 * 
	 * @param calendar
	 *            the calendar to use
	 * @param format
	 *            the format to use
	 * @return This time as a formatted string
	 */
	public String toString(final Calendar calendar, final String format)
	{
		final SimpleDateFormat dateTimeFormat = new SimpleDateFormat(format);
		dateTimeFormat.setCalendar(calendar == null ? localtime : calendar);
		return dateTimeFormat.format(new Date(getMilliseconds()));
	}

	/**
	 * Converts this time to a string using the given format.
	 * 
	 * @param format
	 *            the format to use
	 * @return This time as a formatted string
	 */
	public String toString(final String format)
	{
		return toString(null, format);
	}
}
