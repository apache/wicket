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

import javax.enterprise.context.Conversation;
import javax.inject.Inject;

import org.apache.wicket.cdi.util.tester.ContextManager;
import org.junit.Before;
import org.junit.Test;

/**
 * @author jsarman
 */
public class ConversationManagerTest extends WicketCdiTestCase
{

	@Inject
	ConversationManager conversationManager;
	@Inject
	CdiConfiguration cdiConfiguration;
	@Inject
	Conversation conversation;
	@Inject
	ContextManager contextManager;

	@Before
	public void init()
	{
		tester.configure();
	}

	@Test
	public void testConverationManagerWithConversation()
	{

		conversation.begin();

		assertTrue(testConversationManagerConversationManagement(!cdiConfiguration.isAutoConversationManagement()));
		assertTrue(testConversationManagerConversationManagement(cdiConfiguration.isAutoConversationManagement()));
		for (ConversationPropagation cp : ConversationPropagation.values())
		{
			assertTrue(testConversationManagerPropagation(cp));
		}
		conversation.end();
	}

	@Test
	public void testConverationManagerWithoutConversation()
	{

		// Transient conversation results in conversationManager using global so test should return false
		assertFalse(testConversationManagerConversationManagement(!cdiConfiguration.isAutoConversationManagement()));
		for (ConversationPropagation cp : ConversationPropagation.values())
		{
			//Skip no change test
			if (cp != cdiConfiguration.getPropagation())
			{
				// Transient conversation results in conversationManager using global is test returns false
				assertFalse(testConversationManagerPropagation(cp));
			}
		}

	}

	@Test(expected = Exception.class)
	public void testConversationManagerNullPropagation()
	{
		testConversationManagerPropagation(null);
	}

	public boolean testConversationManagerConversationManagement(boolean manage)
	{
		boolean passed;
		Boolean globalAuto = cdiConfiguration.isAutoConversationManagement();
		cdiConfiguration.setAutoConversationManagement(manage);
		passed = globalAuto == cdiConfiguration.isAutoConversationManagement();
		passed &= manage == conversationManager.getManageConversation();
		return passed;
	}

	public boolean testConversationManagerPropagation(ConversationPropagation propagation)
	{
		boolean passed;
		IConversationPropagation globalPropagation = cdiConfiguration.getPropagation();
		cdiConfiguration.setPropagation(propagation);
		passed = globalPropagation == cdiConfiguration.getPropagation();
		passed &= propagation == conversationManager.getPropagation();
		return passed;
	}
}