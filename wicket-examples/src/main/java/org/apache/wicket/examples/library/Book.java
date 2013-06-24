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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.util.io.IClusterable;
import org.apache.wicket.util.lang.EnumeratedType;


/**
 * An example POJO model.
 * 
 * @author Jonathan Locke
 */
public final class Book implements IClusterable
{
	/** Value for fiction books. */
	public static final boolean FICTION = true;

	/** Value for non-fiction books. */
	public static final boolean NON_FICTION = false;

	/** Funny book */
	public static final WritingStyle FUNNY = new WritingStyle("funny");

	/** Boring book */
	public static final WritingStyle BORING = new WritingStyle("boring");

	/** Sad book */
	public static final WritingStyle SAD = new WritingStyle("sad");

	/** Bad book */
	public static final WritingStyle BAD = new WritingStyle("bad");

	private static long nextId = 0;
	private static final Map<Long, Book> idToBook = new HashMap<>();

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
	private List<WritingStyle> writingStyles = new ArrayList<>();

	/**
	 * Constructor
	 * 
	 * @param title
	 *            Book title
	 * @param author
	 *            The author of the book
	 * @param isFiction
	 *            True (FICTION) if the book is fiction, false (NON_FICTION) if it is not.
	 */
	public Book(final String title, final String author, final boolean isFiction)
	{
		id = nextId++;
		this.title = title;
		this.author = author;
		this.isFiction = isFiction;

		add(this);
	}

	/**
	 * 
	 * @param book
	 */
	private void add(final Book book)
	{
		boolean hit = false;
		for (Book value : idToBook.values())
		{
			if (value.toString().equals(book.toString()))
			{
				book.id = value.id;
				hit = true;
				break;
			}
		}

		if (hit == false)
		{
			idToBook.put(book.id, book);
		}
	}

	/**
	 * @param id
	 *            Book id
	 * @return Book for id
	 */
	public static Book get(final long id)
	{
		return idToBook.get(id);
	}

	/**
	 * @return All books
	 */
	public static Collection<Book> getBooks()
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
	 * @param id
	 *            New id
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

	/**
	 * @return A book that makes a good companion to this one
	 */
	public final Book getCompanionBook()
	{
		return companionBook;
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
	 * @return True if this book is fiction
	 */
	public final boolean getFiction()
	{
		return isFiction;
	}

	/**
	 * @return Returns the writingStyles.
	 */
	public final List<WritingStyle> getWritingStyles()
	{
		return writingStyles;
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
	 * @return Returns the relatedBook.
	 */
	public final Book getRelatedBook()
	{
		return relatedBook;
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
	 * @see java.lang.Object#toString()
	 */
	@Override
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
