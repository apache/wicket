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
package org.apache.wicket.examples.compref;

import org.apache.wicket.util.io.IClusterable;

/**
 * A book.
 * 
 * @author Eelco Hillenius
 */
public final class Book implements IClusterable
{
	private String title;
	private String author;
	private String isbn;

	/**
	 * Construct.
	 */
	public Book()
	{
	}

	/**
	 * Gets the author.
	 * 
	 * @return author
	 */
	public final String getAuthor()
	{
		return author;
	}

	/**
	 * Sets the author.
	 * 
	 * @param author
	 *            author
	 */
	public final void setAuthor(String author)
	{
		this.author = author;
	}

	/**
	 * Gets the isbn.
	 * 
	 * @return isbn
	 */
	public final String getIsbn()
	{
		return isbn;
	}

	/**
	 * Sets the isbn.
	 * 
	 * @param isbn
	 *            isbn
	 */
	public final void setIsbn(String isbn)
	{
		this.isbn = isbn;
	}

	/**
	 * Gets the title.
	 * 
	 * @return title
	 */
	public final String getTitle()
	{
		return title;
	}

	/**
	 * Sets the title.
	 * 
	 * @param title
	 *            title
	 */
	public final void setTitle(String title)
	{
		this.title = title;
	}
}
