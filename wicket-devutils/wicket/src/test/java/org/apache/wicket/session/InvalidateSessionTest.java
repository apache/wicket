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
package org.apache.wicket.session;

import junit.framework.TestCase;

import org.apache.wicket.Page;
import org.apache.wicket.Request;
import org.apache.wicket.Response;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.util.tester.WicketTester;

/**
 * 
 */
public class InvalidateSessionTest extends TestCase
{
	/** */
	public static class MyApp extends WebApplication
	{
		/**
		 * {@inheritDoc}
		 */
		@Override
		public Class<? extends Page> getHomePage()
		{
			return MyPage.class;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Session newSession(Request request, Response response)
		{
			return new MySession(request);
		}
	}

	/** */
	public static class MySession extends WebSession
	{
		private static final long serialVersionUID = 1L;

		private String name;

		/**
		 * Construct.
		 * 
		 * @param request
		 */
		public MySession(Request request)
		{
			super(request);
		}

		public static MySession get()
		{
			return (MySession)Session.get();
		}

		public void setName(String name)
		{
			this.name = name;
		}

		public String getName()
		{
			return name;
		}
	}

	/**
	 * 
	 */
	public static class MyPage extends WebPage
	{
		/**
		 * Construct.
		 */
		public MyPage()
		{
			boolean sessionInvalid = Session.get().isSessionInvalidated();

			MySession sess = MySession.get();
			if (sess.getName() != null)
			{
				Session.get().invalidateNow();
				throw new RestartResponseException(getClass());
				// throw new AbortException();
			}
		}
	}

	/**
	 * 
	 */
	public void test_1()
	{
		WicketTester wicket = new WicketTester(new MyApp());
		wicket.processRequestCycle();

		MySession.get().setName("foo");
		wicket.processRequestCycle();
	}
}
