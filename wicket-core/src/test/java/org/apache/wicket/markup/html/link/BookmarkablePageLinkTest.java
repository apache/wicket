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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.wicket.MockPageWithLink;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

/**
 * @author jcompagner
 */
class BookmarkablePageLinkTest extends WicketTestCase
{
	/**
	 * @throws Exception
	 */
	@Test
	void bookmarkableRequest() throws Exception
	{
		tester.startPage(BookmarkableHomePageLinksPage.class);
		assertEquals(tester.getLastRenderedPage().getClass(), BookmarkableHomePageLinksPage.class);
	}

	/**
	 * @throws Exception
	 */
	@Test
	void bookmarkableRequestWithIntercept() throws Exception
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
	void bookmarkableRequestWithInterceptWithParams() throws Exception
	{
		PageParameters pp = new PageParameters();
		pp.set("test", "test");

		tester.startPage(BookmarkableThrowsInterceptPage.class, pp);

		assertEquals(tester.getLastRenderedPage().getClass(), BookmarkableSetSecurityPage.class);

		tester.startPage(BookmarkableContinueToPage.class);
		assertEquals(tester.getLastRenderedPage().getClass(), BookmarkableThrowsInterceptPage.class);
	}

	/**
	 * @see <a href="https://issues.apache.org/jira/browse/WICKET-3721">WICKET-3721</a>
	 */
	@Test
	void customParametersWithSpecialCharacters()
	{
		BookmarkablePageLink<MockPageWithLink> link =
			new BookmarkablePageLink<MockPageWithLink>("link", MockPageWithLink.class);
		link.getPageParameters().set("urlEscapeNeeded", "someone's ^b%a&d pa\"rameter");

		tester.startComponentInPage(link, null);
		String expected =
			"<html><head><script type=\"text/javascript\" src=\"./resource/org.apache.wicket.resource.JQueryResourceReference/jquery/jquery-3.4.1.js\"></script>\n"
				+ "<script type=\"text/javascript\" src=\"./resource/org.apache.wicket.ajax.AbstractDefaultAjaxBehavior/res/js/wicket-ajax-jquery.js\"></script>\n"
				+ "<script type=\"text/javascript\">\n" + "/*<![CDATA[*/\n"
				+ "Wicket.Event.add(window, \"domready\", function(event) { \n"
				+ "Wicket.Event.add('link1', 'click', function(event) { var win = this.ownerDocument.defaultView || this.ownerDocument.parentWindow; if (win == window) { window.location.href='./bookmarkable/org.apache.wicket.MockPageWithLink?urlEscapeNeeded=someone%27s+%5Eb%25a%26d+pa%22rameter'; } ;return false;});;\n"
				+ "Wicket.Event.publish(Wicket.Event.Topic.AJAX_HANDLERS_BOUND);\n" + ";});\n"
				+ "/*]]>*/\n" + "</script>\n"
				+ "</head><body><span wicket:id=\"link\" id=\"link1\"></span></body></html>";

		String response = tester.getLastResponse().getDocument();
		assertEquals(expected, response);
	}
}
