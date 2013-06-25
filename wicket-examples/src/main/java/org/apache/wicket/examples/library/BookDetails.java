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


import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.examples.library.Book.WritingStyle;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringList;
import org.apache.wicket.util.string.StringValueConversionException;


/**
 * A book details page. Shows information about a book.
 * 
 * @author Jonathan Locke
 */
public final class BookDetails extends AuthenticatedWebPage
{
	/**
	 * Constructor for calls from external page links
	 * 
	 * @param parameters
	 *            Page parameters
	 * @throws StringValueConversionException
	 */
	public BookDetails(final PageParameters parameters) throws StringValueConversionException
	{
		this(Book.get(parameters.get("id").toLong()));
	}

	/**
	 * Constructor
	 * 
	 * @param book
	 *            The model
	 */
	public BookDetails(final Book book)
	{
		add(new Label("title", book.getTitle()));
		add(new Label("author", book.getAuthor()));
		add(new Label("fiction", Boolean.toString(book.getFiction())));
		add(BookDetails.link("companion", book.getCompanionBook(),
			getLocalizer().getString("noBookTitle", this)));
		add(BookDetails.link("related", book.getRelatedBook(),
			getLocalizer().getString("noBookTitle", this)));

		String writingStyles;
		final boolean hasStyles = (book.getWritingStyles() != null) &&
			(book.getWritingStyles().size() > 0);

		if (hasStyles)
		{
			StringList styles = new StringList();

			for (WritingStyle style : book.getWritingStyles())
			{
				styles.add(getLocalizer().getString(style.toString(), this));
			}

			writingStyles = styles.toString();
		}
		else
		{
			writingStyles = getLocalizer().getString("noWritingStyles", this);
		}

		Label writingStylesLabel = new Label("writingStyles", writingStyles);

		final AttributeModifier italic = new AttributeModifier("class", new Model<String>("italic"))
		{
			@Override
			public boolean isEnabled(Component component)
			{
				return !hasStyles;
			}
		};

		add(writingStylesLabel.add(italic));
		add(EditBook.link("edit", book.getId()));
	}

	/**
	 * Creates an external page link
	 * 
	 * @param name
	 *            The name of the link component to create
	 * @param book
	 *            The book to link to
	 * @param noBookTitle
	 *            The title to show if book is null
	 * @return The external page link
	 */
	public static BookmarkablePageLink<Void> link(final String name, final Book book,
		final String noBookTitle)
	{
		final BookmarkablePageLink<Void> link = new BookmarkablePageLink<Void>(name,
			BookDetails.class);

		if (book != null)
		{
			link.getPageParameters().add("id", book.getId());
			link.add(new Label("title", new Model<>(book)));
		}
		else
		{
			link.add(new Label("title", noBookTitle));
			link.setEnabled(false);
		}

		return link;
	}
}
