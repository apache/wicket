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
package org.apache.wicket.examples.library;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;


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
		final PageableListView listView;
		add(listView = new PageableListView("books", new PropertyModel(this, "books"), 4)
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
		add(new PagingNavigator("navigator", listView));
	}

	/**
	 * 
	 * @return List of books
	 */
	public List getBooks()
	{
		// Note: checkAccess() (and thus login etc.) happen after the Page
		// has been instantiated. Thus, you can not realy on user != null.
		// Note2: In any case, all components must be associated with a
		// wicket tag.
		User user = getLibrarySession().getUser();
		if (user == null)
		{
			return new ArrayList();
		}

		return user.getBooks();
	}
}
