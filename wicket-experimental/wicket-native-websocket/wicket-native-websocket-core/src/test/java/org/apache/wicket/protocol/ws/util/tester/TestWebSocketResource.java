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

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.wicket.util.string.Strings;
import org.junit.Assert;
import org.apache.wicket.protocol.ws.api.WebSocketRequestHandler;
import org.apache.wicket.protocol.ws.api.WebSocketResource;
import org.apache.wicket.protocol.ws.api.message.BinaryMessage;
import org.apache.wicket.protocol.ws.api.message.ClosedMessage;
import org.apache.wicket.protocol.ws.api.message.ConnectedMessage;
import org.apache.wicket.protocol.ws.api.message.TextMessage;

/**
 *
 */
public class TestWebSocketResource extends WebSocketResource
{
	static final String TEXT = "TestWebSocketResource-text";
	static final String BINARY = "TestWebSocketResource-binary";

	static final AtomicBoolean ON_CONNECT_CALLED = new AtomicBoolean(false);
	static final AtomicBoolean ON_CLOSE_CALLED = new AtomicBoolean(false);

	private final String expectedMessage;

	private final byte[] expectedBinaryMessage;
	private final int expectedOffset;
	private final int expectedLength;

	TestWebSocketResource(String expected)
	{
		this.expectedMessage = expected;

		this.expectedBinaryMessage = null;
		this.expectedOffset = -1;
		this.expectedLength = -1;
	}

	TestWebSocketResource(byte[] message, int offset, int length)
	{
		this.expectedBinaryMessage = message;
		this.expectedOffset = offset;
		this.expectedLength = length;

		this.expectedMessage = null;
	}

	@Override
	protected void onConnect(ConnectedMessage message)
	{
		super.onConnect(message);
		ON_CONNECT_CALLED.set(true);
	}

	@Override
	protected void onClose(ClosedMessage message)
	{
		ON_CLOSE_CALLED.set(true);
		super.onClose(message);
	}

	@Override
	protected void onMessage(WebSocketRequestHandler handler, TextMessage message)
	{
		super.onMessage(handler, message);

		String text = message.getText();
		Assert.assertEquals(expectedMessage, text);
		handler.push(Strings.capitalize(text));
	}

	@Override
	protected void onMessage(WebSocketRequestHandler handler, BinaryMessage binaryMessage)
	{
		super.onMessage(handler, binaryMessage);

		byte[] data = binaryMessage.getData();
		int offset = binaryMessage.getOffset();
		int length = binaryMessage.getLength();

		Assert.assertEquals(expectedBinaryMessage, data);
		Assert.assertEquals(expectedOffset, offset);
		Assert.assertEquals(expectedLength, length);

		handler.push(data, offset, length);
	}
}
