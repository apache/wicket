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

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;
import org.apache.wicket.Application;
import org.apache.wicket.cdi.testapp.ModelWithInjectedDependency;
import org.apache.wicket.cdi.testapp.TestConversationPage;
import org.apache.wicket.cdi.testapp.TestFilteredApplication;
import org.apache.wicket.cdi.testapp.TestFilteredPage;
import org.apache.wicket.cdi.testapp.TestFilteredSession;
import org.apache.wicket.cdi.testapp.TestPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author jsarman
 */
class CdiConfigurationTest extends WicketCdiTestCase
{
	@Inject
	BeanManager beanManager;

	/**
	 * The filter an application would realistically write: only its own classes are candidates.
	 */
	private static final java.util.function.Predicate<Class<?>> ONLY_TEST_APP = clazz -> clazz
		.getName()
		.startsWith("org.apache.wicket.cdi.testapp");

	@Override
	protected WebApplication newApplication()
	{
		return new TestFilteredApplication();
	}

	private TestFilteredApplication application()
	{
		return (TestFilteredApplication)tester.getApplication();
	}

	@Test
	void testApplicationScope()
	{
		configure(new CdiConfiguration());
		tester.startPage(TestPage.class);
		tester.assertLabel("appscope", "Test ok");
	}

	@Test
	void testUsesCdiJUnitConfiguration()
	{
		configure(new CdiConfiguration().setBeanManager(beanManager));
		tester.startPage(TestPage.class);
		tester.assertLabel("appscope", "Test ok");
	}

	@Test
	void testConversationScope()
	{
		configure(new CdiConfiguration());
		assertConversationCounterWorks();
	}

	/**
	 * The conversation only propagates when wicket-cdi's own listeners got injected, so this asserts
	 * that they are injected even though the filter rejects every class outside the test application.
	 */
	@Test
	void testCdiTypesAreInjectedRegardlessOfFilter()
	{
		assertFalse(ONLY_TEST_APP.test(ConversationPropagator.class),
			"the filter under test has to reject wicket-cdi's own types");
		configure(new CdiConfiguration().setInjectionCandidateFilter(ONLY_TEST_APP));
		assertConversationCounterWorks();
	}

	private void assertConversationCounterWorks()
	{
		tester.startPage(TestConversationPage.class);
		for (int i = 0; i < 20; i++)
		{
			tester.assertCount(i);
			tester.clickLink("increment");
		}
	}

	@Test
	void testDefaultFilterInjectsComponentBehaviorAndSession()
	{
		configure(new CdiConfiguration());

		TestFilteredPage page = startPageWithNewSession();

		assertTrue(page.isInjected(), "component should have been injected");
		assertTrue(page.getBehavior().isInjected(), "behavior should have been injected");
		assertTrue(session().isInjected(), "session should have been injected");
		tester.assertLabel("appscope", "Test ok");
	}

	@Test
	void testFilterIsAppliedToComponentBehaviorAndSession()
	{
		configure(new CdiConfiguration().setInjectionCandidateFilter(clazz -> false));

		TestFilteredPage page = startPageWithNewSession();

		assertFalse(page.isInjected(), "component should not have been injected");
		assertFalse(page.getBehavior().isInjected(), "behavior should not have been injected");
		assertFalse(session().isInjected(), "session should not have been injected");
		tester.assertLabel("appscope", "not injected");
	}

	/**
	 * The session is already created when the tester is built, ie before the configuration under test
	 * is applied, so a new one is forced to have {@link SessionInjector} see it.
	 */
	private TestFilteredPage startPageWithNewSession()
	{
		tester.getSession().invalidateNow();
		return tester.startPage(TestFilteredPage.class);
	}

	private TestFilteredSession session()
	{
		return (TestFilteredSession)tester.getSession();
	}

	/**
	 * The application is injected by {@link CdiConfiguration#configure(Application)} itself, not
	 * through an injector, so the filter must not be able to suppress it.
	 */
	@Test
	void testApplicationPostConstructIsNotAffectedByFilter()
	{
		configure(new CdiConfiguration().setInjectionCandidateFilter(clazz -> false));

		assertTrue(application().isInjected(), "application should have been injected");
		assertTrue(application().isPostConstructed(), "@PostConstruct should have run on the application");
	}

	@Test
	void testShutdownCleanerIsNotAffectedByFilter()
	{
		configure(new CdiConfiguration().setInjectionCandidateFilter(clazz -> false));

		new CdiShutdownCleaner().onBeforeDestroyed(tester.getApplication());

		assertTrue(application().isPreDestroyed(), "@PreDestroy should have run on the application");
	}

	@Test
	void testNotConfigured()
	{
		assertThrows(IllegalStateException.class, () -> {
			new ModelWithInjectedDependency();
		});

	}

	@Test
	void testAlreadyConfigured()
	{
		configure(new CdiConfiguration());

		assertThrows(IllegalStateException.class, () -> {
			CdiConfiguration.get(Application.get()).setBeanManager(beanManager);
		});

	}

	@Test
	void testConfigureTwice()
	{
		configure(new CdiConfiguration());

		assertThrows(Exception.class, () -> {
			new CdiConfiguration().configure(tester.getApplication());
		});

	}

	@Test
	void testApplicationLevelConfiguration()
	{
		CdiConfiguration config = new CdiConfiguration();
		for (ConversationPropagation cp : ConversationPropagation.values())
		{
			config.setPropagation(cp);
			assertEquals(cp, config.getPropagation());
		}
		configure(config);
	}
}
