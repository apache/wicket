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
 * Annotation for configuring what roles are allowed for requesting the annotated resource. It works analogously
 * to {@link org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation}.
 *
 * @author Carl-Eric Menzel
 * @see org.apache.wicket.authorization.IAuthorizationStrategy
 * @see org.apache.wicket.authorization.strategies.role.annotations.AnnotationsRoleAuthorizationStrategy
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PACKAGE, ElementType.TYPE})
@Documented
@Inherited
public @interface AuthorizeResource
{

	/**
	 * Gets the roles that are allowed to take the action.
	 *
	 * @return the roles that are allowed. Returns a zero length array by default
	 */
	String[] value() default {};
}
