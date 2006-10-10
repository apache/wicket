/*
 * $Id: PagedTableTest.java 5131 2006-03-26 10:12:04 +0000 (Sun, 26 Mar 2006)
 * jdonnerstag $ $Revision$ $Date: 2006-03-26 10:12:04 +0000 (Sun, 26 Mar
 * 2006) $
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.markup.html.list;

import junit.framework.TestCase;
import wicket.markup.html.link.Link;
import wicket.protocol.http.MockWebApplication;
import wicket.protocol.http.documentvalidation.HtmlDocumentValidator;
import wicket.protocol.http.documentvalidation.Tag;
import wicket.protocol.http.documentvalidation.TextContent;
import wicket.util.diff.DiffUtil;


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
		MockWebApplication application = new MockWebApplication(null);
		application.setHomePage(PagedTablePage.class);
		application.setupRequestAndResponse();
		application.processRequestCycle();
		PagedTablePage page = (PagedTablePage)application.getLastRenderedPage();
		String document = application.getServletResponse().getDocument();
		assertTrue(validatePage1(document));

		Link link = (Link)page.get("navigation:1:pageLink");
		application.setupRequestAndResponse();
		application.getServletRequest().setRequestToComponent(link);
		application.processRequestCycle();
		document = application.getServletResponse().getDocument();
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
