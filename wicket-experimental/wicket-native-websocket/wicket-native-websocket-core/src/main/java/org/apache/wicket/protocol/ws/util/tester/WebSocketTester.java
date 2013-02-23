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

import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.ws.api.IWebSocketProcessor;
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

		socketProcessor = new TestWebSocketProcessor(wicketTester, page) {

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

	public void destroy()
	{
		socketProcessor.onClose(0, "Closed by WicketTester");
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
