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

import java.util.List;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RefreshingView;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.markup.repeater.data.DataViewBase;
import org.apache.wicket.markup.repeater.data.IDataProvider;
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
 * @param <T>
 *            Model object type
 */
public abstract class AbstractDataGridView<T> extends DataViewBase<T>
{
	private static final long serialVersionUID = 1L;

	private static final String CELL_REPEATER_ID = "cells";
	private static final String CELL_ITEM_ID = "cell";

	private final List<? extends ICellPopulator<T>> populators;

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
	public AbstractDataGridView(final String id,
		final List<? extends ICellPopulator<T>> populators, final IDataProvider<T> dataProvider)
	{
		super(id, dataProvider);

		this.populators = populators;
	}

	protected final List<? extends ICellPopulator<T>> internalGetPopulators()
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
	protected Item<ICellPopulator<T>> newCellItem(final String id, final int index,
		final IModel<ICellPopulator<T>> model)
	{
		return new Item<>(id, index, model);
	}

	@Override
	protected final Item<T> newItem(final String id, final int index, final IModel<T> model)
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
	protected Item<T> newRowItem(final String id, final int index, final IModel<T> model)
	{
		return new Item<>(id, index, model);
	}


	/**
	 * @see org.apache.wicket.markup.repeater.data.DataViewBase#onDetach()
	 */
	@Override
	protected void onDetach()
	{
		super.onDetach();
		if (populators != null)
		{
			for (ICellPopulator<T> populator : populators)
			{
				populator.detach();
			}
		}
	}

	/**
	 * @see org.apache.wicket.markup.repeater.RefreshingView#populateItem(org.apache.wicket.markup.repeater.Item)
	 */
	@Override
	protected final void populateItem(final Item<T> item)
	{
		RepeatingView cells = new RepeatingView(CELL_REPEATER_ID);
		item.add(cells);

		int populatorsNumber = populators.size();
		for (int i = 0; i < populatorsNumber; i++)
		{
			ICellPopulator<T> populator = populators.get(i);
			IModel<ICellPopulator<T>> populatorModel = new Model<>(populator);
			Item<ICellPopulator<T>> cellItem = newCellItem(cells.newChildId(), i, populatorModel);
			cells.add(cellItem);

			populator.populateItem(cellItem, CELL_ITEM_ID, item.getModel());

			if (cellItem.get("cell") == null)
			{
				throw new WicketRuntimeException(
					populator.getClass().getName() +
						".populateItem() failed to add a component with id [" +
						CELL_ITEM_ID +
						"] to the provided [cellItem] object. Make sure you call add() on cellItem and make sure you gave the added component passed in 'componentId' id. ( *cellItem*.add(new MyComponent(*componentId*, rowModel) )");
			}
		}

	}
}
