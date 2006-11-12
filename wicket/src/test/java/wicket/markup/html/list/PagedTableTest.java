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

import junit.framework.TestCase;
import wicket.markup.html.link.Link;
import wicket.protocol.http.documentvalidation.HtmlDocumentValidator;
import wicket.protocol.http.documentvalidation.Tag;
import wicket.protocol.http.documentvalidation.TextContent;
import wicket.util.diff.DiffUtil;
import wicket.util.tester.WicketTester;


/**
 * Test for simple table behavior.
 */
public class PagedTableTest extends TestCase
{

	/**
	 * Construct.
	 */
	public PagedTableTest()
	{
		super();
	}

	/**
	 * Construct.
	 * 
	 * @param name
	 *            name of test
	 */
	public PagedTableTest(String name)
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
		WicketTester tester = new WicketTester(PagedTablePage.class);
		tester.setupRequestAndResponse();
		tester.processRequestCycle();
		PagedTablePage page = (PagedTablePage)tester.getLastRenderedPage();
		String document = tester.getServletResponse().getDocument();
		assertTrue(validatePage1(document));

		Link link = (Link)page.get("navigation:1:pageLink");
		tester.setupRequestAndResponse();
		tester.getServletRequest().setRequestToComponent(link);
		tester.processRequestCycle();
		document = tester.getServletResponse().getDocument();
		assertTrue(DiffUtil.validatePage(document, this.getClass(),
				"PagedTablePageExpectedResult.html"));
	}

	/**
	 * Validates page 1 of paged table.
	 * 
	 * @param document
	 *            The document
	 * @return The validation result
	 */
	private boolean validatePage1(String document)
	{
		HtmlDocumentValidator validator = new HtmlDocumentValidator();
		Tag html = new Tag("html");
		Tag head = new Tag("head");
		html.addExpectedChild(head);
		Tag title = new Tag("title");
		head.addExpectedChild(title);
		title.addExpectedChild(new TextContent("Paged Table Page"));
		Tag body = new Tag("body");
		html.addExpectedChild(body);

		Tag ulTable = new Tag("ul");
		ulTable.addExpectedChild(new Tag("li").addExpectedChild(new Tag("span")
				.addExpectedChild(new TextContent("one"))));
		ulTable.addExpectedChild(new Tag("li").addExpectedChild(new Tag("span")
				.addExpectedChild(new TextContent("two"))));
		// note that we DO NOT expect the third element as this is not on the
		// current page
		body.addExpectedChild(ulTable);

		Tag ulNav = new Tag("ul");
		ulNav.addExpectedChild(new Tag("li").addExpectedChild(new Tag("span")
				.addExpectedChild(new Tag("em").addExpectedChild(new Tag("span")
						.addExpectedChild(new TextContent("1"))))));
		ulNav.addExpectedChild(new Tag("li").addExpectedChild(new Tag("a")
				.addExpectedChild(new Tag("span").addExpectedChild(new TextContent("2")))));

		body.addExpectedChild(ulNav);

		validator.addRootElement(html);

		return validator.isDocumentValid(document);
	}
}
