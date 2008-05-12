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

import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.authorization.Action;
import org.apache.wicket.authorization.IAuthorizationStrategy;
import org.apache.wicket.resource.DummyApplication;
import org.apache.wicket.util.tester.WicketTester;

/**
 * Testcase for bug WICKET-1292.
 * 
 * @see <a href="https://issues.apache.org/jira/browse/WICKET-1292">WICKET-1292</a>
 * @author marrink
 */
public class InterceptTest extends WicketTestCase
{

	/**
	 * @see org.apache.wicket.WicketTestCase#setUp()
	 */
	protected void setUp() throws Exception
	{
		tester = new WicketTester(new DummyApplication()
		{
			protected void init()
			{
				getSecuritySettings().setAuthorizationStrategy(new IAuthorizationStrategy()
				{

					private boolean block = true;

					public boolean isActionAuthorized(Component component, Action action)
					{
						return true;
					}

					public boolean isInstantiationAuthorized(Class componentClass)
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

			public Class getHomePage()
			{
				return HomePage.class;
			}
		});
	}

	/**
	 * Testcase for the behavior of WicketTester with respect to continueToOrginialDestination.
	 * Tests a non homepage class.
	 */
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
	public void testRestartResponseAtInterceptPageAndContinueTorOriginalDestination2()
	{
		tester.startPage(HomePage.class);
		tester.assertRenderedPage(InterceptPage.class);
		tester.clickLink("link");
		tester.assertRenderedPage(HomePage.class);
	}

}
