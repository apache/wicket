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
package org.apache.wicket.util.value;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.wicket.util.parse.metapattern.MetaPattern;
import org.apache.wicket.util.parse.metapattern.parsers.VariableAssignmentParser;
import org.apache.wicket.util.string.IStringIterator;
import org.apache.wicket.util.string.StringList;
import org.apache.wicket.util.string.StringValue;
import org.apache.wicket.util.string.StringValueConversionException;
import org.apache.wicket.util.time.Duration;
import org.apache.wicket.util.time.Time;


/**
 * A <code>IValueMap</code> implementation that holds values, parses <code>String</code>s, and
 * exposes a variety of convenience methods.
 * <p>
 * In addition to a no-arg constructor and a copy constructor that takes a <code>Map</code>
 * argument, <code>ValueMap</code>s can be constructed using a parsing constructor.
 * <code>ValueMap(String)</code> will parse values from the string in comma separated key/value
 * assignment pairs. For example, <code>new ValueMap("a=9,b=foo")</code>.
 * <p>
 * Values can be retrieved from the <code>ValueMap</code> in the usual way or with methods that do
 * handy conversions to various types, including <code>String</code>, <code>StringValue</code>,
 * <code>int</code>, <code>long</code>, <code>double</code>, <code>Time</code> and
 * <code>Duration</code>.
 * <p>
 * The <code>makeImmutable</code> method will make the underlying <code>Map</code> immutable.
 * Further attempts to change the <code>Map</code> will result in a <code>RuntimeException</code>.
 * <p>
 * The <code>toString</code> method converts a <code>ValueMap</code> object to a readable
 * key/value string for diagnostics.
 * 
 * @author Jonathan Locke
 * @since 1.2.6
 */
public class ValueMap extends HashMap implements IValueMap
{
	/** an empty <code>ValueMap</code>. */
	public static final ValueMap EMPTY_MAP = new ValueMap();

	private static final long serialVersionUID = 1L;

	/**
	 * <code>true</code> if this <code>ValueMap</code> has been made immutable.
	 */
	private boolean immutable = false;

	/**
	 * Constructs empty <code>ValueMap</code>.
	 */
	public ValueMap()
	{
	}

	/**
	 * Copy constructor.
	 * 
	 * @param map
	 *            the <code>ValueMap</code> to copy
	 */
	public ValueMap(final Map map)
	{
		super.putAll(map);
	}

	/**
	 * Constructor.
	 * <p>
	 * NOTE: Please use <code>RequestUtils.decodeParameters()</code> if you wish to properly
	 * decode a request URL.
	 * 
	 * @param keyValuePairs
	 *            list of key/value pairs separated by commas. For example, "<code>param1=foo,param2=bar</code>"
	 */
	public ValueMap(final String keyValuePairs)
	{
		this(keyValuePairs, ",");
	}

	/**
	 * Constructor.
	 * <p>
	 * NOTE: Please use <code>RequestUtils.decodeParameters()</code> if you wish to properly
	 * decode a request URL.
	 * 
	 * @param keyValuePairs
	 *            list of key/value pairs separated by a given delimiter. For example, "<code>param1=foo,param2=bar</code>"
	 *            where delimiter is "<code>,</code>".
	 * @param delimiter
	 *            delimiter <code>String</code> used to separate key/value pairs
	 */
	public ValueMap(final String keyValuePairs, final String delimiter)
	{
		int start = 0;
		int equalsIndex = keyValuePairs.indexOf('=');
		int delimiterIndex = keyValuePairs.indexOf(delimiter, equalsIndex);
		if (delimiterIndex == -1)
		{
			delimiterIndex = keyValuePairs.length();
		}
		while (equalsIndex != -1)
		{
			if (delimiterIndex < keyValuePairs.length())
			{
				int equalsIndex2 = keyValuePairs.indexOf('=', delimiterIndex + 1);
				if (equalsIndex2 != -1)
				{
					int delimiterIndex2 = keyValuePairs.lastIndexOf(delimiter, equalsIndex2);
					delimiterIndex = delimiterIndex2;
				}
				else
				{
					delimiterIndex = keyValuePairs.length();
				}
			}
			String key = keyValuePairs.substring(start, equalsIndex);
			String value = keyValuePairs.substring(equalsIndex + 1, delimiterIndex);
			put(key, value);
			if (delimiterIndex < keyValuePairs.length())
			{
				start = delimiterIndex + 1;
				equalsIndex = keyValuePairs.indexOf('=', start);
				if (equalsIndex != -1)
				{
					delimiterIndex = keyValuePairs.indexOf(delimiter, equalsIndex);
					if (delimiterIndex == -1)
					{
						delimiterIndex = keyValuePairs.length();
					}
				}
			}
			else
			{
				equalsIndex = -1;
			}
		}
	}

	/**
	 * Constructor.
	 * 
	 * @param keyValuePairs
	 *            list of key/value pairs separated by a given delimiter. For example, "<code>param1=foo,param2=bar</code>"
	 *            where delimiter is "<code>,</code>".
	 * @param delimiter
	 *            delimiter string used to separate key/value pairs
	 * @param valuePattern
	 *            pattern for value. To pass a simple regular expression, pass "<code>new MetaPattern(regexp)</code>".
	 */
	public ValueMap(final String keyValuePairs, final String delimiter,
			final MetaPattern valuePattern)
	{
		// Get list of strings separated by the delimiter
		final StringList pairs = StringList.tokenize(keyValuePairs, delimiter);

		// Go through each string in the list
		for (IStringIterator iterator = pairs.iterator(); iterator.hasNext();)
		{
			// Get the next key value pair
			final String pair = iterator.next();

			// Parse using metapattern parser for variable assignments
			final VariableAssignmentParser parser = new VariableAssignmentParser(pair, valuePattern);

			// Does it parse?
			if (parser.matches())
			{
				// Succeeded. Put key and value into map
				put(parser.getKey(), parser.getValue());
			}
			else
			{
				throw new IllegalArgumentException("Invalid key value list: '" + keyValuePairs +
						"'");
			}
		}
	}

	/**
	 * @see java.util.Map#clear()
	 */
	public final void clear()
	{
		checkMutability();
		super.clear();
	}

	/**
	 * @see IValueMap#getBoolean(String)
	 */
	public final boolean getBoolean(final String key) throws StringValueConversionException
	{
		return getStringValue(key).toBoolean();
	}

	/**
	 * @see IValueMap#getDouble(String)
	 */
	public final double getDouble(final String key) throws StringValueConversionException
	{
		return getStringValue(key).toDouble();
	}

	/**
	 * @see IValueMap#getDouble(String, double)
	 */
	public final double getDouble(final String key, final double defaultValue)
			throws StringValueConversionException
	{
		return getStringValue(key).toDouble(defaultValue);
	}

	/**
	 * @see IValueMap#getDuration(String)
	 */
	public final Duration getDuration(final String key) throws StringValueConversionException
	{
		return getStringValue(key).toDuration();
	}

	/**
	 * @see IValueMap#getInt(String)
	 */
	public final int getInt(final String key) throws StringValueConversionException
	{
		return getStringValue(key).toInt();
	}

	/**
	 * @see IValueMap#getInt(String, int)
	 */
	public final int getInt(final String key, final int defaultValue)
			throws StringValueConversionException
	{
		return getStringValue(key).toInt(defaultValue);
	}

	/**
	 * @see IValueMap#getLong(String)
	 */
	public final long getLong(final String key) throws StringValueConversionException
	{
		return getStringValue(key).toLong();
	}

	/**
	 * @see IValueMap#getLong(String, long)
	 */
	public final long getLong(final String key, final long defaultValue)
			throws StringValueConversionException
	{
		return getStringValue(key).toLong(defaultValue);
	}

	/**
	 * @see IValueMap#getString(String, String)
	 */
	public final String getString(final String key, final String defaultValue)
	{
		final String value = getString(key);
		return value != null ? value : defaultValue;
	}

	/**
	 * @see IValueMap#getString(String)
	 */
	public final String getString(final String key)
	{
		final Object o = get(key);
		if (o == null)
		{
			return null;
		}
		else if (o.getClass().isArray() && Array.getLength(o) > 0)
		{
			// if it is an array just get the first value
			final Object arrayValue = Array.get(o, 0);
			if (arrayValue == null)
			{
				return null;
			}
			else
			{
				return arrayValue.toString();
			}

		}
		else
		{
			return o.toString();
		}
	}

	/**
	 * @see IValueMap#getCharSequence(String)
	 */
	public final CharSequence getCharSequence(final String key)
	{
		final Object o = get(key);
		if (o == null)
		{
			return null;
		}
		else if (o.getClass().isArray() && Array.getLength(o) > 0)
		{
			// if it is an array just get the first value
			final Object arrayValue = Array.get(o, 0);
			if (arrayValue == null)
			{
				return null;
			}
			else
			{
				if (arrayValue instanceof CharSequence)
				{
					return (CharSequence)arrayValue;
				}
				return arrayValue.toString();
			}

		}
		else
		{
			if (o instanceof CharSequence)
			{
				return (CharSequence)o;
			}
			return o.toString();
		}
	}

	/**
	 * @see IValueMap#getStringArray(String)
	 */
	public String[] getStringArray(final String key)
	{
		final Object o = get(key);
		if (o == null)
		{
			return null;
		}
		else if (o instanceof String[])
		{
			return (String[])o;
		}
		else if (o.getClass().isArray())
		{
			int length = Array.getLength(o);
			String[] array = new String[length];
			for (int i = 0; i < length; i++)
			{
				final Object arrayValue = Array.get(o, i);
				if (arrayValue != null)
				{
					array[i] = arrayValue.toString();
				}
			}
			return array;
		}
		return new String[] { o.toString() };
	}

	/**
	 * @see IValueMap#getStringValue(String)
	 */
	public StringValue getStringValue(final String key)
	{
		return StringValue.valueOf(getString(key));
	}

	/**
	 * @see IValueMap#getTime(String)
	 */
	public final Time getTime(final String key) throws StringValueConversionException
	{
		return getStringValue(key).toTime();
	}

	/**
	 * @see IValueMap#isImmutable()
	 */
	public final boolean isImmutable()
	{
		return immutable;
	}

	/**
	 * @see IValueMap#makeImmutable()
	 */
	public final IValueMap makeImmutable()
	{
		this.immutable = true;
		return this;
	}

	/**
	 * @see java.util.Map#put(java.lang.Object, java.lang.Object)
	 */
	public Object put(final Object key, final Object value)
	{
		checkMutability();
		return super.put(key, value);
	}

	/**
	 * Adds the value to this <code>ValueMap</code> with the given key. If the key already is in
	 * the <code>ValueMap</code> it will combine the values into a <code>String</code> array,
	 * else it will just store the value itself.
	 * 
	 * @param key
	 *            the key to store the value under
	 * @param value
	 *            the value that must be added/merged to the <code>ValueMap</code>
	 * @return the value itself if there was no previous value, or a <code>String</code> array
	 *         with the combined values
	 */
	public final Object add(final String key, final String value)
	{
		checkMutability();
		final Object o = get(key);
		if (o == null)
		{
			return put(key, value);
		}
		else if (o.getClass().isArray())
		{
			int length = Array.getLength(o);
			String destArray[] = new String[length + 1];
			for (int i = 0; i < length; i++)
			{
				final Object arrayValue = Array.get(o, i);
				if (arrayValue != null)
				{
					destArray[i] = arrayValue.toString();
				}
			}
			destArray[length] = value;

			return put(key, destArray);
		}
		else
		{
			return put(key, new String[] { o.toString(), value });
		}
	}

	/**
	 * @see java.util.Map#putAll(java.util.Map)
	 */
	public void putAll(final Map map)
	{
		checkMutability();
		super.putAll(map);
	}

	/**
	 * @see java.util.Map#remove(java.lang.Object)
	 */
	public Object remove(final Object key)
	{
		checkMutability();
		return super.remove(key);
	}

	/**
	 * @see IValueMap#getKey(String)
	 */
	public String getKey(final String key)
	{
		Iterator iter = this.keySet().iterator();
		while (iter.hasNext())
		{
			Object keyValue = iter.next();
			if (keyValue instanceof String)
			{
				String keyString = (String)keyValue;
				if (key.equalsIgnoreCase(keyString))
				{
					return keyString;
				}
			}
		}
		return null;
	}

	/**
	 * Generates a <code>String</code> representation of this object.
	 * 
	 * @return <code>String</code> representation of this <code>ValueMap</code> consistent with
	 *         the tag-attribute style of markup elements. For example:
	 *         <code>a="x" b="y" c="z"</code>.
	 */
	public String toString()
	{
		final StringBuffer buffer = new StringBuffer();
		for (final Iterator iterator = entrySet().iterator(); iterator.hasNext();)
		{
			final Map.Entry entry = (Map.Entry)iterator.next();
			buffer.append(entry.getKey());
			buffer.append(" = \"");
			final Object value = entry.getValue();
			if (value == null)
			{
				buffer.append("null");
			}
			else if (value.getClass().isArray())
			{
				buffer.append(Arrays.asList((Object[])value));
			}
			else
			{
				buffer.append(value);
			}

			buffer.append("\"");
			if (iterator.hasNext())
			{
				buffer.append(' ');
			}
		}
		return buffer.toString();
	}

	/**
	 * Throws an exception if <code>ValueMap</code> is immutable.
	 */
	private final void checkMutability()
	{
		if (immutable)
		{
			throw new UnsupportedOperationException("Map is immutable");
		}
	}
}
