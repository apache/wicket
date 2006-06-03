/*
 * $Id: DataTablePage.java 5838 2006-05-24 20:44:49 +0000 (Wed, 24 May 2006)
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
package wicket.examples.repeater;

import java.util.ArrayList;
import java.util.List;

import wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import wicket.extensions.markup.html.repeater.data.table.DefaultDataTable;
import wicket.extensions.markup.html.repeater.data.table.IColumn;
import wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import wicket.extensions.markup.html.repeater.refreshing.Item;
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
		List<IColumn> columns = new ArrayList<IColumn>();

		columns.add(new AbstractColumn(new Model<String>("Actions"))
		{
			public void populateItem(Item cellItem, String componentId, IModel model)
			{
				new ActionPanel(cellItem, componentId, model);
			}
		});

		columns.add(new PropertyColumn(new Model<String>("ID"), "id"));
		columns.add(new PropertyColumn(new Model<String>("First Name"), "firstName", "firstName"));
		columns.add(new PropertyColumn(new Model<String>("Last Name"), "lastName", "lastName"));
		columns.add(new PropertyColumn(new Model<String>("Home Phone"), "homePhone"));
		columns.add(new PropertyColumn(new Model<String>("Cell Phone"), "cellPhone"));

		new DefaultDataTable(this, "table", columns, new SortableContactDataProvider(), 8);
	}
}
