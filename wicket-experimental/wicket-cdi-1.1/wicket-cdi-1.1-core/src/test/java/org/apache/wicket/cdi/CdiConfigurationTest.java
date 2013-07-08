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

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import org.apache.wicket.cdi.AbstractCdiContainer.ContainerSupport;
import org.apache.wicket.cdi.testapp.TestCdiAdditionApplication;
import org.apache.wicket.cdi.testapp.TestCdiApplication;
import org.apache.wicket.cdi.testapp.TestConversationPage;
import org.apache.wicket.cdi.testapp.TestPage;
import org.apache.wicket.cdi.util.tester.CdiWicketTester;
import org.apache.wicket.util.tester.WicketTester;
import org.jglue.cdiunit.AdditionalClasses;
import org.junit.Test;

/**
 * @author jsarman
 */
@AdditionalClasses({TestCdiAdditionApplication.class})
public class CdiConfigurationTest extends WicketCdiTestCase
{

	@Inject
	ConversationPropagator conversationPropagator;
	@Inject
	ComponentInjector componentInjector;
	@Inject
	CdiConfiguration cdiConfiguration;

	@Override
	public void init()
	{

	}


	@Test
	public void testApplicationScope()
	{
		CdiWicketTester tester = getTester();
		tester.startPage(TestPage.class);
		tester.assertLabel("appscope", "Test ok");
	}

	@Test
	public void testConversationScope()
	{
		CdiWicketTester tester = getTester();
		tester.startPage(TestConversationPage.class);
		for (int i = 0; i < 20; i++)
		{
			tester.assertLabel("count", i + "");
			tester.clickLink("increment");
		}
	}

	@Test(expected = Exception.class)
	public void testConfigureTwice()
	{
		CdiWicketTester tester = getTester();
		tester.configure();
		CdiConfiguration.get().configure(tester.getApplication());
	}

	@Test
	public void testDeprecatedApplicationLevelConfiguration()
	{
		WicketTester tester = new WicketTester();
		CdiConfiguration config = CdiConfiguration.get();
		config.setAutoConversationManagement(true);
		assertTrue(config.isAutoConversationManagement());
		config.setAutoConversationManagement(false);
		assertFalse(config.isAutoConversationManagement());
		config.setInjectApplication(false);
		assertFalse(config.isInjectApplication());
		config.setInjectApplication(true);
		assertTrue(config.isInjectApplication());
		config.setInjectBehaviors(false);
		assertFalse(config.isInjectBehaviors());
		config.setInjectBehaviors(true);
		assertTrue(config.isInjectBehaviors());
		config.setInjectComponents(false);
		assertFalse(config.isInjectComponents());
		config.setInjectComponents(true);
		assertTrue(config.isInjectComponents());
		config.setInjectSession(false);
		assertFalse(config.isInjectSession());
		config.setInjectSession(true);
		assertTrue(config.isInjectSession());
		for (ConversationPropagation cp : ConversationPropagation.values())
		{
			config.setPropagation(cp);
			assertEquals(cp, config.getPropagation());
		}
		config.configure(tester.getApplication());
		assertTrue(config.isConfigured());
	}

	@Test
	public void testFilterInitWithInitParam()
	{

		assertEquals("Test String", ((TestCdiApplication) getTester().getApplication()).getInjectedTestString());
	}

	@Test(expected = Exception.class)
	public void testFilterInitWithoutInitParam()
	{
		filterConfigProducer.removeParameter(CdiWebApplicationFactory.WICKET_APP_NAME);
		getTester();
	}

	/**
	 * Bring up two different apps that are uniquely configured and verify they do not affect the application
	 * dependent global settings.
	 */
	@Test
	public void testMultiAppLoad()
	{
		getTester(); //Bring up app with name mockApp : the default
		try
		{
			Executors.newSingleThreadExecutor().submit(new Runnable()
			{
				@Override
				public void run()
				{
					Map<String, String> params = new TreeMap<>();
					params.put(CdiWebApplicationFactory.WICKET_APP_NAME, "test2");
					//change global default for auto to true
					params.put(CdiWebApplicationFactory.AUTO_CONVERSATION, "true");

					getTester(true, params); // bring up app 2 with name test2
					assertTrue(cdiConfiguration.isAutoConversationManagement());
				}

			}).get();
		} catch (InterruptedException | ExecutionException ex)
		{
			fail(ex.getMessage());
		}
		// now check that app1 auto is still false after app2's auto was set to true
		assertFalse(cdiConfiguration.isAutoConversationManagement());
	}

	@Test
	public void testFilterParamsBooleansTrue()
	{
		testFilterParamsBooleans(true);
	}

	@Test
	public void testFilterParamsBooleansFalse()
	{
		testFilterParamsBooleans(true);
	}

	@Test
	public void testFilterParamPropagationNone()
	{
		testFilterParamPropagation(ConversationPropagation.NONE);
	}

	@Test
	public void testFilterParamPropagationNonBookmarkable()
	{
		testFilterParamPropagation(ConversationPropagation.NONBOOKMARKABLE);
	}

	@Test
	public void testFilterParamPropagationAll()
	{
		testFilterParamPropagation(ConversationPropagation.ALL);
	}

	@Test(expected = Exception.class)
	public void testInvalidNameInFilter()
	{
		Map<String, String> params = Collections.singletonMap(CdiWebApplicationFactory.WICKET_APP_NAME, "0xDEADBEEF");
		getTester(params);
	}

	public void testFilterParamsBooleans(Boolean val)
	{
		Map<String, String> params = new TreeMap<>();
		params.put(CdiWebApplicationFactory.AUTO_CONVERSATION, val.toString());
		params.put(CdiWebApplicationFactory.INJECT_APP, val.toString());
		params.put(CdiWebApplicationFactory.INJECT_BEHAVIOR, val.toString());
		params.put(CdiWebApplicationFactory.INJECT_COMPONENT, val.toString());
		params.put(CdiWebApplicationFactory.INJECT_SESSION, val.toString());
		for (ContainerSupport support : ContainerSupport.values())
		{
			params.put(support.getInitParameterName(), val.toString());
		}
		getTester(params);
		CdiConfiguration cc = CdiConfiguration.get();

		assertFalse(cc.isInjectApplication()); // This is false bacause app is injected in Filter.
		assertEquals(val, cc.isInjectBehaviors());
		assertEquals(val, cc.isInjectComponents());
		assertEquals(val, cc.isInjectSession());
		assertEquals(val, cc.isAutoConversationManagement());
		for (ContainerSupport support : ContainerSupport.values())
		{
			assertEquals(val, cc.isContainerFeatureEnabled(support));
		}
	}

	public void testFilterParamPropagation(ConversationPropagation propagation)
	{
		Map params = Collections.singletonMap(CdiWebApplicationFactory.PROPAGATION, propagation.name());
		getTester(params);
		CdiConfiguration cc = CdiConfiguration.get();

		assertEquals(propagation, cc.getPropagation());
	}


}
