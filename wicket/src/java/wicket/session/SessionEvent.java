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

import java.util.EventObject;

import wicket.Session;

/**
 * Event obect for passing notifications that are cast by {@link wicket.Session}s.
 * To listen for such events, listeners need to implement
 * {@link wicket.session.ISessionAttributeListener}.
 * 
 * @author Eelco Hillenius
 */
public class SessionEvent extends EventObject
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @param source
	 *            the event caster
	 */
	public SessionEvent(Session source)
	{
		super(source);
	}

	/**
	 * Gets the source object casted to {@link Session}.
	 * 
	 * @return the casted source object
	 */
	public final Session getSession()
	{
		return (Session)super.getSource();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return "SessionEvent{session=" + super.getSource() + "}";
	}
}
