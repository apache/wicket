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
package wicket.util.io;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EmptyStackException;

/**
 * TODO document me.
 * 
 * @author jcompagner
 */
final class HandleArrayListStack extends ArrayList
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 */
	public HandleArrayListStack()
	{
		this(10);
	}

	/**
	 * Construct.
	 * 
	 * @param collection
	 *            The collection to add
	 */
	public HandleArrayListStack(final Collection collection)
	{
		super(collection);
	}

	/**
	 * Construct.
	 * 
	 * @param initialCapacity
	 *            Initial capacity of the stack
	 */
	public HandleArrayListStack(final int initialCapacity)
	{
		super(initialCapacity);
	}

	/**
	 * Tests if this stack is empty.
	 * 
	 * @return <code>true</code> if and only if this stack contains no items;
	 *         <code>false</code> otherwise.
	 */
	public final boolean empty()
	{
		return size() == 0;
	}

	/**
	 * @see java.util.ArrayList#indexOf(java.lang.Object)
	 */
	public int indexOf(Object elem)
	{
		int size = size();
		if (elem == null)
		{
			for (int i = 0; i < size; i++)
			{
				if (get(i) == null)
					return i;
			}
		}
		else
		{
			for (int i = 0; i < size; i++)
				if (elem == get(i))
				{
					return i;
				}
		}
		return -1;
	}

	/**
	 * @see java.util.ArrayList#lastIndexOf(java.lang.Object)
	 */
	public int lastIndexOf(Object elem)
	{
		if (elem == null)
		{
			for (int i = size() - 1; i >= 0; i--)
			{
				if (get(i) == null)
					return i;
			}
		}
		else
		{
			for (int i = size() - 1; i >= 0; i--)
			{
				if (elem == get(i))
					return i;
			}
		}
		return -1;
	}

	/**
	 * Looks at the object at the top of this stack without removing it.
	 * 
	 * @return The object at the top of this stack
	 * @exception EmptyStackException
	 *                If this stack is empty.
	 */
	public final Object peek()
	{
		int size = size();
		if (size == 0)
		{
			throw new EmptyStackException();
		}
		return get(size - 1);
	}

	/**
	 * Removes the object at the top of this stack and returns that object.
	 * 
	 * @return The object at the top of this stack
	 * @exception EmptyStackException
	 *                If this stack is empty.
	 */
	public final Object pop()
	{
		final Object top = peek();
		remove(size() - 1);
		return top;
	}

	/**
	 * Pushes an item onto the top of this stack.
	 * 
	 * @param item
	 *            the item to be pushed onto this stack.
	 */
	public final void push(final Object item)
	{
		add(item);
	}

	/**
	 * Returns the 1-based position where an object is on this stack. If the
	 * object <tt>o</tt> occurs as an item in this stack, this method returns
	 * the distance from the top of the stack of the occurrence nearest the top
	 * of the stack; the topmost item on the stack is considered to be at
	 * distance <tt>1</tt>. The <tt>equals</tt> method is used to compare
	 * <tt>o</tt> to the items in this stack.
	 * 
	 * @param o
	 *            the desired object.
	 * @return the 1-based position from the top of the stack where the object
	 *         is located; the return value <code>-1</code> indicates that the
	 *         object is not on the stack.
	 */
	public final int search(final Object o)
	{
		int i = lastIndexOf(o);
		if (i >= 0)
		{
			return size() - i;
		}
		return -1;
	}
}