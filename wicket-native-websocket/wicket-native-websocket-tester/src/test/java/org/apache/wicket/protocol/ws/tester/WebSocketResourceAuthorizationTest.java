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
package org.apache.wicket.protocol.ws.tester;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.wicket.authorization.IAuthorizationStrategy;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.ws.WebSocketSettings;
import org.apache.wicket.protocol.ws.api.ConnectionRejected;
import org.apache.wicket.protocol.ws.api.WebSocketConnectionOriginFilter;
import org.apache.wicket.protocol.ws.api.message.IWebSocketPushMessage;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests that the IAuthorizationStrategy decides who may use a shared WebSocketResource.
 *
 * A WebSocketResource serves nothing over HTTP, so a WebSocket connection is the only way to reach
 * it, and the strategy is asked once, when that connection is opened. A refused client is
 * disconnected and reaches none of the callbacks.
 */
class WebSocketResourceAuthorizationTest
{
	private static final String EXPECTED_TEXT = "expected text";

	private RecordingAuthorizationStrategy strategy;
	private TestWebSocketResource resource;
	private WicketTester tester;

	@BeforeEach
	public void before()
	{
		TestWebSocketResource.resetCallbacks();

		strategy = new RecordingAuthorizationStrategy();
		resource = new TestWebSocketResource(EXPECTED_TEXT);

		WebApplication application = new MockApplication()
		{
			@Override
			protected void init()
			{
				super.init();

				getSecuritySettings().setAuthorizationStrategy(strategy);
				getSharedResources().add(TestWebSocketResource.TEXT, resource);
			}
		};
		tester = new WicketTester(application);
		tester.getSession().bind();
	}

	@AfterEach
	public void after()
	{
		tester.destroy();
		TestWebSocketResource.resetCallbacks();
	}

	/**
	 * The control: an allowed client drives the resource exactly as before, and the strategy was
	 * asked about the very instance that was registered, with parameters that are empty rather than
	 * the connection parameters of the handshake.
	 */
	@Test
	public void anAuthorizedCallerDrivesTheResource()
	{
		AtomicBoolean echoed = new AtomicBoolean(false);

		WebSocketTester webSocketTester = new WebSocketTester(tester, TestWebSocketResource.TEXT)
		{
			@Override
			protected void onOutMessage(String message)
			{
				assertEquals(Strings.capitalize(EXPECTED_TEXT), message);
				echoed.set(true);
			}
		};

		assertEquals(1, strategy.resources.size());
		assertSame(resource, strategy.resources.get(0));
		assertNotNull(strategy.parameters.get(0));
		assertTrue(strategy.parameters.get(0).isEmpty());

		assertTrue(TestWebSocketResource.ON_CONNECT_CALLED.get());
		assertTrue(webSocketTester.isConnectionOpen());

		webSocketTester.sendMessage(EXPECTED_TEXT);
		assertTrue(TestWebSocketResource.ON_MESSAGE_CALLED.get());
		assertTrue(echoed.get());

		webSocketTester.destroy();
		assertTrue(TestWebSocketResource.ON_CLOSE_CALLED.get());
	}

	/**
	 * The refusal itself: no callback of the resource is reached, and the connection is closed and
	 * gone from the registry rather than left open.
	 */
	@Test
	public void anUnauthorizedCallerIsRefusedAtTheHandshake()
	{
		strategy.allow = false;

		WebSocketTester webSocketTester = new WebSocketTester(tester, TestWebSocketResource.TEXT);

		assertEquals(1, strategy.resources.size());
		assertFalse(TestWebSocketResource.ON_CONNECT_CALLED.get());
		assertFalse(TestWebSocketResource.ON_ABORT_CALLED.get());
		assertFalse(TestWebSocketResource.ON_CLOSE_CALLED.get());

		assertFalse(webSocketTester.isConnectionOpen());
		assertEquals(WebSocketConnectionOriginFilter.POLICY_VIOLATION_ERROR_CODE,
				webSocketTester.getCloseCode());
		assertEquals("Not authorized", webSocketTester.getCloseReason());

		assertTrue(connections().isEmpty());
	}

	/**
	 * A refused client that keeps talking is not heard: the connection it would be answered on is
	 * no longer registered.
	 */
	@Test
	public void aRefusedCallerCannotSendAMessage()
	{
		strategy.allow = false;

		AtomicBoolean echoed = new AtomicBoolean(false);
		WebSocketTester webSocketTester = new WebSocketTester(tester, TestWebSocketResource.TEXT)
		{
			@Override
			protected void onOutMessage(String message)
			{
				echoed.set(true);
			}

			@Override
			protected void onOutMessage(byte[] message, int offset, int length)
			{
				echoed.set(true);
			}
		};

		webSocketTester.sendMessage(EXPECTED_TEXT);
		webSocketTester.sendMessage(new byte[] { 1, 2, 3 }, 0, 3);

		assertFalse(TestWebSocketResource.ON_MESSAGE_CALLED.get());
		assertFalse(echoed.get());
	}

	/**
	 * And is not reachable from the server side either, because it was removed from the registry a
	 * broadcast walks.
	 */
	@Test
	public void aRefusedConnectionIsNotReachableByAPush()
	{
		strategy.allow = false;

		WebSocketTester webSocketTester = new WebSocketTester(tester, TestWebSocketResource.TEXT);
		webSocketTester.broadcastAll(tester.getApplication(), new TestPushMessage());

		assertFalse(TestWebSocketResource.ON_PUSH_CALLED.get());
	}

	/**
	 * The decision is taken for the connection, not for every message on it. This pins that choice:
	 * a later change to re-check per message has to change this test deliberately.
	 */
	@Test
	public void anAuthorizedConnectionIsAskedOnlyOnce()
	{
		WebSocketTester webSocketTester = new WebSocketTester(tester, TestWebSocketResource.TEXT);

		webSocketTester.sendMessage(EXPECTED_TEXT);
		webSocketTester.broadcastAll(tester.getApplication(), new TestPushMessage());
		webSocketTester.destroy();

		assertTrue(TestWebSocketResource.ON_MESSAGE_CALLED.get());
		assertTrue(TestWebSocketResource.ON_PUSH_CALLED.get());
		assertTrue(TestWebSocketResource.ON_CLOSE_CALLED.get());

		assertEquals(1, strategy.resources.size());
	}

	/**
	 * A connection to a page is not a request for a resource, so the strategy is not asked about it
	 * at all - not even by a strategy that refuses every resource there is.
	 */
	@Test
	public void aPageConnectionIsNotAskedAbout()
	{
		strategy.allow = false;

		AtomicBoolean echoed = new AtomicBoolean(false);
		WebSocketBehaviorTestPage page = new WebSocketBehaviorTestPage(EXPECTED_TEXT);
		tester.startPage(page);

		WebSocketTester webSocketTester = new WebSocketTester(tester, page)
		{
			@Override
			protected void onOutMessage(String message)
			{
				assertEquals(Strings.capitalize(EXPECTED_TEXT), message);
				echoed.set(true);
			}
		};

		webSocketTester.sendMessage(EXPECTED_TEXT);

		assertTrue(echoed.get());
		assertTrue(webSocketTester.isConnectionOpen());
		assertTrue(strategy.resources.isEmpty());
	}

	/**
	 * The connection filter of the application is untouched: it still rejects before the resource is
	 * authorized, and it still tells the resource so with an AbortedMessage.
	 */
	@Test
	public void theConnectionFilterStillAbortsWithAnAbortedMessage()
	{
		WebSocketSettings.Holder.get(tester.getApplication())
			.setConnectionFilter(servletRequest -> new ConnectionRejected(
				WebSocketConnectionOriginFilter.POLICY_VIOLATION_ERROR_CODE, "Rejected"));

		WebSocketTester webSocketTester = new WebSocketTester(tester, TestWebSocketResource.TEXT);

		assertTrue(TestWebSocketResource.ON_ABORT_CALLED.get());
		assertFalse(TestWebSocketResource.ON_CONNECT_CALLED.get());
		assertFalse(webSocketTester.isConnectionOpen());
		assertEquals("Rejected", webSocketTester.getCloseReason());
	}

	private List<?> connections()
	{
		return new ArrayList<>(WebSocketSettings.Holder.get(tester.getApplication())
			.getConnectionRegistry().getConnections(tester.getApplication()));
	}

	private static class RecordingAuthorizationStrategy
		extends IAuthorizationStrategy.AllowAllAuthorizationStrategy
	{
		private boolean allow = true;

		private final List<IResource> resources = new ArrayList<>();
		private final List<PageParameters> parameters = new ArrayList<>();

		@Override
		public boolean isResourceAuthorized(IResource resource, PageParameters pageParameters)
		{
			resources.add(resource);
			parameters.add(pageParameters);
			return allow;
		}
	}

	private static class TestPushMessage implements IWebSocketPushMessage
	{
	}
}
