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
package org.apache.wicket.authroles.authorization.strategies.role.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Groups a set (technically an array) of {@link AuthorizeAction}s for authorization. This
 * annotation works on a class level, and can be used like this:
 * 
 * <pre>
 * // A panel that is only visible for users with role ADMIN
 * &#064;AuthorizeAction(action = &quot;RENDER&quot;, roles = { &quot;ADMIN&quot;, &quot;USER&quot; })
 * public class ForAdminsAndUsers extends Panel
 * {
 * 	public ForAdminsAndUsers(String id)
 * 	{
 * 		super(id);
 * 	}
 * }
 * </pre>
 * 
 * @see org.apache.wicket.authorization.IAuthorizationStrategy
 * @see AnnotationsRoleAuthorizationStrategy
 * @see AuthorizeAction
 * @see AuthorizeInstantiation
 * @author Eelco Hillenius
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@Documented
@Inherited
public @interface AuthorizeActions {

	/**
	 * The actions that are allowed.
	 * 
	 * @return the allowed actions
	 */
	AuthorizeAction[] actions();
}
