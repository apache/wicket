/*
 * $Id: ExampleSorting.java 5838 2006-05-24 20:44:49 +0000 (Wed, 24 May 2006)
 * joco01 $ $Revision$ $Date: 2006-05-24 20:44:49 +0000 (Wed, 24 May
 * 2006) $
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.examples.displaytag;

import java.util.List;

import wicket.examples.displaytag.list.SortableListViewHeader;
import wicket.examples.displaytag.list.SortableListViewHeaders;
import wicket.examples.displaytag.utils.ListObject;
import wicket.examples.displaytag.utils.SimpleListView;
import wicket.examples.displaytag.utils.TestList;

/**
 * A sorted table example
 * 
 * @author Juergen Donnerstag
 */
public class ExampleSorting extends Displaytag
{
	/**
	 * Constructor.
	 * 
	 */
	public ExampleSorting()
	{
		// Test data
		final List<ListObject> data = new TestList(6, false);

		// Add the table (but no table header)
		SimpleListView<ListObject> table = new SimpleListView<ListObject>(this, "rows", data);

		// And this is with a little bit of magic
		new SortableListViewHeaders<ListObject>(this, "header", table)
		{
			/**
			 * If object does not support equals()
			 */
			@Override
			protected int compareTo(final SortableListViewHeader header, final Object o1,
					final Object o2)
			{
				if (header.getId().equals("id"))
				{
					return ((ListObject)o1).getId() - ((ListObject)o2).getId();
				}

				return super.compareTo(header, o1, o2);
			}

			/**
			 * Define how to do sorting
			 * 
			 * @see SortableListViewHeaders#getObjectToCompare(SortableListViewHeader,
			 *      java.lang.Object)
			 */
			@Override
			protected Comparable getObjectToCompare(final SortableListViewHeader header,
					final Object object)
			{
				final String name = header.getId();
				if (name.equals("name"))
				{
					return ((ListObject)object).getName();
				}
				if (name.equals("email"))
				{
					return ((ListObject)object).getEmail();
				}
				if (name.equals("status"))
				{
					return ((ListObject)object).getStatus();
				}
				if (name.equals("comment"))
				{
					return ((ListObject)object).getDescription();
				}

				return "";
			}
		};
	}
}