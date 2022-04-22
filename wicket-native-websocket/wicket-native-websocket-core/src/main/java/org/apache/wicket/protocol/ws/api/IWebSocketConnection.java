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

import java.io.IOException;
import java.util.concurrent.Future;

import org.apache.wicket.Application;
import org.apache.wicket.protocol.ws.api.message.IWebSocketPushMessage;
import org.apache.wicket.protocol.ws.api.registry.IKey;

/**
 * Common interface for native WebSocket connections
 *
 * @since 6.0
 */
public interface IWebSocketConnection
{
	/**
	 * @return {@code true} when the underlying native web socket
	 *      connection is still open.
	 */
	boolean isOpen();

	/**
	 * Closes the underlying web socket connection
	 *
	 * @param code
	 *      the status code
	 * @param reason
	 *      the reason to close the connection
	 */
	void close(int code, String reason);

	/**
	 * Sends a text message to the client.
	 *
	 * @param message
	 *      the text message
	 * @return {@code this} object, for chaining methods
	 * @throws IOException when an IO error occurs during the write to the client
	 */
	IWebSocketConnection sendMessage(String message) throws IOException;

    /**
     * Sends a text message to the client in an asynchronous way.
     *
     * @param message
     *      the text message
     * @return a {@link java.util.concurrent.Future} representing the send operation
     *
     */
    Future<Void> sendMessageAsync(String message);

    /**
     * Sends a text message to the client in an asynchronous way.
     *
     * @param message
     *      the text message
     * @param timeout
     *      the timeout for operation
     * @return a {@link java.util.concurrent.Future} representing the send operation
     */
    Future<Void> sendMessageAsync(String message, long timeout);

	/**
	 * Sends a binary message to the client.
	 *
	 * @param message
	 *      the binary message
	 * @param offset
	 *      the offset to read from
	 * @param length
	 *      how much data to read
	 * @return {@code this} object, for chaining methods
	 * @throws IOException when an IO error occurs during the write to the client
	 */
	IWebSocketConnection sendMessage(byte[] message, int offset, int length) throws IOException;

    /**
     * Sends a binary message to the client in an asynchronous way.
     *
     * @param message
     *      the binary message
     * @param offset
     *      the offset to read from
     * @param length
     *      how much data to read
     * @return a {@link java.util.concurrent.Future} representing the send operation
     */
    Future<Void> sendMessageAsync(byte[] message, int offset, int length);

    /**
     * Sends a binary message to the client in an asynchronous way.
     *
     * @param message
     *      the binary message
     * @param offset
     *      the offset to read from
     * @param length
     *      how much data to read
     * @param timeout
     *      the timeout for operation
     * @return a {@link java.util.concurrent.Future} representing the send operation
     */
    Future<Void> sendMessageAsync(byte[] message, int offset, int length, long timeout);

	/**
	 * Broadcasts a push message to the wicket page (and it's components) associated with this
	 * connection. The components can then send messages or component updates to client by adding
	 * them to the target.
	 *
	 * @param message
	 *     the push message to send
	 * @since 6.4
	 */
	void sendMessage(IWebSocketPushMessage message);

	/**
	 * Broadcasts a push message to the wicket page (and it's components) associated with this
	 * connection. The components can then send messages or component updates to client by adding
	 * them to the target. Pushing to client is done asynchronously.
	 *
	 * @param message
	 *     the push message to send
	 *
	 */
	void sendMessageAsync(IWebSocketPushMessage message);


	/**
	 * Broadcasts a push message to the wicket page (and it's components) associated with this
	 * connection. The components can then send messages or component updates to client by adding
	 * them to the target. Pushing to client is done asynchronously.
	 *
	 * @param message
	 *     the push message to send
	 * @param timeout
	 *     the timeout in milliseconds
	 *
	 */
	void sendMessageAsync(IWebSocketPushMessage message, long timeout);

	/**
	 * @return The application for which this WebSocket connection is registered
	 */
	Application getApplication();

	/**
	 * @return The id of the session for which this WebSocket connection is registered
	 */
	String getSessionId();

	/**
	 * @return The registry key for which this WebSocket connection is registered
	 */
	IKey getKey();
}
