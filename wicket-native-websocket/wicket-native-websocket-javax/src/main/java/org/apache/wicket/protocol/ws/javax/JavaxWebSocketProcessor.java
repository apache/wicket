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
package org.apache.wicket.protocol.ws.javax;

import java.nio.ByteBuffer;

import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.ws.api.AbstractWebSocketProcessor;

/**
 * An {@link org.apache.wicket.protocol.ws.api.IWebSocketProcessor processor} that integrates with
 * JSR 356 {@link Session web socket} implementation.
 *
 * @since 7.0.0
 */
public class JavaxWebSocketProcessor extends AbstractWebSocketProcessor
{
	/**
	 * Constructor.
	 *
	 * @param session
	 *            the WebSocket session
	 * @param application
	 * @param endpointConfig
	 */
	public JavaxWebSocketProcessor(final Session session, final WebApplication application, EndpointConfig endpointConfig)
	{
		super(new JavaxUpgradeHttpRequest(session, endpointConfig), application);

		onConnect(new JavaxWebSocketConnection(session, this));

		session.addMessageHandler(new StringMessageHandler());
		session.addMessageHandler(new BinaryMessageHandler());
	}


	@Override
	public void onOpen(Object containerConnection)
	{
	}

	private class StringMessageHandler implements MessageHandler.Whole<String>
	{
		@Override
		public void onMessage(String message)
		{
			JavaxWebSocketProcessor.this.onMessage(message);
		}
	}

	private class BinaryMessageHandler implements MessageHandler.Whole<ByteBuffer>
	{
		@Override
		public void onMessage(ByteBuffer message)
		{
			byte[] array = message.array();
			JavaxWebSocketProcessor.this.onMessage(array, 0, array.length);
		}
	}


}
