/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.examples.library;


import java.io.Serializable;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import wicket.util.lang.EnumeratedType;

/**
 * An example POJO model.
 * @author Jonathan Locke
 */
public final class Book implements Serializable
{
    /**
     * Value for fiction books.
     */
    public static final boolean FICTION = true;

    /**
     * Value for non-fiction books.
     */
    public static final boolean NON_FICTION = false;
    public static final WritingStyle FUNNY = new WritingStyle("funny");
    public static final WritingStyle BORING = new WritingStyle("boring");
    public static final WritingStyle SAD = new WritingStyle("sad");
    public static final WritingStyle BAD = new WritingStyle("bad");
    private static long nextId = 0;
    private static final Map idToBook = new HashMap();

    static
    {
        new Book("Cat in Hat", "Dr. Seuss", Book.FICTION);
        new Book("That is Highly Illogical", "Dr. Spock", Book.NON_FICTION);
        new Book("Where's my Tardis, dude?", "Dr. Who", Book.FICTION);
    }

    private long id;
    private String title;
    private String author;
    private Book companionBook;
    private Book relatedBook;
    private boolean isFiction;
    private List writingStyles;

    /**
     * Constructor
     * @param title Book title
     * @param author The author of the book
     * @param isFiction True (FICTION) if the book is fiction, false (NON_FICTION)
     * if it is not.
     */
    public Book(final String title, final String author, final boolean isFiction)
    {
        this.id = nextId++;
        this.title = title;
        this.author = author;
        this.isFiction = isFiction;
        idToBook.put(new Long(id), this);
    }

    /**
     * @param id Book id
     * @return Book for id
     */
    public static Book get(final long id)
    {
        return (Book) idToBook.get(new Long(id));
    }

    /**
     * @return All books
     */
    public static Collection getBooks()
    {
        return idToBook.values();
    }

    /**
     * @return Book id
     */
    public final long getId()
    {
        return id;
    }

    /**
     * @param id New id
     */
    public final void setId(final long id)
    {
        this.id = id;
    }

    /**
     * @return The author
     */
    public final String getAuthor()
    {
        return author;
    }

    /**
     * @return The title
     */
    public final String getTitle()
    {
        return title;
    }

    /**
     * @param string
     */
    public final void setAuthor(final String string)
    {
        author = string;
    }

    /**
     * @param string
     */
    public final void setTitle(final String string)
    {
        title = string;
    }

    public final Book getCompanionBook()
    {
        return companionBook;
    }

    public final void setCompanionBook(final Book book)
    {
        companionBook = book;
    }

    public final void setFiction(final boolean isFiction)
    {
        this.isFiction = isFiction;
    }

    public final boolean getFiction()
    {
        return isFiction;
    }

    /**
     * @return Returns the writingStyles.
     */
    public final List getWritingStyles()
    {
        return writingStyles;
    }

    /**
     * @param writingStyles The writingStyles to set.
     */
    public final void setWritingStyles(final List writingStyles)
    {
        this.writingStyles = writingStyles;
    }

    /**
     * @return Returns the relatedBook.
     */
    public final Book getRelatedBook()
    {
        return relatedBook;
    }

    /**
     * @param relatedBook The relatedBook to set.
     */
    public final void setRelatedBook(final Book relatedBook)
    {
        this.relatedBook = relatedBook;
    }

    public final String toString()
    {
        return title + " (" + author + ")";
    }

    /**
     * Typesafe enumeration for writing styles
     */
    public static final class WritingStyle extends EnumeratedType
    {
        WritingStyle(final String name)
        {
            super(name);
        }
    }
}

///////////////////////////////// End of File /////////////////////////////////
