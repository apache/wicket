/*
 * $Id$
 * $Revision$ $Date$
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

import wicket.WicketTestCase;
import wicket.markup.html.link.Link;
import wicket.protocol.http.MockHttpServletResponse;
import wicket.settings.IRequestCycleSettings;
import wicket.util.diff.DiffUtil;
import wicket.util.tester.WicketTester;


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
		WicketTester application = new WicketTester();
		application.getApplication().getRequestCycleSettings().setRenderStrategy(
				IRequestCycleSettings.REDIRECT_TO_BUFFER);
		application.startPage(SortableTableHeadersPage.class);
		SortableTableHeadersPage page = (SortableTableHeadersPage)application.getLastRenderedPage();
		String document = application.getServletResponse().getDocument();
		DiffUtil.validatePage(document, this.getClass(), "SortableTableHeadersExpectedResult_1.html", true);

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
				"SortableTableHeadersExpectedResult_2.html", false));

		// reverse sorting
		link = (Link)page.get("header:name:actionLink");
		application.setupRequestAndResponse();
		application.getServletRequest().setRequestToComponent(link);
		application.processRequestCycle();

		// Check that redirect was set as expected and invoke it
		// Check that wicket:border tag gets removed
		assertTrue("Response should be a redirect", application.getServletResponse().isRedirect());
		application.getApplication().getMarkupSettings().setStripWicketTags(true);
		redirect = application.getServletResponse().getRedirectLocation();
		application.setupRequestAndResponse();
		application.getServletRequest().setRequestToRedirectString(redirect);
		application.processRequestCycle();

		document = application.getServletResponse().getDocument();
		assertTrue(DiffUtil.validatePage(document, this.getClass(),
				"SortableTableHeadersExpectedResult_3.html", false));
		application.destroy();
	}
}
