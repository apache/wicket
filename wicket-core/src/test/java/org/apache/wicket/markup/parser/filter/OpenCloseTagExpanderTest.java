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
package org.apache.wicket.markup.parser.filter;

import static org.hamcrest.CoreMatchers.instanceOf;

import java.text.ParseException;

import org.apache.wicket.WicketTestCase;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupElement;
import org.apache.wicket.markup.WicketTag;
import org.apache.wicket.markup.parser.AbstractMarkupFilter;
import org.apache.wicket.markup.parser.IMarkupFilter;
import org.apache.wicket.markup.parser.XmlTag;
import org.apache.wicket.markup.resolver.HtmlHeaderResolver;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

/**
 * 
 */
public class OpenCloseTagExpanderTest extends WicketTestCase
{
	/**
	 * @throws Exception
	 */
	@Test
	public void renderHomePage() throws Exception
	{
		executeTest(OpenCloseTagExpanderPage_1.class,
			"OpenCloseTagExpanderPageExpectedResult_1.html");
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-5237
	 * 
	 * @throws ParseException
	 */
	@Test
	public void doNotExpandVoidElements() throws ParseException
	{
		String[] htmlVoidElements = new String[] { "area", "base", "br", "col", "command", "embed",
				"hr", "img", "input", "keygen", "link", "meta", "param", "source", "track", "wbr" };

		for (String htmlVoidElement : htmlVoidElements)
		{
			OpenCloseTagExpander expander = new OpenCloseTagExpander()
			{
				@Override
				public IMarkupFilter getNextFilter()
				{
					return new AbstractMarkupFilter()
					{
						@Override
						protected MarkupElement onComponentTag(ComponentTag tag)
							throws ParseException
						{
							return null;
						}

						@Override
						public MarkupElement nextElement() throws ParseException
						{
							return new TestMarkupElement();
						}
					};
				}
			};

			ComponentTag tag = new ComponentTag(htmlVoidElement, XmlTag.TagType.OPEN_CLOSE);
			expander.onComponentTag(tag);

			MarkupElement markupElement = expander.nextElement();

			// assert the next element is returned by the parent
			assertThat(markupElement, instanceOf(TestMarkupElement.class));
		}
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-5237
	 * 
	 * @throws ParseException
	 */
	@Test
	public void expandNonVoidElements() throws ParseException
	{
		for (String htmlNonVoidElement : OpenCloseTagExpander.REPLACE_FOR_TAGS)
		{
			OpenCloseTagExpander expander = new OpenCloseTagExpander()
			{
				@Override
				public IMarkupFilter getNextFilter()
				{
					return new AbstractMarkupFilter()
					{
						@Override
						protected MarkupElement onComponentTag(ComponentTag tag)
							throws ParseException
						{
							return null;
						}

						@Override
						public MarkupElement nextElement() throws ParseException
						{
							return new TestMarkupElement();
						}
					};
				}
			};

			ComponentTag tag = new ComponentTag(htmlNonVoidElement, XmlTag.TagType.OPEN_CLOSE);
			expander.onComponentTag(tag);

			ComponentTag markupElement = (ComponentTag)expander.nextElement();

			// assert the next element is returned by the parent
			assertEquals(htmlNonVoidElement, markupElement.getName());
			assertTrue(markupElement.closes(tag));
		}
	}

	/**
	 * Verifies that the namespace of the created closing tag is the same
	 * as of the opening one
	 *
	 * @throws ParseException
	 */
	@Test
	public void expandWicketTagWithSameNamespace() throws ParseException
	{
		final String namespace = "customNS";

		OpenCloseTagExpander expander = new OpenCloseTagExpander()
		{
			@Override
			protected String getWicketNamespace()
			{
				return namespace;
			}
		};

		ComponentTag tag = new ComponentTag(HtmlHeaderResolver.HEADER_ITEMS, XmlTag.TagType.OPEN_CLOSE);
		tag.setNamespace(namespace);
		expander.onComponentTag(tag);

		MarkupElement markupElement = expander.nextElement();

		assertThat(markupElement, CoreMatchers.instanceOf(WicketTag.class));
		assertTrue(markupElement.closes(tag));
		assertEquals(namespace, ((ComponentTag) markupElement).getNamespace());
	}

	private static class TestMarkupElement extends WicketTag
	{
		public TestMarkupElement()
		{
			super(new XmlTag());
		}
	}
}
