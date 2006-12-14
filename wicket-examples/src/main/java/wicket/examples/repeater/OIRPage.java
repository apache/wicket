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
import wicket.MarkupContainer;
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
		 * @param parent
		 *            The parent of this component The parent of this component.
		 * @param id
		 * @param index
		 * @param model
		 */
		public HighlitableDataItem(MarkupContainer parent, final String id, int index, IModel model)
		{
			super(parent, id, index, model);
			add(new AttributeModifier("style", true, new Model<String>("background-color:#80b6ed;"))
			{
				@Override
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

		final DataView dataView = new DataView(this, "oir", dp)
		{
			@Override
			protected void populateItem(final Item item)
			{
				Contact contact = (Contact)item.getModelObject();
				new ActionPanel(item, "actions", item.getModel());
				new Link(item, "toggleHighlite")
				{
					@Override
					public void onClick()
					{
						HighlitableDataItem hitem = (HighlitableDataItem)item;
						hitem.toggleHighlite();
					}
				};
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

			/**
			 * @see wicket.markup.repeater.RefreshingView#newItem(wicket.MarkupContainer, java.lang.String, int, wicket.model.IModel)
			 */
			@Override
			protected Item newItem(MarkupContainer parent, String id, int index, IModel model)
			{
				return new HighlitableDataItem(this, id, index, model);
			}
		};

		dataView.setItemsPerPage(8);
		dataView.setItemReuseStrategy(ReuseIfModelsEqualStrategy.getInstance());

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
