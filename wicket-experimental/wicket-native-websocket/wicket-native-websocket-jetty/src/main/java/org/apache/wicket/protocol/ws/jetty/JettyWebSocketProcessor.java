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
package org.apache.wicket.protocol.ws.jetty;

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.ws.api.AbstractWebSocketProcessor;
import org.eclipse.jetty.websocket.WebSocket;

/**
 * An {@link org.apache.wicket.protocol.ws.api.IWebSocketProcessor processor} that integrates
 * with Jetty 7.x {@link WebSocket web socket} implementation.
 *
 * @since 6.0
 */
public class JettyWebSocketProcessor extends AbstractWebSocketProcessor
{
	public class JettyWebSocket implements WebSocket.OnTextMessage, WebSocket.OnBinaryMessage
	{
		@Override
		public void onMessage(byte[] bytes, int offset, int length)
		{
			JettyWebSocketProcessor.this.onMessage(bytes, offset, length);
		}

		@Override
		public void onMessage(String message)
		{
			JettyWebSocketProcessor.this.onMessage(message);
		}

		@Override
		public void onOpen(Connection connection)
		{
			JettyWebSocketProcessor.this.onOpen(connection);
		}

		@Override
		public void onClose(int code, String message)
		{
			JettyWebSocketProcessor.this.onClose(code, message);
		}
	}

	/**
	 * Constructor.
	 * 
	 * @param request
	 *      the http request that was used to create the TomcatWebSocketProcessor
	 * @param application
	 *      the current Wicket Application
	 */
	public JettyWebSocketProcessor(final HttpServletRequest request, final WebApplication application)
	{
		super(request, application);
	}


	@Override
	public void onOpen(Object connection)
	{
		if (!(connection instanceof WebSocket.Connection))
		{
			throw new IllegalArgumentException(JettyWebSocketProcessor.class.getName() + " can work only with " + WebSocket.Connection.class.getName());
		}
		onConnect(new JettyWebSocketConnection((WebSocket.Connection) connection, this));
	}
}
