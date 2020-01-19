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

import java.util.Arrays;
import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.Session;
import org.apache.wicket.authroles.authorization.strategies.role.RoleAuthorizationStrategy;
import org.apache.wicket.authroles.authorization.strategies.role.metadata.MetaDataRoleAuthorizationStrategy;
import org.apache.wicket.examples.WicketExampleApplication;
import org.apache.wicket.examples.authorization.pages.AdminBookmarkablePage;
import org.apache.wicket.examples.authorization.pages.AdminInternalPage;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;


/**
 * Application object for this example.
 * 
 * @author Eelco Hillenius
 */
public class RolesApplication extends WicketExampleApplication
{
	/**
	 * User DB.
	 */
	public static List<User> USERS = Arrays.asList(new User("jon", "ADMIN"),
		new User("kay", "USER"), new User("pam", ""));

	/**
	 * Construct.
	 */
	public RolesApplication()
	{
		super();
	}

	/**
	 * @see org.apache.wicket.Application#getHomePage()
	 */
	@Override
	public Class<? extends Page> getHomePage()
	{
		return Index.class;
	}

	/**
	 * @see org.apache.wicket.protocol.http.WebApplication#newSession(org.apache.wicket.request.Request,
	 *      org.apache.wicket.request.Response)
	 */
	@Override
	public Session newSession(Request request, Response response)
	{
		return new RolesSession(request);
	}

	@Override
	protected void init()
	{
		super.init();

		getDebugSettings().setDevelopmentUtilitiesEnabled(true);

		getSecuritySettings().setAuthorizationStrategy(
			new RoleAuthorizationStrategy(new UserRolesAuthorizer()));
		MetaDataRoleAuthorizationStrategy.authorize(AdminBookmarkablePage.class, "ADMIN");
		MetaDataRoleAuthorizationStrategy.authorize(AdminInternalPage.class, "ADMIN");
	}

}
