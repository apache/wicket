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

import wicket.Session;

/**
 * Event obect for passing notifications when attributes are bound to or removed
 * from {@link wicket.Session}s. To listen for such events, listeners need to
 * implement {@link wicket.session.ISessionAttributeListener}.
 * 
 * @author Eelco Hillenius
 */
public class SessionAttributeEvent extends SessionEvent
{
	private static final long serialVersionUID = 1L;

	/** The name to which the object is being bound or unbound. */

	private final String name;

	/** the old value in case this was a replacement. */
	private final Object oldValue;

	/** The object is being bound or unbound. */

	private final Object value;

	/**
	 * Constructs an event that notifies an object that it has been bound to or
	 * unbound from a session. To receive the event, the object must implement
	 * {@link ISessionAttributeListener}.
	 * 
	 * @param source
	 *            the session delegate to which the object is bound or unbound
	 * 
	 * @param name
	 *            the name with which the object is bound or unbound
	 */
	public SessionAttributeEvent(Session source, String name)
	{
		this(source, name, null, null);
	}

	/**
	 * Constructs an event that notifies an object that it has been bound to or
	 * unbound from a session. To receive the event, the object must implement
	 * {@link ISessionAttributeListener}.
	 * 
	 * @param source
	 *            the session delegate to which the object is bound or unbound
	 * 
	 * @param name
	 *            the name with which the object is bound or unbound
	 * @param value
	 *            the value of the attribute that has been added, removed or
	 *            replaced.
	 */
	public SessionAttributeEvent(Session source, String name, Object value)
	{
		this(source, name, value, null);
	}

	/**
	 * Constructs an event that notifies an object that it has been bound to or
	 * unbound from a session. To receive the event, the object must implement
	 * {@link ISessionAttributeListener}.
	 * 
	 * @param source
	 *            the session delegate to which the object is bound or unbound
	 * 
	 * @param name
	 *            the name with which the object is bound or unbound
	 * @param value
	 *            the value of the attribute that has been added, removed or
	 *            replaced.
	 * @param oldValue
	 *            the old value in case this was a replacement
	 */
	public SessionAttributeEvent(Session source, String name, Object value, Object oldValue)
	{
		super(source);
		this.name = name;
		this.value = value;
		this.oldValue = oldValue;
	}

	/**
	 * Returns the name with which the attribute is bound to or unbound from the
	 * session.
	 * 
	 * @return a string specifying the name with which the object is bound to or
	 *         unbound from the session
	 */
	public final String getName()
	{
		return name;
	}

	/**
	 * Returns the value of the attribute that has been added, removed or
	 * replaced. If the attribute was added (or bound), this is the value of the
	 * attribute. If the attrubute was removed (or unbound), this is the value
	 * of the removed attribute. If the attribute was replaced, this is the old
	 * value of the attribute.
	 * 
	 * @return the value
	 */
	public final Object getValue()
	{
		return value;
	}

	/**
	 * Gets the old value in case this was a replacement.
	 * 
	 * @return the old value or null
	 */
	public Object getOldValue()
	{
		return oldValue;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return "SessionBindingEvent{session=" + super.getSource() + "}";
	}
}
