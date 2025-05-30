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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.wicket.cdi.testapp.TestConversationPage;
import org.apache.wicket.cdi.testapp.TestConversationalPage;
import org.apache.wicket.core.request.mapper.MountedMapper;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.junit.jupiter.api.Test;

import jakarta.enterprise.context.Conversation;
import jakarta.inject.Inject;

/**
 * @author jsarman
 */
class ConversationPropagatorTest extends WicketCdiTestCase
{
	@Inject
	Conversation conversation;

	@Test
	void testAutoConversationNonBookmarkable()
	{
		configure(new CdiConfiguration());

		tester.startPage(TestConversationalPage.class);
		int i;
		for (i = 0; i < 3; i++)
		{
			tester.assertCount(i);
			tester.clickLink("increment");
		}
		tester.clickLink("next");
		for (; i < 6; i++)
		{
			tester.clickLink("increment");
			tester.assertCount(1);
		}
	}

	@Test
	void testAutoConversationBookmarkable()
	{
		configure(new CdiConfiguration());

		tester.startPage(TestConversationalPage.class,
				new PageParameters().add("pageType", "bookmarkable"));

		int i;
		for (i = 0; i < 3; i++)
		{
			tester.assertCount(i);
			tester.clickLink("increment");
		}
		tester.clickLink("next");
		for (i = 0; i < 3; i++)
		{
			tester.clickLink("increment");
			tester.assertCount(1);
		}
	}

	@Test
	void testPropagationAllNonBookmarkable()
	{
		configure(new CdiConfiguration().setPropagation(ConversationPropagation.ALL));

		tester.startPage(TestConversationPage.class);
		int i;
		for (i = 0; i < 3; i++)
		{
			tester.assertCount(i);
			tester.clickLink("increment");
		}
		tester.clickLink("next");
		for (; i < 6; i++)
		{
			tester.assertCount(i);
			tester.clickLink("increment");
		}
	}

	@Test
	void testPropagationAllHybrid()
	{
		configure(new CdiConfiguration().setPropagation(ConversationPropagation.ALL));
		tester.getApplication().getRootRequestMapperAsCompound().add(new MountedMapper("segment/${pageType}", TestConversationPage.class));

		tester.startPage(TestConversationPage.class, new PageParameters().add("pageType", "hybrid"));

		int i;
		for (i = 0; i < 3; i++)
		{
			tester.assertCount(i);
			tester.clickLink("increment");
		}
		tester.clickLink("next");
		for (; i < 6; i++)
		{
			tester.assertCount(i);
			tester.clickLink("increment");
		}
	}

	/**
	 * <a href="https://issues.apache.org/jira/browse/WICKET-6257">WICKET-6257</a>
	 */
	@Test
	void testPropagationAllHybridRefresh()
	{
		configure(new CdiConfiguration().setPropagation(ConversationPropagation.ALL));
		tester.getApplication().getRootRequestMapperAsCompound().add(new MountedMapper("segment/${pageType}", TestConversationPage.class));

		tester.startPage(TestConversationPage.class, new PageParameters().add("pageType", "hybrid"));

		String pageId = tester.getLastRenderedPage().getId();
		String cid = conversation.getId();

		tester.executeUrl("segment/hybrid?"+pageId+"&cid="+cid);

		assertEquals(pageId, tester.getLastRenderedPage().getId());
	}

	@Test
	void testPropagationAllBookmarkable()
	{
		configure(new CdiConfiguration().setPropagation(ConversationPropagation.ALL));

		tester.startPage(TestConversationPage.class,
				new PageParameters().add("pageType", "bookmarkable"));
		int i;
		for (i = 0; i < 3; i++)
		{
			tester.assertCount(i);
			tester.clickLink("increment");
		}
		tester.clickLink("next");
		for (; i < 6; i++)
		{
			tester.assertCount(i);
			tester.clickLink("increment");
		}
	}

	@Test
	void testPropagationNonBookmarkable()
	{
		configure(new CdiConfiguration());

		tester.startPage(TestConversationPage.class,
				new PageParameters().add("pageType", "bookmarkable"));
		int i;
		for (i = 0; i < 3; i++)
		{
			tester.assertCount(i);
			tester.clickLink("increment");
		}
		tester.clickLink("next");
		for (; i < 6; i++)
		{
			tester.clickLink("increment");
			tester.assertCount(1);
		}
	}

	@Test
	void testPropagationNone()
	{
		configure(new CdiConfiguration().setPropagation(ConversationPropagation.NONE));

		tester.startPage(TestConversationPage.class);
		int i;
		for (i = 0; i < 3; i++)
		{
			tester.clickLink("increment");
			tester.assertCount(1);
		}
		tester.clickLink("next");
		for (; i < 6; i++)
		{
			tester.clickLink("increment");
			tester.assertCount(1);
		}
	}

}
