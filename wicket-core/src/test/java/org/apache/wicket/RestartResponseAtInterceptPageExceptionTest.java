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
package org.apache.wicket;

import org.apache.wicket.authorization.strategies.page.AbstractPageAuthorizationStrategy;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 * Tests for {@link RestartResponseAtInterceptPageException}
 */
public class RestartResponseAtInterceptPageExceptionTest extends WicketTestCase
{
	@Override
	protected WebApplication newApplication()
	{
		return new MockApplication()
		{

			@Override
			protected void init()
			{
				super.init();

				getSecuritySettings().setAuthorizationStrategy(
					new AbstractPageAuthorizationStrategy()
					{
						@Override
						protected <T extends Page> boolean isPageAuthorized(Class<T> pageClass)
						{
							if (pageClass != RedirectPage.class)
							{
								RedirectPage intercept = new RedirectPage("http://example.com/path");
								throw new RestartResponseAtInterceptPageException(intercept);
							}
							return true;
						}
					});
			}
		};
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-3836
	 */
	@Test
	public void redirectToBufferForNonVersionedPage()
	{
		tester.startPage(tester.getApplication().getHomePage());

		tester.assertRenderedPage(RedirectPage.class);
	}

}
