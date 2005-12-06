/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.extensions.markup.html.repeater.data.grid;

import java.io.Serializable;
import java.util.Iterator;

import wicket.WicketRuntimeException;
import wicket.extensions.markup.html.repeater.data.AbstractDataView;
import wicket.extensions.markup.html.repeater.data.IDataProvider;
import wicket.extensions.markup.html.repeater.refreshing.Item;
import wicket.extensions.markup.html.repeater.refreshing.RefreshingView;
import wicket.model.IModel;
import wicket.model.Model;

/*
 * 
 * Example:
 * <pre>
 * <code>
 * add(new DataGridViewBase("rows", columns, dataprovider));
 * 
 * &lt;table cellspacing="0" cellpadding="2" border="1"&gt;
 *   &lt;tr wicket:id="rows"&gt;
 *     &lt;td wicket:id="cells"&gt;
 *       &lt;span wicket:id="cell"&gt;cell content goes here&lt;/span&gt;
 *     &lt;/td&gt;
 *   &lt;/tr&gt;
 * &lt;/table&gt;
 * </code>
 * </pre>
 */
public abstract class AbstractDataGridView extends AbstractDataView
{
	private static final long serialVersionUID = 1L;

	private static final String CELL_REPEATER_ID="cells";
	private static final String CELL_ITEM_ID = "cell";

	private ICellPopulator[] populators;
	
	/**
	 * Constructor
	 * 
	 * @param id
	 *            component id
	 * @param dataProvider
	 *            imodel for data provider
	 */
	public AbstractDataGridView(String id, ICellPopulator[] populators, IDataProvider dataProvider)
	{
		super(id, dataProvider);

		this.populators = populators;
	}

	protected final void populateItem(Item item)
	{
		final IModel rowModel = item.getModel();

		item.add(new RefreshingView(CELL_REPEATER_ID)
		{
			private static final long serialVersionUID = 1L;

			protected Iterator getItemModels()
			{
				return new ArrayIteratorAdapter(internalGetPopulators()) {

					protected IModel model(Object object)
					{
						return new Model((Serializable)object);
					}
					
				};
			}

			protected void populateItem(Item item)
			{
				final ICellPopulator populator = (ICellPopulator)item.getModelObject();
				populator.populateItem(item, CELL_ITEM_ID, rowModel);

				if (item.get("cell") == null)
				{
					throw new WicketRuntimeException(populator.getClass().getName()
							+ ".populateItem() failed to add a component with id ["
							+ CELL_ITEM_ID + "] to the provided [cellItem] argument");
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
	
	protected void internalPostProcessCellItem(Item item) {
		//noop
	}
	
	protected void internalPostProcessRowItem(Item item) {
		// noop
	}
}
