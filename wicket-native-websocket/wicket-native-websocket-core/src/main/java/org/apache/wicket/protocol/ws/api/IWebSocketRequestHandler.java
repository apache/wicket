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

import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.request.ILoggableRequestHandler;

import java.util.concurrent.Future;

/**
 * An interface for outbound communication with web socket clients
 *
 * @since 6.0
 */
public interface IWebSocketRequestHandler extends IPartialPageRequestHandler, ILoggableRequestHandler
{
	/**
	 * Pushes a text message to the client.
	 *
	 * @param message
	 *      the text message to push to the client if the web socket connection is open
	 */
	void push(CharSequence message);

	/**
	 * Pushes a text message to the client in an asynchronous way.
	 *
	 * @param message
	 *      the text message to push to the client if the web socket connection is open
	 * @return
	 *      a {@link java.util.concurrent.Future} representing the send operation. Or null if connection is closed.
	 */
	Future<Void> pushAsync(CharSequence message);

	/**
	 * Pushes a text message to the client in an asynchronous way.
	 *
	 * @param message
	 *      the text message to push to the client if the web socket connection is open
	 * @param timeout
	 *      the timeout for operation
	 * @return
	 * 		a {@link java.util.concurrent.Future} representing the send operation. Or null if connection is closed.
	 */
	Future<Void> pushAsync(CharSequence message, long timeout);

	/**
	 * Pushes a binary message to the client.
	 *
	 * @param message
	 *      the binary message to push to the client if the web socket connection is open
	 * @param offset
	 *      the offset to start to read from the message
	 * @param length
	 *      how many bytes to read from the message
	 */
	void push(byte[] message, int offset, int length);

	/**
	 * Pushes a binary message to the client.
	 *
	 * @param message
	 *      the binary message to push to the client if the web socket connection is open
	 * @param offset
	 *      the offset to start to read from the message
	 * @param length
	 *      how many bytes to read from the message
	 * @return
	 * 		a {@link java.util.concurrent.Future} representing the send operation. Or null if connection is closed.
	 */
	Future<Void> pushAsync(byte[] message, int offset, int length);

	/**
	 * Pushes a binary message to the client.
	 *
	 * @param message
	 *      the binary message to push to the client if the web socket connection is open
	 * @param offset
	 *      the offset to start to read from the message
	 * @param length
	 *      how many bytes to read from the message
	 * @param timeout
	 *      the timeout for operation
	 * @return
	 * 		a {@link java.util.concurrent.Future} representing the send operation. Or null if connection is closed.
	 */
	Future<Void> pushAsync(byte[] message, int offset, int length, long timeout);
}
