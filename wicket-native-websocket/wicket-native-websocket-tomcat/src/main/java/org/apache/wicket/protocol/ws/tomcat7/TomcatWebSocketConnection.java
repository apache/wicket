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
import java.nio.ByteBuffer;
import java.nio.CharBuffer;

import org.apache.catalina.websocket.WsOutbound;
import org.apache.wicket.protocol.ws.api.AbstractWebSocketConnection;
import org.apache.wicket.protocol.ws.api.AbstractWebSocketProcessor;
import org.apache.wicket.protocol.ws.api.IWebSocketConnection;
import org.apache.wicket.util.lang.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * * A wrapper around Tomcat's native WsOutbound
 *
 * @since 6.0
 */
public class TomcatWebSocketConnection extends AbstractWebSocketConnection
{
	private static final Logger LOG = LoggerFactory.getLogger(TomcatWebSocketConnection.class);
	
	private final WsOutbound connection;

	private boolean closed = false;

	public TomcatWebSocketConnection(final WsOutbound connection, final AbstractWebSocketProcessor webSocketProcessor)
	{
		super(webSocketProcessor);
		this.connection = Args.notNull(connection, "connection");
	}

	@Override
	public boolean isOpen()
	{
		return !closed;
	}

	@Override
	public void close(int code, String reason)
	{
		if (isOpen())
		{
			try
			{
				ByteBuffer byteBuffer = ByteBuffer.wrap(reason.getBytes("UTF-8"));
				connection.close(0, byteBuffer);
			}
			catch (IOException iox)
			{
				LOG.error("An error occurred while closing WebSocket connection with initial reason: " + reason, iox);
			}
			closed = true;
		}
	}

	@Override
	public IWebSocketConnection sendMessage(String message) throws IOException
	{
		checkClosed();

		CharBuffer buffer = CharBuffer.wrap(message);
		connection.writeTextMessage(buffer);
		return this;
	}

	@Override
	public TomcatWebSocketConnection sendMessage(byte[] message, int offset, int length) throws IOException
	{
		checkClosed();

		ByteBuffer buffer = ByteBuffer.wrap(message, offset, length);
		connection.writeBinaryMessage(buffer);
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
