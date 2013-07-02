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

import org.apache.wicket.cdi.testapp.TestConversationPage;
import org.junit.Test;

/**
 * @author jsarman
 */
public class ConversationPropagatorTest extends CdiBaseTest
{

	@Inject
	CdiConfiguration cdiConfiguration;


	@Test
	public void testAutoConversation()
	{

		tester.startPage(TestConversationPage.class);//, new PageParameters().add("auto", true));
		for (int i = 0; i < 7; i++)
		{
			tester.assertLabel("count", i + "");
			tester.clickLink("increment");

		}
	}
}
