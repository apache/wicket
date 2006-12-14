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
package wicket.markup.repeater;

import java.util.Iterator;

import wicket.MarkupContainer;
import wicket.model.IModel;

/**
 * Implementation of <code>IItemReuseStrategy</code> that returns new items
 * every time.
 * 
 * @see wicket.markup.repeater.IItemReuseStrategy
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 * @param <T> 
 * 			Type of model object this component holds 
 */
public class DefaultItemReuseStrategy<T> implements IItemReuseStrategy<T>
{
	private static final long serialVersionUID = 1L;

	private static final IItemReuseStrategy instance = new DefaultItemReuseStrategy();

	/**
	 * @return static instance of this strategy
	 */
	public static <X> IItemReuseStrategy<X> getInstance()
	{
		return instance;
	}

	/**
	 * @see wicket.markup.repeater.IItemReuseStrategy#getItems(MarkupContainer, wicket.markup.repeater.IItemFactory,
	 *      java.util.Iterator, java.util.Iterator)
	 */
	public Iterator<Item<T>> getItems(final MarkupContainer<?> parent, final IItemFactory<T> factory, final Iterator<IModel<T>> newModels,
			final Iterator<Item<T>> existingItems)
	{
		return new Iterator<Item<T>>()
		{
			private int index = 0;

			public void remove()
			{
				throw new UnsupportedOperationException();
			}

			public boolean hasNext()
			{
				return newModels.hasNext();
			}

			public Item<T> next()
			{
				final IModel<T> model = newModels.next();

				Item<T> item = factory.newItem(parent, index, model);
				index++;

				return item;
			}

		};
	}

}
