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
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.mapper.StalePageException;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.util.resource.StringResourceStream;

/**
 * @author pedro
 */
public class PageProviderTest extends WicketTestCase
{
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
			tester.getWicketAjaxBaseUrlFromLastRequest());
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

		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream("<html><body></body></html>");
		}
	}
}
