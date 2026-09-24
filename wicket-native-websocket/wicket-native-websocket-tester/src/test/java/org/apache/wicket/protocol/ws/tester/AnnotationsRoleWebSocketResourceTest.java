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

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.wicket.authroles.authorization.strategies.role.IRoleCheckingStrategy;
import org.apache.wicket.authroles.authorization.strategies.role.RoleAuthorizationStrategy;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeResource;
import org.apache.wicket.protocol.ws.api.WebSocketRequestHandler;
import org.apache.wicket.protocol.ws.api.WebSocketResource;
import org.apache.wicket.protocol.ws.api.message.ClosedMessage;
import org.apache.wicket.protocol.ws.api.message.ConnectedMessage;
import org.apache.wicket.protocol.ws.api.message.TextMessage;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A WebSocketResource reserved for a role with {@link AuthorizeResource} must not be usable by a
 * client who does not hold that role. A WebSocket connection is the only way such a resource is
 * reached, so this is where the annotation has to bite.
 */
class AnnotationsRoleWebSocketResourceTest
{
	private static final String ADMIN = "admin-resource";
	private static final String OPEN = "open-resource";

	private static final String EXPECTED_TEXT = "expected text";

	private AdminWebSocketResource admin;
	private OpenWebSocketResource open;
	private WicketTester tester;

	@BeforeEach
	public void before()
	{
		admin = new AdminWebSocketResource();
		open = new OpenWebSocketResource();

		tester = new WicketTester();
		tester.getApplication().getSharedResources().add(ADMIN, admin);
		tester.getApplication().getSharedResources().add(OPEN, open);
	}

	@AfterEach
	public void after()
	{
		tester.destroy();
	}

	private void signInAs(String role)
	{
		tester.getApplication()
			.getSecuritySettings()
			.setAuthorizationStrategy(new RoleAuthorizationStrategy(new UserRolesAuthorizer(role)));
	}

	@Test
	public void adminMayOpenAConnectionToTheAdminResource()
	{
		signInAs("ADMIN");

		AtomicBoolean echoed = new AtomicBoolean(false);
		WebSocketTester webSocketTester = new WebSocketTester(tester, ADMIN)
		{
			@Override
			protected void onOutMessage(String message)
			{
				assertEquals(Strings.capitalize(EXPECTED_TEXT), message);
				echoed.set(true);
			}
		};

		assertTrue(admin.connected.get());
		assertTrue(webSocketTester.isConnectionOpen());

		webSocketTester.sendMessage(EXPECTED_TEXT);
		assertTrue(admin.messaged.get());
		assertTrue(echoed.get());

		webSocketTester.destroy();
		assertTrue(admin.closed.get());
	}

	@Test
	public void userMayNotOpenAConnectionToTheAdminResource()
	{
		signInAs("USER");

		AtomicBoolean echoed = new AtomicBoolean(false);
		WebSocketTester webSocketTester = new WebSocketTester(tester, ADMIN)
		{
			@Override
			protected void onOutMessage(String message)
			{
				echoed.set(true);
			}
		};

		assertFalse(admin.connected.get(), "the connection should have been refused");
		assertFalse(webSocketTester.isConnectionOpen());

		webSocketTester.sendMessage(EXPECTED_TEXT);
		webSocketTester.destroy();

		assertFalse(admin.messaged.get());
		assertFalse(admin.closed.get());
		assertFalse(echoed.get());
	}

	@Test
	public void userMayStillOpenAConnectionToAnUnannotatedResource()
	{
		signInAs("USER");

		WebSocketTester webSocketTester = new WebSocketTester(tester, OPEN);

		assertTrue(open.connected.get());
		assertTrue(webSocketTester.isConnectionOpen());

		webSocketTester.sendMessage(EXPECTED_TEXT);
		assertTrue(open.messaged.get());
	}

	private static class RecordingWebSocketResource extends WebSocketResource
	{
		final AtomicBoolean connected = new AtomicBoolean(false);
		final AtomicBoolean messaged = new AtomicBoolean(false);
		final AtomicBoolean closed = new AtomicBoolean(false);

		@Override
		protected void onConnect(ConnectedMessage message)
		{
			connected.set(true);
		}

		@Override
		protected void onClose(ClosedMessage message)
		{
			closed.set(true);
		}

		@Override
		protected void onMessage(WebSocketRequestHandler handler, TextMessage message)
		{
			messaged.set(true);
			handler.push(Strings.capitalize(message.getText()));
		}
	}

	@AuthorizeResource("ADMIN")
	private static class AdminWebSocketResource extends RecordingWebSocketResource
	{
	}

	private static class OpenWebSocketResource extends RecordingWebSocketResource
	{
	}

	private static final class UserRolesAuthorizer implements IRoleCheckingStrategy, Serializable
	{
		private static final long serialVersionUID = 1L;

		private final Roles roles;

		UserRolesAuthorizer(String roles)
		{
			this.roles = new Roles(roles);
		}

		@Override
		public boolean hasAnyRole(Roles roles)
		{
			return this.roles.hasAnyRole(roles);
		}
	}
}
