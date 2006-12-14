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

import wicket.markup.html.basic.Label;
import wicket.markup.html.navigation.paging.PagingNavigator;
import wicket.markup.repeater.Item;
import wicket.markup.repeater.data.GridView;
import wicket.markup.repeater.data.IDataProvider;

/**
 * page for demonstrating the gridview componnet
 * 
 * @author igor
 */
public class GridViewPage extends BasePage
{
	/**
	 * Constructor
	 */
	public GridViewPage()
	{
		IDataProvider dataProvider = new ContactDataProvider();
		GridView gridView = new GridView("rows", dataProvider)
		{
			protected void populateItem(Item item)
			{
				final Contact contact = (Contact)item.getModelObject();
				item.add(new Label("firstName", contact.getFirstName() + " "
						+ contact.getLastName()));
			}

			protected void populateEmptyItem(Item item)
			{
				item.add(new Label("firstName", "*empty*"));
			}
		};

		gridView.setRows(4);
		gridView.setColumns(3);

		add(gridView);
		add(new PagingNavigator("navigator", gridView));
	}
}
