/*
 * $Id: TreeTest.java 4680 2006-02-28 21:11:40 +0000 (Tue, 28 Feb 2006)
 * jdonnerstag $ $Revision$ $Date: 2006-02-28 21:11:40 +0000 (Tue, 28 Feb
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
package wicket.extensions.markup.html.tree;

import wicket.WicketTestCase;

/**
 * Test for the Tree component. Also tests header insertion as that is what the
 * Tree component uses.
 * 
 * @author Juergen Donnerstag
 * @author Eelco Hillenius
 */
public class TreeTest extends WicketTestCase
{
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
	 * Test Tree put on a plain page. Tests first render, and render after a
	 * node click.
	 * 
	 * @throws Exception
	 */
	public void testRenderTreePage_1() throws Exception
	{
		executeTest(TreePage.class, "TreePageExpectedResult_1.html");

		executedListener(TreePage.class, application.getLastRenderedPage().get(
				"tree:tree:3:node:junctionLink"), "TreePageExpectedResult_1-1.html");
	}

	/**
	 * Test Tree put on a page with a border. Tests first render, and render
	 * after a node click.
	 * 
	 * @throws Exception
	 */
	public void testRenderTreePageWithBorder_1() throws Exception
	{
		executeTest(TreePageWithBorder.class, "TreePageWithBorderExpectedResult_1.html");

		executedListener(TreePageWithBorder.class, application.getLastRenderedPage().get(
				"tree:tree:3:node:junctionLink"), "TreePageWithBorderExpectedResult_1-1.html");
	}

	/**
	 * Test Tree put on a plain page with a html head section, but without a
	 * wicket:head tag. Tests first render, and render after a node click.
	 * 
	 * @throws Exception
	 */
	public void testRenderTreePageNoWicketHeadTag_1() throws Exception
	{
		executeTest(TreePageNoWicketHeadTag.class, "TreePageNoWicketHeadTagExpectedResult_1.html");

		executedListener(TreePageNoWicketHeadTag.class, application.getLastRenderedPage().get(
				"tree:tree:3:node:junctionLink"), "TreePageNoWicketHeadTagExpectedResult_1-1.html");
	}
}
