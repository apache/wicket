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
public class ValueMap extends HashMap<String, Object> implements IValueMap
{
	/** An empty ValueMap. */
	public static final ValueMap EMPTY_MAP = new ValueMap(0).makeImmutable();

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
	 * Constructs empty value map.
	 * 
     * @param  initialCapacity the initial capacity.
	 */
	public ValueMap(int initialCapacity)
	{
		super(initialCapacity);
	}

	/**
	 * Copy constructor.
	 * 
	 * @param map
	 *            The map to copy
	 */
	public ValueMap(final Map<? extends String, ? extends Object> map)
	{
		super((int) (map.size()/0.75)+1);
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
	 * @see wicket.util.value.IValueMap#clear()
	 */
	@Override
	public final void clear()
	{
		checkMutability();
		super.clear();
	}

	/**
	 * @see wicket.util.value.IValueMap#getBoolean(java.lang.String)
	 */
	public final boolean getBoolean(final String key) throws StringValueConversionException
	{
		return getStringValue(key).toBoolean();
	}

	/**
	 * @see wicket.util.value.IValueMap#getDouble(java.lang.String)
	 */
	public final double getDouble(final String key) throws StringValueConversionException
	{
		return getStringValue(key).toDouble();
	}

	/**
	 * @see wicket.util.value.IValueMap#getDuration(java.lang.String)
	 */
	public final Duration getDuration(final String key) throws StringValueConversionException
	{
		return getStringValue(key).toDuration();
	}

	/**
	 * @see wicket.util.value.IValueMap#getInt(java.lang.String)
	 */
	public final int getInt(final String key) throws StringValueConversionException
	{
		return getStringValue(key).toInt();
	}

	/**
	 * @see wicket.util.value.IValueMap#getInt(java.lang.String, int)
	 */
	public final int getInt(final String key, final int defaultValue)
			throws StringValueConversionException
	{
		return getStringValue(key).toInt(defaultValue);
	}

	/**
	 * @see wicket.util.value.IValueMap#getLong(java.lang.String)
	 */
	public final long getLong(final String key) throws StringValueConversionException
	{
		return getStringValue(key).toLong();
	}

	/**
	 * @see wicket.util.value.IValueMap#getLong(java.lang.String, long)
	 */
	public final long getLong(final String key, final long defaultValue)
			throws StringValueConversionException
	{
		return getStringValue(key).toLong(defaultValue);
	}

	/**
	 * @see wicket.util.value.IValueMap#getString(java.lang.String, java.lang.String)
	 */
	public final String getString(final String key, final String defaultValue)
	{
		final String value = getString(key);
		return value != null ? value : defaultValue;
	}

	/**
	 * @see wicket.util.value.IValueMap#getString(java.lang.String)
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
	 * @see wicket.util.value.IValueMap#getCharSequence(java.lang.String)
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
	 * @see wicket.util.value.IValueMap#getStringArray(java.lang.String)
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
	 * @see wicket.util.value.IValueMap#getStringValue(java.lang.String)
	 */
	public StringValue getStringValue(final String key)
	{
		return StringValue.valueOf(getString(key));
	}

	/**
	 * @see wicket.util.value.IValueMap#getTime(java.lang.String)
	 */
	public final Time getTime(final String key) throws StringValueConversionException
	{
		return getStringValue(key).toTime();
	}

	/**
	 * @see wicket.util.value.IValueMap#isImmutable()
	 */
	public final boolean isImmutable()
	{
		return immutable;
	}

	/**
	 * @see wicket.util.value.IValueMap#makeImmutable()
	 */
	public final ValueMap makeImmutable()
	{
		this.immutable = true;
		return this;
	}

	/**
	 * @see wicket.util.value.IValueMap#put(java.lang.String, java.lang.Object)
	 */
	@Override
	public Object put(final String key, final Object value)
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
	 * @see wicket.util.value.IValueMap#putAll(java.util.Map)
	 */
	@Override
	public void putAll(final Map<? extends String, ? extends Object> map)
	{
		checkMutability();
		super.putAll(map);
	}

	/**
	 * @see wicket.util.value.IValueMap#remove(java.lang.Object)
	 */
	@Override
	public Object remove(final Object key)
	{
		checkMutability();
		return super.remove(key);
	}

	/**
	 * @see wicket.util.value.IValueMap#getKey(java.lang.String)
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
	@Override
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
