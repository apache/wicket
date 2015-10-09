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

import static org.hamcrest.number.OrderingComparison.lessThan;

import org.apache.wicket.markup.MarkupException;
import org.apache.wicket.util.tester.WicketTestCase;
import org.apache.wicket.Page;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.tester.DummyPanelPage;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Test;

/**
 * Tests for <wicket:header-items/> element
 */
public class HtmlHeaderItemsContainerTest extends WicketTestCase
{
	@Test
	public void withHeaderItems()
	{
		tester.startPage(PageWithHeaderItems.class);
		String responseAsString = tester.getLastResponseAsString();

		int idxMetaCharset = responseAsString.indexOf("<meta charset=\"utf-8\"");
		int idxMetaPanelWicketHead = responseAsString.indexOf("meta name=\"panel-wicket-head\"");
		int idxWicketAjaxJs = responseAsString.indexOf("wicket-ajax-jquery.js");
		int idxTitleElement = responseAsString.indexOf("<title>Apache Wicket Quickstart</title>");
		int idxMetaFromBasePage = responseAsString.indexOf("<meta name='fromBasePage' content='1'");

		assertThat("<meta charset> should be rendered before <meta name=\"panel-wicket-head\"",
				idxMetaCharset, lessThan(idxMetaPanelWicketHead));

		assertThat("<meta  name=\"panel-wicket-head\"> should be rendered before <script src=wicket-ajax-jquery.js>",
				idxMetaPanelWicketHead, lessThan(idxWicketAjaxJs));

		assertThat("<script src=wicket-ajax-jquery.js> should be rendered before the <title> element",
				idxWicketAjaxJs, lessThan(idxTitleElement));

		assertThat("<meta name='fromBasePage'> should be rendered before the <title> element",
				idxMetaFromBasePage, lessThan(idxTitleElement));
	}

	@Test
	public void withoutHeaderItems()
	{
		tester.startPage(PageWithoutHeaderItems.class);
		String responseAsString = tester.getLastResponseAsString();

		int idxMetaCharset = responseAsString.indexOf("<meta charset=\"utf-8\"");
		int idxMetaPanelWicketHead = responseAsString.indexOf("meta name=\"panel-wicket-head\"");
		int idxWicketAjaxJs = responseAsString.indexOf("wicket-ajax-jquery.js");
		int idxTitleElement = responseAsString.indexOf("<title>Apache Wicket Quickstart</title>");
		int idxMetaFromBasePage = responseAsString.indexOf("<meta name='fromBasePage' content='1'");

		assertThat("<meta name=\"panel-wicket-head\"> should be rendered before <script src=wicket-ajax-jquery.js>",
				idxMetaPanelWicketHead, lessThan(idxWicketAjaxJs));

		assertThat("<script src=wicket-ajax-jquery.js> should be rendered before <meta charset>",
				idxWicketAjaxJs, lessThan(idxMetaCharset));

		assertThat("<meta charset> should be rendered before the <title> element",
				idxMetaCharset, lessThan(idxTitleElement));

		assertThat("<title> should be rendered before the <meta name='fromBasePage'> element",
				idxTitleElement, lessThan(idxMetaFromBasePage));
	}

	@Test
	public void withoutHeaderItemsWithWicketHead()
	{
		tester.startPage(SubPageWithoutHeaderItemsAndWicketHead.class);
		String responseAsString = tester.getLastResponseAsString();

		int idxMetaCharset = responseAsString.indexOf("<meta charset=\"utf-8\"");
		int idxMetaPanelWicketHead = responseAsString.indexOf("meta name=\"panel-wicket-head\"");
		int idxWicketAjaxJs = responseAsString.indexOf("wicket-ajax-jquery.js");
		int idxTitleElement = responseAsString.indexOf("<title>Apache Wicket Quickstart</title>");
		int idxMetaFromBasePage = responseAsString.indexOf("<meta name='fromBasePage' content='1'");
		int idxMetaFromSubPage = responseAsString.indexOf("<meta name=\"SubPageWithoutHeaderItemsAndWicketHead\"");

		assertThat("<meta name=\"panel-wicket-head\"> should be rendered before <script src=wicket-ajax-jquery.js>",
				idxMetaPanelWicketHead, lessThan(idxWicketAjaxJs));

		assertThat("<script src=wicket-ajax-jquery.js> should be rendered before <meta charset>",
				idxWicketAjaxJs, lessThan(idxMetaCharset));

		assertThat("<meta charset> should be rendered before the <title> element",
				idxMetaCharset, lessThan(idxTitleElement));

		assertThat("<title> should be rendered before the <meta name=\"SubPageWithoutHeaderItemsAndWicketHead\" element",
				idxTitleElement, lessThan(idxMetaFromSubPage));

		assertThat("<meta name='fromBasePage'> should be rendered before the <meta name=\"SubPageWithoutHeaderItemsAndWicketHead\" element",
				idxMetaFromSubPage, lessThan(idxMetaFromBasePage));
	}

	@Test
	public void withHeaderItemsWithWicketHead()
	{
		tester.startPage(SubPageWithHeaderItemsAndWicketHead.class);
		String responseAsString = tester.getLastResponseAsString();

		int idxMetaCharset = responseAsString.indexOf("<meta charset=\"utf-8\"");
		int idxMetaPanelWicketHead = responseAsString.indexOf("meta name=\"panel-wicket-head\"");
		int idxWicketAjaxJs = responseAsString.indexOf("wicket-ajax-jquery.js");
		int idxTitleElement = responseAsString.indexOf("<title>Apache Wicket Quickstart</title>");
		int idxMetaFromBasePage = responseAsString.indexOf("<meta name='fromBasePage' content='1'");
		int idxMetaFromSubPage = responseAsString.indexOf("<meta name=\"SubPageWithHeaderItemsAndWicketHead\"");

		assertThat("<meta charset> should be rendered before <meta name=\"panel-wicket-head\"",
				idxMetaCharset, lessThan(idxMetaPanelWicketHead));

		assertThat("<meta  name=\"panel-wicket-head\"> should be rendered before <script src=wicket-ajax-jquery.js>",
				idxMetaPanelWicketHead, lessThan(idxWicketAjaxJs));

		assertThat("<script src=wicket-ajax-jquery.js> should be rendered before the <title> element",
				idxWicketAjaxJs, lessThan(idxTitleElement));

		assertThat("<meta name=\"SubPageWithoutHeaderItemsAndWicketHead\"> should be rendered before the <meta name='fromBasePage'> element",
				idxMetaFromSubPage, lessThan(idxMetaFromBasePage));

		assertThat("<meta name='fromBasePage'> should be rendered before the <title> element",
				idxMetaFromBasePage, lessThan(idxTitleElement));
	}

	/**
	 * Only one <wicket:header-items/> is allowed only in <head>
	 * @see org.apache.wicket.markup.parser.filter.HtmlHeaderSectionHandler
	 */
	@Test(expected = MarkupException.class)
	public void pageWithTwoHeaderItems()
	{
		tester.startPage(PageWithTwoHeaderItems.class);
	}

	/**
	 * <wicket:header-items/> is allowed only in <head>
	 * @see org.apache.wicket.markup.parser.filter.HtmlHeaderSectionHandler
	 */
	@Test(expected = MarkupException.class)
	public void pageWithHeaderItemsOutOfHead()
	{
		tester.startPage(PageWithHeaderItemsOutOfHead.class);
	}

	/**
	 * Verifies that all header contributions from <wicket:head> containers
	 * and IHeaderResponse are rendered exactly once
	 *
	 * https://issues.apache.org/jira/browse/WICKET-5531
	 */
	@Test
	public void withHeaderItemsWithWicketHeadNoDuplicates()
	{
		tester.startPage(SubPageWithHeaderItemsAndWicketHead.class);
		String responseAsString = tester.getLastResponseAsString();

		{
			int idxMetaPanelWicketHead = responseAsString.indexOf("meta name=\"panel-wicket-head\"");
			int lastIdxMetaPanelWicketHead = responseAsString.lastIndexOf("meta name=\"panel-wicket-head\"");
			assertEquals(idxMetaPanelWicketHead, lastIdxMetaPanelWicketHead);
		}

		{
			int idxWicketAjaxJs = responseAsString.indexOf("wicket-ajax-jquery.js");
			int lastIdxWicketAjaxJs = responseAsString.lastIndexOf("wicket-ajax-jquery.js");
			assertEquals(idxWicketAjaxJs, lastIdxWicketAjaxJs);
		}

		{
			int idxTitleElement = responseAsString.indexOf("<title>Apache Wicket Quickstart</title>");
			int lastIdxTitleElement = responseAsString.lastIndexOf("<title>Apache Wicket Quickstart</title>");
			assertEquals(idxTitleElement, lastIdxTitleElement);
		}

		{
			int idxMetaFromBasePage = responseAsString.indexOf("<meta name='fromBasePage' content='1'");
			int lastIdxMetaFromBasePage = responseAsString.lastIndexOf("<meta name='fromBasePage' content='1'");
			assertEquals(idxMetaFromBasePage, lastIdxMetaFromBasePage);
		}

		{
			int idxMetaFromSubPage = responseAsString.indexOf("<meta name=\"SubPageWithHeaderItemsAndWicketHead\"");
			int lastIdxMetaFromSubPage = responseAsString.lastIndexOf("<meta name=\"SubPageWithHeaderItemsAndWicketHead\"");
			assertEquals(idxMetaFromSubPage, lastIdxMetaFromSubPage);
		}
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-5989
	 */
	@Test
	public void pageWithBasePageWithHeaderItems() {
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
		protected Page createPage() {
			return new PageExtendingBasePageWithHeaderItems(new PageParameters());
		}


		@Override
		protected String createPageMarkup(final String componentId) {
			return new PageExtendingBasePageWithHeaderItems(new PageParameters()).getMarkup().toString(true);
		}

	}
}
