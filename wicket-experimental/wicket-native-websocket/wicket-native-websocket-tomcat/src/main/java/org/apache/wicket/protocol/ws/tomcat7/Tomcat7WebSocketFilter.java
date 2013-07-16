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
package org.apache.wicket.protocol.ws.tomcat7;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.connector.RequestFacade;
import org.apache.catalina.util.Base64;
import org.apache.tomcat.util.buf.B2CConverter;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.ws.AbstractUpgradeFilter;

/**
 * An upgrade filter that uses code borrowed from Tomcat's WebSocketServlet
 * to decide whether to upgrade the request protocol to websocket or not.
 *
 * @since 6.0
 */
public class Tomcat7WebSocketFilter extends AbstractUpgradeFilter
{
	private static final byte[] WS_ACCEPT =
			"258EAFA5-E914-47DA-95CA-C5AB0DC85B11".getBytes(
					B2CConverter.ISO_8859_1);

	private MessageDigest sha1Helper;


	@Override
	public void init(final boolean isServlet, final FilterConfig filterConfig)
			throws ServletException
	{
		super.init(isServlet, filterConfig);

		try {
			sha1Helper = MessageDigest.getInstance("SHA1");
		} catch (NoSuchAlgorithmException e) {
			throw new ServletException(e);
		}
	}

	@Override
	protected boolean acceptWebSocket(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException
	{
		if (!super.acceptWebSocket(req, resp))
		{
			return false;
		}

		String key = req.getHeader("Sec-WebSocket-Key");
		resp.setHeader("Sec-WebSocket-Accept", getWebSocketAccept(key));

		WebApplication application = getApplication();
		// Small hack until the Servlet API provides a way to do this.
		TomcatWebSocketProcessor webSocketHandler = new TomcatWebSocketProcessor(req, application);
		TomcatWebSocketProcessor.TomcatWebSocket tomcatWebSocket = webSocketHandler.new TomcatWebSocket();

		// the request can be a wrapper from application servlet filters
		while (req instanceof HttpServletRequestWrapper)
		{
			req = (HttpServletRequest) ((HttpServletRequestWrapper) req).getRequest();
		}
		((RequestFacade) req).doUpgrade(tomcatWebSocket);
		return true;
	}

	private String getWebSocketAccept(String key) {
		synchronized (sha1Helper) {
			sha1Helper.reset();
			sha1Helper.update(key.getBytes(B2CConverter.ISO_8859_1));
			return Base64.encode(sha1Helper.digest(WS_ACCEPT));
		}
	}


}
