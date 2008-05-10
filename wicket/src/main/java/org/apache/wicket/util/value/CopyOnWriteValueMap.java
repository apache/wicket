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
 * immutable <code>Map</code>. That is, the <code>Map</code> may or may not be immutable, but
 * if it is, a copy is made.
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
	public CopyOnWriteValueMap(IValueMap wrapped)
	{
		this.wrapped = wrapped;
	}

	/**
	 * @see java.util.Map#clear()
	 */
	public void clear()
	{
		checkAndCopy();
		wrapped.clear();
	}

	/**
	 * Checks if this <code>IValueMap</code> is immutable. If it is, this method makes a new
	 * <code>IValueMap</code> using the <code>ValueMap</code> copy constructor, and sets it to
	 * be this <code>CopyOnWriteValueMap</code>.
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
	public boolean containsKey(Object key)
	{
		return wrapped.containsKey(key);
	}

	/**
	 * @see java.util.Map#containsValue(Object)
	 */
	public boolean containsValue(Object value)
	{
		return wrapped.containsValue(value);
	}

	/**
	 * @see java.util.Map#entrySet()
	 */
	public Set entrySet()
	{
		checkAndCopy();
		return wrapped.entrySet();
	}

	/**
	 * @see java.util.Map#equals(Object)
	 */
	@Override
    public boolean equals(Object o)
	{
		return wrapped.equals(o);
	}

	/**
	 * @see java.util.Map#get(Object)
	 */
	public Object get(Object key)
	{
		return wrapped.get(key);
	}

	/**
	 * @see IValueMap#getBoolean(String)
	 */
	public boolean getBoolean(String key) throws StringValueConversionException
	{
		return wrapped.getBoolean(key);
	}

	/**
	 * @see IValueMap#getCharSequence(String)
	 */
	public CharSequence getCharSequence(String key)
	{
		return wrapped.getCharSequence(key);
	}

	/**
	 * @see IValueMap#getDouble(String)
	 */
	public double getDouble(String key) throws StringValueConversionException
	{
		return wrapped.getDouble(key);
	}

	/**
	 * @see IValueMap#getDouble(String, double)
	 */
	public double getDouble(String key, double defaultValue) throws StringValueConversionException
	{
		return wrapped.getDouble(key, defaultValue);
	}

	/**
	 * @see IValueMap#getDuration(String)
	 */
	public Duration getDuration(String key) throws StringValueConversionException
	{
		return wrapped.getDuration(key);
	}

	/**
	 * @see IValueMap#getInt(String, int)
	 */
	public int getInt(String key, int defaultValue) throws StringValueConversionException
	{
		return wrapped.getInt(key, defaultValue);
	}

	/**
	 * @see IValueMap#getInt(String)
	 */
	public int getInt(String key) throws StringValueConversionException
	{
		return wrapped.getInt(key);
	}

	/**
	 * @see IValueMap#getKey(String)
	 */
	public String getKey(String key)
	{
		return wrapped.getKey(key);
	}

	/**
	 * @see IValueMap#getLong(String, long)
	 */
	public long getLong(String key, long defaultValue) throws StringValueConversionException
	{
		return wrapped.getLong(key, defaultValue);
	}

	/**
	 * @see IValueMap#getLong(String)
	 */
	public long getLong(String key) throws StringValueConversionException
	{
		return wrapped.getLong(key);
	}

	/**
	 * @see IValueMap#getString(String, String)
	 */
	public String getString(String key, String defaultValue)
	{
		return wrapped.getString(key, defaultValue);
	}

	/**
	 * @see IValueMap#getString(String)
	 */
	public String getString(String key)
	{
		return wrapped.getString(key);
	}

	/**
	 * @see IValueMap#getStringArray(String)
	 */
	public String[] getStringArray(String key)
	{
		return wrapped.getStringArray(key);
	}

	/**
	 * @see IValueMap#getStringValue(String)
	 */
	public StringValue getStringValue(String key)
	{
		return wrapped.getStringValue(key);
	}

	/**
	 * @see IValueMap#getTime(String)
	 */
	public Time getTime(String key) throws StringValueConversionException
	{
		return wrapped.getTime(key);
	}

	/**
	 * @see java.util.Map#isEmpty()
	 */
	public boolean isEmpty()
	{
		return wrapped.isEmpty();
	}

	/**
	 * @see IValueMap#isImmutable()
	 */
	public boolean isImmutable()
	{
		return false;
	}

	/**
	 * @see java.util.Map#keySet()
	 */
	public Set keySet()
	{
		checkAndCopy();
		return wrapped.keySet();
	}

	/**
	 * @see IValueMap#makeImmutable()
	 */
	public IValueMap makeImmutable()
	{
		return wrapped.makeImmutable();
	}

	/**
	 * @see java.util.Map#put(Object, Object)
	 */
	public Object put(Object key, Object value)
	{
		checkAndCopy();
		return wrapped.put(key, value);
	}

	/**
	 * @see java.util.Map#putAll(Map)
	 */
	public void putAll(Map map)
	{
		checkAndCopy();
		wrapped.putAll(map);
	}

	/**
	 * @see java.util.Map#remove(Object)
	 */
	public Object remove(Object key)
	{
		checkAndCopy();
		return wrapped.remove(key);
	}

	/**
	 * @see java.util.Map#size()
	 */
	public int size()
	{
		return wrapped.size();
	}

	/**
	 * @see java.util.Map#values()
	 */
	public Collection values()
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

    ////
    //// getAs convenience methods
    ////

    /**
     * @see IValueMap#getAsBoolean(String)
     *
     */
    public Boolean getAsBoolean(String key)
    {
        return wrapped.getAsBoolean(key);
    }

    /**
     * @see IValueMap#getAsBoolean(String, boolean)
     *
     */
    public boolean getAsBoolean(String key, boolean defaultValue)
    {
        return wrapped.getAsBoolean(key, defaultValue);
    }

    /**
     * @see IValueMap#getAsInteger(String)
     */
    public Integer getAsInteger(String key)
    {
        return wrapped.getAsInteger(key);
    }

    /**
     * @see IValueMap#getAsInteger(String, int)
     */
    public int getAsInteger(String key, int defaultValue)
    {
        return wrapped.getAsInteger(key, defaultValue);
    }

    /**
     * @see IValueMap#getAsLong(String)
     */
    public Long getAsLong(String key)
    {
        return wrapped.getAsLong(key);
    }

    /**
     * @see IValueMap#getAsLong(String, long)
     */
    public long getAsLong(String key, long defaultValue)
    {
        return wrapped.getAsLong(key, defaultValue);
    }

    /**
     * @see IValueMap#getAsDouble(String)
     */
    public Double getAsDouble(String key)
    {
        return wrapped.getAsDouble(key);
    }

    /**
     * @see IValueMap#getAsDouble(String, double)
     */
    public double getAsDouble(String key, double defaultValue)
    {
        return wrapped.getAsDouble(key, defaultValue);
    }

    /**
     * @see IValueMap#getAsDuration(String)
     */
    public Duration getAsDuration(String key)
    {
        return wrapped.getAsDuration(key);
    }

    /**
     * @see IValueMap#getAsDuration(String, Duration)
     */
    public Duration getAsDuration(String key, Duration defaultValue)
    {
        return wrapped.getAsDuration(key, defaultValue);
    }

    /**
     * @see IValueMap#getAsTime(String)
     */
    public Time getAsTime(String key)
    {
        return wrapped.getAsTime(key);
    }

    /**
     * @see IValueMap#getAsTime(String, Time)
     */
    public Time getAsTime(String key, Time defaultValue)
    {
        return wrapped.getAsTime(key, defaultValue);
    }

    /**
     * @see IValueMap#getAsEnum(String, Class<T>)
     */
    public <T extends Enum<T>> T getAsEnum(String key, Class<T> eClass)
    {
        return wrapped.getAsEnum(key, eClass);
    }

    /**
     * @see IValueMap#getAsEnum
     */
    public <T extends Enum<T>> T getAsEnum(String key, T defaultValue)
    {
        return wrapped.getAsEnum(key, defaultValue);
    }

    /**
     * @see IValueMap#getAsEnum(String, Class<T>, T)
     */
    public <T extends Enum<T>> T getAsEnum(String key, Class<T> eClass, T defaultValue)
    {
        return wrapped.getAsEnum(key, eClass, defaultValue);
    }
}