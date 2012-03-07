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

import java.io.IOException;
import java.text.ParseException;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.MockPage;
import org.apache.wicket.Page;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.Url;
import org.apache.wicket.core.request.mapper.StalePageException;
import org.apache.wicket.core.request.mapper.TestMapperContext;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.util.resource.StringResourceStream;
import org.junit.Test;

/**
 * @author pedro
 */
public class PageProviderTest extends WicketTestCase
{

	/**
	 * <a href="https://issues.apache.org/jira/browse/WICKET-4046">WICKET-4046</a>
	 */
	@Test
	public void pageProviderDontDeserializeOnePageTwice()
	{
		int oldState = 0;
		int newState = 1;

		StatefullMockPage testPage = new StatefullMockPage();
		testPage.state = oldState;

		// storing test page
		TestMapperContext mapperContext = new TestMapperContext();
		mapperContext.getPageManager().touchPage(testPage);
		mapperContext.getPageManager().commitRequest();

		// by cleaning session cache we make sure of not being testing the same in-memory instance
		mapperContext.cleanSessionCache();

		PageProvider pageProvider = mapperContext.new TestPageProvider(testPage.getPageId(), 0);

		// simulation an test call to isNewPageInstance
		boolean isNewPageInstance = pageProvider.isNewPageInstance();
		assertFalse("test page is already stored", isNewPageInstance);

		// changing some sate
		StatefullMockPage providedPage = (StatefullMockPage)pageProvider.getPageInstance();
		providedPage.state = newState;
		mapperContext.getPageManager().touchPage(providedPage);
		mapperContext.getPageManager().commitRequest();


		mapperContext.cleanSessionCache();

		StatefullMockPage restauredPageAfterStateChage = (StatefullMockPage)mapperContext.getPageInstance(testPage.getPageId());

		// OK, if the correct page got touched/stores its change will be visible now
		assertEquals(newState, restauredPageAfterStateChage.state);
	}

	private static class StatefullMockPage extends MockPage
	{
		int state = 0;
	}

	/**
	 * @see <a href="https://issues.apache.org/jira/browse/WICKET-3252">WICKET-3252</a>
	 * */
	public void testStalePageException()
	{
		tester.startPage(TestPage.class);
		TestPage testPage = (TestPage)tester.getLastRenderedPage();

		// cache the link to the first page version
		String firstHRef = tester.urlFor(testPage.link);
		// request a new page
		tester.clickLink("link");

		try
		{
			// just making clear that we are in the tester land
			tester.setExposeExceptions(true);
			// try to get the old one
			tester.getRequest().setURL(firstHRef);
			tester.processRequest();
			fail("Stale page request process should throw StalePageException");
		}
		catch (StalePageException e)
		{
			assertTrue(true);
		}
	}

	/**
	 * Request an old URL in an AJAX request and assert that we have an AJAX response.
	 * 
	 * @see <a href="https://issues.apache.org/jira/browse/WICKET-3252">WICKET-3252</a>
	 * @throws ParseException
	 * @throws ResourceStreamNotFoundException
	 * @throws IOException
	 * 
	 */
	public void testStalePageExceptionOnAjaxRequest() throws IOException,
		ResourceStreamNotFoundException, ParseException
	{
		tester.startPage(TestPage.class);

		TestPage testPage = (TestPage)tester.getLastRenderedPage();
		// cache the old URL
		Url firstAjaxLinkUrl = tester.urlFor(testPage.ajaxLink);

		// request a new page
		tester.clickLink("link");

		tester.setExposeExceptions(false);
		tester.setFollowRedirects(false);
		tester.setUseRequestUrlAsBase(false);

		// execute the old URL
		executeAjaxUrlWithLastBaseUrl(firstAjaxLinkUrl);

		assertTrue(tester.getLastResponseAsString().startsWith("<ajax-response>"));
		assertTrue(tester.getLastResponse().isRedirect());
	}

	/**
	 * @param url
	 * @throws ParseException
	 * @throws ResourceStreamNotFoundException
	 * @throws IOException
	 */
	private void executeAjaxUrlWithLastBaseUrl(Url url) throws IOException,
		ResourceStreamNotFoundException, ParseException
	{
		tester.getRequest().setUrl(url);
		tester.getRequest().addHeader("Wicket-Ajax-BaseURL",
			tester.getWicketAjaxBaseUrlEncodedInLastResponse());
		tester.getRequest().addHeader("Wicket-Ajax", "true");
		tester.processRequest();
	}

	/**
	 * Asserting that an intercept is returned as result of an redirect response. Important to
	 * prevent an resulting page with broken relative paths, as related in <a
	 * href="https://issues.apache.org/jira/browse/WICKET-3339">WICKET-3339</a>
	 */
	public void test()
	{
		tester.setFollowRedirects(false);
		tester.startPage(TestPage.class);
		tester.clickLink("restartIntercept");
		assertTrue(tester.getLastResponse().isRedirect());
	}

	public void testPageProperties_provided()
	{
		PageProvider provider = new PageProvider(new StatelessPageTest());
		assertTrue(provider.hasPageInstance());
		assertFalse(provider.isPageInstanceFresh());
	}

	public void testPageProperties_bookmarkable()
	{
		PageProvider provider = new PageProvider(StatelessPageTest.class);
		assertFalse(provider.hasPageInstance());
		try
		{
			provider.isPageInstanceFresh();
			fail("expected illegal state exception");
		}
		catch (IllegalStateException e)
		{
			// expected
		}

		provider.getPageInstance();

		assertTrue(provider.hasPageInstance());
		assertTrue(provider.isPageInstanceFresh());
	}

	public void testPageProperties_stored()
	{
		TestMapperContext mapperContext = new TestMapperContext();
		Page page = new TestPage();
		mapperContext.getPageManager().touchPage(page);
		mapperContext.getPageManager().commitRequest();

		// by cleaning session cache we make sure of not being testing the same in-memory instance
		mapperContext.cleanSessionCache();

		PageProvider provider = mapperContext.new TestPageProvider(page.getPageId(), 0);
		assertTrue(provider.hasPageInstance());
		assertFalse(provider.isPageInstanceFresh());
	}

	/** */
	public static class TestPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;
		Link<Void> link;
		AjaxLink<Void> ajaxLink;

		/** */
		public TestPage()
		{
			add(link = new Link<Void>("link")
			{
				private static final long serialVersionUID = 1L;

				@Override
				public void onClick()
				{
				}
			});
			add(ajaxLink = new AjaxLink<Void>("ajaxLink")
			{
				private static final long serialVersionUID = 1L;

				@Override
				public void onClick(AjaxRequestTarget target)
				{
				}
			});
			add(new Link<Void>("restartIntercept")
			{
				private static final long serialVersionUID = 1L;

				@Override
				public void onClick()
				{
					throw new RestartResponseAtInterceptPageException(StatelessPageTest.class);
				}
			});
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream(
				"<html><body><a wicket:id=\"link\"></a><a wicket:id=\"ajaxLink\"></a>"
					+ "<a wicket:id=\"restartIntercept\"></a></body></html>");
		}
	}

	/** just giving readability to tests */
	public static class StatelessPageTest extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream("<html><body></body></html>");
		}
	}
}
