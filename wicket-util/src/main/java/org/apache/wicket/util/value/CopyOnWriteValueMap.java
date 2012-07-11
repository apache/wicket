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

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.wicket.util.string.StringValue;
import org.apache.wicket.util.string.StringValueConversionException;
import org.apache.wicket.util.time.Duration;
import org.apache.wicket.util.time.Time;


/**
 * An implementation of <code>IValueMap</code> that makes a copy when a caller tries to change an
 * immutable <code>Map</code>. That is, the <code>Map</code> may or may not be immutable, but if it
 * is, a copy is made.
 * 
 * @author Johan Compagner
 * @author Doug Donohoe
 * @since 1.2.6
 */
public class CopyOnWriteValueMap implements IValueMap, Serializable
{
	private static final long serialVersionUID = 1L;

	/** the wrapped <code>IValueMap</code> */
	private IValueMap wrapped;

	/**
	 * Constructor.
	 * 
	 * @param wrapped
	 *            the wrapped <code>IValueMap</code>
	 */
	public CopyOnWriteValueMap(final IValueMap wrapped)
	{
		this.wrapped = wrapped;
	}

	/**
	 * @see java.util.Map#clear()
	 */
	@Override
	public void clear()
	{
		checkAndCopy();
		wrapped.clear();
	}

	/**
	 * Checks if this <code>IValueMap</code> is immutable. If it is, this method makes a new
	 * <code>IValueMap</code> using the <code>ValueMap</code> copy constructor, and sets it to be
	 * this <code>CopyOnWriteValueMap</code>.
	 */
	private void checkAndCopy()
	{
		if (wrapped.isImmutable())
		{
			wrapped = new ValueMap(wrapped);
		}
	}

	/**
	 * @see java.util.Map#containsKey(Object)
	 */
	@Override
	public boolean containsKey(final Object key)
	{
		return wrapped.containsKey(key);
	}

	/**
	 * @see java.util.Map#containsValue(Object)
	 */
	@Override
	public boolean containsValue(final Object value)
	{
		return wrapped.containsValue(value);
	}

	/**
	 * @see java.util.Map#entrySet()
	 */
	@Override
	public Set<Entry<String, Object>> entrySet()
	{
		checkAndCopy();
		return wrapped.entrySet();
	}

	/**
	 * @see java.util.Map#equals(Object)
	 */
	@Override
	public boolean equals(final Object o)
	{
		return wrapped.equals(o);
	}

	/**
	 * @see java.util.Map#get(Object)
	 */
	@Override
	public Object get(final Object key)
	{
		return wrapped.get(key);
	}

	/**
	 * @see IValueMap#getBoolean(String)
	 */
	@Override
	public boolean getBoolean(final String key) throws StringValueConversionException
	{
		return wrapped.getBoolean(key);
	}

	/**
	 * @see IValueMap#getCharSequence(String)
	 */
	@Override
	public CharSequence getCharSequence(final String key)
	{
		return wrapped.getCharSequence(key);
	}

	/**
	 * @see IValueMap#getDouble(String)
	 */
	@Override
	public double getDouble(final String key) throws StringValueConversionException
	{
		return wrapped.getDouble(key);
	}

	/**
	 * @see IValueMap#getDouble(String, double)
	 */
	@Override
	public double getDouble(final String key, final double defaultValue)
	{
		return wrapped.getDouble(key, defaultValue);
	}

	/**
	 * @see IValueMap#getDuration(String)
	 */
	@Override
	public Duration getDuration(final String key) throws StringValueConversionException
	{
		return wrapped.getDuration(key);
	}

	/**
	 * @see IValueMap#getInt(String, int)
	 */
	@Override
	public int getInt(final String key, final int defaultValue)
	{
		return wrapped.getInt(key, defaultValue);
	}

	/**
	 * @see IValueMap#getInt(String)
	 */
	@Override
	public int getInt(final String key) throws StringValueConversionException
	{
		return wrapped.getInt(key);
	}

	/**
	 * @see IValueMap#getKey(String)
	 */
	@Override
	public String getKey(final String key)
	{
		return wrapped.getKey(key);
	}

	/**
	 * @see IValueMap#getLong(String, long)
	 */
	@Override
	public long getLong(final String key, final long defaultValue)
	{
		return wrapped.getLong(key, defaultValue);
	}

	/**
	 * @see IValueMap#getLong(String)
	 */
	@Override
	public long getLong(final String key) throws StringValueConversionException
	{
		return wrapped.getLong(key);
	}

	/**
	 * @see IValueMap#getString(String, String)
	 */
	@Override
	public String getString(final String key, final String defaultValue)
	{
		return wrapped.getString(key, defaultValue);
	}

	/**
	 * @see IValueMap#getString(String)
	 */
	@Override
	public String getString(final String key)
	{
		return wrapped.getString(key);
	}

	/**
	 * @see IValueMap#getStringArray(String)
	 */
	@Override
	public String[] getStringArray(final String key)
	{
		return wrapped.getStringArray(key);
	}

	/**
	 * @see IValueMap#getStringValue(String)
	 */
	@Override
	public StringValue getStringValue(final String key)
	{
		return wrapped.getStringValue(key);
	}

	/**
	 * @see IValueMap#getTime(String)
	 */
	@Override
	public Time getTime(final String key) throws StringValueConversionException
	{
		return wrapped.getTime(key);
	}

	/**
	 * @see java.util.Map#isEmpty()
	 */
	@Override
	public boolean isEmpty()
	{
		return wrapped.isEmpty();
	}

	/**
	 * @see IValueMap#isImmutable()
	 */
	@Override
	public boolean isImmutable()
	{
		return false;
	}

	/**
	 * @see java.util.Map#keySet()
	 */
	@Override
	public Set<String> keySet()
	{
		checkAndCopy();
		return wrapped.keySet();
	}

	/**
	 * @see IValueMap#makeImmutable()
	 */
	@Override
	public IValueMap makeImmutable()
	{
		return wrapped.makeImmutable();
	}

	/**
	 * @see java.util.Map#put(Object, Object)
	 */
	@Override
	public Object put(final String key, final Object value)
	{
		checkAndCopy();
		return wrapped.put(key, value);
	}

	/**
	 * @see java.util.Map#putAll(Map)
	 */
	@Override
	public void putAll(final Map<? extends String, ?> map)
	{
		checkAndCopy();
		wrapped.putAll(map);
	}

	/**
	 * @see java.util.Map#remove(Object)
	 */
	@Override
	public Object remove(final Object key)
	{
		checkAndCopy();
		return wrapped.remove(key);
	}

	/**
	 * @see java.util.Map#size()
	 */
	@Override
	public int size()
	{
		return wrapped.size();
	}

	/**
	 * @see java.util.Map#values()
	 */
	@Override
	public Collection<Object> values()
	{
		return wrapped.values();
	}

	/**
	 * @see IValueMap#toString()
	 */
	@Override
	public String toString()
	{
		return super.toString();
	}

	// //
	// // getAs convenience methods
	// //

	/**
	 * @see IValueMap#getAsBoolean(String)
	 * 
	 */
	@Override
	public Boolean getAsBoolean(final String key)
	{
		return wrapped.getAsBoolean(key);
	}

	/**
	 * @see IValueMap#getAsBoolean(String, boolean)
	 * 
	 */
	@Override
	public boolean getAsBoolean(final String key, final boolean defaultValue)
	{
		return wrapped.getAsBoolean(key, defaultValue);
	}

	/**
	 * @see IValueMap#getAsInteger(String)
	 */
	@Override
	public Integer getAsInteger(final String key)
	{
		return wrapped.getAsInteger(key);
	}

	/**
	 * @see IValueMap#getAsInteger(String, int)
	 */
	@Override
	public int getAsInteger(final String key, final int defaultValue)
	{
		return wrapped.getAsInteger(key, defaultValue);
	}

	/**
	 * @see IValueMap#getAsLong(String)
	 */
	@Override
	public Long getAsLong(final String key)
	{
		return wrapped.getAsLong(key);
	}

	/**
	 * @see IValueMap#getAsLong(String, long)
	 */
	@Override
	public long getAsLong(final String key, final long defaultValue)
	{
		return wrapped.getAsLong(key, defaultValue);
	}

	/**
	 * @see IValueMap#getAsDouble(String)
	 */
	@Override
	public Double getAsDouble(final String key)
	{
		return wrapped.getAsDouble(key);
	}

	/**
	 * @see IValueMap#getAsDouble(String, double)
	 */
	@Override
	public double getAsDouble(final String key, final double defaultValue)
	{
		return wrapped.getAsDouble(key, defaultValue);
	}

	/**
	 * @see IValueMap#getAsDuration(String)
	 */
	@Override
	public Duration getAsDuration(final String key)
	{
		return wrapped.getAsDuration(key);
	}

	/**
	 * @see IValueMap#getAsDuration(String, Duration)
	 */
	@Override
	public Duration getAsDuration(final String key, final Duration defaultValue)
	{
		return wrapped.getAsDuration(key, defaultValue);
	}

	/**
	 * @see IValueMap#getAsTime(String)
	 */
	@Override
	public Time getAsTime(final String key)
	{
		return wrapped.getAsTime(key);
	}

	/**
	 * @see IValueMap#getAsTime(String, Time)
	 */
	@Override
	public Time getAsTime(final String key, final Time defaultValue)
	{
		return wrapped.getAsTime(key, defaultValue);
	}

	/**
	 * @see IValueMap#getAsEnum(String, Class)
	 */
	@Override
	public <T extends Enum<T>> T getAsEnum(final String key, final Class<T> eClass)
	{
		return wrapped.getAsEnum(key, eClass);
	}

	/**
	 * @see IValueMap#getAsEnum
	 */
	@Override
	public <T extends Enum<T>> T getAsEnum(final String key, final T defaultValue)
	{
		return wrapped.getAsEnum(key, defaultValue);
	}

	/**
	 * @see IValueMap#getAsEnum(String, Class, Enum)
	 */
	@Override
	public <T extends Enum<T>> T getAsEnum(final String key, final Class<T> eClass,
		final T defaultValue)
	{
		return wrapped.getAsEnum(key, eClass, defaultValue);
	}
}