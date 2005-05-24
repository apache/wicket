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
package wicket.examples.customcomponents;

import java.io.Serializable;
import java.util.Date;

/**
 * A book.
 * @author Eelco Hillenius
 */
public class Book implements Serializable
{
	private String title = "Action Wicket";

	private String author = "Fritz Fritzl";

	private Date orderPlaced = new Date();

	/**
	 * Construct.
	 */
	public Book()
	{
	}

	/**
	 * Gets the title.
	 * @return title
	 */
	public String getTitle()
	{
		return title;
	}

	/**
	 * Sets the title.
	 * @param title title
	 */
	public void setTitle(String title)
	{
		this.title = title;
	}

	/**
	 * Gets the author.
	 * @return author
	 */
	public String getAuthor()
	{
		return author;
	}

	/**
	 * Sets the author.
	 * @param author author
	 */
	public void setAuthor(String author)
	{
		this.author = author;
	}

	/**
	 * Gets the orderPlaced.
	 * @return orderPlaced
	 */
	public Date getOrderPlaced()
	{
		return orderPlaced;
	}

	/**
	 * Sets the orderPlaced.
	 * @param orderPlaced orderPlaced
	 */
	public void setOrderPlaced(Date orderPlaced)
	{
		this.orderPlaced = orderPlaced;
	}
}
