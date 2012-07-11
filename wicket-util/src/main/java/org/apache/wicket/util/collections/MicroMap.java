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

import java.io.Serializable;
import java.util.AbstractList;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * An implementation of the java.util.Map interface which can only hold a single object. This is
 * particularly useful to control memory usage in Wicket because many containers hold only a single
 * component.
 * 
 * @author Jonathan Locke
 * @param <K>
 *            Key type
 * @param <V>
 *            Value type
 */
public final class MicroMap<K, V> implements Map<K, V>, Serializable
{
	private static final long serialVersionUID = 1L;

	/** The maximum number of entries this map supports. */
	public static final int MAX_ENTRIES = 1;

	/** The one and only key in this tiny map */
	private K key;

	/** The value for the only key in this tiny map */
	private V value;

	/**
	 * Constructor
	 */
	public MicroMap()
	{
	}

	/**
	 * Constructs map with a single key and value pair.
	 * 
	 * @param key
	 *            The key
	 * @param value
	 *            The value
	 */
	public MicroMap(final K key, final V value)
	{
		put(key, value);
	}

	/**
	 * @return True if this MicroMap is full
	 */
	public boolean isFull()
	{
		return size() == MAX_ENTRIES;
	}

	/**
	 * @see java.util.Map#size()
	 */
	@Override
	public int size()
	{
		return (key != null) ? 1 : 0;
	}

	/**
	 * @see java.util.Map#isEmpty()
	 */
	@Override
	public boolean isEmpty()
	{
		return size() == 0;
	}

	/**
	 * @see java.util.Map#containsKey(java.lang.Object)
	 */
	@Override
	public boolean containsKey(final Object key)
	{
		return key.equals(this.key);
	}

	/**
	 * @see java.util.Map#containsValue(java.lang.Object)
	 */
	@Override
	public boolean containsValue(final Object value)
	{
		return value.equals(this.value);
	}

	/**
	 * @see java.util.Map#get(java.lang.Object)
	 */
	@Override
	public V get(final Object key)
	{
		if (key.equals(this.key))
		{
			return value;
		}

		return null;
	}

	/**
	 * @see java.util.Map#put(java.lang.Object, java.lang.Object)
	 */
	@Override
	public V put(final K key, final V value)
	{
		// Replace?
		if (key.equals(this.key))
		{
			final V oldValue = this.value;

			this.value = value;

			return oldValue;
		}
		else
		{
			// Is there room for a new entry?
			if (size() < MAX_ENTRIES)
			{
				// Store
				this.key = key;
				this.value = value;

				return null;
			}
			else
			{
				throw new IllegalStateException("Map full");
			}
		}
	}

	/**
	 * @see java.util.Map#remove(java.lang.Object)
	 */
	@Override
	public V remove(final Object key)
	{
		if (key.equals(this.key))
		{
			final V oldValue = value;

			this.key = null;
			value = null;

			return oldValue;
		}

		return null;
	}

	/**
	 * @see java.util.Map#putAll(java.util.Map)
	 */
	@Override
	public void putAll(final Map<? extends K, ? extends V> map)
	{
		if (map.size() <= MAX_ENTRIES)
		{
			final Entry<? extends K, ? extends V> e = map.entrySet().iterator().next();

			put(e.getKey(), e.getValue());
		}
		else
		{
			throw new IllegalStateException("Map full.  Cannot add " + map.size() + " entries");
		}
	}

	/**
	 * @see java.util.Map#clear()
	 */
	@Override
	public void clear()
	{
		key = null;
		value = null;
	}

	/**
	 * @see java.util.Map#keySet()
	 */
	@Override
	public Set<K> keySet()
	{
		return new AbstractSet<K>()
		{
			@Override
			public Iterator<K> iterator()
			{
				return new Iterator<K>()
				{
					@Override
					public boolean hasNext()
					{
						return index < MicroMap.this.size();
					}

					@Override
					public K next()
					{
						if (!hasNext())
						{
							throw new NoSuchElementException();
						}
						index++;

						return key;
					}

					@Override
					public void remove()
					{
						MicroMap.this.clear();
					}

					int index;
				};
			}

			@Override
			public int size()
			{
				return MicroMap.this.size();
			}
		};
	}

	/**
	 * @see java.util.Map#values()
	 */
	@Override
	public Collection<V> values()
	{
		return new AbstractList<V>()
		{
			@Override
			public V get(final int index)
			{
				if (index > size() - 1)
				{
					throw new IndexOutOfBoundsException();
				}
				return value;
			}

			@Override
			public int size()
			{
				return MicroMap.this.size();
			}
		};
	}

	/**
	 * @see java.util.Map#entrySet()
	 */
	@Override
	public Set<Entry<K, V>> entrySet()
	{
		return new AbstractSet<Entry<K, V>>()
		{
			@Override
			public Iterator<Entry<K, V>> iterator()
			{
				return new Iterator<Entry<K, V>>()
				{
					@Override
					public boolean hasNext()
					{
						return index < MicroMap.this.size();
					}

					@Override
					public Entry<K, V> next()
					{
						if (!hasNext())
						{
							throw new NoSuchElementException();
						}
						index++;

						return new Map.Entry<K, V>()
						{
							@Override
							public K getKey()
							{
								return key;
							}

							@Override
							public V getValue()
							{
								return value;
							}

							@Override
							public V setValue(final V value)
							{
								final V oldValue = MicroMap.this.value;

								MicroMap.this.value = value;

								return oldValue;
							}
						};
					}

					@Override
					public void remove()
					{
						clear();
					}

					int index = 0;
				};
			}

			@Override
			public int size()
			{
				return MicroMap.this.size();
			}
		};
	}
}
