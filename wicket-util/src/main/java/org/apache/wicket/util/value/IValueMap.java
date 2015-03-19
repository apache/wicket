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
 * @author Doug Donohoe
 * @since 1.2.6
 */
public interface IValueMap extends Map<String, Object>
{
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
	 * Makes this <code>IValueMap</code> immutable by changing the underlying map representation to
	 * a <code>Collections.unmodifiableMap</code>. After calling this method, any attempt to modify
	 * this <code>IValueMap</code> will result in a <code>RuntimeException</code> being thrown by
	 * the <code>Collections</code> framework.
	 * 
	 * @return this <code>IValueMap</code>
	 */
	IValueMap makeImmutable();

	/**
	 * Provided that the hash key is a <code>String</code> and you need to access the value ignoring
	 * the key's case (upper- or lowercase letters), then you may use this method to get the correct
	 * writing.
	 * 
	 * @param key
	 *            the key
	 * @return the key with the correct writing
	 */
	String getKey(final String key);

	// //
	// // getAs convenience methods
	// //

	/**
	 * Retrieves a <code>Boolean</code> value by key.
	 * 
	 * @param key
	 *            the key
	 * 
	 * @return the value or null if value is not a valid boolean or no value is in this
	 *         <code>IValueMap</code>
	 * 
	 */
	Boolean getAsBoolean(String key);

	/**
	 * Retrieves a <code>boolean</code> value by key.
	 * 
	 * @param key
	 *            the key
	 * 
	 * @param defaultValue
	 *            the default to return
	 * 
	 * @return the value or defaultValue if value is not a valid boolean or no value is in this
	 *         <code>IValueMap</code>
	 * 
	 */
	boolean getAsBoolean(String key, boolean defaultValue);

	/**
	 * Retrieves an <code>Integer</code> value by key.
	 * 
	 * @param key
	 *            the key
	 * 
	 * @return the value or null if value is not a valid integer or no value is in this
	 *         <code>IValueMap</code>
	 * 
	 */
	Integer getAsInteger(String key);

	/**
	 * Retrieves an <code>integer</code> value by key.
	 * 
	 * @param key
	 *            the key
	 * 
	 * @param defaultValue
	 *            the default to return
	 * 
	 * @return the value or defaultValue if value is not a valid integer or no value is in this
	 *         <code>IValueMap</code>
	 * 
	 */
	int getAsInteger(String key, int defaultValue);

	/**
	 * Retrieves a <code>Long</code> value by key.
	 * 
	 * @param key
	 *            the key
	 * 
	 * @return the value or null if value is not a valid long or no value is in this
	 *         <code>IValueMap</code>
	 * 
	 */
	Long getAsLong(String key);

	/**
	 * Retrieves a <code>long</code> value by key.
	 * 
	 * @param key
	 *            the key
	 * 
	 * @param defaultValue
	 *            the default to return
	 * 
	 * @return the value or defaultValue if value is not a valid long or no value is in this
	 *         <code>IValueMap</code>
	 * 
	 */
	long getAsLong(String key, long defaultValue);

	/**
	 * Retrieves a <code>Double</code> value by key.
	 * 
	 * @param key
	 *            the key
	 * 
	 * @return the value or null if value is not a valid double or no value is in this
	 *         <code>IValueMap</code>
	 * 
	 */
	Double getAsDouble(String key);

	/**
	 * Retrieves a <code>double</code> value by key.
	 * 
	 * @param key
	 *            the key
	 * 
	 * @param defaultValue
	 *            the default to return
	 * 
	 * @return the value or defaultValue if value is not a valid double or no value is in this
	 *         <code>IValueMap</code>
	 * 
	 */
	double getAsDouble(String key, double defaultValue);

	/**
	 * Retrieves a <code>Duration</code> value by key.
	 * 
	 * @param key
	 *            the key
	 * 
	 * @return the value or null if value is not a valid Duration or no value is in this
	 *         <code>IValueMap</code>
	 * 
	 */
	Duration getAsDuration(String key);

	/**
	 * Retrieves a <code>Duration</code> value by key.
	 * 
	 * @param key
	 *            the key
	 * 
	 * @param defaultValue
	 *            the default to return
	 * 
	 * @return the value or defaultValue if value is not a valid Duration or no value is in this
	 *         <code>IValueMap</code>
	 * 
	 */
	Duration getAsDuration(String key, Duration defaultValue);

	/**
	 * Retrieves a <code>Time</code> value by key.
	 * 
	 * @param key
	 *            the key
	 * 
	 * @return the value or null if value is not a valid Time or no value is in this
	 *         <code>IValueMap</code>
	 * 
	 */
	Time getAsTime(String key);

	/**
	 * Retrieves a <code>Time</code> value by key.
	 * 
	 * @param key
	 *            the key
	 * 
	 * @param defaultValue
	 *            the default to return
	 * 
	 * @return the value or defaultValue if value is not a valid Time or no value is in this
	 *         <code>IValueMap</code>
	 * 
	 */
	Time getAsTime(String key, Time defaultValue);

	/**
	 * Retrieves an <code>Enum</code> value by key.
	 * 
	 * @param <T>
	 *            type of enum
	 * 
	 * @param key
	 *            the key
	 * 
	 * @param eClass
	 *            the enumeration class
	 * 
	 * @return the value or null if value is not a valid value of the Enumeration or no value is in
	 *         this <code>IValueMap</code>
	 * 
	 */
	<T extends Enum<T>> T getAsEnum(String key, Class<T> eClass);

	/**
	 * Retrieves an <code>Enum</code> value by key.
	 * 
	 * @param <T>
	 *            type of enum
	 * 
	 * @param key
	 *            the key
	 * 
	 * @param defaultValue
	 *            the default value from the Enumeration (cannot be null)
	 * 
	 * @return the value or defaultValue if value is not a valid value of the Enumeration or no
	 *         value is in this <code>IValueMap</code>
	 * 
	 */
	<T extends Enum<T>> T getAsEnum(String key, T defaultValue);

	/**
	 * Retrieves an <code>Enum</code> value by key.
	 * 
	 * @param <T>
	 *            type of enum
	 * 
	 * @param key
	 *            the key
	 * 
	 * @param eClass
	 *            the enumeration class
	 * 
	 * @param defaultValue
	 *            the default value from the Enumeration (may be null)
	 * 
	 * @return the value or defaultValue if value is not a valid value of the Enumeration or no
	 *         value is in this <code>IValueMap</code>
	 * 
	 */
	<T extends Enum<T>> T getAsEnum(String key, Class<T> eClass, T defaultValue);
}