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

import java.io.Serializable;
import java.util.Comparator;

import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.model.IModel;


/**
 * Container that holds components in a RefreshingView. One Item represents one entire row of the
 * view. Users should add all containing components to the Item instead of the view, this is
 * accomplished by implementing refreshingView.populateItem(Item item).
 * 
 * @see RefreshingView
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 * @param <T>
 *            Model object type
 */
public class Item<T> extends ListItem<T>
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * 
	 * @param id
	 *            component id
	 * @param index
	 *            relative index of this item in the pageable view
	 * @param model
	 *            model for this item
	 */
	public Item(final String id, int index, final IModel<T> model)
	{
		super(id, index, model);
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 *            component id
	 * @param index
	 *            relative index of this item in the pageable view
	 */
	public Item(final String id, int index)
	{
		super(id, index);
	}

	/**
	 * @return the primary key assigned to this item
	 */
	public String getPrimaryKey()
	{
		return getId();
	}

	/**
	 * Comparator that compares Items by their index property
	 * 
	 * @author Igor Vaynberg (ivaynberg)
	 */
	public static class IndexComparator implements Comparator<Item<?>>, Serializable
	{
		private static final long serialVersionUID = 1L;
		private static final Comparator<Item<?>> instance = new IndexComparator();

		/**
		 * @return static instance of the comparator
		 */
		public static Comparator<Item<?>> getInstance()
		{
			return instance;
		}

		/**
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(Item<?> lhs, Item<?> rhs)
		{
			long diff = lhs.getIndex() - rhs.getIndex();
			return diff == 0 ? 0 : diff > 0 ? 1 : -1;
		}
	}
}
