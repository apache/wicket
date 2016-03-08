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
package org.apache.wicket.util;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

/**
 * Wrapper class for {@link java.util.Iterator} meant to recursively
 * iterate over its elements if they are iterable.
 * 
 * @author Andrea Del Bene
 */
public class NestedIterator<E> implements Iterator<E>
{
	private Iterator<E> currentIterator;
	private Deque<Iterator<E>> iteratorStack = new ArrayDeque<>();
	
	public NestedIterator(Iterator<E> currentIterator)
	{
		this.currentIterator = currentIterator;
	}
	
	public NestedIterator(Iterable<E> iterable)
	{
		this(iterable.iterator());
	}

	@Override
	public boolean hasNext()
	{
		if (!currentIterator.hasNext() && !iteratorStack.isEmpty())
		{
			currentIterator = iteratorStack.pop();
		}
		
		return currentIterator.hasNext();
	}

	@SuppressWarnings("unchecked")
	@Override
	public E next()
	{
		E nextElement = currentIterator.next();
		
		if (nextElement instanceof Iterable)
		{
			iteratorStack.push(currentIterator);
			currentIterator = ((Iterable<E>)nextElement).iterator();
		}
		
		return nextElement;
	}

}
