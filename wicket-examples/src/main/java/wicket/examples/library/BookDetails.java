/*
 * $Id: BookDetails.java 5860 2006-05-25 20:29:28 +0000 (Thu, 25 May 2006)
 * eelco12 $ $Revision$ $Date: 2006-05-25 20:29:28 +0000 (Thu, 25 May
 * 2006) $
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

import wicket.AttributeModifier;
import wicket.MarkupContainer;
import wicket.PageParameters;
import wicket.examples.library.Book.WritingStyle;
import wicket.markup.html.basic.Label;
import wicket.markup.html.link.BookmarkablePageLink;
import wicket.model.Model;
import wicket.util.string.StringList;
import wicket.util.string.StringValueConversionException;

/**
 * A book details page. Shows information about a book.
 * 
 * @author Jonathan Locke
 */
public final class BookDetails extends AuthenticatedWebPage
{
	/**
	 * Creates an external page link
	 * 
	 * @param parent
	 *            The parent component
	 * @param name
	 *            The name of the link component to create
	 * @param book
	 *            The book to link to
	 * @param noBookTitle
	 *            The title to show if book is null
	 * @return The external page link
	 */
	public static BookmarkablePageLink link(final MarkupContainer parent, final String name,
			final Book book, final String noBookTitle)
	{
		final BookmarkablePageLink link = new BookmarkablePageLink(parent, name, BookDetails.class);

		if (book != null)
		{
			link.setParameter("id", book.getId());
			new Label(link, "title", new Model<Book>(book));
		}
		else
		{
			new Label(link, "title", noBookTitle);
			link.setEnabled(false);
		}

		return link;
	}

	/**
	 * Constructor
	 * 
	 * @param book
	 *            The model
	 */
	public BookDetails(final Book book)
	{
		new Label(this, "title", book.getTitle());
		new Label(this, "author", book.getAuthor());
		new Label(this, "fiction", Boolean.toString(book.getFiction()));
		BookDetails.link(this, "companion", book.getCompanionBook(), getLocalizer().getString(
				"noBookTitle", this));
		BookDetails.link(this, "related", book.getRelatedBook(), getLocalizer().getString(
				"noBookTitle", this));

		String writingStyles;
		final boolean hasStyles = (book.getWritingStyles() != null)
				&& (book.getWritingStyles().size() > 0);

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

		Label writingStylesLabel = new Label(this, "writingStyles", writingStyles);

		final AttributeModifier italic = new AttributeModifier("class", new Model<String>("italic"));
		italic.setEnabled(!hasStyles);

		writingStylesLabel.add(italic);
		EditBook.link(this, "edit", book.getId());
	}

	/**
	 * Constructor for calls from external page links
	 * 
	 * @param parameters
	 *            Page parameters
	 * @throws StringValueConversionException
	 */
	public BookDetails(final PageParameters parameters) throws StringValueConversionException
	{
		this(Book.get(parameters.getLong("id")));
	}
}
