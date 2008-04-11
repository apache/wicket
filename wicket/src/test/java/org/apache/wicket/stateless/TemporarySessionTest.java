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
package org.apache.wicket.stateless;

import org.apache.wicket.Page;
import org.apache.wicket.Session;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.stateless.pages.HomePage;
import org.apache.wicket.stateless.pages.LoginPage;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;

/**
 * A test to detect temporary sessions. However since the http stack has been mocked for this test,
 * it is more a test to see if our mocks can handle temporary sessions then it is to see if wicket
 * supports temporary sessions.
 * 
 * @author marrink
 */
public class TemporarySessionTest extends WicketTestCase
{
// private WicketTester tester;

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception
	{
		tester = new WicketTester(new WebApplication()
		{

			public Class<? extends Page> getHomePage()
			{
				return HomePage.class;
			}

			protected void outputDevelopmentModeWarning()
			{
				// Do nothing.
			}
		}, "src/test/java/" + getClass().getPackage().getName().replace('.', '/'))
		{
			public boolean initializeHttpSessionAsTemporary()
			{
				return true;
			}
		};
	}


	/**
	 * @see junit.framework.TestCase#tearDown()
	 */
// protected void tearDown() throws Exception
// {
// tester.destroy();
// }
	/**
	 * Test if we can keep a session temporary.
	 */
	public void testSessionIsTemporary()
	{
		tester.startPage(LoginPage.class);
		tester.setupRequestAndResponse();
		assertTrue(tester.getWicketSession().isTemporary());
		tester.processRequestCycle(LoginPage.class);
		FormTester form = tester.newFormTester("signInPanel:signInForm");
		form.setValue("username", "test");
		form.setValue("password", "test");
		tester.getWicketSession().bind();
		form.submit();
		tester.assertRenderedPage(HomePage.class);
		tester.setupRequestAndResponse();
		assertFalse(Session.get().isTemporary());
		tester.processRequestCycle(LoginPage.class);
	}

}
