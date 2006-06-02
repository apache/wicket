/*
 * $Id: ExampleDecoratorLink.java 5838 2006-05-24 20:44:49 +0000 (Wed, 24 May
 * 2006) joco01 $ $Revision$ $Date: 2006-05-24 20:44:49 +0000 (Wed, 24
 * May 2006) $
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
package wicket.examples.displaytag;

import java.util.List;

import wicket.AttributeModifier;
import wicket.PageParameters;
import wicket.examples.displaytag.utils.ListObject;
import wicket.examples.displaytag.utils.SimpleListView;
import wicket.examples.displaytag.utils.TestList;
import wicket.markup.html.basic.Label;
import wicket.markup.html.link.BookmarkablePageLink;
import wicket.markup.html.list.ListItem;
import wicket.markup.html.list.ListView;
import wicket.model.Model;


/**
 * Table may not only print value. This example show how to decorate table
 * values.
 * 
 * @author Juergen Donnerstag
 */
public class ExampleDecoratorLink extends Displaytag
{
	/**
	 * Constructor.
	 * 
	 * @param parameters
	 *            Page parameters
	 */
	public ExampleDecoratorLink(final PageParameters parameters)
	{
		// Test data
		List<ListObject> data = new TestList(10, false);

		// Add the table
		new ListView<ListObject>(this, "rows", data)
		{
			@Override
			public void populateItem(final ListItem listItem)
			{
				final ListObject value = (ListObject)listItem.getModelObject();

				// alternating row styles
				listItem.add(new AttributeModifier("class", new Model<String>(
						(listItem.getIndex() % 2) == 0 ? "even" : "odd")));

				BookmarkablePageLink idLink = new BookmarkablePageLink(listItem, "idLink",
						Page3.class);
				idLink.setParameter("id", value.getId());
				new Label(idLink, "id", Integer.toString(value.getId()));

				BookmarkablePageLink emailLink = new BookmarkablePageLink(listItem, "mailLink",
						Page3.class);
				emailLink.setParameter("id", value.getId());
				new Label(emailLink, "email", value.getEmail());

				BookmarkablePageLink statusLink = new BookmarkablePageLink(listItem, "statusLink",
						Page3.class);
				statusLink.setParameter("id", value.getId());
				new Label(statusLink, "status", value.getStatus());
			}
		};

		// Add table of existing comments
		new SimpleListView(this, "rows2", data)
		{
			@Override
			public void populateItem(final ListItem listItem)
			{
				final ListObject value = (ListObject)listItem.getModelObject();

				BookmarkablePageLink idLink = new BookmarkablePageLink(listItem, "idLink",
						Page3.class);
				idLink.setParameter("id", value.getId());
				new Label(idLink, "id", Integer.toString(value.getId()));

				new BookmarkablePageLink(listItem, "view", Page3.class).setParameter("id",
						value.getId()).setParameter("action", "view");

				new BookmarkablePageLink(listItem, "edit", Page3.class).setParameter("id",
						value.getId()).setParameter("action", "edit");

				new BookmarkablePageLink(listItem, "delete", Page3.class).setParameter("id",
						value.getId()).setParameter("action", "delete");
			}
		};
	}
}