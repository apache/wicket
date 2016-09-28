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
package org.apache.wicket.util.lang;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.wicket.util.string.StringValue;
import org.apache.wicket.util.string.StringValueConversionException;
import org.apache.wicket.util.value.LongValue;


/**
 * Represents an immutable byte count. These static factory methods allow easy construction of value
 * objects using either long values like bytes(2034) or megabytes(3):
 * <p>
 * <ul>
 * <li>Bytes.bytes(long)
 * <li>Bytes.kilobytes(long)
 * <li>Bytes.megabytes(long)
 * <li>Bytes.gigabytes(long)
 * <li>Bytes.terabytes(long)
 * </ul>
 * <p>
 * or double precision floating point values like megabytes(3.2):
 * <p>
 * <ul>
 * <li>Bytes.bytes(double)
 * <li>Bytes.kilobytes(double)
 * <li>Bytes.megabytes(double)
 * <li>Bytes.gigabytes(double)
 * <li>Bytes.terabytes(double)
 * </ul>
 * <p>
 * In the case of bytes(double), the value will be rounded off to the nearest integer byte count
 * using Math.round().
 * <p>
 * The precise number of bytes in a Bytes object can be retrieved by calling bytes(). Approximate
 * values for different units can be retrieved as double precision values using these methods:
 * <p>
 * <ul>
 * <li>kilobytes()
 * <li>megabytes()
 * <li>gigabytes()
 * <li>terabytes()
 * </ul>
 * <p>
 * Also, value objects can be constructed from strings, optionally using a Locale with
 * valueOf(String) and valueOf(String,Locale). The string may contain a decimal or floating point
 * number followed by optional whitespace followed by a unit (nothing for bytes, K for kilobyte, M
 * for megabytes, G for gigabytes or T for terabytes) optionally followed by a B (for bytes). Any of
 * these letters can be any case. So, examples of permissible string values are:
 * <p>
 * <ul>
 * <li>37 (37 bytes)
 * <li>2.3K (2.3 kilobytes)
 * <li>2.5 kb (2.5 kilobytes)
 * <li>4k (4 kilobytes)
 * <li>35.2GB (35.2 gigabytes)
 * <li>1024M (1024 megabytes)
 * </ul>
 * <p>
 * Note that if the Locale was not US, the values might substitute "," for "." as that is the custom
 * in Euroland.
 * <p>
 * The toString() and toString(Locale) methods are smart enough to convert a given value object to
 * the most appropriate units for the given value.
 * 
 * @author Jonathan Locke
 */
public final class Bytes extends LongValue
{
	private static final long serialVersionUID = 1L;

	/** Pattern for string parsing. */
	private static final Pattern valuePattern = Pattern.compile(
		"([0-9]+([\\.,][0-9]+)?)\\s*(|K|M|G|T)B?", Pattern.CASE_INSENSITIVE);

	/** Maximum bytes value */
	public static final Bytes MAX = bytes(Long.MAX_VALUE);

	/**
	 * Private constructor forces use of static factory methods.
	 * 
	 * @param bytes
	 *            Number of bytes
	 */
	private Bytes(final long bytes)
	{
		super(bytes);

		if (bytes < 0)
		{
			throw new IllegalArgumentException("'bytes' cannot be negative: " + bytes);
		}
	}

	/**
	 * Instantiate immutable Bytes value object..
	 * 
	 * @param bytes
	 *            Value to convert
	 * @return Input as Bytes
	 */
	public static Bytes bytes(final long bytes)
	{
		return new Bytes(bytes);
	}

	/**
	 * Instantiate immutable Bytes value object..
	 * 
	 * @param kilobytes
	 *            Value to convert
	 * @return Input as Bytes
	 */
	public static Bytes kilobytes(final long kilobytes)
	{
		return bytes(kilobytes * 1024);
	}

	/**
	 * Instantiate immutable Bytes value object..
	 * 
	 * @param megabytes
	 *            Value to convert
	 * @return Input as Bytes
	 */
	public static Bytes megabytes(final long megabytes)
	{
		return kilobytes(megabytes * 1024);
	}

	/**
	 * Instantiate immutable Bytes value object..
	 * 
	 * @param gigabytes
	 *            Value to convert
	 * @return Input as Bytes
	 */
	public static Bytes gigabytes(final long gigabytes)
	{
		return megabytes(gigabytes * 1024);
	}

	/**
	 * Instantiate immutable Bytes value object..
	 * 
	 * @param terabytes
	 *            Value to convert
	 * @return Input as Bytes
	 */
	public static Bytes terabytes(final long terabytes)
	{
		return gigabytes(terabytes * 1024);
	}

	/**
	 * Instantiate immutable Bytes value object..
	 * 
	 * @param bytes
	 *            Value to convert
	 * @return Input as Bytes
	 */
	public static Bytes bytes(final double bytes)
	{
		return bytes(Math.round(bytes));
	}

	/**
	 * Instantiate immutable Bytes value object..
	 * 
	 * @param kilobytes
	 *            Value to convert
	 * @return Input as Bytes
	 */
	public static Bytes kilobytes(final double kilobytes)
	{
		return bytes(kilobytes * 1024.0);
	}

	/**
	 * Instantiate immutable Bytes value object..
	 * 
	 * @param megabytes
	 *            Value to convert
	 * @return Input as Bytes
	 */
	public static Bytes megabytes(final double megabytes)
	{
		return kilobytes(megabytes * 1024.0);
	}

	/**
	 * Instantiate immutable Bytes value object..
	 * 
	 * @param gigabytes
	 *            Value to convert
	 * @return Input as Bytes
	 */
	public static Bytes gigabytes(final double gigabytes)
	{
		return megabytes(gigabytes * 1024.0);
	}

	/**
	 * Instantiate immutable Bytes value object..
	 * 
	 * @param terabytes
	 *            Value to convert
	 * @return Input as Bytes
	 */
	public static Bytes terabytes(final double terabytes)
	{
		return gigabytes(terabytes * 1024.0);
	}

	/**
	 * Gets the byte count represented by this value object.
	 * 
	 * @return Byte count
	 */
	public final long bytes()
	{
		return value;
	}

	/**
	 * Gets the byte count in kilobytes.
	 * 
	 * @return The value in kilobytes
	 */
	public final double kilobytes()
	{
		return value / 1024.0;
	}

	/**
	 * Gets the byte count in megabytes.
	 * 
	 * @return The value in megabytes
	 */
	public final double megabytes()
	{
		return kilobytes() / 1024.0;
	}

	/**
	 * Gets the byte count in gigabytes.
	 * 
	 * @return The value in gigabytes
	 */
	public final double gigabytes()
	{
		return megabytes() / 1024.0;
	}

	/**
	 * Gets the byte count in terabytes.
	 * 
	 * @return The value in terabytes
	 */
	public final double terabytes()
	{
		return gigabytes() / 1024.0;
	}

	/**
	 * Converts a string to a number of bytes. Strings consist of a floating point value followed by
	 * K, M, G or T for kilobytes, megabytes, gigabytes or terabytes, respectively. The
	 * abbreviations KB, MB, GB and TB are also accepted. Matching is case insensitive.
	 * 
	 * @param string
	 *            The string to convert
	 * @param locale
	 *            The Locale to be used for transformation
	 * @return The Bytes value for the string
	 * @throws StringValueConversionException
	 */
	public static Bytes valueOf(final String string, final Locale locale)
		throws StringValueConversionException
	{
		final Matcher matcher = valuePattern.matcher(string);

		// Valid input?
		if (matcher.matches())
		{
			try
			{
				// Get double precision value
				final double value = NumberFormat.getNumberInstance(locale)
					.parse(matcher.group(1))
					.doubleValue();

				// Get units specified
				final String units = matcher.group(3);

				if (units.equalsIgnoreCase(""))
				{
					return bytes(value);
				}
				else if (units.equalsIgnoreCase("K"))
				{
					return kilobytes(value);
				}
				else if (units.equalsIgnoreCase("M"))
				{
					return megabytes(value);
				}
				else if (units.equalsIgnoreCase("G"))
				{
					return gigabytes(value);
				}
				else if (units.equalsIgnoreCase("T"))
				{
					return terabytes(value);
				}
				else
				{
					throw new StringValueConversionException("Units not recognized: " + string);
				}
			}
			catch (ParseException e)
			{
				throw new StringValueConversionException("Unable to parse numeric part: " + string,
					e);
			}
		}
		else
		{
			throw new StringValueConversionException("Unable to parse bytes: " + string);
		}
	}

	/**
	 * Converts a string to a number of bytes. Strings consist of a floating point value followed by
	 * K, M, G or T for kilobytes, megabytes, gigabytes or terabytes, respectively. The
	 * abbreviations KB, MB, GB and TB are also accepted. Matching is case insensitive.
	 * 
	 * @param string
	 *            The string to convert
	 * @return The Bytes value for the string
	 * @throws StringValueConversionException
	 */
	public static Bytes valueOf(final String string) throws StringValueConversionException
	{
		return valueOf(string, Locale.getDefault());
	}

	/**
	 * Converts this byte count to a string using the default locale.
	 * 
	 * @return The string for this byte count
	 */
	@Override
	public String toString()
	{
		return toString(Locale.getDefault());
	}

	/**
	 * Converts this byte count to a string using the given locale.
	 * 
	 * @param locale
	 *            Locale to use for conversion
	 * @return The string for this byte count
	 */
	public String toString(final Locale locale)
	{
		if (terabytes() >= 1.0)
		{
			return unitString(terabytes(), "T", locale);
		}

		if (gigabytes() >= 1.0)
		{
			return unitString(gigabytes(), "G", locale);
		}

		if (megabytes() >= 1.0)
		{
			return unitString(megabytes(), "M", locale);
		}

		if (kilobytes() >= 1.0)
		{
			return unitString(kilobytes(), "K", locale);
		}

		return Long.toString(value) + " bytes";
	}

	/**
	 * Convert value to formatted floating point number and units.
	 * 
	 * @param value
	 *            The value
	 * @param units
	 *            The units
	 * @param locale
	 *            The locale
	 * @return The formatted string
	 */
	private String unitString(final double value, final String units, final Locale locale)
	{
		return StringValue.valueOf(value, locale) + units;
	}

	/**
	 * Compares this <code>Bytes</code> with another <code>Bytes</code> instance.
	 * 
	 * @param other
	 *            the <code>Bytes</code> instance to compare with
	 * @return <code>true</code> if this <code>Bytes</code> is greater than the given
	 *         <code>Bytes</code> instance
	 */
	public boolean greaterThan(final Bytes other)
	{
		if ((this == other) || (other == null))
		{
			return false;
		}
		return bytes() > other.bytes();
	}
}
