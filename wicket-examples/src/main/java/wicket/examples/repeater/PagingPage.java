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

import wicket.AttributeModifier;
import wicket.markup.html.basic.Label;
import wicket.markup.html.navigation.paging.PagingNavigator;
import wicket.markup.repeater.Item;
import wicket.markup.repeater.data.DataView;
import wicket.model.AbstractReadOnlyModel;

/**
 * Page that demonstrates a paging dataview
 * 
 * @see wicket.markup.repeater.data.DataView
 * @see wicket.markup.repeater.PageableRefreshingView
 * @see wicket.markup.html.navigation.paging.PagingNavigator
 * @author igor
 * 
 */
public class PagingPage extends BasePage
{
	/**
	 * constructor
	 */
	public PagingPage()
	{
		DataView dataView = new DataView(this, "pageable", new ContactDataProvider())
		{

			@Override
			protected void populateItem(final Item item)
			{
				Contact contact = (Contact)item.getModelObject();
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

		new PagingNavigator(this, "navigator", dataView);
	}
}
