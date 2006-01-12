/*
 * $Id$ $Revision$
 * $Date$
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
package wicket.authorization;

import java.io.Serializable;

/**
 * A token/key that represents a given component action that needs to be
 * authorized. For example, Component.RENDER or Component.ENABLE.
 * 
 * @see wicket.Component#RENDER
 * @see wicket.Component#ENABLE
 * 
 * @author Eelco Hillenius
 * @author Jonathan Locke
 * @since 1.2
 */
public class Action implements Serializable
{
	private static final long serialVersionUID = -1L;

	/** The name of this action. */
	private final String name;

	/**
	 * Construct.
	 * 
	 * @param name
	 *            The name of this action for debug purposes
	 */
	public Action(String name)
	{
		if (name == null)
		{
			throw new IllegalArgumentException("Name argument may not be null");
		}

		this.name = name;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj)
	{
		if (obj instanceof Action)
		{
			Action that = (Action)obj;
			return name.equals(that.name);
		}
		return false;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode()
	{
		int result = "Action".hashCode();
		result += name.hashCode();
		return 17 * result;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return name;
	}
}
