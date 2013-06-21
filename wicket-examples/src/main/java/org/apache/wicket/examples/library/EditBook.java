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

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.ListMultipleChoice;
import org.apache.wicket.markup.html.form.RadioChoice;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.validation.FormComponentFeedbackBorder;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.util.lang.EnumeratedType;
import org.apache.wicket.validation.validator.StringValidator;


/**
 * A page that contains a form that allows editing of books.
 * 
 * @author Jonathan Locke
 */
public final class EditBook extends AuthenticatedWebPage
{
	static final Book otherBook = new Book("Frisbee Techniques", "Marty van Hoff", Book.FICTION);

	/**
	 * Constructs a page that edits a book
	 * 
	 * @param book
	 *            The book to edit
	 */
	public EditBook(final Book book)
	{
		// Create and add feedback panel to page
		add(new FeedbackPanel("feedback"));

		// Add edit book form to page
		add(new EditBookForm("editBookForm", book));
	}

	/**
	 * Gets a link to a page that will edit a book
	 * 
	 * @param name
	 *            The name of the link
	 * @param id
	 *            The id of the book that the page will edit
	 * @return The page link
	 */
	public static Link<Void> link(final String name, final long id)
	{
		return new Link<Void>(name)
		{
			/**
			 * @see org.apache.wicket.markup.html.link.Link#onClick()
			 */
			@Override
			public void onClick()
			{
				setResponsePage(new EditBook(Book.get(id)));
			}
		};
	}

	/**
	 * Form that edits a book
	 * 
	 * @author Jonathan Locke
	 */
	static public final class EditBookForm extends Form<Book>
	{
		/**
		 * Constructor
		 * 
		 * @param id
		 *            id of form
		 * @param book
		 *            Book model
		 */
		public EditBookForm(final String id, final Book book)
		{
			super(id, new CompoundPropertyModel<>(book));

			// Create a required text field with a max length of 30 characters
			// that edits the book's title
			final TextField<String> title = new TextField<>("title");
			title.setRequired(true);
			title.add(new StringValidator(null, 30));

			final MarkupContainer titleFeedback = new FormComponentFeedbackBorder("titleFeedback");
			add(titleFeedback);
			titleFeedback.add(title);

			// Create a required text field that edits the book's author
			final TextField<String> author = new TextField<>("author");
			author.setRequired(true);
			final MarkupContainer authorFeedback = new FormComponentFeedbackBorder("authorFeedback");
			add(authorFeedback);
			authorFeedback.add(author);

			// Add fiction checkbox
			add(new CheckBox("fiction"));

			// Books is everything but otherBook
			List<Book> books = new ArrayList<>();
			books.addAll(Book.getBooks());
			books.remove(otherBook);

			// Add companion book choice
			add(new DropDownChoice<>("companionBook", books));

			// Add radio choice test
			final RadioChoice<Book> relatedBook = new RadioChoice<>("relatedBook", books);
			add(relatedBook);

			// Multi-select among writing styles
			add(new ListMultipleChoice<EnumeratedType>("writingStyles",
				EnumeratedType.getValues(Book.WritingStyle.class)));
		}

		/**
		 * Show the resulting valid edit
		 */
		@Override
		public final void onSubmit()
		{
			final Book book = getModelObject();
			BookDetails details = new BookDetails(book);

			setResponsePage(details);

			// setRedirect(true);
		}
	}
}
