/*
 * $Id$ $Revision$
 * $Date$
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
package wicket.util.value;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import wicket.util.parse.metapattern.MetaPattern;
import wicket.util.parse.metapattern.parsers.VariableAssignmentParser;
import wicket.util.string.IStringIterator;
import wicket.util.string.StringList;
import wicket.util.string.StringValue;
import wicket.util.string.StringValueConversionException;
import wicket.util.time.Duration;
import wicket.util.time.Time;

/**
 * A Map implementation that holds values, parses strings and exposes a variety
 * of convenience methods.
 * <p>
 * In addition to a no-arg constructor and a copy constructor that takes a Map
 * argument, ValueMaps can be constructed using a parsing constructor.
 * ValueMap(String) will parse values from the string in comma separated
 * key/value assignment pairs. For example, new ValueMap("a=9,b=foo").
 * <p>
 * Values can be retrieved from the map in the usual way or with methods that do
 * handy conversions to various types, including String, StringValue, int, long,
 * double, Time and Duration.
 * <p>
 * The makeImmutable method will make the underlying map immutable. Further
 * attempts to change the map will result in a runtime exception.
 * <p>
 * The toString() method converts a ValueMap object to a readable key/value
 * string for diagnostics.
 * 
 * @author Jonathan Locke
 */
public class ValueMap extends HashMap
{
	/** An empty ValueMap. */
	public static final ValueMap EMPTY_MAP = new ValueMap();

	private static final long serialVersionUID = 1L;

	/** True if this value map has been made immutable. */
	private boolean immutable = false;

	/**
	 * Constructs empty value map.
	 */
	public ValueMap()
	{
	}

	/**
	 * Copy constructor.
	 * 
	 * @param map
	 *            The map to copy
	 */
	public ValueMap(final Map map)
	{
		super.putAll(map);
	}

	/**
	 * Constructor.
	 * 
	 * @param keyValuePairs
	 *            List of key value pairs separated by commas. For example,
	 *            "param1=foo,param2=bar"
	 */
	public ValueMap(final String keyValuePairs)
	{
		this(keyValuePairs, ",");
	}

	/**
	 * Constructor.
	 * 
	 * @param keyValuePairs
	 *            List of key value pairs separated by a given delimiter. For
	 *            example, "param1=foo,param2=bar" where delimiter is ",".
	 * @param delimiter
	 *            Delimiter string used to separate key/value pairs
	 */
	public ValueMap(final String keyValuePairs, final String delimiter)
	{
		int start = 0;
		int equalsIndex = keyValuePairs.indexOf('=');
		int delimiterIndex = keyValuePairs.indexOf(delimiter,equalsIndex);
		if(delimiterIndex == -1) delimiterIndex = keyValuePairs.length();
		while(equalsIndex != -1)
		{
			if(delimiterIndex < keyValuePairs.length())
			{
				int equalsIndex2 = keyValuePairs.indexOf('=', delimiterIndex+1);
				if(equalsIndex2 != -1)
				{
					int delimiterIndex2 = keyValuePairs.lastIndexOf(delimiter, equalsIndex2);
					delimiterIndex = delimiterIndex2;
				}
				else
				{
					delimiterIndex = keyValuePairs.length();
				}
			}
			String key = keyValuePairs.substring(start,equalsIndex);
			String value = keyValuePairs.substring(equalsIndex+1, delimiterIndex);
			put(key,value);
			if(delimiterIndex < keyValuePairs.length())
			{
				start = delimiterIndex+1;
				equalsIndex = keyValuePairs.indexOf('=',start);
				if(equalsIndex != -1)
				{
					delimiterIndex = keyValuePairs.indexOf(delimiter,equalsIndex);
					if(delimiterIndex == -1) delimiterIndex = keyValuePairs.length();
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
	 *            List of key value pairs separated by a given delimiter. For
	 *            example, "param1=foo,param2=bar" where delimiter is ",".
	 * @param delimiter
	 *            Delimiter string used to separate key/value pairs
	 * @param valuePattern
	 *            Pattern for value. To pass a simple regular expression pass
	 *            "new MetaPattern(regexp)".
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
				throw new IllegalArgumentException("Invalid key value list: '" + keyValuePairs
						+ "'");
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
	 * Gets a boolean value by key.
	 * 
	 * @param key
	 *            The key
	 * @return The value
	 * @throws StringValueConversionException
	 */
	public final boolean getBoolean(final String key) throws StringValueConversionException
	{
		return getStringValue(key).toBoolean();
	}

	/**
	 * Gets a double value by key.
	 * 
	 * @param key
	 *            The key
	 * @return The value
	 * @throws StringValueConversionException
	 */
	public final double getDouble(final String key) throws StringValueConversionException
	{
		return getStringValue(key).toDouble();
	}

	/**
	 * Gets a duration.
	 * 
	 * @param key
	 *            The key
	 * @return The value
	 * @throws StringValueConversionException
	 */
	public final Duration getDuration(final String key) throws StringValueConversionException
	{
		return getStringValue(key).toDuration();
	}

	/**
	 * Gets an int.
	 * 
	 * @param key
	 *            The key
	 * @return The value
	 * @throws StringValueConversionException
	 */
	public final int getInt(final String key) throws StringValueConversionException
	{
		return getStringValue(key).toInt();
	}

	/**
	 * Gets an int, using a default if not found.
	 * 
	 * @param key
	 *            The key
	 * @param defaultValue
	 *            Value to use if no value in map
	 * @return The value
	 * @throws StringValueConversionException
	 */
	public final int getInt(final String key, final int defaultValue)
			throws StringValueConversionException
	{
		return getStringValue(key).toInt(defaultValue);
	}

	/**
	 * Gets a long.
	 * 
	 * @param key
	 *            The key
	 * @return The value
	 * @throws StringValueConversionException
	 */
	public final long getLong(final String key) throws StringValueConversionException
	{
		return getStringValue(key).toLong();
	}

	/**
	 * Gets a long using a default if not found.
	 * 
	 * @param key
	 *            The key
	 * @param defaultValue
	 *            Value to use if no value in map
	 * @return The value
	 * @throws StringValueConversionException
	 */
	public final long getLong(final String key, final long defaultValue)
			throws StringValueConversionException
	{
		return getStringValue(key).toLong(defaultValue);
	}

	/**
	 * Gets a string by key.
	 * 
	 * @param key
	 *            The get
	 * @param defaultValue
	 *            Default value to return if value is null
	 * @return The string
	 */
	public final String getString(final String key, final String defaultValue)
	{
		final String value = getString(key);
		return value != null ? value : defaultValue;
	}

	/**
	 * Gets a string by key.
	 * 
	 * @param key
	 *            The get
	 * @return The string
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
	 * Gets a string by key.
	 * 
	 * @param key
	 *            The get
	 * @return The string
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
	 * Gets a String array by key. If the value was a String[] it will be
	 * returned directly. If it was a String it will be converted to a String
	 * array of one. If it was an array of another type a String array will be
	 * made and the elements will be converted to a string.
	 * 
	 * @param key
	 * @return The String array of that key
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
	 * Gets a StringValue by key.
	 * 
	 * @param key
	 *            The key
	 * @return The string value object
	 */
	public StringValue getStringValue(final String key)
	{
		return StringValue.valueOf(getString(key));
	}

	/**
	 * Gets a time.
	 * 
	 * @param key
	 *            The key
	 * @return The value
	 * @throws StringValueConversionException
	 */
	public final Time getTime(final String key) throws StringValueConversionException
	{
		return getStringValue(key).toTime();
	}

	/**
	 * Gets whether this value map is made immutable.
	 * 
	 * @return whether this value map is made immutable
	 */
	public final boolean isImmutable()
	{
		return immutable;
	}

	/**
	 * Makes this value map immutable by changing the underlying map
	 * representation to a collections "unmodifiableMap". After calling this
	 * method, any attempt to modify this map will result in a runtime exception
	 * being thrown by the collections classes.
	 */
	public final void makeImmutable()
	{
		this.immutable = true;
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
	 * This methods adds the value to this map under the given key If the key
	 * already is in the map it will combine the values into a String array else
	 * it will just store the value itself
	 * 
	 * @param key
	 *            The key to store the value under.
	 * @param value
	 *            The value that must be added/merged to the map
	 * @return The value itself if there was no previous value or a string array
	 *         with the combined values.
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
	 * Provided the hash key is a string and you need to access the value
	 * ignoring ignoring the keys case (upper or lower letter), than you may use
	 * this method to get the correct writing.
	 * 
	 * @param key
	 * @return The key with the correct writing
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
	 * Gets a string representation of this object
	 * 
	 * @return String representation of map consistent with tag attribute style
	 *         of markup elements, for example: a="x" b="y" c="z"
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
	 * Throw exception if map is immutable.
	 */
	private final void checkMutability()
	{
		if (immutable)
		{
			throw new UnsupportedOperationException("Map is immutable");
		}
	}
}
