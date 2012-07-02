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
package org.apache.wicket.protocol.http.servlet;

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.util.string.Strings;

/**
 * Common utils for dispatched (error, forward and include) requests.
 *
 * @since 1.5.8
 */
class DispatchedRequestUtils
{
	/**
	 * Extracts the request uri for a dispatched request (error or forward) and removes the leading
	 * {@code filterPrefix} from it if it is there.
	 *
	 * @param request
	 *      the dispatched request
	 * @param attributeName
	 *      the name of the requets attribute in which the original request uri is stored
	 * @param filterPrefix
	 *      the configured filter prefix for WicketFilter
	 * @return the uri of the dispatched request without the leading filterPrefix
	 */
	static String getRequestUri(final HttpServletRequest request, final String attributeName, String filterPrefix)
	{
		if (filterPrefix == null)
		{
			filterPrefix = "";
		}

		if (Strings.isEmpty(filterPrefix) == false && filterPrefix.startsWith("/") == false)
		{
			filterPrefix = '/' + filterPrefix;
		}

		String uri = (String)request.getAttribute(attributeName);
		if (uri != null && uri.startsWith(filterPrefix) && "/".equals(filterPrefix) == false) {
			uri = uri.substring(filterPrefix.length());
		}

		return uri;
	}
}
