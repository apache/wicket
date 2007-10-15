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

import java.util.Map;

import org.apache.wicket.util.string.StringValue;
import org.apache.wicket.util.string.StringValueConversionException;
import org.apache.wicket.util.time.Duration;
import org.apache.wicket.util.time.Time;


/**
 * A <code>Map</code> interface that holds values, parses <code>String</code>s, and exposes a
 * variety of convenience methods.
 * 
 * @author Johan Compagner
 * @since 1.2.6
 */
public interface IValueMap extends Map
{
	/**
	 * @see java.util.Map#clear()
	 */
	void clear();

	/**
	 * Retrieves a <code>boolean</code> value by key.
	 * 
	 * @param key
	 *            the key
	 * @return the value
	 * @throws StringValueConversionException
	 */
	boolean getBoolean(final String key) throws StringValueConversionException;

	/**
	 * Retrieves a <code>double</code> value by key.
	 * 
	 * @param key
	 *            the key
	 * @return the value
	 * @throws StringValueConversionException
	 */
	double getDouble(final String key) throws StringValueConversionException;

	/**
	 * Retrieves a <code>double</code> value by key, using a default value if not found.
	 * 
	 * @param key
	 *            the key
	 * @param defaultValue
	 *            value to use if no value is in this <code>IValueMap</code>
	 * @return the value
	 * @throws StringValueConversionException
	 */
	double getDouble(final String key, final double defaultValue)
			throws StringValueConversionException;

	/**
	 * Retrieves a <code>Duration</code> by key.
	 * 
	 * @param key
	 *            the key
	 * @return the <code>Duration</code> value
	 * @throws StringValueConversionException
	 */
	Duration getDuration(final String key) throws StringValueConversionException;

	/**
	 * Retrieves an <code>int</code> value by key.
	 * 
	 * @param key
	 *            the key
	 * @return the value
	 * @throws StringValueConversionException
	 */
	int getInt(final String key) throws StringValueConversionException;

	/**
	 * Retrieves an <code>int</code> value by key, using a default value if not found.
	 * 
	 * @param key
	 *            the key
	 * @param defaultValue
	 *            value to use if no value is in this <code>IValueMap</code>
	 * @return the value
	 * @throws StringValueConversionException
	 */
	int getInt(final String key, final int defaultValue) throws StringValueConversionException;

	/**
	 * Retrieves a <code>long</code> value by key.
	 * 
	 * @param key
	 *            the key
	 * @return the value
	 * @throws StringValueConversionException
	 */
	long getLong(final String key) throws StringValueConversionException;

	/**
	 * Retrieves a <code>long</code> value by key, using a default value if not found.
	 * 
	 * @param key
	 *            the key
	 * @param defaultValue
	 *            value to use if no value in this <code>IValueMap</code>
	 * @return the value
	 * @throws StringValueConversionException
	 */
	long getLong(final String key, final long defaultValue) throws StringValueConversionException;

	/**
	 * Retrieves a <code>String</code> by key, using a default value if not found.
	 * 
	 * @param key
	 *            the key
	 * @param defaultValue
	 *            default value to return if value is <code>null</code>
	 * @return the <code>String</code>
	 */
	String getString(final String key, final String defaultValue);

	/**
	 * Retrieves a <code>String</code> by key.
	 * 
	 * @param key
	 *            the key
	 * @return the <code>String</code>
	 */
	String getString(final String key);

	/**
	 * Retrieves a <code>CharSequence</code> by key.
	 * 
	 * @param key
	 *            the key
	 * @return the <code>CharSequence</code>
	 */
	CharSequence getCharSequence(final String key);

	/**
	 * Retrieves a <code>String</code> array by key. If the value was a <code>String[]</code> it
	 * will be returned directly. If it was a <code>String</code> it will be converted to a
	 * <code>String</code> array of length one. If it was an array of another type, a
	 * <code>String</code> array will be made and each element will be converted to a
	 * <code>String</code>.
	 * 
	 * @param key
	 *            the key
	 * @return the <code>String</code> array of that key
	 */
	String[] getStringArray(final String key);

	/**
	 * Retrieves a <code>StringValue</code> object by key.
	 * 
	 * @param key
	 *            the key
	 * @return the <code>StringValue</code> object
	 */
	StringValue getStringValue(final String key);

	/**
	 * Retrieves a <code>Time</code> object by key.
	 * 
	 * @param key
	 *            the key
	 * @return the <code>Time</code> object
	 * @throws StringValueConversionException
	 */
	Time getTime(final String key) throws StringValueConversionException;

	/**
	 * Returns whether or not this <code>IValueMap</code> is immutable.
	 * 
	 * @return whether or not this <code>IValueMap</code> is immutable
	 */
	boolean isImmutable();

	/**
	 * Makes this <code>IValueMap</code> immutable by changing the underlying map representation
	 * to a <code>Collections.unmodifiableMap</code>. After calling this method, any attempt to
	 * modify this <code>IValueMap</code> will result in a <code>RuntimeException</code> being
	 * thrown by the <code>Collections</code> framework.
	 * 
	 * @return this <code>IValueMap</code>
	 */
	IValueMap makeImmutable();

	/**
	 * @see java.util.Map#put(Object, Object)
	 */
	Object put(final Object key, final Object value);

	/**
	 * @see java.util.Map#putAll(java.util.Map)
	 */
	void putAll(final Map map);

	/**
	 * @see java.util.Map#remove(java.lang.Object)
	 */
	Object remove(final Object key);

	/**
	 * Provided that the hash key is a <code>String</code> and you need to access the value
	 * ignoring the key's case (upper- or lowercase letters), then you may use this method to get
	 * the correct writing.
	 * 
	 * @param key
	 *            the key
	 * @return the key with the correct writing
	 */
	String getKey(final String key);
}