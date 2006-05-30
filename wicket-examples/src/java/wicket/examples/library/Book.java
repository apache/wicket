/*
 * $Id$
 * $Revision$ $Date$
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


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * An example POJO model.
 * 
 * @author Jonathan Locke
 */
public final class Book implements Serializable
{
	/**
	 * Typesafe enumeration for writing styles
	 */
	public static enum WritingStyle {
		/** Bad book. */
		BAD,

		/** Boring book. */
		BORING,

		/** Funny book. */
		FUNNY,

		/** Sad book. */
		SAD
	}

	/**
	 * Value for fiction books.
	 */
	public static final boolean FICTION = true;

	/**
	 * Value for non-fiction books.
	 */
	public static final boolean NON_FICTION = false;

	private static final Map<Long, Book> idToBook = new HashMap<Long, Book>();

	private static long nextId = 0;

	static
	{
		new Book("Cat in Hat", "Dr. Seuss", Book.FICTION);
		new Book("That is Highly Illogical", "Dr. Spock", Book.NON_FICTION);
		new Book("Where's my Tardis, dude?", "Dr. Who", Book.FICTION);
	}

	/**
	 * @param id
	 *            Book id
	 * @return Book for id
	 */
	public static Book get(final long id)
	{
		return idToBook.get(new Long(id));
	}

	/**
	 * @return All books
	 */
	public static Collection<Book> getBooks()
	{
		return idToBook.values();
	}

	private String author;
	private Book companionBook;
	private long id;
	private boolean isFiction;

	private Book relatedBook;

	private String title;

	private List<WritingStyle> writingStyles = new ArrayList<WritingStyle>();

	/**
	 * Constructor
	 * 
	 * @param title
	 *            Book title
	 * @param author
	 *            The author of the book
	 * @param isFiction
	 *            True (FICTION) if the book is fiction, false (NON_FICTION) if
	 *            it is not.
	 */
	public Book(final String title, final String author, final boolean isFiction)
	{
		this.id = nextId++;
		this.title = title;
		this.author = author;
		this.isFiction = isFiction;

		add(this);
	}

	/**
	 * @return The author
	 */
	public final String getAuthor()
	{
		return author;
	}

	/**
	 * @return A book that makes a good companion to this one
	 */
	public final Book getCompanionBook()
	{
		return companionBook;
	}

	/**
	 * @return True if this book is fiction
	 */
	public final boolean getFiction()
	{
		return isFiction;
	}

	/**
	 * @return Book id
	 */
	public final long getId()
	{
		return id;
	}

	/**
	 * @return Returns the relatedBook.
	 */
	public final Book getRelatedBook()
	{
		return relatedBook;
	}

	/**
	 * @return The title
	 */
	public final String getTitle()
	{
		return title;
	}

	/**
	 * @return Returns the writingStyles.
	 */
	public final List<WritingStyle> getWritingStyles()
	{
		return writingStyles;
	}

	/**
	 * @param string
	 */
	public final void setAuthor(final String string)
	{
		author = string;
	}

	/**
	 * @param book
	 *            A book that makes a good companion to this one
	 */
	public final void setCompanionBook(final Book book)
	{
		companionBook = book;
	}

	/**
	 * @param isFiction
	 *            True if this book is fiction
	 */
	public final void setFiction(final boolean isFiction)
	{
		this.isFiction = isFiction;
	}

	/**
	 * @param id
	 *            New id
	 */
	public final void setId(final long id)
	{
		this.id = id;
	}

	/**
	 * @param relatedBook
	 *            The relatedBook to set.
	 */
	public final void setRelatedBook(final Book relatedBook)
	{
		this.relatedBook = relatedBook;
	}

	/**
	 * @param string
	 */
	public final void setTitle(final String string)
	{
		title = string;
	}

	/**
	 * @param writingStyles
	 *            The writingStyles to set.
	 */
	public final void setWritingStyles(final List<WritingStyle> writingStyles)
	{
		this.writingStyles = writingStyles;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public final String toString()
	{
		return title + " (" + author + ")";
	}

	private void add(final Book book)
	{
		boolean hit = false;
		final Iterator iter = idToBook.values().iterator();
		while (iter.hasNext())
		{
			final Book value = (Book)iter.next();
			if (value.toString().equals(book.toString()))
			{
				book.id = value.id;
				hit = true;
				break;
			}
		}

		if (hit == false)
		{
			idToBook.put(new Long(book.id), book);
		}
	}
}
