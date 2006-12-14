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
import wicket.Component;
import wicket.extensions.markup.html.repeater.data.sort.OrderByBorder;
import wicket.markup.html.basic.Label;
import wicket.markup.html.link.Link;
import wicket.markup.html.navigation.paging.PagingNavigator;
import wicket.markup.repeater.Item;
import wicket.markup.repeater.ReuseIfModelsEqualStrategy;
import wicket.markup.repeater.data.DataView;
import wicket.model.AbstractReadOnlyModel;
import wicket.model.IModel;
import wicket.model.Model;

/**
 * page that demonstrates dataview with ReuseIfModelsEqualStrategy
 * 
 * @author igor
 */
public class OIRPage extends BasePage
{
	private static class HighlitableDataItem extends Item
	{
		private boolean highlite = false;

		/**
		 * toggles highlite
		 */
		public void toggleHighlite()
		{
			highlite = !highlite;
		}

		/**
		 * Constructor
		 * 
		 * @param id
		 * @param index
		 * @param model
		 */
		public HighlitableDataItem(String id, int index, IModel model)
		{
			super(id, index, model);
			add(new AttributeModifier("style", true, new Model("background-color:#80b6ed;"))
			{
				public boolean isEnabled()
				{
					return HighlitableDataItem.this.highlite;
				}
			});
		}
	}

	/**
	 * Constructor
	 */
	public OIRPage()
	{
		SortableContactDataProvider dp = new SortableContactDataProvider();

		final DataView dataView = new DataView("oir", dp)
		{
			protected void populateItem(final Item item)
			{
				Contact contact = (Contact)item.getModelObject();
				item.add(new ActionPanel("actions", item.getModel()));
				item.add(new Link("toggleHighlite")
				{
					public void onClick()
					{
						HighlitableDataItem hitem = (HighlitableDataItem)item;
						hitem.toggleHighlite();
					}
				});
				item.add(new Label("contactid", String.valueOf(contact.getId())));
				item.add(new Label("firstname", contact.getFirstName()));
				item.add(new Label("lastname", contact.getLastName()));
				item.add(new Label("homephone", contact.getHomePhone()));
				item.add(new Label("cellphone", contact.getCellPhone()));

				item.add(new AttributeModifier("class", true, new AbstractReadOnlyModel()
				{
					public Object getObject(Component component)
					{
						return (item.getIndex() % 2 == 1) ? "even" : "odd";
					}
				}));
			}

			protected Item newItem(String id, int index, IModel model)
			{
				return new HighlitableDataItem(id, index, model);
			}
		};

		dataView.setItemsPerPage(8);
		dataView.setItemReuseStrategy(ReuseIfModelsEqualStrategy.getInstance());

		add(new OrderByBorder("orderByFirstName", "firstName", dp)
		{
			protected void onSortChanged()
			{
				dataView.setCurrentPage(0);
			}
		});

		add(new OrderByBorder("orderByLastName", "lastName", dp)
		{
			protected void onSortChanged()
			{
				dataView.setCurrentPage(0);
			}
		});

		add(dataView);
		add(new PagingNavigator("navigator", dataView));
	}
}
