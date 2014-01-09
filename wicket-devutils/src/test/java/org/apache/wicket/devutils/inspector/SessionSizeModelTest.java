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
package org.apache.wicket.devutils.inspector;

import org.apache.wicket.Session;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Pedro Santos
 */
public class SessionSizeModelTest extends Assert
{

	/**
	 * @see <a href="https://issues.apache.org/jira/browse/WICKET-3355">WICKET-3355</a>
	 */
	@Test
	public void testTorelanceOnProblematicSessions()
	{
		new WicketTester(new MockApplication()
		{
			@Override
			public Session newSession(final Request request, final Response response)
			{
				return new TestSession(request);
			}
		});
		SessionSizeModel model = new SessionSizeModel();
		assertEquals(null, model.getObject());
	}

	/**
	 * TestSession
	 */
	public static class TestSession extends WebSession
	{
		private static final long serialVersionUID = 1L;
		/**	 */
		public Object nonSerializableObject = new Object();

		/**
		 * Construct.
		 * 
		 * @param request
		 */
		public TestSession(final Request request)
		{
			super(request);
		}

	}
}
