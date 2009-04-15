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
package org.apache.wicket.authorization;

import org.apache.wicket.util.lang.EnumeratedType;
import org.apache.wicket.util.string.Strings;

/**
 * A class for constructing singleton constants that represent a given component action that needs
 * to be authorized. The Wicket core framework defines Component.RENDER and Component.ENABLE
 * actions, but future versions of the framework may add more actions and user defined components
 * can define their own actions as well.
 * 
 * @see org.apache.wicket.Component#RENDER
 * @see org.apache.wicket.Component#ENABLE
 * 
 * @author Eelco Hillenius
 * @author Jonathan Locke
 * @since 1.2
 */
public class Action extends EnumeratedType
{
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

	private static final long serialVersionUID = -1L;

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
		super(name);
		if (Strings.isEmpty(name))
		{
			throw new IllegalArgumentException(
				"Name argument may not be null, whitespace or the empty string");
		}

		this.name = name;
	}

	/**
	 * @return The name of this action
	 */
	public String getName()
	{
		return name;
	}
}
