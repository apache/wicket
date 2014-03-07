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
package org.apache.wicket.protocol.ws.util.tester;

import org.apache.wicket.Application;
import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.ws.IWebSocketSettings;
import org.apache.wicket.protocol.ws.api.IWebSocketProcessor;
import org.apache.wicket.protocol.ws.api.WebSocketPushBroadcaster;
import org.apache.wicket.protocol.ws.api.message.ConnectedMessage;
import org.apache.wicket.protocol.ws.api.message.IWebSocketPushMessage;
import org.apache.wicket.protocol.ws.api.registry.IKey;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.tester.WicketTester;

/**
 * A helper class to test WebSocket related operations.
 * 
 * @since 6.0
 */
public class WebSocketTester
{
	private final IWebSocketProcessor socketProcessor;

	/**
	 * Constructor.
	 * Prepares a WebSockConnection that will be used to send messages from the client (the test case)
	 * to the server.
	 *
	 * @param page
	 *      the page that may have registered {@link org.apache.wicket.protocol.ws.api.WebSocketBehavior}
	 */
	public WebSocketTester(final WicketTester wicketTester, final Page page)
	{
		Args.notNull(wicketTester, "wicketTester");
		Args.notNull(page, "page");

		WebApplication webApplication = wicketTester.getApplication();
		webApplication.getWicketFilter().setFilterPath("");

		socketProcessor = new TestWebSocketProcessor(wicketTester, page)
		{
			@Override
			protected void onOutMessage(String message)
			{
				WebSocketTester.this.onOutMessage(message);
			}

			@Override
			protected void onOutMessage(byte[] message, int offset, int length)
			{
				WebSocketTester.this.onOutMessage(message, offset, length);
			}
		};
		socketProcessor.onOpen(null);
	}

	/**
	 * Constructor.
	 *
	 * Prepares a WebSockConnection that will be used to send messages from the client (the test case)
	 * to the server.
	 *
	 * @param resourceName
	 *      the name of the shared WebSocketResource that will handle the web socket messages
	 */
	public WebSocketTester(final WicketTester wicketTester, final String resourceName)
	{
		Args.notNull(wicketTester, "wicketTester");
		Args.notNull(resourceName, "resourceName");

		WebApplication webApplication = wicketTester.getApplication();
		webApplication.getWicketFilter().setFilterPath("");

		socketProcessor = new TestWebSocketProcessor(wicketTester, resourceName)
		{
			@Override
			protected void onOutMessage(String message)
			{
				WebSocketTester.this.onOutMessage(message);
			}

			@Override
			protected void onOutMessage(byte[] message, int offset, int length)
			{
				WebSocketTester.this.onOutMessage(message, offset, length);
			}
		};
		socketProcessor.onOpen(null);
	}

	/**
	 * Sends a text message from the client (a test case) to the server
	 * @param message
	 *      the text message to send to the server
	 */
	public void sendMessage(final String message)
	{
		socketProcessor.onMessage(message);
	}


	/**
	 * Sends a binary message from the client (a test case) to the server
	 *
	 * @param message
	 *      the binary message to send to the server
	 * @param offset
	 *      the offset of the binary message to start to read from
	 * @param length
	 *      the length of bytes to read from the binary message
	 */
	public void sendMessage(final byte[] message, final int offset, final int length)
	{
		socketProcessor.onMessage(message, offset, length);
	}

	/**
	 * Broadcasts/pushes a message to specific web socket connection
	 *
	 * @param application
	 *          The application where the web socket connection is registered
	 * @param sessionId
	 *          The id of the http session with which the web socket connection is registered
	 * @param key
	 *          The key with which the web socket connection is registered
	 * @param message
	 *          The message to broadcast/push
	 */
	public void broadcast(Application application, String sessionId, IKey key, IWebSocketPushMessage message)
	{
		IWebSocketSettings webSocketSettings = IWebSocketSettings.Holder.get(application);
		WebSocketPushBroadcaster broadcaster = new WebSocketPushBroadcaster(webSocketSettings.getConnectionRegistry());
		ConnectedMessage wsMessage = new ConnectedMessage(application, sessionId, key);
		broadcaster.broadcast(wsMessage, message);
	}

	/**
	 * Broadcasts/pushes a message to all active web socket connections
	 *
	 * @param application
	 *          The application where the web socket connection is registered
	 * @param message
	 *          The message to broadcast/push
	 */
	public void broadcastAll(Application application, IWebSocketPushMessage message)
	{
		IWebSocketSettings webSocketSettings = IWebSocketSettings.Holder.get(application);
		WebSocketPushBroadcaster broadcaster = new WebSocketPushBroadcaster(webSocketSettings.getConnectionRegistry());
		broadcaster.broadcastAll(application, message);
	}
	
	public void destroy()
	{
		socketProcessor.onClose(0, "Closed by WebSocketTester");
	}

	/**
	 * A callback method which may be overritten to receive messages pushed by the server
	 *
	 * @param message
	 *      the pushed text message from the server
	 */
	protected void onOutMessage(String message)
	{
	}

	/**
	 * A callback method which may be overritten to receive messages pushed by the server
	 *
	 * @param message
	 *      the pushed binary message from the server
	 * @param offset
	 *      the offset of the binary message to start to read from
	 * @param length
	 *      the length of bytes to read from the binary message
	 */
	protected void onOutMessage(byte[] message, int offset, int length)
	{
	}
}
