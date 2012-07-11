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
package org.apache.wicket.markup.head.filter;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.IHeaderResponseDecorator;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests for FilteringHeaderResponse
 * 
 * @since 6.0
 */
public class FilteringHeaderResponseTest extends WicketTestCase
{
	@Override
	protected WebApplication newApplication()
	{
		MockApplication application = new MockApplication()
		{
			@Override
			protected void init()
			{
				super.init();

				setHeaderResponseDecorator(new IHeaderResponseDecorator()
				{
					@Override
					public IHeaderResponse decorate(IHeaderResponse response)
					{
						return new JavaScriptFilteredIntoFooterHeaderResponse(response, "headerJS");
					}
				});
			}
		};

		return application;
	}

	/**
	 * Tests using FilteredResponseContainer in <head>
	 * 
	 * https://issues.apache.org/jira/browse/WICKET-4396
	 */
	@Test
	@Ignore
	public void filter()
	{
		HeaderFilteringPage page = new HeaderFilteringPage();
		tester.startPage(page);
	}

	private static class HeaderFilteringPage extends WebPage
		implements
			IMarkupResourceStreamProvider
	{
		private HeaderFilteringPage()
		{
			add(new HeaderResponseContainer("headerJS", "headerJS"));
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream(
				"<html><head><wicket:container wicket:id='headerJS'/></head></html>");
		}
	}

	@Test
	public void footerDependsOnHeadItem() throws Exception
	{
		tester.getApplication().setHeaderResponseDecorator(new IHeaderResponseDecorator()
		{
			@Override
			public IHeaderResponse decorate(IHeaderResponse response)
			{
				// use this header resource decorator to load all JavaScript resources in the page
				// footer (after </body>)
				return new JavaScriptFilteredIntoFooterHeaderResponse(response, "footerJS");
			}
		});
		executeTest(FilteredHeaderPage.class, "FilteredHeaderPageExpected.html");
	}
}
