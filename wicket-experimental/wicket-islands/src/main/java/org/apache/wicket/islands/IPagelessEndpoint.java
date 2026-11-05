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

/**
 * Handles the calls of a page-less endpoint. An endpoint is registered once with the application
 * and serves every session, so it must be thread-safe and must not hold on to pages or
 * components.
 *
 * @param <Q>
 *            the request type
 * @param <R>
 *            the result type
 * @see PagelessEndpoints
 * @since 12.0.0
 */
@FunctionalInterface
public interface IPagelessEndpoint<Q, R>
{
	/**
	 * @param request
	 *            the request, deserialized from untrusted client input; {@code null} for an
	 *            endpoint registered with request type {@link Void}
	 * @param call
	 *            the call's context
	 * @return the result for the client
	 */
	R handle(Q request, PagelessCall call);
}
