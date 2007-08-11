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
package org.apache.wicket.markup.html.header.inheritance;

import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.Request;
import org.apache.wicket.Response;
import org.apache.wicket.Session;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.protocol.http.HttpSessionStore;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.session.ISessionStore;
import org.apache.wicket.util.tester.WicketTester;


/**
 * Tests the inclusion of the wicket:head section from a panel in a subclassed
 * page.
 * 
 * @author Martijn Dashorst
 */
public class InheritanceHeadTest extends WicketTestCase
{
	/**
	 * Construct.
	 * 
	 * @param name
	 */
	public InheritanceHeadTest(String name)
	{
		super(name);
	}

	/**
	 * 
	 * @throws Exception
	 */
	public void test_2() throws Exception
	{
		executeTest(ConcretePage2.class, "ExpectedResult2.html");
	}

	/**
	 * 
	 * @throws Exception
	 */
	public void test_3() throws Exception
	{
		tester = new WicketTester(new WebApplication()
		{
			/**
			 * @see org.apache.wicket.protocol.http.WebApplication#newSession(Request,
			 *      Response)
			 */
			public Session newSession(Request request, Response response)
			{
				return new WebSession(request).setStyle("myStyle");
			}

			public Class getHomePage()
			{
				return ConcretePage2.class;
			}

			protected WebResponse newWebResponse(HttpServletResponse servletResponse)
			{
				return new WebResponse(servletResponse);
			}

			protected void outputDevelopmentModeWarning()
			{
				// Do nothing.
			}

			protected ISessionStore newSessionStore()
			{
				// Don't use a filestore, or we spawn lots of threads, which
				// makes things slow.
				return new HttpSessionStore(this);
			}
		});

		executeTest(ConcretePage2.class, "ExpectedResult3.html");
	}
}
