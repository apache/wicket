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
import org.apache.wicket.util.string.Strings;

/**
 * Default resource isolation policy used in {@link ResourceIsolationRequestCycleListener},
 * based on <a href="https://web.dev/fetch-metadata/">https://web.dev/fetch-metadata/</a>.
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
	public static final String DEST_OBJECT = "object";
	public static final String DEST_EMBED = "embed";
	public static final String CROSS_SITE = "cross-site";
	public static final String CORS = "cors";
	public static final String DEST_SCRIPT = "script";
	public static final String DEST_IMAGE = "image";
	
	public static final String VARY_HEADER = "Vary";
	
	private static final String VARY_HEADER_VALUE = SEC_FETCH_DEST_HEADER + ", "
		+ SEC_FETCH_SITE_HEADER + ", " + SEC_FETCH_MODE_HEADER;
	
	@Override
	public ResourceIsolationOutcome isRequestAllowed(HttpServletRequest request,
		IRequestablePage targetPage)
	{
		// request made by a legacy browser with no support for Fetch Metadata
		String site = request.getHeader(SEC_FETCH_SITE_HEADER);
		if (Strings.isEmpty(site))
		{
			return ResourceIsolationOutcome.UNKNOWN;
		}
		
		// Allow same-site and browser-initiated requests
		if (SAME_ORIGIN.equals(site) || SAME_SITE.equals(site) || NONE.equals(site))
		{
			return ResourceIsolationOutcome.ALLOWED;
		}

		// Allow simple top-level navigations except <object> and <embed>
		return isAllowedTopLevelNavigation(request)
			? ResourceIsolationOutcome.ALLOWED
			: ResourceIsolationOutcome.DISALLOWED;
	}

	private boolean isAllowedTopLevelNavigation(HttpServletRequest request)
	{
		String mode = request.getHeader(SEC_FETCH_MODE_HEADER);
		String dest = request.getHeader(SEC_FETCH_DEST_HEADER);

		boolean isSimpleTopLevelNavigation = MODE_NAVIGATE.equals(mode)
			|| "GET".equals(request.getMethod());
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
