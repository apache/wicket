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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.wicket.Application;
import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.mock.MockHttpServletRequest;
import org.apache.wicket.protocol.ws.api.AbstractWebSocketProcessor;
import org.apache.wicket.protocol.ws.api.message.IWebSocketPushMessage;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.tester.WicketTester;

/**
 * An {@link org.apache.wicket.protocol.ws.api.IWebSocketProcessor} used by {@link WebSocketTester}
 *
 * @since 6.0
 */
abstract class TestWebSocketProcessor extends AbstractWebSocketProcessor
{
	/**
	 * Constructor.
	 *
	 * @param page
	 *      the page that may have registered {@link org.apache.wicket.protocol.ws.api.WebSocketBehavior}
	 */
	public TestWebSocketProcessor(final WicketTester wicketTester, final Page page)
	{
		super(createRequest(wicketTester, page), page.getApplication());
	}

	/**
	 * Creates an HttpServletRequest that is needed by AbstractWebSocketProcessor
	 *
	 * @param page
	 *      the page that may have registered {@link org.apache.wicket.protocol.ws.api.WebSocketBehavior}
	 * @return a mock http request
	 */
	private static HttpServletRequest createRequest(final WicketTester wicketTester, final Page page)
	{
		Args.notNull(page, "page");
		Application application = page.getApplication();
		HttpSession httpSession = wicketTester.getHttpSession();
		MockHttpServletRequest request = new MockHttpServletRequest(application, httpSession, null);
		request.addParameter("pageId", page.getId());
		return request;
	}

	/**
	 * Setups TestWebSocketConnection.
	 *
	 * @param connection
	 *      the native connection. Not needed/Ignored.
	 */
	@Override
	public void onOpen(Object connection)
	{
		onConnect(new TestWebSocketConnection() {

			@Override
			protected void onOutMessage(String message)
			{
				TestWebSocketProcessor.this.onOutMessage(message);
			}

			@Override
			protected void onOutMessage(byte[] message, int offset, int length)
			{
				TestWebSocketProcessor.this.onOutMessage(message, offset, length);
			}

			@Override
			public void sendMessage(IWebSocketPushMessage message)
			{
				TestWebSocketProcessor.this.broadcastMessage(message);
			}
		});
	}

	/**
	 * A callback method that is being called when a test message is written to the TestWebSocketConnection
	 *
	 * @param message
	 *      the text message to deliver to the client
	 */
	protected abstract void onOutMessage(String message);

	/**
	 * A callback method that is being called when a binary message is written to the TestWebSocketConnection
	 *
	 * @param message
	 *      the binary message to deliver to the client
	 * @param offset
	 *      the offset of the binary message to start to read from
	 * @param length
	 *      the length of bytes to read from the binary message
	 */
	protected abstract void onOutMessage(byte[] message, int offset, int length);
}
