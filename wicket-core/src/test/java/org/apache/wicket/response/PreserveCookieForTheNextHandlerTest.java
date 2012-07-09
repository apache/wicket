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
package org.apache.wicket.response;

import java.util.List;

import javax.servlet.http.Cookie;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.util.cookies.CookieUtils;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.junit.Test;

/**
 * @since 1.5.8
 */
public class PreserveCookieForTheNextHandlerTest extends WicketTestCase
{
	/**
	 * Verifies that any meta data actions set to a BufferedWebResponse during page rendering
	 * wont be lost if at some point during the rendering a new IRequestHandler is scheduled.
	 *
	 * https://issues.apache.org/jira/browse/WICKET-4358
	 */
	@Test
	public void preserveCookie()
	{
		tester.startPage(StartPage.class);
		tester.assertRenderedPage(StartPage.class);
		assertEquals(0, tester.getLastResponse().getCookies().size());

		tester.clickLink("link");
		tester.assertRenderedPage(StartPage.class);
		List<Cookie> cookies = tester.getLastResponse().getCookies();
		assertEquals(1, cookies.size());
		assertEquals("value", cookies.get(0).getValue());
	}

	public static final class StartPage extends WebPage implements IMarkupResourceStreamProvider
	{
		public StartPage()
		{
			add(new BookmarkablePageLink<Void>("link", SetCookiePage.class));
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container, Class<?> containerClass)
		{
			return new StringResourceStream("<html><body><a wicket:id='link'>Link</a></body></html>");
		}
	}

	public static final class SetCookiePage extends WebPage implements IMarkupResourceStreamProvider
	{
		@Override
		protected void onBeforeRender()
		{
			super.onBeforeRender();

			// set a cookie (BufferedWebResponse metadata action)
			new CookieUtils().save("test-cookie", "value");

			// and schedule a new IRequestHandler
			setResponsePage(StartPage.class);
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container, Class<?> containerClass)
		{
			return new StringResourceStream("<html/>");
		}
	}
}
