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
import wicket.contrib.data.model.ISelectCountAndListAction;
import wicket.contrib.data.model.PageableList;
import wicket.contrib.data.model.hibernate.HibernateCountAndListAction;
import wicket.contrib.data.util.hibernate.HibernateHelperSessionDelegate;
import wicket.model.DetachableModel;

/**
 * Model that keeps a (current) search argument, and uses a pageable list as
 * it's model object.
 * 
 * @author Eelco Hillenius
 */
public final class CDSearchModel extends DetachableModel
{
	/** zoek opdracht. */
	private String searchString = null;

	/** number of rows on each page. */
	private int rowsPerPage = 8;

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
	 * @see wicket.model.DetachableModel#onAttach()
	 */
	protected void onAttach()
	{
		final String searchStringParameter;
		if (searchString != null)
		{
			searchStringParameter = '%' + searchString.toUpperCase() + '%';
		}
		else
		{
			searchStringParameter = null;
		}
		ISelectCountAndListAction searchCDAction = new HibernateCountAndListAction(
				"wicket.examples.cdapp.model.SearchCD", "wicket.examples.cdapp.model.SearchCD.count",
				new HibernateHelperSessionDelegate())
		{
			protected void setParameters(Query query, Object queryObject) throws HibernateException
			{
				query.setString("performers", searchStringParameter);
				query.setString("title", searchStringParameter);
				query.setString("label", searchStringParameter);
			}
		};
		PageableList list = new PageableList(rowsPerPage, searchCDAction);
		setObject(list);
	}

	/**
	 * @see wicket.model.DetachableModel#onDetach()
	 */
	protected void onDetach()
	{
		setObject(null);
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
	 * Sets the searchString.
	 * @param searchString searchString
	 */
	public final void setSearchString(String searchString)
	{
		detach(); // force reload right away
		this.searchString = searchString;
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
	 * Convenience method to figure out if this model has any rows at all.
	 * @return whether there are any rows found
	 */
	public final boolean hasResults()
	{
		attach(); // just to be sure
		List results = (List)getObject();
		return (!results.isEmpty());
	}
}
