/*
 * $Id: PanelTest.java 5687 2006-05-08 13:00:54 +0000 (Mon, 08 May 2006)
 * jdonnerstag $ $Revision$ $Date: 2006-05-08 13:00:54 +0000 (Mon, 08 May
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
package wicket.markup.html.panel;

import wicket.WicketTestCase;
import wicket.markup.MarkupException;

/**
 * Simple tester that demonstrates the mock http tester code (and
 * checks that it is working)
 * 
 * @author Chris Turner
 */
public class PanelTest extends WicketTestCase
{
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
			assertTrue(mex.getMessage().indexOf("does not have a close tag") != -1);
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
			assertTrue(mex.getMessage().indexOf("Unable to load associated markup file") != -1);
		}
		assertTrue("Did expect a MarkupException", hit);
	}

	/**
	 * @throws Exception
	 */
	public void testInlinePanel() throws Exception
	{
		executeTest(InlinePanelPage_1.class, "InlinePanelPageExpectedResult_1.html");
	}

	/**
	 * @throws Exception
	 */
	public void testInlinePanel_2() throws Exception
	{
		executeTest(InlinePanelPage_2.class, "InlinePanelPageExpectedResult_2.html");
	}

	/**
	 * @throws Exception
	 */
	public void testInlinePanel_3() throws Exception
	{
		executeTest(InlinePanelPage_3.class, "InlinePanelPageExpectedResult_3.html");
	}

	/**
	 * @throws Exception
	 */
	public void testInlinePanel_4() throws Exception
	{
		executeTest(InlinePanelPage_4.class, "InlinePanelPageExpectedResult_4.html");
	}

	/**
	 * @throws Exception
	 */
	public void testInlinePanel_6() throws Exception
	{
		executeTest(InlinePanelPage_6.class, "InlinePanelPageExpectedResult_6.html");
	}

	/**
	 * @throws Exception
	 */
	public void testPanelWithAttributeModifier() throws Exception
	{
		executeTest(PanelWithAttributeModifierPage.class,
				"PanelWithAttributeModifierPageExpectedResult_1.html");
	}
}
