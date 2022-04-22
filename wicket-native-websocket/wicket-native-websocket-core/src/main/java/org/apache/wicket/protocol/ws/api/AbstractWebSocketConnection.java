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
package org.apache.wicket.protocol.ws.api;

import org.apache.wicket.Application;
import org.apache.wicket.protocol.ws.api.message.IWebSocketPushMessage;
import org.apache.wicket.protocol.ws.api.registry.IKey;
import org.apache.wicket.util.lang.Args;

import java.util.concurrent.Future;

/**
 * Abstract class handling the Web Socket broadcast messages.
 */
public abstract class AbstractWebSocketConnection implements IWebSocketConnection
{

	private final String applicationName;
	private final String sessionId;
	private final IKey key;

	private final AbstractWebSocketProcessor webSocketProcessor;

	/**
	 * Constructor.
	 *
	 * @param webSocketProcessor
	 *      the web socket processor to delegate to
	 */
	public AbstractWebSocketConnection(AbstractWebSocketProcessor webSocketProcessor)
	{
		this.applicationName = webSocketProcessor.getApplication().getName();
		this.sessionId = webSocketProcessor.getSessionId();
		this.key = webSocketProcessor.getRegistryKey();
		this.webSocketProcessor = Args.notNull(webSocketProcessor, "webSocketProcessor");
	}

	@Override
	public void sendMessage(IWebSocketPushMessage message)
	{
		webSocketProcessor.broadcastMessage(message, this, false, -1);
	}

	@Override
	public void sendMessageAsync(IWebSocketPushMessage message)
	{
		webSocketProcessor.broadcastMessage(message, this, true, -1);
	}

	@Override
	public void sendMessageAsync(IWebSocketPushMessage message, long timeout)
	{
		webSocketProcessor.broadcastMessage(message, this, true, timeout);
	}

	@Override
	public Application getApplication()
	{
		return Application.get(applicationName);
	}

	@Override
	public String getSessionId()
	{
		return sessionId;
	}

	@Override
	public IKey getKey()
	{
		return key;
	}
}
