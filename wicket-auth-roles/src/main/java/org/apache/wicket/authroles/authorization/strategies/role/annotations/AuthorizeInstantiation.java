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
 * Annotation for configuring what roles are allowed for instantiation the annotated component or
 * package. This annotation can be used for classes and packages. For classes it can be used like this:
 *
 * <pre>
 *  // only users with role ADMIN are allowed to create instances of this page, whether it is
 *  // either bookmarkable or not
 *  &#064;AuthorizeInstantiation(&quot;ADMIN&quot;)
 *  public class AdminAnnotationsBookmarkablePage extends WebPage
 * </pre>
 *
 * For packages the annotation needs to be specified in the <code>package-info.java</code> file:
 *
 * <pre>
 *  // only users with role ADMIN are allowed to create instances of pages in this package
 *  &#064;AuthorizeInstantiation(&quot;ADMIN&quot;)
 *  package package_name;
 *
 *  import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
 * </pre>
 *
 * An annotation on a class replaces the annotations on its package, rather than adding to them. Since
 * this annotation is {@link Inherited}, that also holds for an annotation inherited from a superclass
 * in another package: such an inherited annotation suppresses the annotation of the subclass' own
 * package. See {@link AnnotationsRoleAuthorizationStrategy} for the complete resolution rules and for
 * the pitfalls of annotating packages.
 *
 * @see org.apache.wicket.authorization.IAuthorizationStrategy
 * @see AnnotationsRoleAuthorizationStrategy
 * @see AuthorizeInstantiations
 * @see AuthorizeActions
 * @see AuthorizeAction
 * @see AuthorizeResource
 *
 * @author Eelco hillenius
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PACKAGE, ElementType.TYPE })
@Documented
@Inherited
public @interface AuthorizeInstantiation {

	/**
	 * Gets the roles that are allowed to take the action.
	 *
	 * @return the roles that are allowed. Returns a zero length array by default, which authorizes
	 *         everybody
	 */
	String[] value() default { };
}
