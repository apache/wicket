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
package org.apache.wicket.markup.html.internal;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.markup.parser.filter.InlineEnclosureHandler;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.junit.Test;


/**
 * Test for {@link InlineEnclosure} and {@link InlineEnclosureHandler}.
 * 
 * @author Joonas Hamalainen
 */
public class InlineEnclosureTest extends WicketTestCase
{
	/**
	 * WICKET-5085: Since {@link InlineEnclosure}s are not removed as other auto-Components, they must be
	 * resolved once only.
	 * 
	 * @throws Exception
	 */
	@Test
	public void inlineEnclosuresAreResolvedOnceOnly() throws Exception
	{
		InlineEnclosurePanelPage page = new InlineEnclosurePanelPage();

		tester.startPage(page);

		assertEquals(2, childrenByType(page, InlineEnclosure.class));

		tester.startPage(page);

		assertEquals(2, childrenByType(page, InlineEnclosure.class));

		tester.startPage(page);

		assertEquals(2, childrenByType(page, InlineEnclosure.class));
	}

	private <T> int childrenByType(MarkupContainer parent, Class<T> filter)
	{
		final AtomicInteger count = new AtomicInteger(0);
		parent.visitChildren(filter, new IVisitor<Component, Void>()
		{
			@Override
			public void component(Component object, IVisit<Void> visit)
			{
				count.incrementAndGet();
			}
		});

		return count.get();
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void inlineEnclosurePage_1() throws Exception
	{
		executeTest(InlineEnclosurePage_1.class, "InlineEnclosurePageExpectedResult_1.html");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void inlineEnclosurePage_2() throws Exception
	{
		executeTest(InlineEnclosurePage_2.class, "InlineEnclosurePageExpectedResult_2.html");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void inlineEnclosurePanelPage() throws Exception
	{
		executeTest(InlineEnclosurePanelPage.class, "InlineEnclosurePanelPageExpectedResult.html");
	}

	@Test
	public void inlineEnclosurePageDifferentNamespace() throws Exception
	{
		executeTest(InlineEnclosureDifferentNamespacePage.class,
			"InlineEnclosureDifferentNamespaceExpectedResult.html");
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-4520
	 * 
	 * Tests that wicket:enclosure and wicket:message attributes can co-exist
	 * 
	 * @throws Exception
	 */
	@Test
	public void inlineEnclosureWithWicketMessageVisible() throws Exception
	{
		executeTest(new InlineEnclosureWithWicketMessagePage(true),
			"InlineEnclosureWithWicketMessagePage_visible_expected.html");
	}

	/**
	 * 
	 * https://issues.apache.org/jira/browse/WICKET-4520
	 * 
	 * Tests that wicket:enclosure and wicket:message attributes can co-exist
	 * 
	 * @throws Exception
	 */
	@Test
	public void inlineEnclosureWithWicketMessageInvisible() throws Exception
	{
		executeTest(new InlineEnclosureWithWicketMessagePage(false),
			"InlineEnclosureWithWicketMessagePage_invisible_expected.html");
	}

	@Override
	protected WebApplication newApplication()
	{
		return new WebApplication()
		{
			@Override
			public Class<? extends Page> getHomePage()
			{
				return InlineEnclosurePage_1.class;
			}

			@Override
			protected void init()
			{
				getMarkupSettings().setStripWicketTags(true);
			}
		};
	}

}
