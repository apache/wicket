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
package org.apache.wicket.request.mapper.parameter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.wicket.IClusterable;
import org.apache.wicket.request.IRequestMapper;
import org.apache.wicket.util.lang.Args;
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
 * <strong>Indexed vs Named Parameters</strong>: Suppose we mounted a page on {@code /user} and the
 * following url was accessed {@code /user/profile/bob?action=view&redirect=false}. In this example
 * {@code profile} and {@code bob} are indexed parameters with respective indexes 0 and 1.
 * {@code action} and {@code redirect} are named parameters.
 * </p>
 * <p>
 * How those parameters are populated depends on the {@link IRequestMapper}s
 * 
 * @author Matej Knopp
 */
public class PageParameters implements IClusterable, IIndexedParameters, INamedParameters
{
	private static class Entry implements IClusterable
	{
		private static final long serialVersionUID = 1L;

		private String key;
		private String value;

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((key == null) ? 0 : key.hashCode());
			result = prime * result + ((value == null) ? 0 : value.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Entry other = (Entry)obj;
			if (key == null)
			{
				if (other.key != null)
					return false;
			}
			else if (!key.equals(other.key))
				return false;
			if (value == null)
			{
				if (other.value != null)
					return false;
			}
			else if (!value.equals(other.value))
				return false;
			return true;
		}
	}

	private static final long serialVersionUID = 1L;

	private List<String> indexedParameters;

	private List<Entry> namedParameters;

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
	public PageParameters(final PageParameters copy)
	{
		if (copy != null)
		{
			if (copy.indexedParameters != null)
			{
				indexedParameters = new ArrayList<String>(copy.indexedParameters);
			}

			if (copy.namedParameters != null)
			{
				namedParameters = new ArrayList<Entry>(copy.namedParameters);
			}
		}
	}


	/**
	 * Construct.
	 * 
	 * @param keyValuePairs
	 *            List of key value pairs separated by commas. For example, "param1=foo,param2=bar"
	 * @see ValueMap#ValueMap(String)
	 * @deprecated use various setter methods to set parameters
	 */
	@Deprecated
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
	 * @deprecated use various setter methods to set parameters
	 */
	@Deprecated
	public PageParameters(final String keyValuePairs, final String delimiter)
	{
		super();

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
				// null value is not allowed by #add
				final String value = "";

				add(key, value);
			}
		}
	}


	/**
	 * @return count of indexed parameters
	 */
	public int getIndexedCount()
	{
		return indexedParameters != null ? indexedParameters.size() : 0;
	}

	/**
	 * @see org.apache.wicket.request.mapper.parameter.IIndexedParameters#set(int, java.lang.Object)
	 */
	public PageParameters set(final int index, final Object object)
	{
		if (indexedParameters == null)
		{
			indexedParameters = new ArrayList<String>(index);
		}

		for (int i = indexedParameters.size(); i <= index; ++i)
		{
			indexedParameters.add(null);
		}

		indexedParameters.set(index, object != null ? object.toString() : null);
		return this;
	}

	/**
	 * @see org.apache.wicket.request.mapper.parameter.IIndexedParameters#get(int)
	 */
	public StringValue get(final int index)
	{
		if (indexedParameters != null)
		{
			if ((index >= 0) && (index < indexedParameters.size()))
			{
				return StringValue.valueOf(indexedParameters.get(index));
			}
		}
		return StringValue.valueOf((String)null);
	}

	/**
	 * @see org.apache.wicket.request.mapper.parameter.IIndexedParameters#remove(int)
	 */
	public PageParameters remove(final int index)
	{
		if (indexedParameters != null)
		{
			if ((index >= 0) && (index < indexedParameters.size()))
			{
				indexedParameters.remove(index);
			}
		}
		return this;
	}

	/**
	 * @see org.apache.wicket.request.mapper.parameter.INamedParameters#getNamedKeys()
	 */
	public Set<String> getNamedKeys()
	{
		if ((namedParameters == null) || namedParameters.isEmpty())
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
	 * @see org.apache.wicket.request.mapper.parameter.INamedParameters#get(java.lang.String)
	 */
	public StringValue get(final String name)
	{
		Args.notNull(name, "name");

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
	 * @see org.apache.wicket.request.mapper.parameter.INamedParameters#getValues(java.lang.String)
	 */
	public List<StringValue> getValues(final String name)
	{
		Args.notNull(name, "name");

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
	 * Represents a named parameter entry. There can be multiple {@link NamedPair}s in
	 * {@link PageParameters} that have same key.
	 * 
	 * @author Matej Knopp
	 */
	public static class NamedPair
	{
		private final String key;
		private final String value;

		/**
		 * Constructor
		 * 
		 * @param key
		 * @param value
		 */
		public NamedPair(final String key, final String value)
		{
			this.key = Args.notNull(key, "key");
			this.value = Args.notNull(value, "value");
		}

		/**
		 * @return key
		 */
		public String getKey()
		{
			return key;
		}

		/**
		 * @return value
		 */
		public String getValue()
		{
			return value;
		}
	}

	/**
	 * @see org.apache.wicket.request.mapper.parameter.INamedParameters#getAllNamed()
	 */
	public List<NamedPair> getAllNamed()
	{
		List<NamedPair> res = new ArrayList<NamedPair>();
		if (namedParameters != null)
		{
			for (Entry e : namedParameters)
			{
				res.add(new NamedPair(e.key, e.value));
			}
		}
		return Collections.unmodifiableList(res);
	}

	/**
	 * @see org.apache.wicket.request.mapper.parameter.INamedParameters#getPosition(String)
	 */
	public int getPosition(final String name)
	{
		int index = -1;
		if (namedParameters != null)
		{
			for (int i = 0; i < namedParameters.size(); i++)
			{
				Entry entry = namedParameters.get(i);
				if (entry.key.equals(name))
				{
					index = i;
					break;
				}
			}
		}
		return index;
	}

	/**
	 * @see org.apache.wicket.request.mapper.parameter.INamedParameters#remove(java.lang.String)
	 */
	// TODO Wicket 1.6 - remove this method and leave only #remove(String, String...)
	public PageParameters remove(final String name)
	{
		return remove(name, new String[0]);
	}

	/**
	 * @see org.apache.wicket.request.mapper.parameter.INamedParameters#remove(java.lang.String,
	 *      java.lang.String...)
	 */
	public PageParameters remove(final String name, final String... values)
	{
		Args.notNull(name, "name");

		if (namedParameters != null)
		{
			for (Iterator<Entry> i = namedParameters.iterator(); i.hasNext();)
			{
				Entry e = i.next();
				if (e.key.equals(name))
				{
					if (values != null && values.length > 0)
					{
						for (String value : values)
						{
							if (e.value.equals(value))
							{
								i.remove();
								break;
							}
						}
					}
					else
					{
						i.remove();
					}
				}
			}
		}
		return this;
	}

	/**
	 * @see org.apache.wicket.request.mapper.parameter.INamedParameters#add(java.lang.String,
	 *      java.lang.Object)
	 */
	public PageParameters add(final String name, final Object value)
	{
		add(name, value, -1);
		return this;
	}

	/**
	 * @see org.apache.wicket.request.mapper.parameter.INamedParameters#add(java.lang.String,
	 *      java.lang.Object, int)
	 */
	public PageParameters add(final String name, final Object value, final int index)
	{
		Args.notNull(name, "name");
		Args.notNull(value, "value");

		if (namedParameters == null)
		{
			namedParameters = new ArrayList<Entry>(1);
		}

		List<String> values = new ArrayList<String>();
		if (value instanceof String[])
		{
			values.addAll(Arrays.asList((String[])value));
		}
		else
		{
			values.add(value.toString());
		}

		for (String val : values)
		{
			Entry entry = new Entry();
			entry.key = name;
			entry.value = val;

			if (index < 0 || index > namedParameters.size())
			{
				namedParameters.add(entry);
			}
			else
			{
				namedParameters.add(index, entry);
			}
		}
		return this;
	}

	/**
	 * @see org.apache.wicket.request.mapper.parameter.INamedParameters#set(java.lang.String,
	 *      java.lang.Object, int)
	 */
	public PageParameters set(final String name, final Object value, final int index)
	{
		remove(name);

		if (value != null)
		{
			add(name, value, index);
		}
		return this;
	}

	/**
	 * @see org.apache.wicket.request.mapper.parameter.INamedParameters#set(java.lang.String,
	 *      java.lang.Object)
	 */
	public PageParameters set(final String name, final Object value)
	{
		int position = getPosition(name);
		set(name, value, position);
		return this;
	}

	/**
	 * @see org.apache.wicket.request.mapper.parameter.IIndexedParameters#clearIndexed()
	 */
	public PageParameters clearIndexed()
	{
		indexedParameters = null;
		return this;
	}

	/**
	 * @see org.apache.wicket.request.mapper.parameter.INamedParameters#clearNamed()
	 */
	public PageParameters clearNamed()
	{
		namedParameters = null;
		return this;
	}

	/**
	 * Copy the page parameters
	 * 
	 * @param other
	 * @return this
	 */
	public PageParameters overwriteWith(final PageParameters other)
	{
		if (this != other)
		{
			indexedParameters = other.indexedParameters;
			namedParameters = other.namedParameters;
		}
		return this;
	}

	/**
	 * Merges the page parameters into this, overwriting existing values
	 * 
	 * @param other
	 * @return this
	 */
	public PageParameters mergeWith(final PageParameters other)
	{
		if (this != other)
		{
			for (int index = 0; index < other.getIndexedCount(); index++)
				set(index, other.get(index));
			for (NamedPair curNamed : other.getAllNamed())
				set(curNamed.getKey(), curNamed.getValue());
		}
		return this;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((indexedParameters == null) ? 0 : indexedParameters.hashCode());
		result = prime * result + ((namedParameters == null) ? 0 : namedParameters.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PageParameters other = (PageParameters)obj;
		if (indexedParameters == null)
		{
			if (other.indexedParameters != null)
				return false;
		}
		else if (!indexedParameters.equals(other.indexedParameters))
			return false;
		if (namedParameters == null)
		{
			if (other.namedParameters != null)
				return false;
		}
		else if (!namedParameters.equals(other.namedParameters))
			return false;
		return true;
	}

	/**
	 * Compares two {@link PageParameters} objects.
	 * 
	 * @param p1
	 * @param p2
	 * @return <code>true</code> if the objects are equal, <code>false</code> otherwise.
	 */
	public static boolean equals(final PageParameters p1, final PageParameters p2)
	{
		if (Objects.equal(p1, p2))
		{
			return true;
		}
		if ((p1 == null) && (p2.getIndexedCount() == 0) && p2.getNamedKeys().isEmpty())
		{
			return true;
		}
		if ((p2 == null) && (p1.getIndexedCount() == 0) && p1.getNamedKeys().isEmpty())
		{
			return true;
		}
		return false;
	}

	/**
	 * @return <code>true</code> if the parameters are empty, <code>false</code> otherwise.
	 */
	public boolean isEmpty()
	{
		return (getIndexedCount() == 0) && getNamedKeys().isEmpty();
	}

	@Override
	public String toString()
	{
		StringBuilder str = new StringBuilder();

		if (indexedParameters != null)
		{
			for (int i = 0; i < indexedParameters.size(); i++)
			{
				if (i > 0)
				{
					str.append(", ");
				}

				str.append(i);
				str.append('=');
				str.append('[').append(indexedParameters.get(i)).append(']');
			}
		}

		if (str.length() > 0)
		{
			str.append(", ");
		}

		if (namedParameters != null)
		{
			for (int i = 0; i < namedParameters.size(); i++)
			{
				Entry entry = namedParameters.get(i);

				if (i > 0)
				{
					str.append(", ");
				}

				str.append(entry.key);
				str.append('=');
				str.append('[').append(entry.value).append(']');
			}
		}
		return str.toString();
	}
}
