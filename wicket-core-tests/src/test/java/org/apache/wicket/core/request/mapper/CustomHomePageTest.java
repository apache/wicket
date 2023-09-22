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
package org.apache.wicket.core.request.mapper;

import org.apache.wicket.Application;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.mock.MockHttpServletRequest;
import org.apache.wicket.resource.DummyApplication;
import org.apache.wicket.util.tester.DummyHomePage;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.Test;

/**
 * A test case for <a href="https://issues.apache.org/jira/browse/WICKET-3126">WICKET-3126</a>
 */
class CustomHomePageTest
{

	/**
	 * Tests no custom home page. The home page is get from {@link Application#getHomePage()}
	 */
	@Test
    void defaultHomePage()
	{
		final WebApplication dummyApplication = new DummyApplication();
		final WicketTester tester = new WicketTester(dummyApplication);

		requestHomePage(tester);

		tester.assertRenderedPage(dummyApplication.getHomePage());
		tester.destroy();
	}

	/**
	 * Tests mounting of a custom home page via {@link WebApplication#mountPage(String, Class)}
	 */
	@Test
    void customHomePage()
	{
		final WebApplication dummyApplication = new DummyApplication()
		{
			/**
			 * @see org.apache.wicket.protocol.http.WebApplication#init()
			 */
			@Override
			protected void init()
			{
				super.init();

				// the following two lines do identical things
				// getRootRequestMapperAsCompound().add(new HomePageMapper(CustomHomePage.class));
				mountPage("/", CustomHomePage.class);
			}
		};
		final WicketTester tester = new WicketTester(dummyApplication);

		requestHomePage(tester);

		tester.assertRenderedPage(CustomHomePage.class);
		tester.destroy();
	}

	private void requestHomePage(final WicketTester tester)
	{
		MockHttpServletRequest request = tester.getRequest();
		String contextPath = request.getContextPath();
		String filterPrefix = request.getFilterPrefix();
		tester.executeUrl(contextPath + "/" + filterPrefix + "/");
	}

	/***/
	public static class CustomHomePage extends DummyHomePage
	{
		private static final long serialVersionUID = 1L;
	}
}
