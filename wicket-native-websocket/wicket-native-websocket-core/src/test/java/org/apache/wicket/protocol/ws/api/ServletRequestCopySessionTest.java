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
package org.apache.wicket.protocol.ws.api;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.apache.wicket.protocol.http.mock.MockHttpServletRequest;
import org.apache.wicket.protocol.http.mock.MockHttpSession;
import org.junit.jupiter.api.Test;

/**
 * A container reports an ended session by throwing {@link IllegalStateException}, which
 * {@link MockHttpSession} does not do - it only empties itself. Both halves have to lead to the
 * same answer, because the copy of the request is what every message of a connection resolves its
 * session through.
 */
class ServletRequestCopySessionTest
{
	/**
	 * A session that behaves the way the servlet API specifies: once invalidated, the methods that
	 * are documented to throw, throw.
	 */
	private static class ContainerLikeHttpSession extends MockHttpSession
	{
		private static final long serialVersionUID = 1L;

		private boolean valid = true;

		ContainerLikeHttpSession()
		{
			super(null);
		}

		@Override
		public void invalidate()
		{
			super.invalidate();
			valid = false;
		}

		@Override
		public long getCreationTime()
		{
			if (valid == false)
			{
				throw new IllegalStateException("The session has been invalidated");
			}
			return super.getCreationTime();
		}
	}

	@Test
	void theLiveSessionIsHandedBackWhileItLasts()
	{
		ContainerLikeHttpSession session = new ContainerLikeHttpSession();
		ServletRequestCopy copy = new ServletRequestCopy(
			new MockHttpServletRequest(null, session, null));

		assertSame(session, copy.getSession(false));
		assertSame(session, copy.getSession(true));
		assertSame(session, copy.getSession());
	}

	@Test
	void thereIsNoSessionOnceTheContainerHasEndedIt()
	{
		ContainerLikeHttpSession session = new ContainerLikeHttpSession();
		ServletRequestCopy copy = new ServletRequestCopy(
			new MockHttpServletRequest(null, session, null));

		session.invalidate();

		assertNull(copy.getSession(false), "an ended session was handed back to the connection");
		assertNull(copy.getSession(true), "a connection cannot start a session, it has no response");
		assertNull(copy.getSession());
	}
}
