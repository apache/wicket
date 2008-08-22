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

public class FunctionList 
{
	private final List<String> list = new ArrayList<String>();
	
	public FunctionList add(String o)
	{
		list.add(o);
		return this;
	}

	public FunctionList add(int index, String element)
	{
		list.add(index, element);
		return this;
	}

	public FunctionList addAll(Collection<? extends String> c)
	{
		list.addAll(c);
		return this;
	}

	public FunctionList addAll(int index, Collection<? extends String> c)
	{
		list.addAll(index, c);
		return this;
	}

	public void clear()
	{
		list.clear();
	}

	public boolean contains(Object o)
	{
		return list.contains(o);
	}

	public boolean containsAll(Collection<?> c)
	{
		return list.containsAll(c);
	}

	public String get(int index)
	{
		return list.get(index);
	}

	public int indexOf(Object o)
	{
		return list.indexOf(o);
	}

	public boolean isEmpty()
	{
		return list.isEmpty();
	}

	public Iterator<String> iterator()
	{
		return list.iterator();
	}

	public int lastIndexOf(Object o)
	{
		return list.lastIndexOf(o);
	}

	public ListIterator<String> listIterator()
	{
		return list.listIterator();
	}

	public ListIterator<String> listIterator(int index)
	{
		return list.listIterator(index);
	}

	public boolean remove(Object o)
	{
		return list.remove(o);
	}

	public String remove(int index)
	{
		return list.remove(index);
	}

	public boolean removeAll(Collection<?> c)
	{
		return list.removeAll(c);
	}

	public boolean retainAll(Collection<?> c)
	{
		return list.retainAll(c);
	}

	public String set(int index, String element)
	{
		return list.set(index, element);
	}

	public int size()
	{
		return list.size();
	}

	public List<String> subList(int fromIndex, int toIndex)
	{
		return list.subList(fromIndex, toIndex);
	}

	public Object[] toArray()
	{
		return list.toArray();
	}

	public <T> T[] toArray(T[] a)
	{
		return list.toArray(a);
	}
}
