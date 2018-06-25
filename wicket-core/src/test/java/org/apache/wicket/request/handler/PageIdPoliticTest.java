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
package org.apache.wicket.request.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.Charset;

import org.apache.wicket.IPageManagerProvider;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.page.IManageablePage;
import org.apache.wicket.page.IPageManager;
import org.apache.wicket.page.PageManager;
import org.apache.wicket.pageStore.IPageContext;
import org.apache.wicket.pageStore.InMemoryPageStore;
import org.apache.wicket.pageStore.RequestPageStore;
import org.apache.wicket.request.Url;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Pedro Santos
 */
public class PageIdPoliticTest
{
	private WicketTester tester;
	private InMemoryPageStore pageStore;
	private MockApplication application;
	private int storeCount;

	/**
	 * Asserting that page get touched in an AJAX request that is only repaint its children. <br />
	 * In this case no new page id is being generated and the old page is touched and stored again.<br />
	 * 
	 * @see <a href="https://issues.apache.org/jira/browse/WICKET-3667">WICKET-3667</a>
	 */
	@Test
	void testPageGetsTouchedInAjaxRequest()
	{
		TestPage testPage = new TestPage();
		tester.startPage(TestPage.class);
		int referenceStoreCount = storeCount;
		tester.executeAjaxUrl(testPage.getAjaxUrl(tester.getRequest().getCharacterEncoding()));
		// the page should be stored even for ajax requests
		assertEquals(referenceStoreCount + 1, storeCount);
	}

	/**
	 * 
	 */
	@Test
	void testPageIdDontGetIncreasedInAjaxRequest()
	{
		TestPage testPage = new TestPage();
		tester.startPage(testPage);
		String testPageId = testPage.getId();
		tester.executeAjaxUrl(testPage.getAjaxUrl(tester.getRequest().getCharacterEncoding()));
		assertEquals(testPageId, testPage.getId());
		assertTrue(testPage.ajaxCallbackExecuted);
	}

	@BeforeEach
	void setUp() throws Exception
	{
		application = new MockApplication();
		pageStore = new InMemoryPageStore("test", Integer.MAX_VALUE)
		{
			@Override
			public void addPage(IPageContext context, IManageablePage page)
			{
				super.addPage(context, page);
				storeCount++;
			}
		};
		tester = new WicketTester(application)
		{
			@Override
			protected IPageManagerProvider newTestPageManagerProvider()
			{
				return new IPageManagerProvider()
				{
					@Override
					public IPageManager get()
					{
						return new PageManager(new RequestPageStore(pageStore));
					}
				};
			}
		};
	}

	@AfterEach
	void tearDown() throws Exception
	{
		tester.destroy();
	}

	/** */
	public static class TestPage extends WebPage implements IMarkupResourceStreamProvider
	{
		/** */
		private static final long serialVersionUID = 1L;
		AjaxEventBehavior eventBehavior;
		boolean ajaxCallbackExecuted;

		/**
		 * Construct.
		 */
		public TestPage()
		{
			WebComponent component;
			component = new WebComponent("component");
			component.add(eventBehavior = new AjaxEventBehavior("click")
			{
				/** */
				private static final long serialVersionUID = 1L;

				@Override
				protected void onEvent(AjaxRequestTarget target)
				{
					ajaxCallbackExecuted = true;
				}
			});
			add(component);
		}

		/**
		 * @param encoding
		 * @return ajaxUrl
		 */
		public Url getAjaxUrl(String encoding)
		{
			return Url.parse(eventBehavior.getCallbackUrl().toString(), Charset.forName(encoding));
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream(
				"<html><body><a wicket:id=\"component\"></a></body></html>");
		}

	}
}
