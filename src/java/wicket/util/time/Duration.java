/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
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
 * A duration is an immutable length of time stored as a number of milliseconds. Various
 * factory and conversion methods are available for convenience.
 * @author Jonathan Locke
 */
public final class Duration extends AbstractTimeValue
{
	/** serialVersionUID */
	private static final long serialVersionUID = 1212559549133827631L;

	/** constant for no duration. */
    public static final Duration NONE = milliseconds(0);

    /** constant for one week. */
    public static final Duration ONE_WEEK = days(7);

    /** constant for one day. */
    public static final Duration ONE_DAY = days(1);

    /** constant for one hour. */
    public static final Duration ONE_HOUR = hours(1);

    /** constant for on minute. */
    public static final Duration ONE_MINUTE = minutes(1);

    /** constant for one second. */
    public static final Duration ONE_SECOND = seconds(1);

    /** Pattern to match strings. */
    private static final Pattern pattern = Pattern.compile(
            "([0-9]+([.,][0-9]+)?)\\s+(millisecond|second|minute|hour|day)s?",
            Pattern.CASE_INSENSITIVE);

    /**
     * Private constructor forces use of static factory methods.
     * @param milliseconds Number of milliseconds in this duration
     */
    private Duration(final long milliseconds)
    {
        super(milliseconds);
    }

    /**
     * gets the given long as a duration.
     * @param time The duration value in milliseconds
     * @return Duration value
     */
    public static Duration valueOf(final long time)
    {
        return new Duration(time);
    }

    /**
     * Converts the given string to a new duration object. The string can take the form of
     * a floating point number followed by a number of milliseconds, seconds, minutes,
     * hours or days. For example "6 hours" or "3.4 days". Parsing is case insensitive.
     * @param string The string to parse
     * @return The duration value of the given string
     * @throws StringValueConversionException
     */
    public static Duration valueOf(final String string) throws StringValueConversionException
    {
    	return valueOf(string, Locale.getDefault());
    }
    
    /**
     * Converts the given string to a new duration object. The string can take the form of
     * a floating point number followed by a number of milliseconds, seconds, minutes,
     * hours or days. For example "6 hours" or "3.4 days". Parsing is case insensitive.
     * @param string The string to parse
     * @param locale Locale used for parsing
     * @return The duration value of the given string
     * @throws StringValueConversionException
     */
    public static Duration valueOf(final String string, final Locale locale) throws StringValueConversionException
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
     * Gets the duration based on miliseconds.
     * @param milliseconds
     * @return duration
     */
    public static Duration milliseconds(final long milliseconds)
    {
        return new Duration(milliseconds);
    }

    /**
     * Gets the duration based on milliseconds.
     * @param milliseconds
     * @return duration
     */
    public static Duration milliseconds(final double milliseconds)
    {
        return milliseconds(Math.round(milliseconds));
    }

    /**
     * Gets the duration based on seconds.
     * @param seconds
     * @return duration
     */
    public static Duration seconds(final int seconds)
    {
        return milliseconds(seconds * 1000L);
    }

    /**
     * Gets the duration based on minutes.
     * @param minutes
     * @return duration
     */
    public static Duration minutes(final int minutes)
    {
        return seconds(60 * minutes);
    }

    /**
     * Gets the duration based on hours.
     * @param hours
     * @return duration
     */
    public static Duration hours(final int hours)
    {
        return minutes(60 * hours);
    }

    /**
     * Gets the duration based on days.
     * @param days
     * @return duration
     */
    public static Duration days(final int days)
    {
        return hours(24 * days);
    }

    /**
     * Gets the duration based on seconds.
     * @param seconds
     * @return duration
     */
    public static Duration seconds(final double seconds)
    {
        return milliseconds(seconds * 1000.0);
    }

    /**
     * Gets the duration based on minutes.
     * @param minutes
     * @return duration
     */
    public static Duration minutes(final double minutes)
    {
        return seconds(60.0 * minutes);
    }

    /**
     * Gets the duration based on hours.
     * @param hours
     * @return duration
     */
    public static Duration hours(final double hours)
    {
        return minutes(60.0 * hours);
    }

    /**
     * Gets the duration based on days.
     * @param days
     * @return duration
     */
    public static Duration days(final double days)
    {
        return hours(24.0 * days);
    }

    /**
     * Gets number of seconds of the current duration.
     * @return number of seconds of the current duration
     */
    public final double getSeconds()
    {
        return getMilliseconds() / 1000.0;
    }

    /**
     * Gets number of minutes of the current duration.
     * @return number of minutes of the current duration
     */
    public final double getMinutes()
    {
        return getSeconds() / 60.0;
    }

    /**
     * Gets number of hours of the current duration.
     * @return number of hours of the current duration
     */
    public final double getHours()
    {
        return getMinutes() / 60.0;
    }

    /**
     * Gets number of days of the current duration.
     * @return number of days of the current duration
     */
    public final double getDays()
    {
        return getHours() / 24.0;
    }

    /**
     * @param code The code
     * @param log Optional log to use with errors and exceptions
     * @return The duration it took to run the code
     */
    public static Duration benchmark(final ICode code, final Log log)
    {
        final Time start = Time.now();
        
        code.run(log);

        return Time.now().subtract(start);
    }

    /**
     * Benchmark the given command.
     * @param code The code
     * @return The duration it took to run the code
     */
    public static Duration benchmark(final Runnable code)
    {
        final Time start = Time.now();

        code.run();

        return Time.now().subtract(start);
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
     * Adds a given duration to this duration.
     * @param duration The duration to add
     * @return The sum of the durations
     */
    public Duration add(final Duration duration)
    {
        return valueOf(getMilliseconds() + duration.getMilliseconds());
    }

    /**
     * Subtract a given duration from this duration.
     * @param that The duration to subtract
     * @return This duration minus that duration
     */
    public Duration subtract(final Duration that)
    {
        return valueOf(getMilliseconds() - that.getMilliseconds());
    }

    /**
     * Gets the string representation of this duration in days, hours, minutes, seconds or
     *         milliseconds, as appropriate. Uses the default locale
     * @return String representation
     */
    public String toString()
    {
    	return toString(Locale.getDefault());
    }

    /**
     * Gets the string representation of this duration in days, hours, minutes, seconds or
     *         milliseconds, as appropriate.
     * @param locale the locale
     * @return String representation
     */
    public String toString(final Locale locale)
    {
        if (getMilliseconds() >= 0)
        {
            if (getDays() >= 1.0)
            {
                return unitString(getDays(), "day", locale);
            }

            if (getHours() >= 1.0)
            {
                return unitString(getHours(), "hour", locale);
            }

            if (getMinutes() >= 1.0)
            {
                return unitString(getMinutes(), "minute", locale);
            }

            if (getSeconds() >= 1.0)
            {
                return unitString(getSeconds(), "second", locale);
            }

            return getMilliseconds() + " milliseconds";
        }
        else
        {
            return "N/A";
        }
    }

    /**
     * Converts a value to a unit suffixed value, taking care of English singular/plural
     * suffix.
     * @param value The value to format
     * @param units The units to apply singular or plural suffix to
     * @param locale The locale
     * @return A string for the value
     */
    private String unitString(final double value, final String units, final Locale locale)
    {
        return StringValue.valueOf(value, locale) + " " + units + ((value > 1.0) ? "s" : "");
    }
}

///////////////////////////////// End of File /////////////////////////////////
