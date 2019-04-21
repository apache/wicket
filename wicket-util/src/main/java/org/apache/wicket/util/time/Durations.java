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
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.wicket.util.string.StringValue;
import org.apache.wicket.util.string.StringValueConversionException;

public class Durations 
{
  
  /** pattern to match strings */
  private static final Pattern pattern = Pattern.compile(
      "([0-9]+([.,][0-9]+)?)\\s+(millisecond|second|minute|hour|day)s?", Pattern.CASE_INSENSITIVE);
  
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
          final long longValue = (long) value;
          final String units = matcher.group(3);

          if (units.equalsIgnoreCase("millisecond"))
          {
              return Duration.ofMillis(longValue);
          }
          else if (units.equalsIgnoreCase("second"))
          {
              return Duration.ofSeconds(longValue);
          }
          else if (units.equalsIgnoreCase("minute"))
          {
              return Duration.ofMinutes(longValue);
          }
          else if (units.equalsIgnoreCase("hour"))
          {
              return Duration.ofHours(longValue);
          }
          else if (units.equalsIgnoreCase("day"))
          {
              return Duration.ofDays(longValue);
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
  
  public static Duration valueOf(final String string) 
  {
    return valueOf(string, Locale.getDefault(Locale.Category.FORMAT));
  }
}
