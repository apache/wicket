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
package org.apache.wicket.protocol.ws.jetty;

import java.io.IOException;

import org.apache.wicket.protocol.ws.api.WebSocketConnection;
import org.apache.wicket.util.lang.Args;
import org.eclipse.jetty.websocket.WebSocket;

/**
 * A wrapper around Jetty's native WebSocket.Connection
 *
 * @since 6.0
 */
public class JettyWebSocketConnection implements WebSocketConnection
{
	private WebSocket.Connection connection;

	public JettyWebSocketConnection(final WebSocket.Connection connection)
	{
		this.connection = Args.notNull(connection, "connection");
	}

	@Override
	public boolean isOpen()
	{
		return connection.isOpen();
	}

	@Override
	public void close(int code, String reason)
	{
		if (isOpen())
		{
			connection.close(code, reason);
		}
	}

	@Override
	public WebSocketConnection sendMessage(String message) throws IOException
	{
		checkClosed();

		connection.sendMessage(message);
		return this;
	}

	@Override
	public WebSocketConnection sendMessage(byte[] message, int offset, int length) throws IOException
	{
		checkClosed();

		connection.sendMessage(message, offset, length);
		return this;
	}

	private void checkClosed()
	{
		if (!isOpen())
		{
			throw new IllegalStateException("The connection is closed.");
		}
	}
}
