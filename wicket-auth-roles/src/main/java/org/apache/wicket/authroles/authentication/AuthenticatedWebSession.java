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
package org.apache.wicket.authroles.authentication;

import org.apache.wicket.Session;
import org.apache.wicket.request.Request;


/**
 * Basic authenticated web session. Subclasses must provide a method that authenticates the session
 * based on a username and password, and a method implementation that gets the Roles
 * 
 * @author Jonathan Locke
 */
public abstract class AuthenticatedWebSession extends AbstractAuthenticatedWebSession
{
	private static final long serialVersionUID = 1L;

	/**
	 * @return Current authenticated web session
	 */
	public static AuthenticatedWebSession get()
	{
		return (AuthenticatedWebSession)Session.get();
	}

	/** True when the user is signed in */
	private volatile boolean signedIn;

	/**
	 * Construct.
	 * 
	 * @param request
	 *            The current request object
	 */
	public AuthenticatedWebSession(Request request)
	{
		super(request);
	}

	/**
	 * Try to logon the user. It'll call {@link #authenticate(String, String)} to do the real work
	 * and that is what you need to subclass to provide your own authentication mechanism.
	 * 
	 * @param username
	 * @param password
	 * @return true, if logon was successful
	 */
	public final boolean signIn(final String username, final String password)
	{
		signedIn = authenticate(username, password);
		if (signedIn)
		{
			bind();
		}
		return signedIn;
	}

	/**
	 * Actual authentication check, has to be implemented by subclasses.
	 * 
	 * @param username
	 *            The username
	 * @param password
	 *            The password
	 * @return True if the user was authenticated successfully
	 */
	protected abstract boolean authenticate(final String username, final String password);

	/**
	 * Cookie based logins (remember me) may not rely on putting username and password into the
	 * cookie but something else that safely identifies the user. This method is meant to support
	 * these use cases.
	 * 
	 * It is protected (and not public) to enforce that cookie based authentication gets implemented
	 * in a subclass (like you need to implement {@link #authenticate(String, String)} for 'normal'
	 * authentication).
	 * 
	 * @see #authenticate(String, String)
	 * 
	 * @param value
	 */
	protected final void signIn(boolean value)
	{
		signedIn = value;
	}

	/**
	 * @return true, if user is signed in
	 */
	@Override
	public final boolean isSignedIn()
	{
		return signedIn;
	}

	/**
	 * Sign the user out.
	 */
	public void signOut()
	{
		signedIn = false;
	}

	/**
	 * Call signOut() and remove the logon data from where ever they have been persisted (e.g.
	 * Cookies)
	 */
	@Override
	public void invalidate()
	{
		signOut();
		super.invalidate();
	}
}
