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
package org.apache.wicket.core.request.handler;

import java.io.IOException;
import java.text.ParseException;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.RequestListenerInterface;
import org.apache.wicket.Session;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.IOnChangeListener;
import org.apache.wicket.markup.html.link.ILinkListener;
import org.apache.wicket.request.Url;
import org.apache.wicket.resource.DummyPage;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 * Tests for {@link ListenerInterfaceRequestHandler}
 */
public class ListenerInterfaceRequestHandlerTest extends WicketTestCase
{

	/**
	 * WICKET-5466
	 */
	@Test
	public void removedComponent()
	{
		// non-existing component on fresh page is ignored
		PageAndComponentProvider freshPage = new PageAndComponentProvider(DummyPage.class, null,
			"foo");
		new ListenerInterfaceRequestHandler(freshPage, IOnChangeListener.INTERFACE).respond(tester
			.getRequestCycle());

		// non-existing component on old page fails
		PageAndComponentProvider oldPage = new PageAndComponentProvider(new DummyPage(), "foo");
		try
		{
			new ListenerInterfaceRequestHandler(oldPage, IOnChangeListener.INTERFACE)
				.respond(tester.getRequestCycle());
			fail();
		}
		catch (WicketRuntimeException ex)
		{
			assertEquals("Component 'foo' has been removed from page.", ex.getMessage());
		}
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-4116
	 * 
	 * @throws Exception
	 */
	@Test
	public void recreateThePageWhenListenereInterfaceIsExecutedOnExpiredPage() throws Exception
	{
		tester.getApplication().mountPage("ajaxLink", AjaxLinkExpirePage.class);
		AjaxLinkExpirePage page = tester.startPage(AjaxLinkExpirePage.class);

		int initialPageId = page.getPageId();

		Url urlToAjaxLink = tester.urlFor(page.link);
		Session session = tester.getSession();
		session.clear();

		// fire a request to the ajax link on the expired page
		executeAjaxUrlWithLastBaseUrl(urlToAjaxLink);

		Page lastRenderedPage = tester.getLastRenderedPage();
		int lastRenderedPageId = lastRenderedPage.getPageId();
		assertTrue("A new page must be create ", lastRenderedPageId > initialPageId);
	}

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
	 * Test page for #recreateThePageWhenListenereInterfaceIsExecutedOnExpiredPage()
	 */
	public static class AjaxLinkExpirePage extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		private AjaxLink<Void> link;

		/**
		 * Constructor.
		 */
		public AjaxLinkExpirePage()
		{
			add(link = new AjaxLink<Void>("test")
			{
				private static final long serialVersionUID = 1L;

				@Override
				public void onClick(AjaxRequestTarget target)
				{
					System.err.println("clicked");
				}
			});
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{

			return new StringResourceStream(
				"<html><body><a wicket:id='test'>Link</a></body></html>");
		}

	}

	/**
	 * Testcase for WICKET-4185
	 */
	@Test
	public void isPageInstanceCreatedOnClassLinks()
	{
		PageAndComponentProvider provider = new PageAndComponentProvider(Page.class, "link");
		ListenerInterfaceRequestHandler handler = new ListenerInterfaceRequestHandler(provider,
			RequestListenerInterface.forName(ILinkListener.class.getSimpleName()));
		assertFalse("Handler should not report a page instance is available ",
			handler.isPageInstanceCreated());
	}
}
