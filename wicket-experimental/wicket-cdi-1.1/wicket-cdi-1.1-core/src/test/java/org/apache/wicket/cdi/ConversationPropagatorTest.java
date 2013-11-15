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
import org.apache.wicket.cdi.testapp.TestConversationalPage;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author jsarman
 */
public class ConversationPropagatorTest extends WicketCdiTestCase
{
	@Test
	@Ignore("Testcase and auto conversations do not match")
	public void testAutoConversationNonBookmarkable()
	{
		configure(new CdiConfiguration().setAutoConversationManagement(true));

		tester.startPage(TestConversationalPage.class);
		int i;
		for (i = 0; i < 3; i++)
		{
			tester.assertLabel("count", i + "");
			tester.clickLink("increment");
		}
		tester.clickLink("next");
		// at this point counter = 3, auto conversation is still enabled and
		// remains enabled, because TestConversationalPage is part of this
		// request
		for (; i < 6; i++)
		{
			// first iteration: i == 3, counter == 3, conversation active
			// second iteration: i == 4, counter == 4, conversation transient
			// third iteration: i == 5, counter == 1: FAIL
			tester.assertLabel("count", i + "");

			// first iteration: conversation is still long-running, counter is
			// incremented, after which auto conversation is disabled and the
			// conversation ended

			// second iteration: transient conversation, counter starts at 1,
			// conversation remains transient
			tester.clickLink("increment");

		}

	}

	@Test
	@Ignore("Testcase and auto conversations do not match")
	public void testAutoConversationBookmarkable()
	{
		configure(new CdiConfiguration().setAutoConversationManagement(true));

		tester.startPage(TestConversationalPage.class,
				new PageParameters().add("pageType", "bookmarkable"));

		int i;
		for (i = 0; i < 3; i++)
		{
			tester.assertLabel("count", i + "");
			tester.clickLink("increment");
		}
		tester.clickLink("next");
		// The conversation should auto end and not create another one
		// so the next page just keeps getting 1 because the conversationscoped
		// bean
		// doesnt persist across requests.
		for (i = 0; i < 3; i++)
		{
			tester.clickLink("increment");
			tester.assertLabel("count", 1 + "");
		}
	}

	@Test
	public void testPropagationAllNonBookmarkable()
	{
		configure(new CdiConfiguration().setPropagation(ConversationPropagation.ALL));

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
		configure(new CdiConfiguration().setPropagation(ConversationPropagation.ALL));

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
		configure(new CdiConfiguration().setPropagation(ConversationPropagation.NONE));

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
	@Ignore("Testcase and auto conversations do not match")
	public void testGlobalAutoSettingNonBookmarkable()
	{
		configure(new CdiConfiguration().setAutoConversationManagement(true));

		tester.startPage(TestConversationPage.class, new PageParameters().add("auto", true));
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
	@Ignore("Testcase and auto conversations do not match")
	public void testGlobalAutoSettingBookmarkable()
	{
		configure(new CdiConfiguration().setAutoConversationManagement(true));

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
