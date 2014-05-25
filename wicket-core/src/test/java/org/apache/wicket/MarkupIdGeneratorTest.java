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
package org.apache.wicket;

import org.apache.wicket.markup.html.link.Link;
import org.junit.Test;

/**
 * Tests for IMarkupIdGenerator
 *
 * @since 6.16.0
 * @see <a href="https://issues.apache.org/jira/browse/WICKET-5577">Generation of wicket ids with prefix / suffix</a>
 */
public class MarkupIdGeneratorTest extends WicketTestCase
{
	@Test
	public void defaultMarkupIdGenerator()
	{
		MockPageWithLink page = new MockPageWithLink();
		Link link = new Link(MockPageWithLink.LINK_ID)
		{
			@Override
			public void onClick()
			{
			}
		};
		link.setOutputMarkupId(true);
		page.add(link);

		assertEquals("link1", link.getMarkupId());
	}

	@Test
	public void customMarkupIdGenerator()
	{
		final String customMarkupId = "custom";
		IMarkupIdGenerator generator = new IMarkupIdGenerator()
		{
			@Override
			public String generateMarkupId(Component component, boolean create)
			{
				return customMarkupId;
			}
		};
		tester.getApplication().getMarkupSettings().setMarkupIdGenerator(generator);
		MockPageWithLink page = new MockPageWithLink();
		Link link = new Link(MockPageWithLink.LINK_ID)
		{
			@Override
			public void onClick()
			{
			}
		};
		link.setOutputMarkupId(true);
		page.add(link);

		assertEquals(customMarkupId, link.getMarkupId());
	}
}
