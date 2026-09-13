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
package org.apache.wicket.ajax;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.apache.wicket.MockPageWithLinkAndComponent;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 * Tests for {@link AjaxNewWindowNotifyingBehavior}
 */
public class AjaxNewWindowNotifyingBehaviorTest extends WicketTestCase
{
	private static final String PARAM_WINDOW_NAME = "windowName";

	/**
	 * The name the browser reports is written back into the precondition of the behavior's Ajax
	 * call, inside a JavaScript string literal, so its quotes and backslashes have to be escaped.
	 */
	@Test
	public void windowNameIsEscapedInPrecondition()
	{
		AjaxNewWindowNotifyingBehavior behavior = new AjaxNewWindowNotifyingBehavior();
		MockPageWithLinkAndComponent page = newPage(behavior);

		tester.startPage(page);
		assertNull(behavior.getWindowName());
		assertFalse("no precondition before the behavior is bound to a window",
			tester.getLastResponseAsString().contains("window.name !=="));

		String name = "d:\\it's \"home\"";
		tester.getRequest().setParameter(PARAM_WINDOW_NAME, name);
		tester.executeBehavior(behavior);
		assertEquals(name, behavior.getWindowName());

		tester.startPage(page);
		String response = tester.getLastResponseAsString();
		assertTrue(response,
			response.contains("return (window.name !== 'd:\\\\it\\'s \\\"home\\\"');"));
		assertFalse(response, response.contains("!== 'd:\\it's"));
	}

	/**
	 * A nameless window gets a generated name, which is also placed inside a string literal.
	 */
	@Test
	public void generatedWindowNameIsEscaped()
	{
		AjaxNewWindowNotifyingBehavior behavior = new AjaxNewWindowNotifyingBehavior()
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected String newWindowName()
			{
				return "it's";
			}
		};
		MockPageWithLinkAndComponent page = newPage(behavior);

		tester.startPage(page);
		tester.getRequest().setParameter(PARAM_WINDOW_NAME, "");
		tester.executeBehavior(behavior);

		assertEquals("it's", behavior.getWindowName());
		assertTrue(tester.getLastResponseAsString(),
			tester.getLastResponseAsString().contains("window.name = 'it\\'s';"));
	}

	/**
	 * The behavior still notices a different window name once it is bound.
	 */
	@Test
	public void differentWindowNameIsNewWindow()
	{
		boolean[] newWindow = new boolean[1];
		AjaxNewWindowNotifyingBehavior behavior = new AjaxNewWindowNotifyingBehavior()
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onNewWindow(AjaxRequestTarget target)
			{
				newWindow[0] = true;
			}
		};
		MockPageWithLinkAndComponent page = newPage(behavior);

		tester.startPage(page);
		tester.getRequest().setParameter(PARAM_WINDOW_NAME, "first");
		tester.executeBehavior(behavior);
		assertFalse(newWindow[0]);

		tester.getRequest().setParameter(PARAM_WINDOW_NAME, "first");
		tester.executeBehavior(behavior);
		assertFalse(newWindow[0]);

		tester.getRequest().setParameter(PARAM_WINDOW_NAME, "second");
		tester.executeBehavior(behavior);
		assertTrue(newWindow[0]);
		assertEquals("first", behavior.getWindowName());
	}

	private static MockPageWithLinkAndComponent newPage(AjaxNewWindowNotifyingBehavior behavior)
	{
		MockPageWithLinkAndComponent page = new MockPageWithLinkAndComponent();
		page.add(new WebComponent(MockPageWithLinkAndComponent.COMPONENT_ID).add(behavior));
		page.add(new Link<Void>(MockPageWithLinkAndComponent.LINK_ID)
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick()
			{
			}
		});
		return page;
	}
}
