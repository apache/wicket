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
package wicket.extensions.markup.html.repeater.data.sort;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * A {@link ISortableDataProvider} that allows sorting on columns.
 * 
 * @author Igor Vaynberg
 * @author Phil Kulak
 */
public abstract class SortableDataProvider implements ISortableDataProvider
{
	private LinkedList/* <SortParam> */sort = new LinkedList();

	private int maxColumns = 1;

	private SortParam defaultSort;

	/**
	 * Removes all sorting fields.
	 */
	public void clearSort()
	{
		sort.clear();
	}

	/**
	 * Sets the sort parameter to use when no other fields are set.
	 * 
	 * @param property
	 *            the name of the property to sort on
	 * @param ascending
	 *            true for ascending, false for descending
	 */
	public void setDefaultSort(String property, boolean ascending)
	{
		defaultSort = new SortParam(property, ascending);
	}

	/**
	 * Sets the maximum number of columns to sort on.
	 * 
	 * @param maxColumns
	 *            max number of columns
	 */
	public void setMaxColumns(int maxColumns)
	{
		if (maxColumns < 0)
		{
			throw new IllegalStateException("Max columns must be positive.");
		}

		// Trim if needed.
		while (maxColumns < sort.size())
		{
			sort.removeLast();
		}

		this.maxColumns = maxColumns;
	}

	/**
	 * Adds the sort parameter to the end of the list.
	 * 
	 * @param sp
	 *            the parameter to add
	 */
	public void addSort(SortParam sp)
	{
		if (maxColumns == 0)
		{
			return;
		}
		if (sort.size() >= maxColumns)
		{
			sort.removeLast();
		}
		sort.addFirst(sp);
	}

	/**
	 * Adds the sort parameter to the front of the list.
	 * 
	 * @param property
	 *            the name of the property to sort on
	 * @param ascending
	 *            true for ascending, false for descending
	 */
	public void addSort(String property, boolean ascending)
	{
		addSort(new SortParam(property, ascending));
	}

	/**
	 * @see ISortableDataProvider#addSort(String)
	 */
	public void addSort(String property)
	{
		SortParam sp = findByProperty(property);
		if (sp != null)
		{
			getSortList().remove(sp);
			addSort(new SortParam(property, !sp.isAscending()));
		}
		else
		{
			addSort(new SortParam(property, true));
		}
	}

	/**
	 * @return the last sort added, null if none.
	 */
	public SortParam getSort()
	{
		if (sort.size() == 0)
		{
			return null;
		}
		else
		{
			return (SortParam)sort.getFirst();
		}
	}

	/**
	 * @return the current list of sort parameters.
	 */
	public List getSortList()
	{
		if (sort.size() == 0 && defaultSort != null)
		{
			ArrayList ret = new ArrayList(1);
			ret.add(defaultSort);
			return ret;
		}
		return sort;
	}

	/**
	 * Removes all previous sort fields and adds the one given.
	 * 
	 * @param sp
	 *            new sort param
	 */
	public void setSort(SortParam sp)
	{
		clearSort();
		addSort(sp);
	}

	/**
	 * Removes all previous sort fields and adds the one given.
	 * 
	 * @param property
	 *            sort property
	 * @param ascending
	 *            ascending flag
	 */
	public void setSort(String property, boolean ascending)
	{
		setSort(new SortParam(property, ascending));
	}

	/**
	 * @see ISortableDataProvider#getSortState(String)
	 */
	public SortState getSortState(String property)
	{
		int level = -1;

		for (Iterator i = sort.iterator(); i.hasNext();)
		{
			level++;
			SortParam sortParam = (SortParam)i.next();
			if (sortParam.getProperty().equals(property))
			{
				return new SortState(sortParam.isAscending()
						? SortState.ASCENDING
						: SortState.DESCENDING, level);
			}
		}
		return new SortState(SortState.NONE, 0);
	}

	private SortParam findByProperty(String property)
	{
		Iterator it = getSortList().iterator();
		while (it.hasNext())
		{
			SortParam sp = (SortParam)it.next();
			if (sp.getProperty().equals(property))
			{
				return sp;
			}
		}
		return null;
	}
}
