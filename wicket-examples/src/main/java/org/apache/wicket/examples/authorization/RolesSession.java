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
package org.apache.wicket.examples.authorization;

import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.request.Request;

/**
 * Web Session for this example.
 * 
 * @author Eelco Hillenius
 */
public class RolesSession extends WebSession
{
	/** the current user. */
	private User user = RolesApplication.USERS.get(0);

	/**
	 * Construct.
	 * 
	 * @param request
	 */
	public RolesSession(Request request)
	{
		super(request);
	}

	/**
	 * Gets user.
	 * 
	 * @return user
	 */
	public User getUser()
	{
		return user;
	}

	/**
	 * Sets user.
	 * 
	 * @param user
	 *            user
	 */
	public void setUser(User user)
	{
		this.user = user;
	}

}
