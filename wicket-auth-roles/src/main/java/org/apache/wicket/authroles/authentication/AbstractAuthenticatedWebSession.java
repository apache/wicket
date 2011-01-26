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
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.request.Request;


/**
 * Authenticated web session. Subclasses must provide a method that gets User authentication status
 * and a method that returns the Roles for current User.
 * 
 * @author Jonathan Locke
 * @author Leonid Bogdanov
 */
public abstract class AbstractAuthenticatedWebSession extends WebSession
{
	private static final long serialVersionUID = 1L;

	/**
	 * @return Current authenticated web session
	 */
	public static AbstractAuthenticatedWebSession get()
	{
		return (AbstractAuthenticatedWebSession)Session.get();
	}

	/**
	 * Construct.
	 * 
	 * @param request
	 *            The current request object
	 */
	public AbstractAuthenticatedWebSession(final Request request)
	{
		super(request);
	}

	/**
	 * @return Get the roles that this session can play
	 */
	public abstract Roles getRoles();

	/**
	 * @return True if the user is signed in to this session
	 */
	public abstract boolean isSignedIn();
}
