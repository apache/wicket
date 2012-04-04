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
package org.apache.wicket.util.string;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import org.apache.wicket.IClusterable;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Objects;
import org.apache.wicket.util.time.Duration;
import org.apache.wicket.util.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Holds an immutable String value and optionally a Locale, with methods to convert to various
 * types. Also provides some handy parsing methods and a variety of static factory methods.
 * <p>
 * Objects can be constructed directly from Strings or by using the valueOf() static factory
 * methods. The repeat() static factory methods provide a way of generating a String value that
 * repeats a given char or String a number of times.
 * <p>
 * Conversions to a wide variety of types can be found in the to*() methods. A generic conversion
 * can be achieved with to(Class).
 * <P>
 * The beforeFirst(), afterFirst(), beforeLast() and afterLast() methods are handy for parsing
 * things like paths and filenames.
 * 
 * @author Jonathan Locke
 */
public class StringValue implements IClusterable
{
	private static final long serialVersionUID = 1L;

	private static final Logger LOG = LoggerFactory.getLogger(StringValue.class);

	/** Locale to be used for formatting and parsing. */
	private final Locale locale;

	/** The underlying string. */
	private final String text;

	/**
	 * @param times
	 *            Number of times to repeat character
	 * @param c
	 *            Character to repeat
	 * @return Repeated character string
	 */
	public static StringValue repeat(final int times, final char c)
	{
		final AppendingStringBuffer buffer = new AppendingStringBuffer(times);

		for (int i = 0; i < times; i++)
		{
			buffer.append(c);
		}

		return valueOf(buffer);
	}

	/**
	 * @param times
	 *            Number of times to repeat string
	 * @param s
	 *            String to repeat
	 * @return Repeated character string
	 */
	public static StringValue repeat(final int times, final String s)
	{
		final AppendingStringBuffer buffer = new AppendingStringBuffer(times);

		for (int i = 0; i < times; i++)
		{
			buffer.append(s);
		}

		return valueOf(buffer);
	}

	/**
	 * Converts the given input to an instance of StringValue.
	 * 
	 * @param value
	 *            Double precision value
	 * @return String value formatted with one place after decimal
	 */
	public static StringValue valueOf(final double value)
	{
		return valueOf(value, Locale.getDefault());
	}

	/**
	 * Converts the given input to an instance of StringValue.
	 * 
	 * @param value
	 *            Double precision value
	 * @param places
	 *            Number of places after decimal
	 * @param locale
	 *            Locale to be used for formatting
	 * @return String value formatted with the given number of places after decimal
	 */
	public static StringValue valueOf(final double value, final int places, final Locale locale)
	{
		if (Double.isNaN(value) || Double.isInfinite(value))
		{
			return valueOf("N/A");
		}
		else
		{
			final DecimalFormat format = new DecimalFormat("#." + repeat(places, '#'),
				new DecimalFormatSymbols(locale));
			return valueOf(format.format(value));
		}
	}

	/**
	 * Converts the given input to an instance of StringValue.
	 * 
	 * @param value
	 *            Double precision value
	 * @param locale
	 *            Locale to be used for formatting
	 * @return String value formatted with one place after decimal
	 */
	public static StringValue valueOf(final double value, final Locale locale)
	{
		return valueOf(value, 1, locale);
	}

	/**
	 * Converts the given input to an instance of StringValue.
	 * 
	 * @param object
	 *            An object
	 * @return String value for object
	 */
	public static StringValue valueOf(final Object object)
	{
		return valueOf(Strings.toString(object));
	}

	/**
	 * Converts the given input to an instance of StringValue.
	 * 
	 * @param object
	 *            An object
	 * @param locale
	 *            Locale to be used for formatting
	 * @return String value for object
	 */
	public static StringValue valueOf(final Object object, final Locale locale)
	{
		return valueOf(Strings.toString(object), locale);
	}

	/**
	 * Converts the given input to an instance of StringValue.
	 * 
	 * @param string
	 *            A string
	 * @return String value for string
	 */
	public static StringValue valueOf(final String string)
	{
		return new StringValue(string);
	}

	/**
	 * Converts the given input to an instance of StringValue.
	 * 
	 * @param string
	 *            A string
	 * @param locale
	 *            Locale to be used for formatting
	 * @return String value for string
	 */
	public static StringValue valueOf(final String string, final Locale locale)
	{
		return new StringValue(string, locale);
	}

	/**
	 * Converts the given input to an instance of StringValue.
	 * 
	 * @param buffer
	 *            A string buffer
	 * @return String value
	 */
	public static StringValue valueOf(final AppendingStringBuffer buffer)
	{
		return valueOf(buffer.toString());
	}

	/**
	 * Private constructor to force use of static factory methods.
	 * 
	 * @param text
	 *            The text for this string value
	 */
	protected StringValue(final String text)
	{
		this.text = text;
		locale = Locale.getDefault();
	}

	/**
	 * Private constructor to force use of static factory methods.
	 * 
	 * @param text
	 *            The text for this string value
	 * @param locale
	 *            the locale for formatting and parsing
	 */
	protected StringValue(final String text, final Locale locale)
	{
		this.text = text;
		this.locale = locale;
	}

	/**
	 * Gets the substring after the first occurrence given char.
	 * 
	 * @param c
	 *            char to scan for
	 * @return the substring
	 */
	public final String afterFirst(final char c)
	{
		return Strings.afterFirst(text, c);
	}

	/**
	 * Gets the substring after the last occurrence given char.
	 * 
	 * @param c
	 *            char to scan for
	 * @return the substring
	 */
	public final String afterLast(final char c)
	{
		return Strings.afterLast(text, c);
	}

	/**
	 * Gets the substring before the first occurrence given char.
	 * 
	 * @param c
	 *            char to scan for
	 * @return the substring
	 */
	public final String beforeFirst(final char c)
	{
		return Strings.beforeFirst(text, c);
	}

	/**
	 * Gets the substring before the last occurrence given char.
	 * 
	 * @param c
	 *            char to scan for
	 * @return the substring
	 */
	public final String beforeLast(final char c)
	{
		return Strings.afterLast(text, c);
	}

	/**
	 * Replaces on this text.
	 * 
	 * @param searchFor
	 *            What to search for
	 * @param replaceWith
	 *            What to replace with
	 * @return This string value with searchFor replaces with replaceWith
	 */
	public final CharSequence replaceAll(final CharSequence searchFor,
		final CharSequence replaceWith)
	{
		return Strings.replaceAll(text, searchFor, replaceWith);
	}

	/**
	 * Converts this StringValue to a given type.
	 * 
	 * @param type
	 *            The type to convert to
	 * @return The converted value
	 * @throws StringValueConversionException
	 */
	public final Object to(final Class<?> type) throws StringValueConversionException
	{
		if (type == null)
		{
			return null;
		}

		if (type == String.class)
		{
			return toString();
		}

		if ((type == Integer.TYPE) || (type == Integer.class))
		{
			return toInteger();
		}

		if ((type == Long.TYPE) || (type == Long.class))
		{
			return toLongObject();
		}

		if ((type == Boolean.TYPE) || (type == Boolean.class))
		{
			return toBooleanObject();
		}

		if ((type == Double.TYPE) || (type == Double.class))
		{
			return toDoubleObject();
		}

		if ((type == Character.TYPE) || (type == Character.class))
		{
			return toCharacter();
		}

		if (type == Time.class)
		{
			return toTime();
		}

		if (type == Duration.class)
		{
			return toDuration();
		}

		if (type.isEnum())
		{
			return (T) toEnum((Class) type);
		}

		throw new StringValueConversionException("Cannot convert '" + toString() + "'to type " +
			type);
	}

	/**
	 * Convert this text to a boolean.
	 * 
	 * @return This string value as a boolean
	 * @throws StringValueConversionException
	 */
	public final boolean toBoolean() throws StringValueConversionException
	{
		return Strings.isTrue(text);
	}

	/**
	 * Convert to boolean, returning default value if text is inconvertible.
	 * 
	 * @param defaultValue
	 *            the default value
	 * @return the converted text as a boolean or the default value if text is empty or inconvertible
	 * @see Strings#isTrue(String) 
	 */
	public final boolean toBoolean(final boolean defaultValue)
	{
		if (text != null)
		{
			try
			{
				return toBoolean();
			}
			catch (StringValueConversionException x)
			{
				if (LOG.isDebugEnabled())
				{
					LOG.debug(String.format(
						"An error occurred while converting '%s' to a boolean: %s", text, x.getMessage()), x);
				}
			}
		}
		return defaultValue;
	}

	/**
	 * Convert this text to a boolean.
	 * 
	 * @return Converted text
	 * @throws StringValueConversionException
	 */
	public final Boolean toBooleanObject() throws StringValueConversionException
	{
		return Strings.toBoolean(text);
	}

	/**
	 * Convert this text to a char.
	 * 
	 * @return This string value as a character
	 * @throws StringValueConversionException
	 */
	public final char toChar() throws StringValueConversionException
	{
		return Strings.toChar(text);
	}

	/**
	 * Convert to character, returning default value if text is inconvertible.
	 * 
	 * @param defaultValue
	 *            the default value
	 * @return the converted text as a primitive char or the default value if text is not a single character
	 */
	public final char toChar(final char defaultValue)
	{
		if (text != null)
		{
			try
			{
				return toChar();
			}
			catch (StringValueConversionException x)
			{
				if (LOG.isDebugEnabled())
				{
					LOG.debug(String.format(
						"An error occurred while converting '%s' to a character: %s", text, x.getMessage()), x);
				}
			}
		}
		return defaultValue;
	}

	/**
	 * Convert this text to a Character.
	 * 
	 * @return Converted text
	 * @throws StringValueConversionException
	 */
	public final Character toCharacter() throws StringValueConversionException
	{
		return toChar();
	}

	/**
	 * Convert this text to a double.
	 * 
	 * @return Converted text
	 * @throws StringValueConversionException
	 */
	public final double toDouble() throws StringValueConversionException
	{
		try
		{
			return NumberFormat.getNumberInstance(locale).parse(text).doubleValue();
		}
		catch (ParseException e)
		{
			throw new StringValueConversionException("Unable to convert '" + text +
				"' to a double value", e);
		}
	}

	/**
	 * Convert to double, returning default value if text is inconvertible.
	 * 
	 * @param defaultValue
	 *            the default value
	 * @return the converted text as a double or the default value if text is empty or inconvertible
	 */
	public final double toDouble(final double defaultValue)
	{
		if (text != null)
		{
			try
			{
				return toDouble();
			}
			catch (Exception x)
			{
				if (LOG.isDebugEnabled())
				{
					LOG.debug(String.format(
						"An error occurred while converting '%s' to a double: %s", text, x.getMessage()), x);
				}
			}
		}
		return defaultValue;
	}

	/**
	 * Convert this text to a Double.
	 * 
	 * @return Converted text
	 * @throws StringValueConversionException
	 */
	public final Double toDoubleObject() throws StringValueConversionException
	{
		return toDouble();
	}

	/**
	 * Convert this text to a Duration instance.
	 * 
	 * @return Converted text
	 * @throws StringValueConversionException
	 * @see Duration#valueOf(String, java.util.Locale) 
	 */
	public final Duration toDuration() throws StringValueConversionException
	{
		return Duration.valueOf(text, locale);
	}

	/**
	 * Convert to duration, returning default value if text is inconvertible.
	 * 
	 * @param defaultValue
	 *            the default value
	 * @return the converted text as a duration or the default value if text is empty or inconvertible
	 * @see Duration#valueOf(String, java.util.Locale) 
	 */
	public final Duration toDuration(final Duration defaultValue)
	{
		if (text != null)
		{
			try
			{
				return toDuration();
			}
			catch (Exception x)
			{
				if (LOG.isDebugEnabled())
				{
					LOG.debug(String.format(
						"An error occurred while converting '%s' to a Duration: %s", text, x.getMessage()), x);
				}
			}
		}
		return defaultValue;
	}

	/**
	 * Convert this text to an int.
	 * 
	 * @return Converted text
	 * @throws StringValueConversionException
	 */
	public final int toInt() throws StringValueConversionException
	{
		try
		{
			return Integer.parseInt(text);
		}
		catch (NumberFormatException e)
		{
			throw new StringValueConversionException("Unable to convert '" + text +
				"' to an int value", e);
		}
	}

	/**
	 * Convert to integer, returning default value if text is inconvertible.
	 * 
	 * @param defaultValue
	 *            the default value
	 * @return the converted text as an integer or the default value if text is not an integer
	 */
	public final int toInt(final int defaultValue)
	{
		if (text != null)
		{
			try
			{
				return toInt();
			}
			catch (StringValueConversionException x)
			{
				if (LOG.isDebugEnabled())
				{
					LOG.debug(String.format(
						"An error occurred while converting '%s' to an integer: %s", text, x.getMessage()), x);
				}
			}
		}
		return defaultValue;
	}

	/**
	 * Convert this text to an Integer.
	 * 
	 * @return Converted text
	 * @throws StringValueConversionException
	 */
	public final Integer toInteger() throws StringValueConversionException
	{
		try
		{
			return new Integer(text);
		}
		catch (NumberFormatException e)
		{
			throw new StringValueConversionException("Unable to convert '" + text +
				"' to an Integer value", e);
		}
	}

	/**
	 * Convert this text to a long.
	 * 
	 * @return Converted text
	 * @throws StringValueConversionException
	 */
	public final long toLong() throws StringValueConversionException
	{
		try
		{
			return Long.parseLong(text);
		}
		catch (NumberFormatException e)
		{
			throw new StringValueConversionException("Unable to convert '" + text +
				"' to a long value", e);
		}
	}

	/**
	 * Convert to long integer, returning default value if text is inconvertible.
	 * 
	 * @param defaultValue
	 *            the default value
	 * @return the converted text as a long integer or the default value if text is empty or inconvertible
	 */
	public final long toLong(final long defaultValue)
	{
		if (text != null)
		{
			try
			{
				return toLong();
			}
			catch (StringValueConversionException x)
			{
				if (LOG.isDebugEnabled())
				{
					LOG.debug(String.format(
						"An error occurred while converting '%s' to a long: %s", text, x.getMessage()), x);
				}
			}
		}
		return defaultValue;
	}

	/**
	 * Convert this text to a Long.
	 * 
	 * @return Converted text
	 * @throws StringValueConversionException
	 */
	public final Long toLongObject() throws StringValueConversionException
	{
		try
		{
			return new Long(text);
		}
		catch (NumberFormatException e)
		{
			throw new StringValueConversionException("Unable to convert '" + text +
				"' to a Long value", e);
		}
	}

	/**
	 * Convert to object types, returning null if text is null or empty.
	 * 
	 * @return converted
	 * @throws StringValueConversionException
	 */
	public final Boolean toOptionalBoolean() throws StringValueConversionException
	{
		return Strings.isEmpty(text) ? null : toBooleanObject();
	}

	/**
	 * Convert to object types, returning null if text is null or empty.
	 * 
	 * @return converted
	 * @throws StringValueConversionException
	 */
	public final Character toOptionalCharacter() throws StringValueConversionException
	{
		return Strings.isEmpty(text) ? null : toCharacter();
	}

	/**
	 * Convert to object types, returning null if text is null or empty.
	 * 
	 * @return converted
	 * @throws StringValueConversionException
	 */
	public final Double toOptionalDouble() throws StringValueConversionException
	{
		return Strings.isEmpty(text) ? null : toDoubleObject();
	}

	/**
	 * Convert to object types, returning null if text is null or empty.
	 * 
	 * @return converted
	 * @throws StringValueConversionException
	 */
	public final Duration toOptionalDuration() throws StringValueConversionException
	{
		return Strings.isEmpty(text) ? null : toDuration();
	}

	/**
	 * Convert to object types, returning null if text is null or empty.
	 * 
	 * @return converted
	 * @throws StringValueConversionException
	 */
	public final Integer toOptionalInteger() throws StringValueConversionException
	{
		return Strings.isEmpty(text) ? null : toInteger();
	}

	/**
	 * Convert to object types, returning null if text is null or empty.
	 * 
	 * @return converted
	 * @throws StringValueConversionException
	 */
	public final Long toOptionalLong() throws StringValueConversionException
	{
		return Strings.isEmpty(text) ? null : toLongObject();
	}

	/**
	 * Convert to object types, returning null if text is null.
	 * 
	 * @return converted
	 */
	public final String toOptionalString()
	{
		return text;
	}

	/**
	 * Convert to object types, returning null if text is null or empty.
	 * 
	 * @return converted
	 * @throws StringValueConversionException
	 */
	public final Time toOptionalTime() throws StringValueConversionException
	{
		return Strings.isEmpty(text) ? null : toTime();
	}

	/**
	 * @return The string value
	 */
	@Override
	public final String toString()
	{
		return text;
	}

	/**
	 * Convert to primitive types, returning default value if text is null.
	 * 
	 * @param defaultValue
	 *            the default value to return of text is null
	 * @return the converted text as a primitive or the default if text is null
	 */
	public final String toString(final String defaultValue)
	{
		return (text == null) ? defaultValue : text;
	}

	/**
	 * Convert this text to a time instance.
	 * 
	 * @return Converted text
	 * @throws StringValueConversionException
	 */
	public final Time toTime() throws StringValueConversionException
	{
		try
		{
			return Time.valueOf(text);
		}
		catch (ParseException e)
		{
			throw new StringValueConversionException("Unable to convert '" + text +
				"' to a Time value", e);
		}
	}

	/**
	 * Convert to time, returning default value if text is inconvertible.
	 * 
	 * @param defaultValue
	 *            the default value
	 * @return the converted text as a time or the default value if text is inconvertible.
	 */
	public final Time toTime(final Time defaultValue)
	{
		if (text != null)
		{
			try
			{
				return toTime();
			}
			catch (StringValueConversionException x)
			{
				if (LOG.isDebugEnabled())
				{
					LOG.debug(String.format(
						"An error occurred while converting '%s' to a Time: %s", text, x.getMessage()), x);
				}
			}
		}
		return defaultValue;
	}

	/**
	 * Convert this text to an enum.
	 *
	 * @param eClass
	 *            enum type
	 * @return The value as an enum
	 * @throws StringValueConversionException
	 */
	public final <T extends Enum<T>> T toEnum(Class<T> eClass)
		throws StringValueConversionException
	{
		return Strings.toEnum(text, eClass);
	}

	/**
	 * Convert this text to an enum.
	 *
	 * @param defaultValue
	 *            This will be returned if there is an error converting the value
	 * @return The value as an enum
	 */
	public final <T extends Enum<T>> T toEnum(final T defaultValue)
	{
		Args.notNull(defaultValue, "defaultValue");
		return toEnum((Class<T>)defaultValue.getClass(), defaultValue);
	}

	/**
	 * Convert this text to an enum.
	 *
	 * @param eClass
	 *            enum type
	 * @param defaultValue
	 *            This will be returned if there is an error converting the value
	 * @return The value as an enum
	 */
	public final <T extends Enum<T>> T toEnum(Class<T> eClass, final T defaultValue)
	{
		if (text != null)
		{
			try
			{
				return toEnum(eClass);
			}
			catch (StringValueConversionException x)
			{
				if (LOG.isDebugEnabled())
				{
					LOG.debug(String.format("An error occurred while converting '%s' to a %s: %s",
						text, eClass, x.getMessage()), x);
				}
			}
		}
		return defaultValue;
	}

	/**
	 * Convert to enum, returning null if text is null or empty.
	 *
	 * @param eClass
	 *            enum type
	 *
	 * @return converted
	 * @throws StringValueConversionException
	 */
	public final <T extends Enum<T>> T toOptionalEnum(Class<T> eClass)
		throws StringValueConversionException
	{
		return Strings.isEmpty(text) ? null : toEnum(eClass);
	}

	/**
	 * Returns whether the text is null.
	 * 
	 * @return <code>true</code> if the text is <code>null</code>, <code>false</code> otherwise.
	 */
	public boolean isNull()
	{
		return text == null;
	}

	/**
	 * Returns whether the text is null or empty
	 * 
	 * @return <code>true</code> if the text is <code>null</code> or
	 *         <code>.trim().length()==0</code>, <code>false</code> otherwise.
	 */
	public boolean isEmpty()
	{
		return Strings.isEmpty(text);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode()
	{
		return Objects.hashCode(locale, text);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(final Object obj)
	{
		if (obj instanceof StringValue)
		{
			StringValue stringValue = (StringValue)obj;
			return Objects.isEqual(text, stringValue.text) &&
				Objects.isEqual(locale, stringValue.locale);
		}
		else
		{
			return false;
		}
	}
}
