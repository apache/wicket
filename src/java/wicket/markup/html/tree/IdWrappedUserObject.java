/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.markup.html.tree;

import java.io.Serializable;
import java.rmi.server.UID;

/**
 * A wrapper that couples a unique id with a tree node's user object.
 *
 * @author Eelco Hillenius
 */
public final class IdWrappedUserObject implements Serializable
{ // TODO finalize javadoc
	/** the wrapped tree node's user object. */
	private final Serializable userObject;

	/** the unique id. */
	private final UID uid;

	/**
	 * Construct.
	 * @param userObject the user object to wrap
	 */
	public IdWrappedUserObject(Serializable userObject)
	{
		this.userObject = userObject;
		uid = new UID();
	}

	/**
	 * Gets uid.
	 * @return uid
	 */
	public UID getUid()
	{
		return uid;
	}

	/**
	 * Gets userObject.
	 * @return userObject
	 */
	public Object getUserObject()
	{
		return userObject;
	}
	
    /**
	 * Compares the specified object with this <code>IdWrappedUserObject</code>
	 * for equality. This method returns <code>true</code> if and only if the
	 * specified object is a <code>IdWrappedUserObject</code> instance with the
	 * same <code>unique id</code> (uid).
	 * @param obj the object to compare this <code>IdWrappedUserObject</code> to
	 * @return <code>true</code> if the given object is equivalent to this one, and
	 *         <code>false</code> otherwise
	 */
	public boolean equals(Object obj)
	{
		if ((obj != null) && (obj instanceof IdWrappedUserObject))
		{
			IdWrappedUserObject wrapper = (IdWrappedUserObject) obj;
			return (wrapper.uid.equals(this.uid));
		}
		else
		{
			return false;
		}
	}

    /**
     * Returns the hash code value for this <code>IdWrappedUserObject</code>.
     * @return	the hash code value for this <code>IdWrappedUserObject</code>
     */
	public int hashCode()
	{
		return uid.hashCode();
	}

    /**
     * Returns a string representation of this <code>IdWrappedUserObject</code>.
     * @return	a string representation of this <code>IdWrappedUserObject</code>
     */
	public String toString()
	{
		return "userObj{id=" + uid.hashCode() + ",obj=" + userObject + "}";
	}
}
