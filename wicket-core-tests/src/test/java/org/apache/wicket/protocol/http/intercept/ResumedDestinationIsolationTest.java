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
package org.apache.wicket.protocol.http.intercept;

import static jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static org.apache.wicket.protocol.http.FetchMetadataResourceIsolationPolicy.CROSS_SITE;
import static org.apache.wicket.protocol.http.FetchMetadataResourceIsolationPolicy.DEST_DOCUMENT;
import static org.apache.wicket.protocol.http.FetchMetadataResourceIsolationPolicy.DEST_EMBED;
import static org.apache.wicket.protocol.http.FetchMetadataResourceIsolationPolicy.MODE_NAVIGATE;
import static org.apache.wicket.protocol.http.FetchMetadataResourceIsolationPolicy.NONE;
import static org.apache.wicket.protocol.http.FetchMetadataResourceIsolationPolicy.SAME_ORIGIN;
import static org.apache.wicket.protocol.http.FetchMetadataResourceIsolationPolicy.SEC_FETCH_DEST_HEADER;
import static org.apache.wicket.protocol.http.FetchMetadataResourceIsolationPolicy.SEC_FETCH_MODE_HEADER;
import static org.apache.wicket.protocol.http.FetchMetadataResourceIsolationPolicy.SEC_FETCH_SITE_HEADER;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.wicket.Page;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.Session;
import org.apache.wicket.authorization.IAuthorizationStrategy;
import org.apache.wicket.core.request.handler.IPageRequestHandler;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.protocol.http.IResourceIsolationPolicy.ResourceIsolationOutcome;
import org.apache.wicket.protocol.http.OriginResourceIsolationPolicy;
import org.apache.wicket.protocol.http.ResourceIsolationRequestCycleListener;
import org.apache.wicket.protocol.http.ResourceIsolationRequestCycleListener.CsrfAction;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.component.IRequestableComponent;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * A request that resumes a destination remembered by
 * {@link RestartResponseAtInterceptPageException} carries the origin that resumed it - this
 * application's own, because signing in redirects within it - and not the origin that chose the
 * URL. {@link ResourceIsolationRequestCycleListener} therefore judges such a request on what was
 * recorded of the request that was interrupted.
 */
class ResumedDestinationIsolationTest extends WicketTestCase
{
	private static final Set<Class<? extends Page>> PROTECTED = new HashSet<>(
		Arrays.asList(SecureLinkPage.class, SecureFormPage.class, SecureRenderPage.class));

	private ResourceIsolationRequestCycleListener listener;

	@Override
	protected WebApplication newApplication()
	{
		return new MockApplication()
		{
			@Override
			public Class<? extends Page> getHomePage()
			{
				return HomePage.class;
			}

			@Override
			public Session newSession(Request request, Response response)
			{
				return new InterceptedSession(request);
			}

			@Override
			protected void init()
			{
				super.init();

				mountPage("secure", SecureLinkPage.class);
				mountPage("secureform", SecureFormPage.class);
				mountPage("secureRender", SecureRenderPage.class);
				mountPage("login", LoginPage.class);

				// the pattern IAuthorizationStrategy documents, and the one both
				// AuthenticatedWebApplication and SimplePageAuthorizationStrategy implement
				getSecuritySettings().setAuthorizationStrategy(
					new IAuthorizationStrategy.AllowAllAuthorizationStrategy()
					{
						@Override
						public <T extends IRequestableComponent> boolean isInstantiationAuthorized(
							Class<T> componentClass)
						{
							if (PROTECTED.contains(componentClass) &&
								((InterceptedSession)Session.get()).isSignedIn() == false)
							{
								throw new RestartResponseAtInterceptPageException(LoginPage.class);
							}
							return true;
						}
					});
			}
		};
	}

	@BeforeEach
	void before()
	{
		SecureLinkPage.invoked = false;
		SecureFormPage.submitted = null;
		withListener(new ResourceIsolationRequestCycleListener());
	}

	/**
	 * Another origin navigates a signed out visitor to a listener URL. The listener is not invoked -
	 * the page is refused and the login page renders instead - but the URL is remembered, and after
	 * the visitor signs in the resume must not run what that origin chose.
	 */
	@Test
	void aListenerChosenByAnotherOriginIsNotInvokedWhenItsDestinationIsResumed()
	{
		navigate(CROSS_SITE, "secure?-1.-link");
		tester.assertRenderedPage(LoginPage.class);

		resume(signIn());

		assertRefused();
		assertFalse(SecureLinkPage.invoked, "the listener another origin chose was invoked");
	}

	/**
	 * The same flow, started by the visitor themselves, resumes as it always has.
	 */
	@Test
	void aListenerTheVisitorChoseIsInvokedWhenItsDestinationIsResumed()
	{
		navigate(SAME_ORIGIN, "secure?-1.-link");
		tester.assertRenderedPage(LoginPage.class);

		resume(signIn());

		tester.assertRenderedPage(SecureLinkPage.class);
		assertTrue(SecureLinkPage.invoked);
	}

	/**
	 * A URL the visitor typed or bookmarked reaches the listener too.
	 */
	@Test
	void aListenerOnABrowserInitiatedUrlIsInvokedWhenItsDestinationIsResumed()
	{
		navigate(NONE, "secure?-1.-link");
		tester.assertRenderedPage(LoginPage.class);

		resume(signIn());

		tester.assertRenderedPage(SecureLinkPage.class);
		assertTrue(SecureLinkPage.invoked);
	}

	/**
	 * The whole URL is remembered, its query string included, so a resumed form submission runs with
	 * the field values the interrupted request carried. A plain top-level navigation is enough to
	 * choose them; no form post is involved.
	 */
	@Test
	void aFormChosenByAnotherOriginIsNotSubmittedWhenItsDestinationIsResumed()
	{
		navigate(CROSS_SITE, "secureform?-1.-form&amount=CHOSEN-ELSEWHERE");
		tester.assertRenderedPage(LoginPage.class);

		resume(signIn());

		assertRefused();
		assertNull(SecureFormPage.submitted, "the form another origin chose was submitted");
	}

	/**
	 * The counterpart, which is what makes the previous test worth having: the same resume, started
	 * by the visitor themselves, is not refused.
	 * <p>
	 * The form is not submitted either, but for a reason of its own: the url carries no page
	 * identifier, and a listener on a stateful page is not invoked on an instance created for the
	 * request that carries it, so the page is rendered instead.
	 */
	@Test
	void aFormTheVisitorChoseIsNotRefusedWhenItsDestinationIsResumed()
	{
		navigate(SAME_ORIGIN, "secureform?-1.-form&amount=42");
		tester.assertRenderedPage(LoginPage.class);

		resume(signIn());

		tester.assertRenderedPage(SecureFormPage.class);
		assertNull(SecureFormPage.submitted);
	}

	/**
	 * Nothing is known about where the interrupted request came from, so the resume is settled by
	 * {@link ResourceIsolationRequestCycleListener#setUnknownOutcomeAction(CsrfAction)} - which is
	 * how such a client is already treated when it invokes a listener directly.
	 */
	@Test
	void aResumeIsRefusedWhenNothingWasKnownAboutTheOriginThatChoseIt()
	{
		navigate(null, "secure?-1.-link");
		tester.assertRenderedPage(LoginPage.class);

		resume(signIn());

		assertRefused();
		assertFalse(SecureLinkPage.invoked);
	}

	/**
	 * @see #aResumeIsRefusedWhenNothingWasKnownAboutTheOriginThatChoseIt()
	 */
	@Test
	void aResumeIsAllowedWhenAnUnknownOriginIsConfiguredToBeAllowed()
	{
		listener.setUnknownOutcomeAction(CsrfAction.ALLOW);

		navigate(null, "secure?-1.-link");
		tester.assertRenderedPage(LoginPage.class);

		resume(signIn());

		tester.assertRenderedPage(SecureLinkPage.class);
		assertTrue(SecureLinkPage.invoked);
	}

	/**
	 * A page may be rendered from anywhere, so a resumed render stays allowed even where the
	 * listener is extended to police renders. The recorded mode, destination and method are what
	 * let the policy see that the interrupted request was a top level navigation.
	 */
	@Test
	void aRenderChosenByAnotherOriginIsStillAllowedWhenItsDestinationIsResumed()
	{
		withRendersChecked();

		navigate(CROSS_SITE, "secureRender");
		tester.assertRenderedPage(LoginPage.class);

		resume(signIn());

		tester.assertRenderedPage(SecureRenderPage.class);
	}

	/**
	 * The policies are asked about the request that chose the url, so they see its headers and its
	 * method, and not those of the request that resumes - which is always a same-origin GET
	 * navigation, whatever the interrupted request was.
	 */
	@Test
	void theResumeIsJudgedOnWhatTheInterruptedRequestCarried()
	{
		List<String> asked = new ArrayList<>();
		withListener(new ResourceIsolationRequestCycleListener((request, page) -> {
			asked.add(request.getHeader(SEC_FETCH_SITE_HEADER) + " " +
				request.getHeader(SEC_FETCH_MODE_HEADER) + " " +
				request.getHeader(SEC_FETCH_DEST_HEADER) + " " + request.getMethod());
			return ResourceIsolationOutcome.ALLOWED;
		}));

		navigate(CROSS_SITE, MODE_NAVIGATE, DEST_EMBED, "POST", "secure?-1.-link");
		tester.assertRenderedPage(LoginPage.class);

		String destination = signIn();
		asked.clear();
		resume(destination);

		assertEquals(asList(SAME_ORIGIN + " " + MODE_NAVIGATE + " " + DEST_DOCUMENT + " GET",
			CROSS_SITE + " " + MODE_NAVIGATE + " " + DEST_EMBED + " POST"), asked,
			"the policies were not asked about the request that chose the url");
	}

	/**
	 * A client that sends no Fetch Metadata is judged by {@link OriginResourceIsolationPolicy}, so
	 * the {@code Origin} of the request that chose the url has to be recorded too. Unknown origins
	 * are allowed here, so that the recorded one is what decides.
	 */
	@Test
	void theOriginHeaderIsRecordedForAClientThatSendsNoFetchMetadata()
	{
		listener.setUnknownOutcomeAction(CsrfAction.ALLOW);

		tester.addRequestHeader(WebRequest.HEADER_ORIGIN, "http://another.example.com");
		navigate(null, "secure?-1.-link");
		tester.assertRenderedPage(LoginPage.class);

		resume(signIn());

		assertRefused();
		assertFalse(SecureLinkPage.invoked);
	}

	/**
	 * What is recorded is the listener's to decide, so a policy reading something else can still be
	 * asked about the request that chose the URL.
	 */
	@Test
	void recordOriginDecidesWhatTheResumeIsJudgedOn()
	{
		withListener(new ResourceIsolationRequestCycleListener(
			(request, page) -> "elsewhere".equals(request.getHeader("X-From"))
				? ResourceIsolationOutcome.DISALLOWED
				: ResourceIsolationOutcome.ALLOWED)
		{
			@Override
			protected Map<String, String> recordOrigin(HttpServletRequest request)
			{
				Map<String, String> origin = super.recordOrigin(request);
				origin.put("X-From", request.getHeader("X-From"));
				return origin;
			}
		});

		tester.addRequestHeader("X-From", "elsewhere");
		navigate(null, "secure?-1.-link");
		tester.assertRenderedPage(LoginPage.class);

		// neither the sign in nor the resume carries X-From
		resume(signIn());

		assertRefused();
		assertFalse(SecureLinkPage.invoked);
	}

	/** Extends the listener to police renders as well, which it does not do by default. */
	private void withRendersChecked()
	{
		withListener(new ResourceIsolationRequestCycleListener()
		{
			@Override
			protected boolean isChecked(IRequestHandler handler)
			{
				return handler instanceof IPageRequestHandler;
			}
		});
	}

	private void withListener(ResourceIsolationRequestCycleListener replacement)
	{
		if (listener != null)
		{
			tester.getApplication().getRequestCycleListeners().remove(listener);
		}
		listener = replacement;
		tester.getApplication().getRequestCycleListeners().add(listener);
	}

	/**
	 * A top level navigation from {@code site}, or from a client that sends no origin headers at
	 * all when {@code site} is {@code null}.
	 */
	private void navigate(String site, String url)
	{
		navigate(site, MODE_NAVIGATE, DEST_DOCUMENT, "GET", url);
	}

	private void navigate(String site, String mode, String dest, String method, String url)
	{
		if (site != null)
		{
			tester.addRequestHeader(SEC_FETCH_SITE_HEADER, site);
			tester.addRequestHeader(SEC_FETCH_MODE_HEADER, mode);
			tester.addRequestHeader(SEC_FETCH_DEST_HEADER, dest);
		}
		tester.getRequest().setMethod(method);
		tester.executeUrl(url);
	}

	/**
	 * Signs in and returns the destination the sign in redirects to, without following it: the
	 * tester builds a fresh request for a redirect and carries no headers over, so the resume has
	 * to be driven the way a browser drives it.
	 */
	private String signIn()
	{
		tester.addRequestHeader(SEC_FETCH_SITE_HEADER, SAME_ORIGIN);
		tester.setFollowRedirects(false);
		tester.newFormTester("form").submit();
		tester.setFollowRedirects(true);

		String destination = tester.getLastResponse().getRedirectLocation();
		assertNotNull(destination, "signing in did not resume a destination");
		return destination;
	}

	/** A browser turns the redirect that follows the sign in into a same origin navigation. */
	private void resume(String destination)
	{
		navigate(SAME_ORIGIN, destination);
	}

	private void assertRefused()
	{
		assertEquals(SC_FORBIDDEN, tester.getLastResponse().getStatus());
		assertEquals(ResourceIsolationRequestCycleListener.ERROR_MESSAGE,
			tester.getLastResponse().getErrorMessage());
	}
}
