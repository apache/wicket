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
package wicket.authentication;

import wicket.Request;
import wicket.Session;
import wicket.authorization.strategies.role.Roles;
import wicket.protocol.http.WebSession;

/**
 * Basic authenticated web session. Subclasses must provide a method that
 * authenticates the session based on a username and password, and a method
 * implementation that gets the Roles
 * 
 * @author Jonathan Locke
 */
public abstract class AuthenticatedWebSession extends WebSession
{
	/**
	 * @return Current authenticated web session
	 */
	public static AuthenticatedWebSession get()
	{
		return (AuthenticatedWebSession)Session.get();
	}

	/** True when the user is signed in */
	private boolean signedIn;

	/**
	 * Construct.
	 * 
	 * @param application
	 *            The web application
	 * @param request
	 *            The current request object
	 */
	public AuthenticatedWebSession(final AuthenticatedWebApplication application, Request request)
	{
		super(application, request);
	}

	/**
	 * Authenticates this session using the given username and password
	 * 
	 * @param username
	 *            The username
	 * @param password
	 *            The password
	 * @return True if the user was authenticated successfully
	 */
	public abstract boolean authenticate(final String username, final String password);

	/**
	 * @return Get the roles that this session can play
	 */
	public abstract Roles getRoles();

	/**
	 * @return True if the user is signed in to this session
	 */
	public final boolean isSignedIn()
	{
		return signedIn;
	}

	/**
	 * Signs user in by authenticating them with a username and password
	 * 
	 * @param username
	 *            The username
	 * @param password
	 *            The password
	 * @return True if the user was signed in successfully
	 */
	public final boolean signIn(final String username, final String password)
	{
		return signedIn = authenticate(username, password);
	}

	/**
	 * Sign the user out.
	 */
	public void signOut()
	{
		signedIn = false;
	}
}
