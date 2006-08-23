/*
 * $Id$ $Revision$
 * $Date$
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
package wicket.extensions.markup.html.repeater.data.table;

import wicket.extensions.markup.html.repeater.RepeatingView;
import wicket.extensions.markup.html.repeater.data.IDataProvider;
import wicket.extensions.markup.html.repeater.data.grid.DataGridView;
import wicket.extensions.markup.html.repeater.refreshing.IItemReuseStrategy;
import wicket.extensions.markup.html.repeater.refreshing.Item;
import wicket.extensions.markup.html.repeater.refreshing.RefreshingView;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.navigation.paging.IPageable;
import wicket.markup.html.panel.Panel;
import wicket.model.IModel;

/**
 * A data table builds on data grid view to introduce toolbars. Toolbars can be
 * used to display sortable column headers, paging information, filter controls,
 * and other information.
 * <p>
 * Data table also provides its own markup for an html table so the user does
 * not need to provide it himself. This makes it very simple to add a datatable
 * to the markup, however, some flexibility.
 * <p>
 * Example
 * 
 * <pre>
 *           &lt;table wicket:id=&quot;datatable&quot;&gt;&lt;/table&gt;
 * </pre>
 * 
 * And the related Java code: ( the first column will be sortable because its
 * sort property is specified, the second column will not )
 * 
 * <pre>
 * 
 * IColumn[] columns = new IColumn[2];
 * 
 * columns[0] = new PropertyColumn(new Model(&quot;First Name&quot;), &quot;name.first&quot;, &quot;name.first&quot;);
 * columns[1] = new PropertyColumn(new Model(&quot;Last Name&quot;), &quot;name.last&quot;);
 * 
 * DataTable table = new DataTable(&quot;datatable&quot;, columns, new UserProvider(), 10);
 * table.add(new NavigationToolbar(table));
 * table.add(new HeadersToolbar(table));
 * add(table);
 * 
 * </pre>
 * 
 * @see DefaultDataTable
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public class DataTable extends Panel implements IPageable
{
	/**
	 * The component id that toolbars must be created with in order to be added
	 * to the data table
	 */
	public static final String TOOLBAR_COMPONENT_ID = "toolbar";

	private static final long serialVersionUID = 1L;

	private final DataGridView datagrid;

	private IColumn[] columns;

	private final RepeatingView topToolbars;
	private final RepeatingView bottomToolbars;

	/**
	 * Constructor
	 * 
	 * @param id
	 *            component id
	 * @param columns
	 *            list of IColumn objects
	 * @param dataProvider
	 *            imodel for data provider
	 * @param rowsPerPage
	 *            number of rows per page
	 */
	public DataTable(String id, IColumn[] columns, IDataProvider dataProvider, int rowsPerPage)
	{
		super(id);

		this.columns = columns;

		datagrid = new DataGridView("rows", columns, dataProvider)
		{
			private static final long serialVersionUID = 1L;

			protected Item newRowItem(String id, int index, IModel model)
			{
				return DataTable.this.newRowItem(id, index, model);
			}

			protected Item newCellItem(String id, int index, IModel model)
			{
				return DataTable.this.newCellItem(id, index, model);
			}
		};
		datagrid.setRowsPerPage(rowsPerPage);
		add(datagrid);

		topToolbars = new RepeatingView("topToolbars")
		{
			private static final long serialVersionUID = 1L;

			public boolean isVisible()
			{
				return size() > 0;
			}

		};

		bottomToolbars = new RepeatingView("bottomToolbars")
		{

			private static final long serialVersionUID = 1L;

			public boolean isVisible()
			{
				return size() > 0;
			}
		};

		add(topToolbars);
		add(bottomToolbars);
	}

	/**
	 * @return array of column objects this table displays
	 */
	public final IColumn[] getColumns()
	{
		return columns;
	}

	/**
	 * Adds a toolbar to the datatable that will be displayed before the data
	 * 
	 * @param toolbar
	 *            toolbar to be added
	 * 
	 * @see AbstractToolbar
	 */
	public void addTopToolbar(AbstractToolbar toolbar)
	{
		addToolbar(toolbar, topToolbars);
	}

	/**
	 * Adds a toolbar to the datatable that will be displayed after the data
	 * 
	 * @param toolbar
	 *            toolbar to be added
	 * 
	 * @see AbstractToolbar
	 */
	public void addBottomToolbar(AbstractToolbar toolbar)
	{
		addToolbar(toolbar, bottomToolbars);
	}

	private void addToolbar(AbstractToolbar toolbar, RepeatingView container)
	{
		if (toolbar == null)
		{
			throw new IllegalArgumentException("argument [toolbar] cannot be null");
		}

		if (!toolbar.getId().equals(TOOLBAR_COMPONENT_ID))
		{
			throw new IllegalArgumentException(
					"Toolbar must have component id equal to AbstractDataTable.TOOLBAR_COMPONENT_ID");
		}

		toolbar.setRenderBodyOnly(true);
		
		// create a container item for the toolbar (required by repeating view)
		WebMarkupContainer item = new WebMarkupContainer(container.newChildId());
		item.setRenderBodyOnly(true);
		item.add(toolbar);

		container.add(item);
	}

	/**
	 * @see wicket.markup.html.navigation.paging.IPageable#getCurrentPage()
	 */
	public final int getCurrentPage()
	{
		return datagrid.getCurrentPage();
	}

	/**
	 * @see wicket.markup.html.navigation.paging.IPageable#setCurrentPage(int)
	 */
	public final void setCurrentPage(int page)
	{
		datagrid.setCurrentPage(page);
	}

	/**
	 * @see wicket.markup.html.navigation.paging.IPageable#getPageCount()
	 */
	public final int getPageCount()
	{
		return datagrid.getPageCount();
	}

	/**
	 * @return total number of rows in this table
	 */
	public final int getRowCount()
	{
		return datagrid.getRowCount();
	}

	/**
	 * Sets the number of items to be displayed per page
	 * 
	 * @param items
	 *            number of items to display per page
	 * 
	 */
	public void setRowsPerPage(int items)
	{
		datagrid.setRowsPerPage(items);
	}
	
	/**
	 * @return number of rows per page
	 */
	public final int getRowsPerPage()
	{
		return datagrid.getRowsPerPage();
	}

	/**
	 * Factory method for Item container that represents a row in the underlying
	 * DataGridView
	 * 
	 * @see Item
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
	 * Factory method for Item container that represents a cell in the
	 * underlying DataGridView
	 * 
	 * @see Item
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

	/**
	 * Sets the item reuse strategy. This strategy controls the creation of
	 * {@link Item}s.
	 * 
	 * @see RefreshingView#setItemReuseStrategy(IItemReuseStrategy)
	 * @see IItemReuseStrategy
	 * 
	 * @param strategy
	 *            item reuse strategy
	 * @return this for chaining
	 */
	public final DataTable setItemReuseStrategy(IItemReuseStrategy strategy)
	{
		datagrid.setItemReuseStrategy(strategy);
		return this;
	}

}
