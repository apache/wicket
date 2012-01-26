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
package org.apache.wicket.extensions.markup.html.repeater.tree.util;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

/**
 * A set holding objects by their keys.
 * 
 * Note: Apart from {@link #contains(Object)} no query methods are supported.
 * 
 * @see #key(Object)
 * 
 * @author svenmeier
 */
public abstract class KeyingSet<T> implements Set<T>, Serializable
{

	private Set<Object> keys = new HashSet<Object>();

	/**
	 * Get the key for the given object.
	 * 
	 * @param t
	 *            object to get key for
	 */
	protected abstract Object key(T t);

	public int size()
	{
		return keys.size();
	}

	public boolean isEmpty()
	{
		return keys.isEmpty();
	}

	@SuppressWarnings("unchecked")
	public boolean contains(Object o)
	{
		Object key = key((T)o);
		if (key == null)
		{
			return false;
		}
		else
		{
			return keys.contains(key);
		}
	}

	public boolean add(T t)
	{
		return keys.add(key(t));
	}

	@SuppressWarnings("unchecked")
	public boolean remove(Object t)
	{
		return keys.remove(key((T)t));
	}

	public boolean containsAll(Collection<?> cs)
	{
		for (Object c : cs)
		{
			if (!contains(c))
			{
				return false;
			}
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	public boolean addAll(Collection<? extends T> cs)
	{
		boolean changed = false;

		for (Object c : cs)
		{
			changed |= add((T)c);
		}

		return changed;
	}

	@SuppressWarnings("unchecked")
	public boolean removeAll(Collection<?> cs)
	{
		boolean changed = false;

		for (Object c : cs)
		{
			changed |= remove(c);
		}

		return changed;
	}

	public void clear()
	{
		keys.clear();
	}

	public Iterator<T> iterator()
	{
		throw new UnsupportedOperationException();
	}

	public Object[] toArray()
	{
		throw new UnsupportedOperationException();
	}

	public <S> S[] toArray(S[] ts)
	{
		throw new UnsupportedOperationException();
	}

	public boolean retainAll(Collection<?> c)
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * Create a model holding this set.
	 * 
	 * @return model
	 */
	public IModel<Set<T>> createModel()
	{
		return new AbstractReadOnlyModel<Set<T>>()
		{
			@Override
			public Set<T> getObject()
			{
				return KeyingSet.this;
			}
		};
	}
}