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
package org.apache.wicket.benchmarks;

import org.apache.wicket.Session;
import org.apache.wicket.ThreadContext;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.mock.MockWebRequest;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.protocol.http.mock.MockServletContext;
import org.apache.wicket.request.Url;

/**
 * Minimal Wicket runtime for benchmarks: enough application and session context for components to
 * be constructed, read and detached, without the cost of a full {@code WicketTester} request cycle.
 */
final class WicketContext
{
	private WicketContext()
	{
	}

	static void attach()
	{
		MockApplication application = new MockApplication();
		application.setName("benchmarks-" + System.nanoTime());
		application.setServletContext(new MockServletContext(application, null));
		ThreadContext.setApplication(application);
		application.initApplication();

		Session session = new WebSession(new MockWebRequest(Url.parse("/")));
		ThreadContext.setSession(session);
	}

	static void detach()
	{
		ThreadContext.detach();
	}
}
