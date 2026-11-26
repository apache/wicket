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
package org.apache.wicket.core.request.handler.logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.core.request.handler.IPageAndComponentProvider;
import org.apache.wicket.core.request.handler.ListenerRequestHandler;
import org.apache.wicket.core.request.handler.PageAndComponentProvider;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.StatelessLink;
import org.apache.wicket.protocol.http.PageExpiredException;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.component.IRequestableComponent;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.cycle.IRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ListenerLogDataTest extends WicketTestCase {

	private ListenerRequestHandler executedListenerHandler;

	private int configuredAtEndOfRequest;

	private int beforeRenderedAtEndOfRequest;

	@BeforeEach
	void recordRequests()
	{
		LifecyclePage.reset();

		tester.getApplication().getRequestCycleListeners().add(new IRequestCycleListener()
		{
			@Override
			public void onRequestHandlerExecuted(RequestCycle cycle, IRequestHandler handler)
			{
				if (handler instanceof ListenerRequestHandler)
				{
					executedListenerHandler = (ListenerRequestHandler)handler;
				}
			}

			@Override
			public void onEndRequest(RequestCycle cycle)
			{
				configuredAtEndOfRequest = LifecyclePage.configured;
				beforeRenderedAtEndOfRequest = LifecyclePage.beforeRendered;
			}
		});
	}

	/**
	 * GitHub issue #1653: collecting the log data of a listener whose component a stateless page
	 * lacks must not initialize the page or prepare it for rendering.
	 */
	@Test
	void missingComponentOfStatelessPageRunsNoLifecycleCallbacks()
	{
		LifecyclePage page = new LifecyclePage();
		ListenerRequestHandler handler = new ListenerRequestHandler(
			new PageAndComponentProvider(page, "missing"));

		handler.detach(tester.getRequestCycle());

		assertEquals(0, LifecyclePage.initialized);
		assertEquals(0, LifecyclePage.configured);
		assertEquals(0, LifecyclePage.beforeRendered);

		ListenerLogData logData = handler.getLogData();
		assertEquals(LifecyclePage.class, logData.getPageClass());
		assertEquals("missing", logData.getComponentPath());
		assertNull(logData.getComponentClass());

		assertTrue(page.isPageStateless());
	}

	/**
	 * GitHub issue #1653: collecting the log data must not create a page the provider has not
	 * created yet.
	 */
	@Test
	void pageNotCreatedYetIsNotCreatedForLogData()
	{
		ListenerRequestHandler handler = new ListenerRequestHandler(
			new PageAndComponentProvider(LifecyclePage.class, "link"));

		handler.detach(tester.getRequestCycle());

		assertEquals(0, LifecyclePage.constructed);

		ListenerLogData logData = handler.getLogData();
		assertEquals(LifecyclePage.class, logData.getPageClass());
		assertEquals("link", logData.getComponentPath());
		assertNull(logData.getComponentClass());
	}

	/**
	 * GitHub issue #1653: a listener URL to a component a stateless page never adds re-renders the
	 * page, and no lifecycle callback runs once the request has ended.
	 */
	@Test
	void missingComponentRunsNoLifecycleCallbacksAfterEndOfRequest()
	{
		String url = "wicket/bookmarkable/" + LifecyclePage.class.getName() + "?-1.-missing";
		tester.getRequest().setUrl(Url.parse(url));
		tester.processRequest();

		tester.assertRenderedPage(LifecyclePage.class);
		assertEquals(configuredAtEndOfRequest, LifecyclePage.configured);
		assertEquals(beforeRenderedAtEndOfRequest, LifecyclePage.beforeRendered);
		assertNull(executedListenerHandler.getLogData().getComponentClass());
	}

	/**
	 * A component a stateless page adds in a lifecycle callback is still resolved for its
	 * listener, and the log data still names it.
	 */
	@Test
	void logDataNamesComponentResolvedForListener()
	{
		tester.startPage(LifecyclePage.class);
		tester.clickLink("link");

		assertTrue(LifecyclePage.clicked);

		ListenerLogData logData = executedListenerHandler.getLogData();
		assertEquals(LifecyclePage.class, logData.getPageClass());
		assertEquals("link", logData.getComponentPath());
		assertEquals(ClickedLink.class, logData.getComponentClass());
	}

	/**
	 * The log data of a behavior listener names the behavior, which is looked up on the component
	 * resolved for the listener.
	 */
	@Test
	void logDataNamesBehaviorOfListener()
	{
		tester.startPage(ListenerPage.class);
		tester.executeAjaxEvent("clickable", "click");

		ListenerLogData logData = executedListenerHandler.getLogData();
		assertEquals("clickable", logData.getComponentPath());
		assertEquals(WebMarkupContainer.class, logData.getComponentClass());
		assertEquals(ClickBehavior.class, logData.getBehaviorClass());
		assertNull(logData.getSubmittingComponentClass());
		assertNull(logData.getSubmittingComponentPath());
	}

	/**
	 * The log data of a form submit names the submitting button, which is looked up on the form
	 * resolved for the listener.
	 */
	@Test
	void logDataNamesSubmittingComponentOfForm()
	{
		tester.startPage(ListenerPage.class);
		tester.newFormTester("form").submit("submit");

		ListenerLogData logData = executedListenerHandler.getLogData();
		assertEquals("form", logData.getComponentPath());
		assertEquals(Form.class, logData.getComponentClass());
		assertNull(logData.getBehaviorClass());
		assertEquals(SubmitButton.class, logData.getSubmittingComponentClass());
		assertEquals("form:submit", logData.getSubmittingComponentPath());
	}

	/**
	 * Test for WICKET-6908.
	 */
	@Test
	void neverFails()
	{
		new ListenerLogData(new FailingProvider(), 1);
	}

	/**
	 * Test for WICKET-6908, with a provider that has the component but fails to return it.
	 */
	@Test
	void neverFailsWhenComponentFails()
	{
		new ListenerLogData(new FailingProvider()
		{
			@Override
			public boolean hasComponent()
			{
				return true;
			}
		}, 1);
	}

	private static class FailingProvider implements IPageAndComponentProvider
	{
		@Override
		public boolean wasExpired()
		{
			throw new IllegalStateException();
		}

		@Override
		public boolean hasPageInstance()
		{
			throw new IllegalStateException();
		}

		@Override
		public Integer getRenderCount()
		{
			throw new IllegalStateException();
		}

		@Override
		public PageParameters getPageParameters() throws PageExpiredException
		{
			throw new IllegalStateException();
		}

		@Override
		public IRequestablePage getPageInstance() throws PageExpiredException
		{
			throw new IllegalStateException();
		}

		@Override
		public Integer getPageId()
		{
			throw new IllegalStateException();
		}

		@Override
		public Class<? extends IRequestablePage> getPageClass() throws PageExpiredException
		{
			throw new IllegalStateException();
		}

		@Override
		public boolean doesProvideNewPage()
		{
			throw new IllegalStateException();
		}

		@Override
		public void detach()
		{
		}

		@Override
		public String getComponentPath()
		{
			throw new IllegalStateException();
		}

		@Override
		public boolean hasComponent()
		{
			throw new IllegalStateException();
		}

		@Override
		public IRequestableComponent getComponent()
		{
			throw new IllegalStateException();
		}
	}

	/**
	 * A page with a behavior listener and a form.
	 */
	public static class ListenerPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		public ListenerPage()
		{
			WebMarkupContainer clickable = new WebMarkupContainer("clickable");
			clickable.add(new ClickBehavior());
			add(clickable);

			Form<Void> form = new Form<>("form");
			form.add(new SubmitButton("submit"));
			add(form);
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream("<html><body><span wicket:id=\"clickable\"></span>" +
				"<form wicket:id=\"form\"><input type=\"submit\" wicket:id=\"submit\"/></form>" +
				"</body></html>");
		}
	}

	private static class ClickBehavior extends AjaxEventBehavior
	{
		private static final long serialVersionUID = 1L;

		ClickBehavior()
		{
			super("click");
		}

		@Override
		protected void onEvent(AjaxRequestTarget target)
		{
		}
	}

	private static class SubmitButton extends Button
	{
		private static final long serialVersionUID = 1L;

		SubmitButton(String id)
		{
			super(id);
		}
	}

	/**
	 * A stateless page that adds its link in {@link #onInitialize()} and counts its lifecycle
	 * callbacks, across the instances created for each request.
	 */
	public static class LifecyclePage extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		static int constructed;
		static int initialized;
		static int configured;
		static int beforeRendered;
		static boolean clicked;

		static void reset()
		{
			constructed = 0;
			initialized = 0;
			configured = 0;
			beforeRendered = 0;
			clicked = false;
		}

		public LifecyclePage()
		{
			constructed++;
		}

		@Override
		protected void onInitialize()
		{
			super.onInitialize();
			initialized++;
			add(new ClickedLink("link"));
		}

		@Override
		protected void onConfigure()
		{
			super.onConfigure();
			configured++;
		}

		@Override
		protected void onBeforeRender()
		{
			super.onBeforeRender();
			beforeRendered++;
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream(
				"<html><body><a wicket:id=\"link\">link</a></body></html>");
		}
	}

	private static class ClickedLink extends StatelessLink<Void>
	{
		private static final long serialVersionUID = 1L;

		ClickedLink(String id)
		{
			super(id);
		}

		@Override
		public void onClick()
		{
			LifecyclePage.clicked = true;
		}
	}
}
