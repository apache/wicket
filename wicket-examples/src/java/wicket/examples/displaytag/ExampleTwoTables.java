/*
 * $Id: ExampleTwoTables.java 5838 2006-05-24 20:44:49 +0000 (Wed, 24 May 2006)
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
import wicket.examples.displaytag.utils.TestList;

/**
 * Two independent sortable + pageable tables on the same page
 * 
 * @author Juergen Donnerstag
 */
public class ExampleTwoTables extends Displaytag
{
	/**
	 * Constructor.
	 * 
	 * @param parameters
	 *            Page parameters
	 */
	public ExampleTwoTables(final PageParameters parameters)
	{
		final List<ListObject> data1 = new TestList(60, false);
		final List<ListObject> data2 = new ArrayList<ListObject>();
		data2.addAll(data1);

		new SortablePageableDisplaytagTableComponent(this, "table1", data1);
		new SortablePageableDisplaytagTableComponent(this, "table2", data2);
	}
}