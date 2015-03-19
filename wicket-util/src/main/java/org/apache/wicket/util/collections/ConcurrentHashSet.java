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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class implements the <tt>Set</tt> interface, backed by a ConcurrentHashMap instance.
 * 
 * @author Matt Tucker
 * @param <E>
 */
public class ConcurrentHashSet<E> extends AbstractSet<E>
	implements
		Set<E>,
		Cloneable,
		java.io.Serializable
{
	/** */
	private static final long serialVersionUID = 1L;

	private transient ConcurrentHashMap<E, Object> map;

	// Dummy value to associate with an Object in the backing Map
	private static final Object PRESENT = new Object();

	/**
	 * Constructs a new, empty set; the backing <tt>ConcurrentHashMap</tt> instance has default
	 * initial capacity (16) and load factor (0.75).
	 */
	public ConcurrentHashSet()
	{
		map = new ConcurrentHashMap<>();
	}

	/**
	 * Constructs a new set containing the elements in the specified collection. The
	 * <tt>ConcurrentHashMap</tt> is created with default load factor (0.75) and an initial capacity
	 * sufficient to contain the elements in the specified collection.
	 * 
	 * @param c
	 *            the collection whose elements are to be placed into this set.
	 * @throws NullPointerException
	 *             if the specified collection is null.
	 */
	public ConcurrentHashSet(final Collection<? extends E> c)
	{
		map = new ConcurrentHashMap<>(Math.max((int)(c.size() / .75f) + 1, 16));
		addAll(c);
	}

	/**
	 * Constructs a new, empty set; the backing <tt>ConcurrentHashMap</tt> instance has the
	 * specified initial capacity and the specified load factor.
	 * 
	 * @param initialCapacity
	 *            the initial capacity of the hash map.
	 * @param loadFactor
	 *            the load factor of the hash map.
	 * @throws IllegalArgumentException
	 *             if the initial capacity is less than zero, or if the load factor is nonpositive.
	 */
	public ConcurrentHashSet(final int initialCapacity, final float loadFactor)
	{
		map = new ConcurrentHashMap<>(initialCapacity, loadFactor, 16);
	}

	/**
	 * Constructs a new, empty set; the backing <tt>HashMap</tt> instance has the specified initial
	 * capacity and default load factor, which is <tt>0.75</tt>.
	 * 
	 * @param initialCapacity
	 *            the initial capacity of the hash table.
	 * @throws IllegalArgumentException
	 *             if the initial capacity is less than zero.
	 */
	public ConcurrentHashSet(final int initialCapacity)
	{
		map = new ConcurrentHashMap<>(initialCapacity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<E> iterator()
	{
		return map.keySet().iterator();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int size()
	{
		return map.size();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isEmpty()
	{
		return map.isEmpty();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean contains(final Object o)
	{
		return map.containsKey(o);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean add(final E o)
	{
		return map.put(o, PRESENT) == null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean remove(final Object o)
	{
		return map.remove(o) == PRESENT;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear()
	{
		map.clear();
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Object clone() throws CloneNotSupportedException
	{
		try
		{
			ConcurrentHashSet<E> newSet = (ConcurrentHashSet<E>)super.clone();
			newSet.map.putAll(map);
			return newSet;
		}
		catch (CloneNotSupportedException e)
		{
			throw new InternalError();
		}
	}

	/**
	 * 
	 * @param s
	 * @throws java.io.IOException
	 */
	private void writeObject(final java.io.ObjectOutputStream s) throws java.io.IOException
	{
		s.defaultWriteObject();
		s.writeInt(map.size());

		for (E key : map.keySet())
		{
			s.writeObject(key);
		}
	}

	/**
	 * Re-constitute the <tt>HashSet</tt> instance from a stream.
	 * 
	 * @param inputStream
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	private void readObject(final ObjectInputStream inputStream) throws ClassNotFoundException,
		IOException
	{
		inputStream.defaultReadObject();

		map = new ConcurrentHashMap<>();

		int size = inputStream.readInt();
		for (int i = 0; i < size; i++)
		{
			E e = (E)inputStream.readObject();
			map.put(e, PRESENT);
		}
	}
}