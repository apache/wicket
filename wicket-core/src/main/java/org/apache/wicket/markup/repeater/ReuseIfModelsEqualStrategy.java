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
import java.util.Map;

import org.apache.wicket.model.IModel;
import org.apache.wicket.util.lang.Generics;


/**
 * Reuse strategy that will reuse an old item if its model is equal to a model inside the newModels
 * iterator. Useful when state needs to be kept across requests for as long as the item is visible
 * within the view.
 * <p>
 * Notice that the <u>model</u> and not the <u>model object</u> needs to implement the
 * {@link #equals(Object)} and {@link #hashCode()} methods. Most of the time it is a good idea to
 * forward the calls to the object, however if a detachable model is used it is often enough to
 * compare an identifier for the object the models are pointing to ( this saves the model from loading the
 * object).
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public class ReuseIfModelsEqualStrategy implements IItemReuseStrategy
{
	private static final long serialVersionUID = 1L;

	private static IItemReuseStrategy instance = new ReuseIfModelsEqualStrategy();

	/**
	 * @return static instance
	 */
	public static IItemReuseStrategy getInstance()
	{
		return instance;
	}

	/**
	 * @see org.apache.wicket.markup.repeater.IItemReuseStrategy#getItems(org.apache.wicket.markup.repeater.IItemFactory,
	 *      java.util.Iterator, java.util.Iterator)
	 */
	@Override
	public <T> Iterator<Item<T>> getItems(final IItemFactory<T> factory,
		final Iterator<IModel<T>> newModels, Iterator<Item<T>> existingItems)
	{
		final Map<IModel<T>, Item<T>> modelToItem = Generics.newHashMap();
		while (existingItems.hasNext())
		{
			final Item<T> item = existingItems.next();
			modelToItem.put(item.getModel(), item);
		}

		return new Iterator<Item<T>>()
		{
			private int index = 0;

			@Override
			public boolean hasNext()
			{
				return newModels.hasNext();
			}

			@Override
			public Item<T> next()
			{
				final IModel<T> model = newModels.next();
				final Item<T> oldItem = modelToItem.get(model);

				final Item<T> item;
				if (oldItem == null)
				{
					item = factory.newItem(index, model);
				}
				else
				{
					oldItem.setIndex(index);
					item = oldItem;
				}
				index++;

				return item;
			}

			@Override
			public void remove()
			{
				throw new UnsupportedOperationException();
			}

		};
	}

}
