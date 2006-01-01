/*
 * $Id$
 * $Revision$ $Date$
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

import java.io.Serializable;

import wicket.Session;


/**
 * Implementations are notified of attribute change events broadcasted by
 * {@link wicket.Session}.
 * 
 * @author Eelco Hillenius
 */
public interface ISessionAttributeListener extends Serializable
{
	/**
	 * Notification that an attribute is about to be added to a session. Called
	 * before the attribute is added.
	 * 
	 * @param evt
	 *            the notification event
	 */
	public void attributeAdded(SessionAttributeEvent evt);

	/**
	 * Notification that an attribute is about to be removed from a session.
	 * Called before the attribute is removed.
	 * 
	 * @param evt
	 *            the notification event
	 */
	public void attributeRemoved(SessionAttributeEvent evt);

	/**
	 * Notification that an attribute is to be replaced in a session. Called
	 * before the attribute is replaced.
	 * 
	 * @param evt
	 *            the notification event
	 */
	public void attributeReplaced(SessionAttributeEvent evt);
}
