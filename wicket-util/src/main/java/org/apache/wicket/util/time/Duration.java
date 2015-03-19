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

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.wicket.util.string.StringValue;
import org.apache.wicket.util.string.StringValueConversionException;
import org.apache.wicket.util.thread.ICode;
import org.slf4j.Logger;


/**
 * A <code>Duration</code> is an immutable length of time stored as a number of milliseconds.
 * Various factory and conversion methods are available for convenience.
 * <p>
 * These static factory methods allow easy construction of value objects using either long values
 * like <code>seconds(2034)</code> or <code>hours(3)</code>:
 * <p>
 * <ul>
 * <li><code>Duration.milliseconds(long)</code>
 * <li><code>Duration.seconds(int)</code>
 * <li><code>Duration.minutes(int)</code>
 * <li><code>Duration.hours(int)</code>
 * <li><code>Duration.days(int)</code>
 * </ul>
 * <p>
 * ...or double-precision floating point values like <code>days(3.2)</code>:
 * <p>
 * <ul>
 * <li><code>Duration.milliseconds(double)</code>
 * <li><code>Duration.seconds(double)</code>
 * <li><code>Duration.minutes(double)</code>
 * <li><code>Duration.hours(double)</code>
 * <li><code>Duration.days(double)</code>
 * </ul>
 * <p>
 * In the case of <code>milliseconds(double)</code>, the value will be rounded off to the nearest
 * integral millisecond using <code>Math.round()</code>.
 * <p>
 * The precise number of milliseconds represented by a <code>Duration</code> object can be retrieved
 * by calling the <code>getMilliseconds</code> method. The value of a <code>Duration</code> object
 * in a given unit like days or hours can be retrieved by calling one of the following unit methods,
 * each of which returns a double-precision floating point number:
 * <p>
 * <ul>
 * <li><code>seconds()</code>
 * <li><code>minutes()</code>
 * <li><code>hours()</code>
 * <li><code>days()</code>
 * </ul>
 * <p>
 * Values can be added and subtracted using the <code>add(Duration)</code> and
 * <code>subtract(Duration)</code> methods, each of which returns a new immutable
 * <code>Duration</code> object.
 * <p>
 * <code>String</code> values can be converted to <code>Duration</code> objects using the static
 * <code>valueOf</code> factory methods. The <code>String</code> format is the opposite of the one
 * created by <code>toString()</code>, which converts a <code>Duration</code> object to a readable
 * form, such as "3.2 hours" or "32.5 minutes". Valid units are: milliseconds, seconds, minutes
 * hours and days. Correct English plural forms are used in creating <code>String</code> values and
 * are parsed as well. The <code>Locale</code> is respected and "," will be used instead of "." in
 * the Eurozone.
 * <p>
 * The benchmark method will "benchmark" a <code>Runnable</code> or an {@link ICode} implementing
 * object, returning a <code>Duration</code> object that represents the amount of time elapsed in
 * running the code.
 * <p>
 * Finally, the <code>sleep</code> method will sleep for the value of a <code>Duration</code>.
 * 
 * @author Jonathan Locke
 * @since 1.2.6
 */
public class Duration extends AbstractTimeValue
{
	private static final long serialVersionUID = 1L;

	/** Constant for maximum duration. */
	public static final Duration MAXIMUM = milliseconds(Long.MAX_VALUE);

	/** Constant for no duration. */
	public static final Duration NONE = milliseconds(0);

	/** Constant for one day. */
	public static final Duration ONE_DAY = days(1);

	/** Constant for one hour. */
	public static final Duration ONE_HOUR = hours(1);

	/** Constant for on minute. */
	public static final Duration ONE_MINUTE = minutes(1);

	/** Constant for one second. */
	public static final Duration ONE_SECOND = seconds(1);

	/** Constant for one week. */
	public static final Duration ONE_WEEK = days(7);

	/** pattern to match strings */
	private static final Pattern pattern = Pattern.compile(
		"([0-9]+([.,][0-9]+)?)\\s+(millisecond|second|minute|hour|day)s?", Pattern.CASE_INSENSITIVE);

	/**
	 * Benchmark the given command.
	 * 
	 * @param code
	 *            an <code>ICode</code>
	 * @param log
	 *            optional logger to use with errors and exceptions
	 * @return the <code>Time</code> value it took to run the code
	 */
	public static Duration benchmark(final ICode code, final Logger log)
	{
		// Get time before running code
		final Time start = Time.now();

		// Run the code
		code.run(log);

		// Return the difference
		return Time.now().subtract(start);
	}

	/**
	 * Benchmark the given command.
	 * 
	 * @param code
	 *            a <code>Runnable</code>
	 * @return the <code>Time</code> value it took to run the code
	 */
	public static Duration benchmark(final Runnable code)
	{
		// Get time before running code
		final Time start = Time.now();

		// Run code
		code.run();

		// Return the difference
		return Time.now().subtract(start);
	}

	/**
	 * Retrieves the <code>Duration</code> based on days.
	 * 
	 * @param days
	 *            days <code>double</code> value
	 * @return the <code>Duration</code> based on days
	 */
	public static Duration days(final double days)
	{
		return hours(24.0 * days);
	}

	/**
	 * Retrieves the <code>Duration</code> based on days.
	 * 
	 * @param days
	 *            days <code>int</code> value
	 * @return the <code>Duration</code> based on days
	 */
	public static Duration days(final int days)
	{
		return hours(24 * days);
	}

	/**
	 * Calculates the amount of time elapsed since start time.
	 * 
	 * @param start
	 *            the start <code>Time</code>
	 * @return the elapsed period as a <code>Duration</code>
	 * @throws IllegalStateException
	 *             if start <code>Time</code> is in the future
	 */
	public static Duration elapsed(final Time start)
	{
		return start.elapsedSince();
	}

	/**
	 * Retrieves the <code>Duration</code> based on hours.
	 * 
	 * @param hours
	 *            hours <code>double</code> value
	 * @return the <code>Duration</code> based on hours
	 */
	public static Duration hours(final double hours)
	{
		return minutes(60.0 * hours);
	}

	/**
	 * Retrieves the <code>Duration</code> based on hours.
	 * 
	 * @param hours
	 *            hours <code>int</code> value
	 * @return the <code>Duration</code> based on hours
	 */
	public static Duration hours(final int hours)
	{
		return minutes(60 * hours);
	}

	/**
	 * Retrieves the <code>Duration</code> based on milliseconds.
	 * 
	 * @param milliseconds
	 *            milliseconds <code>double</code> value
	 * @return the <code>Duration</code> based on milliseconds
	 */
	public static Duration milliseconds(final double milliseconds)
	{
		return milliseconds(Math.round(milliseconds));
	}

	/**
	 * Retrieves the <code>Duration</code> based on milliseconds.
	 * 
	 * @param milliseconds
	 *            milliseconds <code>long</code> value
	 * @return the <code>Duration</code> based on milliseconds
	 */
	public static Duration milliseconds(final long milliseconds)
	{
		return new Duration(milliseconds);
	}

	/**
	 * Retrieves the <code>Duration</code> based on minutes.
	 * 
	 * @param minutes
	 *            minutes <code>double</code> value
	 * @return the <code>Duration</code> based on minutes
	 */
	public static Duration minutes(final double minutes)
	{
		return seconds(60.0 * minutes);
	}

	/**
	 * Retrieves the <code>Duration</code> based on minutes.
	 * 
	 * @param minutes
	 *            minutes <code>int</code> value
	 * @return the <code>Duration</code> based on minutes
	 */
	public static Duration minutes(final int minutes)
	{
		return seconds(60 * minutes);
	}

	/**
	 * Retrieves the <code>Duration</code> based on seconds.
	 * 
	 * @param seconds
	 *            seconds <code>double</code> value
	 * @return the <code>Duration</code> based on seconds
	 */
	public static Duration seconds(final double seconds)
	{
		return milliseconds(seconds * 1000.0);
	}

	/**
	 * Retrieves the <code>Duration</code> based on seconds.
	 * 
	 * @param seconds
	 *            seconds <code>int</code> value
	 * @return the <code>Duration</code> based on seconds
	 */
	public static Duration seconds(final int seconds)
	{
		return milliseconds(seconds * 1000L);
	}

	/**
	 * Retrieves the given <code>long</code> as a <code>Duration</code>.
	 * 
	 * @param time
	 *            the duration <code>long</code> value in milliseconds
	 * @return the <code>Duration</code> value
	 */
	public static Duration valueOf(final long time)
	{
		return new Duration(time);
	}

	/**
	 * Converts the given <code>String</code> to a new <code>Duration</code> object. The string can
	 * take the form of a floating point number followed by a number of milliseconds, seconds,
	 * minutes, hours or days. For example "6 hours" or "3.4 days". Parsing is case-insensitive.
	 * 
	 * @param string
	 *            a <code>String</code> to parse
	 * @return the <code>Duration</code> value of the given <code>String</code>
	 * @throws StringValueConversionException
	 */
	public static Duration valueOf(final String string) throws StringValueConversionException
	{
		return valueOf(string, Locale.getDefault());
	}

	/**
	 * Converts the given <code>String</code> to a new <code>Duration</code> object. The string can
	 * take the form of a floating point number followed by a number of milliseconds, seconds,
	 * minutes, hours or days. For example "6 hours" or "3.4 days". Parsing is case-insensitive.
	 * 
	 * @param string
	 *            a <code>String</code> to parse
	 * @param locale
	 *            the <code>Locale</code> used for parsing
	 * @return the <code>Duration</code> value of the given <code>String</code>
	 * @throws StringValueConversionException
	 */
	public static Duration valueOf(final String string, final Locale locale)
		throws StringValueConversionException
	{
		final Matcher matcher = pattern.matcher(string);

		if (matcher.matches())
		{
			final double value = StringValue.valueOf(matcher.group(1), locale).toDouble();
			final String units = matcher.group(3);

			if (units.equalsIgnoreCase("millisecond"))
			{
				return milliseconds(value);
			}
			else if (units.equalsIgnoreCase("second"))
			{
				return seconds(value);
			}
			else if (units.equalsIgnoreCase("minute"))
			{
				return minutes(value);
			}
			else if (units.equalsIgnoreCase("hour"))
			{
				return hours(value);
			}
			else if (units.equalsIgnoreCase("day"))
			{
				return days(value);
			}
			else
			{
				throw new StringValueConversionException("Unrecognized units: " + string);
			}
		}
		else
		{
			throw new StringValueConversionException("Unable to parse duration: " + string);
		}
	}

	/**
	 * Private constructor forces use of static factory methods.
	 * 
	 * @param milliseconds
	 *            number of milliseconds in this <code>Duration</code>
	 */
	protected Duration(final long milliseconds)
	{
		super(milliseconds);
	}

	/**
	 * Adds a given <code>Duration</code> to this <code>Duration</code>.
	 * 
	 * @param duration
	 *            the <code>Duration</code> to add
	 * @return the sum of the <code>Duration</code>s
	 */
	public Duration add(final Duration duration)
	{
		return valueOf(getMilliseconds() + duration.getMilliseconds());
	}

	/**
	 * Retrieves the number of days of the current <code>Duration</code>.
	 * 
	 * @return number of days of the current <code>Duration</code>
	 */
	public final double days()
	{
		return hours() / 24.0;
	}

	/**
	 * Retrieves the number of hours of the current <code>Duration</code>.
	 * 
	 * @return number of hours of the current <code>Duration</code>
	 */
	public final double hours()
	{
		return minutes() / 60.0;
	}

	/**
	 * Retrieves the number of minutes of the current <code>Duration</code>.
	 * 
	 * @return number of minutes of the current <code>Duration</code>
	 */
	public final double minutes()
	{
		return seconds() / 60.0;
	}

	/**
	 * Retrieves the number of seconds of the current <code>Duration</code>.
	 * 
	 * @return number of seconds of the current <code>Duration</code>
	 */
	public final double seconds()
	{
		return getMilliseconds() / 1000.0;
	}

	/**
	 * Sleeps for the current <code>Duration</code>.
	 */
	public final void sleep()
	{
		if (getMilliseconds() > 0)
		{
			try
			{
				Thread.sleep(getMilliseconds());
			}
			catch (InterruptedException e)
			{
				// Ignored
			}
		}
	}

	/**
	 * Subtracts a given <code>Duration</code> from this <code>Duration</code>.
	 * 
	 * @param that
	 *            the <code>Duration</code> to subtract
	 * @return this <code>Duration</code> minus that <code>Duration</code>
	 */
	public Duration subtract(final Duration that)
	{
		return valueOf(getMilliseconds() - that.getMilliseconds());
	}

	/**
	 * Wait for this duration on the given monitor
	 * 
	 * @param object
	 *            The monitor to wait on
	 */
	public void wait(final Object object)
	{
		try
		{
			object.wait(getMilliseconds());
		}
		catch (InterruptedException e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * Retrieves the <code>String</code> representation of this <code>Duration</code> in days,
	 * hours, minutes, seconds or milliseconds, as appropriate. Uses the default <code>Locale</code>
	 * .
	 * 
	 * @return a <code>String</code> representation
	 */
	@Override
	public String toString()
	{
		return toString(Locale.getDefault());
	}

	/**
	 * Retrieves the <code>String</code> representation of this <code>Duration</code> in days,
	 * hours, minutes, seconds or milliseconds, as appropriate.
	 * 
	 * @param locale
	 *            a <code>Locale</code>
	 * @return a <code>String</code> representation
	 */
	public String toString(final Locale locale)
	{
		if (getMilliseconds() >= 0)
		{
			if (days() >= 1.0)
			{
				return unitString(days(), "day", locale);
			}

			if (hours() >= 1.0)
			{
				return unitString(hours(), "hour", locale);
			}

			if (minutes() >= 1.0)
			{
				return unitString(minutes(), "minute", locale);
			}

			if (seconds() >= 1.0)
			{
				return unitString(seconds(), "second", locale);
			}

			return unitString(getMilliseconds(), "millisecond", locale);
		}
		else
		{
			return "N/A";
		}
	}

	/**
	 * Converts a value to a unit-suffixed value, taking care of English singular/plural suffix.
	 * 
	 * @param value
	 *            a <code>double</code> value to format
	 * @param units
	 *            the units to apply singular or plural suffix to
	 * @param locale
	 *            the <code>Locale</code>
	 * @return a <code>String</code> representation
	 */
	private String unitString(final double value, final String units, final Locale locale)
	{
		return StringValue.valueOf(value, locale) + " " + units + ((value > 1.0) ? "s" : "");
	}
}
