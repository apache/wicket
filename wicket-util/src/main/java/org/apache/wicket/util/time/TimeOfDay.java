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
import java.util.Calendar;

import org.apache.wicket.util.lang.EnumeratedType;


/**
 * An immutable time of day value represented as milliseconds since the most recent midnight.
 * <p>
 * Values can be constructed using various factory methods:
 * <ul>
 * <li><code>valueOf(long)</code> where <code>long</code> is milliseconds since midnight
 * <li><code>valueOf(String)</code> where the <code>String</code> is in 'h.mma' format
 * <li><code>valueOf(Calendar, String)</code> where the <code>String</code> is in 'h.mma' format
 * <li><code>valueOf(Duration)</code> where <code>Duration</code> is time since midnight
 * <li><code>valueOf(Time)</code> where <code>Time</code> is some point in time today
 * <li><code>valueOf(Calendar, Time)</code> where <code>Time</code> is some point in time today
 * <li><code>militaryTime(int hour, int minute, int second)</code> for 24-hour time
 * <li><code>time(int hour, int minute, Meridian)</code> where <code>Meridian</code> is AM or PM
 * <li><code>time(int hour, int minute, int second, Meridian)</code> where <code>Meridian</code> is
 * AM or PM
 * <li><code>now()</code> to construct the current time of day
 * <li><code>now(Calendar)</code> to construct the current time of day using a given
 * <code>Calendar</code>
 * </ul>
 * <p>
 * If an attempt is made to construct an illegal time of day value (one that is greater than 24
 * hours worth of milliseconds), an <code>IllegalArgumentException</code> will be thrown.
 * <p>
 * Military hours, minutes and seconds of the time of day can be retrieved by calling the
 * <code>hour</code>, <code>minute</code>, and <code>second</code> methods.
 * <p>
 * The next occurrence of a given <code>TimeOfDay</code> can be retrieved by calling
 * <code>next()</code> or <code>next(Calendar)</code>.
 * 
 * @author Jonathan Locke
 * @since 1.2.6
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
		 * Constructor.
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
	 * Retrieves a <code>TimeOfDay</code> value on a 24-hour clock.
	 * 
	 * @param hour
	 *            the hour (0-23)
	 * @param minute
	 *            the minute (0-59)
	 * @param second
	 *            the second (0-59)
	 * @return the time of day
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

		return valueOf(Duration.hours(hour)
			.add(Duration.minutes(minute))
			.add(Duration.seconds(second)));
	}

	/**
	 * Retrieves the <code>TimeOfDay</code> representing 'now'.
	 * 
	 * @return the time of day it is now
	 */
	public static TimeOfDay now()
	{
		return valueOf(Time.now());
	}

	/**
	 * Retrieves the <code>TimeOfDay</code> representing 'now' on the given <code>Calendar</code>.
	 * 
	 * @param calendar
	 *            the <code>Calendar</code> to use
	 * @return the time of day it is now on the given <code>Calendar</code>
	 */
	public static TimeOfDay now(final Calendar calendar)
	{
		return valueOf(calendar, Time.now());
	}

	/**
	 * Retrieves a <code>TimeOfDay</code> on a 12-hour clock.
	 * 
	 * @param hour
	 *            the hour (1-12)
	 * @param minute
	 *            the minute (0-59)
	 * @param second
	 *            the second (0-59)
	 * @param meridian
	 *            AM or PM
	 * @return the <code>TimeOfDay</code> value
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
	 * Retrieves a <code>TimeOfDay</code> on a 12-hour clock.
	 * 
	 * @param hour
	 *            the hour (1-12)
	 * @param minute
	 *            the minute (0-59)
	 * @param meridian
	 *            AM of PM
	 * @return the <code>TimeOfDay</code> value
	 */
	public static TimeOfDay time(final int hour, final int minute, final Meridian meridian)
	{
		return time(hour, minute, 0, meridian);
	}

	/**
	 * Converts a time <code>String</code> and <code>Calendar</code> to a <code>TimeOfDay</code>
	 * instance.
	 * 
	 * @param calendar
	 *            the <code>Calendar</code> to use when parsing time <code>String</code>
	 * @param time
	 *            a <code>String</code> in 'h.mma' format
	 * @return the <code>TimeOfDay</code> on the given <code>Calendar</code>
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
	 * Converts a <code>Time</code> instance and <code>Calendar</code> to a <code>TimeOfDay</code>
	 * instance.
	 * 
	 * @param calendar
	 *            the <code>Calendar</code> to use
	 * @param time
	 *            a <code>Time</code> instance
	 * @return the <code>TimeOfDay</code> on the given <code>Calendar</code>
	 */
	public static TimeOfDay valueOf(final Calendar calendar, final Time time)
	{
		return militaryTime(time.getHour(calendar), time.getMinute(calendar),
			time.getSecond(calendar));
	}

	/**
	 * Converts a <code>Duration</code> instance to a <code>TimeOfDay</code> instance.
	 * 
	 * @param duration
	 *            the <code>Duration</code> to use
	 * @return the <code>TimeOfDay</code> of the given <code>Duration</code>
	 */
	public static TimeOfDay valueOf(final Duration duration)
	{
		return new TimeOfDay(duration.getMilliseconds());
	}

	/**
	 * Converts a <code>long</code> value to a <code>TimeOfDay</code> instance.
	 * 
	 * @param time
	 *            the time in milliseconds today
	 * @return the <code>TimeOfDay</code>
	 */
	public static TimeOfDay valueOf(final long time)
	{
		return new TimeOfDay(time);
	}

	/**
	 * Converts a <code>String</code> value to a <code>TimeOfDay</code> instance.
	 * 
	 * @param time
	 *            a <code>String</code> in 'h.mma' format
	 * @return the <code>TimeOfDay</code>
	 * @throws ParseException
	 */
	public static TimeOfDay valueOf(final String time) throws ParseException
	{
		return valueOf(localtime, time);
	}

	/**
	 * Converts a <code>String</code> value to a <code>TimeOfDay</code> instance.
	 * 
	 * @param time
	 *            a <code>Time</code> to convert to <code>TimeOfDay</code>
	 * @return the <code>TimeOfDay</code> in the current time zone
	 */
	public static TimeOfDay valueOf(final Time time)
	{
		return valueOf(AbstractTime.localtime, time);
	}

	/**
	 * Private utility constructor forces use of static factory methods.
	 * 
	 * @param time
	 *            the time today in milliseconds
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
	 * Retrieves the hour of the day.
	 * 
	 * @return the hour (0-23) of this <code>TimeOfDay</code>
	 */
	public int hour()
	{
		return toHours(getMilliseconds());
	}

	/**
	 * Retrieves the minute.
	 * 
	 * @return the minute (0-59) of this <code>TimeOfDay</code>
	 */
	public int minute()
	{
		return toMinutes(getMilliseconds()) % 60;
	}

	/**
	 * Retrieves the next occurrence of this <code>TimeOfDay</code> in local time.
	 * 
	 * @return the next occurrence of this <code>TimeOfDay</code> in local time
	 */
	public Time next()
	{
		return next(AbstractTime.localtime);
	}

	/**
	 * Retrieves the next occurrence of this <code>TimeOfDay</code> on the given
	 * <code>Calendar</code>.
	 * 
	 * @param calendar
	 *            the <code>Calendar</code> to use
	 * @return the next occurrence of this <code>TimeOfDay</code> on the given <code>Calendar</code>
	 */
	public Time next(final Calendar calendar)
	{
		// Get this time of day today
		final Time timeToday = Time.valueOf(calendar, this);

		// If it has already passed
		if (timeToday.before(Time.now()))
		{
			// Return the time tomorrow
			return Time.valueOf(calendar, this).add(Duration.ONE_DAY);
		}
		else
		{
			// Time hasn't happened yet today
			return timeToday;
		}
	}

	/**
	 * Retrieves the second.
	 * 
	 * @return the second (0-59)
	 */
	public int second()
	{
		return toSeconds(getMilliseconds()) % 60;
	}

	/**
	 * @see Object#toString()
	 */
	@Override
	public String toString()
	{
		final int second = second();
		return "" + hour() + ":" + minute() + (second != 0 ? ":" + second : "");
	}

	/**
	 * Retrieves milliseconds as hours.
	 * 
	 * @param milliseconds
	 *            milliseconds to convert
	 * @return converted input
	 */
	private int toHours(final long milliseconds)
	{
		return toMinutes(milliseconds) / 60;
	}

	/**
	 * Retrieves milliseconds as minutes.
	 * 
	 * @param milliseconds
	 *            milliseconds to convert
	 * @return converted input
	 */
	private int toMinutes(final long milliseconds)
	{
		return toSeconds(milliseconds) / 60;
	}

	/**
	 * Retrieves milliseconds as seconds.
	 * 
	 * @param milliseconds
	 *            milliseconds to convert
	 * @return converted input
	 */
	private int toSeconds(final long milliseconds)
	{
		return (int)(milliseconds / 1000);
	}
}
