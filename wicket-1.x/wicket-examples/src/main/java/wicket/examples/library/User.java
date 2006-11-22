/*
 * $Id$ $Revision$ $Date:
 * 2006-01-04 10:43:32 +0100 (Mi, 04 Jan 2006) $
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
import java.util.List;

/**
 * Trivial user model for example application
 * 
 * @author Jonathan Locke
 */
public final class User implements Serializable
{
	// The user's name
	private String name;

	// The user's personal book list
	private List books = new ArrayList();

	/**
	 * @return User name
	 */
	public final String getName()
	{
		return name;
	}

	/**
	 * @param string
	 *            User name
	 */
	public final void setName(final String string)
	{
		name = string;
	}

	/**
	 * @return User's book list
	 */
	public final List getBooks()
	{
		return books;
	}

	/**
	 * @param books
	 *            New book list
	 */
	public void setBooks(final List books)
	{
		this.books = books;
	}
}
