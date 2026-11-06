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

import java.io.Serializable;

/**
 * Keeps the context of page-less calls behind handles. An island issues a handle when it renders;
 * the client sends it with each call, and the endpoint reads the context with
 * {@link PagelessCall#getHandle(Class)}. The client cannot forge a context this way: it only ever
 * holds the handle.
 * <p>
 * A handle must be unguessable and must only resolve in the session that it was issued in, for
 * the endpoint it was issued for. It is still a token: it can leak or be replayed within its
 * session. <strong>A handle is not an authorization mechanism</strong>; an endpoint checks
 * whether the session may access the context on its own terms.
 * <p>
 * Handles are issued while a page renders and resolved by page-less calls, which run in parallel
 * without the page lock, so an implementation must be thread-safe.
 *
 * @see SessionHandleStore
 * @since 12.0.0
 */
public interface IIslandHandleStore
{
	/**
	 * Issues a handle in the current session.
	 *
	 * @param endpointId
	 *            the endpoint the handle is valid for
	 * @param context
	 *            the context the handle stands for
	 * @return the handle
	 */
	String issue(String endpointId, Serializable context);

	/**
	 * Resolves a handle sent by the client in the current session.
	 *
	 * @param endpointId
	 *            the endpoint that was called
	 * @param handle
	 *            the handle, untrusted
	 * @return the context, or {@code null} when the handle is unknown in this session, was issued
	 *         for another endpoint, or is no longer kept
	 */
	Serializable resolve(String endpointId, String handle);
}
