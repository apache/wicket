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

import org.apache.wicket.markup.repeater.RefreshingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.lang.Args;


/**
 * Iterator adapter that wraps adaptee's elements with a model. Convenient when implementing
 * {@link RefreshingView}.
 * 
 * @author Igor Vaynberg (ivaynberg)
 * @param <T>
 * 
 */
public abstract class ModelIteratorAdapter<T> implements Iterator<IModel<T>>
{
	private final Iterator<T> delegate;

	/**
	 * Constructor
	 * 
	 * @param iterable
	 *            iterable whose iterator will be wrapped
	 */
	public ModelIteratorAdapter(Iterable<T> iterable)
	{
		Args.notNull(iterable, "iterable");
		this.delegate = iterable.iterator();
	}


	/**
	 * Constructor
	 * 
	 * @param delegate
	 *            iterator that will be wrapped
	 */
	public ModelIteratorAdapter(Iterator<T> delegate)
	{
		this.delegate = delegate;
	}

	/**
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext()
	{
		return delegate.hasNext();
	}

	/**
	 * @see java.util.Iterator#next()
	 */
	@Override
	public IModel<T> next()
	{
		return model(delegate.next());
	}

	/**
	 * @see java.util.Iterator#remove()
	 */
	@Override
	public void remove()
	{
		delegate.remove();
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
