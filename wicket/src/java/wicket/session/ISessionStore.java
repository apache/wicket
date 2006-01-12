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
package wicket.session;

import java.util.List;

/**
 * The actual store that is used by {@link wicket.Session} to store its
 * attributes.
 * 
 * @author Eelco Hillenius
 */
public interface ISessionStore
{
	/**
	 * Gets the id for this session.
	 * 
	 * @return the id for this session
	 */
	String getId();

	/**
	 * Adds or replaces the attribute with the given name and value.
	 * 
	 * @param name
	 *            the name of the attribute
	 * @param value
	 *            the value of the attribute
	 */
	void setAttribute(String name, Object value);

	/**
	 * Gets the attribute value with the given name
	 * 
	 * @param name
	 *            The name of the attribute to store
	 * @return The value of the attribute
	 */
	Object getAttribute(final String name);

	/**
	 * Removes the attribute with the given name.
	 * 
	 * @param name
	 *            the name of the attribute to remove
	 */
	void removeAttribute(String name);

	/**
	 * @return List of attributes for this session
	 */
	List getAttributeNames();

	/**
	 * Invalidates the session.
	 */
	void invalidate();
}
