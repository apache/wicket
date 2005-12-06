/*
 * $Id$ $Revision:
 * 1.1 $ $Date$
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
package wicket.request;

/**
 * Defines the contract for mounting classes to string aliases.
 * 
 * @author Eelco Hillenius
 */
public interface IClassAliasMounter
{
	/**
	 * Mounts a class with the given alias.
	 * 
	 * @param alias
	 *            the alias to mount the class with
	 * @param c
	 *            the class to mount
	 */
	void mountClassAlias(String alias, Class c);

	/**
	 * Unmounts an alias.
	 * 
	 * @param alias
	 *            the alias to unmount
	 */
	void unmountClassAlias(String alias);

	/**
	 * Gets the class that was registered with the given alias.
	 * 
	 * @param alias
	 *            the alias
	 * @return the class or null if nothing was mounted with the given alias
	 */
	Class classForAlias(String alias);

	/**
	 * Gets the alias that the provided class was registered with.
	 * 
	 * @param c
	 *            the class
	 * @return the alias that the provided class was registered with
	 */
	String aliasForClass(Class c);
}
