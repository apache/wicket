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
package org.apache.wicket.ajaxng;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Simple list like class that supports method chaining during item manipulation.
 * 
 * @author Matej Knopp
 * @param <T> item type
 */
public class ChainingList<T> implements Iterable<T>
{
	private final List<T> list;

	/**
	 * Creates new {@link ChainingList} instance from the list.
	 * 
	 * @param list
	 */
	public ChainingList(List<T> list)
	{
		this.list = list;
	}
	
	/**
	 * Creates new empty {@link ChainingList} instance.
	 */
	public ChainingList()
	{
		this.list = new ArrayList<T>();	
	}
	
	/**
	 * Returns the underlying list instance.
	 * @return list
	 */
	public List<T> getList()
	{
		return list;
	}
	
	/**
	 * @see List#add(Object)
	 * @param o
	 * @return <code>this</code>
	 */
	public ChainingList<T> add(T o)
	{
		list.add(o);
		return this;
	}

	/**
	 * @see List#add(int, Object)
	 * 
	 * @param index
	 * @param element
	 * @return <code>this</code>
	 */
	public ChainingList<T> add(int index, T element)
	{
		list.add(index, element);
		return this;
	}

	/**
	 * @see List#addAll(Collection)
	 * @param c
	 * @return <code>this</code>
	 */
	public ChainingList<T> addAll(Collection<? extends T> c)
	{
		list.addAll(c);
		return this;
	}

	/**
	 * @see List#addAll(int, Collection)
	 * @param index
	 * @param c
	 * @return <code>this</code>
	 */
	public ChainingList<T> addAll(int index, Collection<? extends T> c)
	{
		list.addAll(index, c);
		return this;
	}

	/**
	 * @see List#clear()
	 * @return <code>this</code> 
	 */
	public ChainingList<T> clear()
	{
		list.clear();
		return this;
	}

	/**
	 * @see List#contains(Object)
	 * @param o
	 * @return boolean
	 */
	public boolean contains(Object o)
	{
		return list.contains(o);
	}

	/**
	 * @see List#contains(Object)
	 * @param c
	 * @return boolean
	 */
	public boolean containsAll(Collection<?> c)
	{
		return list.containsAll(c);
	}

	/**
	 * @see List#get(int)
	 * @param index
	 * @return item or <code>null</code>
	 */
	public T get(int index)
	{
		return list.get(index);
	}

	/**
	 * @see List#indexOf(Object)
	 * @param o
	 * @return index
	 */
	public int indexOf(Object o)
	{
		return list.indexOf(o);
	}

	/**
	 * @see List#isEmpty()
	 * @return boolean
	 */
	public boolean isEmpty()
	{
		return list.isEmpty();
	}

	/** 
	 * @see java.lang.Iterable#iterator()
	 */
	public Iterator<T> iterator()
	{
		return list.iterator();
	}

	/**
	 * @see List#lastIndexOf(Object)
	 * @param o
	 * @return last index
	 */
	public int lastIndexOf(Object o)
	{
		return list.lastIndexOf(o);
	}

	/**
	 * @see List#listIterator()
	 * @return list iterator
	 */
	public ListIterator<T> listIterator()
	{
		return list.listIterator();
	}

	/**
	 * @see List#listIterator(int)
	 * @param index
	 * @return list iterator
	 */
	public ListIterator<T> listIterator(int index)
	{
		return list.listIterator(index);
	}

	/**
	 * @see List#remove(Object)
	 * @param o
	 * @return <code>this</code>
	 */
	public ChainingList<T> remove(Object o)
	{
		list.remove(o);
		return this;
	}

	/**
	 * @see List#remove(int)
	 * @param index
	 * @return <code>this</code>
	 */
	public ChainingList<T> remove(int index)
	{
		list.remove(index);
		return this;
	}

	/**
	 * @see List#removeAll(Collection)
	 * @param c
	 * @return <code>this</code>
	 */
	public ChainingList<T> removeAll(Collection<?> c)
	{
		list.removeAll(c);
		return this;
	}

	/**
	 * @see List#retainAll(Collection)
	 * @param c
	 * @return <code>this</code>
	 */
	public ChainingList<T> retainAll(Collection<?> c)
	{
		list.retainAll(c);
		return this;
	}

	/**
	 * @see List#set(int, Object)
	 * @param index
	 * @param element
	 * @return <code>this</code>
	 */
	public ChainingList<T> set(int index, T element)
	{
		list.set(index, element);
		return this;
	}

	/**
	 * @see List#size()
	 * @return size
	 */
	public int size()
	{
		return list.size();
	}

	/**
	 * @see List#subList(int, int) 
	 * @param fromIndex
	 * @param toIndex
	 * @return chaining list
	 */
	public ChainingList<T> subList(int fromIndex, int toIndex)
	{
		return new ChainingList<T>(list.subList(fromIndex, toIndex));
	}

	/**
	 * @see List#toArray()
	 * @return array
	 */
	public Object[] toArray()
	{
		return list.toArray();
	}

	/**
	 * @see List#toArray(Object[])
	 * @param a
	 * @return array
	 */
	public T[] toArray(T[] a)
	{
		return list.toArray(a);
	}
}
