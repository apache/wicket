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
package org.apache.wicket.extensions.markup.html.floating;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

class FloatingPanelTest extends WicketTestCase
{
	@Test
	void rendersTheEscapedTitleTheBodyAndAClosingButton()
	{
		TestPanel panel = new TestPanel(true);
		tester.startPage(new TestPage(panel));

		tester.assertLabel("panel:title", "&lt;Title&gt;");
		tester.assertLabel("panel:body", "Body");
		TagTester close = tester.getTagByWicketId("close");
		assertEquals("Close", close.getAttribute("aria-label"));
		assertEquals("Close", close.getAttribute("title"));
		assertTrue(tester.getLastResponseAsString().contains("wicket-floating-panel.css"));
	}

	@Test
	void theButtonCallsOnClose()
	{
		TestPanel panel = new TestPanel(true);
		tester.startPage(new TestPage(panel));

		tester.clickLink("panel:close");

		assertEquals(1, panel.closed);
	}

	@Test
	void aPanelThatCannotBeClosedHasNoButton()
	{
		tester.startPage(new TestPage(new TestPanel(false)));

		tester.assertInvisible("panel:close");
	}

	private static class TestPanel extends FloatingPanel
	{
		private static final long serialVersionUID = 1L;

		private final boolean closable;

		int closed;

		TestPanel(boolean closable)
		{
			super("panel", Model.of("<Title>"));
			this.closable = closable;
		}

		@Override
		protected Component newBody(String id)
		{
			return new Label(id, "Body");
		}

		@Override
		protected boolean isClosable()
		{
			return closable;
		}

		@Override
		protected void onClose(AjaxRequestTarget target)
		{
			closed++;
		}
	}

	public static class TestPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		TestPage(FloatingPanel panel)
		{
			add(panel);
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream(
				"<html><head></head><body><div wicket:id=\"panel\"></div></body></html>");
		}
	}
}
