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
package org.apache.wicket.markup.repeater.data;

import java.util.Iterator;

import org.apache.wicket.markup.html.navigation.paging.IPageable;
import org.apache.wicket.markup.repeater.AbstractPageableView;
import org.apache.wicket.markup.repeater.RefreshingView;


/**
 * Base class for data views.
 * 
 * Data views aim to make it very simple to populate your repeating view from a
 * database by utilizing {@link IDataProvider} to act as an interface between
 * the database and the dataview.
 * 
 * @see IDataProvider
 * @see DataView
 * @see IPageable
 * @see RefreshingView
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public abstract class DataViewBase extends AbstractPageableView
{
	private final IDataProvider dataProvider;

	/**
	 * @param id
	 *            component id
	 * @param dataProvider
	 *            data provider
	 */
	public DataViewBase(String id, IDataProvider dataProvider)
	{
		super(id);
		if (dataProvider == null)
		{
			throw new IllegalArgumentException("argument [dataProvider] cannot be null");
		}
		this.dataProvider = dataProvider;
	}

	/**
	 * @return data provider associated with this view
	 */
	protected final IDataProvider internalGetDataProvider()
	{
		return dataProvider;
	}


	protected final Iterator getItemModels(int offset, int count)
	{
		return new ModelIterator(internalGetDataProvider(), offset, count);
	}

	/**
	 * Helper class that converts input from IDataProvider to an iterator over
	 * view items.
	 * 
	 * @author Igor Vaynberg (ivaynberg)
	 * 
	 */
	private static final class ModelIterator implements Iterator
	{
		private final Iterator items;
		private final IDataProvider dataProvider;
		private final int max;
		private int index;

		/**
		 * Constructor
		 * 
		 * @param dataProvider
		 *            data provider
		 * @param offset
		 *            index of first item
		 * @param count
		 *            max number of items to return
		 */
		public ModelIterator(IDataProvider dataProvider, int offset, int count)
		{
			items = dataProvider.iterator(offset, count);
			this.dataProvider = dataProvider;
			max = count;
		}

		/**
		 * @see java.util.Iterator#remove()
		 */
		public void remove()
		{
			throw new UnsupportedOperationException();
		}

		/**
		 * @see java.util.Iterator#hasNext()
		 */
		public boolean hasNext()
		{
			return items.hasNext() && (index < max);
		}

		/**
		 * @see java.util.Iterator#next()
		 */
		public Object next()
		{
			index++;
			return dataProvider.model(items.next());
		}
	}

	protected final int internalGetItemCount()
	{
		return internalGetDataProvider().size();
	}

	/**
	 * @see org.apache.wicket.markup.repeater.AbstractPageableView#onDetach()
	 */
	protected void onDetach()
	{
		dataProvider.detach();
		super.onDetach();
	}
}
