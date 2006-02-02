/*
 * $Id$ $Revision$ $Date$
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.authorization.roles.annot;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for configuring what roles are allowed for an
 * {@link wicket.authorization.Action} and {@link wicket.Component} combination.
 * 
 * @author Eelco hillenius
 */
@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.PACKAGE, ElementType.TYPE })
@Documented
@Inherited
public @interface AuthorizedRoles {

	/**
	 * Gets the roles that are allowed to take the action.
	 * 
	 * @return the roles that are allowed
	 */
	String[] value() default { "" };
}
