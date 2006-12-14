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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import wicket.MarkupContainer;
import wicket.model.IModel;

/**
 * Reuse strategy that will reuse an old item if its model is equal to a model
 * inside the newModels iterator. Useful when state needs to be kept across
 * requests for as long as the item is visible within the view.
 * <p>
 * Notice that the <u>model</u> and not the <u>model object</u> needs to
 * implement the equals method. Most of the time it is a good idea to forward
 * the equals call to the object, however if a detachable model is used it is
 * often enough to compare object ids models point to ( this saves the model
 * from loading the object).
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
	 * @see wicket.markup.repeater.IItemReuseStrategy#getItems(MarkupContainer, wicket.markup.repeater.IItemFactory,
	 *      java.util.Iterator, java.util.Iterator)
	 */
	public Iterator getItems(final MarkupContainer parent, final IItemFactory factory, final Iterator newModels,
			Iterator existingItems)
	{
		final Map<IModel< ? >, Item> modelToItem = new HashMap<IModel< ? >, Item>();
		while (existingItems.hasNext())
		{
			final Item item = (Item)existingItems.next();
			modelToItem.put(item.getModel(), item);
		}

		return new Iterator()
		{
			private int index = 0;

			public boolean hasNext()
			{
				return newModels.hasNext();
			}

			public Object next()
			{
				final IModel model = (IModel)newModels.next();
				final Item oldItem = (Item)modelToItem.get(model);

				final Item item;
				if (oldItem == null)
				{
					item = factory.newItem(parent, index, model);
				}
				else
				{
					oldItem.setIndex(index);
					item = oldItem;
				}
				index++;

				return item;
			}

			public void remove()
			{
				throw new UnsupportedOperationException();
			}

		};
	}

}
