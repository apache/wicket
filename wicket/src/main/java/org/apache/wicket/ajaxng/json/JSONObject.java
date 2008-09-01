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
package org.apache.wicket.ajaxng.json;

/*
 * Copyright (c) 2002 JSON.org
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A JSONObject is an unordered collection of name/value pairs. Its external form is a string
 * wrapped in curly braces with colons between the names and values, and commas between the values
 * and names. The internal form is an object having <code>get</code> and <code>opt</code>
 * methods for accessing the values by name, and <code>put</code> methods for adding or replacing
 * values by name. The values can be any of these types: <code>Boolean</code>,
 * <code>JSONArray</code>, <code>JSONObject</code>, <code>Number</code>,
 * <code>String</code>, or the <code>JSONObject.NULL</code> object. A JSONObject constructor
 * can be used to convert an external form JSON text into an internal form whose values can be
 * retrieved with the <code>get</code> and <code>opt</code> methods, or to convert values into a
 * JSON text using the <code>put</code> and <code>toString</code> methods. A <code>get</code>
 * method returns a value if one can be found, and throws an exception if one cannot be found. An
 * <code>opt</code> method returns a default value instead of throwing an exception, and so is
 * useful for obtaining optional values.
 * <p>
 * The generic <code>get()</code> and <code>opt()</code> methods return an object, which you can
 * cast or query for type. There are also typed <code>get</code> and <code>opt</code> methods
 * that do type checking and type coersion for you.
 * <p>
 * The <code>put</code> methods adds values to an object. For example,
 * 
 * <pre>
 * myString = new JSONObject().put(&quot;JSON&quot;, &quot;Hello, World!&quot;).toString();
 * </pre>
 * 
 * produces the string <code>{"JSON": "Hello, World"}</code>.
 * <p>
 * The texts produced by the <code>toString</code> methods strictly conform to the JSON sysntax
 * rules.
 * <p>
 * Note that this class has been slightly modified not to quote parameter names unless necessary.
 * i.e. it will produce <code>{ JSON: "Hello, World"}</code> rather than
 * <code>{"JSON": "Hello, World"}<code>
 * 
 * @author JSON.org
 * @version 2
 */
@SuppressWarnings("unchecked")
public class JSONObject
{

	/**
	 * JSONObject.NULL is equivalent to the value that JavaScript calls null, whilst Java's null is
	 * equivalent to the value that JavaScript calls undefined.
	 */
	private static final class Null
	{

		/**
		 * There is only intended to be a single instance of the NULL object, so the clone method
		 * returns itself.
		 * 
		 * @return NULL.
		 */
		protected final Object clone()
		{
			return this;
		}


		/**
		 * A Null object is equal to the null value and to itself.
		 * 
		 * @param object
		 *            An object to test for nullness.
		 * @return true if the object parameter is the JSONObject.NULL object or null.
		 */
		public boolean equals(Object object)
		{
			return object == null || object == this;
		}


		/**
		 * Get the "null" string value.
		 * 
		 * @return The string "null".
		 */
		public String toString()
		{
			return "null";
		}
	}


	/**
	 * The hash map where the JSONObject's properties are kept.
	 */
	private HashMap myHashMap;


	/**
	 * It is sometimes more convenient and less ambiguous to have a <code>NULL</code> object than
	 * to use Java's <code>null</code> value. <code>JSONObject.NULL.equals(null)</code> returns
	 * <code>true</code>. <code>JSONObject.NULL.toString()</code> returns <code>"null"</code>.
	 */
	public static final Object NULL = new Null();


	/**
	 * Construct an empty JSONObject.
	 */
	public JSONObject()
	{
		this.myHashMap = new HashMap();
	}


	/**
	 * Construct a JSONObject from a subset of another JSONObject. An array of strings is used to
	 * identify the keys that should be copied. Missing keys are ignored.
	 * 
	 * @param jo
	 *            A JSONObject.
	 * @param sa
	 *            An array of strings.
	 * @exception JSONException
	 *                If a value is a non-finite number.
	 */
	public JSONObject(JSONObject jo, String[] sa) throws JSONException
	{
		this();
		for (int i = 0; i < sa.length; i += 1)
		{
			putOpt(sa[i], jo.opt(sa[i]));
		}
	}


	/**
	 * Construct a JSONObject from a Map.
	 * 
	 * @param map
	 *            A map object that can be used to initialize the contents of the JSONObject.
	 */
	public JSONObject(Map map)
	{
		this.myHashMap = (map == null) ? new HashMap() : new HashMap(map);
	}


	/**
	 * Construct a JSONObject from an Object, using reflection to find the public members. The
	 * resulting JSONObject's keys will be the strings from the names array, and the values will be
	 * the field values associated with those keys in the object. If a key is not found or not
	 * visible, then it will not be copied into the new JSONObject.
	 * 
	 * @param object
	 *            An object that has fields that should be used to make a JSONObject.
	 * @param names
	 *            An array of strings, the names of the fields to be used from the object.
	 */
	public JSONObject(Object object, String names[])
	{
		this();
		Class c = object.getClass();
		for (int i = 0; i < names.length; i += 1)
		{
			try
			{
				String name = names[i];
				Field field = c.getField(name);
				Object value = field.get(object);
				this.put(name, value);
			}
			catch (Exception e)
			{
				/* forget about it */
			}
		}
	}


	/**
	 * Accumulate values under a key. It is similar to the put method except that if there is
	 * already an object stored under the key then a JSONArray is stored under the key to hold all
	 * of the accumulated values. If there is already a JSONArray, then the new value is appended to
	 * it. In contrast, the put method replaces the previous value.
	 * 
	 * @param key
	 *            A key string.
	 * @param value
	 *            An object to be accumulated under the key.
	 * @return this.
	 * @throws JSONException
	 *             If the value is an invalid number or if the key is null.
	 */
	public JSONObject accumulate(String key, Object value) throws JSONException
	{
		testValidity(value);
		Object o = opt(key);
		if (o == null)
		{
			put(key, value);
		}
		else if (o instanceof JSONArray)
		{
			((JSONArray)o).put(value);
		}
		else
		{
			put(key, new JSONArray().put(o).put(value));
		}
		return this;
	}


	/**
	 * Append values to the array under a key. If the key does not exist in the JSONObject, then the
	 * key is put in the JSONObject with its value being a JSONArray containing the value parameter.
	 * If the key was already associated with a JSONArray, then the value parameter is appended to
	 * it.
	 * 
	 * @param key
	 *            A key string.
	 * @param value
	 *            An object to be accumulated under the key.
	 * @return this.
	 * @throws JSONException
	 *             If the key is null or if the current value associated with the key is not a
	 *             JSONArray.
	 */
	public JSONObject append(String key, Object value) throws JSONException
	{
		testValidity(value);
		Object o = opt(key);
		if (o == null)
		{
			put(key, new JSONArray().put(value));
		}
		else if (o instanceof JSONArray)
		{
			throw new JSONException("JSONObject[" + key + "] is not a JSONArray.");
		}
		else
		{
			put(key, new JSONArray().put(o).put(value));
		}
		return this;
	}


	/**
	 * Produce a string from a double. The string "null" will be returned if the number is not
	 * finite.
	 * 
	 * @param d
	 *            A double.
	 * @return A String.
	 */
	static public String doubleToString(double d)
	{
		if (Double.isInfinite(d) || Double.isNaN(d))
		{
			return "null";
		}

// Shave off trailing zeros and decimal point, if possible.

		String s = Double.toString(d);
		if (s.indexOf('.') > 0 && s.indexOf('e') < 0 && s.indexOf('E') < 0)
		{
			while (s.endsWith("0"))
			{
				s = s.substring(0, s.length() - 1);
			}
			if (s.endsWith("."))
			{
				s = s.substring(0, s.length() - 1);
			}
		}
		return s;
	}


	/**
	 * Get the value object associated with a key.
	 * 
	 * @param key
	 *            A key string.
	 * @return The object associated with the key.
	 * @throws JSONException
	 *             if the key is not found.
	 */
	public Object get(String key) throws JSONException
	{
		Object o = opt(key);
		if (o == null)
		{
			throw new JSONException("JSONObject[" + quote(key) + "] not found.");
		}
		return o;
	}


	/**
	 * Get the boolean value associated with a key.
	 * 
	 * @param key
	 *            A key string.
	 * @return The truth.
	 * @throws JSONException
	 *             if the value is not a Boolean or the String "true" or "false".
	 */
	public boolean getBoolean(String key) throws JSONException
	{
		Object o = get(key);
		if (o.equals(Boolean.FALSE) ||
			(o instanceof String && ((String)o).equalsIgnoreCase("false")))
		{
			return false;
		}
		else if (o.equals(Boolean.TRUE) ||
			(o instanceof String && ((String)o).equalsIgnoreCase("true")))
		{
			return true;
		}
		throw new JSONException("JSONObject[" + quote(key) + "] is not a Boolean.");
	}


	/**
	 * Get the double value associated with a key.
	 * 
	 * @param key
	 *            A key string.
	 * @return The numeric value.
	 * @throws JSONException
	 *             if the key is not found or if the value is not a Number object and cannot be
	 *             converted to a number.
	 */
	public double getDouble(String key) throws JSONException
	{
		Object o = get(key);
		try
		{
			return o instanceof Number ? ((Number)o).doubleValue() : Double.valueOf((String)o)
				.doubleValue();
		}
		catch (Exception e)
		{
			throw new JSONException("JSONObject[" + quote(key) + "] is not a number.");
		}
	}


	/**
	 * Get the int value associated with a key. If the number value is too large for an int, it will
	 * be clipped.
	 * 
	 * @param key
	 *            A key string.
	 * @return The integer value.
	 * @throws JSONException
	 *             if the key is not found or if the value cannot be converted to an integer.
	 */
	public int getInt(String key) throws JSONException
	{
		Object o = get(key);
		return o instanceof Number ? ((Number)o).intValue() : (int)getDouble(key);
	}


	/**
	 * Get the JSONArray value associated with a key.
	 * 
	 * @param key
	 *            A key string.
	 * @return A JSONArray which is the value.
	 * @throws JSONException
	 *             if the key is not found or if the value is not a JSONArray.
	 */
	public JSONArray getJSONArray(String key) throws JSONException
	{
		Object o = get(key);
		if (o instanceof JSONArray)
		{
			return (JSONArray)o;
		}
		throw new JSONException("JSONObject[" + quote(key) + "] is not a JSONArray.");
	}


	/**
	 * Get the JSONObject value associated with a key.
	 * 
	 * @param key
	 *            A key string.
	 * @return A JSONObject which is the value.
	 * @throws JSONException
	 *             if the key is not found or if the value is not a JSONObject.
	 */
	public JSONObject getJSONObject(String key) throws JSONException
	{
		Object o = get(key);
		if (o instanceof JSONObject)
		{
			return (JSONObject)o;
		}
		throw new JSONException("JSONObject[" + quote(key) + "] is not a JSONObject.");
	}


	/**
	 * Get the long value associated with a key. If the number value is too long for a long, it will
	 * be clipped.
	 * 
	 * @param key
	 *            A key string.
	 * @return The long value.
	 * @throws JSONException
	 *             if the key is not found or if the value cannot be converted to a long.
	 */
	public long getLong(String key) throws JSONException
	{
		Object o = get(key);
		return o instanceof Number ? ((Number)o).longValue() : (long)getDouble(key);
	}


	/**
	 * Get the string associated with a key.
	 * 
	 * @param key
	 *            A key string.
	 * @return A string which is the value.
	 * @throws JSONException
	 *             if the key is not found.
	 */
	public String getString(String key) throws JSONException
	{
		return get(key).toString();
	}


	/**
	 * Determine if the JSONObject contains a specific key.
	 * 
	 * @param key
	 *            A key string.
	 * @return true if the key exists in the JSONObject.
	 */
	public boolean has(String key)
	{
		return this.myHashMap.containsKey(key);
	}


	/**
	 * Determine if the value associated with the key is null or if there is no value.
	 * 
	 * @param key
	 *            A key string.
	 * @return true if there is no value associated with the key or if the value is the
	 *         JSONObject.NULL object.
	 */
	public boolean isNull(String key)
	{
		return JSONObject.NULL.equals(opt(key));
	}


	/**
	 * Get an enumeration of the keys of the JSONObject.
	 * 
	 * @return An iterator of the keys.
	 */
	public Iterator keys()
	{
		return this.myHashMap.keySet().iterator();
	}


	/**
	 * Get the number of keys stored in the JSONObject.
	 * 
	 * @return The number of keys in the JSONObject.
	 */
	public int length()
	{
		return this.myHashMap.size();
	}


	/**
	 * Produce a JSONArray containing the names of the elements of this JSONObject.
	 * 
	 * @return A JSONArray containing the key strings, or null if the JSONObject is empty.
	 */
	public JSONArray names()
	{
		JSONArray ja = new JSONArray();
		Iterator keys = keys();
		while (keys.hasNext())
		{
			ja.put(keys.next());
		}
		return ja.length() == 0 ? null : ja;
	}

	/**
	 * Produce a string from a Number.
	 * 
	 * @param n
	 *            A Number
	 * @return A String.
	 * @throws JSONException
	 *             If n is a non-finite number.
	 */
	static public String numberToString(Number n) throws JSONException
	{
		if (n == null)
		{
			throw new JSONException("Null pointer");
		}
		testValidity(n);

// Shave off trailing zeros and decimal point, if possible.

		String s = n.toString();
		if (s.indexOf('.') > 0 && s.indexOf('e') < 0 && s.indexOf('E') < 0)
		{
			while (s.endsWith("0"))
			{
				s = s.substring(0, s.length() - 1);
			}
			if (s.endsWith("."))
			{
				s = s.substring(0, s.length() - 1);
			}
		}
		return s;
	}


	/**
	 * Get an optional value associated with a key.
	 * 
	 * @param key
	 *            A key string.
	 * @return An object which is the value, or null if there is no value.
	 */
	public Object opt(String key)
	{
		return key == null ? null : this.myHashMap.get(key);
	}


	/**
	 * Get an optional boolean associated with a key. It returns false if there is no such key, or
	 * if the value is not Boolean.TRUE or the String "true".
	 * 
	 * @param key
	 *            A key string.
	 * @return The truth.
	 */
	public boolean optBoolean(String key)
	{
		return optBoolean(key, false);
	}


	/**
	 * Get an optional boolean associated with a key. It returns the defaultValue if there is no
	 * such key, or if it is not a Boolean or the String "true" or "false" (case insensitive).
	 * 
	 * @param key
	 *            A key string.
	 * @param defaultValue
	 *            The default.
	 * @return The truth.
	 */
	public boolean optBoolean(String key, boolean defaultValue)
	{
		try
		{
			return getBoolean(key);
		}
		catch (Exception e)
		{
			return defaultValue;
		}
	}


	/**
	 * Put a key/value pair in the JSONObject, where the value will be a JSONArray which is produced
	 * from a Collection.
	 * 
	 * @param key
	 *            A key string.
	 * @param value
	 *            A Collection value.
	 * @return this.
	 * @throws JSONException
	 */
	public JSONObject put(String key, Collection value) throws JSONException
	{
		put(key, new JSONArray(value));
		return this;
	}


	/**
	 * Get an optional double associated with a key, or NaN if there is no such key or if its value
	 * is not a number. If the value is a string, an attempt will be made to evaluate it as a
	 * number.
	 * 
	 * @param key
	 *            A string which is the key.
	 * @return An object which is the value.
	 */
	public double optDouble(String key)
	{
		return optDouble(key, Double.NaN);
	}


	/**
	 * Get an optional double associated with a key, or the defaultValue if there is no such key or
	 * if its value is not a number. If the value is a string, an attempt will be made to evaluate
	 * it as a number.
	 * 
	 * @param key
	 *            A key string.
	 * @param defaultValue
	 *            The default.
	 * @return An object which is the value.
	 */
	public double optDouble(String key, double defaultValue)
	{
		try
		{
			Object o = opt(key);
			return o instanceof Number ? ((Number)o).doubleValue()
				: new Double((String)o).doubleValue();
		}
		catch (Exception e)
		{
			return defaultValue;
		}
	}


	/**
	 * Get an optional int value associated with a key, or zero if there is no such key or if the
	 * value is not a number. If the value is a string, an attempt will be made to evaluate it as a
	 * number.
	 * 
	 * @param key
	 *            A key string.
	 * @return An object which is the value.
	 */
	public int optInt(String key)
	{
		return optInt(key, 0);
	}


	/**
	 * Get an optional int value associated with a key, or the default if there is no such key or if
	 * the value is not a number. If the value is a string, an attempt will be made to evaluate it
	 * as a number.
	 * 
	 * @param key
	 *            A key string.
	 * @param defaultValue
	 *            The default.
	 * @return An object which is the value.
	 */
	public int optInt(String key, int defaultValue)
	{
		try
		{
			return getInt(key);
		}
		catch (Exception e)
		{
			return defaultValue;
		}
	}


	/**
	 * Get an optional JSONArray associated with a key. It returns null if there is no such key, or
	 * if its value is not a JSONArray.
	 * 
	 * @param key
	 *            A key string.
	 * @return A JSONArray which is the value.
	 */
	public JSONArray optJSONArray(String key)
	{
		Object o = opt(key);
		return o instanceof JSONArray ? (JSONArray)o : null;
	}


	/**
	 * Get an optional JSONObject associated with a key. It returns null if there is no such key, or
	 * if its value is not a JSONObject.
	 * 
	 * @param key
	 *            A key string.
	 * @return A JSONObject which is the value.
	 */
	public JSONObject optJSONObject(String key)
	{
		Object o = opt(key);
		return o instanceof JSONObject ? (JSONObject)o : null;
	}


	/**
	 * Get an optional long value associated with a key, or zero if there is no such key or if the
	 * value is not a number. If the value is a string, an attempt will be made to evaluate it as a
	 * number.
	 * 
	 * @param key
	 *            A key string.
	 * @return An object which is the value.
	 */
	public long optLong(String key)
	{
		return optLong(key, 0);
	}


	/**
	 * Get an optional long value associated with a key, or the default if there is no such key or
	 * if the value is not a number. If the value is a string, an attempt will be made to evaluate
	 * it as a number.
	 * 
	 * @param key
	 *            A key string.
	 * @param defaultValue
	 *            The default.
	 * @return An object which is the value.
	 */
	public long optLong(String key, long defaultValue)
	{
		try
		{
			return getLong(key);
		}
		catch (Exception e)
		{
			return defaultValue;
		}
	}


	/**
	 * Get an optional string associated with a key. It returns an empty string if there is no such
	 * key. If the value is not a string and is not null, then it is coverted to a string.
	 * 
	 * @param key
	 *            A key string.
	 * @return A string which is the value.
	 */
	public String optString(String key)
	{
		return optString(key, "");
	}


	/**
	 * Get an optional string associated with a key. It returns the defaultValue if there is no such
	 * key.
	 * 
	 * @param key
	 *            A key string.
	 * @param defaultValue
	 *            The default.
	 * @return A string which is the value.
	 */
	public String optString(String key, String defaultValue)
	{
		Object o = opt(key);
		return o != null ? o.toString() : defaultValue;
	}


	/**
	 * Put a key/boolean pair in the JSONObject.
	 * 
	 * @param key
	 *            A key string.
	 * @param value
	 *            A boolean which is the value.
	 * @return this.
	 * @throws JSONException
	 *             If the key is null.
	 */
	public JSONObject put(String key, boolean value) throws JSONException
	{
		put(key, value ? Boolean.TRUE : Boolean.FALSE);
		return this;
	}


	/**
	 * Put a key/double pair in the JSONObject.
	 * 
	 * @param key
	 *            A key string.
	 * @param value
	 *            A double which is the value.
	 * @return this.
	 * @throws JSONException
	 *             If the key is null or if the number is invalid.
	 */
	public JSONObject put(String key, double value) throws JSONException
	{
		put(key, new Double(value));
		return this;
	}


	/**
	 * Put a key/int pair in the JSONObject.
	 * 
	 * @param key
	 *            A key string.
	 * @param value
	 *            An int which is the value.
	 * @return this.
	 * @throws JSONException
	 *             If the key is null.
	 */
	public JSONObject put(String key, int value) throws JSONException
	{
		put(key, new Integer(value));
		return this;
	}


	/**
	 * Put a key/long pair in the JSONObject.
	 * 
	 * @param key
	 *            A key string.
	 * @param value
	 *            A long which is the value.
	 * @return this.
	 * @throws JSONException
	 *             If the key is null.
	 */
	public JSONObject put(String key, long value) throws JSONException
	{
		put(key, new Long(value));
		return this;
	}


	/**
	 * Put a key/value pair in the JSONObject, where the value will be a JSONObject which is
	 * produced from a Map.
	 * 
	 * @param key
	 *            A key string.
	 * @param value
	 *            A Map value.
	 * @return this.
	 * @throws JSONException
	 */
	public JSONObject put(String key, Map value) throws JSONException
	{
		put(key, new JSONObject(value));
		return this;
	}


	/**
	 * Put a key/value pair in the JSONObject. If the value is null, then the key will be removed
	 * from the JSONObject if it is present.
	 * 
	 * @param key
	 *            A key string.
	 * @param value
	 *            An object which is the value. It should be of one of these types: Boolean, Double,
	 *            Integer, JSONArray, JSONObject, Long, String, or the JSONObject.NULL object.
	 * @return this.
	 * @throws JSONException
	 *             If the value is non-finite number or if the key is null.
	 */
	public JSONObject put(String key, Object value) throws JSONException
	{
		if (key == null)
		{
			throw new JSONException("Null key.");
		}
		if (value != null)
		{
			testValidity(value);
			this.myHashMap.put(key, value);
		}
		else
		{
			remove(key);
		}
		return this;
	}


	/**
	 * Put a key/value pair in the JSONObject, but only if the key and the value are both non-null.
	 * 
	 * @param key
	 *            A key string.
	 * @param value
	 *            An object which is the value. It should be of one of these types: Boolean, Double,
	 *            Integer, JSONArray, JSONObject, Long, String, or the JSONObject.NULL object.
	 * @return this.
	 * @throws JSONException
	 *             If the value is a non-finite number.
	 */
	public JSONObject putOpt(String key, Object value) throws JSONException
	{
		if (key != null && value != null)
		{
			put(key, value);
		}
		return this;
	}


	/**
	 * Produce a string in double quotes with backslash sequences in all the right places. A
	 * backslash will be inserted within </, allowing JSON text to be delivered in HTML. In JSON
	 * text, a string cannot contain a control character or an unescaped quote or backslash.
	 * 
	 * @param string
	 *            A String
	 * @return A String correctly formatted for insertion in a JSON text.
	 */
	public static String quote(String string)
	{
		if (string == null || string.length() == 0)
		{
			return "\"\"";
		}

		char b;
		char c = 0;
		int i;
		int len = string.length();
		StringBuffer sb = new StringBuffer(len + 4);
		String t;

		sb.append('"');
		for (i = 0; i < len; i += 1)
		{
			b = c;
			c = string.charAt(i);
			switch (c)
			{
				case '\\' :
				case '"' :
					sb.append('\\');
					sb.append(c);
					break;
				case '/' :
					if (b == '<')
					{
						sb.append('\\');
					}
					sb.append(c);
					break;
				case '\b' :
					sb.append("\\b");
					break;
				case '\t' :
					sb.append("\\t");
					break;
				case '\n' :
					sb.append("\\n");
					break;
				case '\f' :
					sb.append("\\f");
					break;
				case '\r' :
					sb.append("\\r");
					break;
				default :
					if (c < ' ')
					{
						t = "000" + Integer.toHexString(c);
						sb.append("\\u" + t.substring(t.length() - 4));
					}
					else
					{
						sb.append(c);
					}
			}
		}
		sb.append('"');
		return sb.toString();
	}

	/**
	 * Remove a name and its value, if present.
	 * 
	 * @param key
	 *            The name to be removed.
	 * @return The value that was associated with the name, or null if there was no value.
	 */
	public Object remove(String key)
	{
		return this.myHashMap.remove(key);
	}


	/**
	 * Throw an exception if the object is an NaN or infinite number.
	 * 
	 * @param o
	 *            The object to test.
	 * @throws JSONException
	 *             If o is a non-finite number.
	 */
	static void testValidity(Object o) throws JSONException
	{
		if (o != null)
		{
			if (o instanceof Double)
			{
				if (((Double)o).isInfinite() || ((Double)o).isNaN())
				{
					throw new JSONException("JSON does not allow non-finite numbers");
				}
			}
			else if (o instanceof Float)
			{
				if (((Float)o).isInfinite() || ((Float)o).isNaN())
				{
					throw new JSONException("JSON does not allow non-finite numbers.");
				}
			}
		}
	}


	/**
	 * Produce a JSONArray containing the values of the members of this JSONObject.
	 * 
	 * @param names
	 *            A JSONArray containing a list of key strings. This determines the sequence of the
	 *            values in the result.
	 * @return A JSONArray of values.
	 * @throws JSONException
	 *             If any of the values are non-finite numbers.
	 */
	public JSONArray toJSONArray(JSONArray names) throws JSONException
	{
		if (names == null || names.length() == 0)
		{
			return null;
		}
		JSONArray ja = new JSONArray();
		for (int i = 0; i < names.length(); i += 1)
		{
			ja.put(this.opt(names.getString(i)));
		}
		return ja;
	}

	/**
	 * Make a JSON text of this JSONObject. For compactness, no whitespace is added. If this would
	 * not result in a syntactically correct JSON text, then null will be returned instead.
	 * <p>
	 * Warning: This method assumes that the data structure is acyclical.
	 * 
	 * @return a printable, displayable, portable, transmittable representation of the object,
	 *         beginning with <code>{</code>&nbsp;<small>(left brace)</small> and ending with
	 *         <code>}</code>&nbsp;<small>(right brace)</small>.
	 */
	public String toString()
	{
		try
		{
			Iterator keys = keys();
			StringBuffer sb = new StringBuffer("{");

			while (keys.hasNext())
			{
				if (sb.length() > 1)
				{
					sb.append(',');
				}
				Object o = keys.next();
				String s = o.toString();
				if (needsQuote(s))
				{
					sb.append(quote(s));
				}
				else
				{
					sb.append(s);
				}
				sb.append(':');
				sb.append(valueToString(this.myHashMap.get(o)));
			}
			sb.append('}');
			return sb.toString();
		}
		catch (Exception e)
		{
			return null;
		}
	}


	/**
	 * Make a prettyprinted JSON text of this JSONObject.
	 * <p>
	 * Warning: This method assumes that the data structure is acyclical.
	 * 
	 * @param indentFactor
	 *            The number of spaces to add to each level of indentation.
	 * @return a printable, displayable, portable, transmittable representation of the object,
	 *         beginning with <code>{</code>&nbsp;<small>(left brace)</small> and ending with
	 *         <code>}</code>&nbsp;<small>(right brace)</small>.
	 * @throws JSONException
	 *             If the object contains an invalid number.
	 */
	public String toString(int indentFactor) throws JSONException
	{
		return toString(indentFactor, 0);
	}


	/**
	 * Make a prettyprinted JSON text of this JSONObject.
	 * <p>
	 * Warning: This method assumes that the data structure is acyclical.
	 * 
	 * @param indentFactor
	 *            The number of spaces to add to each level of indentation.
	 * @param indent
	 *            The indentation of the top level.
	 * @return a printable, displayable, transmittable representation of the object, beginning with
	 *         <code>{</code>&nbsp;<small>(left brace)</small> and ending with <code>}</code>&nbsp;<small>(right
	 *         brace)</small>.
	 * @throws JSONException
	 *             If the object contains an invalid number.
	 */
	String toString(int indentFactor, int indent) throws JSONException
	{
		int i;
		int n = length();
		if (n == 0)
		{
			return "{}";
		}
		Iterator keys = keys();
		StringBuffer sb = new StringBuffer("{");
		int newindent = indent + indentFactor;
		Object o;
		if (n == 1)
		{
			o = keys.next();
			sb.append(quote(o.toString()));
			sb.append(": ");
			sb.append(valueToString(this.myHashMap.get(o), indentFactor, indent));
		}
		else
		{
			while (keys.hasNext())
			{
				o = keys.next();
				if (sb.length() > 1)
				{
					sb.append(",\n");
				}
				else
				{
					sb.append('\n');
				}
				for (i = 0; i < newindent; i += 1)
				{
					sb.append(' ');
				}
				sb.append(quote(o.toString()));
				sb.append(": ");
				sb.append(valueToString(this.myHashMap.get(o), indentFactor, newindent));
			}
			if (sb.length() > 1)
			{
				sb.append('\n');
				for (i = 0; i < indent; i += 1)
				{
					sb.append(' ');
				}
			}
		}
		sb.append('}');
		return sb.toString();
	}


	/**
	 * Make a JSON text of an Object value. If the object has an value.toJSONString() method, then
	 * that method will be used to produce the JSON text. The method is required to produce a
	 * strictly conforming text. If the object does not contain a toJSONString method (which is the
	 * most common case), then a text will be produced by the rules.
	 * <p>
	 * Warning: This method assumes that the data structure is acyclical.
	 * 
	 * @param value
	 *            The value to be serialized.
	 * @return a printable, displayable, transmittable representation of the object, beginning with
	 *         <code>{</code>&nbsp;<small>(left brace)</small> and ending with <code>}</code>&nbsp;<small>(right
	 *         brace)</small>.
	 * @throws JSONException
	 *             If the value is or contains an invalid number.
	 */
	static String valueToString(Object value) throws JSONException
	{
		if (value == null || value.equals(null))
		{
			return "null";
		}
		if (value instanceof JSONString)
		{
			Object o;
			try
			{
				o = ((JSONString)value).toJSONString();
			}
			catch (Exception e)
			{
				throw new JSONException(e);
			}
			if (o instanceof String)
			{
				return (String)o;
			}
			throw new JSONException("Bad value from toJSONString: " + o);
		}
		if (value instanceof Number)
		{
			return numberToString((Number)value);
		}
		if (value instanceof Boolean || value instanceof JSONObject || value instanceof JSONArray)
		{
			return value.toString();
		}
		return quote(value.toString());
	}


	/**
	 * Make a prettyprinted JSON text of an object value.
	 * <p>
	 * Warning: This method assumes that the data structure is acyclical.
	 * 
	 * @param value
	 *            The value to be serialized.
	 * @param indentFactor
	 *            The number of spaces to add to each level of indentation.
	 * @param indent
	 *            The indentation of the top level.
	 * @return a printable, displayable, transmittable representation of the object, beginning with
	 *         <code>{</code>&nbsp;<small>(left brace)</small> and ending with <code>}</code>&nbsp;<small>(right
	 *         brace)</small>.
	 * @throws JSONException
	 *             If the object contains an invalid number.
	 */
	static String valueToString(Object value, int indentFactor, int indent) throws JSONException
	{
		if (value == null || value.equals(null))
		{
			return "null";
		}
		try
		{
			if (value instanceof JSONString)
			{
				Object o = ((JSONString)value).toJSONString();
				if (o instanceof String)
				{
					return (String)o;
				}
			}
		}
		catch (Exception e)
		{
			/* forget about it */
		}
		if (value instanceof Number)
		{
			return numberToString((Number)value);
		}
		if (value instanceof Boolean)
		{
			return value.toString();
		}
		if (value instanceof JSONObject)
		{
			return ((JSONObject)value).toString(indentFactor, indent);
		}
		if (value instanceof JSONArray)
		{
			return ((JSONArray)value).toString(indentFactor, indent);
		}
		return quote(value.toString());
	}

	private boolean needsQuote(CharSequence s)
	{
		for (int i = 0; i < s.length(); ++i)
		{
			if (i == 0 && !Character.isJavaIdentifierStart(s.charAt(i)))
			{
				return true;
			}
			else if (i > 0 && !Character.isJavaIdentifierPart(s.charAt(i)))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Write the contents of the JSONObject as JSON text to a writer. For compactness, no whitespace
	 * is added.
	 * <p>
	 * Warning: This method assumes that the data structure is acyclical.
	 * 
	 * @param writer
	 * 
	 * @return The writer.
	 * @throws JSONException
	 */
	public Writer write(Writer writer) throws JSONException
	{
		try
		{
			boolean b = false;
			Iterator keys = keys();
			writer.write('{');

			while (keys.hasNext())
			{
				if (b)
				{
					writer.write(',');
				}
				Object k = keys.next();
				String key = k.toString();
				if (needsQuote(key))
				{
					writer.write(quote(k.toString()));
				}
				else
				{
					writer.write(k.toString());
				}

				writer.write(':');
				Object v = this.myHashMap.get(k);
				if (v instanceof JSONObject)
				{
					((JSONObject)v).write(writer);
				}
				else if (v instanceof JSONArray)
				{
					((JSONArray)v).write(writer);
				}
				else
				{
					writer.write(valueToString(v));
				}
				b = true;
			}
			writer.write('}');
			return writer;
		}
		catch (IOException e)
		{
			throw new JSONException(e);
		}
	}
}