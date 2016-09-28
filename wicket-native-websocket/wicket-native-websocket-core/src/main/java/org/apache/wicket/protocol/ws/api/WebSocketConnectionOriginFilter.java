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
package org.apache.wicket.protocol.ws.api;

import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.string.Strings;

/**
 * This filter will reject those requests which contain 'Origin' header that does not match the origin of the
 * application host. This kind of extended security might be necessary if the application needs to enforce the
 * Same Origin Policy which is not provided by the HTML5 WebSocket protocol.
 *
 * @see <a href="http://www.christian-schneider.net/CrossSiteWebSocketHijacking.html">http://www.christian-schneider.net/CrossSiteWebSocketHijacking.html</a>
 *
 * @author Gergely Nagy
 */
public class WebSocketConnectionOriginFilter implements IWebSocketConnectionFilter
{

	/**
	 * Error code 1008 indicates that an endpoint is terminating the connection because it has received a message that
     * violates its policy. This is a generic status code that can be returned when there is no other more suitable
     * status code (e.g., 1003 or 1009) or if there is a need to hide specific details about the policy.
	 * <p>
	 * See <a href="https://tools.ietf.org/html/rfc6455#section-7.4.1">RFC 6455, Section 7.4.1 Defined Status Codes</a>.
	 */
	public static final int POLICY_VIOLATION_ERROR_CODE = 1008;

	/**
	 * Explanatory text for the client to explain why the connection is getting aborted
	 */
	public static final String ORIGIN_MISMATCH = "Origin mismatch";

	private final List<String> allowedDomains;

	public WebSocketConnectionOriginFilter(final List<String> allowedDomains)
	{
		this.allowedDomains = Args.notNull(allowedDomains, "allowedDomains");
	}

	@Override
	public ConnectionRejected doFilter(HttpServletRequest servletRequest)
	{
		if (allowedDomains != null && !allowedDomains.isEmpty())
		{
			String oUrl = getOriginUrl(servletRequest);
			if (invalid(oUrl, allowedDomains))
			{
				return new ConnectionRejected(POLICY_VIOLATION_ERROR_CODE, ORIGIN_MISMATCH);
			}
		}

		return null;
	}

	/**
	 * The list of whitelisted domains which are allowed to initiate a websocket connection. This
	 * list will be eventually used by the
	 * {@link org.apache.wicket.protocol.ws.api.IWebSocketConnectionFilter} to abort potentially
	 * unsafe connections. Example domain names might be:
	 *
	 * <pre>
	 *      http://www.example.com
	 *      http://ww2.example.com
	 * </pre>
	 *
	 * @param domains
	 *            The collection of domains
	 */
	public void setAllowedDomains(Iterable<String> domains) {
		this.allowedDomains.clear();
		if (domains != null)
		{
			for (String domain : domains)
			{
				this.allowedDomains.add(domain);
			}
		}
	}

	/**
	 * The list of whitelisted domains which are allowed to initiate a websocket connection. This
	 * list will be eventually used by the
	 * {@link org.apache.wicket.protocol.ws.api.IWebSocketConnectionFilter} to abort potentially
	 * unsafe connections
	 */
	public List<String> getAllowedDomains()
	{
		return allowedDomains;
	}

	private boolean invalid(String oUrl, List<String> allowedDomains)
	{
		return Strings.isEmpty(oUrl) || !allowedDomains.contains(oUrl);
	}

	private String getOriginUrl(final HttpServletRequest servletRequest)
	{
		Enumeration<String> originHeaderValues = servletRequest.getHeaders("Origin");
		List<String> origins;
		if (originHeaderValues != null)
		{
			origins = Collections.list(originHeaderValues);
		}
		else
		{
			origins = Collections.emptyList();
		}

		if (origins.size() != 1)
		{
			return null;
		}
		return origins.get(0);
	}
}
