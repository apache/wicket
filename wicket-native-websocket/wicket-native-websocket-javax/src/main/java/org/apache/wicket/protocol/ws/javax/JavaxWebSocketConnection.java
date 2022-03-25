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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

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

	private final AtomicBoolean alive = new AtomicBoolean(false);
	private final AtomicLong lastTimeAlive = new AtomicLong(System.currentTimeMillis());

	/**
	 * Constructor.
	 *
	 * @param session
	 *            the WebSocket session
	 */
	public JavaxWebSocketConnection(Session session, AbstractWebSocketProcessor webSocketProcessor)
	{
		super(webSocketProcessor);
		this.session = Args.notNull(session, "session");
		setAlive(true);
	}

	@Override
	public long getLastTimeAlive()
	{
		return lastTimeAlive.get();
	}

	@Override
	public boolean isAlive()
	{
		return alive.get();
	}

	@Override
	public void setAlive(boolean alive)
	{
		if (alive)
		{
			// is connection if alive we set the timestamp.
			this.lastTimeAlive.set(System.currentTimeMillis());
		}
		this.alive.set(alive);
	}

	@Override
	public synchronized void terminate(String reason)
	{
		close(CloseReason.CloseCodes.CLOSED_ABNORMALLY.getCode(), reason);
	}

	@Override
	public void ping() throws IOException
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Pinging connection {}", getKey());
		}
		ByteBuffer buf = ByteBuffer.wrap(new byte[]{0xA});
		session.getBasicRemote().sendPing(buf);
	}

	@Override
	public void pong() throws IOException
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Sending unidirectional pon for connection {}", getKey());
		}
		ByteBuffer buf = ByteBuffer.wrap(new byte[]{0xA});
		session.getBasicRemote().sendPong(buf);
	}

	@Override
	public void onPong(ByteBuffer byteBuffer)
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Pong receive for {} with contents {}", getKey(), byteBuffer.array());
		}
		// we received pong answer from remote peer. Thus, connection is alive
		setAlive(true);
	}

	@Override
	public boolean isOpen()
	{
		return session.isOpen();
	}

	@Override
	public synchronized void close(int code, String reason)
	{
		if (isOpen())
		{
			try
			{
				session.close(new CloseReason(new CloseCode(code), reason));
			}
			catch (IOException iox)
			{
				LOG.error("An error occurred while closing WebSocket session", iox);
			}
		}
	}

	@Override
	public synchronized IWebSocketConnection sendMessage(String message) throws IOException
	{
		checkClosed();

		session.getBasicRemote().sendText(message);
		return this;
	}

	@Override
	public synchronized IWebSocketConnection sendMessage(byte[] message, int offset, int length)
		throws IOException
	{
		checkClosed();

		ByteBuffer buf = ByteBuffer.wrap(message, offset, length);
		session.getBasicRemote().sendBinary(buf);
		return this;
	}

	@Override
	public IWebSocketConnection sendMessage(byte[] message) throws IOException {
		checkClosed();

		ByteBuffer buf = ByteBuffer.wrap(message);
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
