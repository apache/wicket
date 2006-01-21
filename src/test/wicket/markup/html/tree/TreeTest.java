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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Page;
import wicket.WicketTestCase;
import wicket.markup.html.list.DiffUtil;

/**
 * Test for the Tree component. Also tests header insertion as that is what the Tree
 * component uses.
 *
 * @author Juergen Donnerstag
 * @author Eelco Hillenius
 */
public class TreeTest extends WicketTestCase
{
	private static Log log = LogFactory.getLog(TreeTest.class);

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
	 * Test Tree put on a plain page. Tests first render, and render after a node click.
	 * @throws Exception
	 */
	public void testRenderTreePage_1() throws Exception
	{
		executeTest(TreePage.class, "TreePageExpectedResult_1.html");

		Page page = application.getLastRenderedPage();
		application.setupRequestAndResponse();
		application.getServletRequest().setRequestToRedirectString("?path=0:tree:tree:3:node:junctionLink&interface=ILinkListener");
		application.processRequestCycle();
		String document = application.getServletResponse().getDocument();

		assertTrue(DiffUtil.validatePage(document, this.getClass(), "TreePageExpectedResult_1-1.html"));
	}

	/**
	 * Test Tree put on a page with a border. Tests first render, and render after a node click.
	 * @throws Exception
	 */
	public void testRenderTreePageWithBorder_1() throws Exception
	{
		executeTest(TreePageWithBorder.class, "TreePageWithBorderExpectedResult_1.html");

		Page page = application.getLastRenderedPage();
		application.setupRequestAndResponse();
		application.getServletRequest().setRequestToRedirectString("?path=0:border:tree:tree:3:node:junctionLink&interface=ILinkListener");
		application.processRequestCycle();
		String document = application.getServletResponse().getDocument();

		assertTrue(DiffUtil.validatePage(document, this.getClass(), "TreePageWithBorderExpectedResult_1-1.html"));
	}

	/**
	 * Test Tree put on a plain page with a html head section, but without a wicket:head tag.
	 * Tests first render, and render after a node click.
	 * @throws Exception
	 */
	public void testRenderTreePageNoWicketHeadTag_1() throws Exception
	{
		executeTest(TreePageNoWicketHeadTag.class, "TreePageNoWicketHeadTagExpectedResult_1.html");

		Page page = application.getLastRenderedPage();
		application.setupRequestAndResponse();
		application.getServletRequest().setRequestToRedirectString("?path=0:tree:tree:3:node:junctionLink&interface=ILinkListener");
		application.processRequestCycle();
		String document = application.getServletResponse().getDocument();

		assertTrue(DiffUtil.validatePage(document, this.getClass(), "TreePageNoWicketHeadTagExpectedResult_1-1.html"));
	}
}
