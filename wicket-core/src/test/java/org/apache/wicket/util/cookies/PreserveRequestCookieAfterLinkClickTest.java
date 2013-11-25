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
package org.apache.wicket.util.cookies;

import javax.servlet.http.Cookie;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.junit.Test;

/**
 * Test for https://issues.apache.org/jira/browse/WICKET-5425
 */
public class PreserveRequestCookieAfterLinkClickTest extends WicketTestCase
{
	@Test
	public void cookiesTransferAfterClickingLink()
	{
		Cookie testCookie = new Cookie("lostCookie", "lostValue");
		testCookie.setMaxAge(1);
		tester.getRequest().addCookie(testCookie);
		tester.startPage(Page1.class);
		tester.clickLink("testCookieTransfer");
		tester.assertRenderedPage(Page2.class);
		Cookie[] cookies = tester.getRequest().getCookies();
		assertEquals(1, cookies.length);
		Cookie firstCookie = cookies[0];
		assertEquals("lostCookie", firstCookie.getName());
		assertEquals("lostValue", firstCookie.getValue());
	}

	public static class Page1 extends WebPage implements IMarkupResourceStreamProvider
	{
		public Page1()
		{
			add(new Link<Void>("testCookieTransfer")
			{
				@Override
				public void onClick() {
					setResponsePage(Page2.class);
				}
			});
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container, Class<?> containerClass)
		{
			return new StringResourceStream("<html><body><a wicket:id='testCookieTransfer'></a></body></html>");
		}
	}

	public static class Page2 extends WebPage implements IMarkupResourceStreamProvider
	{
		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container, Class<?> containerClass)
		{
			return new StringResourceStream("<html/>");
		}
	}
}
