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
package org.apache.wicket.examples.authentication3;

import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.request.Request;


/**
 * Authenticated session subclass. Note that it is derived from AuthenticatedWebSession which is
 * defined in the auth-role module.
 * 
 * @author Jonathan Locke
 */
public class MyAuthenticatedWebSession extends AuthenticatedWebSession
{
	private static final String USERNAME_PASSWORD = "wicket";

	/**
	 * Construct.
	 * 
	 * @param request
	 *            The current request object
	 */
	public MyAuthenticatedWebSession(Request request)
	{
		super(request);
	}

	@Override
	public boolean authenticate(final String username, final String password)
	{
		return USERNAME_PASSWORD.equals(username) && USERNAME_PASSWORD.equals(password);
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
}
