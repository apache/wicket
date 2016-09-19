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
package org.apache.wicket.authroles.authentication;

import static java.util.Locale.getDefault;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.wicket.Application;
import org.apache.wicket.ThreadContext;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.session.ISessionStore;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Pedro Santos
 */
public class AuthenticatedWebSessionTest extends WicketTestCase {
	private Request request;
	private Response response;
	private ISessionStore sessionStore;
	private AuthenticatedWebSession session;

	@Before
	public void initialize() {
		request = mock(Request.class);
		response = mock(Response.class);
		sessionStore = mock(ISessionStore.class);
		when(request.getLocale()).thenReturn(getDefault());
		session = new TestAuthenticatedWebSession(request);
	}

	@Test
	public void shouldLookupForSessionOnce() throws InterruptedException {
		ExecutorService executorService = Executors.newFixedThreadPool(10);
		for (int i = 0; i < 10; i++)
			executorService.submit(new SiginTask(tester.getApplication()));
		executorService.shutdown();
		executorService.awaitTermination(5, SECONDS);
		// counting lookup calls since sesion.bind() is final
		// TODO: test for bind calls itself
		verify(sessionStore, times(1)).lookup(request);
	}

	class SiginTask implements Runnable {
		Application application;

		public SiginTask(Application application) {
			this.application = application;
		}

		@Override
		public void run() {
			ThreadContext.setRequestCycle(application.createRequestCycle(request, response));
			session.signIn("user", "pass");
		}

	}

	class TestAuthenticatedWebSession extends AuthenticatedWebSession {
		private static final long serialVersionUID = 1L;

		public TestAuthenticatedWebSession(Request request) {
			super(request);
		}

		@Override
		protected boolean authenticate(String username, String password) {
			return true;
		}

		@Override
		protected ISessionStore getSessionStore() {
			return sessionStore;
		}

		@Override
		public Roles getRoles() {
			return null;
		}
	}
}
