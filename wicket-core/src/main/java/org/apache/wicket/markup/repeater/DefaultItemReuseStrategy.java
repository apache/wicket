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
package org.apache.wicket.markup.repeater;

import java.util.Iterator;

import org.apache.wicket.model.IModel;


/**
 * Implementation of <code>IItemReuseStrategy</code> that returns new items every time.
 * 
 * @see org.apache.wicket.markup.repeater.IItemReuseStrategy
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public class DefaultItemReuseStrategy implements IItemReuseStrategy
{
	private static final long serialVersionUID = 1L;

	private static final IItemReuseStrategy instance = new DefaultItemReuseStrategy();

	/**
	 * @return static instance of this strategy
	 */
	public static IItemReuseStrategy getInstance()
	{
		return instance;
	}

	/**
	 * 
	 * @see org.apache.wicket.markup.repeater.IItemReuseStrategy#getItems(org.apache.wicket.markup.repeater.IItemFactory,
	 *      java.util.Iterator, java.util.Iterator)
	 */
	@Override
	public <T> Iterator<Item<T>> getItems(final IItemFactory<T> factory,
		final Iterator<IModel<T>> newModels, Iterator<Item<T>> existingItems)
	{
		return new Iterator<Item<T>>()
		{
			private int index = 0;

			@Override
			public void remove()
			{
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean hasNext()
			{
				return newModels.hasNext();
			}

			@Override
			public Item<T> next()
			{
				IModel<T> model = newModels.next();
				Item<T> item = factory.newItem(index, model);
				index++;
				return item;
			}

		};
	}

}
