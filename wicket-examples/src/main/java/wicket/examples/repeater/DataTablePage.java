/*
 * $Id$ $Revision$
 * $Date$
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
package wicket.examples.repeater;

import java.util.ArrayList;
import java.util.List;

import wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import wicket.extensions.markup.html.repeater.data.table.DefaultDataTable;
import wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import wicket.markup.repeater.Item;
import wicket.model.IModel;
import wicket.model.Model;

/**
 * demo page for the datatable component
 * 
 * @see wicket.extensions.markup.html.repeater.data.table.DefaultDataTable
 * @author igor
 * 
 */
public class DataTablePage extends BasePage
{
	/**
	 * constructor
	 */
	public DataTablePage()
	{
		List columns = new ArrayList();

		columns.add(new AbstractColumn(new Model("Actions"))
		{
			public void populateItem(Item cellItem, String componentId, IModel model)
			{
				cellItem.add(new ActionPanel(componentId, model));
			}
		});

		columns.add(new PropertyColumn(new Model("ID"), "id"));
		columns.add(new PropertyColumn(new Model("First Name"), "firstName", "firstName"));
		columns.add(new PropertyColumn(new Model("Last Name"), "lastName", "lastName"));
		columns.add(new PropertyColumn(new Model("Home Phone"), "homePhone"));
		columns.add(new PropertyColumn(new Model("Cell Phone"), "cellPhone"));

		add(new DefaultDataTable("table", columns, new SortableContactDataProvider(), 8));
	}
}
