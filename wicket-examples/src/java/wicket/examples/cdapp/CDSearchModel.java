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
package wicket.examples.cdapp;

import java.util.List;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import wicket.contrib.data.model.PageableList;
import wicket.contrib.data.model.hibernate.HibernateCountAndListAction;
import wicket.contrib.data.util.hibernate.HibernateHelperSessionDelegate;
import wicket.model.AbstractDetachableModel;

/**
 * Model that keeps a (current) search argument, and uses a pageable list as
 * it's model object.
 * 
 * @author Eelco Hillenius
 */
public final class CDSearchModel extends AbstractDetachableModel
{
	/** action used by the pageable list (has our order columns). */
	private CountAndListAction countAndListAction = new CountAndListAction();	
	
	/** the list of matches */
	private transient PageableList list;

	/** number of rows on each page. */
	private int rowsPerPage = 8;
	
	/** zoek opdracht. */
	private String searchString = null;

	/** count and list action that works with this model. */
	private final class CountAndListAction extends HibernateCountAndListAction
	{
		/**
		 * Construct.
		 */
		public CountAndListAction()
		{
			super("wicket.examples.cdapp.model.SearchCD",
					"wicket.examples.cdapp.model.SearchCD.count",
					new HibernateHelperSessionDelegate());
		}

		/**
		 * @see wicket.contrib.data.model.hibernate.HibernateCountAndListAction#setParameters(net.sf.hibernate.Query, java.lang.Object)
		 */
		protected void setParameters(Query query, Object queryObject) throws HibernateException
		{
			final String searchStringParameter = getSearchStringParameter();
			query.setString("performers", searchStringParameter);
			query.setString("title", searchStringParameter);
			query.setString("label", searchStringParameter);
		}
	}

	/**
	 * Construct.
	 */
	public CDSearchModel()
	{
	}

	/**
	 * Construct.
	 * @param rowsPerPage number of rows on each page
	 */
	public CDSearchModel(int rowsPerPage)
	{
		this.rowsPerPage = rowsPerPage;
	}

	/**
	 * Add order-by field to query
	 * @param field the field to add
	 */
	public final void addOrdering(String field)
	{
		PageableList list = (PageableList)getObject();
		if (list != null)
		{
			HibernateCountAndListAction action =
				(HibernateCountAndListAction)list.getCountAndListAction();
			action.addOrdering(field);
			list.clear();
		}	
	}

	/**
	 * Gets number of rows on each page.
	 * @return number of rows on each page
	 */
	public final int getRowsPerPage()
	{
		return rowsPerPage;
	}

	/**
	 * Gets the searchString.
	 * @return searchString
	 */
	public final String getSearchString()
	{
		return searchString;
	}

	/**
	 * Convenience method to figure out if this model has any rows at all.
	 * @return whether there are any rows found
	 */
	public final boolean hasResults()
	{
		List results = (List)getObject();
		return (!results.isEmpty());
	}

	/**
	 * Sets the searchString.
	 * @param searchString searchString
	 */
	public final void setSearchString(String searchString)
	{
		detach(); // force reload right away
		this.searchString = searchString;
	}

	/**
	 * @see AbstractDetachableModel#onAttach()
	 */
	protected void onAttach()
	{
		list = new PageableList(rowsPerPage, countAndListAction);
	}

	/**
	 * @see AbstractDetachableModel#onDetach()
	 */
	protected void onDetach()
	{
		list = null;
	}

	/**
	 * @see wicket.model.AbstractDetachableModel#onGetObject()
	 */
	protected Object onGetObject()
	{
		return list;
	}

	/**
	 * Gets the current search string as a query parameter.
	 * @return the current search string as a query parameter
	 */
	private String getSearchStringParameter()
	{
		final String searchStringParameter;
		if (searchString != null)
		{
			return '%' + searchString.toUpperCase() + '%';
		}
		return null;
	}
}
