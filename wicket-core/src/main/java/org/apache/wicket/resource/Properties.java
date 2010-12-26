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
package org.apache.wicket.resource;

import org.apache.wicket.util.value.ValueMap;

/**
 * Kind of like java.util.Properties but based on Wicket's ValueMap and thus benefiting from all its
 * nice build-in type converters and without parent properties.
 * 
 * @author Juergen Donnerstag
 */
public final class Properties
{
	/** Empty Properties */
	public static final Properties EMPTY_PROPERTIES = new Properties("NULL", ValueMap.EMPTY_MAP);

	/** A unique key for this specific group of properties. */
	private final String key;

	/** Property values */
	private final ValueMap strings;

	/**
	 * Construct
	 * 
	 * @param key
	 *            The key
	 * @param strings
	 *            Properties values
	 */
	public Properties(final String key, final ValueMap strings)
	{
		this.key = key;
		this.strings = strings;
	}

	/**
	 * Get direct access to all values from the properties file.
	 * 
	 * @return map
	 */
	public final ValueMap getAll()
	{
		return strings;
	}

	/**
	 * Get the property value identified by its 'key'.
	 * 
	 * @param key
	 * @return property message
	 */
	public final String getString(final String key)
	{
		return strings.getString(key);
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public final String toString()
	{
		return "unique key:" + key;
	}
}