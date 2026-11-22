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
 * Decides whether a page-less call may run. Every page-less endpoint is registered with one; there
 * is no default that silently allows everything.
 *
 * @since 12.0.0
 */
@FunctionalInterface
public interface IPagelessAuthorization
{
	/**
	 * Allows every call. Use it for endpoints serving public data, so that the decision is visible
	 * where the endpoint is registered.
	 */
	IPagelessAuthorization PERMIT_ALL = call -> true;

	/**
	 * @param call
	 *            the call
	 * @return whether the call may run
	 */
	boolean isAllowed(PagelessCall call);
}
