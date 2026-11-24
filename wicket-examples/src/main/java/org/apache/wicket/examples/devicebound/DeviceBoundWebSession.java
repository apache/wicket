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
package org.apache.wicket.examples.devicebound;

import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.request.Request;

/**
 * Accepts user name and password {@code wicket}.
 */
public class DeviceBoundWebSession extends AuthenticatedWebSession
{
	private static final long serialVersionUID = 1L;

	private static final String USERNAME_PASSWORD = "wicket";

	/**
	 * Constructor.
	 *
	 * @param request
	 *            the request
	 */
	public DeviceBoundWebSession(Request request)
	{
		super(request);
	}

	@Override
	protected boolean authenticate(String username, String password)
	{
		return USERNAME_PASSWORD.equals(username) && USERNAME_PASSWORD.equals(password);
	}

	@Override
	public Roles getRoles()
	{
		return isSignedIn() ? new Roles(Roles.USER) : null;
	}
}
