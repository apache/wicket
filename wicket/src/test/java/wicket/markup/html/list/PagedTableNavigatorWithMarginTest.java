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
package wicket.markup.html.list;

import java.io.IOException;

import junit.framework.TestCase;
import wicket.markup.html.link.Link;
import wicket.util.diff.DiffUtil;
import wicket.util.tester.WicketTester;


/**
 * Test for simple table behavior.
 */
public class PagedTableNavigatorWithMarginTest extends TestCase
{
	/**
	 * Construct.
	 */
	public PagedTableNavigatorWithMarginTest()
	{
		super();
	}

	/**
	 * Construct.
	 * 
	 * @param name
	 *            name of test
	 */
	public PagedTableNavigatorWithMarginTest(String name)
	{
		super(name);
	}

	/**
	 * Test simple table behavior.
	 * 
	 * @throws Exception
	 */
	public void testPagedTable() throws Exception
	{
		WicketTester tester = new WicketTester(PagedTableNavigatorWithMarginPage.class);
		tester.setupRequestAndResponse();
		tester.processRequestCycle();
		PagedTableNavigatorWithMarginPage page = (PagedTableNavigatorWithMarginPage)tester
				.getLastRenderedPage();
		String document = tester.getServletResponse().getDocument();
		assertTrue(validatePage(document, "PagedTableNavigatorWithMarginExpectedResult_1.html"));

		Link link = (Link)page.get("navigator:first");
		assertFalse(link.isEnabled());

		link = (Link)page.get("navigator:prev");
		assertFalse(link.isEnabled());

		link = (Link)page.get("navigator:next");
		assertTrue(link.isEnabled());

		link = (Link)page.get("navigator:last");
		assertTrue(link.isEnabled());

		link = (Link)page.get("navigator:next");
		tester.setupRequestAndResponse();
		tester.getServletRequest().setRequestToComponent(link);
		tester.processRequestCycle();
		document = tester.getServletResponse().getDocument();
		assertTrue(validatePage(document, "PagedTableNavigatorWithMarginExpectedResult_2.html"));

		link = (Link)page.get("navigator:first");
		assertTrue(link.isEnabled());

		link = (Link)page.get("navigator:prev");
		assertTrue(link.isEnabled());

		link = (Link)page.get("navigator:next");
		assertTrue(link.isEnabled());

		link = (Link)page.get("navigator:last");
		assertTrue(link.isEnabled());

		link = (Link)page.get("navigator:prev");
		tester.setupRequestAndResponse();
		tester.getServletRequest().setRequestToComponent(link);
		tester.processRequestCycle();
		document = tester.getServletResponse().getDocument();
		assertTrue(validatePage(document, "PagedTableNavigatorWithMarginExpectedResult_3.html"));

		link = (Link)page.get("navigator:first");
		assertFalse(link.isEnabled());

		link = (Link)page.get("navigator:prev");
		assertFalse(link.isEnabled());

		link = (Link)page.get("navigator:next");
		assertTrue(link.isEnabled());

		link = (Link)page.get("navigator:last");
		assertTrue(link.isEnabled());

		link = (Link)page.get("navigator:last");
		tester.setupRequestAndResponse();
		tester.getServletRequest().setRequestToComponent(link);
		tester.processRequestCycle();
		document = tester.getServletResponse().getDocument();
		assertTrue(validatePage(document, "PagedTableNavigatorWithMarginExpectedResult_4.html"));

		link = (Link)page.get("navigator:first");
		assertTrue(link.isEnabled());

		link = (Link)page.get("navigator:prev");
		assertTrue(link.isEnabled());

		link = (Link)page.get("navigator:next");
		assertFalse(link.isEnabled());

		link = (Link)page.get("navigator:last");
		assertFalse(link.isEnabled());

		link = (Link)page.get("navigator:first");
		tester.setupRequestAndResponse();
		tester.getServletRequest().setRequestToComponent(link);
		tester.processRequestCycle();
		document = tester.getServletResponse().getDocument();
		assertTrue(validatePage(document, "PagedTableNavigatorWithMarginExpectedResult_5.html"));

		link = (Link)page.get("navigator:first");
		assertFalse(link.isEnabled());

		link = (Link)page.get("navigator:prev");
		assertFalse(link.isEnabled());

		link = (Link)page.get("navigator:next");
		assertTrue(link.isEnabled());

		link = (Link)page.get("navigator:last");
		assertTrue(link.isEnabled());

		link = (Link)page.get("navigator:navigation:3:pageLink");
		tester.setupRequestAndResponse();
		tester.getServletRequest().setRequestToComponent(link);
		tester.processRequestCycle();
		document = tester.getServletResponse().getDocument();
		assertTrue(validatePage(document, "PagedTableNavigatorWithMarginExpectedResult_6.html"));

		link = (Link)page.get("navigator:first");
		assertTrue(link.isEnabled());

		link = (Link)page.get("navigator:prev");
		assertTrue(link.isEnabled());

		link = (Link)page.get("navigator:next");
		assertTrue(link.isEnabled());

		link = (Link)page.get("navigator:last");
		assertTrue(link.isEnabled());

		link = (Link)page.get("navigator:prev");
		tester.setupRequestAndResponse();
		tester.getServletRequest().setRequestToComponent(link);
		tester.processRequestCycle();
		document = tester.getServletResponse().getDocument();
		assertTrue(validatePage(document, "PagedTableNavigatorWithMarginExpectedResult_7.html"));

		link = (Link)page.get("navigator:first");
		assertTrue(link.isEnabled());

		link = (Link)page.get("navigator:prev");
		assertTrue(link.isEnabled());

		link = (Link)page.get("navigator:next");
		assertTrue(link.isEnabled());

		link = (Link)page.get("navigator:last");
		assertTrue(link.isEnabled());
	}

	private boolean validatePage(final String document, final String file) throws IOException
	{
		return DiffUtil.validatePage(document, this.getClass(), file);
	}
}
