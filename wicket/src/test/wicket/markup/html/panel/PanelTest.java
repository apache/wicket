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
package wicket.markup.html.panel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.WicketRuntimeException;
import wicket.WicketTestCase;
import wicket.markup.MarkupException;

/**
 * Simple application that demonstrates the mock http application code (and
 * checks that it is working)
 * 
 * @author Chris Turner
 */
public class PanelTest extends WicketTestCase
{
	private static Log log = LogFactory.getLog(PanelTest.class);

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
		catch (WicketRuntimeException ex)
		{
			if ((ex.getCause() != null) && (ex.getCause() instanceof MarkupException))
			{
				hit = true;
				
				MarkupException mex = (MarkupException)ex.getCause();
				assertNotNull(mex.getMarkupStream());
				assertTrue(mex.getMessage().indexOf("did not have a close tag") != -1);
				assertTrue(mex.getMessage().indexOf("SimplePanel_1.html") != -1);
			}
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
		catch (WicketRuntimeException ex)
		{
			if ((ex.getCause() != null) && (ex.getCause() instanceof MarkupException))
			{
				hit = true;
				
				MarkupException mex = (MarkupException)ex.getCause();
				assertNotNull(mex.getMarkupStream());
				assertTrue(mex.getMessage().indexOf("has to contain part '<wicket:panel>'") != -1);
				assertTrue(mex.getMessage().indexOf("SimplePanel_2.html") != -1);
			}
		}
		assertTrue("Did expect a MarkupException", hit);
	}
}
