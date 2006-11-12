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
package wicket.authorization;

import java.io.Serializable;

import wicket.util.string.Strings;

/**
 * A class for constructing singleton constants that represent a given component
 * action that needs to be authorized. The Wicket core framework defines
 * Component.RENDER and Component.ENABLE actions, but future versions of the
 * framework may add more actions and user defined components can define their
 * own actions as well.
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

	/**
	 * RENDER action name (for consistent name and use in annotations).
	 * <p>
	 * DO NOT use for equals on Action, like
	 * 
	 * <pre>
	 * action.equals(Action.RENDER)
	 * </pre>
	 * 
	 * as you'll compare an action with a string. Rather, do:
	 * 
	 * <pre>
	 * action.equals(Component.RENDER)
	 * </pre>
	 * 
	 * </p>
	 */
	public static final String RENDER = "RENDER";

	/**
	 * ENABLE action name (for consistent name and use in annotations).
	 * <p>
	 * DO NOT use for equals on Action, like
	 * 
	 * <pre>
	 * action.equals(Action.ENABLE)
	 * </pre>
	 * 
	 * as you'll compare an action with a string. Rather, do:
	 * 
	 * <pre>
	 * action.equals(Component.ENABLE)
	 * </pre>
	 * 
	 * </p>
	 */
	public static final String ENABLE = "ENABLE";

	/** The name of this action. */
	private final String name;

	/**
	 * Construct.
	 * 
	 * @param name
	 *            The name of this action for debug purposes
	 */
	public Action(final String name)
	{
		if (Strings.isEmpty(name))
		{
			throw new IllegalArgumentException(
					"Name argument may not be null, whitespace or the empty string");
		}

		this.name = name;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj)
	{
		if (obj instanceof Action)
		{
			final Action that = (Action)obj;
			return name.equals(that.name);
		}
		return false;
	}

	/**
	 * @return The name of this action
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		int result = "Action".hashCode();
		result += name.hashCode();
		return 17 * result;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return name;
	}
}
