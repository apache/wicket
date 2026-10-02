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
package org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.dynamic;

import java.io.IOException;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.protocol.ws.WebSocketSettings;
import org.apache.wicket.protocol.ws.api.IWebSocketConnection;
import org.apache.wicket.protocol.ws.api.registry.IKey;
import org.apache.wicket.protocol.ws.api.registry.PageIdKey;

/**
 * A message pushed to a {@link DynamicDataTable} over a web socket connection, handled in the
 * browser without a round trip to the server.
 * <p>
 * The message is sent as the JSON its implementation serializes to: {@code tableId},
 * {@code typeId} and every other property of the bean. The table it addresses hands it to the
 * function registered for its {@link IWebSocketMessageType type}, if the table
 * {@link DynamicDataTable#setPushEnabled(boolean) accepts pushes}. Every property is sent to the
 * browser, so one that must not reach it has to be left out of the bean.
 * <p>
 * The serializer finds {@code tableId} and {@code typeId} by reflection, like every other
 * property. Jackson reads them through {@link #getTableId()} and {@link #getTypeId()}; a
 * serializer reading fields, such as Gson, needs fields of these names. An implementation should
 * therefore hold both in fields named {@code tableId} and {@code typeId}, rather than compute
 * them in the getters.
 * <p>
 * Sending needs {@code org.apache.wicket:wicket-native-websocket-core}, which is an optional
 * dependency of wicket-extensions.
 *
 * @since 11.0.0
 */
public interface IWebSocketLightWeightMessage
{
	/**
	 * @return the markup id of the table the message is addressed to
	 */
	String getTableId();

	/**
	 * @return the {@link IWebSocketMessageType#getTypeId() id of the type} whose function handles
	 *         the message
	 */
	String getTypeId();

	/**
	 * Sends this message, serialized with Jackson, over the web socket connection of the page the
	 * component is on.
	 * <p>
	 * The connection is looked up in the current session, so this has to be called in a request
	 * of the session the page belongs to, not from a background thread.
	 *
	 * @param component
	 *            a component on the page showing the table
	 * @return {@code false} if the page has no open web socket connection, {@code true} if the
	 *         message was sent
	 * @throws IOException
	 *             if the message cannot be sent
	 */
	default boolean send(Component component) throws IOException
	{
		Page page = component.getPage();
		String sessionId = page.getSession().getId();
		if (sessionId == null)
		{
			return false;
		}
		return send(page.getApplication(), sessionId, new PageIdKey(page.getPageId()));
	}

	/**
	 * Sends this message, serialized with Jackson, over a registered web socket connection.
	 * <p>
	 * Needs no request, so it can be called from a background thread, with the application,
	 * session id and key captured when the connection was opened, for example in
	 * {@link org.apache.wicket.protocol.ws.api.WebSocketBehavior#onConnect}.
	 *
	 * @param application
	 *            the application the connection belongs to
	 * @param sessionId
	 *            the id of the session the connection belongs to
	 * @param key
	 *            the key the connection is registered with, for a page a {@link PageIdKey}
	 * @return {@code false} if there is no such open connection, {@code true} if the message was
	 *         sent
	 * @throws IOException
	 *             if the message cannot be sent
	 */
	default boolean send(Application application, String sessionId, IKey key) throws IOException
	{
		IWebSocketConnection connection = WebSocketSettings.Holder.get(application)
			.getConnectionRegistry()
			.getConnection(application, sessionId, key);
		if (connection == null || !connection.isOpen())
		{
			return false;
		}
		send(connection);
		return true;
	}

	/**
	 * Sends this message, serialized with Jackson.
	 *
	 * @param connection
	 *            the connection to the browser showing the table
	 * @throws IOException
	 *             if the message cannot be sent
	 * @see JacksonJsonSerializer
	 */
	default void send(IWebSocketConnection connection) throws IOException
	{
		send(connection, JacksonJsonSerializer.INSTANCE);
	}

	/**
	 * Sends this message.
	 *
	 * @param connection
	 *            the connection to the browser showing the table
	 * @param serializer
	 *            the serializer turning this message into JSON
	 * @throws IOException
	 *             if the message cannot be sent
	 */
	default void send(IWebSocketConnection connection, IJsonSerializer serializer)
		throws IOException
	{
		connection.sendMessage(serializer.toJson(this));
	}
}
