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
package org.apache.wicket.markup.html.internal.headeritems;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.wicket.Page;
import org.apache.wicket.markup.MarkupException;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.tester.DummyPanelPage;
import org.apache.wicket.util.tester.WicketTestCase;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.Test;

/**
 * Tests for <wicket:header-items/> element
 */
class HtmlHeaderItemsContainerTest extends WicketTestCase
{
	@Test
	void withHeaderItems()
	{
		tester.startPage(PageWithHeaderItems.class);
		String responseAsString = tester.getLastResponseAsString();

		int idxMetaCharset = responseAsString.indexOf("<meta charset=\"utf-8\"");
		int idxMetaPanelWicketHead = responseAsString.indexOf("meta name=\"panel-wicket-head\"");
		int idxWicketAjaxJs = responseAsString.indexOf("wicket-ajax-jquery.js");
		int idxTitleElement = responseAsString.indexOf("<title>Apache Wicket Quickstart</title>");
		int idxMetaFromBasePage = responseAsString.indexOf("<meta name='fromBasePage' content='1'");

		assertThat(idxMetaCharset).isLessThan(idxMetaPanelWicketHead).withFailMessage(
			"<meta charset> should be rendered before <meta name=\"panel-wicket-head\"");

		assertThat(idxMetaPanelWicketHead).isLessThan(idxWicketAjaxJs).withFailMessage(
			"<meta  name=\"panel-wicket-head\"> should be rendered before <script src=wicket-ajax-jquery.js>");

		assertThat(idxWicketAjaxJs).isLessThan(idxTitleElement).withFailMessage(
			"<script src=wicket-ajax-jquery.js> should be rendered before the <title> element");

		assertThat(idxMetaFromBasePage).isLessThan(idxTitleElement).withFailMessage(
			"<meta name='fromBasePage'> should be rendered before the <title> element");
	}

	@Test
	void withoutHeaderItems()
	{
		tester.startPage(PageWithoutHeaderItems.class);
		String responseAsString = tester.getLastResponseAsString();

		int idxMetaCharset = responseAsString.indexOf("<meta charset=\"utf-8\"");
		int idxMetaPanelWicketHead = responseAsString.indexOf("meta name=\"panel-wicket-head\"");
		int idxWicketAjaxJs = responseAsString.indexOf("wicket-ajax-jquery.js");
		int idxTitleElement = responseAsString.indexOf("<title>Apache Wicket Quickstart</title>");
		int idxMetaFromBasePage = responseAsString.indexOf("<meta name='fromBasePage' content='1'");

		assertThat(idxMetaPanelWicketHead).isLessThan(idxWicketAjaxJs).withFailMessage(
			"<meta name=\"panel-wicket-head\"> should be rendered before <script src=wicket-ajax-jquery.js>");

		assertThat(idxWicketAjaxJs).isLessThan(idxMetaCharset).withFailMessage(
			"<script src=wicket-ajax-jquery.js> should be rendered before <meta charset>");

		assertThat(idxMetaCharset).isLessThan(idxTitleElement)
			.withFailMessage("<meta charset> should be rendered before the <title> element");

		assertThat(idxTitleElement).isLessThan(idxMetaFromBasePage).withFailMessage(
			"<title> should be rendered before the <meta name='fromBasePage'> element");
	}

	@Test
	void withoutHeaderItemsWithWicketHead()
	{
		tester.startPage(SubPageWithoutHeaderItemsAndWicketHead.class);
		String responseAsString = tester.getLastResponseAsString();

		int idxMetaCharset = responseAsString.indexOf("<meta charset=\"utf-8\"");
		int idxMetaPanelWicketHead = responseAsString.indexOf("meta name=\"panel-wicket-head\"");
		int idxWicketAjaxJs = responseAsString.indexOf("wicket-ajax-jquery.js");
		int idxTitleElement = responseAsString.indexOf("<title>Apache Wicket Quickstart</title>");
		int idxMetaFromBasePage = responseAsString.indexOf("<meta name='fromBasePage' content='1'");
		int idxMetaFromSubPage = responseAsString
			.indexOf("<meta name=\"SubPageWithoutHeaderItemsAndWicketHead\"");

		assertThat(idxMetaPanelWicketHead).isLessThan(idxWicketAjaxJs).withFailMessage(
			"<meta name=\"panel-wicket-head\"> should be rendered before <script src=wicket-ajax-jquery.js>");

		assertThat(idxWicketAjaxJs).isLessThan(idxMetaCharset).withFailMessage(
			"<script src=wicket-ajax-jquery.js> should be rendered before <meta charset>");

		assertThat(idxMetaCharset).isLessThan(idxTitleElement)
			.withFailMessage("<meta charset> should be rendered before the <title> element");

		assertThat(idxTitleElement).isLessThan(idxMetaFromSubPage).withFailMessage(
			"<title> should be rendered before the <meta name=\"SubPageWithoutHeaderItemsAndWicketHead\" element");

		assertThat(idxMetaFromSubPage).isLessThan(idxMetaFromBasePage).withFailMessage(
			"<meta name='fromBasePage'> should be rendered before the <meta name=\"SubPageWithoutHeaderItemsAndWicketHead\" element");
	}

	@Test
	void withHeaderItemsWithWicketHead()
	{
		tester.startPage(SubPageWithHeaderItemsAndWicketHead.class);
		String responseAsString = tester.getLastResponseAsString();

		int idxMetaCharset = responseAsString.indexOf("<meta charset=\"utf-8\"");
		int idxMetaPanelWicketHead = responseAsString.indexOf("meta name=\"panel-wicket-head\"");
		int idxWicketAjaxJs = responseAsString.indexOf("wicket-ajax-jquery.js");
		int idxTitleElement = responseAsString.indexOf("<title>Apache Wicket Quickstart</title>");
		int idxMetaFromBasePage = responseAsString.indexOf("<meta name='fromBasePage' content='1'");
		int idxMetaFromSubPage = responseAsString
			.indexOf("<meta name=\"SubPageWithHeaderItemsAndWicketHead\"");

		assertThat(idxMetaCharset).isLessThan(idxMetaPanelWicketHead).withFailMessage(
			"<meta charset> should be rendered before <meta name=\"panel-wicket-head\"");

		assertThat(idxMetaPanelWicketHead).isLessThan(idxWicketAjaxJs).withFailMessage(
			"<meta  name=\"panel-wicket-head\"> should be rendered before <script src=wicket-ajax-jquery.js>");

		assertThat(idxWicketAjaxJs).isLessThan(idxTitleElement).withFailMessage(
			"<script src=wicket-ajax-jquery.js> should be rendered before the <title> element");

		assertThat(idxMetaFromSubPage).isLessThan(idxMetaFromBasePage).withFailMessage(
			"<meta name=\"SubPageWithoutHeaderItemsAndWicketHead\"> should be rendered before the <meta name='fromBasePage'> element");

		assertThat(idxMetaFromBasePage).isLessThan(idxTitleElement).withFailMessage(
			"<meta name='fromBasePage'> should be rendered before the <title> element");
	}

	/**
	 * Only one <wicket:header-items/> is allowed only in <head>
	 * 
	 * @see org.apache.wicket.markup.parser.filter.HtmlHeaderSectionHandler
	 */
	@Test
	void pageWithTwoHeaderItems()
	{
		assertThrows(MarkupException.class, () -> {
			tester.startPage(PageWithTwoHeaderItems.class);
		});
	}

	/**
	 * <wicket:header-items/> is allowed only in <head>
	 * 
	 * @see org.apache.wicket.markup.parser.filter.HtmlHeaderSectionHandler
	 */
	@Test
	void pageWithHeaderItemsOutOfHead()
	{
		assertThrows(MarkupException.class, () -> {
			tester.startPage(PageWithHeaderItemsOutOfHead.class);
		});
	}

	/**
	 * Verifies that all header contributions from <wicket:head> containers and IHeaderResponse are
	 * rendered exactly once
	 *
	 * https://issues.apache.org/jira/browse/WICKET-5531
	 */
	@Test
	void withHeaderItemsWithWicketHeadNoDuplicates()
	{
		tester.startPage(SubPageWithHeaderItemsAndWicketHead.class);
		String responseAsString = tester.getLastResponseAsString();

		{
			int idxMetaPanelWicketHead = responseAsString
				.indexOf("meta name=\"panel-wicket-head\"");
			int lastIdxMetaPanelWicketHead = responseAsString
				.lastIndexOf("meta name=\"panel-wicket-head\"");
			assertEquals(idxMetaPanelWicketHead, lastIdxMetaPanelWicketHead);
		}

		{
			int idxWicketAjaxJs = responseAsString.indexOf("wicket-ajax-jquery.js");
			int lastIdxWicketAjaxJs = responseAsString.lastIndexOf("wicket-ajax-jquery.js");
			assertEquals(idxWicketAjaxJs, lastIdxWicketAjaxJs);
		}

		{
			int idxTitleElement = responseAsString
				.indexOf("<title>Apache Wicket Quickstart</title>");
			int lastIdxTitleElement = responseAsString
				.lastIndexOf("<title>Apache Wicket Quickstart</title>");
			assertEquals(idxTitleElement, lastIdxTitleElement);
		}

		{
			int idxMetaFromBasePage = responseAsString
				.indexOf("<meta name='fromBasePage' content='1'");
			int lastIdxMetaFromBasePage = responseAsString
				.lastIndexOf("<meta name='fromBasePage' content='1'");
			assertEquals(idxMetaFromBasePage, lastIdxMetaFromBasePage);
		}

		{
			int idxMetaFromSubPage = responseAsString
				.indexOf("<meta name=\"SubPageWithHeaderItemsAndWicketHead\"");
			int lastIdxMetaFromSubPage = responseAsString
				.lastIndexOf("<meta name=\"SubPageWithHeaderItemsAndWicketHead\"");
			assertEquals(idxMetaFromSubPage, lastIdxMetaFromSubPage);
		}
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-5989
	 */
	@Test
	void pageWithBasePageWithHeaderItems()
	{
		WicketTesterForBasePageWithHeaderItems tester = new WicketTesterForBasePageWithHeaderItems();
		try
		{
			tester.startComponentInPage(new PanelA(DummyPanelPage.TEST_PANEL_ID));
		}
		finally
		{
			tester.destroy();
		}
	}

	private static class WicketTesterForBasePageWithHeaderItems extends WicketTester
	{
		@Override
		protected Page createPage()
		{
			return new PageExtendingBasePageWithHeaderItems(new PageParameters());
		}


		@Override
		protected String createPageMarkup(final String componentId)
		{
			return new PageExtendingBasePageWithHeaderItems(new PageParameters()).getMarkup()
				.toString(true);
		}

	}
}
