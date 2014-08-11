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
package org.apache.wicket.markup.html.link;

import org.apache.wicket.MockPageWithLink;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.request.mapper.parameter.INamedParameters;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author jcompagner
 */
public class BookmarkablePageLinkTest extends WicketTestCase
{
	/**
	 * @throws Exception
	 */
	@Test
	public void bookmarkableRequest() throws Exception
	{
		tester.startPage(BookmarkableHomePageLinksPage.class);
		assertEquals(tester.getLastRenderedPage().getClass(), BookmarkableHomePageLinksPage.class);
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void bookmarkableRequestWithIntercept() throws Exception
	{
		tester.startPage(BookmarkableThrowsInterceptPage.class);

		assertEquals(tester.getLastRenderedPage().getClass(), BookmarkableSetSecurityPage.class);

		tester.startPage(BookmarkableContinueToPage.class);
		assertEquals(tester.getLastRenderedPage().getClass(), BookmarkableThrowsInterceptPage.class);
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void bookmarkableRequestWithInterceptWithParams() throws Exception
	{
		PageParameters pp = new PageParameters();
		pp.set("test", "test", INamedParameters.Type.MANUAL);

		tester.startPage(BookmarkableThrowsInterceptPage.class, pp);

		assertEquals(tester.getLastRenderedPage().getClass(), BookmarkableSetSecurityPage.class);

		tester.startPage(BookmarkableContinueToPage.class);
		assertEquals(tester.getLastRenderedPage().getClass(), BookmarkableThrowsInterceptPage.class);
	}

	/**
	 * @see <a href="https://issues.apache.org/jira/browse/WICKET-3721">WICKET-3721</a>
	 */
	@Test
	public void customParametersWithSpecialCharacters()
	{
		BookmarkablePageLink<MockPageWithLink> link = new BookmarkablePageLink<MockPageWithLink>(
			"link", MockPageWithLink.class);
		link.getPageParameters().set("urlEscapeNeeded", "someone's ^b%a&d pa\"rameter", INamedParameters.Type.MANUAL);

		tester.startComponentInPage(link, null);
		String response = tester.getLastResponse().getDocument();
		Assert.assertEquals(
			"<html><body><span wicket:id=\"link\" onclick=\"var win = this.ownerDocument.defaultView || this.ownerDocument.parentWindow; if (win == window) { window.location.href=&#039;./bookmarkable/org.apache.wicket.MockPageWithLink?urlEscapeNeeded=someone%27s+%5Eb%25a%26d+pa%22rameter&#039;; } ;return false\"></span></body></html>",
			response);
	}
}
