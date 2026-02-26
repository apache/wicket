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
import org.apache.wicket.cdi.testapp.ModelWithInjectedDependency;
import org.apache.wicket.cdi.testapp.TestConversationPage;
import org.apache.wicket.cdi.testapp.TestPage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author jsarman
 */
class CdiConfigurationTest extends WicketCdiTestCase
{
	@Inject
	BeanManager beanManager;

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
		configure(new CdiConfiguration(beanManager));
		tester.startPage(TestPage.class);
		tester.assertLabel("appscope", "Test ok");
	}

	@Test
	void testConversationScope()
	{
		configure(new CdiConfiguration());
		tester.startPage(TestConversationPage.class);
		for (int i = 0; i < 20; i++)
		{
			tester.assertCount(i);
			tester.clickLink("increment");
		}
	}

	@Test
	void testNotConfigured()
	{
		assertThrows(IllegalStateException.class, () -> {
			new ModelWithInjectedDependency();
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
