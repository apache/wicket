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
package org.apache.wicket.ng.request.component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.wicket.RequestCycle;
import org.apache.wicket.ng.request.RequestMapper;
import org.apache.wicket.util.lang.Checks;
import org.apache.wicket.util.lang.Objects;
import org.apache.wicket.util.string.IStringIterator;
import org.apache.wicket.util.string.StringList;
import org.apache.wicket.util.string.StringValue;
import org.apache.wicket.util.value.ValueMap;

/**
 * Mutable class that holds parameters of a Page. Page parameters consist of indexed parameters and
 * named parameters. Indexed parameters are URL segments before the query string. Named parameters
 * are usually represented as query string params (i.e. ?arg1=var1&amp;arg2=val)
 * <p>
 * How those parameters are populated depends on the {@link RequestMapper}s
 * 
 * @author Matej Knopp
 * @author Igor Vaynberg
 */
public class PageParameters implements Serializable
{

	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 */
	public PageParameters()
	{

	}

	/**
	 * Copy constructor.
	 * 
	 * @param copy
	 */
	public PageParameters(PageParameters copy)
	{
		Checks.argumentNotNull(copy, "copy");

		if (copy.indexedParameters != null)
			indexedParameters = new ArrayList<String>(copy.indexedParameters);

		if (copy.namedParameters != null)
			namedParameters = new ArrayList<Entry>(copy.namedParameters);
	}


	private List<String> indexedParameters = null;

	private static class Entry implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private String key;
		private String value;
	};

	private List<Entry> namedParameters = null;

	/**
	 * @return count of indexed parameters
	 */
	public int getIndexedParamsCount()
	{
		return indexedParameters != null ? indexedParameters.size() : 0;
	}

	/**
	 * Sets the indexed parameter on given index
	 * 
	 * @param index
	 * @param object
	 */
	public void setIndexedParameter(int index, Object object)
	{
		if (indexedParameters == null)
			indexedParameters = new ArrayList<String>(index);

		for (int i = indexedParameters.size(); i <= index; ++i)
		{
			indexedParameters.add(null);
		}

		indexedParameters.set(index, object != null ? object.toString() : null);
	}

	/**
	 * @param index
	 * @return indexed parameter on given index
	 */
	public StringValue getIndexedParameter(int index)
	{
		if (indexedParameters != null)
		{
			if (index >= 0 && index < indexedParameters.size())
			{
				return StringValue.valueOf(indexedParameters.get(index));
			}
		}
		return StringValue.valueOf((String)null);
	};

	/**
	 * Removes indexed parameter on given index
	 * 
	 * @param index
	 */
	public void removeIndexedParameter(int index)
	{
		if (indexedParameters != null)
		{
			if (index >= 0 && index < indexedParameters.size())
			{
				indexedParameters.remove(index);
			}
		}
	}

	/**
	 * Return set of all named parameter names.
	 * 
	 * @return named parameter names
	 */
	public Set<String> getNamedParameterKeys()
	{
		if (namedParameters == null || namedParameters.isEmpty())
		{
			return Collections.emptySet();
		}
		Set<String> set = new TreeSet<String>();
		for (Entry entry : namedParameters)
		{
			set.add(entry.key);
		}
		return Collections.unmodifiableSet(set);
	}

	/**
	 * Returns parameter value of named parameter with given name
	 * 
	 * @param name
	 * @return parameter value
	 */
	public StringValue getNamedParameter(String name)
	{
		Checks.argumentNotNull(name, "name");

		if (namedParameters != null)
		{
			for (Entry entry : namedParameters)
			{
				if (entry.key.equals(name))
				{
					return StringValue.valueOf(entry.value);
				}
			}
		}
		return StringValue.valueOf((String)null);
	}

	/**
	 * Return list of all values for named parameter with given name
	 * 
	 * @param name
	 * @return list of parameter values
	 */
	public List<StringValue> getNamedParameters(String name)
	{
		Checks.argumentNotNull(name, "name");

		if (namedParameters != null)
		{
			List<StringValue> result = new ArrayList<StringValue>();
			for (Entry entry : namedParameters)
			{
				if (entry.key.equals(name))
				{
					result.add(StringValue.valueOf(entry.value));
				}
			}
			return Collections.unmodifiableList(result);
		}
		else
		{
			return Collections.emptyList();
		}
	}

	/**
	 * Removes named parameter with given name.
	 * 
	 * @param name
	 */
	public void removeNamedParameter(String name)
	{
		Checks.argumentNotNull(name, "name");

		if (namedParameters != null)
		{
			for (Iterator<Entry> i = namedParameters.iterator(); i.hasNext();)
			{
				Entry e = i.next();
				if (e.key.equals(name))
				{
					i.remove();
				}
			}
		}
	}

	/**
	 * Adds value to named parameter with given name.
	 * 
	 * @param name
	 * @param value
	 */
	public void addNamedParameter(String name, Object value)
	{
		addNamedParameter(name, value, -1);
	}

	/**
	 * Adds named parameter to a specified position. The {@link RequestMapper}s may or may not take
	 * the order into account.
	 * 
	 * @param name
	 * @param value
	 * @param index
	 */
	public void addNamedParameter(String name, Object value, int index)
	{
		Checks.argumentNotNull(name, "name");
		Checks.argumentNotNull(value, "value");

		if (namedParameters == null)
			namedParameters = new ArrayList<Entry>(1);
		Entry entry = new Entry();
		entry.key = name;
		entry.value = value.toString();

		if (index == -1)
			namedParameters.add(entry);
		else
			namedParameters.add(index, entry);
	}

	/**
	 * Sets the named parameter on specified position. The {@link RequestMapper}s may or may not
	 * take the order into account.
	 * 
	 * @param name
	 * @param value
	 * @param index
	 */
	public void setNamedParameter(String name, Object value, int index)
	{
		removeNamedParameter(name);

		if (value != null)
		{
			addNamedParameter(name, value);
		}
	}

	/**
	 * Sets the value for named parameter with given name.
	 * 
	 * @param name
	 * @param value
	 */
	public void setNamedParameter(String name, Object value)
	{
		setNamedParameter(name, value, -1);
	}

	/**
	 * Removes all indexed parameters.
	 */
	public void clearIndexedParameters()
	{
		indexedParameters = null;
	}

	/**
	 * Removes all named parameters.
	 */
	public void clearNamedParameters()
	{
		namedParameters = null;
	}

	/**
	 * Copy the paga parameters
	 * 
	 * @param other
	 */
	public void assign(PageParameters other)
	{
		if (this != other)
		{
			indexedParameters = other.indexedParameters;
			namedParameters = other.namedParameters;
		}
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}

		if (obj instanceof PageParameters == false)
		{
			return false;
		}

		PageParameters rhs = (PageParameters)obj;
		if (!Objects.equal(indexedParameters, rhs.indexedParameters))
		{
			return false;
		}

		if (namedParameters == null || rhs.namedParameters == null)
		{
			return rhs.namedParameters == namedParameters;
		}

		if (namedParameters.size() != rhs.namedParameters.size())
		{
			return false;
		}

		for (String key : getNamedParameterKeys())
		{
			List<StringValue> values1 = getNamedParameters(key);
			Set<String> v1 = new TreeSet<String>();
			List<StringValue> values2 = rhs.getNamedParameters(key);
			Set<String> v2 = new TreeSet<String>();
			for (StringValue sv : values1)
			{
				v1.add(sv.toString());
			}
			for (StringValue sv : values2)
			{
				v2.add(sv.toString());
			}
			if (v1.equals(v2) == false)
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * Compares two {@link PageParameters} objects.
	 * 
	 * @param p1
	 * @param p2
	 * @return <code>true</code> if the objects are equal, <code>false</code> otherwise.
	 */
	public static boolean equals(PageParameters p1, PageParameters p2)
	{
		if (Objects.equal(p1, p2))
		{
			return true;
		}
		if (p1 == null && p2.getIndexedParamsCount() == 0 && p2.getNamedParameterKeys().isEmpty())
		{
			return true;
		}
		if (p2 == null && p1.getIndexedParamsCount() == 0 && p1.getNamedParameterKeys().isEmpty())
		{
			return true;
		}
		return false;
	}

	public boolean isEmpty()
	{
		return getIndexedParamsCount() == 0 && getNamedParameterKeys().isEmpty();
	}


	/**
	 * Removes a named parameter
	 * 
	 * TODO NG MIGRATION
	 * 
	 * @deprecated compatibility with old pageparameters
	 * @param parameterName
	 * @return old value if any or <code>null</code>
	 */
	@Deprecated
	public String put(String parameterName, Object value)
	{
		String old = getNamedParameter(parameterName).toString();
		if (old != null)
		{
			removeNamedParameter(parameterName);
		}
		setNamedParameter(parameterName, value);
		return old;
	}

	/**
	 * Removes a named parameter
	 * 
	 * TODO NG MIGRATION
	 * 
	 * @deprecated compatibility with old pageparameters
	 * @param parameterName
	 * @return old value if any or <code>null</code>
	 */
	@Deprecated
	public String remove(String parameterName)
	{
		String old = getNamedParameter(parameterName).toString();
		removeNamedParameter(parameterName);
		return old;
	}

	/**
	 * Constructor
	 * 
	 * TODO NG MIGRATION
	 * 
	 * @deprecated compatibility with old pageparameters
	 * 
	 * @param params
	 */
	@Deprecated
	public PageParameters(Map<String, ?> parameters)
	{
		this(new ValueMap(parameters));
	}


	/**
	 * Constructor
	 * 
	 * TODO NG MIGRATION
	 * 
	 * @deprecated compatibility with old pageparameters
	 * 
	 * @param params
	 */
	@Deprecated
	public PageParameters(ValueMap params)
	{
		for (Map.Entry<String, Object> param : params.entrySet())
		{
			if (param.getValue() instanceof String[])
			{
				for (String value : (String[])param.getValue())
				{
					addNamedParameter(param.getKey(), value);
				}
			}
			else
			{
				addNamedParameter(param.getKey(), param.getValue());
			}
		}
	}


	public PageParameters(String string)
	{
		setOnRequestCycle();

		// We can not use ValueMaps constructor as it uses
		// VariableAssignmentParser which is more suitable for markup
		// attributes, rather than URL parameters. URL param keys for
		// examples are allowed to start with a digit (e.g. 0=xxx)
		// and quotes are not "quotes".

		// Get list of strings separated by the delimiter
		final StringList pairs = StringList.tokenize(string, ",");

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
	 * Creates a named-parameter map
	 * 
	 * TODO NG MIGRATION
	 * 
	 * @deprecated compatibility with old pageparameters
	 * 
	 */
	@Deprecated
	public Map<String, String[]> getNamedParametersMap()
	{
		Map<String, String[]> map = new HashMap<String, String[]>();
		for (String name : getNamedParameterKeys())
		{
			List<String> vals = new ArrayList<String>();
			for (StringValue sv : getNamedParameters(name))
			{
				vals.add(sv.toString());
			}
			map.put(name, vals.toArray(new String[0]));
		}
		return map;
	}

	/**
	 * Adds named parameter
	 * 
	 * TODO NG MIGRATION
	 * 
	 * @deprecated compatibility with old pageparameters
	 * 
	 */
	@Deprecated
	public void add(String name, Object value)
	{
		addNamedParameter(name, value);
	}


	/**
	 * Gets keyset of named parameters
	 * 
	 * TODO NG MIGRATION
	 * 
	 * @deprecated compatibility with old pageparameters
	 * 
	 */
	@Deprecated
	public Set<String> keySet()
	{
		return new HashSet<String>(getNamedParameterKeys());
	}

	/**
	 * Gets named parameter
	 * 
	 * TODO NG MIGRATION
	 * 
	 * @deprecated compatibility with old pageparameters
	 * 
	 */
	@Deprecated
	public Object get(String name)
	{
		return getNamedParameter(name).toString();
	}

	/**
	 * Gets named parameter value array
	 * 
	 * TODO NG MIGRATION
	 * 
	 * @deprecated compatibility with old pageparameters
	 * 
	 */
	@Deprecated
	public String[] getStringArray(String name)
	{
		List<String> vals = new ArrayList<String>();
		for (StringValue value : getNamedParameters(name))
		{
			vals.add(value.toString());
		}
		return vals.toArray(new String[0]);
	}

	/**
	 * Gets number of named parameters
	 * 
	 * TODO NG MIGRATION
	 * 
	 * @deprecated compatibility with old pageparameters
	 * 
	 */
	@Deprecated
	public int size()
	{
		return getNamedParameterKeys().size();
	}

	/**
	 * TODO NG MIGRATION
	 * 
	 * @deprecated compatibility with old pageparameters
	 * 
	 */
	@Deprecated
	public boolean containsKey(String key)
	{
		return getNamedParameterKeys().contains(key);
	}

	/**
	 * TODO NG MIGRATION
	 * 
	 * @deprecated compatibility with old pageparameters
	 * 
	 */
	@Deprecated
	public String getString(String uri)
	{
		return getNamedParameter(uri).toString();
	}

	/**
	 * TODO NG MIGRATION
	 * 
	 * @deprecated compatibility with old pageparameters
	 * 
	 */
	@Deprecated
	public ValueMap toValueMap()
	{
		return new ValueMap(getNamedParametersMap());
	}

	public void putAll(Map<String, ?> urlParameters)
	{
		for (Map.Entry<String, ?> param : urlParameters.entrySet())
		{
			add(param.getKey(), param.getValue());
		}
	}

	/**
	 * Converts page parameters to servlet request parameters
	 * 
	 * TODO NG MIGRATION
	 * 
	 * @deprecated compatibility with old pageparameters
	 * @return request parameters map
	 */
	@Deprecated
	public Map<String, String[]> toRequestParameters()
	{
		Map<String, String[]> params = new HashMap<String, String[]>(size());
		for (Map.Entry<String, Object> entry : toValueMap().entrySet())
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
	 * TODO NG MIGRATION
	 * 
	 * @deprecated compatibility with old pageparameters
	 * 
	 * 
	 * @param string
	 * @param b
	 * @return
	 */
	@Deprecated
	public boolean getAsBoolean(String string, boolean def)
	{
		return getNamedParameter(string).toBoolean();
	}

	/**
	 * TODO NG MIGRATION
	 * 
	 * @deprecated compatibility with old pageparameters
	 * 
	 * 
	 * @param string
	 * @param b
	 * @return
	 */
	@Deprecated
	public boolean getBoolean(String string)
	{
		return getNamedParameter(string).toBoolean();
	}

	/**
	 * TODO NG MIGRATION
	 * 
	 * @deprecated compatibility with old pageparameters
	 * 
	 * 
	 * @param string
	 * @param b
	 * @return
	 */
	@Deprecated
	public int getInt(String string)
	{
		return getNamedParameter(string).toInt();
	}

	/**
	 * TODO NG MIGRATION
	 * 
	 * @deprecated compatibility with old pageparameters
	 * 
	 * 
	 * @param string
	 * @param b
	 * @return
	 */
	@Deprecated
	public long getLong(String string)
	{
		return getNamedParameter(string).toLong();
	}


}
