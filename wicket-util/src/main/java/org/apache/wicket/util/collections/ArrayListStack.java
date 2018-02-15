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
import java.util.Collection;
import java.util.EmptyStackException;

/**
 * A faster, smaller stack implementation. ArrayListStack is final and unsynchronized (the JDK's
 * methods are synchronized). In addition you can set the initial capacity if you want via the
 * ArrayListStack(int) constructor.
 * 
 * @author Jonathan Locke
 * @param <T>
 * @deprecated Scheduled to be removed with Wicket 9.x, use {@link java.util.ArrayDeque} instead.
 */
@Deprecated
public class ArrayListStack<T> extends ArrayList<T>
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @param initialCapacity
	 *            Initial capacity of the stack
	 */
	public ArrayListStack(final int initialCapacity)
	{
		super(initialCapacity);
	}

	/**
	 * Construct.
	 */
	public ArrayListStack()
	{
		this(10);
	}

	/**
	 * Construct.
	 * 
	 * @param collection
	 *            The collection to add
	 */
	public ArrayListStack(final Collection<T> collection)
	{
		super(collection);
	}

	/**
	 * Pushes an item onto the top of this stack.
	 * 
	 * @param item
	 *            the item to be pushed onto this stack.
	 */
	public final void push(final T item)
	{
		add(item);
	}

	/**
	 * Removes the object at the top of this stack and returns that object.
	 * 
	 * @return The object at the top of this stack
	 * @exception EmptyStackException
	 *                If this stack is empty.
	 */
	public final T pop()
	{
		final T top = peek();
		remove(size() - 1);
		return top;
	}

	/**
	 * Looks at the object at the top of this stack without removing it.
	 * 
	 * @return The object at the top of this stack
	 * @exception EmptyStackException
	 *                If this stack is empty.
	 */
	public final T peek()
	{
		int size = size();
		if (size == 0)
		{
			throw new EmptyStackException();
		}
		return get(size - 1);
	}

	/**
	 * Tests if this stack is empty.
	 * 
	 * @return <code>true</code> if and only if this stack contains no items; <code>false</code>
	 *         otherwise.
	 */
	public final boolean empty()
	{
		return size() == 0;
	}

	/**
	 * Returns the 1-based position where an object is on this stack. If the object <tt>o</tt>
	 * occurs as an item in this stack, this method returns the distance from the top of the stack
	 * of the occurrence nearest the top of the stack; the topmost item on the stack is considered
	 * to be at distance <tt>1</tt>. The <tt>equals</tt> method is used to compare <tt>o</tt> to the
	 * items in this stack.
	 * 
	 * @param o
	 *            the desired object.
	 * @return the 1-based position from the top of the stack where the object is located; the
	 *         return value <code>-1</code> indicates that the object is not on the stack.
	 */
	public final int search(final T o)
	{
		int i = lastIndexOf(o);
		if (i >= 0)
		{
			return size() - i;
		}
		return -1;
	}
}
