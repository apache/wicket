/*
 * $Id$ $Revision: 460265 $ $Date: 2006-04-16 15:36:52 +0200 (Dim, 16 avr 2006) $
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

import java.util.Iterator;

import wicket.MarkupContainer;
import wicket.extensions.markup.html.repeater.refreshing.Item;
import wicket.extensions.markup.html.repeater.refreshing.OddEvenItem;
import wicket.extensions.markup.html.repeater.refreshing.RefreshingView;
import wicket.extensions.markup.html.repeater.refreshing.ReuseIfModelsEqualStrategy;
import wicket.extensions.markup.html.repeater.util.ModelIteratorAdapter;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.SubmitLink;
import wicket.markup.html.form.TextField;
import wicket.markup.html.link.Link;
import wicket.markup.html.panel.Panel;
import wicket.model.IModel;
import wicket.model.PropertyModel;

/**
 * Page that demonstrates using RefreshingView in a form. The component reuses
 * its items, to allow adding or removing rows without necessarily validating
 * the form, and preserving component state which preserves error messages, etc.
 */
public class FormPage extends BasePage
{
	final Form form;

	/**
	 * constructor
	 */
	public FormPage()
	{
		form = new Form(this, "form");

		// create a repeater that will display the list of contacts.
		RefreshingView<Contact> refreshingView = new RefreshingView<Contact>(form, "simple")
		{
			@Override
			protected Iterator<IModel<Contact>> getItemModels()
			{
				// for simplicity we only show the first 10 contacts
				Iterator<Contact> contacts = DatabaseLocator.getDatabase().find(0, 10, "firstName",
						true).iterator();

				// the iterator returns contact objects, but we need it to
				// return models, we use this handy adapter class to perform
				// on-the-fly conversion.
				return new ModelIteratorAdapter<Contact>(contacts)
				{

					protected IModel<Contact> model(Contact object)
					{
						return new DetachableContactModel(object);
					}

				};

			}

			@SuppressWarnings("unchecked")
			@Override
			protected void populateItem(final Item<Contact> item)
			{
				// populate the row of the repeater
				IModel contact = item.getModel();
				new ActionPanel(item, "actions", contact);
				// FIXME use CompoundPropertyModel!
				new TextField(item, "id", new PropertyModel(contact, "id"));
				new TextField(item, "firstName", new PropertyModel(contact, "firstName"));
				new TextField(item, "lastName", new PropertyModel(contact, "lastName"));
				new TextField(item, "homePhone", new PropertyModel(contact, "homePhone"));
				new TextField(item, "cellPhone", new PropertyModel(contact, "cellPhone"));
			}

			@Override
			protected Item<Contact> newItem(MarkupContainer parent, String id, int index,
					IModel<Contact> model)
			{
				// this item sets markup class attribute to either 'odd' or
				// 'even' for decoration
				return new OddEvenItem<Contact>(parent, id, index, model);
			}

		};

		// because we are in a form we need to preserve state of the component
		// hierarchy (because it might contain things like form errors that
		// would be lost if the hierarchy for each item was recreated every
		// request by default), so we use an item reuse strategy.
		refreshingView.setItemReuseStrategy(ReuseIfModelsEqualStrategy.getInstance());
	}

	/**
	 * Panel that houses row-actions
	 */
	class ActionPanel extends Panel
	{
		/**
		 * @param parent
		 * @param id
		 *            component id
		 * @param model
		 *            model for contact
		 */
		public ActionPanel(MarkupContainer parent, String id, IModel model)
		{
			super(parent, id, model);
			new Link(this, "select")
			{
				@Override
				public void onClick()
				{
					FormPage.this.setSelected((Contact)getParent().getModelObject());
				}
			};

			SubmitLink removeLink = new SubmitLink(this, "remove", form)
			{
				public void onSubmit()
				{
					Contact contact = (Contact)getParent().getModelObject();
					info("Removed contact " + contact);
					DatabaseLocator.getDatabase().delete(contact);
				}
			};
			removeLink.setDefaultFormProcessing(false);
		}
	}
}
