/*
 * $Id$
 * $Revision$ $Date$
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.markup.html.list;

import java.io.Serializable;
import java.util.List;

import wicket.markup.html.WebMarkupContainer;
import wicket.model.IModel;
import wicket.model.Model;

/**
 * PageableListView is similar to ListView but provides in addition pageable
 * views. A PageableListView holds pageable rows of information. The rows can be
 * re-ordered and deleted, either one at a time or many at a time.
 *
 * @author Jonathan Locke
 */
public abstract class PageableListView_Detachable extends WebMarkupContainer
{
    // TODO should delegate to ListView instead of derive
    
    private final ListView listView;
    
	/** The page to show. */
	private int currentPage;

	/** Number of rows per page of the list view. */
	private final int rowsPerPage;

	/**
	 * Constructor
	 * 
	 * @param name
	 *            See Component
	 * @param model
	 *            See Component
	 * @param rowsPerPage
	 *            Number of rows to show on a page
	 */
	public PageableListView_Detachable(final String name, final IModel model, int rowsPerPage)
	{
		super(name, model);
		this.listView = new ListView(name, model)
		{
			protected void populateItem(final ListItem listItem)
			{
			    PageableListView_Detachable.this.populateItem(listItem);
			}

			/**
			 * @see wicket.markup.html.list.ListView#getViewSize()
			 */
			public int getViewSize()
			{
				if (this.getModelObject() != null)
				{
					super.setStartIndex(PageableListView_Detachable.this.getCurrentPage() * PageableListView_Detachable.this.getRowsPerPage());
					super.setViewSize(PageableListView_Detachable.this.getRowsPerPage());
				}

				return super.getViewSize();
			}
		};
		
		this.rowsPerPage = rowsPerPage;
		this.setCurrentPage(0);
	}

	/**
	 * Creates a pagable list view having the given number of rows per page that
	 * uses the provided object as a simple model.
	 * 
	 * @param name
	 *            See Component
	 * @param list
	 *            See Component
	 * @param rowsPerPage
	 *            Number of rows to show on a page
	 * @see ListView#ListView(String, List)
	 */
	public PageableListView_Detachable(final String name, final List list, final int rowsPerPage)
	{
		this(name, new Model((Serializable)list), rowsPerPage);
	}

	/**
	 * @see wicket.Component#initModel()
	 */
	protected IModel initModel()
	{
		return listView.getModel();
	}
	
	/**
	 * Populate a given listItem.
	 * 
	 * @param listItem
	 *            The listItem to populate
	 */
	protected abstract void populateItem(final ListItem listItem);

	/**
	 * 
	 * @return List
	 */
	public List getList()
	{
	    return listView.getList();
	}
	
	/**
	 * Gets the index of the current page being displayed by this list view.
	 * 
	 * @return Returns the currentPage.
	 */
	public final int getCurrentPage()
	{
	    // Depending on the list, that may take some time (select count...)
		final int size = getList().size();
		
		// If first cell is out of range, bring page back into range
		while ((currentPage * rowsPerPage) > size)
		{
			currentPage--;
		}

		return currentPage;
	}

	/**
	 * Gets the number of pages in this list view.
	 * 
	 * @return The number of pages in this list view
	 */
	public final int getPageCount()
	{
		return ((getList().size() + rowsPerPage) - 1) / rowsPerPage;
	}

	/**
	 * Get the maximum number of rows on each page.
	 * 
	 * @return the maximum number of rows on each page.
	 */
	public final int getRowsPerPage()
	{
		return rowsPerPage;
	}

	/**
	 * Sets the current page that this list view should show.
	 * 
	 * @param currentPage
	 *            The currentPage to set.
	 */
	public final void setCurrentPage(final int currentPage)
	{
		if (currentPage < 0)
		{
			throw new IllegalArgumentException("Cannot set current page to " + currentPage);
		}

		int pageCount = getPageCount();
		if (currentPage > 0 && (currentPage >= pageCount))
		{
			throw new IllegalArgumentException("Cannot set current page to " + currentPage
					+ " because this pageable list view only has " + pageCount + " pages");
		}

		this.currentPage = currentPage;
		
		listView.setStartIndex(currentPage * this.rowsPerPage);
		listView.setViewSize(this.getRowsPerPage());
	}
	
	/**
	 * @see ListView#getStartIndex()
	 * @return int
	 */
	public int getStartIndex()
	{
		return listView.getStartIndex();
	}
	/**
	 * @see ListView#getViewSize()
	 * @return int
	 */
	public int getViewSize()
	{
		return listView.getViewSize();
	}
	
	/**
	 * Get the underlying list view.
	 * @return ListView
	 */
	public ListView getListView()
	{
	    return this.listView;
	}
}