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

import java.util.Iterator;

import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.OddEvenItem;
import org.apache.wicket.markup.repeater.RefreshingView;
import org.apache.wicket.markup.repeater.ReuseIfModelsEqualStrategy;
import org.apache.wicket.markup.repeater.util.ModelIteratorAdapter;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;


/**
 * Page that demonstrates using RefreshingView in a form. The component reuses its items, to allow
 * adding or removing rows without necessarily validating the form, and preserving component state
 * which preserves error messages, etc.
 */
public class FormPage extends BasePage
{
	final Form<?> form;

	/**
	 * constructor
	 */
	public FormPage()
	{
		form = new Form("form");
		add(form);

		// create a repeater that will display the list of contacts.
		RefreshingView<Contact> refreshingView = new RefreshingView<Contact>("simple")
		{
			@Override
			protected Iterator<IModel<Contact>> getItemModels()
			{
				// for simplicity we only show the first 10 contacts
				SortParam<String> sort = new SortParam<String>("firstName", true);
				Iterator<Contact> contacts = DatabaseLocator.getDatabase()
					.find(0, 10, sort)
					.iterator();

				// the iterator returns contact objects, but we need it to
				// return models, we use this handy adapter class to perform
				// on-the-fly conversion.
				return new ModelIteratorAdapter<Contact>(contacts)
				{

					@Override
					protected IModel<Contact> model(Contact object)
					{
						return new CompoundPropertyModel<Contact>(
							new DetachableContactModel(object));
					}

				};

			}

			@Override
			protected void populateItem(final Item<Contact> item)
			{
				// populate the row of the repeater
				IModel<Contact> contact = item.getModel();
				item.add(new ActionPanel("actions", contact));
				item.add(new TextField<Long>("id"));
				item.add(new TextField<String>("firstName"));
				item.add(new TextField<String>("lastName"));
				item.add(new TextField<String>("homePhone"));
				item.add(new TextField<String>("cellPhone"));
			}

			@Override
			protected Item<Contact> newItem(String id, int index, IModel<Contact> model)
			{
				// this item sets markup class attribute to either 'odd' or
				// 'even' for decoration
				return new OddEvenItem<Contact>(id, index, model);
			}
		};

		// because we are in a form we need to preserve state of the component
		// hierarchy (because it might contain things like form errors that
		// would be lost if the hierarchy for each item was recreated every
		// request by default), so we use an item reuse strategy.
		refreshingView.setItemReuseStrategy(ReuseIfModelsEqualStrategy.getInstance());


		form.add(refreshingView);
	}

	/**
	 * Panel that houses row-actions
	 */
	private class ActionPanel extends Panel
	{
		/**
		 * @param id
		 *            component id
		 * @param model
		 *            model for contact
		 */
		public ActionPanel(String id, IModel<Contact> model)
		{
			super(id, model);
			add(new Link("select")
			{
				@Override
				public void onClick()
				{
					setSelected((Contact)ActionPanel.this.getDefaultModelObject());
				}
			});

			SubmitLink removeLink = new SubmitLink("remove", form)
			{
				@Override
				public void onSubmitBeforeForm()
				{
					Contact contact = (Contact)ActionPanel.this.getDefaultModelObject();
					info("Removed contact " + contact);
					DatabaseLocator.getDatabase().delete(contact);
				}
			};
			removeLink.setDefaultFormProcessing(false);
			add(removeLink);
		}
	}
}
