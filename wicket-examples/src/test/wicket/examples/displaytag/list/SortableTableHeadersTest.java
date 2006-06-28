/*
 * $Id: SortableTableHeadersTest.java 5395 2006-04-16 13:42:28 +0000 (Sun, 16
 * Apr 2006) jdonnerstag $ $Revision$ $Date: 2006-04-16 13:42:28 +0000
 * (Sun, 16 Apr 2006) $
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
package wicket.examples.displaytag.list;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.examples.WicketTestCase;
import wicket.markup.html.link.Link;
import wicket.protocol.http.MockHttpServletResponse;
import wicket.protocol.http.MockWebApplication;
import wicket.settings.IRequestCycleSettings.RenderStrategy;
import wicket.util.diff.DiffUtil;


/**
 * Test for simple table behavior.
 */
public class SortableTableHeadersTest extends WicketTestCase
{
	private static final Log log = LogFactory.getLog(SortableTableHeadersTest.class);

	/**
	 * Construct.
	 * 
	 * @param name
	 *            name of test
	 */
	public SortableTableHeadersTest(String name)
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
		application.getRequestCycleSettings().setRenderStrategy(
				RenderStrategy.REDIRECT_TO_BUFFER);
		application.setHomePage(SortableTableHeadersPage.class);
		application.setupRequestAndResponse();
		application.processRequestCycle();
		SortableTableHeadersPage page = (SortableTableHeadersPage)application.getLastRenderedPage();
		String document = application.getServletResponse().getDocument();
		assertTrue(DiffUtil.validatePage(document, this.getClass(),
				"SortableTableHeadersExpectedResult_1.html"));

		Link link = (Link)page.get("header:id:actionLink");
		assertTrue(link.isEnabled());

		link = (Link)page.get("header:name:actionLink");
		assertTrue(link.isEnabled());

		link = (Link)page.get("header:email:actionLink");
		assertNull(link);

		link = (Link)page.get("header:name:actionLink");
		application.setupRequestAndResponse();
		application.getServletRequest().setRequestToComponent(link);
		application.processRequestCycle();

		// Check that redirect was set as expected and invoke it
		MockHttpServletResponse redirectResponse = application.getServletResponse();

		assertTrue("Response should be a redirect", redirectResponse.isRedirect());
		String redirect = application.getServletResponse().getRedirectLocation();
		application.setupRequestAndResponse();
		application.getServletRequest().setRequestToRedirectString(redirect);
		application.processRequestCycle();

		document = application.getServletResponse().getDocument();
		assertTrue(DiffUtil.validatePage(document, this.getClass(),
				"SortableTableHeadersExpectedResult_2.html"));

		// reverse sorting
		link = (Link)page.get("header:name:actionLink");
		application.setupRequestAndResponse();
		application.getServletRequest().setRequestToComponent(link);
		application.processRequestCycle();

		// Check that redirect was set as expected and invoke it
		// Check that wicket:border tag gets removed
		assertTrue("Response should be a redirect", application.getServletResponse().isRedirect());
		application.getMarkupSettings().setStripWicketTags(true);
		redirect = application.getServletResponse().getRedirectLocation();
		application.setupRequestAndResponse();
		application.getServletRequest().setRequestToRedirectString(redirect);
		application.processRequestCycle();

		document = application.getServletResponse().getDocument();
		assertTrue(DiffUtil.validatePage(document, this.getClass(),
				"SortableTableHeadersExpectedResult_3.html"));
	}
}
