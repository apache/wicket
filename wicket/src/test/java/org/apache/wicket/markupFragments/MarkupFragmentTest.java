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
package org.apache.wicket.markupFragments;

import java.io.IOException;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.markup.IMarkupFragment;
import org.apache.wicket.markup.html.border.Border;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.InlinePanelPage_1;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.util.diff.DiffUtil;

/**
 * 
 */
public class MarkupFragmentTest extends WicketTestCase
{
	private void compareWithFile(IMarkupFragment markup, String filename) throws IOException
	{
		String doc = markup.toString(true);
		DiffUtil.validatePage(doc, MyPage.class, filename, true);
	}

	private void compare(IMarkupFragment markup, String testMarkup) throws IOException
	{
		testMarkup = testMarkup.replaceAll("\n\r", "");
		testMarkup = testMarkup.replaceAll("\r\n", "");

		String doc = markup.toString(true);
		doc = doc.replaceAll("\n\r", "");
		doc = doc.replaceAll("\r\n", "");
		assertEquals(doc, testMarkup);
	}

	/**
	 * page.getAssociatedMarkup(), page.getMarkup() and page.getMarkup(null) must all return the
	 * same
	 * 
	 * @throws Exception
	 */
	public void testPage() throws Exception
	{
		IMarkupFragment markup = new MyPage().getAssociatedMarkup();
		compareWithFile(markup, "MyPage_ExpectedResult.html");

		markup = new MyPage().getMarkup();
		compareWithFile(markup, "MyPage_ExpectedResult.html");

		markup = new MyPage().getMarkup(null);
		compareWithFile(markup, "MyPage_ExpectedResult.html");
	}

	/**
	 * 
	 * @throws Exception
	 */
	public void testPanel() throws Exception
	{
		Page page = new MyPage();
		Panel panel = new MyPanel("panel");
		page.add(panel);

		// Get the associated markup file
		IMarkupFragment markup = panel.getAssociatedMarkup();
		compareWithFile(markup, "MyPanel_ExpectedResult.html");

		// The Page is missing the tag to "call" the panel
		assertNull(panel.getMarkup());

		// Create a Page with proper markup for the panel
		page = new MyPanelPage();
		panel = (Panel)page.get("panel");

		// getMarkup() returns the "calling" tags
		markup = panel.getMarkup();
		compare(markup, "<span wicket:id=\"panel\">test</span>");

		// getMarkup(null) returns the markup which is used to find a child component
		markup = panel.getMarkup(null);
		compare(markup, "<wicket:panel>  <span wicket:id=\"label\">text</span></wicket:panel>");
	}

	/**
	 * 
	 * @throws Exception
	 */
	public void testLabel() throws Exception
	{
		Component label = new MyPage().get("label");
		IMarkupFragment markup = label.getMarkup();
		compare(markup, "<span wicket:id=\"label\">text</span>");

		label = new MyPanelPage().get("panel:label");
		markup = label.getMarkup();
		compare(markup, "<span wicket:id=\"label\">text</span>");
	}

	/**
	 * 
	 * @throws Exception
	 */
	public void testWebMarkupContainer() throws Exception
	{
		MarkupContainer container = (MarkupContainer)new MyPage().get("container");
		IMarkupFragment markup = container.getMarkup();
		compare(markup, "<span wicket:id=\"container\">text</span>");

		// The container doesn't have an external markup file
		markup = container.getAssociatedMarkup();
		assertNull(markup);

		// Get the markup which is used to search for children.
		markup = container.getMarkup(null);
		compare(markup, "<span wicket:id=\"container\">text</span>");
	}

	/**
	 * 
	 * @throws Exception
	 */
	public void testBorder() throws Exception
	{
		Page page = new MyBorderPage();
		Border border = (Border)page.get("border");

		// Get the associated markup file
		IMarkupFragment markup = border.getAssociatedMarkup();
		compareWithFile(markup, "MyBorder_ExpectedResult.html");

		// getMarkup() returns the "calling" tags
		markup = border.getMarkup();
		compare(markup, "<span wicket:id=\"border\">test</span>");

		// getMarkup(null) returns the markup which is used to find a child component
		markup = border.getMarkup(null);
		compare(markup, "<wicket:border>  111  <wicket:body/>  222</wicket:border>");

		assertNull(border.getBodyContainer().getAssociatedMarkup());

		markup = border.getBodyContainer().getMarkup();
		compare(markup, "<wicket:body/>");

		markup = border.getBodyContainer().getMarkup(null);
		compare(markup, "<span wicket:id=\"border\">test</span>");

		markup = border.getBodyContainer().getParent().getMarkup(border.getBodyContainer());
		compare(markup, "<wicket:body/>");

		// getMarkup(null) returns the markup which is used to find a child component
		markup = border.getBodyContainer().getMarkup(null);
		compare(markup, "<span wicket:id=\"border\">test</span>");
	}

	/**
	 * 
	 * @throws Exception
	 */
	public void testBorder2() throws Exception
	{
		Page page = new MyBorderPage();
		Border border = (Border)page.get("border2");

		// Get the associated markup file
		IMarkupFragment markup = border.getAssociatedMarkup();
		compareWithFile(markup, "MyBorder2_ExpectedResult.html");

		// getMarkup() returns the "calling" tags
		markup = border.getMarkup();
		compare(markup, "<span wicket:id=\"border2\">test</span>");

		// getMarkup(null) returns the markup which is used to find a child component
		markup = border.getMarkup(null);
		compare(markup, "<wicket:border>  111  <wicket:body>333</wicket:body>  222</wicket:border>");

		assertNull(border.getBodyContainer().getAssociatedMarkup());

		// See explanation in BaseBorder.BorderBodyContainer.getMarkup()
		markup = border.getBodyContainer().getParent().getMarkup(border.getBodyContainer());
		compare(markup, "<wicket:body>333</wicket:body>");

		markup = border.getBodyContainer().getMarkup();
		compare(markup, "<wicket:body>333</wicket:body>");

		markup = border.getBodyContainer().getMarkup(null);
		compare(markup, "<span wicket:id=\"border2\">test</span>");

		// getMarkup(null) returns the markup which is used to find a child component
		markup = border.getBodyContainer().getMarkup(null);
		compare(markup, "<span wicket:id=\"border2\">test</span>");
	}

	/**
	 * 
	 * @throws Exception
	 */
	public void testFragments() throws Exception
	{
		Page page = new InlinePanelPage_1();
		Fragment fragment = (Fragment)page.get("myPanel1");

		// Get the associated markup file
		IMarkupFragment markup = fragment.getAssociatedMarkup();
		assertNull(markup);

		// getMarkup() returns the "calling" tags
		markup = fragment.getMarkup();
		compare(markup, "<span wicket:id=\"myPanel1\">panel</span>");

		// getMarkup(null) returns the markup which is used to find a child component
		markup = fragment.getMarkup(null);
		compare(markup, "<wicket:fragment wicket:id=\"frag1\">panel 1</wicket:fragment>");
	}
}
