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
package org.apache.wicket.authroles.authorization.strategies.role;

/**
 * Strategy for doing role checking. Normally, an implementation of this strategy interface would
 * look in the current session for credentials that indicate what roles the current user can take
 * on, but any kind of strategy is possible. For example, you could have a role checking strategy
 * that allowed gave users the ADMIN role between 9AM and 5PM.
 * 
 * @author Eelco Hillenius
 * @author Jonathan Locke
 */
@FunctionalInterface
public interface IRoleCheckingStrategy
{
	/**
	 * Whether any of the given roles matches. For example, if a user has role USER and the provided
	 * roles are {USER, ADMIN} this method should return true as the user has at least one of the
	 * roles that were provided.
	 * 
	 * @param roles
	 *            the roles
	 * @return true if a user or whatever subject this implementation wants to work with has at
	 *         least on of the provided roles
	 */
	boolean hasAnyRole(Roles roles);
}
