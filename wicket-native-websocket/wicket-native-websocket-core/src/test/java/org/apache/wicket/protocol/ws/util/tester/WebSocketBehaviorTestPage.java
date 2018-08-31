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

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.ws.api.WebSocketBehavior;
import org.apache.wicket.protocol.ws.api.WebSocketRequestHandler;
import org.apache.wicket.protocol.ws.api.message.BinaryMessage;
import org.apache.wicket.protocol.ws.api.message.IWebSocketPushMessage;
import org.apache.wicket.protocol.ws.api.message.TextMessage;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.string.Strings;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A page used for {@link WebSocketTesterBehaviorTest}
 *
 * @since 6.0
 */
class WebSocketBehaviorTestPage extends WebPage implements IMarkupResourceStreamProvider
{
	WebSocketBehaviorTestPage()
	{
		add(new WebSocketBehavior() {});
	}

	WebSocketBehaviorTestPage(final String expectedMessage)
	{
		add(new WebSocketBehavior()
		{
			@Override
			protected void onMessage(WebSocketRequestHandler handler, TextMessage message)
			{
				// assert the inbould message
				assertEquals(expectedMessage, message.getText());

				// now send an outbound message
				handler.push(Strings.capitalize(expectedMessage));
			}
		});
	}
	
	WebSocketBehaviorTestPage(final byte[] message, final int offset, final int length)
	{
		add(new WebSocketBehavior()
		{
			@Override
			protected void onMessage(WebSocketRequestHandler handler, BinaryMessage binaryMessage)
			{
				assertArrayEquals(binaryMessage.getData(), message);
				assertEquals(offset, binaryMessage.getOffset());
				assertEquals(length, binaryMessage.getLength());
				
				String msg = new String(message);
				byte[] pushedMessage = Strings.capitalize(msg).getBytes();
				
				handler.push(pushedMessage, offset + 1, length - 1);
			}
		});
	}

	WebSocketBehaviorTestPage(final WebSocketTesterBehaviorTest.BroadcastMessage expectedMessage)
	{
		add(new WebSocketBehavior()
		{
			@Override
			protected void onPush(WebSocketRequestHandler handler, IWebSocketPushMessage message)
			{
				assertTrue(message instanceof WebSocketTesterBehaviorTest.BroadcastMessage);
				WebSocketTesterBehaviorTest.BroadcastMessage broadcastMessage = (WebSocketTesterBehaviorTest.BroadcastMessage) message;
				assertSame(expectedMessage, broadcastMessage);

				String pushedMessage = broadcastMessage.getText().toUpperCase();

				handler.push(pushedMessage);
			}
		});
	}

	@Override
	public IResourceStream getMarkupResourceStream(MarkupContainer container, Class<?> containerClass)
	{
		return new StringResourceStream("<html/>");
	}
}
