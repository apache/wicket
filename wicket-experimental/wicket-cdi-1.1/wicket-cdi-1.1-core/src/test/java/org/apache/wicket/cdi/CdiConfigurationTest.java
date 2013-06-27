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
package org.apache.wicket.cdi;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.cdi.testapp.TestAppScope;
import org.apache.wicket.cdi.testapp.TestApplication;
import org.apache.wicket.cdi.testapp.TestConversationBean;
import org.apache.wicket.cdi.testapp.TestConversationPage;
import org.apache.wicket.util.tester.WicketTester;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;
import org.jglue.cdiunit.ContextController;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author jsarman
 */
@RunWith(CdiRunner.class)
@AdditionalClasses({BehaviorInjector.class,
		CdiConfiguration.class,
		CdiShutdownCleaner.class,
		ComponentInjector.class,
		ConversationExpiryChecker.class,
		ConversationPropagator.class,
		DetachEventEmitter.class,
		NonContextualManager.class,
		SessionInjector.class,
		MockContainer.class,
		TestAppScope.class,
		TestConversationBean.class})
public class CdiConfigurationTest extends Assert
{

	private WicketTester tester;
	@Inject
	ContextController contextController;
	@Inject
	ConversationPropagator conversationPropagator;
	@Inject
	ComponentInjector componentInjector;

	@Before
	public void before()
	{
		tester = new WicketTester(new TestApplication());
		prepareRequest(tester.getRequest());
	}

	@After
	public void after()
	{
		tester.destroy();
		tester = null;
	}

	public void prepareRequest(HttpServletRequest request)
	{
		contextController.openRequest(request);
		contextController.openSession(request);
		contextController.openConversation(request);
	}

	@Test
	public void testApplicationScope()
	{
		tester.startPage(tester.getApplication().getHomePage());
		tester.assertLabel("appscope", "Test ok");
	}

	@Test
	public void testConversationScope()
	{
		tester.startPage(TestConversationPage.class);
		for (int i = 0; i < 20; i++)
		{
			tester.assertLabel("count", i + "");
			tester.clickLink("increment");
		}
	}

	@Test
	public void testDynamicConfigureChange()
	{
		// CdiConfiguration is configured at begin auto is false
		assertEquals(false, conversationPropagator.getAuto());
		// set auto to true in configuration
		CdiConfiguration.get().setAutoConversationManagement(true);
		assertEquals(true, conversationPropagator.getAuto());
		// Test Changing Propagation
		for (ConversationPropagation propagation : ConversationPropagation.values())
		{
			CdiConfiguration.get().setPropagation(propagation);
			assertEquals(propagation, conversationPropagator.getPropagation());
		}

		int ignoreCnt = componentInjector.getIgnorePackages().length;

		CdiConfiguration.get().addPackagesToIgnore("test1", "test2", "test3");
		assertEquals(ignoreCnt + 3, componentInjector.getIgnorePackages().length);

		CdiConfiguration.get().removePackagesToIgnore("test1", "test2");
		assertEquals(ignoreCnt + 1, componentInjector.getIgnorePackages().length);
	}
}
