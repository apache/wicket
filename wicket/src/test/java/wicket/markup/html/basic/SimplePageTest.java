/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import wicket.util.value.ValueMap;

/**
 * Simple application that demonstrates the mock http application code (and
 * checks that it is working)
 * 
 * @author Chris Turner
 */
public class SimplePageTest extends WicketTestCase
{
	private static final Log log = LogFactory.getLog(SimplePageTest.class);

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
		application.processRequestCycle(label);
		String document = application.getServletResponse().getDocument();
		assertNotNull(document);
		assertEquals("<span wicket:id=\"myLabel\">Test Label</span>", document);
		
	    Panel panel = (Panel)application.getLastRenderedPage().get("myPanel");
	    assertNotNull(panel);
		application.processRequestCycle(panel);
		document = application.getServletResponse().getDocument();
		assertNotNull(document);
		assertEquals("<wicket:panel>Inside the panel<span wicket:id=\"label\">mein Label</span></wicket:panel>", document);
		
	    label = (Label)application.getLastRenderedPage().get("myPanel:label");
	    assertNotNull(label);
		application.processRequestCycle(label);
		document = application.getServletResponse().getDocument();
		assertNotNull(document);
		assertFalse("".equals(document));
		assertEquals("<span wicket:id=\"label\">mein Label</span>", document);
		
	    Border border = (Border)application.getLastRenderedPage().get("myBorder");
	    assertNotNull(border);
		application.processRequestCycle(border);
		document = application.getServletResponse().getDocument();
		assertNotNull(document);
		assertFalse("".equals(document));
		assertEquals("<wicket:border>before body - <wicket:body>border</wicket:body> - after body</wicket:border>", document);
		
	    border = (Border)application.getLastRenderedPage().get("myBorder2");
	    assertNotNull(border);
		application.processRequestCycle(border);
		document = application.getServletResponse().getDocument();
		assertNotNull(document);
		assertFalse("".equals(document));
		assertEquals("<span wicket:id=\"myBorder2\" testAttr=\"myValue\"><wicket:border>before body - <wicket:body>border</wicket:body> - after body</wicket:border></span>", document);

		// do the same test twice. Igor reported a problem with that, so we have to test it.
	    border = (Border)application.getLastRenderedPage().get("myBorder2");
	    assertNotNull(border);
		application.processRequestCycle(border);
		document = application.getServletResponse().getDocument();
		assertNotNull(document);
		assertFalse("".equals(document));
		assertEquals("<span wicket:id=\"myBorder2\" testAttr=\"myValue\"><wicket:border>before body - <wicket:body>border</wicket:body> - after body</wicket:border></span>", document);
		
	    WebMarkupContainer container = (WebMarkupContainer)application.getLastRenderedPage().get("test");
	    assertNotNull(container);
		application.processRequestCycle(container);
		document = application.getServletResponse().getDocument();
		assertNotNull(document);
		assertFalse("".equals(document));
		assertEquals("body<span wicket:id=\"myLabel2\">Test Label2</span>", document);
		
	    label = (Label)application.getLastRenderedPage().get("test:myLabel2");
	    assertNotNull(label);
		application.processRequestCycle(label);
		document = application.getServletResponse().getDocument();
		assertNotNull(document);
		assertFalse("".equals(document));
		assertEquals("<span wicket:id=\"myLabel2\">Test Label2</span>", document);
	}

	/**
	 * @throws Exception
	 */
	public void testRenderHomePage_2a() throws Exception
	{
		// Render the component without having rendered the page previously
		SimplePage page = new SimplePage();

	    Label label = (Label)page.get("myLabel");
	    assertNotNull(label);
		application.processRequestCycle(label);
		String document = application.getServletResponse().getDocument();
		assertNotNull(document);
		assertEquals("<span wicket:id=\"myLabel\">Test Label</span>", document);
		
	    Panel panel = (Panel)page.get("myPanel");
	    assertNotNull(panel);
		application.processRequestCycle(panel);
		document = application.getServletResponse().getDocument();
		assertNotNull(document);
		assertEquals("<wicket:panel>Inside the panel<span wicket:id=\"label\">mein Label</span></wicket:panel>", document);
		
	    label = (Label)page.get("myPanel:label");
	    assertNotNull(label);
		application.processRequestCycle(label);
		document = application.getServletResponse().getDocument();
		assertNotNull(document);
		assertFalse("".equals(document));
		assertEquals("<span wicket:id=\"label\">mein Label</span>", document);
		
	    Border border = (Border)page.get("myBorder");
	    assertNotNull(border);
		application.processRequestCycle(border);
		document = application.getServletResponse().getDocument();
		assertNotNull(document);
		assertFalse("".equals(document));
		assertEquals("<wicket:border>before body - <wicket:body>border</wicket:body> - after body</wicket:border>", document);
		
	    border = (Border)page.get("myBorder2");
	    assertNotNull(border);
		application.processRequestCycle(border);
		document = application.getServletResponse().getDocument();
		assertNotNull(document);
		assertFalse("".equals(document));
		assertEquals("<span wicket:id=\"myBorder2\" testAttr=\"myValue\"><wicket:border>before body - <wicket:body>border</wicket:body> - after body</wicket:border></span>", document);

		// do the same test twice. Igor reported a problem with that, so we have to test it.
	    border = (Border)page.get("myBorder2");
	    assertNotNull(border);
		application.processRequestCycle(border);
		document = application.getServletResponse().getDocument();
		assertNotNull(document);
		assertFalse("".equals(document));
		assertEquals("<span wicket:id=\"myBorder2\" testAttr=\"myValue\"><wicket:border>before body - <wicket:body>border</wicket:body> - after body</wicket:border></span>", document);
		
	    WebMarkupContainer container = (WebMarkupContainer)page.get("test");
	    assertNotNull(container);
		application.processRequestCycle(container);
		document = application.getServletResponse().getDocument();
		assertNotNull(document);
		assertFalse("".equals(document));
		assertEquals("body<span wicket:id=\"myLabel2\">Test Label2</span>", document);
		
	    label = (Label)page.get("test:myLabel2");
	    assertNotNull(label);
		application.processRequestCycle(label);
		document = application.getServletResponse().getDocument();
		assertNotNull(document);
		assertFalse("".equals(document));
		assertEquals("<span wicket:id=\"myLabel2\">Test Label2</span>", document);
	}

	/**
	 * @throws Exception
	 */
	public void testRenderHomePage_2b() throws Exception
	{
		// Render the component without having rendered the page previously
		SimplePage page = new SimplePage();

	    Label label = (Label)page.get("myLabel");
	    assertNotNull(label);
		ValueMap attr = label.getMarkupAttributes();
		assertNotNull(attr);
		assertEquals("myLabel", attr.getString("wicket:id"));
		
	    Panel panel = (Panel)page.get("myPanel");
	    assertNotNull(panel);
		attr = panel.getMarkupAttributes();
		assertNotNull(attr);
		assertEquals("myPanel", attr.getString("wicket:id"));
		
	    label = (Label)page.get("myPanel:label");
	    assertNotNull(label);
		attr = label.getMarkupAttributes();
		assertNotNull(attr);
		assertEquals("label", attr.getString("wicket:id"));
		
	    Border border = (Border)page.get("myBorder");
	    assertNotNull(border);
		attr = border.getMarkupAttributes();
		assertNotNull(attr);
		assertEquals("myBorder", attr.getString("wicket:id"));
		
	    border = (Border)page.get("myBorder2");
	    assertNotNull(border);
		attr = border.getMarkupAttributes();
		assertNotNull(attr);
		assertEquals("myBorder2", attr.getString("wicket:id"));

		// do the same test twice. Igor reported a problem with that, so we have to test it.
	    border = (Border)page.get("myBorder2");
	    assertNotNull(border);
		attr = border.getMarkupAttributes();
		assertNotNull(attr);
		assertEquals("myBorder2", attr.getString("wicket:id"));
		
	    WebMarkupContainer container = (WebMarkupContainer)page.get("test");
	    assertNotNull(container);
		attr = container.getMarkupAttributes();
		assertNotNull(attr);
		assertEquals("test", attr.getString("wicket:id"));
		
	    label = (Label)page.get("test:myLabel2");
	    assertNotNull(label);
		attr = label.getMarkupAttributes();
		assertNotNull(attr);
		assertEquals("myLabel2", attr.getString("wicket:id"));
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
		catch (MarkupException mex)
		{
			hit = true;
			
			assertNotNull(mex.getMarkupStream());
			assertTrue(mex.getMessage().indexOf("<span>") != -1);
			assertTrue(mex.getMessage().indexOf("SimplePage_4.html") != -1);
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
		catch (MarkupException ex)
		{
			hit = true;
		}
		assertTrue("Did expect a MarkupException", hit);
	}

	/**
	 * @throws Exception
	 */
	public void testRenderHomePage_7() throws Exception
	{
		executeTest(SimplePage_7.class, "SimplePageExpectedResult_7.html");
	}

	/**
	 * @throws Exception
	 */
	public void testRenderHomePage_8() throws Exception
	{
		boolean hit = false;
		try
		{
			executeTest(SimplePage_8.class, "SimplePageExpectedResult_8.html");
		}
		catch (MarkupException ex)
		{
			hit = true;
		}
		assertTrue("Did expect a MarkupException", hit);
	}

	/**
	 * @throws Exception
	 */
	public void testRenderHomePage_9() throws Exception
	{
		executeTest(SimplePage_9.class, "SimplePageExpectedResult_9.html");
	}

	/**
	 * @throws Exception
	 */
	public void testRenderHomePage_10() throws Exception
	{
	    executeTest(SimplePage_10.class, "SimplePageExpectedResult_10.html");

	    Panel panel = (Panel)application.getLastRenderedPage().get("myPanel");
	    assertNotNull(panel);
		// we need to setup request/response before calling setvisible
		application.setupRequestAndResponse();
		application.createRequestCycle();

	    panel.setVisible(true);
		application.processRequestCycle(panel);
		String document = application.getServletResponse().getDocument();
		assertNotNull(document);
		assertEquals("<wicket:panel>Inside the panel<span wicket:id=\"label\">mein Label</span></wicket:panel>", document);
	}

	/**
	 * @throws Exception
	 */
	public void testRenderHomePage_11() throws Exception
	{
		executeTest(SimplePage_11.class, "SimplePageExpectedResult_11.html");
	}

	/**
	 * @throws Exception
	 */
	public void testRenderHomePage_12() throws Exception
	{
		executeTest(SimplePage_12.class, "SimplePageExpectedResult_12.html");
	}
}
