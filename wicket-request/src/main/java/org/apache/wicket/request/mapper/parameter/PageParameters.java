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

import org.apache.wicket.request.IRequestMapper;
import org.apache.wicket.util.io.IClusterable;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Objects;
import org.apache.wicket.util.string.StringValue;
import org.apache.wicket.util.string.Strings;

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
	private static final long serialVersionUID = 1L;

	private List<String> indexedParameters;

	private List<NamedPair> namedParameters;

	/**
	 * Constructor.
	 */
	public PageParameters()
	{
	}

	/**
	 * Copy constructor.
	 * 
	 * @param copy
	 *          The parameters to copy from
	 */
	public PageParameters(final PageParameters copy)
	{
		if (copy != null)
		{
			if (copy.indexedParameters != null)
			{
				indexedParameters = new ArrayList<>(copy.indexedParameters);
			}

			if (copy.namedParameters != null)
			{
				namedParameters = new ArrayList<>(copy.namedParameters);
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
	@Override
	public PageParameters set(final int index, final Object object)
	{
		if (indexedParameters == null)
		{
			indexedParameters = new ArrayList<>(index);
		}

		for (int i = indexedParameters.size(); i <= index; ++i)
		{
			indexedParameters.add(null);
		}

		indexedParameters.set(index, Strings.toString(object));
		return this;
	}

	@Override
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

	@Override
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

	@Override
	public Set<String> getNamedKeys()
	{
		if ((namedParameters == null) || namedParameters.isEmpty())
		{
			return Collections.emptySet();
		}
		Set<String> set = new TreeSet<>();
		for (NamedPair entry : namedParameters)
		{
			set.add(entry.getKey());
		}
		return Collections.unmodifiableSet(set);
	}

	@Override
	public StringValue get(final String name)
	{
		Args.notNull(name, "name");

		if (namedParameters != null)
		{
			for (NamedPair entry : namedParameters)
			{
				if (entry.getKey().equals(name))
				{
					return StringValue.valueOf(entry.getValue());
				}
			}
		}
		return StringValue.valueOf((String)null);
	}

	@Override
	public List<StringValue> getValues(final String name)
	{
		Args.notNull(name, "name");

		if (namedParameters != null)
		{
			List<StringValue> result = new ArrayList<>();
			for (NamedPair entry : namedParameters)
			{
				if (entry.getKey().equals(name))
				{
					result.add(StringValue.valueOf(entry.getValue()));
				}
			}
			return Collections.unmodifiableList(result);
		}
		else
		{
			return Collections.emptyList();
		}
	}

	@Override
	public List<NamedPair> getAllNamed()
	{
		return namedParameters != null ? Collections.unmodifiableList(namedParameters) : Collections.<NamedPair>emptyList();
	}


	@Override
	public List<NamedPair> getAllNamedByType(Type type)
	{
		List<NamedPair> allNamed = getAllNamed();
		if (type == null || allNamed.isEmpty())
		{
			return allNamed;
		}

		List<NamedPair> parametersByType = new ArrayList<>();
		Iterator<NamedPair> iterator = allNamed.iterator();
		while (iterator.hasNext())
		{
			NamedPair pair = iterator.next();
			if (type == pair.getType())
			{
				parametersByType.add(pair);
			}
		}
		return Collections.unmodifiableList(parametersByType);
	}

	@Override
	public int getPosition(final String name)
	{
		int index = -1;
		if (namedParameters != null)
		{
			for (int i = 0; i < namedParameters.size(); i++)
			{
				NamedPair entry = namedParameters.get(i);
				if (entry.getKey().equals(name))
				{
					index = i;
					break;
				}
			}
		}
		return index;
	}

	@Override
	public PageParameters remove(final String name, final String... values)
	{
		Args.notNull(name, "name");

		if (namedParameters != null)
		{
			for (Iterator<NamedPair> i = namedParameters.iterator(); i.hasNext();)
			{
				NamedPair e = i.next();
				if (e.getKey().equals(name))
				{
					if (values != null && values.length > 0)
					{
						for (String value : values)
						{
							if (e.getValue().equals(value))
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

	@Override
	public PageParameters add(final String name, final Object value, Type type)
	{
		add(name, value, -1, Type.MANUAL);
		return this;
	}

	@Override
	public PageParameters add(final String name, final Object value, final int index, Type type)
	{
		Args.notEmpty(name, "name");
		Args.notNull(value, "value");

		if (namedParameters == null)
		{
			namedParameters = new ArrayList<>(1);
		}

		List<String> values = new ArrayList<>();
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
			NamedPair entry = new NamedPair(name, val, type);

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

	@Override
	public PageParameters set(final String name, final Object value, final int index, Type type)
	{
		remove(name);

		if (value != null)
		{
			add(name, value, index, type);
		}
		return this;
	}

	@Override
	public PageParameters set(final String name, final Object value, Type type)
	{
		int position = getPosition(name);
		set(name, value, position, type);
		return this;
	}

	@Override
	public PageParameters clearIndexed()
	{
		indexedParameters = null;
		return this;
	}

	@Override
	public PageParameters clearNamed()
	{
		namedParameters = null;
		return this;
	}

	/**
	 * Copy the page parameters
	 * 
	 * @param other
	 *          The new parameters
	 * @return this instance, for chaining
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
	 *          The parameters to merge
	 * @return this instance, for chaining
	 */
	public PageParameters mergeWith(final PageParameters other)
	{
		if (this != other)
		{
			for (int index = 0; index < other.getIndexedCount(); index++)
			{
				if (!other.get(index).isNull())
				{
					set(index, other.get(index));
				}
			}
			for (String name : other.getNamedKeys())
			{
				remove(name);
			}
			for (NamedPair curNamed : other.getAllNamed())
			{
				add(curNamed.getKey(), curNamed.getValue(), curNamed.getType());
			}
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
	 *          The first parameters
	 * @param p2
	 *          The second parameters
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
				NamedPair entry = namedParameters.get(i);

				if (i > 0)
				{
					str.append(", ");
				}

				str.append(entry.getKey());
				str.append('=');
				str.append('[').append(entry.getValue()).append(']');
			}
		}
		return str.toString();
	}
}
