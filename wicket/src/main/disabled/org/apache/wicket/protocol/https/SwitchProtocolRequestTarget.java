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

import org.apache.wicket.IRequestHandler;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebResponse;

/**
 * Request target that performs redirects across http and https
 */
class SwitchProtocolRequestTarget implements IRequestHandler
{
	/**
	 * Protocols
	 */
	public enum Protocol {
		/*** HTTP */
		HTTP,
		/** HTTPS */
		HTTPS,
		/** CURRENT */
		PRESERVE_CURRENT
	}

	private final Protocol protocol;

	/**
	 * Constructor
	 * 
	 * @param protocol
	 *            required protocol
	 */
	public SwitchProtocolRequestTarget(Protocol protocol)
	{
		if (protocol == null)
		{
			throw new IllegalArgumentException("Argument 'protocol' may not be null.");
		}
		if (protocol == Protocol.PRESERVE_CURRENT)
		{
			throw new IllegalArgumentException("Argument 'protocol' may not have value '" +
				Protocol.PRESERVE_CURRENT.toString() + "'.");
		}
		this.protocol = protocol;
	}

	/** {@inheritDoc} */
	public void detach(RequestCycle requestCycle)
	{

	}

	/**
	 * Rewrite the url using the specified protocol
	 * 
	 * @param protocol
	 * @param port
	 * @param request
	 * @return url
	 */
	private String getUrl(String protocol, Integer port, HttpServletRequest request)
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
	public void respond(RequestCycle requestCycle)
	{
		WebRequest webRequest = (WebRequest)requestCycle.getRequest();
		HttpServletRequest request = webRequest.getHttpServletRequest();

		HttpsRequestCycleProcessor processor = (HttpsRequestCycleProcessor)requestCycle.getProcessor();
		Integer port = null;
		if (protocol == Protocol.HTTP)
		{
			if (processor.getConfig().getHttpPort() != 80)
			{
				port = processor.getConfig().getHttpPort();
			}
		}
		else if (protocol == Protocol.HTTPS)
		{
			if (processor.getConfig().getHttpsPort() != 443)
			{
				port = processor.getConfig().getHttpsPort();
			}
		}

		String url = getUrl(protocol.toString().toLowerCase(), port, request);

		WebResponse response = (WebResponse)requestCycle.getResponse();

		// an attempt to rewrite a secure jsessionid into nonsecure, doesnt seem to work
		// Session session = Session.get();
		// if (!session.isTemporary())
		// {
		// response.addCookie(new Cookie("JSESSIONID", session.getId()));
		// }

		response.redirect(url);
	}

	/**
	 * Returns a target that can be used to redirect to the specified protocol. If no change is
	 * required null will be returned.
	 * 
	 * @param protocol
	 *            required protocol
	 * @return request target or null
	 */
	public static IRequestHandler requireProtocol(Protocol protocol)
	{
		RequestCycle requestCycle = RequestCycle.get();
		WebRequest webRequest = (WebRequest)requestCycle.getRequest();
		HttpServletRequest request = webRequest.getHttpServletRequest();
		if (protocol == null || protocol == Protocol.PRESERVE_CURRENT ||
			request.getScheme().equals(protocol.toString().toLowerCase()))
		{
			return null;
		}
		else
		{
			return new SwitchProtocolRequestTarget(protocol);
		}
	}

}
