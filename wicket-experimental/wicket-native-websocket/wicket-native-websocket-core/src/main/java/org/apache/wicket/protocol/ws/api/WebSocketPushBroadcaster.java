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

import static java.util.Collections.singletonList;

import java.util.Collection;

import org.apache.wicket.Application;
import org.apache.wicket.protocol.ws.WebSocketSettings;
import org.apache.wicket.protocol.ws.api.message.ConnectedMessage;
import org.apache.wicket.protocol.ws.api.message.IWebSocketPushMessage;
import org.apache.wicket.protocol.ws.api.registry.IKey;
import org.apache.wicket.protocol.ws.api.registry.IWebSocketConnectionRegistry;
import org.apache.wicket.protocol.ws.concurrent.Executor;
import org.apache.wicket.util.lang.Args;

/**
 * Allows pushing events for processing to Pages that have active web sockets.
 *
 * @since 6.4
 * @author Mikko Tiihonen
 */
public class WebSocketPushBroadcaster
{
	private final IWebSocketConnectionRegistry registry;

	public WebSocketPushBroadcaster(IWebSocketConnectionRegistry registry)
	{
		Args.notNull(registry, "registry");

		this.registry = registry;
	}

	/**
	 * Processes the given message in the page and session identified by the given Web Socket connection.
	 * The message is sent as an event to the Page and components of the session allowing the components
	 * to be updated.
	 *
	 * This method can be invoked from any thread, even a non-wicket thread. By default all processing
	 * is done in the caller thread. Use
	 * {@link WebSocketSettings#setWebSocketPushMessageExecutor(org.apache.wicket.protocol.ws.concurrent.Executor)}
	 * to move processing to background threads.
	 *
	 * If the given connection is no longer open then the broadcast is silently ignored.
	 *
	 * @param connection
	 *			The Web Socket connection that identifies the page and session
	 * @param message
	 *			The push message event
	 */
	public void broadcast(ConnectedMessage connection, IWebSocketPushMessage message)
	{
		Args.notNull(connection, "connection");
		Args.notNull(message, "message");

		Application application = connection.getApplication();
		String sessionId = connection.getSessionId();
		IKey key = connection.getKey();
		IWebSocketConnection wsConnection = registry.getConnection(application, sessionId, key);
		if (wsConnection == null)
		{
			return;
		}
		process(application, singletonList(wsConnection), message);
	}

	/**
	 * Processes the given message in all pages that have active Web Socket connections.
	 * The message is sent as an event to the Page and components of the session allowing the components
	 * to be updated.
	 *
	 * This method can be invoked from any thread, even a non-wicket thread. By default all processing
	 * is done in the caller thread. Use
	 * {@link WebSocketSettings#setWebSocketPushMessageExecutor(org.apache.wicket.protocol.ws.concurrent.Executor)}
	 * to move processing to background threads.
	 *
	 * If some connections are not in valid state they are silently ignored.
	 *
	 * @param application
	 *			The wicket application
	 * @param message
	 *			The push message event
	 */
	public void broadcastAll(Application application, IWebSocketPushMessage message)
	{
		Args.notNull(application, "application");
		Args.notNull(message, "message");

		Collection<IWebSocketConnection> wsConnections = registry.getConnections(application);
		if (wsConnections == null)
		{
			return;
		}
		process(application, wsConnections, message);
	}

	private void process(final Application application, final Collection<IWebSocketConnection> wsConnections,
	                     final IWebSocketPushMessage message)
	{
		WebSocketSettings webSocketSettings = WebSocketSettings.Holder.get(application);
		Executor executor = webSocketSettings.getWebSocketPushMessageExecutor();
		for (final IWebSocketConnection wsConnection : wsConnections)
		{
			executor.run(new Runnable()
			{
				@Override
				public void run()
				{
					wsConnection.sendMessage(message);
				}
			});
		}
	}
}
