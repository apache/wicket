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
package org.apache.wicket.util.tester.apps_2;

import org.apache.wicket.authorization.IAuthorizationStrategy;
import org.apache.wicket.authorization.strategies.page.SimplePageAuthorizationStrategy;
import org.apache.wicket.util.tester.WicketTestCase;

/**
 * 
 * @author Juergen Donnerstag
 */
public class Test extends WicketTestCase
{
	/**
	 * 
	 */
	@org.junit.Test
	public void testRedirect()
	{
		final IAuthorizationStrategy authorizationStrategy = new SimplePageAuthorizationStrategy(
			RedirectPage.class, LoginPage.class)
		{
			@Override
			protected boolean isAuthorized()
			{
				return false;
			}
		};

		tester.getApplication()
			.getSecuritySettings()
			.setAuthorizationStrategy(authorizationStrategy);

		tester.startPage(RedirectPage.class);
		tester.assertRenderedPage(LoginPage.class);
	}
}
