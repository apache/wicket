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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Session;
import org.apache.wicket.ThreadContext;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.ws.WebSocketSettings;
import org.apache.wicket.protocol.ws.api.IWebSocketConnection;
import org.apache.wicket.protocol.ws.api.WebSocketBehavior;
import org.apache.wicket.protocol.ws.api.WebSocketRequestHandler;
import org.apache.wicket.protocol.ws.api.WebSocketResource;
import org.apache.wicket.protocol.ws.api.message.TextMessage;
import org.apache.wicket.session.HttpSessionStore;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * A connection resolves its {@link Session} out of the HTTP session it was opened on, for every
 * message, and must stop working when that HTTP session ends - whoever ended it. What it writes to
 * the session must reach that same HTTP session and not a copy of it.
 */
class WebSocketSessionLifetimeTest
{
	private static final String RESOURCE_NAME = "sessionLifetimeResource";

	private static final String ATTRIBUTE_NAME = "setOverTheConnection";

	private static final AtomicInteger BEHAVIOR_MESSAGES = new AtomicInteger();

	private static final AtomicInteger RESOURCE_MESSAGES = new AtomicInteger();

	/** what the callback of the page or of the resource should do, if anything */
	private static final AtomicReference<Runnable> IN_CALLBACK = new AtomicReference<>();

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
				protected void onMessage(WebSocketRequestHandler handler, TextMessage message)
				{
					BEHAVIOR_MESSAGES.incrementAndGet();
					runInCallback();
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

	/**
	 * The shared resource a connection can name instead of a page. Unlike a page it never consults
	 * the page manager, so the session is the only thing standing between a client and its
	 * callbacks.
	 */
	static class CountingWebSocketResource extends WebSocketResource
	{
		@Override
		protected void onMessage(WebSocketRequestHandler handler, TextMessage message)
		{
			RESOURCE_MESSAGES.incrementAndGet();
			runInCallback();
		}
	}

	private static void runInCallback()
	{
		Runnable runnable = IN_CALLBACK.getAndSet(null);
		if (runnable != null)
		{
			runnable.run();
		}
	}

	@BeforeEach
	void before()
	{
		BEHAVIOR_MESSAGES.set(0);
		RESOURCE_MESSAGES.set(0);
		IN_CALLBACK.set(null);

		tester = new WicketTester(new MockApplication()
		{
			@Override
			protected void init()
			{
				super.init();

				// MockApplication installs MockSessionStore, whose lookup() ignores the request
				// and hands back the one Session it holds. Nothing would then read the session
				// out of the connection's request, which is the whole subject here.
				setSessionStoreProvider(HttpSessionStore::new);

				getSharedResources().add(RESOURCE_NAME, new CountingWebSocketResource());
			}
		});
	}

	@AfterEach
	void after()
	{
		tester.destroy();
	}

	/**
	 * Sends a message the way the transport does, rather than the way the tester does.
	 * <p>
	 * A frame arrives on a container thread that carries none of Wicket's thread locals, so
	 * {@code AbstractWebSocketProcessor#broadcastMessage} resolves the session from the request of
	 * the connection. The tester runs on the thread that served the last HTTP request, which still
	 * has a {@link Session} in its {@link ThreadContext} that the processor would reuse instead -
	 * and then nothing about the session of the connection is exercised at all.
	 */
	private static void sendFromTheContainer(WebSocketTester webSocketTester, String message)
	{
		ThreadContext threadContext = ThreadContext.detach();
		try
		{
			webSocketTester.sendMessage(message);
		}
		finally
		{
			ThreadContext.restore(threadContext);
		}
	}

	private Collection<IWebSocketConnection> connectionsOf(String sessionId)
	{
		WebApplication application = tester.getApplication();
		return WebSocketSettings.Holder.get(application)
			.getConnectionRegistry()
			.getConnections(application, sessionId);
	}

	/**
	 * @return the name {@link #ATTRIBUTE_NAME} has in the HTTP session, which carries the prefix of
	 *         the application, or {@code null} when it is not there
	 */
	private String attributeOfTheHttpSession()
	{
		return Collections.list(tester.getHttpSession().getAttributeNames())
			.stream()
			.filter(name -> name.endsWith(ATTRIBUTE_NAME))
			.findFirst()
			.orElse(null);
	}

	@Test
	void aBehaviorStopsWhenTheContainerEndsTheSession()
	{
		WebSocketPage page = tester.startPage(WebSocketPage.class);
		tester.getSession().bind();

		WebSocketTester webSocketTester = new WebSocketTester(tester, page);
		try
		{
			sendFromTheContainer(webSocketTester, "while the session lives");
			assertEquals(1, BEHAVIOR_MESSAGES.get(), "the behavior must be reachable to start with");

			// the container ends the session: an idle timeout, a filter, a container-managed logout
			tester.getHttpSession().invalidate();

			sendFromTheContainer(webSocketTester, "after the session ended");
			assertEquals(1, BEHAVIOR_MESSAGES.get(),
				"the behavior ran on a session that had already been invalidated");
		}
		finally
		{
			webSocketTester.destroy();
		}
	}

	@Test
	void aResourceStopsWhenTheContainerEndsTheSession()
	{
		tester.startPage(WebSocketPage.class);
		tester.getSession().bind();

		WebSocketTester webSocketTester = new WebSocketTester(tester, RESOURCE_NAME);
		try
		{
			sendFromTheContainer(webSocketTester, "while the session lives");
			assertEquals(1, RESOURCE_MESSAGES.get(), "the resource must be reachable to start with");

			tester.getHttpSession().invalidate();

			sendFromTheContainer(webSocketTester, "after the session ended");
			assertEquals(1, RESOURCE_MESSAGES.get(),
				"the resource ran on a session that had already been invalidated");
		}
		finally
		{
			webSocketTester.destroy();
		}
	}

	/**
	 * A resource never asks the page manager for anything, so clearing it - which is what
	 * {@link Session#invalidateNow()} does besides ending the session - cannot stop one. Ending the
	 * session it was opened on is what stops it.
	 */
	@Test
	void aResourceStopsWhenWicketEndsTheSession()
	{
		tester.startPage(WebSocketPage.class);
		tester.getSession().bind();

		WebSocketTester webSocketTester = new WebSocketTester(tester, RESOURCE_NAME);
		try
		{
			sendFromTheContainer(webSocketTester, "while the session lives");
			assertEquals(1, RESOURCE_MESSAGES.get(), "the resource must be reachable to start with");

			tester.getSession().invalidateNow();

			sendFromTheContainer(webSocketTester, "after the sign out");
			assertEquals(1, RESOURCE_MESSAGES.get(), "the resource ran after Session#invalidateNow()");
		}
		finally
		{
			webSocketTester.destroy();
		}
	}

	/**
	 * A connection whose session has ended is not merely ignored, it is closed - otherwise it stays
	 * open for as long as the client keeps it, delivering nothing.
	 */
	@Test
	void theConnectionIsClosedWhenTheSessionHasEnded()
	{
		WebSocketPage page = tester.startPage(WebSocketPage.class);
		tester.getSession().bind();

		String sessionId = tester.getHttpSession().getId();

		WebSocketTester webSocketTester = new WebSocketTester(tester, page);
		try
		{
			Collection<IWebSocketConnection> connections = connectionsOf(sessionId);
			assertEquals(1, connections.size(), "the connection must be registered to start with");
			IWebSocketConnection connection = connections.iterator().next();
			assertTrue(connection.isOpen());

			tester.getHttpSession().invalidate();
			sendFromTheContainer(webSocketTester, "after the session ended");

			assertFalse(connection.isOpen(), "the connection was left open");
			assertTrue(connectionsOf(sessionId).isEmpty(), "the connection was left in the registry");
		}
		finally
		{
			webSocketTester.destroy();
		}
	}

	/**
	 * The write direction: a sign out issued from inside a callback must end the HTTP session of
	 * the client, not a copy of it that nobody else can see.
	 */
	@Test
	void endingTheSessionOverTheConnectionEndsTheHttpSession()
	{
		WebSocketPage page = tester.startPage(WebSocketPage.class);
		tester.getSession().bind();

		tester.getSession().setAttribute(ATTRIBUTE_NAME, "set before the connection was opened");
		String sessionIdBefore = tester.getHttpSession().getId();
		assertNotNull(attributeOfTheHttpSession(),
			"the attribute must be in the HTTP session to start with");

		WebSocketTester webSocketTester = new WebSocketTester(tester, page);
		try
		{
			IN_CALLBACK.set(() -> Session.get().invalidateNow());
			sendFromTheContainer(webSocketTester, "sign me out");
			assertEquals(1, BEHAVIOR_MESSAGES.get());

			// MockHttpSession#invalidate() clears the attributes and gives itself a new id; a copy
			// of the session could do neither of those to the session the client still holds
			assertNotEquals(sessionIdBefore, tester.getHttpSession().getId(),
				"the HTTP session of the client survived a sign out made over the connection");
			assertNull(attributeOfTheHttpSession(),
				"an attribute set before the connection was opened survived a sign out made over it");
		}
		finally
		{
			webSocketTester.destroy();
		}
	}

	/**
	 * The same write direction, one step smaller: a session attribute set over a connection has to
	 * be readable by every other request of that session.
	 */
	@Test
	void aSessionAttributeSetOverTheConnectionReachesTheHttpSession()
	{
		WebSocketPage page = tester.startPage(WebSocketPage.class);
		tester.getSession().bind();

		WebSocketTester webSocketTester = new WebSocketTester(tester, page);
		try
		{
			IN_CALLBACK.set(() -> Session.get().setAttribute(ATTRIBUTE_NAME, "written over the connection"));
			sendFromTheContainer(webSocketTester, "remember this");
			assertEquals(1, BEHAVIOR_MESSAGES.get());

			String attributeName = attributeOfTheHttpSession();

			assertNotNull(attributeName,
				"the attribute was written to a copy of the HTTP session instead of to the session");
			assertEquals("written over the connection",
				tester.getHttpSession().getAttribute(attributeName));
		}
		finally
		{
			webSocketTester.destroy();
		}
	}
}
