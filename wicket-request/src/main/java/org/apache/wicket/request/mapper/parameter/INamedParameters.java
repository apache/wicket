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
import org.apache.wicket.util.io.IClusterable;
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
	 * A hint where the parameter is read/parsed from.
	 */
	enum Type
	{
		/**
		 * The named parameter is set manually in the application code
		 */
		MANUAL,

		/**
		 * The named parameter is read/parsed from the query string
		 */
		QUERY_STRING,

		/**
		 * The named parameter is read/parsed from the url path
		 */
		PATH
	}

	/**
	 * Represents a named parameter entry. There can be multiple {@link NamedPair}s in
	 * {@link PageParameters} that have same key.
	 * 
	 * @author Matej Knopp
	 */
	public static class NamedPair implements IClusterable
	{
		private final String key;
		private final String value;
		private final Type type;

		/**
		 * Constructor
		 * 
		 * @param key
		 * @param value
		 */
		public NamedPair(final String key, final String value)
		{
			this(key, value, Type.MANUAL);
		}

		public NamedPair(final String key, final String value, Type type)
		{
			this.key = Args.notEmpty(key, "key");
			this.value = Args.notNull(value, "value");
			this.type = Args.notNull(type, "type");
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

		public Type getType()
		{
			return type;
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
	 * @param type
	 *          The type to filter
	 * @return All named parameters with the given type. If the type is {@code null} then returns all named parameters.
	 */
	List<NamedPair> getAllNamedByType(Type type);

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
	 * @param type
	 * @return this
	 */
	INamedParameters add(final String name, final Object value, Type type);

	/**
	 * Adds named parameter to a specified position. The {@link IRequestMapper}s may or may not take
	 * the order into account.
	 * 
	 * @param name
	 * @param value
	 * @param index
	 * @param type
	 * @return this
	 */
	INamedParameters add(final String name, final Object value, final int index, Type type);

	/**
	 * Sets the named parameter on specified position. The {@link IRequestMapper}s may or may not
	 * take the order into account.
	 * 
	 * @param name
	 * @param value
	 * @param index
	 * @param type
	 * @return this
	 */
	INamedParameters set(final String name, final Object value, final int index, Type type);

	/**
	 * Sets the value for named parameter with given name.
	 * 
	 * @param name
	 * @param value
	 * @param type
	 * @return this
	 */
	INamedParameters set(final String name, final Object value, Type type);

	/**
	 * Removes all named parameters.
	 * 
	 * @return this
	 */
	INamedParameters clearNamed();

}
