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
package org.apache.wicket.protocol.ws.api.registry;

import java.util.Collection;

import jakarta.annotation.Nonnull;

import org.apache.wicket.Application;
import org.apache.wicket.protocol.ws.api.IWebSocketConnection;

/**
 * Tracks all currently connected WebSocket clients
 *
 * @since 6.0
 */
public interface IWebSocketConnectionRegistry
{
	/**
	 * Interface allowing to filter web-sockets connections. This could be used for use cases like the
	 * following: you need to deliver messages to all page instances satisfying certain conditions (e.g.
	 * they contain some progress reporting component).
	 */
	interface IConnectionsFilter
	{

		boolean accept(String sessionId, IKey key);

	}

	/**
	 * @param application
	 *      the web application to look in
	 * @param sessionId
	 *      the http session id
	 * @param key
	 *      the web socket client key
	 * @return the web socket connection used by a client from the specified coordinates
	 */
	IWebSocketConnection getConnection(@Nonnull Application application, @Nonnull String sessionId, @Nonnull IKey key);

	/**
	 * @param application
	 *            the web application to look in
	 * @param sessionId
	 *            the http session id
	 * @return collection of web socket connections used by a client with the given session id
	 */
	Collection<IWebSocketConnection> getConnections(@Nonnull Application application, @Nonnull String sessionId);


	/**
	 *
	 * @param application
	 * 			 the web application to look in
	 * @param connectionsFilter
	 * 			the {@link org.apache.wicket.protocol.ws.api.registry.IWebSocketConnectionRegistry.IConnectionsFilter}
	 *
	 * @return collection of web socket connections that match certain filter
	 */
	Collection<IWebSocketConnection> getConnections(@Nonnull Application application, @Nonnull IConnectionsFilter connectionsFilter);


	/**
	 * @param application
	 *            the web application to look in
	 * @return collection of web socket connection used by any client connected to specified application
	 */
	Collection<IWebSocketConnection> getConnections(@Nonnull Application application);

	/**
	 * Adds a new connection into the registry at the specified coordinates (application+session+page)
	 *
	 * @param application
	 *      the web application to look in
	 * @param sessionId
	 *      the http session id
	 * @param key
	 *      the web socket client key
	 * @param connection
	 *      the web socket connection to add
	 */
	void setConnection(@Nonnull Application application, @Nonnull String sessionId, @Nonnull IKey key, IWebSocketConnection connection);

	/**
	 * Removes a web socket connection from the registry at the specified coordinates (application+session+page)
	 *
	 * @param application
	 *      the web application to look in
	 * @param sessionId
	 *      the http session id
	 * @param key
	 *      the web socket client key
	 */
	void removeConnection(Application application, String sessionId, IKey key);
}
