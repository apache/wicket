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

import org.apache.wicket.Session;
import org.apache.wicket.request.http.WebRequest;

/**
 * The context of one page-less call, handed to its endpoint and authorization. A page-less call
 * has no page and no Ajax request target: it runs outside the page lock, in parallel with other
 * requests of the session.
 *
 * @since 12.0.0
 */
public final class PagelessCall
{
	private final String endpoint;

	private final WebRequest request;

	private final List<String> invalidations = new ArrayList<>();

	PagelessCall(String endpoint, WebRequest request)
	{
		this.endpoint = endpoint;
		this.request = request;
	}

	/**
	 * @return the id the endpoint is registered under
	 */
	public String getEndpoint()
	{
		return endpoint;
	}

	/**
	 * @return the request
	 */
	public WebRequest getRequest()
	{
		return request;
	}

	/**
	 * @return the session of the request
	 */
	public Session getSession()
	{
		return Session.get();
	}

	/**
	 * Creates the exception that rejects this call. Throw it.
	 *
	 * @param messages
	 *            the messages for the client
	 * @return the exception to throw
	 * @see IslandCall#reject(String...)
	 */
	public IslandCallRejectedException reject(String... messages)
	{
		return new IslandCallRejectedException(messages);
	}

	/**
	 * Marks client-side query keys as stale.
	 *
	 * @param keys
	 *            the query keys
	 * @return {@code this}
	 * @see IslandCall#invalidate(String...)
	 */
	public PagelessCall invalidate(String... keys)
	{
		Collections.addAll(invalidations, keys);
		return this;
	}

	List<String> getInvalidations()
	{
		return invalidations;
	}
}
