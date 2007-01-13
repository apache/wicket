/*
 * $Id: SignIn2Session.java 460265 2006-04-16 15:36:52 +0200 (Sun, 16 Apr 2006)
 * jdonnerstag $ $Revision$ $Date: 2006-04-16 15:36:52 +0200 (Sun, 16 Apr
 * 2006) $
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.examples.signin2;

import wicket.Request;
import wicket.protocol.http.WebApplication;
import wicket.protocol.http.WebSession;

/**
 * Session class for signin example. Holds and authenticates users.
 * 
 * @author Jonathan Locke
 */
public final class SignIn2Session extends WebSession
{
	/** Trivial user representation */
	private String user;

	/**
	 * Constructor
	 * 
	 * @param application
	 *            The application
	 * @param request
	 *            The current request object
	 */
	protected SignIn2Session(final WebApplication application, Request request)
	{
		super(application, request);
	}

	/**
	 * Checks the given username and password, returning a User object if if the
	 * username and password identify a valid user.
	 * 
	 * @param username
	 *            The username
	 * @param password
	 *            The password
	 * @return True if the user was authenticated
	 */
	public final boolean authenticate(final String username, final String password)
	{
		if (user == null)
		{
			// Trivial password "db"
			if ("wicket".equalsIgnoreCase(username) && "wicket".equalsIgnoreCase(password))
			{
				user = username;
			}
		}

		return user != null;
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
	public String getUser()
	{
		return user;
	}

	/**
	 * @param user
	 *            New user
	 */
	public void setUser(final String user)
	{
		this.user = user;
	}
}
