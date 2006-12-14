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

import wicket.model.IModel;

/**
 * Implementation of <code>IItemReuseStrategy</code> that returns new items
 * every time.
 * 
 * @see wicket.extensions.markup.html.repeater.refreshing.IItemReuseStrategy
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
	 * @see wicket.extensions.markup.html.repeater.refreshing.IItemReuseStrategy#getItems(wicket.extensions.markup.html.repeater.refreshing.IItemFactory,
	 *      java.util.Iterator, java.util.Iterator)
	 */
	public Iterator getItems(final IItemFactory factory, final Iterator newModels,
			final Iterator existingItems)
	{
		return new Iterator()
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

			public Object next()
			{
				final IModel model = (IModel)newModels.next();

				Item item = factory.newItem(index, model);
				index++;

				return item;
			}

		};
	}

}
