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
package org.apache.wicket.ajax;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.MockPageWithLink;
import org.apache.wicket.MockPageWithLinkAndComponent;
import org.apache.wicket.Page;
import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.cycle.IRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.response.filter.IResponseFilter;
import org.apache.wicket.settings.RequestCycleSettings;
import org.apache.wicket.util.encoding.UrlEncoder;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.DiffUtil;
import org.apache.wicket.util.tester.WicketTestCase;
import org.apache.wicket.util.tester.WicketTester;
import org.apache.wicket.util.time.Instants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test the {@link AjaxRequestHandler}.
 *
 * @author Frank Bille
 */
class AjaxRequestHandlerTest extends WicketTestCase
{
	private static final Logger log = LoggerFactory.getLogger(AjaxRequestHandlerTest.class);

	/**
	 * Test that a normal <style> header contribution is added correctly.
	 *
	 * @throws IOException
	 */
	@Test
	void headerContribution1() throws IOException
	{
		executeHeaderTest(MockComponent1.class, "MockComponent1-expected.html");
	}

	/**
	 * Test that if there are no headers contributed in any components added to the response, we
	 * then don't add <header-contribution> at all.
	 *
	 * @throws IOException
	 */
	@Test
	void headerContribution2() throws IOException
	{
		executeHeaderTest(MockComponent2.class);
	}

	/**
	 * Test that a link with a wicket:id is added correctly.
	 *
	 * @throws IOException
	 */
	@Test
	void headerContribution3() throws IOException
	{
		executeHeaderTest(MockComponent3.class, "MockComponent3-expected.html");
	}

	private <C extends Component> void executeHeaderTest(final Class<C> componentClass)
		throws IOException
	{
		executeHeaderTest(componentClass, null);
	}

	private <C extends Component> void executeHeaderTest(final Class<C> componentClass,
		String expectedFile) throws IOException
	{
		final MockPageWithLinkAndComponent page = new MockPageWithLinkAndComponent();

		page.add(
			new WebComponent(MockPageWithLinkAndComponent.COMPONENT_ID).setOutputMarkupId(true));

		page.add(new AjaxLink<Void>(MockPageWithLinkAndComponent.LINK_ID)
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target)
			{
				// Create an instance of the component
				try
				{
					Constructor<? extends Component> con = componentClass
						.getConstructor(new Class[] { String.class });

					Component comp = con.newInstance(MockPageWithLinkAndComponent.COMPONENT_ID);
					page.replace(comp);
					comp.setOutputMarkupId(true);

					target.add(comp);
				}
				catch (Exception e)
				{
					log.error(e.getMessage(), e);
				}
			}
		});

		tester.startPage(page);

		// System.out.println(tester.getServletResponse().getDocument());
		tester.debugComponentTrees();

		tester.clickLink(MockPageWithLinkAndComponent.LINK_ID);

		String document = tester.getLastResponseAsString();
		String headerContribution = null;

		Pattern pat = Pattern.compile(".*<header-contribution.*?>(.*?)</header-contribution>.*",
			Pattern.DOTALL);
		Matcher mat = pat.matcher(document);
		if (mat.matches())
		{
			headerContribution = mat.group(1);
		}

		// If the filename is empty we use it to say that the headerContribution
		// should be empty.
		// This means that it doesn't exist at all
		if (expectedFile == null)
		{
			assertNull(headerContribution, "There was a header contribution on the response " +
				"(though we didn't expect one): <" + headerContribution + ">");
		}
		else if (headerContribution == null)
		{
			fail("Failed to find header contribution: \n" + document);
		}
		else
		{
			DiffUtil.validatePage(headerContribution, getClass(), expectedFile, true);
		}
	}

	/**
	 * WICKET-2328
	 */
	@Test
	void renderMyPage()
	{
		// start and render the test page
		tester.startPage(HomePage2.class);

		// assert rendered page class
		tester.assertRenderedPage(HomePage2.class);

		// assert rendered label component
		tester.assertLabel("msg", "onBeforeRender called");

		// click the ajax link; this should have no effect
		tester.clickLink("link");

		// assert rendered label component again to make sure
		// THIS FAILS! even though the same sequence of clicks
		// done in a browser does not cause the label to change
		tester.assertLabel("msg", "onBeforeRender called");
	}

	/**
	 * WICKET-6568
	 */
	@Test
	void lastFocusedEncoding()
	{
		FocusPage page = new FocusPage();

		tester.startPage(page);

		// wicket-ajax-jquery encodes non ASCII id
		String encoded = UrlEncoder.QUERY_INSTANCE.encode("€uro", StandardCharsets.UTF_8);
		tester.getRequest().setHeader("Wicket-FocusedElementId", encoded);

		tester.executeAjaxEvent("link", "click");

		assertEquals("€uro", page.lastFocusedElementId);
	}

	/**
	 * WICKET-2543
	 */
	@Test
	void varargsAddComponent()
	{
		tester.startPage(VarargsAddComponentPage.class);

		for (int i = 0; i < VarargsAddComponentPage.NUMBER_OF_LABELS; i++)
		{
			final String labelMarkupId = "label" + i;
			final String expectedContent = String.format(VarargsAddComponentPage.INITIAL_CONTENT,
				i);
			tester.assertLabel(labelMarkupId, expectedContent);
		}

		tester.clickLink("link");

		for (int i = 0; i < VarargsAddComponentPage.NUMBER_OF_LABELS; i++)
		{
			final String labelMarkupId = "label" + i;
			final String expectedContent = String.format(VarargsAddComponentPage.INITIAL_CONTENT,
				i) + VarargsAddComponentPage.AJAX_APPENDED_SUFFIX;
			tester.assertLabel(labelMarkupId, expectedContent);
		}
	}

	@Test
	public void addDuringRender()
	{
		tester.startPage(AddDuringRenderPage.class);

		assertThrows(IllegalStateException.class, () -> {
			tester.clickLink("link");
	    });
	}

	/**
	 * Testing the default event raised whenever Wicket begins to create an AJAX response
	 */
	@Test
	void defaultEventRaisedOnAjaxResponse()
	{
		tester.startPage(TestEventPage.class);
		tester.clickLink(MockPageWithLinkAndComponent.LINK_ID, true);
		TestEventPage page = (TestEventPage)tester.getLastRenderedPage();
		assertTrue(page.defaultEventRaised);
	}

	/**
	 * WICKET-3263<br>
	 * WICKET-6902
	 */
	@Test
	void globalAjaxRequestTargetListeners()
	{
		JavaScriptPrependerAppender listener = new JavaScriptPrependerAppender();
		
		tester.getApplication().getAjaxRequestTargetListeners().add(listener);
		tester.getApplication().getRequestCycleListeners().add(listener);

		tester.startPage(TestEventPage.class);
		tester.clickLink(MockPageWithLinkAndComponent.LINK_ID, true);

		tester.assertContains("BEFORE_RESPOND_PREPEND");
		tester.assertContains("BEFORE_RESPOND_APPEND");
		tester.assertContains("AFTER_RESPOND_PREPEND");
		tester.assertContains("AFTER_RESPOND_APPEND");
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-3921
	 */
	@Test
	void ajaxRedirectSetsNoCachingHeaders()
	{
		tester.startPage(new Wicket3921());

		tester.clickLink("updatePage");
		assertEquals(Instants.toRFC7231Format(Instant.EPOCH),
			tester.getLastResponse().getHeader("Expires"));
		assertEquals("no-cache", tester.getLastResponse().getHeader("Pragma"));
		assertEquals("no-cache, no-store", tester.getLastResponse().getHeader("Cache-Control"));


		tester.clickLink("updateComponent");
		assertEquals(Instants.toRFC7231Format(Instant.EPOCH),
			tester.getLastResponse().getHeader("Expires"));
		assertEquals("no-cache", tester.getLastResponse().getHeader("Pragma"));
		assertEquals("no-cache, no-store", tester.getLastResponse().getHeader("Cache-Control"));
	}

	private static class Wicket7074Application extends MockApplication
	{
		private RequestCycleSettings requestCycleSettings;

		@Override
		public Class<? extends Page> getHomePage()
		{
			return Wicket7074.class;
		}

		public final RequestCycleSettings getRequestCycleSettings()
		{
			checkSettingsAvailable();
			if (requestCycleSettings == null)
			{
				requestCycleSettings = new RequestCycleSettings() {
					@Override
					public RequestCycleSettings addResponseFilter(IResponseFilter responseFilter) {
						// do nothing
						return this;
					}
				};
			}
			return requestCycleSettings;
		}
	}

	private static class WeirdException extends IllegalStateException {

	}

	private static class Wicket7074 extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		private boolean generateError = false;

		private final Component label;
		/**
		 * Construct.
		 */
		private Wicket7074()
		{
			setOutputMarkupId(true);

			add(new AjaxLink<Void>("action")
			{
				private static final long serialVersionUID = 1L;

				@Override
				public void onClick(AjaxRequestTarget target)
				{
					// we simulate producing an exception in rendering phase
					generateError = true;
					target.add(label);
				}
			});

			add(label = new Label("label", "error")
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected void onBeforeRender()
				{
					super.onBeforeRender();
					// we simulate producing an exception in rendering phase
					if (generateError)
					{
						throw new WeirdException();
					}
				}
			}.setOutputMarkupId(true));
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
													   Class<?> containerClass)
		{
			return new StringResourceStream(
					"<html><body><a wicket:id='action'>link1</a><br/><br/><br/><span wicket:id='label'>Link2</span></body></html>");
		}
	}

	@Test
	void ajaxResponseIsEmptyIfExceptionIsProducedDuringRenderingAndNoFiltersAreSet() {
		Wicket7074Application application = new Wicket7074Application();
		WicketTester tester = newWicketTester(application);
		// the page renders normally the first time
		tester.startPage(new Wicket7074());
		// click on action to produce render phase error
		assertThrows(WeirdException.class, () -> {
			tester.clickLink("action");
		});
		// AJAX response is written now into a buffer and to the response => response should empty
		assertTrue(tester.getLastResponseAsString().isEmpty());
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-6808
	 */
	@Test
	void addCurrentPage()
	{
		final MockPageWithLink page1 = new MockPageWithLink();
		page1.add(new AjaxLink<Void>(MockPageWithLink.LINK_ID) {

			@Override
			public void onClick(final AjaxRequestTarget target) {
				target.add(page1);
			}
		});

		assertEquals(0, page1.getRenderCount());

		tester.startPage(page1);

		assertEquals(1, page1.getRenderCount());

		tester.clickLink(MockPageWithLink.LINK_ID);

		assertEquals(2, page1.getRenderCount());
	}

	@Test
	void addAnotherPage()
	{
		final MockPageWithLink currentPage = new MockPageWithLink();
		final MockPageWithLink anotherPage = new MockPageWithLink();
		currentPage.add(new AjaxLink<Void>(MockPageWithLink.LINK_ID) {

			@Override
			public void onClick(final AjaxRequestTarget target) {
				target.add(anotherPage);
			}
		});

		tester.startPage(currentPage);

		final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
			tester.clickLink(MockPageWithLink.LINK_ID);
		});

		assertEquals(exception.getMessage(), "Cannot add another page");
	}

	/**
	 * Test page for {@linkplain AjaxRequestHandlerTest#ajaxRedirectSetsNoCachingHeaders()}
	 */
	private static class Wicket3921 extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 */
		private Wicket3921()
		{
			setOutputMarkupId(true);

			add(new AjaxLink<Void>("updatePage")
			{
				private static final long serialVersionUID = 1L;

				@Override
				public void onClick(AjaxRequestTarget target)
				{
					// adding the page to the target will produce a wicket ajax redirect
					// without any cache headers
					target.add(getPage());
				}
			});

			add(new AjaxLink<Void>("updateComponent")
			{
				private static final long serialVersionUID = 1L;

				@Override
				public void onClick(AjaxRequestTarget target)
				{
					// this produces an ajax response with cache headers set properly
					target.add(this);
				}
			}.setOutputMarkupId(true));

		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream(
				"<html><body><a wicket:id='updatePage'>link1</a><br/><br/><br/><a wicket:id='updateComponent'>Link2</a></body></html>");
		}
	}

	/**
	 * Listener to AjaxRequestTarget and RequestCycle to test prepending and appending of
	 * JavaScript.  
	 */
	private static class JavaScriptPrependerAppender implements AjaxRequestTarget.IListener, IRequestCycleListener
	{
		@Override
		public void onBeforeRespond(Map<String, Component> map, AjaxRequestTarget target)
		{
			target.prependJavaScript("BEFORE_RESPOND_PREPEND");
			target.appendJavaScript("BEFORE_RESPOND_APPEND");
		}

		@Override
		public void onAfterRespond(Map<String, Component> map, AjaxRequestTarget target)
		{
			target.prependJavaScript("AFTER_RESPOND_PREPEND");
			target.appendJavaScript("AFTER_RESPOND_APPEND");
		}
		
		@Override
		public void onRequestHandlerExecuted(RequestCycle cycle, IRequestHandler handler) {
			if (handler instanceof AjaxRequestHandler) {
				try {
					((AjaxRequestHandler) handler).appendJavaScript("FAIL");
					
					fail("appendJavaScript should not be allowed at this state");
				} catch (IllegalStateException javascriptFrozen) {
				}

				try {
					((AjaxRequestHandler) handler).prependJavaScript("FAIL");
					
					fail("prependJavaScript should not be allowed at this state");
				} catch (IllegalStateException javascriptFrozen) {
				}
			}
		}
	}

	/**
	 */
	public static class TestEventPage extends MockPageWithLinkAndComponent
	{
		private static final long serialVersionUID = 1L;

		boolean defaultEventRaised = false;

		/**
		 */
		public TestEventPage()
		{
			add(new AjaxLink<Void>(LINK_ID)
			{
				private static final long serialVersionUID = 1L;

				@Override
				public void onClick(AjaxRequestTarget target)
				{
				}
			});
			add(new WebComponent(COMPONENT_ID)
			{
				private static final long serialVersionUID = 1L;

				@Override
				public void onEvent(IEvent<?> event)
				{
					if (event.getPayload() instanceof AjaxRequestTarget)
					{
						defaultEventRaised = true;
					}
				}
			});
		}
	}
}
