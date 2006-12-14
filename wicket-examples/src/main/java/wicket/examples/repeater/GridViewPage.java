/*
 * $Id: GridViewPage.java 5838 2006-05-24 20:44:49 +0000 (Wed, 24 May 2006)
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

import wicket.markup.html.basic.Label;
import wicket.markup.html.navigation.paging.PagingNavigator;
import wicket.markup.repeater.Item;
import wicket.markup.repeater.data.GridView;
import wicket.markup.repeater.data.IDataProvider;

/**
 * page for demonstrating the gridview componnet
 * 
 * @see wicket.markup.repeater.data.GridView
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
		GridView gridView = new GridView(this, "rows", dataProvider)
		{
			@Override
			protected void populateItem(Item item)
			{
				final Contact contact = (Contact)item.getModelObject();
				new Label(item, "firstName", contact.getFirstName() + " " + contact.getLastName());
			}

			@Override
			protected void populateEmptyItem(Item item)
			{
				new Label(item, "firstName", "*empty*");
			}
		};

		gridView.setRows(4);
		gridView.setColumns(3);

		new PagingNavigator(this, "navigator", gridView);
	}
}
