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

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.markup.IMarkupFragment;
import org.apache.wicket.markup.html.border.Border;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.InlinePanelPage_1;
import org.apache.wicket.markup.html.panel.Panel;
import org.junit.Test;

/**
 * 
 */
public class MarkupFragmentTest extends WicketTestCase
{
	/**
	 * page.getAssociatedMarkup(), page.getMarkup() and page.getMarkup(null) must all return the
	 * same
	 * 
	 * @throws Exception
	 */
	@Test
	public void page() throws Exception
	{
		IMarkupFragment markup = new MyPage().getAssociatedMarkup();
		compareMarkupWithFile(markup, "MyPage_ExpectedResult.html");

		markup = new MyPage().getMarkup();
		compareMarkupWithFile(markup, "MyPage_ExpectedResult.html");

		markup = new MyPage().getMarkup(null);
		compareMarkupWithFile(markup, "MyPage_ExpectedResult.html");
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void panel() throws Exception
	{
		Page page = new MyPage();
		Panel panel = new MyPanel("panel");
		page.add(panel);

		// Get the associated markup file
		IMarkupFragment markup = panel.getAssociatedMarkup();
		compareMarkupWithFile(markup, "MyPanel_ExpectedResult.html");

		// The Page is missing the tag to "call" the panel
		assertNull(panel.getMarkup());

		// Create a Page with proper markup for the panel
		page = new MyPanelPage();
		panel = (Panel)page.get("panel");

		// getMarkup() returns the "calling" tags
		markup = panel.getMarkup();
		compareMarkupWithString(markup, "<span wicket:id=\"panel\">test</span>");

		// getMarkup(null) returns the markup which is used to find a child component, which in case
		// of Panel is the <wicket:panel> tag and is thus may not be equal to the associated markup
		// file.
		markup = panel.getMarkup(null);
		compareMarkupWithString(markup,
			"<wicket:panel>  <span wicket:id=\"label\">text</span></wicket:panel>");
	}

	/**
	 * @see href http://issues.apache.org/jira/browse/WICKET-3111
	 * 
	 * @throws Exception
	 */
	@Test
	public void panelWithAutoComponent() throws Exception
	{
		Page page = new MyPage();
		Panel panel = new MyPanelWithAutoComponent("panel");
		page.add(panel);

		// Get the associated markup file
		IMarkupFragment markup = panel.getAssociatedMarkup();
		compareMarkupWithFile(markup, "MyPanelWithAutoComponent_ExpectedResult.html");

		// The Page is missing the tag to "call" the panel
		assertNull(panel.getMarkup());

		// Create a Page with proper markup for the panel
		page = new MyPanelWithAutoComponentPage();
		panel = (Panel)page.get("panel");

		// getMarkup() returns the "calling" tags
		markup = panel.getMarkup();
		compareMarkupWithString(markup, "<span wicket:id=\"panel\">test</span>");

		// getMarkup(null) returns the markup which is used to find a child component
		markup = panel.getMarkup(null);
		compareMarkupWithString(markup,
			"<wicket:panel><a href=\"something\"><span wicket:id=\"label\">text</span></a></wicket:panel>");
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void label() throws Exception
	{
		Component label = new MyPage().get("label");
		IMarkupFragment markup = label.getMarkup();
		compareMarkupWithString(markup, "<span wicket:id=\"label\">text</span>");

		label = new MyPanelPage().get("panel:label");
		markup = label.getMarkup();
		compareMarkupWithString(markup, "<span wicket:id=\"label\">text</span>");
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void webMarkupContainer() throws Exception
	{
		MarkupContainer container = (MarkupContainer)new MyPage().get("container");
		IMarkupFragment markup = container.getMarkup();
		compareMarkupWithString(markup, "<span wicket:id=\"container\">text</span>");

		// The container doesn't have an external markup file
		markup = container.getAssociatedMarkup();
		assertNull(markup);

		// Get the markup which is used to search for children.
		markup = container.getMarkup(null);
		compareMarkupWithString(markup, "<span wicket:id=\"container\">text</span>");
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void border() throws Exception
	{
		Page page = new MyBorderPage();
		Border border = (Border)page.get("border");

		// Get the associated markup file
		IMarkupFragment markup = border.getAssociatedMarkup();
		compareMarkupWithFile(markup, "MyBorder_ExpectedResult.html");

		// getMarkup() returns the "calling" tags
		markup = border.getMarkup();
		compareMarkupWithString(markup, "<span wicket:id=\"border\">test</span>");

		// getMarkup(null) returns the markup which is used to find a child component
		markup = border.getMarkup(null);
		compareMarkupWithString(markup, "<wicket:border>  111  <wicket:body/>  222</wicket:border>");

		assertNull(border.getBodyContainer().getAssociatedMarkup());

		border.dequeue();
		markup = border.getBodyContainer().getMarkup();
		compareMarkupWithString(markup, "<wicket:body/>");

		markup = border.getBodyContainer().getMarkup(null);
		compareMarkupWithString(markup, "<span wicket:id=\"border\">test</span>");

		markup = border.getBodyContainer().getParent().getMarkup(border.getBodyContainer());
		compareMarkupWithString(markup, "<wicket:body/>");

		// getMarkup(null) returns the markup which is used to find a child component
		markup = border.getBodyContainer().getMarkup(null);
		compareMarkupWithString(markup, "<span wicket:id=\"border\">test</span>");
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void border2() throws Exception
	{
		Page page = new MyBorderPage();
		Border border = (Border)page.get("border2");

		// Get the associated markup file
		IMarkupFragment markup = border.getAssociatedMarkup();
		compareMarkupWithFile(markup, "MyBorder2_ExpectedResult.html");

		// getMarkup() returns the "calling" tags
		markup = border.getMarkup();
		compareMarkupWithString(markup, "<span wicket:id=\"border2\">test</span>");

		// getMarkup(null) returns the markup which is used to find a child component
		markup = border.getMarkup(null);
		compareMarkupWithString(markup,
			"<wicket:border>  111  <wicket:body>333</wicket:body>  222</wicket:border>");

		assertNull(border.getBodyContainer().getAssociatedMarkup());

		// See explanation in BaseBorder.BorderBodyContainer.getMarkup()
		border.dequeue();
		markup = border.getBodyContainer().getParent().getMarkup(border.getBodyContainer());
		compareMarkupWithString(markup, "<wicket:body>333</wicket:body>");

		markup = border.getBodyContainer().getMarkup();
		compareMarkupWithString(markup, "<wicket:body>333</wicket:body>");

		// getMarkup(null) returns the markup which is used to find a child component
		markup = border.getBodyContainer().getMarkup(null);
		compareMarkupWithString(markup, "<span wicket:id=\"border2\">test</span>");
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void fragments() throws Exception
	{
		Page page = new InlinePanelPage_1();
		Fragment fragment = (Fragment)page.get("myPanel1");

		// Get the associated markup file
		IMarkupFragment markup = fragment.getAssociatedMarkup();
		assertNull(markup);

		// getMarkup() returns the "calling" tags
		markup = fragment.getMarkup();
		compareMarkupWithString(markup, "<span wicket:id=\"myPanel1\">panel</span>");

		// getMarkup(null) returns the markup which is used to find a child component
		markup = fragment.getMarkup(null);
		compareMarkupWithString(markup,
			"<wicket:fragment wicket:id=\"frag1\">panel 1</wicket:fragment>");
	}
}
