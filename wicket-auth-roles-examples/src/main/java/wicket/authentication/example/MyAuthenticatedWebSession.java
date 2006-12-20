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
package wicket.authentication.example;

import wicket.authentication.AuthenticatedWebApplication;
import wicket.authentication.AuthenticatedWebSession;
import wicket.authorization.strategies.role.Roles;

/**
 * Authenticated session subclass
 * 
 * @author Jonathan Locke
 */
public class MyAuthenticatedWebSession extends AuthenticatedWebSession
{
	/**
	 * Construct.
	 * 
	 * @param application
	 *            The application
	 */
	public MyAuthenticatedWebSession(final AuthenticatedWebApplication application)
	{
		super(application);
	}

	/**
	 * @see wicket.authentication.AuthenticatedWebSession#authenticate(java.lang.String,
	 *      java.lang.String)
	 */
	@Override
	public boolean authenticate(final String username, final String password)
	{
		// Check username and password
		return username.equals("wicket") && password.equals("wicket");
	}

	/**
	 * @see wicket.authentication.AuthenticatedWebSession#getRoles()
	 */
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
}
