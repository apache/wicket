package wicket.extensions.markup.html.repeater.data;

import java.util.Iterator;

import wicket.MarkupContainer;
import wicket.extensions.markup.html.repeater.OrderedRepeatingView;
import wicket.extensions.markup.html.repeater.pageable.Item;
import wicket.model.IModel;
import wicket.version.undo.Change;

/**
 * A pageable DataView which breaks the data in the IDataProvider into a number
 * of data-rows, depending on the column size. A typical use case is to show
 * items in a table with ie 3 columns where the table is filled left to right
 * top-down so that for each third item a new row is created.
 * <p>
 * Example:
 * 
 * <pre>
 *         
 *         
 *         &lt;tbody&gt; &lt;tr wicket:id=&quot;rows&quot; class=&quot;even&quot;&gt;
 *         &lt;td wicket:id=&quot;cols&quot;&gt; &lt;span
 *         wicket:id=&quot;id&quot;&gt;Test ID&lt;/span&gt;&lt;/td&gt; ...
 *         
 *         
 * </pre>
 * 
 * <p>
 * 
 * @author Igor Vaynberg
 * @author Christian Essl
 * 
 */
public abstract class GridView extends AbstractDataView
{

	private int columns = 1;
	private int rows = 1;


	/**
	 * @param id
	 *            component id
	 * @param dataProvider
	 *            data provider
	 */
	public GridView(String id, IDataProvider dataProvider)
	{
		super(id, dataProvider);
	}

	/**
	 * @param id
	 *            component id
	 * @param model
	 *            component model - model object must be instance of
	 *            IDataProvider
	 */
	public GridView(String id, IModel model)
	{
		super(id, model);
	}

	/**
	 * @param id
	 *            component id
	 */
	public GridView(String id)
	{
		super(id);
		// TODO Auto-generated constructor stub
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
	public GridView setColumns(int cols)
	{
		if (cols < 1)
		{
			throw new IllegalArgumentException();
		}

		if (columns != cols)
		{
			addStateChange(new Change()
			{
				private static final long serialVersionUID = 1L;

				final int old = columns;

				public void undo()
				{
					columns = old;
				}
			});
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
	public GridView setRows(int rows)
	{
		if (rows < 1)
		{
			throw new IllegalArgumentException();
		}

		if (this.rows != rows)
		{
			addStateChange(new Change()
			{
				private static final long serialVersionUID = 1L;

				final int old = GridView.this.rows;

				public void undo()
				{
					GridView.this.rows = old;
				}
			});
			this.rows = rows;
		}
		updateItemsPerPage();
		return this;
	}

	private void updateItemsPerPage()
	{
		internalSetItemsPerPage(rows * columns);
	}

	protected void addItems(Iterator items)
	{
		if (items.hasNext())
		{
			final int cols = getColumns();

			int row = 0;

			// TODO do we really need this index?
			int index = 0;

			do
			{
				// build a row
				Item rowItem = newRowItem(newChildId(), row);
				OrderedRepeatingView rowView = new OrderedRepeatingView("cols");
				rowItem.add(rowView);
				add(rowItem);

				// populate the row
				for (int i = 0; i < cols; i++)
				{
					final Item cellItem;
					if (items.hasNext())
					{
						cellItem = (Item)items.next();
					}
					else
					{
						cellItem = newEmptyItem(newChildId(), index);
						populateEmptyItem(cellItem);
					}
					rowView.add(cellItem);
					index++;
				}

				// increase row
				row++;

			}
			while (items.hasNext());
		}

	}

	public Iterator getItems()
	{
		return new ItemsIterator(iterator());
	}

	/**
	 * Add component to an Item for which there is no model anymore and is shown
	 * in a cell
	 * 
	 * @param item
	 *            Item object
	 */
	abstract protected void populateEmptyItem(Item item);

	/**
	 * Create a Item which represents an empty cell (there is no model for it in
	 * the DataProvider)
	 * 
	 * @param id
	 * @param index
	 * @return created item
	 */
	protected Item newEmptyItem(String id, int index)
	{
		return new Item(id, index, null);
	}

	/**
	 * Create a new Item which will hold a row.
	 * 
	 * @param id
	 * @param index
	 * @return created Item
	 */
	protected Item newRowItem(String id, int index)
	{
		return new Item(id, index, null);
	}

	/**
	 * Iterator that iterats over all items in the cells
	 * 
	 * @author igor
	 * 
	 */
	private static class ItemsIterator implements Iterator
	{
		private Iterator rows;
		private Iterator cells;

		private Item next;

		/**
		 * @param rows iterator over child row views
		 */
		public ItemsIterator(Iterator rows)
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
		public Object next()
		{
			Item item = next;
			findNext();
			return item;
		}

		private void findNext()
		{
			next = null;

			if (cells != null && cells.hasNext())
			{
				next = (Item)cells.next();
			}

			while (rows.hasNext())
			{
				MarkupContainer row = (MarkupContainer)rows.next();
				cells = ((MarkupContainer)row.iterator().next()).iterator();
				if (cells.hasNext())
				{
					next = (Item)cells.next();
					break;
				}
			}
		}

	}
}