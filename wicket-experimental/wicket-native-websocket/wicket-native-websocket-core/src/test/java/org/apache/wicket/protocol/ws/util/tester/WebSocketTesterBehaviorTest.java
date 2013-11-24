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

import java.io.UnsupportedEncodingException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.wicket.event.IEvent;
import org.apache.wicket.protocol.ws.WebSocketSettings;
import org.apache.wicket.protocol.ws.api.WebSocketPushBroadcaster;
import org.apache.wicket.protocol.ws.api.event.WebSocketPushPayload;
import org.apache.wicket.protocol.ws.api.message.ConnectedMessage;
import org.apache.wicket.protocol.ws.api.message.IWebSocketPushMessage;
import org.apache.wicket.protocol.ws.api.registry.PageIdKey;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for WebSocketTester.
 * Uses WebSocketBehavior.
 *
 * @since 6.0
 */
public class WebSocketTesterBehaviorTest extends Assert
{
	WicketTester tester;

	@Before
	public void before()
	{
		tester = new WicketTester();
	}

	@After
	public void after()
	{
		tester.destroy();
	}

	/**
	 * A simple test that sends and receives a text message.
	 * The page asserts that it received the correct message and then
	 * pushed back the same message but capitalized.
	 */
	@Test
	public void sendTextMessageBehavior()
	{
		final String expectedMessage = "some message";

		WebSocketBehaviorTestPage page = new WebSocketBehaviorTestPage(expectedMessage);
		tester.startPage(page);

		WebSocketTester webSocketTester = new WebSocketTester(tester, page) {
			@Override
			protected void onOutMessage(String message)
			{
				assertEquals(Strings.capitalize(expectedMessage), message);
			}
		};

		webSocketTester.sendMessage(expectedMessage);
		webSocketTester.destroy();
	}

	/**
	 * A simple test that sends and receives a binary message.
	 * The page asserts that it received the correct message, offset and lenght and then
	 * pushes back the same message but capitalized, offset plus 1 and length minus 1.
	 */
	@Test
	public void sendBinaryMessageBehavior() throws UnsupportedEncodingException
	{
		final byte[] expectedMessage = "some message".getBytes("UTF-8");
		final int offset = 1;
		final int length = 2;

		WebSocketBehaviorTestPage page = new WebSocketBehaviorTestPage(expectedMessage, offset, length);
		tester.startPage(page);

		WebSocketTester webSocketTester = new WebSocketTester(tester, page) {
			@Override
			protected void onOutMessage(byte[] message, int off, int len)
			{
				try
				{
					String msg = new String(expectedMessage);
					byte[] pushedMessage = Strings.capitalize(msg).getBytes("UTF-8");

					assertArrayEquals(pushedMessage, message);
					assertEquals(offset + 1, off);
					assertEquals(length - 1, len);

				} catch (UnsupportedEncodingException uex)
				{
					throw new RuntimeException(uex);
				}
			}
		};

		webSocketTester.sendMessage(expectedMessage, offset, length);
		webSocketTester.destroy();
	}

	@Test
	public void serverSideBroadcast()
	{
		final String message = "Broadcasted Message";

		final AtomicBoolean messageReceived = new AtomicBoolean(false);

		WebSocketBehaviorTestPage page = new WebSocketBehaviorTestPage()
		{
			@Override
			public void onEvent(IEvent<?> event)
			{
				super.onEvent(event);

				if (event.getPayload() instanceof WebSocketPushPayload)
				{
					WebSocketPushPayload payload = (WebSocketPushPayload) event.getPayload();

					if (payload.getMessage() instanceof BroadcastMessage)
					{
						BroadcastMessage broadcastMessage = (BroadcastMessage) payload.getMessage();
						if (message.equals(broadcastMessage.getText()))
						{
							messageReceived.set(true);
						}
					}
				}
			}
		};
		tester.startPage(page);
		tester.getSession().bind();

		new WebSocketTester(tester, page);
		WebSocketSettings webSocketSettings = WebSocketSettings.Holder.get(tester.getApplication());
		WebSocketPushBroadcaster broadcaster = new WebSocketPushBroadcaster(webSocketSettings.getConnectionRegistry());
		ConnectedMessage wsMessage = new ConnectedMessage(tester.getApplication(),
				tester.getHttpSession().getId(), new PageIdKey(page.getPageId()));
		broadcaster.broadcast(wsMessage, new BroadcastMessage(message));

		assertEquals(true, messageReceived.get());
	}

	private static class BroadcastMessage implements IWebSocketPushMessage
	{
		private final String message;

		private BroadcastMessage(String message)
		{
			this.message = message;
		}

		public String getText()
		{
			return message;
		}
	}
}
