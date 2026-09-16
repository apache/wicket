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
 * Groups a set (technically an array) of {@link AuthorizeAction}s for authorization, for when more
 * than one action has to be restricted. A single action does not need this annotation and can be
 * declared with {@link AuthorizeAction} directly. This annotation can be used like this:
 * 
 * <pre>
 * // a panel that users with role ADMIN and USER are allowed to see, but that only
 * // users with role ADMIN are allowed to interact with
 * &#064;AuthorizeActions(actions = {
 * 		&#064;AuthorizeAction(action = &quot;RENDER&quot;, roles = { &quot;ADMIN&quot;, &quot;USER&quot; }),
 * 		&#064;AuthorizeAction(action = &quot;ENABLE&quot;, roles = &quot;ADMIN&quot;) })
 * public class ForAdminsAndUsers extends Panel
 * {
 * 	public ForAdminsAndUsers(String id)
 * 	{
 * 		super(id);
 * 	}
 * }
 * </pre>
 * 
 * It can be placed on a class or on a package, the latter by specifying it in the
 * <code>package-info.java</code> file of that package. Restrictions are resolved per action; see
 * {@link AnnotationsRoleAuthorizationStrategy} for the complete resolution rules.
 * 
 * @see org.apache.wicket.authorization.IAuthorizationStrategy
 * @see AnnotationsRoleAuthorizationStrategy
 * @see AuthorizeAction
 * @see AuthorizeInstantiation
 * @author Eelco Hillenius
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PACKAGE, ElementType.TYPE })
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
