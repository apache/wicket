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
package org.apache.wicket.markup.html.basic;

import org.apache.wicket.WicketTestCase;
import org.apache.wicket.markup.MarkupException;
import org.apache.wicket.markup.MarkupNotFoundException;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.border.Border;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.util.value.ValueMap;
import org.junit.Test;


/**
 * Simple application that demonstrates the mock http application code (and checks that it is
 * working)
 * 
 * @author Chris Turner
 */
public class SimplePageTest extends WicketTestCase
{
	/**
	 * @throws Exception
	 */
	@Test
	public void renderHomePage() throws Exception
	{
		executeTest(SimplePage.class, "SimplePageExpectedResult.html");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void renderHomePage_2() throws Exception
	{
		executeTest(SimplePage.class, "SimplePageExpectedResult.html");
		String document = tester.getLastResponseAsString();

		Label label = (Label)tester.getLastRenderedPage().get("myLabel");
		assertNotNull(label);
		assertTrue(document.contains("<span wicket:id=\"myLabel\">Test Label</span>"));

		Panel panel = (Panel)tester.getLastRenderedPage().get("myPanel");
		assertNotNull(panel);
		assertTrue(document.contains("<wicket:panel>Inside the panel<span wicket:id=\"label\">mein Label</span></wicket:panel>"));

		label = (Label)tester.getLastRenderedPage().get("myPanel:label");
		assertNotNull(label);
		assertFalse("".equals(document));
		assertTrue(document.contains("<span wicket:id=\"label\">mein Label</span>"));

		Border border = (Border)tester.getLastRenderedPage().get("myBorder");
		assertNotNull(border);
		assertFalse("".equals(document));
		assertTrue(document.contains("<wicket:border>before body - <wicket:body>border</wicket:body> - after body</wicket:border>"));

		border = (Border)tester.getLastRenderedPage().get("myBorder2");
		assertNotNull(border);
		assertFalse("".equals(document));
		assertTrue(document.contains("<span wicket:id=\"myBorder2\" testAttr=\"myValue\"><wicket:border>before body - <wicket:body>border</wicket:body> - after body</wicket:border></span>"));

		// do the same test twice. Igor reported a problem with that, so we have to test it.
		border = (Border)tester.getLastRenderedPage().get("myBorder2");
		assertNotNull(border);
		assertFalse("".equals(document));
		assertTrue(document.contains("<span wicket:id=\"myBorder2\" testAttr=\"myValue\"><wicket:border>before body - <wicket:body>border</wicket:body> - after body</wicket:border></span>"));

		WebMarkupContainer container = (WebMarkupContainer)tester.getLastRenderedPage().get("test");
		assertNotNull(container);
		assertFalse("".equals(document));
		assertTrue(document.contains("body<span wicket:id=\"myLabel2\">Test Label2</span>"));

		label = (Label)tester.getLastRenderedPage().get("test:myLabel2");
		assertNotNull(label);
		assertFalse("".equals(document));
		assertTrue(document.contains("<span wicket:id=\"myLabel2\">Test Label2</span>"));
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void renderHomePage_2a() throws Exception
	{
		// Render the component without having rendered the page previously
		SimplePage page = new SimplePage();
		tester.startPage(page);
		String document = tester.getLastResponseAsString();
		Label label = (Label)page.get("myLabel");
		assertNotNull(label);
		assertTrue(document.contains("<span wicket:id=\"myLabel\">Test Label</span>"));

		Panel panel = (Panel)page.get("myPanel");
		assertNotNull(panel);
		assertTrue(document.contains("<wicket:panel>Inside the panel<span wicket:id=\"label\">mein Label</span></wicket:panel>"));

		label = (Label)page.get("myPanel:label");
		assertNotNull(label);
		assertTrue(document.contains("<span wicket:id=\"label\">mein Label</span>"));

		Border border = (Border)page.get("myBorder");
		assertNotNull(border);
		assertTrue(document.contains("<wicket:border>before body - <wicket:body>border</wicket:body> - after body</wicket:border>"));

		border = (Border)page.get("myBorder2");
		assertNotNull(border);
		assertTrue(document.contains("<span wicket:id=\"myBorder2\" testAttr=\"myValue\"><wicket:border>before body - <wicket:body>border</wicket:body> - after body</wicket:border></span>"));

		// do the same test twice. Igor reported a problem with that, so we have to test it.
		border = (Border)page.get("myBorder2");
		assertNotNull(border);
		assertTrue(document.contains("<span wicket:id=\"myBorder2\" testAttr=\"myValue\"><wicket:border>before body - <wicket:body>border</wicket:body> - after body</wicket:border></span>"));

		WebMarkupContainer container = (WebMarkupContainer)page.get("test");
		assertNotNull(container);
		assertTrue(document.contains("body<span wicket:id=\"myLabel2\">Test Label2</span>"));

		label = (Label)page.get("test:myLabel2");
		assertNotNull(label);
		assertTrue(document.contains("<span wicket:id=\"myLabel2\">Test Label2</span>"));
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void renderHomePage_2b() throws Exception
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
	@Test
	public void renderHomePage_3() throws Exception
	{
		executeTest(SimplePage_3.class, "SimplePageExpectedResult_3.html");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void renderHomePage_4() throws Exception
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
			assertTrue(mex.getMessage().contains("<span>"));
			assertTrue(mex.toString().contains("SimplePage_4.html"));
		}
		assertTrue("Did expect a MarkupException", hit);
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void renderHomePage_5() throws Exception
	{
		boolean hit = false;
		try
		{
			executeTest(SimplePage_5.class, "SimplePageExpectedResult_5.html");
		}
		catch (MarkupNotFoundException ex)
		{
			hit = true;
		}
		assertTrue("Did expect a MarkupNotFoundException", hit);
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void renderHomePage_6() throws Exception
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
	@Test
	public void renderHomePage_7() throws Exception
	{
		tester.getApplication().getResourceSettings().setThrowExceptionOnMissingResource(false);
		// This is for issue https://issues.apache.org/jira/browse/WICKET-590
		executeTest(SimplePage_7.class, "SimplePageExpectedResult_7.html");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void renderHomePage_8() throws Exception
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
	@Test
	public void renderHomePage_9() throws Exception
	{
		executeTest(SimplePage_9.class, "SimplePageExpectedResult_9.html");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void renderHomePage_10() throws Exception
	{
		executeTest(new SimplePage_10(false), "SimplePageExpectedResult_10.html");
		tester.startPage(new SimplePage_10(true));
		String document = tester.getLastResponseAsString();
		assertTrue(document.contains("<wicket:panel>Inside the panel<span wicket:id=\"label\">mein Label</span></wicket:panel>"));
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void renderHomePage_11() throws Exception
	{
		executeTest(SimplePage_11.class, "SimplePageExpectedResult_11.html");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void renderHomePage_12() throws Exception
	{
		executeTest(SimplePage_12.class, "SimplePageExpectedResult_12.html");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void renderHomePage_13() throws Exception
	{
		executeTest(SimplePage_13.class, "SimplePageExpectedResult_13.html");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void renderHomePage_14() throws Exception
	{
		executeTest(SimplePage_14.class, "SimplePageExpectedResult_14.html");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void renderHomePage_15() throws Exception
	{
		executeTest(SimplePage_15.class, "SimplePageExpectedResult_15.html");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void renderHomePage_16() throws Exception
	{
		executeTest(SimplePage_16.class, "SimplePageExpectedResult_16.html");
	}
}
