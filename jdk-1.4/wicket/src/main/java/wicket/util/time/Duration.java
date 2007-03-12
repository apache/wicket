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

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;

import wicket.util.string.StringValue;
import wicket.util.string.StringValueConversionException;
import wicket.util.thread.ICode;

/**
 * A duration is an immutable length of time stored as a number of milliseconds.
 * Various factory and conversion methods are available for convenience.
 * <P>
 * These static factory methods allow easy construction of value objects using
 * either long values like seconds(2034) or hours(3):
 * <p>
 * <ul>
 * <li>Duration.milliseconds(long)
 * <li>Duration.seconds(int)
 * <li>Duration.minutes(int)
 * <li>Duration.hours(int)
 * <li>Duration.days(int)
 * </ul>
 * <p>
 * or double precision floating point values like days(3.2):
 * <p>
 * <ul>
 * <li>Duration.milliseconds(double)
 * <li>Duration.seconds(double)
 * <li>Duration.minutes(double)
 * <li>Duration.hours(double)
 * <li>Duration.days(double)
 * </ul>
 * <p>
 * In the case of milliseconds(double), the value will be rounded off to the
 * nearest integral millisecond using Math.round().
 * <p>
 * The precise number of milliseconds represented by a Duration object can be
 * retrieved by calling the milliseconds() method. The value of a Duration
 * object in a given unit like days or hours can be retrieved by calling one of
 * the following unit methods, each of which returns a double precision floating
 * point number:
 * <p>
 * <ul>
 * <li>seconds()
 * <li>minutes()
 * <li>hours()
 * <li>days()
 * </ul>
 * <p>
 * Values can be added and subtracted using the add() and subtract() methods,
 * each of which returns a new immutable Duration object.
 * <p>
 * String values can be converted to Duration objects using the static valueOf
 * factory methods. The string format is the opposite of the one created by
 * toString(), which converts a Duration object to a readable form, such as "3.2
 * hours" or "32.5 minutes". Valid units are: milliseconds, seconds, minutes
 * hours and days. Correct English plural forms are used in creating string
 * values and are parsed as well. The Locale is respected and "," will be used
 * instead of "." in the Eurozone.
 * <p>
 * The benchmark method will "benchmark" a Runnable or an ICode implementing
 * object, returning a Duration object that represents the amount of time
 * elapsed in running the code.
 * <p>
 * Finally, the sleep() method will sleep for the value of a Duration.
 * 
 * @author Jonathan Locke
 */
public class Duration extends AbstractTimeValue
{
	private static final long serialVersionUID = 1L;

	/** Constant for maximum duration */
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

	/** Pattern to match strings. */
	private static final Pattern pattern = Pattern.compile(
			"([0-9]+([.,][0-9]+)?)\\s+(millisecond|second|minute|hour|day)s?",
			Pattern.CASE_INSENSITIVE);

	/**
	 * @param code
	 *            The code
	 * @param log
	 *            Optional log to use with errors and exceptions
	 * @return The duration it took to run the code
	 */
	public static Duration benchmark(final ICode code, final Log log)
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
	 *            The code
	 * @return The duration it took to run the code
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
	 * Gets the duration based on days.
	 * 
	 * @param days
	 * @return duration
	 */
	public static Duration days(final double days)
	{
		return hours(24.0 * days);
	}

	/**
	 * Gets the duration based on days.
	 * 
	 * @param days
	 * @return duration
	 */
	public static Duration days(final int days)
	{
		return hours(24 * days);
	}

	/**
	 * The amount of time elapsed since start time
	 * 
	 * @param start
	 *            The start time
	 * @return The elapsed period
	 * @throws IllegalStateException Thrown if start is in the future
	 */
	public static Duration elapsed(final Time start)
	{
		return start.elapsedSince();
	}

	/**
	 * Gets the duration based on hours.
	 * 
	 * @param hours
	 * @return duration
	 */
	public static Duration hours(final double hours)
	{
		return minutes(60.0 * hours);
	}

	/**
	 * Gets the duration based on hours.
	 * 
	 * @param hours
	 * @return duration
	 */
	public static Duration hours(final int hours)
	{
		return minutes(60 * hours);
	}

	/**
	 * Gets the duration based on milliseconds.
	 * 
	 * @param milliseconds
	 * @return duration
	 */
	public static Duration milliseconds(final double milliseconds)
	{
		return milliseconds(Math.round(milliseconds));
	}

	/**
	 * Gets the duration based on miliseconds.
	 * 
	 * @param milliseconds
	 * @return duration
	 */
	public static Duration milliseconds(final long milliseconds)
	{
		return new Duration(milliseconds);
	}

	/**
	 * Gets the duration based on minutes.
	 * 
	 * @param minutes
	 * @return duration
	 */
	public static Duration minutes(final double minutes)
	{
		return seconds(60.0 * minutes);
	}

	/**
	 * Gets the duration based on minutes.
	 * 
	 * @param minutes
	 * @return duration
	 */
	public static Duration minutes(final int minutes)
	{
		return seconds(60 * minutes);
	}

	/**
	 * Gets the duration based on seconds.
	 * 
	 * @param seconds
	 * @return duration
	 */
	public static Duration seconds(final double seconds)
	{
		return milliseconds(seconds * 1000.0);
	}

	/**
	 * Gets the duration based on seconds.
	 * 
	 * @param seconds
	 * @return duration
	 */
	public static Duration seconds(final int seconds)
	{
		return milliseconds(seconds * 1000L);
	}

	/**
	 * Gets the given long as a duration.
	 * 
	 * @param time
	 *            The duration value in milliseconds
	 * @return Duration value
	 */
	public static Duration valueOf(final long time)
	{
		return new Duration(time);
	}

	/**
	 * Converts the given string to a new duration object. The string can take
	 * the form of a floating point number followed by a number of milliseconds,
	 * seconds, minutes, hours or days. For example "6 hours" or "3.4 days".
	 * Parsing is case insensitive.
	 * 
	 * @param string
	 *            The string to parse
	 * @return The duration value of the given string
	 * @throws StringValueConversionException
	 */
	public static Duration valueOf(final String string) throws StringValueConversionException
	{
		return valueOf(string, Locale.getDefault());
	}

	/**
	 * Converts the given string to a new Duration object. The string can take
	 * the form of a floating point number followed by a number of milliseconds,
	 * seconds, minutes, hours or days. For example "6 hours" or "3.4 days".
	 * Parsing is case insensitive.
	 * 
	 * @param string
	 *            The string to parse
	 * @param locale
	 *            Locale used for parsing
	 * @return The duration value of the given string
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
	 *            Number of milliseconds in this duration
	 */
	protected Duration(final long milliseconds)
	{
		super(milliseconds);
	}

	/**
	 * Adds a given duration to this duration.
	 * 
	 * @param duration
	 *            The duration to add
	 * @return The sum of the durations
	 */
	public Duration add(final Duration duration)
	{
		return valueOf(getMilliseconds() + duration.getMilliseconds());
	}

	/**
	 * Gets number of days of the current duration.
	 * 
	 * @return number of days of the current duration
	 */
	public final double days()
	{
		return hours() / 24.0;
	}

	/**
	 * Gets number of hours of the current duration.
	 * 
	 * @return number of hours of the current duration
	 */
	public final double hours()
	{
		return minutes() / 60.0;
	}

	/**
	 * Gets number of minutes of the current duration.
	 * 
	 * @return number of minutes of the current duration
	 */
	public final double minutes()
	{
		return seconds() / 60.0;
	}

	/**
	 * Gets number of seconds of the current duration.
	 * 
	 * @return number of seconds of the current duration
	 */
	public final double seconds()
	{
		return getMilliseconds() / 1000.0;
	}

	/**
	 * Sleep for the current duration.
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
	 * Subtract a given duration from this duration.
	 * 
	 * @param that
	 *            The duration to subtract
	 * @return This duration minus that duration
	 */
	public Duration subtract(final Duration that)
	{
		return valueOf(getMilliseconds() - that.getMilliseconds());
	}

	/**
	 * Gets the string representation of this duration in days, hours, minutes,
	 * seconds or milliseconds, as appropriate. Uses the default locale.
	 * 
	 * @return String representation
	 */
	public String toString()
	{
		return toString(Locale.getDefault());
	}

	/**
	 * Gets the string representation of this duration in days, hours, minutes,
	 * seconds or milliseconds, as appropriate.
	 * 
	 * @param locale
	 *            the locale
	 * @return String representation
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

			return getMilliseconds() + " milliseconds";
		}
		else
		{
			return "N/A";
		}
	}

	/**
	 * Converts a value to a unit suffixed value, taking care of English
	 * singular/plural suffix.
	 * 
	 * @param value
	 *            The value to format
	 * @param units
	 *            The units to apply singular or plural suffix to
	 * @param locale
	 *            The locale
	 * @return A string for the value
	 */
	private String unitString(final double value, final String units, final Locale locale)
	{
		return StringValue.valueOf(value, locale) + " " + units + ((value > 1.0) ? "s" : "");
	}
}
