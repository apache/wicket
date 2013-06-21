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
package org.apache.wicket.util.collections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple multimap
 * 
 * @author igor
 * @param <K>
 * @param <V>
 */
public class MultiMap<K, V> extends HashMap<K, List<V>>
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * 
	 * @see HashMap#HashMap()
	 */
	public MultiMap()
	{
	}

	/**
	 * Constructor
	 * 
	 * @param initialCapacity
	 * @param loadFactor
	 * 
	 * @see HashMap#HashMap(int, float)
	 */
	public MultiMap(final int initialCapacity, final float loadFactor)
	{
		super(initialCapacity, loadFactor);
	}

	/**
	 * Constructor
	 * 
	 * @param initialCapacity
	 * 
	 * @see HashMap#HashMap(int)
	 */
	public MultiMap(final int initialCapacity)
	{
		super(initialCapacity);
	}

	/**
	 * Constructor
	 * 
	 * @param m
	 * 
	 * @see HashMap#HashMap(Map)
	 */
	public MultiMap(final Map<? extends K, ? extends List<V>> m)
	{
		super(m);
	}

	/**
	 * Adds value to the specified key
	 * 
	 * @param key
	 * @param value
	 */
	public void addValue(final K key, final V value)
	{
		List<V> list = get(key);
		if (list == null)
		{
			list = new ArrayList<>(1);
			put(key, list);
		}
		list.add(value);
	}

	/**
	 * Removes value from the specified key
	 * 
	 * @param key
	 * @param value
	 */
	public void removeValue(final K key, final V value)
	{
		List<V> list = get(key);
		if (list != null)
		{
			list.remove(value);
		}
	}

	/**
	 * Replaces all existing values with the specified value. If no values exist for the key the
	 * value will be added.
	 * 
	 * @param key
	 * @param value
	 */
	public void replaceValues(final K key, final V value)
	{
		List<V> list = get(key);
		if (list != null)
		{
			list.clear();
			list.add(value);
		}
		else
		{
			addValue(key, value);
		}
	}

	/**
	 * Gets the first value in the value list
	 * 
	 * @param key
	 * @return first value
	 */
	public V getFirstValue(final K key)
	{
		List<V> list = get(key);
		if ((list != null) && !list.isEmpty())
		{
			return list.get(0);
		}
		return null;
	}
}
