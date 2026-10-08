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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.wicket.request.component.IRequestablePage;

/**
 * Interface for the resource isolation policies.
 * <p>
 * Resource isolation policies are designed to protect against cross-origin attacks.
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
	 * @see IResourceIsolationPolicy#isRequestAllowed(jakarta.servlet.http.HttpServletRequest, org.apache.wicket.request.component.IRequestablePage)
	 */
	public enum ResourceIsolationOutcome
	{
		ALLOWED, DISALLOWED, UNKNOWN
	}

	/**
	 * What the request is going to do with the targeted page. A policy usually has to treat these
	 * differently: a page may legitimately be rendered as the result of a top-level navigation from
	 * another site, but a listener on that page must never be invoked from another site.
	 *
	 * @author papegaaij
	 *
	 * @see IResourceIsolationPolicy#isRequestAllowed(jakarta.servlet.http.HttpServletRequest,
	 *      org.apache.wicket.request.component.IRequestablePage, RequestType)
	 */
	public enum RequestType
	{
		/** The request renders the targeted page. */
		RENDER,
		/** The request invokes a listener on the targeted page, such as a link or a form submit. */
		LISTENER
	}

	/**
	 * Is the given request allowed. Implement {@link #isRequestAllowed(HttpServletRequest,
	 * IRequestablePage, RequestType)} instead when the outcome depends on what the request does with
	 * the page, which is normally the case.
	 * <p>
	 * Implementations of this method are called for both renders and listener invocations without
	 * being able to tell them apart, so they must apply the rules that are safe for a listener
	 * invocation.
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
	 * Is the given request allowed, given what it is going to do with the targeted page.
	 * <p>
	 * This is the method {@link ResourceIsolationRequestCycleListener} calls. The default
	 * implementation ignores {@code requestType} and delegates to
	 * {@link #isRequestAllowed(HttpServletRequest, IRequestablePage)}, so that policies written
	 * against the two-argument method keep working unchanged.
	 *
	 * @param request
	 *            request
	 * @param targetPage
	 *            targeted page
	 * @param requestType
	 *            what the request does with {@code targetPage}
	 * @return outcome, must not be <code>null</code>
	 */
	default ResourceIsolationOutcome isRequestAllowed(HttpServletRequest request,
		IRequestablePage targetPage, RequestType requestType)
	{
		return isRequestAllowed(request, targetPage);
	}

	/**
	 * Set possible response headers.
	 * 
	 * @param response
	 */
	default void setHeaders(HttpServletResponse response)
	{
	}
}
