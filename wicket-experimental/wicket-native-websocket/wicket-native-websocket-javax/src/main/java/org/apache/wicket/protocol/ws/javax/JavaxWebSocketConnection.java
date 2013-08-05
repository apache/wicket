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
package org.apache.wicket.protocol.ws.javax;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.websocket.CloseReason;
import javax.websocket.Session;

import org.apache.wicket.protocol.ws.api.AbstractWebSocketConnection;
import org.apache.wicket.protocol.ws.api.AbstractWebSocketProcessor;
import org.apache.wicket.protocol.ws.api.IWebSocketConnection;
import org.apache.wicket.util.lang.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A wrapper around JSR 356's native Session.
 *
 * @since 7.0.0
 */
public class JavaxWebSocketConnection extends AbstractWebSocketConnection
{
	private static final Logger LOG = LoggerFactory.getLogger(JavaxWebSocketConnection.class);

	private final Session session;

	/**
	 * Constructor.
	 *
	 * @param session
	 *            the WebSocket session
	 */
	public JavaxWebSocketConnection(Session session, AbstractWebSocketProcessor webSocketProcessor)
	{
		super(webSocketProcessor);
		this.session = Args.notNull(session, "connection");
	}

	@Override
	public boolean isOpen()
	{
		return session.isOpen();
	}

	@Override
	public void close(int code, String reason)
	{
		if (isOpen())
		{
			try
			{
				session.close(new CloseReason(new CloseCode(code), reason));
			} catch (IOException iox)
			{
				LOG.error("An error occurred while closing WebSocket session", iox);
			}
		}
	}

	@Override
	public IWebSocketConnection sendMessage(String message) throws IOException
	{
		checkClosed();

		session.getBasicRemote().sendText(message);
		return this;
	}

	@Override
	public IWebSocketConnection sendMessage(byte[] message, int offset, int length)
		throws IOException
	{
		checkClosed();

		ByteBuffer buf = ByteBuffer.wrap(message, offset, length);
		session.getBasicRemote().sendBinary(buf);
		return this;
	}

	private void checkClosed()
	{
		if (!isOpen())
		{
			throw new IllegalStateException("The connection is closed.");
		}
	}

	private static class CloseCode implements CloseReason.CloseCode
	{
		private final int code;

		private CloseCode(int code)
		{
			this.code = code;
		}

		@Override
		public int getCode()
		{
			return code;
		}
	}
}
