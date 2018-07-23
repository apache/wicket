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
package org.apache.wicket.ajax.markup.html;

import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for checking whether or not a component is attached to the component hierarchy when
 * updating through AJAX.
 */
public class ComponentNotOnPageTest extends WicketTestCase
{
	private RuntimeConfigurationType configuration = RuntimeConfigurationType.DEVELOPMENT;

	/**
	 * Overrides the application factory to enable changing the configuration type of Wicket.
	 */
	@Override
	protected WebApplication newApplication()
	{
		return new MockApplication()
		{
			@Override
			public RuntimeConfigurationType getConfigurationType()
			{
				return configuration;
			}
		};
	}

	/**
	 * When running in development mode Wicket should trigger an exception signaling the error on
	 * the developers part that a component that is not part of the page is being refreshed in the
	 * AJAX response, resulting in a no-op (which is not the intended result).
	 */
	@Test(expected = IllegalArgumentException.class)
	public void responseTargetInDevelopmentModeShouldFail()
	{
		configuration = RuntimeConfigurationType.DEVELOPMENT;

		try
		{
			// this should not fail
			ComponentNotOnPage page = tester.startPage(ComponentNotOnPage.class);
			tester.clickLink("listview:0:link", true);
			tester.startPage(page);
		}
		catch (Exception e)
		{
			Assert.fail("Unexpected exception: " + e);
		}

		// this should fail
		tester.clickLink("refresher:refresh", true);
	}

	/**
	 * When running in deployment mode Wicket should <b>not</b> trigger an exception signaling the
	 * error on the developers part that a component that is not part of the page is being refreshed
	 * in the AJAX response, resulting in a no-op (which is not the intended result). Instead Wicket
	 * should signal the error in the log, but not prevent the user of the application to continue
	 * (which happened in Wicket 7).
	 */
	@Test
	public void responseTargetInDeploymentModeShouldNotFail()
	{
		configuration = RuntimeConfigurationType.DEPLOYMENT;

		try
		{
			// this should not fail
			ComponentNotOnPage page = tester.startPage(ComponentNotOnPage.class);
			tester.clickLink("listview:0:link", true);
			tester.startPage(page);
		}
		catch (Exception e)
		{
			Assert.fail("Unexpected exception: " + e);
		}

		// this shouldn't fail as well
		tester.clickLink("refresher:refresh", true);
	}
}
