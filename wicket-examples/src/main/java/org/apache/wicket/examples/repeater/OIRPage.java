/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.examples.repeater;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.OrderByBorder;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.ReuseIfModelsEqualStrategy;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

/**
 * page that demonstrates dataview with ReuseIfModelsEqualStrategy
 * 
 * @author igor
 */
public class OIRPage extends BasePage
{
	private static final long serialVersionUID = 1L;

	private static class HighlitableDataItem<T> extends Item<T>
	{
		private static final long serialVersionUID = 1L;

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
		public HighlitableDataItem(String id, int index, IModel<T> model)
		{
			super(id, index, model);
			add(new AttributeModifier("style", "background-color:#80b6ed;")
			{
				private static final long serialVersionUID = 1L;

				@Override
				public boolean isEnabled(Component component)
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

		final DataView<Contact> dataView = new DataView<Contact>("oir", dp)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(final Item<Contact> item)
			{
				Contact contact = item.getModelObject();
				item.add(new ActionPanel("actions", item.getModel()));
				item.add(new Link<Void>("toggleHighlite")
				{
					private static final long serialVersionUID = 1L;

					@Override
					public void onClick()
					{
						HighlitableDataItem<Contact> hitem = (HighlitableDataItem<Contact>)item;
						hitem.toggleHighlite();
					}
				});
				item.add(new Label("contactid", String.valueOf(contact.getId())));
				item.add(new Label("firstname", contact.getFirstName()));
				item.add(new Label("lastname", contact.getLastName()));
				item.add(new Label("homephone", contact.getHomePhone()));
				item.add(new Label("cellphone", contact.getCellPhone()));

				item.add(AttributeModifier.replace("class", new AbstractReadOnlyModel<String>()
				{
					private static final long serialVersionUID = 1L;

					@Override
					public String getObject()
					{
						return (item.getIndex() % 2 == 1) ? "even" : "odd";
					}
				}));
			}

			@Override
			protected Item<Contact> newItem(String id, int index, IModel<Contact> model)
			{
				return new HighlitableDataItem<>(id, index, model);
			}
		};

		dataView.setItemsPerPage(8L);
		dataView.setItemReuseStrategy(ReuseIfModelsEqualStrategy.getInstance());

		add(new OrderByBorder("orderByFirstName", "firstName", dp)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSortChanged()
			{
				dataView.setCurrentPage(0);
			}
		});

		add(new OrderByBorder("orderByLastName", "lastName", dp)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSortChanged()
			{
				dataView.setCurrentPage(0);
			}
		});

		add(dataView);
		add(new PagingNavigator("navigator", dataView));
	}
}
