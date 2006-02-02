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
 * Groups a set (technically a list) of actions for authorization.
 * 
 * TODO expand docs and give an example
 * 
 * @author Eelco Hillenius
 */
@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.TYPE })
@Documented
@Inherited
public @interface AuthorizedActions {

	/**
	 * The actions that are allowed.
	 * 
	 * @return the allowed actions
	 */
	AuthorizedAction[] actions();
}
