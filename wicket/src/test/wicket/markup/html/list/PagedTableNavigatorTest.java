/*
 * $Id: PagedTableNavigatorTest.java 3764 2006-01-14 17:38:33 +0000 (Sat, 14 Jan
 * 2006) jonathanlocke $ $Revision$ $Date: 2006-01-14 17:38:33 +0000
 * (Sat, 14 Jan 2006) $
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

import java.io.IOException;
import java.util.List;

import junit.framework.TestCase;
import wicket.markup.html.link.Link;
import wicket.protocol.http.MockWebApplication;


/**
 * Test for simple table behavior.
 */
public class PagedTableNavigatorTest extends TestCase
{
	/**
	 * Construct.
	 */
	public PagedTableNavigatorTest()
	{
		super();
	}

	/**
	 * Construct.
	 * 
	 * @param name
	 *            name of test
	 */
	public PagedTableNavigatorTest(String name)
	{
		super(name);
	}

	/**
	 * Test simple table behavior.
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public void testPagedTable() throws Exception
	{
		MockWebApplication application = new MockWebApplication(null);
		application.setHomePage(PagedTableNavigatorPage.class);
		application.setupRequestAndResponse();
		application.processRequestCycle();
		PagedTableNavigatorPage page = (PagedTableNavigatorPage)application.getLastRenderedPage();
		String document = application.getServletResponse().getDocument();
		assertTrue(validatePage(document, "PagedTableNavigatorExpectedResult_1.html"));

		Link link = (Link)page.get("navigator:first");
		assertFalse(link.isEnabled());

		link = (Link)page.get("navigator:prev");
		assertFalse(link.isEnabled());

		link = (Link)page.get("navigator:next");
		assertTrue(link.isEnabled());

		link = (Link)page.get("navigator:last");
		assertTrue(link.isEnabled());

		link = (Link)page.get("navigator:next");
		application.setupRequestAndResponse();
		application.getServletRequest().setRequestToComponent(link);
		application.processRequestCycle();
		document = application.getServletResponse().getDocument();
		assertTrue(validatePage(document, "PagedTableNavigatorExpectedResult_2.html"));

		link = (Link)page.get("navigator:first");
		assertTrue(link.isEnabled());

		link = (Link)page.get("navigator:prev");
		assertTrue(link.isEnabled());

		link = (Link)page.get("navigator:next");
		assertTrue(link.isEnabled());

		link = (Link)page.get("navigator:last");
		assertTrue(link.isEnabled());

		link = (Link)page.get("navigator:prev");
		application.setupRequestAndResponse();
		application.getServletRequest().setRequestToComponent(link);
		application.processRequestCycle();
		document = application.getServletResponse().getDocument();
		assertTrue(validatePage(document, "PagedTableNavigatorExpectedResult_3.html"));

		link = (Link)page.get("navigator:first");
		assertFalse(link.isEnabled());

		link = (Link)page.get("navigator:prev");
		assertFalse(link.isEnabled());

		link = (Link)page.get("navigator:next");
		assertTrue(link.isEnabled());

		link = (Link)page.get("navigator:last");
		assertTrue(link.isEnabled());

		link = (Link)page.get("navigator:last");
		application.setupRequestAndResponse();
		application.getServletRequest().setRequestToComponent(link);
		application.processRequestCycle();
		document = application.getServletResponse().getDocument();
		assertTrue(validatePage(document, "PagedTableNavigatorExpectedResult_4.html"));

		link = (Link)page.get("navigator:first");
		assertTrue(link.isEnabled());

		link = (Link)page.get("navigator:prev");
		assertTrue(link.isEnabled());

		link = (Link)page.get("navigator:next");
		assertFalse(link.isEnabled());

		link = (Link)page.get("navigator:last");
		assertFalse(link.isEnabled());

		link = (Link)page.get("navigator:first");
		application.setupRequestAndResponse();
		application.getServletRequest().setRequestToComponent(link);
		application.processRequestCycle();
		document = application.getServletResponse().getDocument();
		assertTrue(validatePage(document, "PagedTableNavigatorExpectedResult_5.html"));

		link = (Link)page.get("navigator:first");
		assertFalse(link.isEnabled());

		link = (Link)page.get("navigator:prev");
		assertFalse(link.isEnabled());

		link = (Link)page.get("navigator:next");
		assertTrue(link.isEnabled());

		link = (Link)page.get("navigator:last");
		assertTrue(link.isEnabled());

		link = (Link)page.get("navigator:navigation:2:pageLink");
		application.setupRequestAndResponse();
		application.getServletRequest().setRequestToComponent(link);
		application.processRequestCycle();
		document = application.getServletResponse().getDocument();
		assertTrue(validatePage(document, "PagedTableNavigatorExpectedResult_6.html"));

		link = (Link)page.get("navigator:first");
		assertTrue(link.isEnabled());

		link = (Link)page.get("navigator:prev");
		assertTrue(link.isEnabled());

		link = (Link)page.get("navigator:next");
		assertTrue(link.isEnabled());

		link = (Link)page.get("navigator:last");
		assertTrue(link.isEnabled());

		link = (Link)page.get("navigator:prev");
		application.setupRequestAndResponse();
		application.getServletRequest().setRequestToComponent(link);
		application.processRequestCycle();
		document = application.getServletResponse().getDocument();
		assertTrue(validatePage(document, "PagedTableNavigatorExpectedResult_7.html"));

		link = (Link)page.get("navigator:first");
		assertTrue(link.isEnabled());

		link = (Link)page.get("navigator:prev");
		assertTrue(link.isEnabled());

		link = (Link)page.get("navigator:next");
		assertTrue(link.isEnabled());

		link = (Link)page.get("navigator:last");
		assertTrue(link.isEnabled());

		// add entries to the model list.
		List<String> modelData = (List)page.get("table").getModelObject();
		modelData.add("add-1");
		modelData.add("add-2");
		modelData.add("add-3");

		link = (Link)page.get("navigator:first");
		application.setupRequestAndResponse();
		application.getServletRequest().setRequestToComponent(link);
		application.processRequestCycle();
		document = application.getServletResponse().getDocument();
		assertTrue(validatePage(document, "PagedTableNavigatorExpectedResult_8.html"));
	}

	private boolean validatePage(final String document, final String file) throws IOException
	{
		return DiffUtil.validatePage(document, this.getClass(), file);
	}
}
