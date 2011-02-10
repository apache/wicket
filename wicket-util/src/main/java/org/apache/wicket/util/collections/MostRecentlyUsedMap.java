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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Holds a map of most recently used items of a given maximum size. Old entries are expired when the
 * map exceeds that maximum size.
 * 
 * @author Jonathan Locke
 * @param <K>
 *            key type
 * @param <V>
 *            value type
 */
public class MostRecentlyUsedMap<K, V> extends LinkedHashMap<K, V>
{
	private static final long serialVersionUID = 1L;

	/** Value most recently removed from map */
	protected V removedValue;

	/** Maximum number of entries allowed in this map */
	private final int maxEntries;

	/**
	 * Constructor
	 * 
	 * @param maxEntries
	 *            Maximum number of entries allowed in the map
	 */
	public MostRecentlyUsedMap(final int maxEntries)
	{
		super(10, 0.75f, true);

		if (maxEntries <= 0)
		{
			throw new IllegalArgumentException("Must have at least one entry");
		}

		this.maxEntries = maxEntries;
	}

	/**
	 * @return Returns the removedValue.
	 */
	public V getRemovedValue()
	{
		return removedValue;
	}

	/**
	 * @see java.util.LinkedHashMap#removeEldestEntry(java.util.Map.Entry)
	 */
	@Override
	protected boolean removeEldestEntry(final Map.Entry<K, V> eldest)
	{
		final boolean remove = size() > maxEntries;
		// when it should be removed remember the oldest value that will be removed
		if (remove)
		{
			this.removedValue = eldest.getValue();
		}
		else
		{
			removedValue = null;
		}
		return remove;
	}
}
