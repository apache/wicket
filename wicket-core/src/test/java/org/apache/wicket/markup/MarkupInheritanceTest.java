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
package org.apache.wicket.markup;

import org.apache.wicket.WicketTestCase;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.util.tester.DiffUtil;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 */
public class MarkupInheritanceTest extends WicketTestCase
{
	private static final Logger log = LoggerFactory.getLogger(MarkupInheritanceTest.class);

	/**
	 * TEST FOR WICKET-1507
	 * 
	 * @throws Exception
	 */
	@Test
	public void renderChildPageWithStyleVariation() throws Exception
	{
		// first, render page with no style
		executeTest(MarkupInheritanceExtension_1.class, "MarkupInheritanceExpectedResult_1.html");

		// then, render with style1
		tester.getSession().setStyle("style1");
		tester.startPage(MarkupInheritanceExtension_1.class);
		tester.assertRenderedPage(MarkupInheritanceExtension_1.class);
		tester.assertResultPage(getClass(), "MarkupInheritanceExpectedResult_1_style1.html");

		// then, render with style2
		tester.getSession().setStyle("style2");
		tester.startPage(MarkupInheritanceExtension_1.class);
		tester.assertRenderedPage(MarkupInheritanceExtension_1.class);
		tester.assertResultPage(getClass(), "MarkupInheritanceExpectedResult_1_style2.html");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void renderHomePage_1() throws Exception
	{
		executeTest(MarkupInheritanceExtension_1.class, "MarkupInheritanceExpectedResult_1.html");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void renderHomePage_2() throws Exception
	{
		executeTest(MarkupInheritanceExtension_2.class, "MarkupInheritanceExpectedResult_2.html");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void renderHomePage_3() throws Exception
	{
		executeTest(MarkupInheritanceExtension_3.class, "MarkupInheritanceExpectedResult_3.html");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void renderHomePage_4() throws Exception
	{
		System.out.println("=== " + MarkupInheritanceExtension_4.class.getName() + " ===");

		tester.startPage(MarkupInheritanceExtension_4.class);

		// Validate the document
		assertEquals(MarkupInheritanceExtension_4.class, tester.getLastRenderedPage().getClass());
		String document = tester.getLastResponse().getDocument();
		DiffUtil.validatePage(document, getClass(), "MarkupInheritanceExpectedResult_4.html", true);

		MarkupInheritanceExtension_4 page = (MarkupInheritanceExtension_4)tester.getLastRenderedPage();

		Link<?> link = (Link<?>)page.get("link");
		tester.clickLink(link.getPageRelativePath());

		assertEquals(MarkupInheritanceExtension_4.class, tester.getLastRenderedPage().getClass());

		document = tester.getLastResponse().getDocument();
		DiffUtil.validatePage(document, getClass(), "MarkupInheritanceExpectedResult_4-1.html",
			true);
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void renderHomePage_5() throws Exception
	{
		executeTest(MarkupInheritanceExtension_5.class, "MarkupInheritanceExpectedResult_5.html");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void renderHomePage_6() throws Exception
	{
		executeTest(MarkupInheritancePage_6.class, "MarkupInheritanceExpectedResult_6.html");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void renderHomePage_7() throws Exception
	{
		executeTest(MarkupInheritanceExtension_7.class, "MarkupInheritanceExpectedResult_7.html");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void renderHomePage_8() throws Exception
	{
		tester.getApplication().getMarkupSettings().setStripWicketTags(true);
		executeTest(MarkupInheritanceExtension_8.class, "MarkupInheritanceExpectedResult_8.html");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void renderHomePage_9() throws Exception
	{
		executeTest(MarkupInheritancePage_9.class, "MarkupInheritanceExpectedResult_9.html");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void renderHomePage_10() throws Exception
	{
		executeTest(MarkupInheritanceExtension_10.class, "MarkupInheritanceExpectedResult_10.html");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void renderHomePage_11() throws Exception
	{
		executeTest(MarkupInheritanceExtension_11.class, "MarkupInheritanceExpectedResult_11.html");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void renderHomePage_12() throws Exception
	{
		executeTest(MarkupInheritanceExtension_12.class, "MarkupInheritanceExpectedResult_12.html");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void renderHomePage_13() throws Exception
	{
		executeTest(MarkupInheritanceExtension_13.class, "MarkupInheritanceExpectedResult_13.html");
	}
}
