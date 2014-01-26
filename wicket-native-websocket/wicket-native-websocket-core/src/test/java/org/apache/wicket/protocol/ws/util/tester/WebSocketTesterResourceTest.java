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

import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.protocol.http.WebApplication;
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
public class WebSocketTesterResourceTest extends Assert
{
	private static final String EXPECTED_TEXT = "expected text";
	private static final byte[] EXPECTED_BINARY = new byte[] {1, 2, 3};
	private static final int    EXPECTED_OFFSET = 1;
	private static final int    EXPECTED_LENGTH = 1;

	private static final AtomicBoolean ON_OUT_TEXT_CALLED = new AtomicBoolean(false);
	private static final AtomicBoolean ON_OUT_BINARY_CALLED = new AtomicBoolean(false);

	WicketTester tester;

	@Before
	public void before()
	{
		TestWebSocketResource.ON_CONNECT_CALLED.set(false);
		TestWebSocketResource.ON_CLOSE_CALLED.set(false);
		ON_OUT_BINARY_CALLED.set(false);
		ON_OUT_TEXT_CALLED.set(false);

		WebApplication application = new MockApplication()
		{
			@Override
			protected void init()
			{
				super.init();

				getSharedResources().add(TestWebSocketResource.TEXT,
						new TestWebSocketResource(EXPECTED_TEXT));

				getSharedResources().add(TestWebSocketResource.BINARY,
						new TestWebSocketResource(EXPECTED_BINARY, EXPECTED_OFFSET, EXPECTED_LENGTH));
			}
		};
		tester = new WicketTester(application);
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
	public void sendTextMessage()
	{
		assertFalse(TestWebSocketResource.ON_CONNECT_CALLED.get());

		WebSocketTester webSocketTester = new WebSocketTester(tester, TestWebSocketResource.TEXT)
		{
			@Override
			protected void onOutMessage(String message)
			{
				ON_OUT_TEXT_CALLED.set(true);
				assertEquals(Strings.capitalize(EXPECTED_TEXT), message);
			}
		};

		assertTrue(TestWebSocketResource.ON_CONNECT_CALLED.get());
		assertFalse(ON_OUT_TEXT_CALLED.get());

		webSocketTester.sendMessage(EXPECTED_TEXT);
		assertTrue(ON_OUT_TEXT_CALLED.get());

		assertFalse(TestWebSocketResource.ON_CLOSE_CALLED.get());
		webSocketTester.destroy();
		assertTrue(TestWebSocketResource.ON_CLOSE_CALLED.get());
	}

	/**
	 * A simple test that sends and receives a text message.
	 * The page asserts that it received the correct message and then
	 * pushed back the same message but capitalized.
	 */
	@Test
	public void sendBinaryMessage()
	{
		assertFalse(TestWebSocketResource.ON_CONNECT_CALLED.get());

		WebSocketTester webSocketTester = new WebSocketTester(tester, TestWebSocketResource.BINARY)
		{
			@Override
			protected void onOutMessage(byte[] message, int offset, int length)
			{
				ON_OUT_BINARY_CALLED.set(true);
				Assert.assertArrayEquals(EXPECTED_BINARY, message);
				Assert.assertEquals(offset, offset);
				Assert.assertEquals(length, length);
			}
		};

		assertTrue(TestWebSocketResource.ON_CONNECT_CALLED.get());
		assertFalse(ON_OUT_BINARY_CALLED.get());

		webSocketTester.sendMessage(EXPECTED_BINARY, EXPECTED_OFFSET, EXPECTED_LENGTH);
		assertTrue(ON_OUT_BINARY_CALLED.get());

		assertFalse(TestWebSocketResource.ON_CLOSE_CALLED.get());
		webSocketTester.destroy();
		assertTrue(TestWebSocketResource.ON_CLOSE_CALLED.get());
	}
}
