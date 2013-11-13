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
package org.apache.wicket.protocol.ws;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.ThreadContext;
import org.apache.wicket.protocol.http.WicketFilter;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.util.string.Strings;

/**
 * An extension of WicketFilter that is used to check whether
 * the processed HttpServletRequest needs to upgrade its protocol
 * from HTTP to something else
 *
 * @since 6.0
 */
public class AbstractUpgradeFilter extends WicketFilter
{
	protected boolean processRequestCycle(final RequestCycle requestCycle, final WebResponse webResponse,
			final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse,
			final FilterChain chain)
		throws IOException, ServletException
	{

		// Assume we are able to handle the request
		boolean res = true;

		ThreadContext.setRequestCycle(requestCycle);

		if (acceptWebSocket(httpServletRequest, httpServletResponse) || httpServletResponse.isCommitted())
		{
			res = true;
		}
		else if (!requestCycle.processRequestAndDetach() && !httpServletResponse.isCommitted())
		{
			if (chain != null)
			{
				chain.doFilter(httpServletRequest, httpServletResponse);
			}
			res = false;
		}
		else
		{
			webResponse.flush();
		}

		return res;
	}

	protected boolean acceptWebSocket(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException
	{
		// Information required to send the server handshake message
		String key;
		String subProtocol = null;

		if (!headerContainsToken(req, "upgrade", "websocket"))
		{
			return false;
		}

		if (!headerContainsToken(req, "connection", "upgrade"))
		{
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return false;
		}

		if (!headerContainsToken(req, "sec-websocket-version", "13"))
		{
			resp.setStatus(426);
			resp.setHeader("Sec-WebSocket-Version", "13");
			return false;
		}

		key = req.getHeader("Sec-WebSocket-Key");
		if (key == null)
		{
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return false;
		}

		String origin = req.getHeader("Origin");
		if (!verifyOrigin(origin))
		{
			resp.sendError(HttpServletResponse.SC_FORBIDDEN);
			return false;
		}

		List<String> subProtocols = getTokensFromHeader(req, "Sec-WebSocket-Protocol-Client");
		if (!subProtocols.isEmpty())
		{
			subProtocol = selectSubProtocol(subProtocols);
		}

		if (subProtocol != null)
		{
			resp.setHeader("Sec-WebSocket-Protocol", subProtocol);
		}

		return true;
	}

	/*
	 * This only works for tokens. Quoted strings need more sophisticated
	 * parsing.
	 */
	private boolean headerContainsToken(HttpServletRequest req, String headerName, String target)
	{
		Enumeration<String> headers = req.getHeaders(headerName);
		while (headers.hasMoreElements()) {
			String header = headers.nextElement();
			String[] tokens = Strings.split(header, ',');
			for (String token : tokens) {
				if (target.equalsIgnoreCase(token.trim())) {
					return true;
				}
			}
		}
		return false;
	}

	/*
	 * This only works for tokens. Quoted strings need more sophisticated
	 * parsing.
	 */
	protected List<String> getTokensFromHeader(HttpServletRequest req, String headerName)
	{
		List<String> result = new ArrayList<>();

		Enumeration<String> headers = req.getHeaders(headerName);
		while (headers.hasMoreElements()) {
			String header = headers.nextElement();
			String[] tokens = header.split(",");
			for (String token : tokens) {
				result.add(token.trim());
			}
		}
		return result;
	}

	/**
	 * Intended to be overridden by sub-classes that wish to verify the origin
	 * of a WebSocket request before processing it.
	 *
	 * @param origin    The value of the origin header from the request which
	 *                  may be <code>null</code>
	 *
	 * @return  <code>true</code> to accept the request. <code>false</code> to
	 *          reject it. This default implementation always returns
	 *          <code>true</code>.
	 */
	protected boolean verifyOrigin(String origin)
	{
		return true;
	}

	/**
	 * Intended to be overridden by sub-classes that wish to select a
	 * sub-protocol if the client provides a list of supported protocols.
	 *
	 * @param subProtocols  The list of sub-protocols supported by the client
	 *                      in client preference order. The server is under no
	 *                      obligation to respect the declared preference
	 * @return  <code>null</code> if no sub-protocol is selected or the name of
	 *          the protocol which <b>must</b> be one of the protocols listed by
	 *          the client. This default implementation always returns
	 *          <code>null</code>.
	 */
	protected String selectSubProtocol(List<String> subProtocols)
	{
		return null;
	}
}
