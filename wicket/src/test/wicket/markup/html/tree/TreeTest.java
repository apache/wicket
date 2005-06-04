/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.markup.html.tree;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.markup.html.list.DiffUtil;
import wicket.protocol.http.MockWebApplication;

/**
 * @author Chris Turner
 */
public class TreeTest extends TestCase
{
	private static Log log = LogFactory.getLog(TreeTest.class);

	private MockWebApplication application;

	/**
	 * Create the test.
	 * 
	 * @param name
	 *            The test name
	 */
	public TreeTest(String name)
	{
		super(name);
	}

	/**
	 * @throws Exception
	 */
/*	
	public void testRenderHomePage_1() throws Exception
	{
	    executeTest(TreePage_1.class, "TreePageExpectedResult_1.html");
	}
*/
	/**
	 * @throws Exception
	 */
	public void testRenderHomePage_1() throws Exception
	{
		System.out.println("=== " + TreePage.class.getName() + " ===");
		
		application = new MockWebApplication(null);
		application.getPages().setHomePage(TreePage.class);

		// Do the processing
		application.setupRequestAndResponse();
		application.processRequestCycle();

		// Validate the document
		String document = application.getServletResponse().getDocument();
		//System.out.println(document);

		assertTrue(DiffUtil.validatePage(document, this.getClass(), "TreePageExpectedResult_1.html"));

		TreePage page = (TreePage)application.getLastRenderedPage();
		application.setupRequestAndResponse();
		application.getServletRequest().setRequestToRedirectString("?component=0.tree.tree.3.node.junctionLink&version=0&interface=ILinkListener");
		application.processRequestCycle();
		document = application.getServletResponse().getDocument();

		assertTrue(DiffUtil.validatePage(document, this.getClass(), "TreePageExpectedResult_1-1.html"));
	}

	/**
	 * @param pageClass
	 * @param filename
	 * @throws Exception
	 */
	public void executeTest(final Class pageClass, final String filename) throws Exception
	{
		System.out.println("=== " + pageClass.getName() + " ===");
		
		application = new MockWebApplication(null);
		application.getPages().setHomePage(pageClass);

		// Do the processing
		application.setupRequestAndResponse();
		application.processRequestCycle();

		// Validate the document
		String document = application.getServletResponse().getDocument();
		System.out.println(document);

		assertTrue(DiffUtil.validatePage(document, this.getClass(), filename));
	}
}
