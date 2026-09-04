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
import java.sql.Time;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.wicket.util.string.StringValue;
import org.apache.wicket.util.string.StringValueConversionException;


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

	@Override
	public boolean containsKey(final Object key)
	{
		return wrapped.containsKey(key);
	}

	@Override
	public boolean containsValue(final Object value)
	{
		return wrapped.containsValue(value);
	}

	@Override
	public Set<Entry<String, Object>> entrySet()
	{
		checkAndCopy();
		return wrapped.entrySet();
	}

	@Override
	public boolean equals(final Object o)
	{
		return wrapped.equals(o);
	}

	@Override
	public int hashCode()
	{
		return wrapped.hashCode();
	}

	@Override
	public Object get(final Object key)
	{
		return wrapped.get(key);
	}

	@Override
	public boolean getBoolean(final String key) throws StringValueConversionException
	{
		return wrapped.getBoolean(key);
	}

	@Override
	public CharSequence getCharSequence(final String key)
	{
		return wrapped.getCharSequence(key);
	}

	@Override
	public double getDouble(final String key) throws StringValueConversionException
	{
		return wrapped.getDouble(key);
	}

	@Override
	public double getDouble(final String key, final double defaultValue)
	{
		return wrapped.getDouble(key, defaultValue);
	}

	@Override
	public Duration getDuration(final String key) throws StringValueConversionException
	{
		return wrapped.getDuration(key);
	}

	@Override
	public int getInt(final String key, final int defaultValue)
	{
		return wrapped.getInt(key, defaultValue);
	}

	@Override
	public int getInt(final String key) throws StringValueConversionException
	{
		return wrapped.getInt(key);
	}

	@Override
	public String getKey(final String key)
	{
		return wrapped.getKey(key);
	}

	@Override
	public long getLong(final String key, final long defaultValue)
	{
		return wrapped.getLong(key, defaultValue);
	}

	@Override
	public long getLong(final String key) throws StringValueConversionException
	{
		return wrapped.getLong(key);
	}

	@Override
	public String getString(final String key, final String defaultValue)
	{
		return wrapped.getString(key, defaultValue);
	}

	@Override
	public String getString(final String key)
	{
		return wrapped.getString(key);
	}

	@Override
	public String[] getStringArray(final String key)
	{
		return wrapped.getStringArray(key);
	}

	@Override
	public StringValue getStringValue(final String key)
	{
		return wrapped.getStringValue(key);
	}

	@Override
	public Instant getInstant(final String key) throws StringValueConversionException
	{
		return wrapped.getInstant(key);
	}

	@Override
	public boolean isEmpty()
	{
		return wrapped.isEmpty();
	}

	@Override
	public boolean isImmutable()
	{
		return false;
	}

	@Override
	public Set<String> keySet()
	{
		checkAndCopy();
		return wrapped.keySet();
	}

	@Override
	public IValueMap makeImmutable()
	{
		return wrapped.makeImmutable();
	}

	@Override
	public Object put(final String key, final Object value)
	{
		checkAndCopy();
		return wrapped.put(key, value);
	}

	@Override
	public void putAll(final Map<? extends String, ?> map)
	{
		checkAndCopy();
		wrapped.putAll(map);
	}

	@Override
	public Object remove(final Object key)
	{
		checkAndCopy();
		return wrapped.remove(key);
	}

	@Override
	public int size()
	{
		return wrapped.size();
	}

	@Override
	public Collection<Object> values()
	{
		return wrapped.values();
	}

	// //
	// // getAs convenience methods
	// //

	@Override
	public Boolean getAsBoolean(final String key)
	{
		return wrapped.getAsBoolean(key);
	}

	@Override
	public boolean getAsBoolean(final String key, final boolean defaultValue)
	{
		return wrapped.getAsBoolean(key, defaultValue);
	}

	@Override
	public Integer getAsInteger(final String key)
	{
		return wrapped.getAsInteger(key);
	}

	@Override
	public int getAsInteger(final String key, final int defaultValue)
	{
		return wrapped.getAsInteger(key, defaultValue);
	}

	@Override
	public Long getAsLong(final String key)
	{
		return wrapped.getAsLong(key);
	}

	@Override
	public long getAsLong(final String key, final long defaultValue)
	{
		return wrapped.getAsLong(key, defaultValue);
	}

	@Override
	public Double getAsDouble(final String key)
	{
		return wrapped.getAsDouble(key);
	}

	@Override
	public double getAsDouble(final String key, final double defaultValue)
	{
		return wrapped.getAsDouble(key, defaultValue);
	}

	@Override
	public Duration getAsDuration(final String key)
	{
		return wrapped.getAsDuration(key);
	}

	@Override
	public Duration getAsDuration(final String key, final Duration defaultValue)
	{
		return wrapped.getAsDuration(key, defaultValue);
	}

	@Override
	public Instant getAsInstant(final String key)
	{
		return wrapped.getAsInstant(key);
	}

	@Override
	public Instant getAsTime(final String key, final Instant defaultValue)
	{
		return wrapped.getAsTime(key, defaultValue);
	}

	@Override
	public <T extends Enum<T>> T getAsEnum(final String key, final Class<T> eClass)
	{
		return wrapped.getAsEnum(key, eClass);
	}

	@Override
	public <T extends Enum<T>> T getAsEnum(final String key, final T defaultValue)
	{
		return wrapped.getAsEnum(key, defaultValue);
	}

	@Override
	public <T extends Enum<T>> T getAsEnum(final String key, final Class<T> eClass,
		final T defaultValue)
	{
		return wrapped.getAsEnum(key, eClass, defaultValue);
	}
}
