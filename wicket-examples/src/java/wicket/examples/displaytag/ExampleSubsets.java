/*
 * $Id: ExampleSubsets.java 5838 2006-05-24 20:44:49 +0000 (Wed, 24 May 2006)
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

import java.util.ArrayList;
import java.util.List;

import wicket.PageParameters;
import wicket.examples.displaytag.utils.ListObject;
import wicket.examples.displaytag.utils.SimpleListView;
import wicket.examples.displaytag.utils.TestList;
import wicket.markup.html.list.ListView;
import wicket.markup.html.panel.Fragment;

/**
 * Show different means of displaying subsets of a table
 * 
 * @author Juergen Donnerstag
 */
public class ExampleSubsets extends Displaytag
{
	/**
	 * Constructor.
	 * 
	 * @param parameters
	 *            Page parameters
	 */
	public ExampleSubsets(final PageParameters parameters)
	{
		List<ListObject> data = new TestList(10, false);

		// Add table of existing comments
		newTable("table1", data);

		// First alternativ
		// Because subList() returns a view (not a copy) it is not serializable
		// and thus can not be used directly.
		List<ListObject> data2 = new ArrayList<ListObject>();
		data2.addAll(data.subList(0, 5));
		newTable("table2", data);

		// Second alternativ
		ListView table = newTable("table3", data);
		table.setStartIndex(3);
		table.setViewSize(8 - 3);
	}

	/**
	 * Because the page contains 3 times the very same table, I made use of
	 * Wicket Fragment component (inline panels).
	 * 
	 * @param id
	 * @param data
	 * @return ListView
	 */
	private ListView newTable(final String id, final List<ListObject> data)
	{
		Fragment panel = new Fragment(this, id, "tableFrag");

		ListView table = new SimpleListView<ListObject>(panel, "rows", data);

		return table;
	}
}