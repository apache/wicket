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
import java.io.InterruptedIOException;
import java.util.concurrent.ExecutionException;

import org.apache.wicket.protocol.ws.api.IWebSocketConnection;
import org.apache.wicket.util.lang.Args;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.FutureCallback;
import org.eclipse.jetty.websocket.core.api.WebSocketConnection;

/**
 * A wrapper around Jetty9's native WebSocket.Connection
 *
 * @since 6.2
 */
public class Jetty9WebSocketConnection implements IWebSocketConnection
{
	private final WebSocketConnection connection;

	/**
	 * Constructor.
	 *
	 * @param connection
	 *            the jetty websocket connection
	 */
	public Jetty9WebSocketConnection(WebSocketConnection connection)
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
	public IWebSocketConnection sendMessage(String message) throws IOException
	{
		checkClosed();

		FutureCallback<Void> waiter = new FutureCallback<Void>();
		connection.write(null, waiter, message);
		waitForMessageSent(waiter);
		return this;
	}

	@Override
	public IWebSocketConnection sendMessage(byte[] message, int offset, int length)
		throws IOException
	{
		checkClosed();

		FutureCallback<Void> waiter = new FutureCallback<Void>();
		connection.write(null, new Callback.Empty<Void>(), message, offset, length);
		waitForMessageSent(waiter);
		return this;
	}

	private void waitForMessageSent(FutureCallback<?> waiter) throws IOException
	{
		try
		{
			waiter.get();
		}
		catch (InterruptedException e)
		{
			throw new InterruptedIOException();
		}
		catch (ExecutionException e)
		{
			FutureCallback.rethrow(e);
		}
	}

	private void checkClosed()
	{
		if (!isOpen())
		{
			throw new IllegalStateException("The connection is closed.");
		}
	}
}
