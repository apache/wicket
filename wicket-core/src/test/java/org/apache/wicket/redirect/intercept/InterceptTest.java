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
package org.apache.wicket.redirect.intercept;

import org.apache.wicket.Page;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.authorization.IAuthorizationStrategy;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.component.IRequestableComponent;
import org.junit.Test;

/**
 * Testcase for bug WICKET-1292.
 * 
 * @see <a href="https://issues.apache.org/jira/browse/WICKET-1292">WICKET-1292</a>
 * @author marrink
 */
public class InterceptTest extends WicketTestCase
{

	@Override
	protected WebApplication newApplication()
	{
		return new MockApplication()
		{
			@Override
			protected void init()
			{
				getSecuritySettings().setAuthorizationStrategy(new IAuthorizationStrategy.AllowAllAuthorizationStrategy()
				{

					private boolean block = true;

					@Override
					public <T extends IRequestableComponent> boolean isInstantiationAuthorized(
						Class<T> componentClass)
					{
						if (block &&
							(componentClass == TargetPage.class || componentClass == HomePage.class))
						{
							block = false;
							throw new RestartResponseAtInterceptPageException(InterceptPage.class);
						}
						return true;
					}
				});
				super.init();
			}

			@Override
			public Class<? extends Page> getHomePage()
			{
				return HomePage.class;
			}
		};
	}

	/**
	 * Testcase for the behavior of WicketTester with respect to continueToOrginialDestination.
	 * Tests a non homepage class.
	 */
	@Test
	public void testRestartResponseAtInterceptPageAndContinueTorOriginalDestination()
	{
		tester.startPage(TargetPage.class);
		tester.assertRenderedPage(InterceptPage.class);
		tester.clickLink("link");
		tester.assertRenderedPage(TargetPage.class);
	}

	/**
	 * Testcase for the behavior of WicketTester with respect to continueToOrginialDestination.
	 * Tests homepage class.
	 */
	@Test
	public void testRestartResponseAtInterceptPageAndContinueTorOriginalDestination2()
	{
		tester.startPage(HomePage.class);
		tester.assertRenderedPage(InterceptPage.class);
		tester.clickLink("link");
		tester.assertRenderedPage(HomePage.class);
	}

}
