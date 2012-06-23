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

/**
 * Processes web socket messages.
 *
 * @since 6.0
 */
public interface IWebSocketProcessor
{
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
	 * @param message
	 */
	void onClose(int closeCode, String message);
}
