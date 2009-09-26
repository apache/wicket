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
package org.apache.wicket;

import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.protocol.http.RequestUtils;
import org.apache.wicket.util.string.IStringIterator;
import org.apache.wicket.util.string.StringList;
import org.apache.wicket.util.value.ValueMap;


/**
 * A typesafe abstraction and container for parameters to a requested page. Page parameters in HTTP
 * are query string values in the request URL. In other protocols, the parameters to a page might
 * come from some other source.
 * <p>
 * Pages which take a PageParameters object as an argument to their constructor can be accessed
 * directly from a URL and are known as "bookmarkable" pages since the URL is stable across sessions
 * and can be stored in a browser's bookmark database.
 * 
 * @author Jonathan Locke
 */
public final class PageParameters extends ValueMap
{
	/** Null value for page parameters */
	public static final PageParameters NULL = new PageParameters();

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 */
	public PageParameters()
	{
		super();

		setOnRequestCycle();
	}

	/**
	 * Copy constructor.
	 * 
	 * @param parameterMap
	 *            The map to copy
	 * 
	 * @see RequestUtils#decodeParameters(String, ValueMap)
	 * @see ValueMap#ValueMap(String, String)
	 */
	public PageParameters(final Map<String, ?> parameterMap)
	{
		super(parameterMap);

		setOnRequestCycle();
	}

	/**
	 * Construct.
	 * 
	 * @param keyValuePairs
	 *            List of key value pairs separated by commas. For example, "param1=foo,param2=bar"
	 * @see ValueMap#ValueMap(String)
	 */
	public PageParameters(final String keyValuePairs)
	{
		this(keyValuePairs, ",");
	}

	/**
	 * Construct.
	 * 
	 * @param keyValuePairs
	 *            List of key value pairs separated by commas. For example, "param1=foo,param2=bar"
	 * @param delimiter
	 *            Delimiter string used to separate key/value pairs
	 * @see ValueMap#ValueMap(String)
	 * 
	 * @deprecated Please use {@link RequestUtils#decodeParameters(String, ValueMap)} to decode a
	 *             request URL, or {@link ValueMap#ValueMap(String, String)} for other usecases.
	 */
	@Deprecated
	public PageParameters(final String keyValuePairs, final String delimiter)
	{
		super();

		setOnRequestCycle();

		// We can not use ValueMaps constructor as it uses
		// VariableAssignmentParser which is more suitable for markup
		// attributes, rather than URL parameters. URL param keys for
		// examples are allowed to start with a digit (e.g. 0=xxx)
		// and quotes are not "quotes".

		// Get list of strings separated by the delimiter
		final StringList pairs = StringList.tokenize(keyValuePairs, delimiter);

		// Go through each string in the list
		for (IStringIterator iterator = pairs.iterator(); iterator.hasNext();)
		{
			// Get the next key value pair
			final String pair = iterator.next();

			final int pos = pair.indexOf('=');
			if (pos == 0)
			{
				throw new IllegalArgumentException("URL parameter is missing the lvalue: " + pair);
			}
			else if (pos != -1)
			{
				final String key = pair.substring(0, pos).trim();
				final String value = pair.substring(pos + 1).trim();

				add(key, value);
			}
			else
			{
				final String key = pair.trim();
				final String value = null;

				add(key, value);
			}
		}
	}

	/**
	 * @see org.apache.wicket.util.value.ValueMap#put(java.lang.String, java.lang.Object)
	 */
	@Override
	public Object put(String key, Object value)
	{
		return super.put(key, value);
		/*
		 * see WICKET-2162 as well. BRING BACK IN 1.4
		 * 
		 * if (!(key instanceof String)) { throw new IllegalArgumentException( "PageParameter keys
		 * must be of type String, but you supplied a " + key.getClass().getName()); } if (value
		 * instanceof String || value instanceof String[]) { return super.put(key, value); } else {
		 * throw new IllegalArgumentException("You tried to add an object of type " +
		 * value.getClass().getName() + " to your PageParameters for key " + key + ", but you are
		 * only allowed to use String or String[]."); }
		 */
	}

	/**
	 * Set this on request cycle. The RequestCycle will decide whether to keep it as a reference or
	 * not.
	 * 
	 * @see RequestCycle#setPageParameters(PageParameters)
	 */
	private void setOnRequestCycle()
	{
		RequestCycle cycle = RequestCycle.get();
		if (cycle != null)
		{
			cycle.setPageParameters(this);
		}
	}

	/**
	 * Converts page parameters to servlet request parameters
	 * 
	 * @return request parameters map
	 */
	public Map<String, String[]> toRequestParameters()
	{
		Map<String, String[]> params = new HashMap<String, String[]>(size());
		for (Map.Entry<String, Object> entry : entrySet())
		{
			if (entry.getValue() == null)
			{
				params.put(entry.getKey(), null);
			}
			else if (entry.getValue().getClass().isArray())
			{
				final Object[] arr = (Object[])entry.getValue();
				final String[] str = new String[arr.length];
				for (int i = 0; i < arr.length; i++)
				{
					str[i] = arr[i].toString();
				}
				params.put(entry.getKey(), str);
			}
			else
			{
				params.put(entry.getKey(), new String[] { entry.getValue().toString() });
			}
		}
		return params;
	}

	/**
	 * Get an instance of an interface, which transparently reads and writes to this PageParameters.
	 * Useful if you would prefer to deal in Java objects rather than extracting key/value pairs.
	 * <h4>Usage:</h4> Write an interface that follows the beans pattern, such as
	 * 
	 * <pre>
	 * public interface MyData
	 * {
	 * 	double getUserid();
	 * 
	 * 	void setUserId(double id);
	 * 
	 * 	String getRequestedCheese();
	 * 
	 * 	void setRequestedCheese(String cheese);
	 * 
	 * 	boolean isBackorder();
	 * 
	 * 	void setBackorder(boolean val);
	 * }
	 * </pre>
	 * 
	 * It <em>must</em> be a Java interface, because the implementation uses dynamic proxies to
	 * generate an implementation of that interface.
	 * <p/>
	 * Your PageParameters should contain key/value pairs with names such as
	 * &quot;requestedCheese&quot;, &quot;userid&quot; and &quot;backorder&quot;
	 * <p/>
	 * You will be returned an implementation of your interface, which delegates to the
	 * PageParameters for the actual values, but handles typecasting and avoids issues with typos in
	 * string keys.
	 * <p/>
	 * The resulting object is read/write, so you can use it both to populate and to read from a
	 * PageParameters object.
	 * <p/>
	 * If the requested value does not exist in the PageParameters, the return value will be null,
	 * -1 or false depending on the requested type.
	 * <p/>
	 * Note that it is possible to read and write <code>Serializable</code> objects; however, this
	 * should not be done for objects with many fields, as it may exceed the browser's URL-length
	 * limit.
	 * 
	 * @param <T>
	 *            The return type
	 * @param ifaceType
	 *            The concrete type of the interface you need
	 * @return An instance of that interface, dynamically generated
	 */
	public <T> T asObject(Class<T> ifaceType)
	{
		if (ifaceType == null)
		{
			throw new NullPointerException("Parameter 'ifaceType' must not be null");
		}
		if (!ifaceType.isInterface())
		{
			throw new IllegalArgumentException("ifaceType is not an interface: " +
				ifaceType.getName());
		}
		return new PageParametersMarshaller().read(ifaceType, this);
	}
}
