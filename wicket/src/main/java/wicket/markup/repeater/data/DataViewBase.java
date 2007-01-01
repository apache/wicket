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
package wicket.markup.repeater.data;

import java.util.Iterator;

import wicket.MarkupContainer;
import wicket.markup.html.navigation.paging.IPageable;
import wicket.markup.repeater.PageableRefreshingView;
import wicket.markup.repeater.RefreshingView;
import wicket.model.IModel;

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
 * @param <T> 
 * 			Type of model object this component holds 
 */
public abstract class DataViewBase<T> extends PageableRefreshingView<T>
{
	private IDataProvider<T> dataProvider;

	/**
	 * @param parent
	 *            The parent of this component The parent of this component.
	 * @param id
	 *            component id
	 * @param dataProvider
	 *            data provider
	 */
	public DataViewBase(MarkupContainer<?> parent, final String id, IDataProvider<T> dataProvider)
	{
		super(parent, id);
		if (dataProvider == null)
		{
			throw new IllegalArgumentException("argument [dataProvider] cannot be null");
		}
		this.dataProvider = dataProvider;
	}

	/**
	 * @return data provider associated with this view
	 */
	protected final IDataProvider<T> internalGetDataProvider()
	{
		return dataProvider;
	}


	@Override
	protected final Iterator<IModel<T>> getItemModels(int offset, int count)
	{
		return new ModelIterator<T>(internalGetDataProvider(), offset, count);
	}

	/**
	 * Helper class that converts input from IDataProvider to an iterator over
	 * view items.
	 * 
	 * @author Igor Vaynberg (ivaynberg)
	 * 
	 * @param <T> 
	 * 
	 */
	private static final class ModelIterator<T> implements Iterator<IModel<T>>
	{
		private Iterator<T> items;
		private IDataProvider<T> dataProvider;
		private int max;
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
		public ModelIterator(IDataProvider<T> dataProvider, int offset, int count)
		{
			this.items = dataProvider.iterator(offset, count);
			this.dataProvider = dataProvider;
			this.max = count;
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
		public IModel<T> next()
		{
			index++;
			return dataProvider.model(items.next());
		}
	}

	@Override
	protected final int internalGetItemCount()
	{
		return internalGetDataProvider().size();
	}

	@Override
	protected void onDetach()
	{
		dataProvider.detach();
		super.onDetach();
	}
}
