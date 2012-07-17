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

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.DataGridView;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.navigation.paging.IPageableItems;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.IItemReuseStrategy;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RefreshingView;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;


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
 * @param <S>
 *            the type of the sorting parameter
 * 
 */
public class DataTable<T, S> extends Panel implements IPageableItems
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
				tag.append("class", className, " ");
			}
		}
	}

	private static final long serialVersionUID = 1L;

	private final DataGridView<T> datagrid;

	private final WebMarkupContainer body;

	private final List<IColumn<T, S>> columns;

	private final ToolbarsContainer topToolbars;

	private final ToolbarsContainer bottomToolbars;

	private final Caption caption;

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
	public DataTable(final String id, final List<IColumn<T, S>> columns,
		final IDataProvider<T> dataProvider, final long rowsPerPage)
	{
		super(id);

		Args.notEmpty(columns, "columns");


		this.columns = columns;
		this.caption = new Caption("caption", getCaptionModel());
		add(caption);
		body = newBodyContainer("body");
		datagrid = new DataGridView<T>("rows", columns, dataProvider)
		{
			private static final long serialVersionUID = 1L;

			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			protected Item newCellItem(final String id, final int index, final IModel model)
			{
				Item item = DataTable.this.newCellItem(id, index, model);
				final IColumn<T, S> column = DataTable.this.columns.get(index);
				if (column instanceof IStyledColumn)
				{
					item.add(new CssAttributeBehavior()
					{
						private static final long serialVersionUID = 1L;

						@Override
						protected String getCssClass()
						{
							return ((IStyledColumn<T, S>)column).getCssClass();
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
	 * Returns the model for table's caption. The caption wont be rendered if the model has empty
	 * value.
	 * 
	 * @return the model for table's caption
	 */
	protected IModel<String> getCaptionModel()
	{
		return null;
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
		body.add(AttributeModifier.replace("class", cssStyle));
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
	public final List<IColumn<T, S>> getColumns()
	{
		return columns;
	}

	/**
	 * @see org.apache.wicket.markup.html.navigation.paging.IPageable#getCurrentPage()
	 */
	@Override
	public final long getCurrentPage()
	{
		return datagrid.getCurrentPage();
	}

	/**
	 * @see org.apache.wicket.markup.html.navigation.paging.IPageable#getPageCount()
	 */
	@Override
	public final long getPageCount()
	{
		return datagrid.getPageCount();
	}

	/**
	 * @return total number of rows in this table
	 */
	public final long getRowCount()
	{
		return datagrid.getRowCount();
	}

	/**
	 * @return number of rows per page
	 */
	@Override
	public final long getItemsPerPage()
	{
		return datagrid.getItemsPerPage();
	}

	/**
	 * @see org.apache.wicket.markup.html.navigation.paging.IPageable#setCurrentPage(long)
	 */
	@Override
	public final void setCurrentPage(final long page)
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
	public final DataTable<T, S> setItemReuseStrategy(final IItemReuseStrategy strategy)
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
	public void setItemsPerPage(final long items)
	{
		datagrid.setItemsPerPage(items);
	}

	/**
	 * @see org.apache.wicket.markup.html.navigation.paging.IPageableItems#getItemCount()
	 */
	@Override
	public long getItemCount()
	{
		return datagrid.getItemCount();
	}

	private void addToolbar(final AbstractToolbar toolbar, final ToolbarsContainer container)
	{
		Args.notNull(toolbar, "toolbar");

		container.getRepeatingView().add(toolbar);
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
	protected Item<IColumn<T, S>> newCellItem(final String id, final int index,
		final IModel<IColumn<T, S>> model)
	{
		return new Item<IColumn<T, S>>(id, index, model);
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

		for (IColumn<T, S> column : columns)
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
	 * This class acts as a repeater that will contain the toolbar. It makes sure that the table row
	 * group (e.g. thead) tags are only visible when they contain rows in accordance with the HTML
	 * specification.
	 * 
	 * @author igor.vaynberg
	 */
	private static class ToolbarsContainer extends WebMarkupContainer
	{
		private static final long serialVersionUID = 1L;

		private final RepeatingView toolbars;

		/**
		 * Constructor
		 * 
		 * @param id
		 */
		private ToolbarsContainer(final String id)
		{
			super(id);
			toolbars = new RepeatingView("toolbars");
			add(toolbars);
		}

		public RepeatingView getRepeatingView()
		{
			return toolbars;
		}

		@Override
		public void onConfigure()
		{
			super.onConfigure();

			toolbars.configure();

			Boolean visible = toolbars.visitChildren(new IVisitor<Component, Boolean>()
			{
				@Override
				public void component(Component object, IVisit<Boolean> visit)
				{
					object.configure();
					if (object.isVisible())
					{
						visit.stop(Boolean.TRUE);
					}
					else
					{
						visit.dontGoDeeper();
					}
				}
			});
			if (visible == null)
			{
				visible = false;
			}
			setVisible(visible);
		}
	}

	/**
	 * A caption for the table. It renders itself only if {@link DataTable#getCaptionModel()} has
	 * non-empty value.
	 */
	private static class Caption extends Label
	{
		/**
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 * 
		 * @param id
		 *            the component id
		 * @param model
		 *            the caption model
		 */
		public Caption(String id, IModel<String> model)
		{
			super(id, model);
		}

		@Override
		protected void onConfigure()
		{
			setRenderBodyOnly(Strings.isEmpty(getDefaultModelObjectAsString()));

			super.onConfigure();
		}

		@Override
		protected IModel<String> initModel()
		{
			// don't try to find the model in the parent
			return null;
		}
	}
}
