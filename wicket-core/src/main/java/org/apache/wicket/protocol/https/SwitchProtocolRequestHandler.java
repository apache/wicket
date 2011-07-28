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
package org.apache.wicket.protocol.https;

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.request.IRequestCycle;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.util.lang.Args;

/**
 * Request handler that performs redirects across http and https
 */
class SwitchProtocolRequestHandler implements IRequestHandler
{

	/** the protocol this request handler is going to switch to */
	private final Protocol protocol;

	private final HttpsConfig httpsConfig;

	/**
	 * Constructor
	 * 
	 * @param protocol
	 *            required protocol
	 * @param httpsConfig
	 *            the https configuration
	 */
	SwitchProtocolRequestHandler(Protocol protocol, final HttpsConfig httpsConfig)
	{
		Args.notNull(protocol, "protocol");
		Args.notNull(httpsConfig, "httpsConfig");

		if (protocol == Protocol.PRESERVE_CURRENT)
		{
			throw new IllegalArgumentException("Argument 'protocol' may not have value '" +
				Protocol.PRESERVE_CURRENT.toString() + "'.");
		}

		this.protocol = protocol;
		this.httpsConfig = httpsConfig;
	}

	/**
	 * Rewrite the url using the specified protocol
	 * 
	 * @param protocol
	 * @param port
	 * @param request
	 * @return url
	 */
	protected String getUrl(String protocol, Integer port, HttpServletRequest request)
	{
		StringBuilder result = new StringBuilder();
		result.append(protocol);
		result.append("://");
		result.append(request.getServerName());
		if (port != null)
		{
			result.append(":");
			result.append(port);
		}
		result.append(request.getRequestURI());
		if (request.getQueryString() != null)
		{
			result.append("?");
			result.append(request.getQueryString());
		}
		return result.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public void respond(IRequestCycle requestCycle)
	{
		WebRequest webRequest = (WebRequest)requestCycle.getRequest();
		HttpServletRequest request = (HttpServletRequest)webRequest.getContainerRequest();

		Integer port = null;
		if (protocol == Protocol.HTTP)
		{
			if (httpsConfig.getHttpPort() != 80)
			{
				port = httpsConfig.getHttpPort();
			}
		}
		else if (protocol == Protocol.HTTPS)
		{
			if (httpsConfig.getHttpsPort() != 443)
			{
				port = httpsConfig.getHttpsPort();
			}
		}

		final String url = getUrl(protocol.toString().toLowerCase(), port, request);

		WebResponse response = (WebResponse)requestCycle.getResponse();

		response.sendRedirect(url);
	}

	/**
	 * Returns a target that can be used to redirect to the specified protocol. If no change is
	 * required {@code null} will be returned.
	 * 
	 * @param protocol
	 *            required protocol
	 * @param httpsConfig
	 *            the https configuration
	 * @return request handler or {@code null}
	 */
	public static IRequestHandler requireProtocol(Protocol protocol, final HttpsConfig httpsConfig)
	{
		IRequestCycle requestCycle = RequestCycle.get();
		WebRequest webRequest = (WebRequest)requestCycle.getRequest();
		HttpServletRequest request = (HttpServletRequest)webRequest.getContainerRequest();
		if (protocol == null || protocol == Protocol.PRESERVE_CURRENT ||
			request.getScheme().equals(protocol.toString().toLowerCase()))
		{
			return null;
		}
		else
		{
			return new SwitchProtocolRequestHandler(protocol, httpsConfig);
		}
	}

	/** {@inheritDoc} */
	public void detach(IRequestCycle requestCycle)
	{
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "SwitchProtocolRequestHandler";
	}
}
