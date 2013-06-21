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
package org.apache.wicket.examples.tree;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.wicket.MetaDataKey;
import org.apache.wicket.Session;

/**
 * Example of a custom expansion state:
 * <ul>
 * <li>expanded {@link Foo}s are identified by their id</li>
 * <li>efficient expansion of all {@link Foo}</li>
 * <li>state is stored in the session</li>
 * </ul>
 * 
 * @author svenmeier
 */
public class FooExpansion implements Set<Foo>, Serializable
{
	private static final long serialVersionUID = 1L;

	private static MetaDataKey<FooExpansion> KEY = new MetaDataKey<FooExpansion>()
	{
		private static final long serialVersionUID = 1L;
	};

	private Set<String> ids = new HashSet<>();

	private boolean inverse;

	public void expandAll()
	{
		ids.clear();

		inverse = true;
	}

	public void collapseAll()
	{
		ids.clear();

		inverse = false;
	}

	@Override
	public boolean add(Foo foo)
	{
		if (inverse)
		{
			return ids.remove(foo.getId());
		}
		else
		{
			return ids.add(foo.getId());
		}
	}

	@Override
	public boolean remove(Object o)
	{
		Foo foo = (Foo)o;

		if (inverse)
		{
			return ids.add(foo.getId());
		}
		else
		{
			return ids.remove(foo.getId());
		}
	}

	@Override
	public boolean contains(Object o)
	{
		Foo foo = (Foo)o;

		if (inverse)
		{
			return !ids.contains(foo.getId());
		}
		else
		{
			return ids.contains(foo.getId());
		}
	}

	@Override
	public void clear()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public int size()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isEmpty()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public <A> A[] toArray(A[] a)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterator<Foo> iterator()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Object[] toArray()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsAll(Collection<?> c)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(Collection<? extends Foo> c)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> c)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> c)
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * Get the expansion for the session.
	 * 
	 * @return expansion
	 */
	public static FooExpansion get()
	{
		FooExpansion expansion = Session.get().getMetaData(KEY);
		if (expansion == null)
		{
			expansion = new FooExpansion();

			Session.get().setMetaData(KEY, expansion);
		}
		return expansion;
	}
}
