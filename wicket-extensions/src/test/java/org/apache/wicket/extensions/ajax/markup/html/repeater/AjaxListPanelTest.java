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
package org.apache.wicket.extensions.ajax.markup.html.repeater;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link AjaxListPanel}.
 * 
 * @author svenmeier
 */
class AjaxListPanelTest extends WicketTestCase
{

	@Test
	void test()
	{
		final AjaxListPanel list = new AjaxListPanel("list");

		AjaxEventBehavior behavior = new AjaxEventBehavior("click") {
			Label label;
			
			@Override
			protected void onEvent(AjaxRequestTarget target)
			{
				if (label == null) {
					label = new Label(list.newChildId());
					list.append(label, target);
				} else {
					list.delete(label, target);
					label = null;
				}
			}
		};
		list.add(behavior);
		
		tester.startComponentInPage(list);
		
		tester.executeBehavior(behavior);
		assertTrue(tester.getLastResponseAsString().contains("Wicket.DOM.add(Wicket.DOM.get('container2'), '<div wicket:id=\\\"repeater\\\" id=\\\"id13\\\"/>');"));

		tester.executeBehavior(behavior);
		assertTrue(tester.getLastResponseAsString().contains("Wicket.DOM.remove(Wicket.DOM.get('id13'));"));
	}

	/**
	 * The appended markup is written into a single quoted JavaScript string literal, so a backslash
	 * in it must not be able to escape the backslash that precedes the following quote.
	 */
	@Test
	void appendedMarkupStaysInsideItsJavaScriptStringLiteral()
	{
		final AjaxListPanel list = new BackslashBeforeQuotePanel("list");

		AjaxEventBehavior behavior = new AjaxEventBehavior("click") {
			@Override
			protected void onEvent(AjaxRequestTarget target)
			{
				Label label = new Label(list.newChildId());
				list.append(label, target);
			}
		};
		list.add(behavior);

		tester.startComponentInPage(list);

		tester.executeBehavior(behavior);

		String response = tester.getLastResponseAsString();
		assertTrue(response.contains("a\\\\&#039;;b"), "no markup was appended");
	}

	/**
	 * A list panel whose repeater tag carries a backslash before a quote, which ends up in the
	 * appended markup.
	 */
	private static class BackslashBeforeQuotePanel extends AjaxListPanel
		implements
			IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		BackslashBeforeQuotePanel(String id)
		{
			super(id);
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream("<wicket:panel><div wicket:id=\"container\">" +
				"<div wicket:id=\"repeater\" title=\"a\\';b\"></div></div></wicket:panel>");
		}
	}
}
