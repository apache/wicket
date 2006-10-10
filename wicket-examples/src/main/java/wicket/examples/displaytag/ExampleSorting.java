/*
 * $Id$ $Revision:
 * 5389 $ $Date$
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
		final List data = new TestList(6, false);

		// Add the table (but no table header)
		SimpleListView table = new SimpleListView("rows", data);
		add(table);

		// And this is with a little bit of magic
		add(new SortableListViewHeaders("header", table)
		{
			/**
			 * If object does not support equals()
			 */
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
		});
	}
}