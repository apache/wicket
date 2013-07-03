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
import org.apache.wicket.cdi.testapp.TestConversationalPage;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.junit.Test;

/**
 * @author jsarman
 */
public class ConversationPropagatorTest extends CdiBaseTest
{

	@Inject
	CdiConfiguration cdiConfiguration;


	@Test
	public void testAutoConversationNonBookmarkable()
	{

		tester.startPage(TestConversationalPage.class);
		int i;
		for (i = 0; i < 3; i++)
		{
			tester.assertLabel("count", i + "");
			tester.clickLink("increment");
		}
		tester.clickLink("next");
		for (; i < 6; i++)
		{
			tester.assertLabel("count", i + "");
			tester.clickLink("increment");
		}

	}

	@Test
	public void testAutoConversationBookmarkable()
	{
		tester.startPage(TestConversationalPage.class,
				new PageParameters().add("pageType", "bookmarkable"));

		int i;
		for (i = 0; i < 3; i++)
		{
			tester.assertLabel("count", i + "");
			tester.clickLink("increment");
		}
		tester.clickLink("next");
		//The conversation should auto end and not create another one
		//so the next page just keeps getting 1 because the conversationscoped bean
		//doesnt persist across requests.
		for (i = 0; i < 3; i++)
		{
			tester.clickLink("increment");
			tester.assertLabel("count", 1 + "");
		}
	}

	@Test
	public void testPropagationAllNonBookmarkable()
	{

		cdiConfiguration.setPropagation(ConversationPropagation.ALL);
		cdiConfiguration.configure(tester.getApplication());
		tester.startPage(TestConversationPage.class);
		int i;
		for (i = 0; i < 3; i++)
		{
			tester.assertLabel("count", i + "");
			tester.clickLink("increment");
		}
		tester.clickLink("next");
		for (; i < 6; i++)
		{
			tester.assertLabel("count", i + "");
			tester.clickLink("increment");
		}

	}

	@Test
	public void testPropagationAllBookmarkable()
	{
		cdiConfiguration.setPropagation(ConversationPropagation.ALL);
		cdiConfiguration.configure(tester.getApplication());
		tester.startPage(TestConversationPage.class,
				new PageParameters().add("pageType", "bookmarkable"));
		int i;
		for (i = 0; i < 3; i++)
		{
			tester.assertLabel("count", i + "");
			tester.clickLink("increment");
		}
		tester.clickLink("next");
		for (; i < 6; i++)
		{
			tester.assertLabel("count", i + "");
			tester.clickLink("increment");
		}

	}

	@Test
	public void testPropagationNone()
	{
		cdiConfiguration.setPropagation(ConversationPropagation.NONE);
		cdiConfiguration.configure(tester.getApplication());
		tester.startPage(TestConversationPage.class);
		int i;
		for (i = 0; i < 3; i++)
		{
			tester.clickLink("increment");
			tester.assertLabel("count", "1");
		}
		tester.clickLink("next");
		for (; i < 6; i++)
		{
			tester.clickLink("increment");
			tester.assertLabel("count", "1");
		}

	}

	@Test
	public void testGlobalAutoSettingNonBookmarkable()
	{
		cdiConfiguration.setAutoConversationManagement(true);
		cdiConfiguration.configure(tester.getApplication());
		tester.startPage(TestConversationPage.class,
				new PageParameters().add("auto", true));
		int i;
		for (i = 0; i < 3; i++)
		{
			tester.assertLabel("count", i + "");
			tester.clickLink("increment");
		}
		tester.clickLink("next");
		for (; i < 6; i++)
		{
			tester.assertLabel("count", i + "");
			tester.clickLink("increment");
		}
	}

	@Test
	public void testGlobalAutoSettingBookmarkable()
	{
		cdiConfiguration.setAutoConversationManagement(true);
		cdiConfiguration.configure(tester.getApplication());
		tester.startPage(TestConversationPage.class,
				new PageParameters().add("auto", true).add("pageType", "bookmarkable"));
		int i;
		for (i = 0; i < 3; i++)
		{
			tester.assertLabel("count", i + "");
			tester.clickLink("increment");
		}
		tester.clickLink("next");
		for (i = 0; i < 3; i++)
		{
			tester.assertLabel("count", i + "");
			tester.clickLink("increment");
		}
	}


}
