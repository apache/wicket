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
package org.apache.wicket.extensions.markup.html.collapsible;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

class CollapsiblePanelTest extends WicketTestCase
{
	private static final String DETAILS = "panel:details";

	@Test
	void rendersACollapsedDetailsElementWithTitleAndBody()
	{
		TestPanel panel = new TestPanel();
		tester.startPage(new TestPage(panel));

		assertFalse(panel.isExpanded());
		TagTester details = tester.getTagByWicketId("details");
		assertEquals("details", details.getName());
		assertNull(details.getAttribute("open"));
		assertEquals("summary", tester.getTagByWicketId("title").getName());
		assertEquals("&lt;b&gt;About&lt;/b&gt;", tester.getTagByWicketId("title").getValue());
		assertEquals("The body", tester.getTagByWicketId("body").getValue());
		tester.assertContains("wicket-collapsible.*\\.css");
		assertFalse(tester.getLastResponseAsString().contains("style="), "no inline style");
	}

	@Test
	void rendersExpanded()
	{
		tester.startPage(new TestPage(new TestPanel().setExpanded(true)));

		assertEquals("open", tester.getTagByWicketId("details").getAttribute("open"));
	}

	@Test
	void doesNotReportTogglesByDefault()
	{
		tester.startPage(new TestPage(new TestPanel()));

		assertFalse(tester.getLastResponseAsString().contains("\"e\":\"toggle\""));
	}

	@Test
	void remembersTheStateTheBrowserReports()
	{
		TestPanel panel = new TestPanel();
		panel.setRememberExpanded(true);
		tester.startPage(new TestPage(panel));
		tester.assertContains("\"e\":\"toggle\"");

		tester.getRequest().setParameter("open", "true");
		tester.executeAjaxEvent(DETAILS, "toggle");

		assertTrue(panel.isExpanded());
		assertEquals(1, panel.toggles);

		tester.getRequest().setParameter("open", "false");
		tester.executeAjaxEvent(DETAILS, "toggle");

		assertFalse(panel.isExpanded());
		assertEquals(2, panel.toggles);
	}

	private static class TestPanel extends CollapsiblePanel
	{
		private static final long serialVersionUID = 1L;

		int toggles;

		TestPanel()
		{
			super("panel", Model.of("<b>About</b>"));
		}

		@Override
		protected Component newBody(String id)
		{
			return new Label(id, "The body");
		}

		@Override
		protected void onToggle(AjaxRequestTarget target)
		{
			toggles++;
		}
	}

	public static class TestPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		TestPage(CollapsiblePanel panel)
		{
			add(panel);
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream("<html><head></head><body>" +
				"<div wicket:id=\"panel\"></div></body></html>");
		}
	}
}
