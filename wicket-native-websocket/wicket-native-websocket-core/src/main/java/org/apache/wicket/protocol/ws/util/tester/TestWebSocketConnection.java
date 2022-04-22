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
package org.apache.wicket.protocol.ws.util.tester;

import java.io.IOException;
import java.util.concurrent.Future;

import org.apache.wicket.Application;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.ws.api.IWebSocketConnection;
import org.apache.wicket.protocol.ws.api.message.IWebSocketPushMessage;
import org.apache.wicket.protocol.ws.api.registry.IKey;

/**
 * A WebSocketConnection used for the testing.
 *
 * @since 6.0
 */
abstract class TestWebSocketConnection implements IWebSocketConnection
{
	private final WebApplication application;
	private final String sessionId;
	private final IKey registryKey;
	private boolean isOpen = true;

	public TestWebSocketConnection(WebApplication application, String sessionId, IKey registryKey)
	{
		this.application = application;
		this.sessionId = sessionId;
		this.registryKey = registryKey;
	}

	@Override
	public boolean isOpen()
	{
		return isOpen;
	}

	@Override
	public void close(int code, String reason)
	{
		isOpen = false;
	}

	@Override
	public IWebSocketConnection sendMessage(String message) throws IOException
	{
		checkOpenness();
		onOutMessage(message);
		return this;
	}

    @Override
    public Future<Void> sendMessageAsync(String message)
    {
        return sendMessageAsync(message, -1);
    }

    @Override
    public Future<Void> sendMessageAsync(String message, long timeOut)
    {
        checkOpenness();
        onOutMessage(message);
        return null;
    }

    @Override
	public IWebSocketConnection sendMessage(byte[] message, int offset, int length) throws IOException
	{
		checkOpenness();
		onOutMessage(message, offset, length);
		return this;
	}

    @Override
    public Future<Void> sendMessageAsync(byte[] message, int offset, int length)
    {
        return sendMessageAsync(message, offset, length, -1);
    }

    @Override
    public Future<Void> sendMessageAsync(byte[] message, int offset, int length, long timeout)
    {
        checkOpenness();
        onOutMessage(message, offset, length);
        return null;
    }

	/**
	 * A callback method that is called when a text message should be send to the client
	 *
	 * @param message
	 *      the text message to deliver
	 */
	protected abstract void onOutMessage(String message);

	/**
	 * A callback method that is called when a text message should be send to the client
	 *
	 * @param message
	 *      the binary message to deliver to the client
	 * @param offset
	 *      the offset of the binary message to start to read from
	 * @param length
	 *      the length of bytes to read from the binary message
	 */
	protected abstract void onOutMessage(byte[] message, int offset, int length);

	private void checkOpenness()
	{
		if (isOpen() == false)
		{
			throw new IllegalStateException("The connection is closed!");
		}
	}

	@Override
	public Application getApplication()
	{
		return application;
	}

	@Override
	public String getSessionId()
	{
		return sessionId;
	}

	@Override
	public IKey getKey()
	{
		return registryKey;
	}
}
