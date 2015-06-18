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
package org.apache.wicket.markup.html.panel;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.markup.MarkupException;
import org.apache.wicket.markup.MarkupNotFoundException;
import org.apache.wicket.markup.html.markupId.MyPanel;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;


/**
 * Simple application that demonstrates the mock http application code (and checks that it is
 * working)
 * 
 * @author Chris Turner
 */
public class PanelTest extends WicketTestCase
{
	/**
	 * @throws Exception
	 */
	@Test
	public void renderHomePage_1() throws Exception
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
			assertTrue(mex.getMessage().contains("Tag does not have a close tag"));
			assertTrue(mex.toString().contains("SimplePanel_1.html"));
		}
		assertTrue("Did expect a MarkupException", hit);
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void renderHomePage_2() throws Exception
	{
		boolean hit = false;
		try
		{
			executeTest(PanelPage_2.class, "Dummy.html");
		}
		catch (MarkupNotFoundException mex)
		{
			hit = true;

			assertTrue(mex.getMessage().contains("Expected to find <wicket:panel>"));
			assertTrue(mex.getMessage().contains("SimplePanel_2.html"));
		}
		assertTrue("Did expect a MarkupException", hit);
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void panel3() throws Exception
	{
		executeTest(PanelPage_3.class, "PanelPageExpectedResult_3.html");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void panel4() throws Exception
	{
		executeTest(PanelPage_4.class, "PanelPageExpectedResult_4.html");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void inlinePanel() throws Exception
	{
		executeTest(InlinePanelPage_1.class, "InlinePanelPageExpectedResult_1.html");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void inlinePanel_2() throws Exception
	{
		executeTest(InlinePanelPage_2.class, "InlinePanelPageExpectedResult_2.html");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void inlinePanel_3() throws Exception
	{
		executeTest(InlinePanelPage_3.class, "InlinePanelPageExpectedResult_3.html");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void inlinePanel_4() throws Exception
	{
		executeTest(InlinePanelPage_4.class, "InlinePanelPageExpectedResult_4.html");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void inlinePanel_5() throws Exception
	{
		executeTest(InlinePanelPage_5.class, "InlinePanelPageExpectedResult_5.html");
	}

	/**
	 * @throws Exception
	 */
	// TODO FIX the implementation. Fragment markup provider can not be a
	// sibling of the panel.
	@Test
	public void inlinePanel_6() throws Exception
	{
		executeTest(InlinePanelPage_6.class, "InlinePanelPageExpectedResult_6.html");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void panelWithAttributeModifier() throws Exception
	{
		executeTest(PanelWithAttributeModifierPage.class,
			"PanelWithAttributeModifierPageExpectedResult_1.html");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void inlinePanel_7() throws Exception
	{
		executeTest(InlinePanelPage_7.class, "InlinePanelPageExpectedResult_7.html");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void inlinePanel_8() throws Exception
	{
		executeTest(InlinePanelPage_8.class, "InlinePanelPageExpectedResult_8.html");
		Page page = tester.getLastRenderedPage();
		MarkupContainer node = (MarkupContainer)page.get("first:nextContainer");
		assertNotNull(node);
		tester.clickLink("add");
		tester.assertComponentOnAjaxResponse(node);
	}

	/**
	 * 
	 */
	@Test
	public void startPanel()
	{
		tester.startComponentInPage(MyPanel.class);
		tester.assertLabel("label", "Hello, World!");
	}
}
