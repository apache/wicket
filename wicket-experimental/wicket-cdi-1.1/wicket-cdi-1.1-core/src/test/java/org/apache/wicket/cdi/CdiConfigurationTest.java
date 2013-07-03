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

import org.apache.wicket.Application;
import org.apache.wicket.cdi.testapp.TestCdiApplication;
import org.apache.wicket.cdi.testapp.TestConversationPage;
import org.apache.wicket.cdi.testapp.TestPage;
import org.jglue.cdiunit.AdditionalClasses;
import org.junit.Test;

/**
 * @author jsarman
 */
@AdditionalClasses({
		TestCdiApplication.class})
public class CdiConfigurationTest extends CdiFilterBaseTest
{

	@Inject
	ConversationPropagator conversationPropagator;
	@Inject
	ComponentInjector componentInjector;

	/**
	 * Allows to force an app name when class is extended
	 *
	 * @return
	 */
	protected String overrideAppName()
	{
		return null;
	}

	@Test
	public void testApplicationScope()
	{

		tester.startPage(TestPage.class);
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

	@Test(expected = Exception.class)
	public void testConfigureTwice()
	{
		tester.configure();
		CdiConfiguration.get().configure(tester.getApplication());
	}

	@Test
	public void testDeprecatedApplicationLevelConfiguration()
	{
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
		WicketApp annot = TestCdiApplication.class.getAnnotation(WicketApp.class);
		Application app = testFilterInitialization(null, annot.value());
		//Did our app get Injected
		assertEquals("Test String", ((TestCdiApplication) app).getInjectedTestString());
	}

	@Test
	public void testFilterInitWithoutInitParam()
	{
		if (overrideAppName() == null)
		{
			Application app = testFilterInitialization(null, null);
			assertEquals("Test String", ((TestCdiApplication) app).getInjectedTestString());
		}
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
		testFilterInitialization(null, "0xDEADBEEF");
	}

	public void testFilterParamsBooleans(boolean val)
	{
		ConfigurationParameters cp = new ConfigurationParameters();
		cp.setInjectApplication(val);
		cp.setInjectBehaviors(val);
		cp.setInjectComponents(val);
		cp.setInjectSession(val);
		cp.setAutoConversationManagement(val);
		testFilterInitialization(cp, overrideAppName());
		CdiConfiguration cc = CdiConfiguration.get();

		assertFalse(cc.isInjectApplication()); // This is false bacause app is injected in Filter.
		assertEquals(val, cc.isInjectBehaviors());
		assertEquals(val, cc.isInjectComponents());
		assertEquals(val, cc.isInjectSession());
		assertEquals(val, cc.isAutoConversationManagement());
	}

	public void testFilterParamPropagation(ConversationPropagation propagation)
	{
		ConfigurationParameters cp = new ConfigurationParameters();
		cp.setPropagation(propagation);
		testFilterInitialization(cp, overrideAppName());
		CdiConfiguration cc = CdiConfiguration.get();

		assertEquals(propagation, cc.getPropagation());
	}


}
