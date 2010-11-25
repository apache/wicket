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
package org.apache.wicket.util.parse.metapattern;

import java.util.regex.Matcher;

/**
 * A Group is a piece of a regular expression (referenced by some Java field or local variable) that
 * forms a "capturing group" within the larger regular expression. A Group is bound to a regular
 * expression MetaPattern when a matcher is retrieved for the pattern by calling one of the
 * matcher() methods. Once bound, a Group cannot be rebound.
 * 
 * @author Jonathan Locke
 */
public class Group extends MetaPattern
{
	private static final long serialVersionUID = 1L;

	/** The capturing group that this Group is bound to. */
	private int group = -1;

	/**
	 * Constructor.
	 * 
	 * @param pattern
	 *            MetaPattern to capture
	 */
	public Group(final MetaPattern pattern)
	{
		super(pattern);
	}

	/**
	 * Threadsafe method to retrieve contents of this captured group.
	 * 
	 * @param matcher
	 *            The matcher from which to retrieve this Group's group
	 * @return The captured characters
	 */
	public final String get(final Matcher matcher)
	{
		if (group == -1)
		{
			throw new GroupNotBoundException();
		}

		return matcher.group(group);
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "(" + super.toString() + ")";
	}

	/**
	 * Binds this capture expression if not already bound.
	 * 
	 * @param bindTo
	 *            The group to bind to
	 * @throws GroupAlreadyBoundException
	 *             Thrown if this Group is already bound
	 */
	final void bind(final int bindTo)
	{
		if (group == -1)
		{
			group = bindTo;
		}
		else
		{
			throw new GroupAlreadyBoundException();
		}
	}
}
