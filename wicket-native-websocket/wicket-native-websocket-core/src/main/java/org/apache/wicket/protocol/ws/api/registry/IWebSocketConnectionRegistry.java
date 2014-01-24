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
	 * @param application
	 *      the web application to look in
	 * @param sessionId
	 *      the web socket client session id
	 * @param key
	 *      the web socket client key
	 * @return the web socket connection used by a client from the specified coordinates
	 */
	IWebSocketConnection getConnection(Application application, String sessionId, IKey key);


	/**
	 * @param application
	 *            the web application to look in
	 * @return collection of web socket connection used by any client connected to specified application
	 */
	Collection<IWebSocketConnection> getConnections(Application application);

	/**
	 * Adds a new connection into the registry at the specified coordinates (application+session+page)
	 *
	 * @param application
	 *      the web application to look in
	 * @param sessionId
	 *      the web socket client session id
	 * @param key
	 *      the web socket client key
	 * @param connection
	 *      the web socket connection to add
	 */
	void setConnection(Application application, String sessionId, IKey key, IWebSocketConnection connection);

	/**
	 * Removes a web socket connection from the registry at the specified coordinates (application+session+page)
	 *
	 * @param application
	 *      the web application to look in
	 * @param sessionId
	 *      the web socket client session id
	 * @param key
	 *      the web socket client key
	 */
	void removeConnection(Application application, String sessionId, IKey key);
}
