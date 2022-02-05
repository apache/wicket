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

import java.nio.ByteBuffer;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.ws.WebSocketSettings;
import org.apache.wicket.protocol.ws.api.registry.IKey;

/**
 * Processes web socket messages.
 *
 * @since 6.0
 */
public interface IWebSocketProcessor
{
	/**
	 * Called when then {@link org.apache.wicket.protocol.ws.api.IWebSocketSession} is being opened: to allow to configure
	 * the underlying web socket session.
	 *
	 * @param webSocketSession
	 * 			the {@link org.apache.wicket.protocol.ws.api.IWebSocketSession}
	 * @param application
	 *          the {@link org.apache.wicket.protocol.http.WebApplication}
	 */
	default void onOpen(IWebSocketSession webSocketSession, final WebApplication application) {
		// find the current org.apache.wicket.protocol.ws.api.IWebSocketSessionConfigurer and use it
		// in order to configure the session
		WebSocketSettings.Holder.get(application).getSocketSessionConfigurer().configureSession(webSocketSession);
	}

	/**
	 * Called when remote peer answers to ping with pong message.
	 *
	 * @param byteBuffer Contains application specific content
	 */
	void onPong(ByteBuffer byteBuffer);

	/**
	 * Called when a text message arrives from the client
	 *
	 * @param message
	 *      the text message from the client
	 */
	void onMessage(final String message);

	/**
	 * Called when a binary message arrives from the client
	 *
	 * @param data
	 *      the binary message from the client
	 * @param offset
	 *      the offset to read from
	 * @param length
	 *      how much data to read
	 */
	void onMessage(byte[] data, int offset, int length);

	/**
	 * A client successfully has made a web socket connection.
	 *
	 * @param containerConnection
	 *      the web socket connection to use to communicate with the client
	 */
	void onOpen(Object containerConnection);

	/**
	 * A notification after the close of the web socket connection.
	 * The connection could be closed by either the client or the server
	 *
	 * @param closeCode
	 *   		The close code
	 * @param message
	 *          the message
	 */
	void onClose(int closeCode, String message);

	/**
	 * A notification after a communication error.
	 *
	 * @param t
	 *      The throwable for the communication problem
	 */
	void onError(Throwable t);
}
