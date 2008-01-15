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
package org.apache.wicket.extensions.markup.html.repeater.data.grid;

import java.io.Serializable;
import java.util.Iterator;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RefreshingView;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.markup.repeater.data.DataViewBase;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.markup.repeater.util.ArrayIteratorAdapter;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;


/**
 * Acts as a base for data-grid views. Unlike a data view a data-grid view populates both rows and
 * columns. The columns are populated by an array of provided ICellPopulator objects.
 * 
 * @see DataGridView
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public abstract class AbstractDataGridView extends DataViewBase
{
	private static final long serialVersionUID = 1L;

	private static final String CELL_REPEATER_ID = "cells";
	private static final String CELL_ITEM_ID = "cell";

	private ICellPopulator[] populators;

	private transient ArrayIteratorAdapter populatorsIteratorCache;

	/**
	 * Constructor
	 * 
	 * @param id
	 *            component id
	 * @param populators
	 *            array of ICellPopulator objects that will be used to populate cell items
	 * @param dataProvider
	 *            data provider
	 */
	public AbstractDataGridView(String id, ICellPopulator[] populators, IDataProvider dataProvider)
	{
		super(id, dataProvider);

		this.populators = populators;
	}

	/**
	 * Returns iterator over ICellPopulator elements in the populators array. This method caches the
	 * iterator implementation in a transient member instance.
	 * 
	 * @return iterator over ICellPopulator elements in the populators array
	 */
	private Iterator getPopulatorsIterator()
	{
		if (populatorsIteratorCache == null)
		{
			populatorsIteratorCache = new ArrayIteratorAdapter(internalGetPopulators())
			{

				protected IModel model(Object object)
				{
					return new Model((Serializable)object);
				}

			};
		}
		else
		{
			populatorsIteratorCache.reset();
		}
		return populatorsIteratorCache;
	}

	protected final ICellPopulator[] internalGetPopulators()
	{
		return populators;
	}


	/**
	 * Factory method for Item container that represents a cell.
	 * 
	 * @see Item
	 * @see RefreshingView#newItem(String, int, IModel)
	 * 
	 * @param id
	 *            component id for the new data item
	 * @param index
	 *            the index of the new data item
	 * @param model
	 *            the model for the new data item
	 * 
	 * @return DataItem created DataItem
	 */
	protected Item newCellItem(final String id, int index, final IModel model)
	{
		return new Item(id, index, model);
	}

	protected final Item newItem(String id, int index, IModel model)
	{
		return newRowItem(id, index, model);
	}

	/**
	 * Factory method for Item container that represents a row.
	 * 
	 * @see Item
	 * @see RefreshingView#newItem(String, int, IModel)
	 * 
	 * @param id
	 *            component id for the new data item
	 * @param index
	 *            the index of the new data item
	 * @param model
	 *            the model for the new data item.
	 * 
	 * @return DataItem created DataItem
	 */
	protected Item newRowItem(final String id, int index, final IModel model)
	{
		return new Item(id, index, model);
	}


	/**
	 * @see org.apache.wicket.markup.repeater.data.DataViewBase#onDetach()
	 */
	protected void onDetach()
	{
		super.onDetach();
		if (populators != null)
		{
			for (int i = 0; i < populators.length; i++)
			{
				populators[i].detach();
			}
		}
	}

	/**
	 * @see org.apache.wicket.markup.repeater.RefreshingView#populateItem(org.apache.wicket.markup.repeater.Item)
	 */
	protected final void populateItem(Item item)
	{
		RepeatingView cells = new RepeatingView(CELL_REPEATER_ID);
		item.add(cells);

		Iterator populators = getPopulatorsIterator();

		for (int i = 0; populators.hasNext(); i++)
		{
			IModel populatorModel = (IModel)populators.next();
			Item cellItem = newCellItem(cells.newChildId(), i, populatorModel);
			cells.add(cellItem);

			ICellPopulator populator = (ICellPopulator)cellItem.getModelObject();
			populator.populateItem(cellItem, CELL_ITEM_ID, item.getModel());

			if (cellItem.get("cell") == null)
			{
				throw new WicketRuntimeException(
					populator.getClass().getName() +
						".populateItem() failed to add a component with id [" +
						CELL_ITEM_ID +
						"] to the provided [cellItem] object. Make sure you call add() on cellItem ( cellItem.add(new MyComponent(componentId, rowModel) )");
			}
		}

	}
}
