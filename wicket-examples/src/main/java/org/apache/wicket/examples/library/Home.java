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

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;


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
		final PageableListView<Book> listView;
		add(listView = new PageableListView<Book>("books", new PropertyModel<List<Book>>(this,
			"books"), 4)
		{
			@Override
			public void populateItem(final ListItem<Book> listItem)
			{
				final Book book = listItem.getModelObject();
				listItem.add(BookDetails.link("details", book, getLocalizer().getString(
					"noBookTitle", this)));
				listItem.add(new Label("author", new Model<>(book)));
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
	public List<Book> getBooks()
	{
		// Note: checkAccess() (and thus login etc.) happen after the Page
		// has been instantiated. Thus, you can not realy on user != null.
		// Note2: In any case, all components must be associated with a
		// wicket tag.
		User user = getLibrarySession().getUser();
		if (user == null)
		{
			return new ArrayList<>();
		}

		return user.getBooks();
	}
}
