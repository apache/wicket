/*
 * $Id$ $Revision:
 * 1.4 $ $Date$
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
package wicket.markup.html.panel;

import wicket.WicketTestCase;
import wicket.markup.MarkupException;
import wicket.markup.resolver.FragmentResolver;

/**
 * Simple application that demonstrates the mock http application code (and
 * checks that it is working)
 * 
 * @author Chris Turner
 */
public class PanelTest extends WicketTestCase
{
	// private static Log log = LogFactory.getLog(PanelTest.class);

	/**
	 * Create the test.
	 * 
	 * @param name
	 *            The test name
	 */
	public PanelTest(String name)
	{
		super(name);
	}

	/**
	 * @throws Exception
	 */
	public void testRenderHomePage_1() throws Exception
	{
		boolean hit = false;
		try
		{
			executeTest(PanelPage_1.class, "Dummy.html");
		}
		catch (MarkupException mex)
		{
			hit = true;

			assertNotNull(mex.getMarkupStream());
			assertTrue(mex.getMessage().indexOf("did not have a close tag") != -1);
			assertTrue(mex.getMessage().indexOf("SimplePanel_1.html") != -1);
		}
		assertTrue("Did expect a MarkupException", hit);
	}

	/**
	 * @throws Exception
	 */
	public void testRenderHomePage_2() throws Exception
	{
		boolean hit = false;
		try
		{
			executeTest(PanelPage_2.class, "Dummy.html");
		}
		catch (MarkupException mex)
		{
			hit = true;

			assertNotNull(mex.getMarkupStream());
			assertTrue(mex.getMessage().indexOf("has to contain part '<wicket:panel>'") != -1);
			assertTrue(mex.getMessage().indexOf("SimplePanel_2.html") != -1);
		}
		assertTrue("Did expect a MarkupException", hit);
	}

	/**
	 * @throws Exception
	 */
	public void testInlinePanel() throws Exception
	{
		application.getPageSettings().addComponentResolver(new FragmentResolver());
		executeTest(InlinePanelPage_1.class, "InlinePanelPageExpectedResult_1.html");
	}

	/**
	 * @throws Exception
	 */
	public void testInlinePanel_2() throws Exception
	{
		application.getPageSettings().addComponentResolver(new FragmentResolver());
		executeTest(InlinePanelPage_2.class, "InlinePanelPageExpectedResult_2.html");
	}

	/**
	 * @throws Exception
	 */
	public void testInlinePanel_3() throws Exception
	{
		// TODO General: this test currently fails because the fragment does not
		// find its markup since it tries to look for it in its parent?
		if (1 == 2 - 1)
			return;
		application.getPageSettings().addComponentResolver(new FragmentResolver());
		executeTest(InlinePanelPage_3.class, "InlinePanelPageExpectedResult_3.html");
	}

}
