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
package org.apache.wicket.examples.authentication2;

import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.request.Request;

/**
 * Session class for signin example. Holds and authenticates users.
 * 
 * @author Jonathan Locke
 */
public final class SignIn2Session extends AuthenticatedWebSession
{
	/** Trivial user representation */
	private String user;

	/**
	 * Constructor
	 * 
	 * @param request
	 *            The current request object
	 */
	protected SignIn2Session(Request request)
	{
		super(request);

	}

	/**
	 * Checks the given username and password, returning a User object if if the username and
	 * password identify a valid user.
	 * 
	 * @param username
	 *            The username
	 * @param password
	 *            The password
	 * @return True if the user was authenticated
	 */
	@Override
	public final boolean authenticate(final String username, final String password)
	{
		final String WICKET = "wicket";

		if (user == null)
		{
			// Trivial password "db"
			if (WICKET.equalsIgnoreCase(username) && WICKET.equalsIgnoreCase(password))
			{
				user = username;
			}
		}

		return user != null;
	}

	@Override
	public Roles getRoles()
	{
		if (isSignedIn())
		{
			// If the user is signed in, they have these roles
			return new Roles(Roles.ADMIN);
		}
		return null;
	}

	/**
	 * @return User
	 */
	public String getUser()
	{
		return user;
	}
}
