/*
 * $Id: SortingPage.java 5838 2006-05-24 20:44:49 +0000 (Wed, 24 May 2006)
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

import wicket.AttributeModifier;
import wicket.extensions.markup.html.repeater.data.sort.OrderByBorder;
import wicket.markup.html.basic.Label;
import wicket.markup.html.navigation.paging.PagingNavigator;
import wicket.markup.repeater.Item;
import wicket.markup.repeater.data.DataView;
import wicket.model.AbstractReadOnlyModel;

/**
 * page that demonstrates dataview and sorting
 * 
 * @see wicket.markup.repeater.data.DataView
 * @see wicket.extensions.markup.html.repeater.data.sort.OrderByBorder
 * @see wicket.extensions.markup.html.repeater.data.sort.OrderByLink
 * 
 * @author igor
 * 
 */
public class SortingPage extends BasePage
{
	/**
	 * constructor
	 */
	public SortingPage()
	{
		SortableContactDataProvider dp = new SortableContactDataProvider();
		final DataView<Contact> dataView = new DataView<Contact>(this, "sorting", dp)
		{
			@Override
			protected void populateItem(final Item<Contact> item)
			{
				Contact contact = item.getModelObject();
				new ActionPanel(item, "actions", item.getModel());
				new Label(item, "contactid", String.valueOf(contact.getId()));
				new Label(item, "firstname", contact.getFirstName());
				new Label(item, "lastname", contact.getLastName());
				new Label(item, "homephone", contact.getHomePhone());
				new Label(item, "cellphone", contact.getCellPhone());

				item.add(new AttributeModifier("class", true, new AbstractReadOnlyModel<String>()
				{
					@Override
					public String getObject()
					{
						return (item.getIndex() % 2 == 1) ? "even" : "odd";
					}
				}));
			}
		};

		dataView.setItemsPerPage(8);

		new OrderByBorder(this, "orderByFirstName", "firstName", dp)
		{
			@Override
			protected void onSortChanged()
			{
				dataView.setCurrentPage(0);
			}
		};

		new OrderByBorder(this, "orderByLastName", "lastName", dp)
		{
			@Override
			protected void onSortChanged()
			{
				dataView.setCurrentPage(0);
			}
		};


		new PagingNavigator(this, "navigator", dataView);
	}
}
