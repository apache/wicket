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
import org.apache.wicket.util.string.Strings;

/**
 * Default resource isolation policy used in {@link ResourceIsolationRequestCycleListener},
 * based on <a href="https://web.dev/fetch-metadata/">https://web.dev/fetch-metadata/</a>.
 * <p>
 * The policy decides on the {@code Sec-Fetch-*} headers, which a browser sets itself and which
 * cannot be set or removed by page content, and it distinguishes what the request does with the
 * targeted page:
 * <ul>
 * <li>{@code same-origin} and {@code none} are allowed. {@code none} means there was no initiating
 * document at all - a typed URL or a bookmark - which another document cannot cause.</li>
 * <li>{@code same-site} is a <em>different</em> origin on the same site, so by default it may not
 * invoke a listener. See {@link #setSameSiteAllowed(boolean)}.</li>
 * <li>Anything else from another origin may still {@link RequestType#RENDER} a page through a simple
 * top-level navigation, so that pages can be linked to from elsewhere, but may never invoke a
 * {@link RequestType#LISTENER}.</li>
 * <li>When the request carries no {@code Sec-Fetch-Site} header at all the outcome is
 * {@link ResourceIsolationOutcome#UNKNOWN} and the next policy decides.</li>
 * </ul>
 * Wicket invokes listeners through ordinary GET navigations, so the headers alone do not say whether
 * a request merely renders a page or performs an action on it. The {@link RequestType} the listener
 * passes in is what separates the two.
 *
 * @see <a href="https://web.dev/fetch-metadata/">https://web.dev/fetch-metadata/</a>
 *
 * @author Santiago Diaz - saldiaz@google.com
 * @author Ecenaz Jen Ozmen - ecenazo@google.com
 */
public class FetchMetadataResourceIsolationPolicy implements IResourceIsolationPolicy
{

	public static final String SEC_FETCH_SITE_HEADER = "sec-fetch-site";
	public static final String SEC_FETCH_MODE_HEADER = "sec-fetch-mode";
	public static final String SEC_FETCH_DEST_HEADER = "sec-fetch-dest";

	public static final String SAME_ORIGIN = "same-origin";
	public static final String SAME_SITE = "same-site";
	public static final String NONE = "none";
	public static final String MODE_NAVIGATE = "navigate";
	public static final String MODE_NO_CORS = "no-cors";
	public static final String DEST_OBJECT = "object";
	public static final String DEST_EMBED = "embed";
	public static final String CROSS_SITE = "cross-site";
	public static final String CORS = "cors";
	public static final String DEST_DOCUMENT = "document";
	public static final String DEST_SCRIPT = "script";
	public static final String DEST_IMAGE = "image";
	
	public static final String VARY_HEADER = "Vary";
	
	private static final String VARY_HEADER_VALUE = SEC_FETCH_DEST_HEADER + ", "
		+ SEC_FETCH_SITE_HEADER + ", " + SEC_FETCH_MODE_HEADER;
	
	private boolean sameSiteAllowed = false;

	/**
	 * Called when the type of the request is not known. Applies the rules for a
	 * {@link RequestType#LISTENER}, which are the stricter of the two.
	 */
	@Override
	public ResourceIsolationOutcome isRequestAllowed(HttpServletRequest request,
		IRequestablePage targetPage)
	{
		return isRequestAllowed(request, targetPage, RequestType.LISTENER);
	}

	@Override
	public ResourceIsolationOutcome isRequestAllowed(HttpServletRequest request,
		IRequestablePage targetPage, RequestType requestType)
	{
		// request made by a legacy browser with no support for Fetch Metadata
		String site = request.getHeader(SEC_FETCH_SITE_HEADER);
		if (Strings.isEmpty(site))
		{
			return ResourceIsolationOutcome.UNKNOWN;
		}

		// Allow same-origin and browser-initiated requests. A browser cannot be made to report
		// 'none' by another document, so it is not a forgeable value.
		if (SAME_ORIGIN.equals(site) || NONE.equals(site))
		{
			return ResourceIsolationOutcome.ALLOWED;
		}

		// Allow requests from a sibling origin on the same site only when configured to do so
		if (SAME_SITE.equals(site) && sameSiteAllowed)
		{
			return ResourceIsolationOutcome.ALLOWED;
		}

		// The request comes from another origin. Rendering a page is allowed for a simple top-level
		// navigation, except <object> and <embed>, so that the page can still be linked to from
		// elsewhere. Invoking a listener from another origin is not allowed.
		return requestType == RequestType.RENDER && isAllowedTopLevelNavigation(request)
			? ResourceIsolationOutcome.ALLOWED
			: ResourceIsolationOutcome.DISALLOWED;
	}

	/**
	 * Sets whether requests from a different origin on the same site are allowed to invoke a
	 * listener. {@code Sec-Fetch-Site: same-site} means the request comes from the same registrable
	 * domain and scheme but a <em>different</em> origin, such as another subdomain or another port.
	 * <p>
	 * This is {@code false} by default, so a sibling origin cannot invoke a listener. Enable it when
	 * every origin on the site is trusted, for instance when the sibling subdomains are all part of
	 * the same application. Note that a hostile sibling - through a subdomain takeover, delegated
	 * user content, or an XSS elsewhere on the site - can then perform actions in the context of an
	 * authenticated user, because the browser sends the session cookie on a same-site request.
	 * <p>
	 * This setting does not affect renders: a page can always be reached by a simple top-level
	 * navigation, from a sibling origin or from another site entirely.
	 *
	 * @param sameSiteAllowed
	 *            {@code true} to trust every origin on the same site
	 * @return {@code this} object for chaining
	 */
	public FetchMetadataResourceIsolationPolicy setSameSiteAllowed(boolean sameSiteAllowed)
	{
		this.sameSiteAllowed = sameSiteAllowed;
		return this;
	}

	/**
	 * @return whether requests from a different origin on the same site may invoke a listener,
	 *         {@code false} by default
	 * @see #setSameSiteAllowed(boolean)
	 */
	public boolean isSameSiteAllowed()
	{
		return sameSiteAllowed;
	}

	private boolean isAllowedTopLevelNavigation(HttpServletRequest request)
	{
		String mode = request.getHeader(SEC_FETCH_MODE_HEADER);
		String dest = request.getHeader(SEC_FETCH_DEST_HEADER);

		boolean isSimpleTopLevelNavigation = MODE_NAVIGATE.equals(mode)
			&& "GET".equalsIgnoreCase(request.getMethod());
		boolean isNotObjectOrEmbedRequest = !DEST_EMBED.equals(dest) && !DEST_OBJECT.equals(dest);

		return isSimpleTopLevelNavigation && isNotObjectOrEmbedRequest;
	}

	/**
	 * Set vary headers to avoid caching responses processed by Fetch Metadata.
	 * <p>
	 * Caching these responses may return 403 responses to legitimate requests
	 * defeat the protection.
	 */
	@Override
	public void setHeaders(HttpServletResponse response)
	{
		response.addHeader(VARY_HEADER, VARY_HEADER_VALUE);
	}
}
