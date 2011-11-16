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
package org.apache.wicket.markup.repeater.util;

import java.util.Iterator;

import org.apache.wicket.model.IModel;


/**
 * Iterator over an array. Implementation must provide {@link ArrayIteratorAdapter#model(Object) }
 * method to wrap each item in a model before it is returned through
 * {@link ArrayIteratorAdapter#next() } method.
 * 
 * @author Igor Vaynberg (ivaynberg)
 * @param <T>
 *            type of array element
 * 
 */
public abstract class ArrayIteratorAdapter<T> implements Iterator<IModel<T>>
{
	private final T[] array;
	private int pos = 0;

	/**
	 * Constructor
	 * 
	 * @param array
	 */
	public ArrayIteratorAdapter(T[] array)
	{
		this.array = array;
	}

	/**
	 * @see java.util.Iterator#remove()
	 */
	@Override
	public void remove()
	{
		throw new UnsupportedOperationException("remove() is not allowed");
	}

	/**
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext()
	{
		return pos < array.length;
	}

	/**
	 * @see java.util.Iterator#next()
	 */
	@Override
	public IModel<T> next()
	{
		return model(array[pos++]);
	}

	/**
	 * Resets the iterator position back to the beginning of the array
	 */
	public void reset()
	{
		pos = 0;
	}

	/**
	 * This method is used to wrap the provided object with an implementation of IModel. The
	 * provided object is guaranteed to be returned from the delegate iterator.
	 * 
	 * @param object
	 *            object to be wrapped
	 * @return IModel wrapper for the object
	 */
	abstract protected IModel<T> model(T object);


}
