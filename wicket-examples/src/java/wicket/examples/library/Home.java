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
package wicket.examples.library;

import wicket.PageParameters;
import wicket.markup.html.basic.Label;
import wicket.markup.html.list.ListItem;
import wicket.markup.html.list.ListView;
import wicket.model.Model;
import wicket.model.PropertyModel;


/**
 * Page that displays a list of books and lets the user re-order them.
 * 
 * @author Jonathan Locke
 */
public final class Home extends AuthenticatedWebPage
{
	/**
	 * Constructor
	 * 
	 * @param parameters
	 */
	public Home(final PageParameters parameters)
	{
		// Add table of books
		final User user = getLibrarySession().getUser();
		if (user != null)
		{
			add(new ListView("books", new PropertyModel(user, "books"))
			{
				public void populateItem(final ListItem listItem)
				{
					final Book book = (Book)listItem.getModelObject();
					listItem.add(BookDetails.link("details", book, getLocalizer().getString(
							"noBookTitle", this)));
					listItem.add(new Label("author", new Model(book)));
					listItem.add(moveUpLink("moveUp", listItem));
					listItem.add(moveDownLink("moveDown", listItem));
					listItem.add(removeLink("remove", listItem));
					listItem.add(EditBook.link("edit", book.getId()));
				}
			});
		}
	}
}
