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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.ws.WebSocketSettings;
import org.apache.wicket.protocol.ws.api.IWebSocketConnection;
import org.apache.wicket.protocol.ws.api.WebSocketBehavior;
import org.apache.wicket.protocol.ws.api.WebSocketConnectionOriginFilter;
import org.apache.wicket.protocol.ws.api.WebSocketRequestHandler;
import org.apache.wicket.protocol.ws.api.message.ClosedMessage;
import org.apache.wicket.protocol.ws.api.message.ConnectedMessage;
import org.apache.wicket.protocol.ws.api.message.IWebSocketPushMessage;
import org.apache.wicket.protocol.ws.api.message.TextMessage;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * A connection whose session lives on but whose page is no longer in the page store reaches no
 * behavior of that page, and is closed rather than left open delivering nothing.
 */
class WebSocketPageExpiredTest
{
	private static final AtomicInteger CONNECTS = new AtomicInteger();

	private static final AtomicInteger MESSAGES = new AtomicInteger();

	private static final AtomicInteger PUSHES = new AtomicInteger();

	private static final AtomicInteger CLOSES = new AtomicInteger();

	private WicketTester tester;

	/**
	 * The page the connection is opened for.
	 */
	public static class WebSocketPage extends WebPage implements IMarkupResourceStreamProvider
	{
		public WebSocketPage()
		{
			add(new WebSocketBehavior()
			{
				@Override
				protected void onConnect(ConnectedMessage message)
				{
					CONNECTS.incrementAndGet();
				}

				@Override
				protected void onMessage(WebSocketRequestHandler handler, TextMessage message)
				{
					MESSAGES.incrementAndGet();
				}

				@Override
				protected void onPush(WebSocketRequestHandler handler, IWebSocketPushMessage message)
				{
					PUSHES.incrementAndGet();
				}

				@Override
				protected void onClose(ClosedMessage message)
				{
					CLOSES.incrementAndGet();
				}
			});
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream("<html><head></head><body></body></html>");
		}
	}

	private static class PushMessage implements IWebSocketPushMessage
	{
	}

	@BeforeEach
	void before()
	{
		CONNECTS.set(0);
		MESSAGES.set(0);
		PUSHES.set(0);
		CLOSES.set(0);

		tester = new WicketTester();
	}

	@AfterEach
	void after()
	{
		tester.destroy();
	}

	private Collection<IWebSocketConnection> connections()
	{
		WebApplication application = tester.getApplication();
		return WebSocketSettings.Holder.get(application)
			.getConnectionRegistry()
			.getConnections(application, tester.getHttpSession().getId());
	}

	private void expirePages()
	{
		tester.getSession().getPageManager().clear();
	}

	private void assertClosedAsExpired(WebSocketTester webSocketTester)
	{
		assertFalse(webSocketTester.isConnectionOpen(), "the connection was left open");
		assertEquals(WebSocketConnectionOriginFilter.POLICY_VIOLATION_ERROR_CODE,
			webSocketTester.getCloseCode());
		assertEquals("Page expired", webSocketTester.getCloseReason());
		assertTrue(connections().isEmpty(), "the connection was left in the registry");
	}

	@Test
	void aConnectionToALivePageStaysOpen()
	{
		WebSocketPage page = tester.startPage(WebSocketPage.class);
		tester.getSession().bind();

		WebSocketTester webSocketTester = new WebSocketTester(tester, page);
		try
		{
			assertEquals(1, CONNECTS.get());
			assertTrue(webSocketTester.isConnectionOpen());
			assertEquals(1, connections().size());

			webSocketTester.sendMessage("hello");
			assertEquals(1, MESSAGES.get());
			assertTrue(webSocketTester.isConnectionOpen());
		}
		finally
		{
			webSocketTester.destroy();
		}
	}

	/**
	 * The session survived but the page store did not - a restart, or an eviction - and the client
	 * reconnects with the page id it was rendered with.
	 */
	@Test
	void aConnectionToAnExpiredPageIsClosedAtTheHandshake()
	{
		WebSocketPage page = tester.startPage(WebSocketPage.class);
		tester.getSession().bind();

		expirePages();

		WebSocketTester webSocketTester = new WebSocketTester(tester, page);
		try
		{
			assertEquals(0, CONNECTS.get(), "onConnect ran for a page that is not in the store");
			assertClosedAsExpired(webSocketTester);
		}
		finally
		{
			webSocketTester.destroy();
		}
	}

	@Test
	void aConnectionIsClosedByTheFirstMessageAfterItsPageExpired()
	{
		WebSocketPage page = tester.startPage(WebSocketPage.class);
		tester.getSession().bind();

		WebSocketTester webSocketTester = new WebSocketTester(tester, page);
		try
		{
			assertEquals(1, CONNECTS.get(), "the page must be reachable to start with");

			expirePages();
			webSocketTester.sendMessage("after the page expired");

			assertEquals(0, MESSAGES.get());
			assertClosedAsExpired(webSocketTester);
		}
		finally
		{
			webSocketTester.destroy();
		}
	}

	@Test
	void aConnectionIsClosedByAPushAfterItsPageExpired()
	{
		WebSocketPage page = tester.startPage(WebSocketPage.class);
		tester.getSession().bind();

		WebSocketTester webSocketTester = new WebSocketTester(tester, page);
		try
		{
			expirePages();
			webSocketTester.broadcastAll(tester.getApplication(), new PushMessage());

			assertEquals(0, PUSHES.get());
			assertClosedAsExpired(webSocketTester);
		}
		finally
		{
			webSocketTester.destroy();
		}
	}

	/**
	 * A connection that is going away anyway is not closed a second time, but it does not stay in
	 * the registry either.
	 */
	@Test
	void aClosingConnectionToAnExpiredPageIsNotClosedAgain()
	{
		WebSocketPage page = tester.startPage(WebSocketPage.class);
		tester.getSession().bind();

		WebSocketTester webSocketTester = new WebSocketTester(tester, page);

		expirePages();
		webSocketTester.destroy();

		assertEquals(0, CLOSES.get());
		assertEquals(-1, webSocketTester.getCloseCode(), "the server closed the connection again");
		assertNull(webSocketTester.getCloseReason());
		assertTrue(connections().isEmpty(), "the connection was left in the registry");
	}
}
