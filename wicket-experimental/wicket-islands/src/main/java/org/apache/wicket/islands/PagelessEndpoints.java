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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.apache.wicket.util.lang.Args;

/**
 * The page-less endpoints of an application. An island makes one callable by its client-side peer
 * with {@code usePageless}.
 *
 * <pre>
 * IslandSettings.install(this).getPagelessEndpoints().register("scores", ScoreQuery.class,
 * 	new ScoresEndpoint(highScores), IPagelessAuthorization.PERMIT_ALL);
 * </pre>
 *
 * @since 12.0.0
 */
public final class PagelessEndpoints
{
	private static final Pattern ID_PATTERN = Pattern.compile("[A-Za-z0-9_-]+");

	record Registration(Class<?> requestType, IPagelessEndpoint<Object, ?> endpoint,
		IPagelessAuthorization authorization)
	{
	}

	private final Map<String, Registration> registrations = new ConcurrentHashMap<>();

	PagelessEndpoints()
	{
	}

	/**
	 * Registers an endpoint.
	 *
	 * @param <Q>
	 *            the request type
	 * @param id
	 *            the id of the endpoint; letters, digits, '-' and '_'
	 * @param requestType
	 *            the request type, {@link Void} for calls without payload
	 * @param endpoint
	 *            the endpoint
	 * @param authorization
	 *            decides which calls may run, {@link IPagelessAuthorization#PERMIT_ALL} for all
	 * @return {@code this}
	 */
	@SuppressWarnings("unchecked")
	public <Q> PagelessEndpoints register(String id, Class<Q> requestType,
		IPagelessEndpoint<Q, ?> endpoint, IPagelessAuthorization authorization)
	{
		Args.notNull(id, "id");
		if (!ID_PATTERN.matcher(id).matches())
		{
			throw new IllegalArgumentException("Invalid endpoint id '" + id +
				"': use letters, digits, '-' and '_'");
		}
		Args.notNull(requestType, "requestType");
		Args.notNull(endpoint, "endpoint");
		Args.notNull(authorization, "authorization");
		registrations.put(id,
			new Registration(requestType, (IPagelessEndpoint<Object, ?>)endpoint, authorization));
		return this;
	}

	Registration get(String id)
	{
		return registrations.get(id);
	}
}
