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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.authroles.authorization.strategies.role.RoleAuthorizationStrategy;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.authroles.authorization.strategies.role.metadata.MetaDataRoleAuthorizationStrategy;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.ws.api.WebSocketBehavior;
import org.apache.wicket.protocol.ws.api.WebSocketRequestHandler;
import org.apache.wicket.protocol.ws.api.event.WebSocketTextPayload;
import org.apache.wicket.protocol.ws.api.message.BinaryMessage;
import org.apache.wicket.protocol.ws.api.message.ConnectedMessage;
import org.apache.wicket.protocol.ws.api.message.IWebSocketPushMessage;
import org.apache.wicket.protocol.ws.api.message.TextMessage;
import org.apache.wicket.protocol.ws.api.registry.PageIdKey;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * A message sent by the client reaches {@link WebSocketBehavior#onMessage} only where the client
 * may invoke a listener of the behavior, just like an Ajax behavior. What the server pushes, and
 * the event broadcast itself, are not affected.
 */
class WebSocketBehaviorCanCallListenerTest
{
	/** what reached which callback, as "callback:component id" */
	private static final List<String> RECEIVED = new CopyOnWriteArrayList<>();

	private WicketTester tester;

	/**
	 * Records what reaches it, under the id of its component.
	 */
	private static class RecordingBehavior extends WebSocketBehavior
	{
		private String componentId;

		@Override
		public void bind(Component component)
		{
			super.bind(component);
			componentId = component.getId();
		}

		@Override
		protected void onMessage(WebSocketRequestHandler handler, TextMessage message)
		{
			RECEIVED.add("text:" + componentId);
		}

		@Override
		protected void onMessage(WebSocketRequestHandler handler, BinaryMessage binaryMessage)
		{
			RECEIVED.add("binary:" + componentId);
		}

		@Override
		protected void onConnect(ConnectedMessage message)
		{
			RECEIVED.add("connect:" + componentId);
		}

		@Override
		protected void onPush(WebSocketRequestHandler handler, IWebSocketPushMessage message)
		{
			RECEIVED.add("push:" + componentId);
		}
	}

	private static class PushMessage implements IWebSocketPushMessage
	{
	}

	/**
	 * A page with a behavior on the page and on containers in every state that matters.
	 */
	public static class TestPage extends WebPage implements IMarkupResourceStreamProvider
	{
		public TestPage()
		{
			add(new RecordingBehavior());

			add(container("visible"));

			WebMarkupContainer hidden = container("hidden");
			hidden.setVisible(false);
			hidden.add(container("hiddenChild"));
			add(hidden);

			WebMarkupContainer disabled = container("disabled");
			disabled.setEnabled(false);
			add(disabled);

			WebMarkupContainer renderDenied = container("renderDenied");
			MetaDataRoleAuthorizationStrategy.authorize(renderDenied, RENDER, "ADMIN");
			add(renderDenied);

			WebMarkupContainer enableDenied = container("enableDenied");
			MetaDataRoleAuthorizationStrategy.authorize(enableDenied, ENABLE, "ADMIN");
			add(enableDenied);

			WebMarkupContainer optedIn = new WebMarkupContainer("optedIn");
			optedIn.setVisible(false);
			optedIn.add(new RecordingBehavior()
			{
				@Override
				public boolean canCallListener(Component component)
				{
					return true;
				}
			});
			add(optedIn);

			// a component handling the payload itself receives it as every event broadcast does
			add(new WebMarkupContainer("ownOnEvent")
			{
				@Override
				public void onEvent(IEvent<?> event)
				{
					if (event.getPayload() instanceof WebSocketTextPayload)
					{
						RECEIVED.add("onEvent:" + getId());
					}
				}
			}.setVisible(false));
		}

		private static WebMarkupContainer container(String id)
		{
			WebMarkupContainer container = new WebMarkupContainer(id);
			container.add(new RecordingBehavior());
			return container;
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream("<html><head></head><body>" +
				"<div wicket:id=\"visible\"></div>" +
				"<div wicket:id=\"hidden\"><div wicket:id=\"hiddenChild\"></div></div>" +
				"<div wicket:id=\"disabled\"></div>" +
				"<div wicket:id=\"renderDenied\"></div>" +
				"<div wicket:id=\"enableDenied\"></div>" +
				"<div wicket:id=\"optedIn\"></div>" +
				"<div wicket:id=\"ownOnEvent\"></div>" +
				"</body></html>");
		}
	}

	@BeforeEach
	void before()
	{
		RECEIVED.clear();

		tester = new WicketTester();
		tester.getApplication()
			.getSecuritySettings()
			.setAuthorizationStrategy(
				new RoleAuthorizationStrategy(roles -> roles.hasAnyRole(new Roles("USER"))));
	}

	@AfterEach
	void after()
	{
		tester.destroy();
	}

	private static Set<String> expected(String callback, String... componentIds)
	{
		Set<String> expected = new TreeSet<>();
		for (String componentId : componentIds)
		{
			expected.add(callback + ":" + componentId);
		}
		return expected;
	}

	private static Set<String> received()
	{
		return new TreeSet<>(RECEIVED);
	}

	@Test
	void aTextMessageReachesOnlyTheBehaviorsTheClientMayInvoke()
	{
		TestPage page = tester.startPage(TestPage.class);

		WebSocketTester webSocketTester = new WebSocketTester(tester, page);
		try
		{
			RECEIVED.clear();
			webSocketTester.sendMessage("a message");

			Set<String> expected = expected("text", page.getId(), "visible", "optedIn");
			expected.add("onEvent:ownOnEvent");
			assertEquals(expected, received());
		}
		finally
		{
			webSocketTester.destroy();
		}
	}

	@Test
	void aBinaryMessageReachesOnlyTheBehaviorsTheClientMayInvoke()
	{
		TestPage page = tester.startPage(TestPage.class);

		WebSocketTester webSocketTester = new WebSocketTester(tester, page);
		try
		{
			RECEIVED.clear();
			byte[] message = "a message".getBytes(StandardCharsets.UTF_8);
			webSocketTester.sendMessage(message, 0, message.length);

			assertEquals(expected("binary", page.getId(), "visible", "optedIn"), received());
		}
		finally
		{
			webSocketTester.destroy();
		}
	}

	@Test
	void theNotificationsOfTheServerReachEveryBehavior()
	{
		TestPage page = tester.startPage(TestPage.class);

		WebSocketTester webSocketTester = new WebSocketTester(tester, page);
		try
		{
			String[] everyBehavior = { page.getId(), "visible", "hidden", "hiddenChild", "disabled",
					"renderDenied", "enableDenied", "optedIn" };

			assertEquals(expected("connect", everyBehavior), received());

			RECEIVED.clear();
			webSocketTester.broadcast(tester.getApplication(), tester.getHttpSession().getId(),
				new PageIdKey(page.getPageId(), page.getClass().getName()), new PushMessage());

			assertEquals(expected("push", everyBehavior), received());
		}
		finally
		{
			webSocketTester.destroy();
		}
	}
}
