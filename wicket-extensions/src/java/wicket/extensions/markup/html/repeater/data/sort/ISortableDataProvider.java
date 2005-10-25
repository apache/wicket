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

import java.util.List;

import wicket.extensions.markup.html.repeater.data.IDataProvider;

/**
 * A data provider that can sort on it's columns.
 * 
 * @author Igor Vaynberg ( ivaynberg )
 * @author Phil Kulak
 */
public interface ISortableDataProvider extends IDataProvider
{
	/**
	 * Adds the field to the end of the sort list. If it already exists, it's
	 * first removed, then the ordering is flipped.
	 * 
	 * @param property
	 *            the name of the property to sort on
	 */
	public void addSort(String property);

	/**
	 * @return list of sort params (most recent first)
	 */
	public List getSortList();

	/**
	 * Gets the sort state of a property
	 * 
	 * @param property
	 *            sort property to be checked
	 * @return 1 if ascending, -1 if descending, 0 if none
	 */
	public SortState getSortState(String property);

	/**
	 * A simple class to represent the state of a sort field.
	 */
	public class SortState
	{
		/** the field is sorted "up" */
		public static final int ASCENDING = 1;

		/** the field is sorted "down" */
		public static final int DESCENDING = -1;

		/** the field is not used for sorting */
		public static final int NONE = 0;

		private int state;

		private int level;

		/**
		 * @param state
		 *            one of ASCENDING, DESCENDING, or NONE
		 * @param level
		 *            level of sort param used for sorting on multiple columns
		 */
		public SortState(int state, int level)
		{
			this.state = state;
			this.level = level;
		}

		/**
		 * @return sort level used when sorting by multiple columns
		 */
		public int getLevel()
		{
			return level;
		}

		/**
		 * @return one of ASCENDING, DESCENDING, or NONE
		 */
		public int getState()
		{
			return state;
		}
	}

}
