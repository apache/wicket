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
package wicket.markup.html.basic;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.WicketRuntimeException;
import wicket.WicketTestCase;
import wicket.markup.MarkupException;
import wicket.markup.MarkupNotFoundException;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.border.Border;
import wicket.markup.html.panel.Panel;

/**
 * Simple application that demonstrates the mock http application code (and
 * checks that it is working)
 * 
 * @author Chris Turner
 */
public class SimplePageTest extends WicketTestCase
{
	private static Log log = LogFactory.getLog(SimplePageTest.class);

	/**
	 * Create the test.
	 * 
	 * @param name
	 *            The test name
	 */
	public SimplePageTest(String name)
	{
		super(name);
	}

	/**
	 * @throws Exception
	 */
	public void testRenderHomePage() throws Exception
	{
	    executeTest(SimplePage.class, "SimplePageExpectedResult.html");
	}

	/**
	 * @throws Exception
	 */
	public void testRenderHomePage_2() throws Exception
	{
	    executeTest(SimplePage.class, "SimplePageExpectedResult.html");

	    Label label = (Label)application.getLastRenderedPage().get("myLabel");
	    assertNotNull(label);
		application.rerender(label);
		String document = application.getServletResponse().getDocument();
		assertNotNull(document);
		assertEquals("<span wicket:id=\"myLabel\">Test Label</span>", document);
		
	    Panel panel = (Panel)application.getLastRenderedPage().get("myPanel");
	    assertNotNull(panel);
		application.rerender(panel);
		document = application.getServletResponse().getDocument();
		assertNotNull(document);
		assertFalse("".equals(document));
		assertEquals("<wicket:panel>Inside the panel</wicket:panel>", document);
		
	    Border border = (Border)application.getLastRenderedPage().get("myBorder");
	    assertNotNull(border);
		application.rerender(border);
		document = application.getServletResponse().getDocument();
		assertNotNull(document);
		assertFalse("".equals(document));
		assertEquals("<wicket:border>before body - <wicket:body>border</wicket:body> - after body</wicket:border>", document);
		
	    border = (Border)application.getLastRenderedPage().get("myBorder2");
	    assertNotNull(border);
		application.rerender(border);
		document = application.getServletResponse().getDocument();
		assertNotNull(document);
		assertFalse("".equals(document));
		assertEquals("<span wicket:id=\"myBorder2\" testAttr=\"myValue\"><wicket:border>before body - <wicket:body>border</wicket:body> - after body</wicket:border></span>", document);
		
	    WebMarkupContainer container = (WebMarkupContainer)application.getLastRenderedPage().get("test");
	    assertNotNull(container);
		application.rerender(container);
		document = application.getServletResponse().getDocument();
		assertNotNull(document);
		assertFalse("".equals(document));
		assertEquals("body", document);
	}

	/**
	 * @throws Exception
	 */
	public void testRenderHomePage_3() throws Exception
	{
	    executeTest(SimplePage_3.class, "SimplePageExpectedResult_3.html");
	}

	/**
	 * @throws Exception
	 */
	public void testRenderHomePage_4() throws Exception
	{
		boolean hit = false;
		try
		{
			executeTest(SimplePage_4.class, "SimplePageExpectedResult_4.html");
		}
		catch (WicketRuntimeException ex)
		{
			if ((ex.getCause() != null) && (ex.getCause() instanceof MarkupException))
			{
				hit = true;
				
				MarkupException mex = (MarkupException)ex.getCause();
				assertNotNull(mex.getMarkupStream());
				assertTrue(mex.getMessage().indexOf("<span>") != -1);
				assertTrue(mex.getMessage().indexOf("SimplePage_4.html") != -1);
			}
		}
		assertTrue("Did expect a MarkupException", hit);
	}

	/**
	 * @throws Exception
	 */
	public void testRenderHomePage_5() throws Exception
	{
		boolean hit = false;
		try
		{
			executeTest(SimplePage_5.class, "SimplePageExpectedResult_5.html");
		}
		catch (WicketRuntimeException ex)
		{
			if ((ex.getCause() != null) && (ex.getCause() instanceof MarkupNotFoundException))
			{
				hit = true;
			}
		}
		assertTrue("Did expect a MarkupNotFoundException", hit);
	}

	/**
	 * @throws Exception
	 */
	public void testRenderHomePage_6() throws Exception
	{
		boolean hit = false;
		try
		{
			executeTest(SimplePage_6.class, "SimplePageExpectedResult_6.html");
		}
		catch (WicketRuntimeException ex)
		{
			if ((ex.getCause() != null) && (ex.getCause() instanceof MarkupException))
			{
				hit = true;
			}
		}
		assertTrue("Did expect a MarkupException", hit);
	}
}
