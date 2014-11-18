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
import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This is an integer hashmap that has the exact same features and interface as a normal Map except
 * that the key is directly an integer. So no hash is calculated or key object is stored.
 * 
 * @author jcompagner
 * 
 * @param <V>
 *            The value in the map
 */
public class IntHashMap<V> implements Cloneable, Serializable
{
	transient volatile Set<Integer> keySet = null;

	transient volatile Collection<V> values = null;

	/**
	 * The default initial capacity - MUST be a power of two.
	 */
	static final int DEFAULT_INITIAL_CAPACITY = 16;

	/**
	 * The maximum capacity, used if a higher value is implicitly specified by either of the
	 * constructors with arguments. MUST be a power of two <= 1<<30.
	 */
	static final int MAXIMUM_CAPACITY = 1 << 30;

	/**
	 * The load factor used when none specified in constructor.
	 */
	static final float DEFAULT_LOAD_FACTOR = 0.75f;

	/**
	 * The table, resized as necessary. Length MUST Always be a power of two.
	 */
	transient Entry<V>[] table;

	/**
	 * The number of key-value mappings contained in this identity hash map.
	 */
	transient int size;

	/**
	 * The next size value at which to resize (capacity * load factor).
	 * 
	 * @serial
	 */
	int threshold;

	/**
	 * The load factor for the hash table.
	 * 
	 * @serial
	 */
	final float loadFactor;

	/**
	 * The number of times this HashMap has been structurally modified Structural modifications are
	 * those that change the number of mappings in the HashMap or otherwise modify its internal
	 * structure (e.g., rehash). This field is used to make iterators on Collection-views of the
	 * HashMap fail-fast. (See ConcurrentModificationException).
	 */
	transient AtomicInteger modCount = new AtomicInteger(0);

	/**
	 * Constructs an empty <tt>HashMap</tt> with the specified initial capacity and load factor.
	 * 
	 * @param initialCapacity
	 *            The initial capacity.
	 * @param loadFactor
	 *            The load factor.
	 * @throws IllegalArgumentException
	 *             if the initial capacity is negative or the load factor is nonpositive.
	 */
	@SuppressWarnings("unchecked")
	public IntHashMap(int initialCapacity, final float loadFactor)
	{
		if (initialCapacity < 0)
		{
			throw new IllegalArgumentException("Illegal initial capacity: " + //$NON-NLS-1$
				initialCapacity);
		}
		if (initialCapacity > MAXIMUM_CAPACITY)
		{
			initialCapacity = MAXIMUM_CAPACITY;
		}
		if ((loadFactor <= 0) || Float.isNaN(loadFactor))
		{
			throw new IllegalArgumentException("Illegal load factor: " + //$NON-NLS-1$
				loadFactor);
		}

		// Find a power of 2 >= initialCapacity
		int capacity = 1;
		while (capacity < initialCapacity)
		{
			capacity <<= 1;
		}

		this.loadFactor = loadFactor;
		threshold = (int)(capacity * loadFactor);
		table = new Entry[capacity];
		init();
	}

	/**
	 * Constructs an empty <tt>HashMap</tt> with the specified initial capacity and the default load
	 * factor (0.75).
	 * 
	 * @param initialCapacity
	 *            the initial capacity.
	 * @throws IllegalArgumentException
	 *             if the initial capacity is negative.
	 */
	public IntHashMap(final int initialCapacity)
	{
		this(initialCapacity, DEFAULT_LOAD_FACTOR);
	}

	/**
	 * Constructs an empty <tt>HashMap</tt> with the default initial capacity (16) and the default
	 * load factor (0.75).
	 */
	@SuppressWarnings("unchecked")
	public IntHashMap()
	{
		loadFactor = DEFAULT_LOAD_FACTOR;
		threshold = (int)(DEFAULT_INITIAL_CAPACITY * DEFAULT_LOAD_FACTOR);
		table = new Entry[DEFAULT_INITIAL_CAPACITY];
		init();
	}

	// internal utilities

	/**
	 * Initialization hook for subclasses. This method is called in all constructors and
	 * pseudo-constructors (clone, readObject) after HashMap has been initialized but before any
	 * entries have been inserted. (In the absence of this method, readObject would require explicit
	 * knowledge of subclasses.)
	 */
	void init()
	{
	}

	/**
	 * Returns index for hash code h.
	 * 
	 * @param h
	 * @param length
	 * @return The index for the hash integer for the given length
	 */
	static int indexFor(final int h, final int length)
	{
		return h & (length - 1);
	}

	/**
	 * Returns the number of key-value mappings in this map.
	 * 
	 * @return the number of key-value mappings in this map.
	 */
	public int size()
	{
		return size;
	}

	/**
	 * Returns <tt>true</tt> if this map contains no key-value mappings.
	 * 
	 * @return <tt>true</tt> if this map contains no key-value mappings.
	 */
	public boolean isEmpty()
	{
		return size == 0;
	}

	/**
	 * Returns the value to which the specified key is mapped in this identity hash map, or
	 * <tt>null</tt> if the map contains no mapping for this key. A return value of <tt>null</tt>
	 * does not <i>necessarily</i> indicate that the map contains no mapping for the key; it is also
	 * possible that the map explicitly maps the key to <tt>null</tt>. The <tt>containsKey</tt>
	 * method may be used to distinguish these two cases.
	 * 
	 * @param key
	 *            the key whose associated value is to be returned.
	 * @return the value to which this map maps the specified key, or <tt>null</tt> if the map
	 *         contains no mapping for this key.
	 * @see #put(int, Object)
	 */
	public V get(final int key)
	{
		int i = indexFor(key, table.length);
		Entry<V> e = table[i];
		while (true)
		{
			if (e == null)
			{
				return null;
			}
			if (key == e.key)
			{
				return e.value;
			}
			e = e.next;
		}
	}

	/**
	 * Returns <tt>true</tt> if this map contains a mapping for the specified key.
	 * 
	 * @param key
	 *            The key whose presence in this map is to be tested
	 * @return <tt>true</tt> if this map contains a mapping for the specified key.
	 */
	public boolean containsKey(final int key)
	{
		int i = indexFor(key, table.length);
		Entry<V> e = table[i];
		while (e != null)
		{
			if (key == e.key)
			{
				return true;
			}
			e = e.next;
		}
		return false;
	}

	/**
	 * Returns the entry associated with the specified key in the HashMap. Returns null if the
	 * HashMap contains no mapping for this key.
	 * 
	 * @param key
	 * @return The Entry object for the given hash key
	 */
	Entry<V> getEntry(final int key)
	{
		int i = indexFor(key, table.length);
		Entry<V> e = table[i];
		while ((e != null) && !(key == e.key))
		{
			e = e.next;
		}
		return e;
	}

	/**
	 * Associates the specified value with the specified key in this map. If the map previously
	 * contained a mapping for this key, the old value is replaced.
	 * 
	 * @param key
	 *            key with which the specified value is to be associated.
	 * @param value
	 *            value to be associated with the specified key.
	 * @return previous value associated with specified key, or <tt>null</tt> if there was no
	 *         mapping for key. A <tt>null</tt> return can also indicate that the HashMap previously
	 *         associated <tt>null</tt> with the specified key.
	 */
	public V put(final int key, final V value)
	{
		int i = indexFor(key, table.length);

		for (Entry<V> e = table[i]; e != null; e = e.next)
		{
			if (key == e.key)
			{
				V oldValue = e.value;
				e.value = value;
				return oldValue;
			}
		}

		modCount.incrementAndGet();
		addEntry(key, value, i);
		return null;
	}

	/**
	 * This method is used instead of put by constructors and pseudoconstructors (clone,
	 * readObject). It does not resize the table, check for comodification, etc. It calls
	 * createEntry rather than addEntry.
	 * 
	 * @param key
	 * @param value
	 */
	private void putForCreate(final int key, final V value)
	{
		int i = indexFor(key, table.length);

		/**
		 * Look for preexisting entry for key. This will never happen for clone or deserialize. It
		 * will only happen for construction if the input Map is a sorted map whose ordering is
		 * inconsistent w/ equals.
		 */
		for (Entry<V> e = table[i]; e != null; e = e.next)
		{
			if (key == e.key)
			{
				e.value = value;
				return;
			}
		}

		createEntry(key, value, i);
	}

	void putAllForCreate(final IntHashMap<V> m)
	{
		for (Entry<V> entry : m.entrySet())
		{
			putForCreate(entry.getKey(), entry.getValue());
		}
	}

	/**
	 * Rehashes the contents of this map into a new array with a larger capacity. This method is
	 * called automatically when the number of keys in this map reaches its threshold.
	 * 
	 * If current capacity is MAXIMUM_CAPACITY, this method does not resize the map, but but sets
	 * threshold to Integer.MAX_VALUE. This has the effect of preventing future calls.
	 * 
	 * @param newCapacity
	 *            the new capacity, MUST be a power of two; must be greater than current capacity
	 *            unless current capacity is MAXIMUM_CAPACITY (in which case value is irrelevant).
	 */
	@SuppressWarnings("unchecked")
	void resize(final int newCapacity)
	{
		Entry<V>[] oldTable = table;
		int oldCapacity = oldTable.length;
		if (oldCapacity == MAXIMUM_CAPACITY)
		{
			threshold = Integer.MAX_VALUE;
			return;
		}

		Entry<V>[] newTable = new Entry[newCapacity];
		transfer(newTable);
		table = newTable;
		threshold = (int)(newCapacity * loadFactor);
	}

	/**
	 * Transfer all entries from current table to newTable.
	 * 
	 * @param newTable
	 */
	void transfer(final Entry<V>[] newTable)
	{
		Entry<V>[] src = table;
		int newCapacity = newTable.length;
		for (int j = 0; j < src.length; j++)
		{
			Entry<V> e = src[j];
			if (e != null)
			{
				src[j] = null;
				do
				{
					Entry<V> next = e.next;
					int i = indexFor(e.key, newCapacity);
					e.next = newTable[i];
					newTable[i] = e;
					e = next;
				}
				while (e != null);
			}
		}
	}

	/**
	 * Copies all of the mappings from the specified map to this map These mappings will replace any
	 * mappings that this map had for any of the keys currently in the specified map.
	 * 
	 * @param m
	 *            mappings to be stored in this map.
	 * @throws NullPointerException
	 *             if the specified map is null.
	 */
	public void putAll(final IntHashMap<V> m)
	{
		int numKeysToBeAdded = m.size();
		if (numKeysToBeAdded == 0)
		{
			return;
		}

		/*
		 * Expand the map if the map if the number of mappings to be added is greater than or equal
		 * to threshold. This is conservative; the obvious condition is (m.size() + size) >=
		 * threshold, but this condition could result in a map with twice the appropriate capacity,
		 * if the keys to be added overlap with the keys already in this map. By using the
		 * conservative calculation, we subject ourself to at most one extra resize.
		 */
		if (numKeysToBeAdded > threshold)
		{
			int targetCapacity = (int)(numKeysToBeAdded / loadFactor + 1);
			if (targetCapacity > MAXIMUM_CAPACITY)
			{
				targetCapacity = MAXIMUM_CAPACITY;
			}
			int newCapacity = table.length;
			while (newCapacity < targetCapacity)
			{
				newCapacity <<= 1;
			}
			if (newCapacity > table.length)
			{
				resize(newCapacity);
			}
		}

		for (Entry<V> entry : m.entrySet())
		{
			put(entry.getKey(), entry.getValue());
		}
	}

	/**
	 * Removes the mapping for this key from this map if present.
	 * 
	 * @param key
	 *            key whose mapping is to be removed from the map.
	 * @return previous value associated with specified key, or <tt>null</tt> if there was no
	 *         mapping for key. A <tt>null</tt> return can also indicate that the map previously
	 *         associated <tt>null</tt> with the specified key.
	 */
	public V remove(final int key)
	{
		Entry<V> e = removeEntryForKey(key);
		return (e == null ? null : e.value);
	}

	/**
	 * Removes and returns the entry associated with the specified key in the HashMap. Returns null
	 * if the HashMap contains no mapping for this key.
	 * 
	 * @param key
	 * @return The Entry object that was removed
	 */
	Entry<V> removeEntryForKey(final int key)
	{
		int i = indexFor(key, table.length);
		Entry<V> prev = table[i];
		Entry<V> e = prev;

		while (e != null)
		{
			Entry<V> next = e.next;
			if (key == e.key)
			{
				modCount.incrementAndGet();
				size--;
				if (prev == e)
				{
					table[i] = next;
				}
				else
				{
					prev.next = next;
				}
				return e;
			}
			prev = e;
			e = next;
		}

		return e;
	}

	/**
	 * Special version of remove for EntrySet.
	 * 
	 * @param o
	 * @return The entry that was removed
	 */
	@SuppressWarnings("unchecked")
	Entry<V> removeMapping(final Object o)
	{
		if (!(o instanceof Entry))
		{
			return null;
		}

		Entry<V> entry = (Entry<V>)o;
		int key = entry.getKey();
		int i = indexFor(key, table.length);
		Entry<V> prev = table[i];
		Entry<V> e = prev;

		while (e != null)
		{
			Entry<V> next = e.next;
			if ((e.key == key) && e.equals(entry))
			{
				modCount.incrementAndGet();
				size--;
				if (prev == e)
				{
					table[i] = next;
				}
				else
				{
					prev.next = next;
				}
				return e;
			}
			prev = e;
			e = next;
		}

		return e;
	}

	/**
	 * Removes all mappings from this map.
	 */
	public void clear()
	{
		modCount.incrementAndGet();
		Entry<V> tab[] = table;
		for (int i = 0; i < tab.length; i++)
		{
			tab[i] = null;
		}
		size = 0;
	}

	/**
	 * Returns <tt>true</tt> if this map maps one or more keys to the specified value.
	 * 
	 * @param value
	 *            value whose presence in this map is to be tested.
	 * @return <tt>true</tt> if this map maps one or more keys to the specified value.
	 */
	public boolean containsValue(final Object value)
	{
		if (value == null)
		{
			return containsNullValue();
		}

		for (Entry<V> entry : table)
		{
			for (Entry<V> e = entry; e != null; e = e.next)
			{
				if (value.equals(e.value))
				{
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Special-case code for containsValue with null argument
	 * 
	 * @return boolean true if there is a null value in this map
	 */
	private boolean containsNullValue()
	{
		Entry<V> tab[] = table;
		for (Entry<V> tabEntry : tab)
		{
			for (Entry<V> e = tabEntry; e != null; e = e.next)
			{
				if (e.value == null)
				{
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns a shallow copy of this <tt>HashMap</tt> instance: the keys and values themselves are
	 * not cloned.
	 * 
	 * @return a shallow copy of this map.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Object clone() throws CloneNotSupportedException
	{
		IntHashMap<V> result = null;
		try
		{
			result = (IntHashMap<V>)super.clone();
			result.table = new Entry[table.length];
			result.entrySet = null;
			result.modCount.set(0);
			result.size = 0;
			result.init();
			result.putAllForCreate(this);
		}
		catch (CloneNotSupportedException e)
		{
			// assert false;
		}
		return result;
	}

	/**
	 * @author jcompagner
	 * @param <V>
	 *            type of value object
	 */
	public static class Entry<V>
	{
		final int key;
		V value;
		Entry<V> next;

		/**
		 * Create new entry.
		 * 
		 * @param k
		 * @param v
		 * @param n
		 */
		Entry(final int k, final V v, final Entry<V> n)
		{
			value = v;
			next = n;
			key = k;
		}

		/**
		 * @return The int key of this entry
		 */
		public int getKey()
		{
			return key;
		}

		/**
		 * @return Gets the value object of this entry
		 */
		public V getValue()
		{
			return value;
		}

		/**
		 * @param newValue
		 * @return The previous value
		 */
		public V setValue(final V newValue)
		{
			V oldValue = value;
			value = newValue;
			return oldValue;
		}

		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@SuppressWarnings("unchecked")
		@Override
		public boolean equals(final Object o)
		{
			if (!(o instanceof Entry))
			{
				return false;
			}
			Entry<V> e = (Entry<V>)o;
			int k1 = getKey();
			int k2 = e.getKey();
			if (k1 == k2)
			{
				Object v1 = getValue();
				Object v2 = e.getValue();
				if ((v1 == v2) || ((v1 != null) && v1.equals(v2)))
				{
					return true;
				}
			}
			return false;
		}

		/**
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode()
		{
			return key ^ (value == null ? 0 : value.hashCode());
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString()
		{
			return getKey() + "=" + getValue(); //$NON-NLS-1$
		}
	}

	/**
	 * Add a new entry with the specified key, value and hash code to the specified bucket. It is
	 * the responsibility of this method to resize the table if appropriate.
	 * 
	 * Subclass overrides this to alter the behavior of put method.
	 * 
	 * @param key
	 * @param value
	 * @param bucketIndex
	 */
	void addEntry(final int key, final V value, final int bucketIndex)
	{
		table[bucketIndex] = new Entry<>(key, value, table[bucketIndex]);
		if (size++ >= threshold)
		{
			resize(2 * table.length);
		}
	}

	/**
	 * Like addEntry except that this version is used when creating entries as part of Map
	 * construction or "pseudo-construction" (cloning, deserialization). This version needn't worry
	 * about resizing the table.
	 * 
	 * Subclass overrides this to alter the behavior of HashMap(Map), clone, and readObject.
	 * 
	 * @param key
	 * @param value
	 * @param bucketIndex
	 */
	void createEntry(final int key, final V value, final int bucketIndex)
	{
		table[bucketIndex] = new Entry<>(key, value, table[bucketIndex]);
		size++;
	}

	private abstract class HashIterator<H> implements Iterator<H>
	{
		Entry<V> next; // next entry to return
		int expectedModCount; // For fast-fail
		int index; // current slot
		Entry<V> current; // current entry

		HashIterator()
		{
			expectedModCount = modCount.get();
			Entry<V>[] t = table;
			int i = t.length;
			Entry<V> n = null;
			if (size != 0)
			{ // advance to first entry
				while ((i > 0) && ((n = t[--i]) == null))
				{
					/* NoOp */
				}
			}
			next = n;
			index = i;
		}

		/**
		 * @see java.util.Iterator#hasNext()
		 */
		@Override
		public boolean hasNext()
		{
			return next != null;
		}

		Entry<V> nextEntry()
		{
			if (!modCount.compareAndSet(expectedModCount, expectedModCount))
			{
				throw new ConcurrentModificationException();
			}
			Entry<V> e = next;
			if (e == null)
			{
				throw new NoSuchElementException();
			}

			Entry<V> n = e.next;
			Entry<V>[] t = table;
			int i = index;
			while ((n == null) && (i > 0))
			{
				n = t[--i];
			}
			index = i;
			next = n;
			return current = e;
		}

		/**
		 * @see java.util.Iterator#remove()
		 */
		@Override
		public void remove()
		{
			if (current == null)
			{
				throw new IllegalStateException();
			}
			if (!modCount.compareAndSet(expectedModCount, expectedModCount))
			{
				throw new ConcurrentModificationException();
			}
			int k = current.key;
			current = null;
			removeEntryForKey(k);
			expectedModCount = modCount.get();
		}

	}

	private class ValueIterator extends HashIterator<V>
	{
		/**
		 * @see java.util.Iterator#next()
		 */
		@Override
		public V next()
		{
			return nextEntry().value;
		}
	}

	private class KeyIterator extends HashIterator<Integer>
	{
		/**
		 * @see java.util.Iterator#next()
		 */
		@Override
		public Integer next()
		{
			return nextEntry().getKey();
		}
	}

	private class EntryIterator extends HashIterator<Entry<V>>
	{
		/**
		 * @see java.util.Iterator#next()
		 */
		@Override
		public Entry<V> next()
		{
			return nextEntry();
		}
	}

	// Subclass overrides these to alter behavior of views' iterator() method
	Iterator<Integer> newKeyIterator()
	{
		return new KeyIterator();
	}

	Iterator<V> newValueIterator()
	{
		return new ValueIterator();
	}

	Iterator<Entry<V>> newEntryIterator()
	{
		return new EntryIterator();
	}

	// Views

	private transient Set<Entry<V>> entrySet = null;

	/**
	 * Returns a set view of the keys contained in this map. The set is backed by the map, so
	 * changes to the map are reflected in the set, and vice-versa. The set supports element
	 * removal, which removes the corresponding mapping from this map, via the
	 * <tt>Iterator.remove</tt>, <tt>Set.remove</tt>, <tt>removeAll</tt>, <tt>retainAll</tt>, and
	 * <tt>clear</tt> operations. It does not support the <tt>add</tt> or <tt>addAll</tt>
	 * operations.
	 * 
	 * @return a set view of the keys contained in this map.
	 */
	public Set<Integer> keySet()
	{
		Set<Integer> ks = keySet;
		return (ks != null ? ks : (keySet = new KeySet()));
	}

	private class KeySet extends AbstractSet<Integer>
	{
		/**
		 * @see java.util.AbstractCollection#iterator()
		 */
		@Override
		public Iterator<Integer> iterator()
		{
			return newKeyIterator();
		}

		/**
		 * @see java.util.AbstractCollection#size()
		 */
		@Override
		public int size()
		{
			return size;
		}

		/**
		 * @see java.util.AbstractCollection#contains(java.lang.Object)
		 */
		@Override
		public boolean contains(final Object o)
		{
			if (o instanceof Number)
			{
				return containsKey(((Number)o).intValue());
			}
			return false;
		}

		/**
		 * @see java.util.AbstractCollection#remove(java.lang.Object)
		 */
		@Override
		public boolean remove(final Object o)
		{
			if (o instanceof Number)
			{
				return removeEntryForKey(((Number)o).intValue()) != null;
			}
			return false;
		}

		/**
		 * @see java.util.AbstractCollection#clear()
		 */
		@Override
		public void clear()
		{
			IntHashMap.this.clear();
		}
	}

	/**
	 * Returns a collection view of the values contained in this map. The collection is backed by
	 * the map, so changes to the map are reflected in the collection, and vice-versa. The
	 * collection supports element removal, which removes the corresponding mapping from this map,
	 * via the <tt>Iterator.remove</tt>, <tt>Collection.remove</tt>, <tt>removeAll</tt>,
	 * <tt>retainAll</tt>, and <tt>clear</tt> operations. It does not support the <tt>add</tt> or
	 * <tt>addAll</tt> operations.
	 * 
	 * @return a collection view of the values contained in this map.
	 */
	public Collection<V> values()
	{
		Collection<V> vs = values;
		return (vs != null ? vs : (values = new Values()));
	}

	private class Values extends AbstractCollection<V>
	{
		/**
		 * @see java.util.AbstractCollection#iterator()
		 */
		@Override
		public Iterator<V> iterator()
		{
			return newValueIterator();
		}

		/**
		 * @see java.util.AbstractCollection#size()
		 */
		@Override
		public int size()
		{
			return size;
		}

		/**
		 * @see java.util.AbstractCollection#contains(java.lang.Object)
		 */
		@Override
		public boolean contains(final Object o)
		{
			return containsValue(o);
		}

		/**
		 * @see java.util.AbstractCollection#clear()
		 */
		@Override
		public void clear()
		{
			IntHashMap.this.clear();
		}
	}

	/**
	 * Returns a collection view of the mappings contained in this map. Each element in the returned
	 * collection is a <tt>Map.Entry</tt>. The collection is backed by the map, so changes to the
	 * map are reflected in the collection, and vice-versa. The collection supports element removal,
	 * which removes the corresponding mapping from the map, via the <tt>Iterator.remove</tt>,
	 * <tt>Collection.remove</tt>, <tt>removeAll</tt>, <tt>retainAll</tt>, and <tt>clear</tt>
	 * operations. It does not support the <tt>add</tt> or <tt>addAll</tt> operations.
	 * 
	 * @return a collection view of the mappings contained in this map.
	 * @see Map.Entry
	 */
	public Set<Entry<V>> entrySet()
	{
		Set<Entry<V>> es = entrySet;
		return (es != null ? es : (entrySet = new EntrySet()));
	}

	private class EntrySet extends AbstractSet<Entry<V>>
	{
		/**
		 * @see java.util.AbstractCollection#iterator()
		 */
		@Override
		public Iterator<Entry<V>> iterator()
		{
			return newEntryIterator();
		}

		/**
		 * @see java.util.AbstractCollection#contains(java.lang.Object)
		 */
		@SuppressWarnings("unchecked")
		@Override
		public boolean contains(final Object o)
		{
			if (!(o instanceof Entry))
			{
				return false;
			}
			Entry<V> e = (Entry<V>)o;
			Entry<V> candidate = getEntry(e.getKey());
			return (candidate != null) && candidate.equals(e);
		}

		/**
		 * @see java.util.AbstractCollection#remove(java.lang.Object)
		 */
		@Override
		public boolean remove(final Object o)
		{
			return removeMapping(o) != null;
		}

		/**
		 * @see java.util.AbstractCollection#size()
		 */
		@Override
		public int size()
		{
			return size;
		}

		/**
		 * @see java.util.AbstractCollection#clear()
		 */
		@Override
		public void clear()
		{
			IntHashMap.this.clear();
		}
	}

	/**
	 * Save the state of the <tt>HashMap</tt> instance to a stream (i.e., serialize it).
	 * 
	 * @param s
	 *            The ObjectOutputStream
	 * @throws IOException
	 * 
	 * @serialData The <i>capacity</i> of the HashMap (the length of the bucket array) is emitted
	 *             (int), followed by the <i>size</i> of the HashMap (the number of key-value
	 *             mappings), followed by the key (Object) and value (Object) for each key-value
	 *             mapping represented by the HashMap The key-value mappings are emitted in the
	 *             order that they are returned by <tt>entrySet().iterator()</tt>.
	 * 
	 */
	private void writeObject(final java.io.ObjectOutputStream s) throws IOException
	{
		// Write out the threshold, loadfactor, and any hidden stuff
		s.defaultWriteObject();

		// Write out number of buckets
		s.writeInt(table.length);

		// Write out size (number of Mappings)
		s.writeInt(size);

		// Write out keys and values (alternating)
		for (Entry<V> entry : entrySet())
		{
			s.writeInt(entry.getKey());
			s.writeObject(entry.getValue());
		}
	}

	private static final long serialVersionUID = 362498820763181265L;

	/**
	 * Reconstitute the <tt>HashMap</tt> instance from a stream (i.e., deserialize it).
	 * 
	 * @param s
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings("unchecked")
	private void readObject(final java.io.ObjectInputStream s) throws IOException,
		ClassNotFoundException
	{
		modCount = new AtomicInteger(0);

		// Read in the threshold, loadfactor, and any hidden stuff
		s.defaultReadObject();

		// Read in number of buckets and allocate the bucket array;
		int numBuckets = s.readInt();
		table = new Entry[numBuckets];

		init(); // Give subclass a chance to do its thing.

		// Read in size (number of Mappings)
		int size = s.readInt();

		// Read the keys and values, and put the mappings in the HashMap
		for (int i = 0; i < size; i++)
		{
			int key = s.readInt();
			V value = (V)s.readObject();
			putForCreate(key, value);
		}
	}

	// These methods are used when serializing HashSets
	int capacity()
	{
		return table.length;
	}

	float loadFactor()
	{
		return loadFactor;
	}
}
