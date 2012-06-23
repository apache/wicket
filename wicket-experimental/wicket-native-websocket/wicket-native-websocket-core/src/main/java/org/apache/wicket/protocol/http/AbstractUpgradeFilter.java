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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.Application;
import org.apache.wicket.ThreadContext;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;

/**
 * An extension of WicketFilter that is used to check whether
 * the processed HttpServletRequest needs to upgrade its protocol
 * from HTTP to something else
 *
 * @since 6.0
 */
public class AbstractUpgradeFilter extends WicketFilter
{
	/**
	 * This is Wicket's main method to execute a request
	 *
	 * @param request
	 *      the http request
	 * @param response
	 *      the http response
	 * @param chain
	 *      the filter chain
	 * @return false, if the request could not be processed
	 * @throws IOException
	 * @throws ServletException
	 */
	// TODO improve WicketFilter#processRequest() to provide a hook instead of copy/pasting its whole code
	@Override
	boolean processRequest(ServletRequest request, final ServletResponse response,
	                       final FilterChain chain) throws IOException, ServletException
	{
		final ThreadContext previousThreadContext = ThreadContext.detach();

		// Assume we are able to handle the request
		boolean res = true;

		final ClassLoader previousClassLoader = Thread.currentThread().getContextClassLoader();
		final ClassLoader newClassLoader = getClassLoader();

		try
		{
			if (previousClassLoader != newClassLoader)
			{
				Thread.currentThread().setContextClassLoader(newClassLoader);
			}

			HttpServletRequest httpServletRequest = (HttpServletRequest)request;
			HttpServletResponse httpServletResponse = (HttpServletResponse)response;

			// Make sure getFilterPath() gets called before checkIfRedirectRequired()
			String filterPath = getFilterPath(httpServletRequest);

			if (filterPath == null)
			{
				throw new IllegalStateException("filter path was not configured");
			}

			WebApplication application = getApplication();
			// No redirect; process the request
			ThreadContext.setApplication(application);

			WebRequest webRequest = application.createWebRequest(httpServletRequest, filterPath);
			WebResponse webResponse = application.createWebResponse(webRequest,
					httpServletResponse);

			RequestCycle requestCycle = application.createRequestCycle(webRequest, webResponse);
			ThreadContext.setRequestCycle(requestCycle);

			if (acceptWebSocket(httpServletRequest, httpServletResponse, application) || httpServletResponse.isCommitted())
			{
				res = true;
			}
			else if (!requestCycle.processRequestAndDetach() && !httpServletResponse.isCommitted())
			{
				if (chain != null)
				{
					chain.doFilter(request, response);
				}
				res = false;
			}
			else
			{
				webResponse.flush();
			}

		}
		finally
		{
			ThreadContext.restore(previousThreadContext);

			if (newClassLoader != previousClassLoader)
			{
				Thread.currentThread().setContextClassLoader(previousClassLoader);
			}

			if (response.isCommitted())
			{
				response.flushBuffer();
			}
		}
		return res;
	}

	protected boolean acceptWebSocket(HttpServletRequest req, HttpServletResponse resp, Application application)
			throws ServletException, IOException
	{
		// Information required to send the server handshake message
		String key;
		String subProtocol = null;
		List<String> extensions = Collections.emptyList();

		if (!headerContainsToken(req, "upgrade", "websocket")) {
//			resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return false;
		}

		if (!headerContainsToken(req, "connection", "upgrade")) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return false;
		}

		if (!headerContainsToken(req, "sec-websocket-version", "13")) {
			resp.setStatus(426);
			resp.setHeader("Sec-WebSocket-Version", "13");
			return false;
		}

		key = req.getHeader("Sec-WebSocket-Key");
		if (key == null) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return false;
		}

		String origin = req.getHeader("Origin");
		if (!verifyOrigin(origin)) {
			resp.sendError(HttpServletResponse.SC_FORBIDDEN);
			return false;
		}

		List<String> subProtocols = getTokensFromHeader(req,
				"Sec-WebSocket-Protocol-Client");
		if (!subProtocols.isEmpty()) {
			subProtocol = selectSubProtocol(subProtocols);

		}

		resp.setHeader("upgrade", "websocket");
		resp.setHeader("connection", "upgrade");

		if (subProtocol != null) {
			resp.setHeader("Sec-WebSocket-Protocol", subProtocol);
		}

		return true;
	}

	/*
	 * This only works for tokens. Quoted strings need more sophisticated
	 * parsing.
	 */
	private boolean headerContainsToken(HttpServletRequest req,
	                                    String headerName, String target) {
		Enumeration<String> headers = req.getHeaders(headerName);
		while (headers.hasMoreElements()) {
			String header = headers.nextElement();
			String[] tokens = header.split(",");
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
	protected List<String> getTokensFromHeader(HttpServletRequest req,
	                                           String headerName) {
		List<String> result = new ArrayList<String>();

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
	protected boolean verifyOrigin(String origin) {
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
	protected String selectSubProtocol(List<String> subProtocols) {
		return null;
	}
}
