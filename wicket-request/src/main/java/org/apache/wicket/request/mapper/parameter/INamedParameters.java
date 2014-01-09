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

import java.util.List;
import java.util.Set;

import org.apache.wicket.request.IRequestMapper;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.string.StringValue;

/**
 * Container for parameters that are identified by their name
 * 
 * @author igor
 */
public interface INamedParameters
{
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
			this.key = Args.notNull(key, "key");;
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

		@Override
		public boolean equals(Object o)
		{
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			NamedPair namedPair = (NamedPair) o;

			if (key != null ? !key.equals(namedPair.key) : namedPair.key != null) return false;
			if (value != null ? !value.equals(namedPair.value) : namedPair.value != null) return false;

			return true;
		}

		@Override
		public int hashCode()
		{
			int result = key != null ? key.hashCode() : 0;
			result = 31 * result + (value != null ? value.hashCode() : 0);
			return result;
		}
	}


	/**
	 * Return set of all named parameter names.
	 * 
	 * @return named parameter names
	 */
	Set<String> getNamedKeys();

	/**
	 * Returns parameter value of named parameter with given name
	 * 
	 * @param name
	 * @return parameter value
	 */
	StringValue get(final String name);

	/**
	 * Return list of all values for named parameter with given name
	 * 
	 * @param name
	 * @return list of parameter values
	 */
	List<StringValue> getValues(final String name);

	/**
	 * @return All named parameters in exact order.
	 */
	List<NamedPair> getAllNamed();

	/**
	 * Returns the position of a named parameter.
	 * 
	 * @param name
	 *            the name of the parameter to look for
	 * @return the position of the parameter. {@code -1} if there is no parameter with that name.
	 */
	int getPosition(String name);

	/**
	 * Removes named parameter with given name.
	 * 
	 * @param name
	 *            the name of the parameter to remove
	 * @param values
	 *            values used as criteria. The parameter will be removed only if its value is equal
	 *            to any of the criteria.
	 * @return this
	 */
	INamedParameters remove(final String name, String... values);

	/**
	 * Adds value to named parameter with given name.
	 * 
	 * @param name
	 * @param value
	 * @return this
	 */
	INamedParameters add(final String name, final Object value);

	/**
	 * Adds named parameter to a specified position. The {@link IRequestMapper}s may or may not take
	 * the order into account.
	 * 
	 * @param name
	 * @param value
	 * @param index
	 * @return this
	 */
	INamedParameters add(final String name, final Object value, final int index);

	/**
	 * Sets the named parameter on specified position. The {@link IRequestMapper}s may or may not
	 * take the order into account.
	 * 
	 * @param name
	 * @param value
	 * @param index
	 * @return this
	 */
	INamedParameters set(final String name, final Object value, final int index);

	/**
	 * Sets the value for named parameter with given name.
	 * 
	 * @param name
	 * @param value
	 * @return this
	 */
	INamedParameters set(final String name, final Object value);

	/**
	 * Removes all named parameters.
	 * 
	 * @return this
	 */
	INamedParameters clearNamed();

}