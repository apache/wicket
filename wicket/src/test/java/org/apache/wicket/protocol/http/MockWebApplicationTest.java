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
package org.apache.wicket.protocol.http;

import java.util.Iterator;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.wicket.Application;
import org.apache.wicket.Session;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.feedback.FeedbackMessages;
import org.apache.wicket.feedback.IFeedbackMessageFilter;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.settings.IResourceSettings;
import org.apache.wicket.util.diff.DiffUtil;
import org.apache.wicket.util.file.IResourceFinder;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.tester.WicketTester;
import org.apache.wicket.util.tester.WicketTester.DummyWebApplication;

/**
 * Simple application that demonstrates the mock http application code (and checks that it is
 * working)
 * 
 * @author Chris Turner
 */
public class MockWebApplicationTest extends TestCase
{
	private WicketTester application;

	/**
	 * Filter that returns all rendered messages.
	 */
	private static final IFeedbackMessageFilter RENDERED_MESSAGES = new IFeedbackMessageFilter()
	{
		private static final long serialVersionUID = 1L;

		public boolean accept(FeedbackMessage message)
		{
			return message.isRendered();
		}
	};

	/**
	 * Create the test.
	 * 
	 * @param name
	 *            The test name
	 */
	public MockWebApplicationTest(String name)
	{
		super(name);
	}

	@Override
    protected void setUp() throws Exception
	{
		super.setUp();
		application = new WicketTester();
		application.startPage(MockPage.class);
	}

	@Override
    protected void tearDown() throws Exception
	{
		application.destroy();
	}

	/**
	 * @throws Exception
	 */
	public void testRenderHomePage() throws Exception
	{
		// Validate the document
		String document = application.getServletResponse().getDocument();
		DiffUtil.validatePage(document, getClass(), "MockPage_expectedResult.html", true);

		// Inspect the page & model
		MockPage p = (MockPage)application.getLastRenderedPage();
		Assert.assertEquals("Link should have been clicked 0 times", 0, p.getLinkClickCount());
	}

	/**
	 * Tests the clean up of flash messages put into the session when they are rendered.
	 */
	public void testSessionFeedbackMessagesCleanUp()
	{
		Session session = Session.get();
		session.info("Message");
		session.info("Not rendered");
		FeedbackMessages feedbackMessages = session.getFeedbackMessages();
		Iterator<FeedbackMessage> iterator = feedbackMessages.iterator();
		FeedbackMessage message = iterator.next();
		message.markRendered();
		feedbackMessages.clear(RENDERED_MESSAGES);
		assertEquals(1, feedbackMessages.size());
		message = iterator.next();
		message.markRendered();
		feedbackMessages.clear(RENDERED_MESSAGES);
		assertEquals(0, feedbackMessages.size());
	}

	/**
	 * @throws Exception
	 */
	public void testClickLink() throws Exception
	{
		// Need to call the home page first
		testRenderHomePage();

		// Now request that we click the link
		application.setupRequestAndResponse();
		MockPage p = (MockPage)application.getLastRenderedPage();
		Link<?> link = (Link<?>)p.get("actionLink");
		application.getServletRequest().setRequestToComponent(link);
		application.processRequestCycle();

		// Check that redirect was set as expected and invoke it
		/*
		 * Assert.assertTrue("Response should be a redirect",
		 * application.getServletResponse().isRedirect()); String redirect =
		 * application.getServletResponse().getRedirectLocation();
		 * application.setupRequestAndResponse();
		 * application.getServletRequest().setRequestToRedirectString(redirect);
		 * application.processRequestCycle();
		 */
		// Validate the document
		String document = application.getServletResponse().getDocument();
		DiffUtil.validatePage(document, getClass(), "MockPage_expectedResult2.html", true);

		// Inspect the page & model
		p = (MockPage)application.getLastRenderedPage();
		Assert.assertEquals("Link should have been clicked 1 time", 1, p.getLinkClickCount());
	}

	public void testProvidesDefaultResourceFinderIfNotSetByApplication()
	{
		 Assert.assertNotNull(Application.get().getResourceSettings().getResourceFinder());
	}

	public void testHonorsResourceFinderSettingsSetByApplication()
	{
		final IResourceFinder customResourceFinder = new IResourceFinder()
		{
			public IResourceStream find(Class<?> clazz, String pathname)
			{
				throw new UnsupportedOperationException("Not implemented");
			}

			@Override
            public String toString()
			{
				return "customResourceFinder";
			}
		};
		WebApplication wicketApplication = new DummyWebApplication() {
			@Override
            protected void init()
			{
				IResourceSettings resourceSettings = getResourceSettings();
				resourceSettings.setResourceFinder(customResourceFinder);
			}
		};
		new MockWebApplication(wicketApplication, "foo");
		IResourceFinder resourceFinderInApplication = Application.get().getResourceSettings().getResourceFinder();
		Assert.assertSame(customResourceFinder, resourceFinderInApplication);
	}
}
