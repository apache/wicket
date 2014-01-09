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
package org.apache.wicket.ajax;

import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.authorization.Action;
import org.apache.wicket.authorization.IAuthorizationStrategy;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;

/**
 * @author marrink
 */
public class AjaxBehaviorEnabledTest extends WicketTestCase
{
	/**
	 * Custom security strategy to disable all components where the id ends with "disable".
	 * 
	 * @author marrink
	 */
	private static final class CustomStrategy extends IAuthorizationStrategy.AllowAllAuthorizationStrategy
	{
		/**
		 * 
		 * @see org.apache.wicket.authorization.IAuthorizationStrategy#isActionAuthorized(org.apache.wicket.Component,
		 *      org.apache.wicket.authorization.Action)
		 */
		@Override
		public boolean isActionAuthorized(Component component, Action action)
		{
			if (action == Component.ENABLE && component.getId().endsWith("disabled"))
			{
				return false;
			}
			return true;
		}
	}

	/**
	 * 
	 */
	@Before
	public void before()
	{
		final IAuthorizationStrategy strategy = new CustomStrategy();
		tester = new WicketTester(new MockApplication()
		{
			@Override
			public Session newSession(Request request, Response response)
			{
				return new WebSession(request)
				{
					private static final long serialVersionUID = 1L;

					@Override
					public IAuthorizationStrategy getAuthorizationStrategy()
					{
						return strategy;
					}
				};
			}
		});
	}

	/**
	 * TestCase for bug.
	 * 
	 * @see <a href="https://issues.apache.org/jira/browse/WICKET-1575">1575</a>
	 */
	@Test
	public void disabledBehavior() throws Exception
	{
		executeTest(AjaxBehaviorEnabledPage.class, "AjaxBehaviorEnabled_expected.html");
	}

}
