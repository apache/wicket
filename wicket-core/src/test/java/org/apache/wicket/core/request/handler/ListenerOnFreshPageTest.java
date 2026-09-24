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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.authorization.Action;
import org.apache.wicket.authorization.IAuthorizationStrategy;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.StatelessLink;
import org.apache.wicket.request.Url;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Before;
import org.junit.Test;

/**
 * An {@link org.apache.wicket.IRequestListener} must not be invoked on a page instance that Wicket
 * created for the very request that carries the listener URL.
 * <p>
 * Such an instance has never been through {@link Component#configure()}, so the
 * {@link Component#RENDER} decision of the {@link IAuthorizationStrategy} has not been made and
 * {@link Component#onConfigure()} has not run - while
 * {@link org.apache.wicket.core.request.handler.ListenerRequestHandler} gates the invocation on
 * {@link Component#canCallListener()}, which reads exactly those two.
 * <p>
 * A listener URL reaches such an instance in two ways, and this test covers both: the page
 * identifier is simply left out of a URL that would otherwise carry it, or the page is one Wicket
 * considers stateless, for which Wicket itself writes listener URLs without a page identifier.
 */
public class ListenerOnFreshPageTest extends WicketTestCase
{
	@Before
	public void resetInvocations()
	{
		// a fresh page instance is created per request, so the pages record their invocations
		// in static fields
		StatefulTestPage.restrictedClicked = false;
		StatefulTestPage.ordinaryClicked = false;
		StatefulTestPage.hiddenClicked = false;
		StatefulTestPage.disabledClicked = false;
		StatefulTestPage.nestedClicked = false;
		StatefulTestPage.ajaxClicked = false;
		StatelessTestPage.restrictedClicked = false;
		StatelessTestPage.ordinaryClicked = false;
	}

	/**
	 * Denies {@code action} for the component with the given id, and allows everything else.
	 */
	private void deny(final Action action, final String componentId)
	{
		tester.getApplication()
			.getSecuritySettings()
			.setAuthorizationStrategy(new IAuthorizationStrategy.AllowAllAuthorizationStrategy()
			{
				@Override
				public boolean isActionAuthorized(Component component, Action a)
				{
					return !(action.equals(a) && componentId.equals(component.getId()));
				}
			});
	}

	/**
	 * The listener URL of a component, with the page identifier left out - the shape Wicket writes
	 * for a stateless page, and the shape a caller can hand-craft for any page.
	 */
	private static String freshPageUrl(Class<? extends Page> pageClass, String componentPath)
	{
		return "wicket/bookmarkable/" + pageClass.getName() + "?-1.-" + componentPath;
	}

	/**
	 * The listener URL of a component on the page instance that was last rendered.
	 */
	private String renderedPageUrl(String componentPath)
	{
		Page page = tester.getLastRenderedPage();
		return "wicket/bookmarkable/" + page.getClass().getName() + "?" + page.getPageId() + "-"
			+ page.getRenderCount() + ".-" + componentPath;
	}

	private void request(String url)
	{
		tester.getRequest().setUrl(Url.parse(url));
		tester.processRequest();
	}

	/**
	 * A stateful page, a link the strategy denies {@link Component#RENDER} for, and a listener URL
	 * with the page identifier left out.
	 */
	@Test
	public void renderDeniedListenerIsNotInvokedOnAFreshPage()
	{
		deny(Component.RENDER, "restricted");
		tester.startPage(StatefulTestPage.class);
		tester.assertRenderedPage(StatefulTestPage.class);

		// the fixture is stateful, so Wicket itself never writes a page-id-less URL for it
		assertFalse(tester.getLastRenderedPage().isPageStateless());
		// and the denied link really was kept out of the markup
		assertFalse(tester.getLastResponseAsString().contains("wicket:id=\"restricted\""));
		assertTrue(tester.getLastResponseAsString().contains("wicket:id=\"ordinary\""));

		try
		{
			request(freshPageUrl(StatefulTestPage.class, "restricted"));
			fail("the listener should have been refused");
		}
		catch (ListenerInvocationNotAllowedException expected)
		{
			// refused, as it should be
		}

		assertFalse("Link#onClick() was invoked although IAuthorizationStrategy denied RENDER for the link",
			StatefulTestPage.restrictedClicked);
	}

	/**
	 * Control: the {@link Component#ENABLE} decision is consulted on every invocation, so it holds
	 * on a fresh page instance as well.
	 */
	@Test
	public void enableDeniedListenerIsNotInvokedOnAFreshPage()
	{
		deny(Component.ENABLE, "restricted");
		tester.startPage(StatefulTestPage.class);

		try
		{
			request(freshPageUrl(StatefulTestPage.class, "restricted"));
			fail("the listener should have been refused");
		}
		catch (ListenerInvocationNotAllowedException expected)
		{
			// refused, as it should be
		}

		assertFalse(StatefulTestPage.restrictedClicked);
	}

	/**
	 * A page that reports itself stateful cannot serve a listener on an instance created for this
	 * request: the caller, not the application, chose the page parameters that instance built its
	 * state from. Wicket re-renders the new instance instead.
	 */
	@Test
	public void listenerIsNotInvokedOnAFreshInstanceOfAStatefulPage()
	{
		tester.startPage(StatefulTestPage.class);
		assertFalse(tester.getLastRenderedPage().isPageStateless());

		request(freshPageUrl(StatefulTestPage.class, "ordinary"));

		assertFalse("Link#onClick() was invoked on a page instance created for this very request",
			StatefulTestPage.ordinaryClicked);
		tester.assertRenderedPage(StatefulTestPage.class);
	}

	/**
	 * Control: the same two requests against the instance that was actually rendered. The ordinary
	 * link works and the denied link is refused, which is the behaviour the page-id-less URL has to
	 * match.
	 */
	@Test
	public void listenersOnTheRenderedPageInstanceAreUnaffected()
	{
		deny(Component.RENDER, "restricted");
		tester.startPage(StatefulTestPage.class);

		try
		{
			request(renderedPageUrl("restricted"));
			fail("the listener should have been refused");
		}
		catch (ListenerInvocationNotAllowedException expected)
		{
			// refused, as it should be
		}
		assertFalse(StatefulTestPage.restrictedClicked);

		tester.startPage(StatefulTestPage.class);
		request(renderedPageUrl("ordinary"));
		assertTrue(StatefulTestPage.ordinaryClicked);
	}

	/**
	 * Visibility an application sets in {@link Component#onConfigure()} is applied on a fresh page
	 * instance too, because onConfigure() is now run before the listener gate.
	 */
	@Test
	public void listenerHiddenInOnConfigureIsNotInvokedOnAFreshPage()
	{
		tester.startPage(StatefulTestPage.class);
		assertFalse(tester.getLastResponseAsString().contains("wicket:id=\"hidden\""));

		try
		{
			request(freshPageUrl(StatefulTestPage.class, "hidden"));
			fail("the listener should have been refused");
		}
		catch (ListenerInvocationNotAllowedException expected)
		{
			// refused, as it should be
		}

		assertFalse("Link#onClick() was invoked although onConfigure() had made the link invisible",
			StatefulTestPage.hiddenClicked);
	}

	/**
	 * The enabled half of {@link Component#canCallListener()} needs the same treatment. Only
	 * {@link Component#isEnableAllowed()} consults the {@link IAuthorizationStrategy} on every
	 * call; {@link Component#isEnabled()} reads a flag that is true by default, so an enabled
	 * flag an application clears in {@link Component#onConfigure()} is no better protected than
	 * visibility is.
	 */
	@Test
	public void listenerDisabledInOnConfigureIsNotInvokedOnAFreshPage()
	{
		tester.startPage(StatefulTestPage.class);
		// unlike an invisible link, a disabled one is rendered, so the caller has its path
		assertTrue(tester.getLastResponseAsString().contains("wicket:id=\"disabled\""));

		try
		{
			request(freshPageUrl(StatefulTestPage.class, "disabled"));
			fail("the listener should have been refused");
		}
		catch (ListenerInvocationNotAllowedException expected)
		{
			// refused, as it should be
		}

		assertFalse("Link#onClick() was invoked although onConfigure() had disabled the link",
			StatefulTestPage.disabledClicked);
	}

	/**
	 * Both halves of {@link Component#canCallListener()} walk the parent chain, and a parent's
	 * {@link Component#onConfigure()} decides what its children inherit, so configuring the
	 * component alone is not enough: here the link itself is never restricted and it is the
	 * container above it that hides itself.
	 */
	@Test
	public void listenerUnderAContainerHiddenInOnConfigureIsNotInvokedOnAFreshPage()
	{
		tester.startPage(StatefulTestPage.class);
		assertFalse(tester.getLastResponseAsString().contains("wicket:id=\"nested\""));

		try
		{
			request(freshPageUrl(StatefulTestPage.class, "container:nested"));
			fail("the listener should have been refused");
		}
		catch (ListenerInvocationNotAllowedException expected)
		{
			// refused, as it should be
		}

		assertFalse("Link#onClick() was invoked although onConfigure() had made its container invisible",
			StatefulTestPage.nestedClicked);
	}

	/**
	 * A behaviour is gated on {@link Component#canCallListener()} of the component it is attached
	 * to, so it is refused for the same reason.
	 */
	@Test
	public void behaviourOnARenderDeniedComponentIsNotInvokedOnAFreshPage()
	{
		deny(Component.RENDER, "ajax");
		tester.startPage(StatefulTestPage.class);
		assertFalse(tester.getLastResponseAsString().contains("wicket:id=\"ajax\""));

		try
		{
			request("wicket/bookmarkable/" + StatefulTestPage.class.getName() + "?-1.0-ajax");
			fail("the listener should have been refused");
		}
		catch (ListenerInvocationNotAllowedException expected)
		{
			// refused, as it should be
		}

		assertFalse("An AJAX behaviour was invoked although IAuthorizationStrategy denied RENDER for its component",
			StatefulTestPage.ajaxClicked);
	}

	/**
	 * On a stateless page Wicket writes the page-id-less listener URL itself, so this route needs
	 * no manipulation at all. The RENDER decision has to be made for it just the same.
	 */
	@Test
	public void renderDeniedListenerIsNotInvokedOnAStatelessPage()
	{
		deny(Component.RENDER, "restricted");
		tester.startPage(StatelessTestPage.class);
		assertTrue(tester.getLastRenderedPage().isPageStateless());

		try
		{
			request(freshPageUrl(StatelessTestPage.class, "restricted"));
			fail("the listener should have been refused");
		}
		catch (ListenerInvocationNotAllowedException expected)
		{
			// refused, as it should be
		}

		assertFalse("StatelessLink#onClick() was invoked although IAuthorizationStrategy denied RENDER for the link",
			StatelessTestPage.restrictedClicked);
	}

	/**
	 * Control, and the regression this fix must not cause: a stateless page serves its listeners
	 * from a page instance created for the request by design, and that has to keep working.
	 */
	@Test
	public void ordinaryListenerIsStillInvokedOnAStatelessPage()
	{
		tester.startPage(StatelessTestPage.class);
		assertTrue(tester.getLastRenderedPage().isPageStateless());
		// the URL Wicket itself wrote carries no page identifier
		assertTrue(tester.getTagByWicketId("ordinary").getAttribute("href"),
			tester.getTagByWicketId("ordinary").getAttribute("href").contains("?-1.-ordinary"));

		request(freshPageUrl(StatelessTestPage.class, "ordinary"));

		assertTrue("StatelessLink#onClick() should be invoked", StatelessTestPage.ordinaryClicked);
	}

	/** A stateful page with one link the application may restrict. */
	public static class StatefulTestPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		static boolean restrictedClicked;
		static boolean ordinaryClicked;
		static boolean hiddenClicked;
		static boolean disabledClicked;
		static boolean nestedClicked;
		static boolean ajaxClicked;

		public StatefulTestPage()
		{
			add(new Link<Void>("restricted")
			{
				private static final long serialVersionUID = 1L;

				@Override
				public void onClick()
				{
					restrictedClicked = true;
				}
			});
			add(new Link<Void>("ordinary")
			{
				private static final long serialVersionUID = 1L;

				@Override
				public void onClick()
				{
					ordinaryClicked = true;
				}
			});
			add(new Link<Void>("hidden")
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected void onConfigure()
				{
					super.onConfigure();
					setVisible(false);
				}

				@Override
				public void onClick()
				{
					hiddenClicked = true;
				}
			});
			add(new Link<Void>("disabled")
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected void onConfigure()
				{
					super.onConfigure();
					setEnabled(false);
				}

				@Override
				public void onClick()
				{
					disabledClicked = true;
				}
			});
			WebMarkupContainer container = new WebMarkupContainer("container")
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected void onConfigure()
				{
					super.onConfigure();
					setVisible(false);
				}
			};
			add(container);
			container.add(new Link<Void>("nested")
			{
				private static final long serialVersionUID = 1L;

				@Override
				public void onClick()
				{
					nestedClicked = true;
				}
			});
			add(new AjaxLink<Void>("ajax")
			{
				private static final long serialVersionUID = 1L;

				@Override
				public void onClick(AjaxRequestTarget target)
				{
					ajaxClicked = true;
				}
			});
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream("<html><body>" //
				+ "<a wicket:id=\"restricted\">restricted</a>" //
				+ "<a wicket:id=\"ordinary\">ordinary</a>" //
				+ "<a wicket:id=\"hidden\">hidden</a>" //
				+ "<a wicket:id=\"disabled\">disabled</a>" //
				+ "<div wicket:id=\"container\"><a wicket:id=\"nested\">nested</a></div>" //
				+ "<a wicket:id=\"ajax\">ajax</a>" //
				+ "</body></html>");
		}
	}

	/** A page Wicket considers stateless, so it writes page-id-less listener URLs for it. */
	public static class StatelessTestPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		static boolean restrictedClicked;
		static boolean ordinaryClicked;

		public StatelessTestPage()
		{
			add(new StatelessLink<Void>("restricted")
			{
				private static final long serialVersionUID = 1L;

				@Override
				public void onClick()
				{
					restrictedClicked = true;
				}
			});
			add(new StatelessLink<Void>("ordinary")
			{
				private static final long serialVersionUID = 1L;

				@Override
				public void onClick()
				{
					ordinaryClicked = true;
				}
			});
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream("<html><body>" //
				+ "<a wicket:id=\"restricted\">restricted</a>" //
				+ "<a wicket:id=\"ordinary\">ordinary</a>" //
				+ "</body></html>");
		}
	}
}
