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


import java.util.List;

import wicket.Session;

/**
 * Simple authenticator class is associated with sessions and holds a User
 * object for any signed in user.
 *
 * @author Jonathan Locke
 */
public class Authenticator
{
    private User user;

    /**
     * Force use of static getter method
     */
    private Authenticator()
    {
    }

    /**
     * Gets Authenticator for session.  If no authenticator exists for the session,
     * one is created and attached to the session.
     * @param session
     * @return Authenticator for session
     */
    public static Authenticator forSession(final Session session)
    {
        final String key = "authenticator";
        Authenticator authenticator = (Authenticator) session.getProperty(key);

        if (authenticator == null)
        {
            session.setProperty(key, authenticator = new Authenticator());
        }

        return authenticator;
    }

    /**
     * Checks the given username and password, returning a User object if
     * if the username and password identify a valid user.
     * @param username The username
     * @param password The password
     * @return The signed in user
     */
    public final User authenticate(final String username, final String password)
    {
        if (user == null)
        {
            // Trivial password "db"
            if ("jonathan".equalsIgnoreCase(username)
                && "password".equalsIgnoreCase(password))
            {
                // Create User object
                final User user = new User();

                user.setName(username);

                final List books = user.getBooks();

                books.add(new Book("Effective Java", "Joshua Bloch",
                        Book.NON_FICTION));
                books.add(new Book("The Illiad", "Homer Simpson", Book.FICTION));
                books.add(new Book("Why Stock Markets Crash",
                        "Didier Sornette", Book.NON_FICTION));
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
     * @param user New user
     */
    public void setUser(final User user)
    {
        this.user = user;
    }
}


