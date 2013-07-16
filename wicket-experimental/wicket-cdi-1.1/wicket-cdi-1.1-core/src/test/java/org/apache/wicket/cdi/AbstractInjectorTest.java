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

import javax.enterprise.inject.Any;
import javax.inject.Inject;

import org.junit.Test;

/**
 * @author jsarman
 */
public class AbstractInjectorTest extends WicketCdiTestCase
{

	@Inject
	ConversationPropagator conversationPropagator;
	@Inject
	@Any
	AbstractInjector abstractInjector;


	@Test
	public void testIgnore()
	{
		CdiConfiguration.get().addClassesToIgnore(Object.class);
		assertTrue(abstractInjector.ignore(Object.class));

		CdiConfiguration.get().removeClassesToIgnore(Object.class);
		assertFalse(abstractInjector.ignore(Object.class));

		CdiConfiguration.get().addPackagesToIgnore("java.lang");
		assertTrue(abstractInjector.ignore(Object.class));
		assertTrue(abstractInjector.ignore(Runtime.class));

		CdiConfiguration.get().removePackagesToIgnore("java.lang");
		assertFalse(abstractInjector.ignore(Object.class));
		assertFalse(abstractInjector.ignore(Runtime.class));

	}


}
