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
package org.apache.wicket.markup.html.list;

import java.util.Locale;

import org.apache.wicket.WicketTestCase;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.util.tester.DiffUtil;
import org.junit.Test;


/**
 * Test for simple table behavior.
 */
public class PagedTableNavigatorWithMarginTest extends WicketTestCase
{

	/**
	 * Test simple table behavior.
	 * 
	 * @throws Exception
	 */
	@Test
	public void pagedTable() throws Exception
	{
		tester.getSession().setLocale(Locale.ENGLISH);
		tester.startPage(PagedTableNavigatorWithMarginPage.class);
		PagedTableNavigatorWithMarginPage page = (PagedTableNavigatorWithMarginPage)tester.getLastRenderedPage();
		String document = tester.getLastResponseAsString();
		DiffUtil.validatePage(document, this.getClass(),
			"PagedTableNavigatorWithMarginExpectedResult_1.html", true);

		Link<?> link = (Link<?>)page.get("navigator:first");
		assertFalse(link.isEnabled());

		link = (Link<?>)page.get("navigator:prev");
		assertFalse(link.isEnabled());

		link = (Link<?>)page.get("navigator:next");
		assertTrue(link.isEnabled());

		link = (Link<?>)page.get("navigator:last");
		assertTrue(link.isEnabled());

		link = (Link<?>)page.get("navigator:next");
		tester.clickLink(link.getPageRelativePath());
		document = tester.getLastResponseAsString();
		DiffUtil.validatePage(document, this.getClass(),
			"PagedTableNavigatorWithMarginExpectedResult_2.html", true);

		link = (Link<?>)page.get("navigator:first");
		assertTrue(link.isEnabled());

		link = (Link<?>)page.get("navigator:prev");
		assertTrue(link.isEnabled());

		link = (Link<?>)page.get("navigator:next");
		assertTrue(link.isEnabled());

		link = (Link<?>)page.get("navigator:last");
		assertTrue(link.isEnabled());

		link = (Link<?>)page.get("navigator:prev");
		tester.clickLink(link.getPageRelativePath());
		document = tester.getLastResponseAsString();
		DiffUtil.validatePage(document, this.getClass(),
			"PagedTableNavigatorWithMarginExpectedResult_3.html", true);

		link = (Link<?>)page.get("navigator:first");
		assertFalse(link.isEnabled());

		link = (Link<?>)page.get("navigator:prev");
		assertFalse(link.isEnabled());

		link = (Link<?>)page.get("navigator:next");
		assertTrue(link.isEnabled());

		link = (Link<?>)page.get("navigator:last");
		assertTrue(link.isEnabled());

		link = (Link<?>)page.get("navigator:last");
		tester.clickLink(link.getPageRelativePath());
		document = tester.getLastResponseAsString();
		DiffUtil.validatePage(document, this.getClass(),
			"PagedTableNavigatorWithMarginExpectedResult_4.html", true);

		link = (Link<?>)page.get("navigator:first");
		assertTrue(link.isEnabled());

		link = (Link<?>)page.get("navigator:prev");
		assertTrue(link.isEnabled());

		link = (Link<?>)page.get("navigator:next");
		assertFalse(link.isEnabled());

		link = (Link<?>)page.get("navigator:last");
		assertFalse(link.isEnabled());

		link = (Link<?>)page.get("navigator:first");
		tester.clickLink(link.getPageRelativePath());
		document = tester.getLastResponseAsString();
		DiffUtil.validatePage(document, this.getClass(),
			"PagedTableNavigatorWithMarginExpectedResult_5.html", true);

		link = (Link<?>)page.get("navigator:first");
		assertFalse(link.isEnabled());

		link = (Link<?>)page.get("navigator:prev");
		assertFalse(link.isEnabled());

		link = (Link<?>)page.get("navigator:next");
		assertTrue(link.isEnabled());

		link = (Link<?>)page.get("navigator:last");
		assertTrue(link.isEnabled());

		link = (Link<?>)page.get("navigator:navigation:3:pageLink");
		tester.clickLink(link.getPageRelativePath());
		document = tester.getLastResponseAsString();
		DiffUtil.validatePage(document, this.getClass(),
			"PagedTableNavigatorWithMarginExpectedResult_6.html", true);

		link = (Link<?>)page.get("navigator:first");
		assertTrue(link.isEnabled());

		link = (Link<?>)page.get("navigator:prev");
		assertTrue(link.isEnabled());

		link = (Link<?>)page.get("navigator:next");
		assertTrue(link.isEnabled());

		link = (Link<?>)page.get("navigator:last");
		assertTrue(link.isEnabled());

		link = (Link<?>)page.get("navigator:prev");
		tester.clickLink(link.getPageRelativePath());
		document = tester.getLastResponseAsString();
		DiffUtil.validatePage(document, this.getClass(),
			"PagedTableNavigatorWithMarginExpectedResult_7.html", true);

		link = (Link<?>)page.get("navigator:first");
		assertTrue(link.isEnabled());

		link = (Link<?>)page.get("navigator:prev");
		assertTrue(link.isEnabled());

		link = (Link<?>)page.get("navigator:next");
		assertTrue(link.isEnabled());

		link = (Link<?>)page.get("navigator:last");
		assertTrue(link.isEnabled());
	}
}
