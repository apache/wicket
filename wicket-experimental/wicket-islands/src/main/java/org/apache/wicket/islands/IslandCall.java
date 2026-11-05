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
package org.apache.wicket.islands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;

/**
 * The context of one island call, handed to its handler.
 *
 * @since 12.0.0
 */
public final class IslandCall
{
	private final Component component;

	private final AjaxRequestTarget target;

	private final List<String> invalidations = new ArrayList<>();

	IslandCall(Component component, AjaxRequestTarget target)
	{
		this.component = component;
		this.target = target;
	}

	/**
	 * @return the island that was called: an {@link Island} or a {@link FormIsland}
	 */
	public Component getComponent()
	{
		return component;
	}

	/**
	 * The target of the Ajax request the call travels in. Components added to it are updated on the
	 * client before the call's result is delivered, also when the call is rejected.
	 *
	 * @return the Ajax request target
	 */
	public AjaxRequestTarget getTarget()
	{
		return target;
	}

	/**
	 * Creates the exception that rejects this call. Throw it:
	 *
	 * <pre>
	 * throw call.reject("The game is over.");
	 * </pre>
	 *
	 * @param messages
	 *            the messages for the client
	 * @return the exception to throw
	 */
	public IslandCallRejectedException reject(String... messages)
	{
		return new IslandCallRejectedException(messages);
	}

	/**
	 * Marks client-side query keys as stale, so every island on the page that shows data under one
	 * of them fetches it again.
	 *
	 * @param keys
	 *            the query keys
	 * @return {@code this}
	 */
	public IslandCall invalidate(String... keys)
	{
		Collections.addAll(invalidations, keys);
		return this;
	}

	List<String> getInvalidations()
	{
		return invalidations;
	}
}
