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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.text.ParseException;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.Session;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.core.request.mapper.MountedMapper;
import org.apache.wicket.markup.IMarkupFragment;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.Markup;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.StatelessLink;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.resource.DummyPage;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ListenerRequestHandler}
 */
public class ListenerRequestHandlerTest extends WicketTestCase
{

	/**
	 * WICKET-5466
	 */
	@Test
	void removedComponent()
	{
		// non-existing component on fresh page is ignored
		PageAndComponentProvider freshPage = new PageAndComponentProvider(DummyPage.class, null,
			"foo");
		new ListenerRequestHandler(freshPage).respond(tester.getRequestCycle());

		// non-existing component on old page fails
		PageAndComponentProvider oldPage = new PageAndComponentProvider(new DummyPage(), "foo");
		try
		{
			new ListenerRequestHandler(oldPage).respond(tester.getRequestCycle());
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
	void recreateThePageWhenListenereInterfaceIsExecutedOnExpiredPage() throws Exception
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
		assertTrue(lastRenderedPageId > initialPageId, "A new page must be create ");
	}

	private void executeAjaxUrlWithLastBaseUrl(Url url)
		throws IOException, ResourceStreamNotFoundException, ParseException
	{
		tester.getRequest().setUrl(url);
		tester.getRequest().addHeader("Wicket-Ajax-BaseURL",
			tester.getWicketAjaxBaseUrlEncodedInLastResponse());
		tester.getRequest().addHeader("Wicket-Ajax", "true");
		tester.processRequest();
	}

	/**
	 * Testcase for WICKET-4185
	 */
	@Test
	void isPageInstanceCreatedOnClassLinks()
	{
		PageAndComponentProvider provider = new PageAndComponentProvider(Page.class, "link");
		ListenerRequestHandler handler = new ListenerRequestHandler(provider);
		assertFalse(handler.isPageInstanceCreated(), "A new page must be create ");
	}

	@Test
	void executeStatelessLinkInAFreshPage()
	{
		tester.startPage(StatelessPage.class);

		tester.clickLink("statelessLink");

		StatelessPage page = (StatelessPage)tester.getLastRenderedPage();
		assertTrue(page.invoked);
		assertTrue(page.executedInAnFreshPage);
	}

	@Test
	void executeStatelessLinkInAFreshPageAtASegment()
	{
		tester.getApplication().getRootRequestMapperAsCompound().add(
			new MountedMapper("/segment", TemporarilyStateful.class));
		tester.startPage(TemporarilyStateful.class);

		tester.clickLink("statelessLink");

		TemporarilyStateful page = (TemporarilyStateful)tester.getLastRenderedPage();
		assertTrue(page.invoked);
		assertTrue(page.executedInAnFreshPage);
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

	public static class StatelessPage extends WebPage
	{
		boolean invoked;
		boolean executedInAnFreshPage;
		private boolean initialState = true;

		public StatelessPage(PageParameters pageParameters)
		{
			super(pageParameters);
			add(new StatelessLink<Object>("statelessLink")
			{
				public void onClick()
				{
					invoked = true;
					executedInAnFreshPage = initialState;
				}
			});
		}

		@Override
		public IMarkupFragment getMarkup()
		{
			return Markup.of("<html><body><a wicket:id=\"statelessLink\"></a></body></html>");
		}

		@Override
		protected void onBeforeRender()
		{
			initialState = false;
			super.onBeforeRender();
		}
	}
	public static class TemporarilyStateful extends StatelessPage
	{

		public TemporarilyStateful(PageParameters pageParameters)
		{
			super(pageParameters);
			setStatelessHint(false);
		}


		@Override
		protected void onBeforeRender()
		{
			setStatelessHint(true);
			super.onBeforeRender();
		}
	}

}
