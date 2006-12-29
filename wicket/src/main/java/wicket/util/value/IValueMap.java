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
package wicket.util.value;

import java.util.Map;

import wicket.util.string.StringValue;
import wicket.util.string.StringValueConversionException;
import wicket.util.time.Duration;
import wicket.util.time.Time;


/**
 * A Map interface that holds values, parses strings and exposes a variety of
 * convenience methods.
 * 
 * @author jcompagner
 */
public interface IValueMap extends Map<String, Object>
{
	/**
	 * @see java.util.Map#clear()
	 */
	void clear();

	/**
	 * Gets a boolean value by key.
	 * 
	 * @param key
	 *            The key
	 * @return The value
	 * @throws StringValueConversionException
	 */
	boolean getBoolean(final String key) throws StringValueConversionException;

	/**
	 * Gets a double value by key.
	 * 
	 * @param key
	 *            The key
	 * @return The value
	 * @throws StringValueConversionException
	 */
	double getDouble(final String key) throws StringValueConversionException;

	/**
	 * Gets a double using a default if not found.
	 * 
	 * @param key
	 *            The key
	 * @param defaultValue
	 *            Value to use if no value in map
	 * @return The value
	 * @throws StringValueConversionException
	 */
	double getDouble(final String key, final double defaultValue)
			throws StringValueConversionException;

	/**
	 * Gets a duration.
	 * 
	 * @param key
	 *            The key
	 * @return The value
	 * @throws StringValueConversionException
	 */
	Duration getDuration(final String key) throws StringValueConversionException;

	/**
	 * Gets an int.
	 * 
	 * @param key
	 *            The key
	 * @return The value
	 * @throws StringValueConversionException
	 */
	int getInt(final String key) throws StringValueConversionException;

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
	int getInt(final String key, final int defaultValue) throws StringValueConversionException;

	/**
	 * Gets a long.
	 * 
	 * @param key
	 *            The key
	 * @return The value
	 * @throws StringValueConversionException
	 */
	long getLong(final String key) throws StringValueConversionException;

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
	long getLong(final String key, final long defaultValue) throws StringValueConversionException;

	/**
	 * Gets a string by key.
	 * 
	 * @param key
	 *            The get
	 * @param defaultValue
	 *            Default value to return if value is null
	 * @return The string
	 */
	String getString(final String key, final String defaultValue);

	/**
	 * Gets a string by key.
	 * 
	 * @param key
	 *            The get
	 * @return The string
	 */
	String getString(final String key);

	/**
	 * Gets a string by key.
	 * 
	 * @param key
	 *            The get
	 * @return The string
	 */
	CharSequence getCharSequence(final String key);

	/**
	 * Gets a String array by key. If the value was a String[] it will be
	 * returned directly. If it was a String it will be converted to a String
	 * array of one. If it was an array of another type a String array will be
	 * made and the elements will be converted to a string.
	 * 
	 * @param key
	 * @return The String array of that key
	 */
	String[] getStringArray(final String key);

	/**
	 * Gets a StringValue by key.
	 * 
	 * @param key
	 *            The key
	 * @return The string value object
	 */
	StringValue getStringValue(final String key);

	/**
	 * Gets a time.
	 * 
	 * @param key
	 *            The key
	 * @return The value
	 * @throws StringValueConversionException
	 */
	Time getTime(final String key) throws StringValueConversionException;

	/**
	 * Gets whether this value map is made immutable.
	 * 
	 * @return whether this value map is made immutable
	 */
	boolean isImmutable();

	/**
	 * Makes this value map immutable by changing the underlying map
	 * representation to a collections "unmodifiableMap". After calling this
	 * method, any attempt to modify this map will result in a runtime exception
	 * being thrown by the collections classes.
	 * 
	 * @return this
	 */
	IValueMap makeImmutable();

	/**
	 * @see java.util.Map#put(K, V)
	 */
	Object put(final String key, final Object value);

	/**
	 * @see java.util.Map#putAll(java.util.Map)
	 */
	void putAll(final Map<? extends String, ? extends Object> map);

	/**
	 * @see java.util.Map#remove(java.lang.Object)
	 */
	Object remove(final Object key);

	/**
	 * Provided the hash key is a string and you need to access the value
	 * ignoring ignoring the keys case (upper or lower letter), than you may use
	 * this method to get the correct writing.
	 * 
	 * @param key
	 * @return The key with the correct writing
	 */
	String getKey(final String key);
}