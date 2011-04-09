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
package org.apache.wicket.extensions.markup.html.repeater.data.table;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.DataGridView;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.navigation.paging.IPageableItems;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.IItemReuseStrategy;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RefreshingView;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.string.Strings;


/**
 * A data table builds on data grid view to introduce toolbars. Toolbars can be used to display
 * sortable column headers, paging information, filter controls, and other information.
 * <p>
 * Data table also provides its own markup for an html table so the user does not need to provide it
 * himself. This makes it very simple to add a datatable to the markup, however, some flexibility.
 * <p>
 * Example
 * 
 * <pre>
 * &lt;table wicket:id=&quot;datatable&quot;&gt;&lt;/table&gt;
 * </pre>
 * 
 * And the related Java code: ( the first column will be sortable because its sort property is
 * specified, the second column will not )
 * 
 * <pre>
 * List&lt;IColumn&lt;T&gt;&gt; columns = new ArrayList&lt;IColumn&lt;T&gt;&gt;();
 * 
 * columns.add(new PropertyColumn(new Model&lt;String&gt;(&quot;First Name&quot;), &quot;name.first&quot;, &quot;name.first&quot;));
 * columns.add(new PropertyColumn(new Model&lt;String&gt;(&quot;Last Name&quot;), &quot;name.last&quot;));
 * 
 * DataTable table = new DataTable(&quot;datatable&quot;, columns, new UserProvider(), 10);
 * table.addBottomToolbar(new NavigationToolbar(table));
 * table.addTopToolbar(new HeadersToolbar(table, null));
 * add(table);
 * </pre>
 * 
 * @see DefaultDataTable
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 * @param <T>
 *            The model object type
 * 
 */
public class DataTable<T> extends Panel implements IPageableItems
{
	static abstract class CssAttributeBehavior extends Behavior
	{
		private static final long serialVersionUID = 1L;

		protected abstract String getCssClass();

		/**
		 * @see Behavior#onComponentTag(Component, ComponentTag)
		 */
		@Override
		public void onComponentTag(final Component component, final ComponentTag tag)
		{
			String className = getCssClass();
			if (!Strings.isEmpty(className))
			{
				CharSequence oldClassName = tag.getAttribute("class");
				if (Strings.isEmpty(oldClassName))
				{
					tag.put("class", className);
				}
				else
				{
					tag.put("class", oldClassName + " " + className);
				}
			}
		}
	}

	private static final long serialVersionUID = 1L;

	private final DataGridView<T> datagrid;

	private final WebMarkupContainer body;

	private final List<IColumn<T>> columns;

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
	public DataTable(final String id, final List<IColumn<T>> columns,
		final IDataProvider<T> dataProvider, final int rowsPerPage)
	{
		super(id);

		if ((columns == null) || (columns.size() < 1))
		{
			throw new IllegalArgumentException("Argument `columns` cannot be null or empty");
		}

		this.columns = columns;
		body = newBodyContainer("body");
		datagrid = new DataGridView<T>("rows", columns, dataProvider)
		{
			private static final long serialVersionUID = 1L;

			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			protected Item newCellItem(final String id, final int index, final IModel model)
			{
				Item item = DataTable.this.newCellItem(id, index, model);
				final IColumn<T> column = DataTable.this.columns.get(index);
				if (column instanceof IStyledColumn)
				{
					item.add(new DataTable.CssAttributeBehavior()
					{
						private static final long serialVersionUID = 1L;

						@Override
						protected String getCssClass()
						{
							return ((IStyledColumn<?>)column).getCssClass();
						}
					});
				}
				return item;
			}

			@Override
			protected Item<T> newRowItem(final String id, final int index, final IModel<T> model)
			{
				return DataTable.this.newRowItem(id, index, model);
			}
		};
		datagrid.setItemsPerPage(rowsPerPage);
		body.add(datagrid);
		add(body);
		topToolbars = new ToolbarsContainer("topToolbars");
		bottomToolbars = new ToolbarsContainer("bottomToolbars");
		add(topToolbars);
		add(bottomToolbars);
	}

	/**
	 * Create the MarkupContainer for the <tbody> tag. Users may subclass it to provide their own
	 * (modified) implementation.
	 * 
	 * @param id
	 * @return A new markup container
	 */
	protected WebMarkupContainer newBodyContainer(final String id)
	{
		return new WebMarkupContainer(id);
	}

	/**
	 * Set the 'class' attribute for the tbody tag.
	 * 
	 * @param cssStyle
	 */
	public final void setTableBodyCss(final String cssStyle)
	{
		body.add(new SimpleAttributeModifier("class", cssStyle));
	}

	/**
	 * Adds a toolbar to the datatable that will be displayed after the data
	 * 
	 * @param toolbar
	 *            toolbar to be added
	 * 
	 * @see AbstractToolbar
	 */
	public void addBottomToolbar(final AbstractToolbar toolbar)
	{
		addToolbar(toolbar, bottomToolbars);
	}

	/**
	 * Adds a toolbar to the datatable that will be displayed before the data
	 * 
	 * @param toolbar
	 *            toolbar to be added
	 * 
	 * @see AbstractToolbar
	 */
	public void addTopToolbar(final AbstractToolbar toolbar)
	{
		addToolbar(toolbar, topToolbars);
	}

	/**
	 * @return dataprovider
	 */
	public final IDataProvider<T> getDataProvider()
	{
		return datagrid.getDataProvider();
	}

	/**
	 * @return array of column objects this table displays
	 */
	public final List<IColumn<T>> getColumns()
	{
		return columns;
	}

	/**
	 * @see org.apache.wicket.markup.html.navigation.paging.IPageable#getCurrentPage()
	 */
	public final int getCurrentPage()
	{
		return datagrid.getCurrentPage();
	}

	/**
	 * @see org.apache.wicket.markup.html.navigation.paging.IPageable#getPageCount()
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
	 * @return number of rows per page
	 */
	public final int getItemsPerPage()
	{
		return datagrid.getItemsPerPage();
	}

	/**
	 * @see org.apache.wicket.markup.html.navigation.paging.IPageable#setCurrentPage(int)
	 */
	public final void setCurrentPage(final int page)
	{
		datagrid.setCurrentPage(page);
		onPageChanged();
	}


	/**
	 * Sets the item reuse strategy. This strategy controls the creation of {@link Item}s.
	 * 
	 * @see RefreshingView#setItemReuseStrategy(IItemReuseStrategy)
	 * @see IItemReuseStrategy
	 * 
	 * @param strategy
	 *            item reuse strategy
	 * @return this for chaining
	 */
	public final DataTable<T> setItemReuseStrategy(final IItemReuseStrategy strategy)
	{
		datagrid.setItemReuseStrategy(strategy);
		return this;
	}

	/**
	 * Sets the number of items to be displayed per page
	 * 
	 * @param items
	 *            number of items to display per page
	 * 
	 */
	public void setItemsPerPage(final int items)
	{
		datagrid.setItemsPerPage(items);
	}

	/**
	 * @see org.apache.wicket.markup.html.navigation.paging.IPageableItems#getItemCount()
	 */
	public int getItemCount()
	{
		return datagrid.getItemCount();
	}

	private void addToolbar(final AbstractToolbar toolbar, final RepeatingView container)
	{
		if (toolbar == null)
		{
			throw new IllegalArgumentException("argument [toolbar] cannot be null");
		}

		container.add(toolbar);
	}

	/**
	 * Factory method for Item container that represents a cell in the underlying DataGridView
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
	protected Item<IColumn<T>> newCellItem(final String id, final int index,
		final IModel<IColumn<T>> model)
	{
		return new Item<IColumn<T>>(id, index, model);
	}

	/**
	 * Factory method for Item container that represents a row in the underlying DataGridView
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
	protected Item<T> newRowItem(final String id, final int index, final IModel<T> model)
	{
		return new Item<T>(id, index, model);
	}

	/**
	 * @see org.apache.wicket.Component#onDetach()
	 */
	@Override
	protected void onDetach()
	{
		super.onDetach();

		for (IColumn<?> column : columns)
		{
			column.detach();
		}
	}

	/**
	 * Event listener for page-changed event
	 */
	protected void onPageChanged()
	{
		// noop
	}

	/**
	 * This class acts as a repeater that will contain the toolbar.
	 * 
	 * @author igor.vaynberg
	 */
	private static class ToolbarsContainer extends RepeatingView
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Constructor
		 * 
		 * @param id
		 */
		private ToolbarsContainer(final String id)
		{
			super(id);
		}
	}
}
