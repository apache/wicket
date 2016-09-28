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

import static org.hamcrest.CoreMatchers.is;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.wicket.request.cycle.AbstractRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
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
 * @since 6.18.0
 */
public class WebSocketTesterRequestCycleListenerTest extends Assert
{
	private final AtomicBoolean beginRequestCalled = new AtomicBoolean(false);
	private final AtomicBoolean endRequestCalled = new AtomicBoolean(false);
	private final AtomicBoolean detachCalled = new AtomicBoolean(false);

	private WicketTester tester;

	@Before
	public void before()
	{
		tester = new WicketTester();
		tester.getApplication().getRequestCycleListeners().add(new AbstractRequestCycleListener()
		{
			@Override
			public void onBeginRequest(RequestCycle cycle)
			{
				beginRequestCalled.set(true);
			}

			@Override
			public void onEndRequest(RequestCycle cycle)
			{
				endRequestCalled.set(true);
			}

			@Override
			public void onDetach(RequestCycle cycle)
			{
				detachCalled.set(true);
			}
		});
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
	public void verifyRequestCycleListeners()
	{
		final String expectedMessage = "some message";

		WebSocketBehaviorTestPage page = new WebSocketBehaviorTestPage(expectedMessage);
		tester.startPage(page);

		// reset the variables after starting the page (no WebSocket related request)
		beginRequestCalled.set(false);
		endRequestCalled.set(false);
		detachCalled.set(false);

		// broadcasts WebSocket.ConnectedMessage and notifies the listeners
		WebSocketTester webSocketTester = new WebSocketTester(tester, page) {
			@Override
			protected void onOutMessage(String message)
			{
				assertEquals(Strings.capitalize(expectedMessage), message);
			}
		};

		// assert and reset
		assertThat(beginRequestCalled.compareAndSet(true, false), is(true));
		assertThat(endRequestCalled.compareAndSet(true, false), is(true));
		assertThat(detachCalled.compareAndSet(true, false), is(true));

		// broadcasts WebSocket.TextMessage and notifies the listeners
		webSocketTester.sendMessage(expectedMessage);

		assertThat(beginRequestCalled.get(), is(true));
		assertThat(endRequestCalled.get(), is(true));
		assertThat(detachCalled.get(), is(true));

		webSocketTester.destroy();
	}

}
