/*
 * $Id$ $Revision:
 * 1.4 $ $Date$
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.util.time;

import java.text.ParseException;
import java.util.Calendar;

import wicket.util.lang.EnumeratedType;

/**
 * An immutable time of day value represented as milliseconds since the most
 * recent midnight.
 * <p>
 * Values can be constructed using various factory methods:
 * <ul>
 * <li>valueOf(long) where long is milliseconds since midnight
 * <li>valueOf(String) where the string is in h.mma format
 * <li>valueOf(Calendar, String) where the string is in h.mma format
 * <li>valueOf(Duration) where duration is time since midnight
 * <li>valueOf(Time) where time is some point in time today
 * <li>valueOf(Calendar, Time) where time is some point in time today
 * <li>militaryTime(int hour, int minute, int second) for 24 hour time
 * <li>time(int hour, int minute, Meridian) where Meridian is AM or PM
 * <li>time(int hour, int minute, int second, Meridian) where Meridian is AM or
 * PM
 * <li>now() to construct the current time of day
 * <li>now(Calendar) to construct the current time of day using a given
 * calendar
 * </ul>
 * <p>
 * If an attempt is made to construct an illegal time of day value (one that is
 * greater than 24 hours worth of milliseconds), an IllegalArgumentException
 * will be thrown.
 * <p>
 * Military hours, minutes and seconds of the time of day can be retrieved by
 * calling hour(), minute() and second().
 * <p>
 * The next occurrence of a given time of day can be retrieved by calling next()
 * or next(Calendar).
 * 
 * @author Jonathan Locke
 */
public final class TimeOfDay extends AbstractTime
{
	private static final long serialVersionUID = 1L;

	/** Constant for AM time. */
	public static final Meridian AM = new Meridian("AM");

	/** Constant for midnight. */
	public static final TimeOfDay MIDNIGHT = time(12, 0, AM);

	/** Constant for PM time. */
	public static final Meridian PM = new Meridian("PM");

	/** Constant for noon. */
	public static final TimeOfDay NOON = time(12, 0, PM);
	
	/** Typesafe AM/PM enumeration. */
	public static final class Meridian extends EnumeratedType
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 * 
		 * @param name
		 *            the meridian name (value)
		 */
		Meridian(final String name)
		{
			super(name);
		}
	}

	/**
	 * Gets a time of day value on a 24 hour clock.
	 * 
	 * @param hour
	 *            The hour (0-23)
	 * @param minute
	 *            The minute (0-59)
	 * @param second
	 *            The second (0-59)
	 * @return The time of day
	 */
	public static TimeOfDay militaryTime(final int hour, final int minute, final int second)
	{
		if ((hour > 23) || (hour < 0))
		{
			throw new IllegalArgumentException("Hour " + hour + " is not valid");
		}

		if ((minute > 59) || (minute < 0))
		{
			throw new IllegalArgumentException("Minute " + minute + " is not valid");
		}

		if ((second > 59) || (second < 0))
		{
			throw new IllegalArgumentException("Second " + second + " is not valid");
		}

		return valueOf(Duration.hours(hour).add(Duration.minutes(minute)).add(
				Duration.seconds(second)));
	}

	/**
	 * Gets the time of day it is now.
	 * 
	 * @return The time of day it is now
	 */
	public static TimeOfDay now()
	{
		return valueOf(Time.now());
	}

	/**
	 * Gets the time of day it is now on the given calendar.
	 * 
	 * @param calendar
	 *            The calendar to use
	 * @return The time of day it is now on the given calendar
	 */
	public static TimeOfDay now(final Calendar calendar)
	{
		return valueOf(calendar, Time.now());
	}

	/**
	 * Gets a time of day on a 12 hour clock.
	 * 
	 * @param hour
	 *            The hour (1-12)
	 * @param minute
	 *            The minute (0-59)
	 * @param second
	 *            The second (0-59)
	 * @param meridian
	 *            AM/PM
	 * @return The time value
	 */
	public static TimeOfDay time(final int hour, final int minute, final int second,
			final Meridian meridian)
	{
		if (meridian == PM)
		{
			if (hour == 12)
			{
				return militaryTime(12, minute, second);
			}
			else
			{
				return militaryTime(hour + 12, minute, second);
			}
		}
		else
		{
			if (hour == 12)
			{
				return militaryTime(0, minute, second);
			}
			else
			{
				return militaryTime(hour, minute, second);
			}
		}
	}

	/**
	 * Gets a time of day on a 12 hour clock.
	 * 
	 * @param hour
	 *            The hour (1-12)
	 * @param minute
	 *            The minute (0-59)
	 * @param meridian
	 *            AM/PM
	 * @return The time value
	 */
	public static TimeOfDay time(final int hour, final int minute, final Meridian meridian)
	{
		return time(hour, minute, 0, meridian);
	}

	/**
	 * Converts to TimeOfDay instance.
	 * 
	 * @param calendar
	 *            The calendar to use when parsing time string
	 * @param time
	 *            A string in h.mma format
	 * @return The time of day on the given calendar
	 * @throws ParseException
	 */
	public static TimeOfDay valueOf(final Calendar calendar, final String time)
			throws ParseException
	{
		synchronized (timeFormat)
		{
			synchronized (calendar)
			{
				timeFormat.setCalendar(calendar);
				return new TimeOfDay(timeFormat.parse(time).getTime());
			}
		}
	}

	/**
	 * Converts to TimeOfDay instance.
	 * 
	 * @param calendar
	 *            The calendar to use when converting time value
	 * @param time
	 *            The time to convert to a time of day
	 * @return The time of day for this time
	 */
	public static TimeOfDay valueOf(final Calendar calendar, final Time time)
	{
		return militaryTime(time.getHour(calendar), time.getMinute(calendar), time
				.getSecond(calendar));
	}

	/**
	 * Converts to TimeOfDay instance.
	 * 
	 * @param duration
	 *            The duration
	 * @return The time of day for the duration since midnight
	 */
	public static TimeOfDay valueOf(final Duration duration)
	{
		return new TimeOfDay(duration.getMilliseconds());
	}

	/**
	 * Converts to TimeOfDay instance.
	 * 
	 * @param time
	 *            The time in milliseconds today
	 * @return The time of day
	 */
	public static TimeOfDay valueOf(final long time)
	{
		return new TimeOfDay(time);
	}

	/**
	 * Converts to TimeOfDay instance.
	 * 
	 * @param time
	 *            A string in h.mma format
	 * @return The time of day on the given calendar
	 * @throws ParseException
	 */
	public static TimeOfDay valueOf(final String time) throws ParseException
	{
		return valueOf(localtime, time);
	}

	/**
	 * Converts to TimeOfDay instance.
	 * 
	 * @param time
	 *            Time to convert to time of day
	 * @return The time of day in the current timezone
	 */
	public static TimeOfDay valueOf(final Time time)
	{
		return valueOf(AbstractTime.localtime, time);
	}

	/**
	 * Private utility constructor forces use of static factory methods.
	 * 
	 * @param time
	 *            Time today in milliseconds
	 */
	private TimeOfDay(final long time)
	{
		super(time);

		// A time of day value must be less than 1 day of milliseconds
		if (Duration.valueOf(time).greaterThan(Duration.ONE_DAY))
		{
			throw new IllegalArgumentException("Time " + this + " is not a time of day value");
		}
	}

	/**
	 * Gets the hour of the day.
	 * 
	 * @return The hour of the day (0-23)
	 */
	public int hour()
	{
		return toHours(getMilliseconds());
	}

	/**
	 * Gets the minute.
	 * 
	 * @return The minute (0-59)
	 */
	public int minute()
	{
		return toMinutes(getMilliseconds()) % 60;
	}

	/**
	 * Gets the next occurrence of this time of day in localtime.
	 * 
	 * @return The next occurrence of this time of day in localtime
	 */
	public Time next()
	{
		return next(AbstractTime.localtime);
	}

	/**
	 * Gets the next occurence of this time of day on the given calendar.
	 * 
	 * @param calendar
	 *            The calendar to use
	 * @return The next occurrence of this time of day on the given calendar
	 */
	public Time next(final Calendar calendar)
	{
		// Get this time of day today
		final Time timeToday = Time.valueOf(calendar, this);

		// If it has already passed
		if (timeToday.before(Time.now()))
		{
			// Return the time tomorrow
			calendar.add(Calendar.DATE, 1);
			return Time.valueOf(calendar, this);
		}
		else
		{
			// Time hasn't happened yet today
			return timeToday;
		}
	}

	/**
	 * Gets the second.
	 * 
	 * @return The second (0-59)
	 */
	public int second()
	{
		return toSeconds(getMilliseconds()) % 60;
	}

	/**
	 * @see Object#toString()
	 */
	public String toString()
	{
		final int second = second();
		return "" + hour() + ":" + minute() + (second != 0 ? ":" + second : "");
	}

	/**
	 * Gets miliseconds as hours.
	 * 
	 * @param milliseconds
	 *            miliseconds to convert
	 * @return converted input
	 */
	private int toHours(final long milliseconds)
	{
		return toMinutes(milliseconds) / 60;
	}

	/**
	 * Gets miliseconds as minutes.
	 * 
	 * @param milliseconds
	 *            miliseconds to convert
	 * @return converted input
	 */
	private int toMinutes(final long milliseconds)
	{
		return toSeconds(milliseconds) / 60;
	}

	/**
	 * Gets miliseconds as seconds.
	 * 
	 * @param milliseconds
	 *            miliseconds to convert
	 * @return converted input
	 */
	private int toSeconds(final long milliseconds)
	{
		return (int)(milliseconds / 1000);
	}
}
