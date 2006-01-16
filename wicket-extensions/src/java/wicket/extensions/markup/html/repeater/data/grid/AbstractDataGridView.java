/*
 * $Id$
 * $Revision$ $Date$
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.extensions.markup.html.repeater.data.grid;

import java.io.Serializable;
import java.util.Iterator;

import wicket.WicketRuntimeException;
import wicket.extensions.markup.html.repeater.data.DataViewBase;
import wicket.extensions.markup.html.repeater.data.IDataProvider;
import wicket.extensions.markup.html.repeater.refreshing.Item;
import wicket.extensions.markup.html.repeater.refreshing.RefreshingView;
import wicket.extensions.markup.html.repeater.util.ArrayIteratorAdapter;
import wicket.model.IModel;
import wicket.model.Model;

/**
 * Acts as a base for data-grid views. Unlike a data view a data-grid view
 * populates both rows and columns. The columns are populated by an array of
 * provided ICellPopulator objects.
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
	 *            array of ICellPopulator objects that will be used to populate
	 *            cell items
	 * @param dataProvider
	 *            data provider
	 */
	public AbstractDataGridView(String id, ICellPopulator[] populators, IDataProvider dataProvider)
	{
		super(id, dataProvider);

		this.populators = populators;
	}

	/**
	 * Returns iterator over ICellPopulator elements in the populators array.
	 * This method caches the iterator implemenation in a transient member
	 * instance.
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


	protected final void populateItem(Item item)
	{
		final IModel rowModel = item.getModel();

		// TODO General: Does this need to be a refreshing view? since the rows
		// is a refreshing view this will be recreated anyways. maybe can se
		// orderedrepeatingview instead to simplify.
		item.add(new RefreshingView(CELL_REPEATER_ID)
		{
			private static final long serialVersionUID = 1L;

			protected Iterator getItemModels()
			{
				return getPopulatorsIterator();
			}

			protected void populateItem(Item item)
			{
				final ICellPopulator populator = (ICellPopulator)item.getModelObject();
				populator.populateItem(item, CELL_ITEM_ID, rowModel);

				if (item.get("cell") == null)
				{
					throw new WicketRuntimeException(populator.getClass().getName()
							+ ".populateItem() failed to add a component with id [" + CELL_ITEM_ID
							+ "] to the provided [cellItem] argument");
				}

				internalPostProcessCellItem(item);

			}

		});

		internalPostProcessRowItem(item);
	}

	protected final ICellPopulator[] internalGetPopulators()
	{
		return populators;
	}

	protected void internalPostProcessCellItem(Item item)
	{
		// noop
	}

	protected void internalPostProcessRowItem(Item item)
	{
		// noop
	}
}
