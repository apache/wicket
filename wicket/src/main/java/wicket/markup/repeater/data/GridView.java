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

import wicket.Component;
import wicket.MarkupContainer;
import wicket.markup.repeater.Item;
import wicket.markup.repeater.RepeatingView;
import wicket.version.undo.Change;

/**
 * A pageable DataView which breaks the data in the IDataProvider into a number
 * of data-rows, depending on the column size. A typical use case is to show
 * items in a table with ie 3 columns where the table is filled left to right
 * top-down so that for each third item a new row is created.
 * <p>
 * Example
 * 
 * <pre>
 *     &lt;tbody&gt;
 *       &lt;tr wicket:id=&quot;rows&quot; class&quot;even&quot;&gt;
 *         &lt;td wicket:id=&quot;cols&quot;&gt;
 *           &lt;span wicket:id=&quot;id&quot;&gt;Test ID&lt;/span&gt;
 *         &lt;/td&gt;
 *       &lt;/tr&gt;
 *     &lt;/tbody&gt;  
 * </pre>
 * 
 * and in java:
 * 
 * <pre>
 * add(new GridView(&quot;rows&quot;, dataProvider).setColumns(3));
 * </pre>
 * 
 * @author Igor Vaynberg
 * @author Christian Essl
 * 
 * @param <T> 
 * 			Type of model object this component holds 
 */
public abstract class GridView<T> extends DataViewBase<T>
{

	private int columns = 1;
	private int rows = Integer.MAX_VALUE;


	/**
	 * @param parent
	 *            The parent of this component The parent of this component.
	 * @param id
	 *            component id
	 * @param dataProvider
	 *            data provider
	 */
	public GridView(MarkupContainer<?> parent, final String id, IDataProvider<T> dataProvider)
	{
		super(parent, id, dataProvider);
	}


	/**
	 * @return number of columns
	 */
	public int getColumns()
	{
		return columns;
	}

	/**
	 * Sets number of columns
	 * 
	 * @param cols
	 *            number of colums
	 * @return this for chaining
	 */
	public GridView<T> setColumns(int cols)
	{
		if (cols < 1)
		{
			throw new IllegalArgumentException();
		}

		if (columns != cols)
		{
			if (isVersioned())
			{
				addStateChange(new Change()
				{
					private static final long serialVersionUID = 1L;

					final int old = columns;

					@Override
					public void undo()
					{
						columns = old;
					}

					@Override
					public String toString()
					{
						return "GridViewColumnsChange[component: " + getPath()
								+ ", removed columns: " + old + "]";
					}
				});
			}
			columns = cols;
		}
		updateItemsPerPage();
		return this;
	}

	/**
	 * @return number of rows per page
	 */
	public int getRows()
	{
		return rows;
	}

	/**
	 * Sets number of rows per page
	 * 
	 * @param rows
	 *            number of rows
	 * @return this for chaining
	 */
	public GridView<T> setRows(int rows)
	{
		if (rows < 1)
		{
			throw new IllegalArgumentException();
		}

		if (this.rows != rows)
		{
			if (isVersioned())
			{
				addStateChange(new Change()
				{
					private static final long serialVersionUID = 1L;

					final int old = GridView.this.rows;

					@Override
					public void undo()
					{
						GridView.this.rows = old;
					}

					@Override
					public String toString()
					{
						return "GridViewRowsChange[component: " + getPath() + ", removed rows: "
								+ old + "]";
					}
				});
			}
			this.rows = rows;
		}

		// TODO Post 1.2: Performance: Can this be moved into the this.rows !=
		// rows if
		// block for optimization?
		updateItemsPerPage();
		return this;
	}

	private void updateItemsPerPage()
	{
		int items = Integer.MAX_VALUE;

		long result = (long)rows * (long)columns;

		// overflow check
		int desiredHiBits = -((int)(result >>> 31) & 1);
		int actualHiBits = (int)(result >>> 32);

		if (desiredHiBits == actualHiBits)
		{
			items = (int)result;
		}

		internalSetRowsPerPage(items);

	}


	@Override
	protected void addItems(Iterator<Item<T>> items)
	{
		if (items.hasNext())
		{
			final int cols = getColumns();

			int row = 0;

			do
			{
				// Build a row
				Item<T> rowItem = newRowItem(newChildId(), row);
				new RepeatingView<T>(rowItem, "cols");

				// Populate the row
				for (int index = 0; index < cols; index++)
				{
					final Item<T> cellItem;
					if (items.hasNext())
					{
						cellItem = items.next();
					}
					else
					{
						cellItem = newEmptyItem(newChildId(), index);
						populateEmptyItem(cellItem);
					}
				}

				// increase row
				row++;

			}
			while (items.hasNext());
		}

	}

	/**
	 * @return data provider
	 */
	public IDataProvider<T> getDataProvider()
	{
		return internalGetDataProvider();
	}

	/**
	 * @see wicket.markup.repeater.AbstractPageableView#getItems()
	 */
	@Override
	public Iterator<Item<T>> getItems()
	{
		return new ItemsIterator<T>(iterator());
	}

	/**
	 * Add component to an Item for which there is no model anymore and is shown
	 * in a cell
	 * 
	 * @param item
	 *            Item object
	 */
	abstract protected void populateEmptyItem(Item<T> item);

	/**
	 * Create a Item which represents an empty cell (there is no model for it in
	 * the DataProvider)
	 * 
	 * @param id
	 * @param index
	 * @return created item
	 */
	protected Item<T> newEmptyItem(final String id, int index)
	{
		return new Item<T>(this, id, index, null);
	}

	/**
	 * Create a new Item which will hold a row.
	 * 
	 * @param id
	 * @param index
	 * @return created Item
	 */
	protected Item<T> newRowItem(final String id, int index)
	{
		return new Item<T>(this, id, index, null);
	}

	/**
	 * Iterator that iterats over all items in the cells
	 * 
	 * @author igor
	 *
	 * @param <T> 
	 * 		Type of model object this component holds 
	 */
	private static class ItemsIterator<T> implements Iterator<Item<T>>
	{
		private Iterator<Component<?>> rows;
		private Iterator<Item<T>> cells;

		private Item<T> next;

		/**
		 * @param rows
		 *            iterator over child row views
		 */
		public ItemsIterator(Iterator<Component<?>> rows)
		{
			this.rows = rows;
			findNext();
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
			return next != null;
		}

		/**
		 * @see java.util.Iterator#next()
		 */
		public Item<T> next()
		{
			Item<T> item = next;
			findNext();
			return item;
		}

		private void findNext()
		{
			next = null;

			if (cells != null && cells.hasNext())
			{
				next = cells.next();
			}

			while (rows.hasNext())
			{
				MarkupContainer<?> row = (MarkupContainer)rows.next();
				cells = ((MarkupContainer)row.iterator().next()).iterator();
				if (cells.hasNext())
				{
					next = cells.next();
					break;
				}
			}
		}

	}
}