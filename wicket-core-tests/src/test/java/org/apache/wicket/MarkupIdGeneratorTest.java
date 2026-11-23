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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

/**
 * Tests for IMarkupIdGenerator
 *
 * @since 6.16.0
 * @see <a href="https://issues.apache.org/jira/browse/WICKET-5577">Generation of wicket ids with prefix / suffix</a>
 */
class MarkupIdGeneratorTest extends WicketTestCase
{
	@Test
	void defaultMarkupIdGenerator()
	{
		MockPageWithLink page = new MockPageWithLink();
		Link link = new Link<Void>(MockPageWithLink.LINK_ID)
		{
			@Override
			public void onClick()
			{
			}
		};
		link.setOutputMarkupId(true);
		page.add(link);

		assertEquals("link_1", link.getMarkupId());
	}

	@Test
	void customMarkupIdGenerator()
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
		Link link = new Link<Void>(MockPageWithLink.LINK_ID)
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

	/**
	 * GitHub issue #1951, WICKET-7114: in development mode the component id and the sequence must
	 * not run together, or repeater children such as {@code 1} and {@code 11} share an id.
	 */
	@Test
	void developmentModeIdsOfDifferentSequencesDiffer()
	{
		assertNotEquals(markupId("1", 0x1a), markupId("11", 0xa));
		assertNotEquals(markupId("ab", 0xc), markupId("a", 0xbc));
		assertNotEquals(markupId("1", 0x11), markupId("11", 0x1));
	}

	@Test
	void developmentModeIdsAreUniquePerSequence()
	{
		List<String> componentIds = List.of("1", "11", "111", "a", "ab", "abc", "b1", "a_", "a__",
			"_a", "x-1", "x.1", "x_1");
		Map<String, Integer> sequenceOf = new HashMap<>();
		for (String componentId : componentIds)
		{
			for (int sequence = 1; sequence < 0x300; sequence++)
			{
				if (sequence == 0xAD)
				{
					continue;
				}
				String markupId = markupId(componentId, sequence);
				Integer other = sequenceOf.putIfAbsent(markupId, sequence);
				if (other != null && other != sequence)
				{
					fail(markupId + " is generated for the sequences 0x" +
						Integer.toHexString(other) + " and 0x" + Integer.toHexString(sequence));
				}
			}
		}
	}

	@Test
	void developmentModeIdStartsWithTheComponentId()
	{
		assertEquals("link_1", markupId("link", 0x1));
		assertEquals("id1_1a", markupId("1", 0x1a));
	}

	static String markupId(String componentId, int sequence)
	{
		WebMarkupContainer component = new WebMarkupContainer(componentId);
		component.setMarkupIdImpl(sequence);
		return new DefaultMarkupIdGenerator().generateMarkupId(component, true);
	}
}
