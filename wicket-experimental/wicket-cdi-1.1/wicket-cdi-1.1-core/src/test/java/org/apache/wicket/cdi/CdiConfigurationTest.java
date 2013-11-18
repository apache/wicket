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

import org.apache.wicket.cdi.testapp.TestConversationPage;
import org.apache.wicket.cdi.testapp.TestPage;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Test;

/**
 * @author jsarman
 */
public class CdiConfigurationTest extends WicketCdiTestCase
{
	@Test
	public void testApplicationScope()
	{
		configure(new CdiConfiguration());
		tester.startPage(TestPage.class);
		tester.assertLabel("appscope", "Test ok");
	}

	@Test
	public void testConversationScope()
	{
		configure(new CdiConfiguration());
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
		configure(new CdiConfiguration());
		new CdiConfiguration().configure(tester.getApplication());
	}

	@Test
	public void testApplicationLevelConfiguration()
	{
		WicketTester tester = new WicketTester();
		CdiConfiguration config = new CdiConfiguration();
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
	}
}
