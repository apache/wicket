/*
 * $Id$ $Revision:
 * 3156 $ $Date$
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

import java.util.List;

import wicket.protocol.http.WebApplication;
import wicket.protocol.http.WebSession;

/**
 * Session class for library example. Holds User object and authenticates users.
 * 
 * @author Jonathan Locke
 */
public final class LibrarySession extends WebSession
{
	private User user;

	/**
	 * Constructor
	 * 
	 * @param application
	 *            The application
	 */
	protected LibrarySession(final WebApplication application)
	{
		super(application);
	}

	/**
	 * Checks the given username and password, returning a User object if if the
	 * username and password identify a valid user.
	 * 
	 * @param username
	 *            The username
	 * @param password
	 *            The password
	 * @return The signed in user
	 */
	public final User authenticate(final String username, final String password)
	{
		if (user == null)
		{
			// Trivial password "db"
			if ("wicket".equalsIgnoreCase(username) && "wicket".equalsIgnoreCase(password))
			{
				// Create User object
				final User user = new User();

				user.setName(username);

				final List books = user.getBooks();

				books.add(new Book("Effective Java", "Joshua Bloch", Book.NON_FICTION));
				books.add(new Book("The Illiad", "Homer Simpson", Book.FICTION));
				books.add(new Book("Why Stock Markets Crash", "Didier Sornette", Book.NON_FICTION));
				books.add(new Book("The Netherlands", "Mike Jones", Book.NON_FICTION));
				books.add(new Book("Windows, Windows, Windows!", "Steve Ballmer", Book.FICTION));
				books.add(new Book("This is a test", "Vincent Rumsfield", Book.FICTION));
				books.add(new Book("Movies", "Mark Marksfield", Book.NON_FICTION));
				books.add(new Book("DOS Capitol", "Billy G", Book.FICTION));
				books.add(new Book("Whatever", "Jonny Zoom", Book.FICTION));
				books.add(new Book("Tooty Fruity", "Rudy O", Book.FICTION));
				setUser(user);
			}
		}

		return user;
	}

	/**
	 * @return True if user is signed in
	 */
	public boolean isSignedIn()
	{
		return user != null;
	}

	/**
	 * @return User
	 */
	public User getUser()
	{
		return user;
	}

	/**
	 * @param user
	 *            New user
	 */
	public void setUser(final User user)
	{
		this.user = user;
	}
}
