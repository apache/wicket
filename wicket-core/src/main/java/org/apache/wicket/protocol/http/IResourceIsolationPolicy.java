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
package org.apache.wicket.protocol.http;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.request.component.IRequestablePage;

/**
 * Interface for the resource isolation policies.
 * <p>
 * Resource isolation policies are designed to protect against cross origin attacks.
 * <p>
 * See {@link FetchMetadataResourceIsolationPolicy} for the default implementation used
 * by {@link ResourceIsolationRequestCycleListener}.
 *
 * @see <a href="https://web.dev/fetch-metadata/">https://web.dev/fetch-metadata/</a>
 *
 * @author Santiago Diaz - saldiaz@google.com
 * @author Ecenaz Jen Ozmen - ecenazo@google.com
 */
@FunctionalInterface
public interface IResourceIsolationPolicy
{
	/**
	 * Indicates the outcome for a resource isolation policy for a request. When the outcome is
	 * {@link #UNKNOWN}, the next policy will be consulted.
	 * 
	 * @author papegaaij
	 * 
	 * @see IResourceIsolationPolicy#isRequestAllowed(javax.servlet.http.HttpServletRequest, org.apache.wicket.request.component.IRequestablePage)
	 */
	public enum ResourceIsolationOutcome
	{
		ALLOWED, DISALLOWED, UNKNOWN
	}

	/**
	 * Is the given request allowed.
	 * 
	 * @param request
	 *            request
	 * @param targetPage
	 *            targeted page
	 * @return outcome, must not be <code>null</code>
	 */
	ResourceIsolationOutcome isRequestAllowed(HttpServletRequest request,
		IRequestablePage targetPage);

	/**
	 * Set possible response headers.
	 * 
	 * @param response
	 */
	default void setHeaders(HttpServletResponse response)
	{
	}
}
