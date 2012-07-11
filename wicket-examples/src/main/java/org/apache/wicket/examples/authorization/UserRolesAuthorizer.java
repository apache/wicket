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

import org.apache.wicket.Session;
import org.apache.wicket.authroles.authorization.strategies.role.IRoleCheckingStrategy;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;


/**
 * The authorizer we need to provide to the authorization strategy implementation
 * {@link org.apache.wicket.authorization.strategies.role.annotations.AnnotationsRoleAuthorizationStrategy}
 * .
 * 
 * @author Eelco Hillenius
 */
public class UserRolesAuthorizer implements IRoleCheckingStrategy
{

	/**
	 * Construct.
	 */
	public UserRolesAuthorizer()
	{
	}

	/**
	 * @see org.apache.wicket.authorization.strategies.role.IRoleCheckingStrategy#hasAnyRole(Roles)
	 */
	@Override
	public boolean hasAnyRole(Roles roles)
	{
		RolesSession authSession = (RolesSession)Session.get();
		return authSession.getUser().hasAnyRole(roles);
	}

}
