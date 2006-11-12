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
package wicket.extensions.markup.html.repeater.refreshing;

import java.util.Comparator;

import wicket.MarkupContainer;
import wicket.markup.html.WebMarkupContainer;
import wicket.model.IModel;
import wicket.version.undo.Change;

/**
 * Container that holds components in a RefreshingView. One Item represents one
 * entire row of the view. Users should add all containing components to the
 * Item instead of the view, this is accomplished by implementing
 * refreshingView.populateItem(Item item).
 * 
 * @param <T>
 *            The type
 * @see RefreshingView
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public class Item<T> extends WebMarkupContainer<T>
{
	private static final long serialVersionUID = 1L;

	/** relative index of this item */
	private int index;

	/**
	 * @param parent
	 *            The parent of this component The parent of this component.
	 * @param id
	 *            component id
	 * @param index
	 *            relative index of this item in the pageable view
	 * @param model
	 *            model for this item
	 */
	public Item(MarkupContainer parent, final String id, int index, final IModel<T> model)
	{
		super(parent, id, model);
		this.index = index;
	}

	/**
	 * Sets the index of this item
	 * 
	 * @param index
	 *            new index
	 */
	public void setIndex(int index)
	{
		if (this.index != index)
		{
			if (isVersioned())
			{
				addStateChange(new Change()
				{
					final int oldIndex = Item.this.index;
					private static final long serialVersionUID = 1L;

					@Override
					public void undo()
					{
						Item.this.index = oldIndex;
					}

					@Override
					public String toString()
					{
						return "IndexChange[component: " + getPath() + ", index: " + oldIndex + "]";
					}
				});
			}
			this.index = index;
		}
	}

	/**
	 * @return the index assigned to this item
	 */
	public int getIndex()
	{
		return index;
	}


	/**
	 * @return the primary key assigned to this item
	 */
	//FIXME General: ivaynberg: wth is this?
	public String getPrimaryKey()
	{
		return getId();
	}

	/**
	 * Comparator that compares Items by their index property
	 * 
	 * @author Igor Vaynberg (ivaynberg)
	 * 
	 */
	public static class IndexComparator implements Comparator<Item>
	{
		private static final Comparator instance = new IndexComparator();

		/**
		 * @return static instance of the comparator
		 */
		public static final Comparator getInstance()
		{
			return instance;
		}

		/**
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Item o1, Item o2)
		{
			return o1.getIndex() - o2.getIndex();
		}

	};

}
