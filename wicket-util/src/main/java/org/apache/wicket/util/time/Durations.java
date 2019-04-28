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

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;

import org.apache.wicket.util.string.StringValue;

/**
 * 
 * Utility class for {@link Duration}
 *
 */
public class Durations
{

	public static String toString(final Duration duration, final Locale locale)
	{
		if (duration.toMillis() >= 0)
		{
			if (duration.toDays() >= 1.0)
			{
				return unitString(duration.toDays(), "day", locale);
			}

			if (duration.toHours() >= 1.0)
			{
				return unitString(duration.toHours(), "hour", locale);
			}

			if (duration.toMinutes() >= 1.0)
			{
				return unitString(duration.toMinutes(), "minute", locale);
			}

			if (duration.toSeconds() >= 1.0)
			{
				return unitString(duration.toSeconds(), "second", locale);
			}

			return unitString(duration.toMillis(), "millisecond", locale);
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
	private static String unitString(final double value, final String units, final Locale locale)
	{
		return StringValue.valueOf(value, locale) + " " + units + ((value > 1.0) ? "s" : "");
	}

	/**
	 * Calculates the duration between a given {@link Instant} and the current one.
	 * @param start 
	 * 			a given instant
	 * @return the duration between a given Instant and the current one
	 */
	public static Duration elapsedSince(Instant start)
	{
	    return Duration.between(start, Instant.now());
	}
}
