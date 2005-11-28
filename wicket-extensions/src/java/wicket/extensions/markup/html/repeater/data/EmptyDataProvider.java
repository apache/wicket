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
package wicket.extensions.markup.html.repeater.data;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import wicket.extensions.markup.html.repeater.data.sort.ISortableDataProvider;
import wicket.model.IModel;

/**
 * A convienience class to represent an empty data provider.
 * 
 * @author Phil Kulak
 */
public class EmptyDataProvider implements ISortableDataProvider
{
	private static final long serialVersionUID = 1L;

	private static final SortState sortState=new SortState(SortState.NONE, 0);
	
	private static EmptyDataProvider INSTANCE = new EmptyDataProvider();

	/**
	 * @return the singleton instance of this class
	 */
	public static EmptyDataProvider getInstance()
	{
		return INSTANCE;
	}

	/**
	 * @see wicket.extensions.markup.html.repeater.data.sort.ISortableDataProvider#addSort(java.lang.String)
	 */
	public void addSort(String property)
	{
	}

	/**
	 * @see wicket.extensions.markup.html.repeater.data.sort.ISortableDataProvider#getSortList()
	 */
	public List getSortList()
	{
		return Collections.EMPTY_LIST;
	}

	/**
	 * @see wicket.extensions.markup.html.repeater.data.sort.ISortableDataProvider#getSortState(java.lang.String)
	 */
	public SortState getSortState(String property)
	{
		return sortState;
	}

	/**
	 * @see wicket.extensions.markup.html.repeater.data.IDataProvider#iterator(int,
	 *      int)
	 */
	public Iterator iterator(int first, int count)
	{
		return Collections.EMPTY_LIST.iterator();
	}

	/**
	 * @see wicket.extensions.markup.html.repeater.data.IDataProvider#size()
	 */
	public int size()
	{
		return 0;
	}

	/**
	 * @see wicket.extensions.markup.html.repeater.data.IDataProvider#model(java.lang.Object)
	 */
	public IModel model(Object object)
	{
		return null;
	}
}
