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
package org.apache.wicket.protocol.ws.jetty9;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.wicket.Application;
import org.apache.wicket.protocol.ws.api.AbstractWebSocketProcessor;
import org.eclipse.jetty.websocket.core.annotations.WebSocket;
import org.eclipse.jetty.websocket.core.api.UpgradeRequest;
import org.eclipse.jetty.websocket.core.api.UpgradeResponse;
import org.eclipse.jetty.websocket.core.api.WebSocketConnection;
import org.eclipse.jetty.websocket.core.api.WebSocketException;
import org.eclipse.jetty.websocket.core.api.WebSocketListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An {@link org.apache.wicket.protocol.ws.api.IWebSocketProcessor processor} that integrates with
 * Jetty 9.x {@link WebSocket web socket} implementation.
 *
 * @since 6.2
 */
public class Jetty9WebSocketProcessor extends AbstractWebSocketProcessor
	implements
		WebSocketListener
{
	private static final Logger LOG = LoggerFactory.getLogger(Jetty9WebSocketProcessor.class);

	/**
	 * Constructor.
	 *
	 * @param upgradeRequest
	 *            the jetty upgrade request
	 * @param upgradeResponse
	 *            the jetty upgrade response
	 * @param application
	 *            the current Wicket Application
	 */
	public Jetty9WebSocketProcessor(final UpgradeRequest upgradeRequest,
		final UpgradeResponse upgradeResponse, final Application application)
	{
		super((HttpServletRequest)((HttpServletRequestWrapper)upgradeRequest).getRequest(),
			application);
	}

	@Override
	public void onWebSocketConnect(WebSocketConnection connection)
	{
		onConnect(new Jetty9WebSocketConnection(connection, this));
	}

	@Override
	public void onWebSocketText(String message)
	{
		onMessage(message);
	}

	@Override
	public void onWebSocketBinary(byte[] payload, int offset, int len)
	{
		onMessage(payload, offset, len);
	}

	@Override
	public void onWebSocketClose(int statusCode, String reason)
	{
		onClose(statusCode, reason);
	}

	@Override
	public void onWebSocketException(WebSocketException error)
	{
		LOG.error("An error occurred when using WebSocket.", error);
	}

	@Override
	public void onOpen(Object connection)
	{
		if (!(connection instanceof WebSocketConnection))
		{
			throw new IllegalArgumentException(WebSocketConnection.class.getName() +
				" can work only with " + WebSocketConnection.class.getName());
		}
		onWebSocketConnect((WebSocketConnection)connection);
	}
}