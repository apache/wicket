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
package org.apache.wicket.protocol.http;

import static org.hamcrest.CoreMatchers.is;

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.protocol.http.CsrfPreventionRequestCycleListener.CsrfAction;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.component.IRequestablePage;
import org.junit.Before;
import org.junit.Test;

/**
 * Test cases for the CsrfPreventionRequestCycleListener. FirstPage has a link that when clicked
 * should render SecondPage.
 */
public class CsrfPreventionRequestCycleListenerTest extends WicketTestCase
{
	/**
	 * Sets up the test cases. Installs the CSRF listener and renders the FirstPage.
	 */
	@Before
	public void startWithFirstPageRender()
	{
		WebApplication application = tester.getApplication();

		csrfListener = new MockCsrfPreventionRequestCycleListener();
		setErrorCode(errorCode);
		setErrorMessage(errorMessage);
		application.getRequestCycleListeners().add(csrfListener);

		// Rendering a page is allowed, regardless of Origin (this allows external links into your
		// website to function)

		tester.addRequestHeader("Origin", "https://google.com/");

		tester.startPage(FirstPage.class);
		tester.assertRenderedPage(FirstPage.class);
	}

	/** Tests that disabling the CSRF listener doesn't check Origin headers. */
	@Test
	public void disabledListenerDoesntCheckAnything()
	{
		csrfEnabled = false;
		tester.clickLink("link");

		assertOriginsNotChecked();
		tester.assertRenderedPage(SecondPage.class);
	}

	/** Tests that disabling the CSRF listener doesn't check Origin headers. */
	@Test
	public void disabledListenerDoesntCheckMismatchedOrigin()
	{
		csrfEnabled = false;
		tester.addRequestHeader("Origin", "http://malicioussite.com/");
		tester.clickLink("link");
		assertOriginsNotChecked();
		tester.assertRenderedPage(SecondPage.class);
	}

	/** Tests the default setting of allowing a missing Origin. */
	@Test
	public void withoutOriginAllowed()
	{
		tester.clickLink("link");
		assertConflictingOriginsRequestAllowed();
		tester.assertRenderedPage(SecondPage.class);
	}

	/** Tests the alternative action of suppressing a request without Origin header */
	@Test
	public void withoutOriginSuppressed()
	{
		csrfListener.setNoOriginAction(CsrfAction.SUPPRESS);
		tester.clickLink("link");
		tester.assertRenderedPage(FirstPage.class);
		assertConflictingOriginsRequestSuppressed();
	}

	/** Tests the alternative action of aborting a request without Origin header */
	@Test
	public void withoutOriginAborted()
	{
		csrfListener.setNoOriginAction(CsrfAction.ABORT);
		tester.clickLink("link");
		assertConflictingOriginsRequestAborted();
	}

	/** Tests when the Origin header matches the request. */
	@Test
	public void matchingOriginsAllowed()
	{
		csrfListener.setConflictingOriginAction(CsrfAction.ALLOW);
		tester.addRequestHeader("Origin", "http://localhost/");

		tester.clickLink("link");

		assertOriginsMatched();
		tester.assertRenderedPage(SecondPage.class);
	}

	/** Tests when the default action is changed to ALLOW when origins conflict. */
	@Test
	public void conflictingOriginsAllowed()
	{
		csrfListener.setConflictingOriginAction(CsrfAction.ALLOW);
		tester.addRequestHeader("Origin", "http://example.com/");

		tester.clickLink("link");

		assertConflictingOriginsRequestAllowed();
		tester.assertRenderedPage(SecondPage.class);
	}

	/** Tests when the default action is changed to SUPPRESS when origins conflict. */
	@Test
	public void conflictingOriginsSuppressed()
	{
		tester.addRequestHeader("Origin", "http://example.com/");
		csrfListener.setConflictingOriginAction(CsrfAction.SUPPRESS);

		tester.clickLink("link");

		assertConflictingOriginsRequestSuppressed();
		tester.assertRenderedPage(FirstPage.class);
	}

	/** Tests the default action to ABORT when origins conflict. */
	@Test
	public void conflictingOriginsAborted()
	{
		tester.addRequestHeader("Origin", "http://example.com/");

		tester.clickLink("link");

		assertConflictingOriginsRequestAborted();
	}

	/** Tests custom error code/message when the default action is ABORT. */
	@Test
	public void conflictingOriginsAbortedWith401Unauhorized()
	{
		setErrorCode(401);
		setErrorMessage("NOT AUTHORIZED");

		tester.addRequestHeader("Origin", "http://example.com/");
		csrfListener.setNoOriginAction(CsrfAction.ABORT);

		tester.clickLink("link");

		assertConflictingOriginsRequestAborted();
	}

	/** Tests whitelisting for conflicting origins. */
	@Test
	public void conflictingButWhitelistedOriginAllowed()
	{
		csrfListener.setConflictingOriginAction(CsrfAction.ALLOW);
		csrfListener.addAcceptedOrigin("example.com");
		tester.addRequestHeader("Origin", "http://example.com/");

		tester.clickLink("link");

		assertOriginsWhitelisted();
		tester.assertRenderedPage(SecondPage.class);
	}

	/** Tests whitelisting with conflicting subdomain origin. */
	@Test
	public void conflictingButWhitelistedSubdomainOriginAllowed()
	{
		csrfListener.addAcceptedOrigin("example.com");
		csrfListener.setConflictingOriginAction(CsrfAction.ALLOW);

		tester.addRequestHeader("Origin", "http://foo.example.com/");

		tester.clickLink("link");

		tester.assertRenderedPage(SecondPage.class);
		assertOriginsWhitelisted();
	}

	/**
	 * Tests when the listener is disabled for a specific page (by overriding
	 * {@link CsrfPreventionRequestCycleListener#isChecked(IRequestablePage)})
	 */
	@Test
	public void conflictingOriginPageNotCheckedAllowed()
	{
		tester.addRequestHeader("Origin", "http://example.com/");
		csrfListener.setConflictingOriginAction(CsrfAction.ABORT);

		// disable the check for this page
		checkPage = false;

		tester.clickLink("link");

		assertConflictingOriginsRequestAllowed();
		tester.assertRenderedPage(SecondPage.class);
	}

	/** Tests overriding the onSuppressed method for a conflicting origin. */
	@Test
	public void conflictingOriginSuppressedCallsCustomHandler()
	{
		// redirect to third page to ensure we are not suppressed to the first page, nor that the
		// request was not suppressed and the second page was rendered erroneously

		Runnable thirdPageRedirect = new Runnable()
		{
			@Override
			public void run()
			{
				throw new RestartResponseException(new ThirdPage());
			}
		};
		setSuppressHandler(thirdPageRedirect);
		csrfListener.setConflictingOriginAction(CsrfAction.SUPPRESS);

		tester.addRequestHeader("Origin", "http://example.com/");

		tester.clickLink("link");

		assertConflictingOriginsRequestSuppressed();
		tester.assertRenderedPage(ThirdPage.class);
	}

	/** Tests overriding the onAllowed method for a conflicting origin. */
	@Test
	public void conflictingOriginAllowedCallsCustomHandler()
	{
		// redirect to third page to ensure we are not suppressed to the first page, nor that the
		// request was not allowed and the second page was rendered erroneously

		Runnable thirdPageRedirect = new Runnable()
		{
			@Override
			public void run()
			{
				throw new RestartResponseException(new ThirdPage());
			}
		};
		setAllowHandler(thirdPageRedirect);
		csrfListener.setConflictingOriginAction(CsrfAction.ALLOW);

		tester.addRequestHeader("Origin", "http://example.com/");

		tester.clickLink("link");

		assertConflictingOriginsRequestAllowed();
		tester.assertRenderedPage(ThirdPage.class);
	}

	/** Tests overriding the onAborted method for a conflicting origin. */
	@Test
	public void conflictingOriginAbortedCallsCustomHandler()
	{
		// redirect to third page to ensure we are not suppressed to the first page, nor that the
		// request was not aborted and the second page was rendered erroneously

		Runnable thirdPageRedirect = new Runnable()
		{
			@Override
			public void run()
			{
				throw new RestartResponseException(new ThirdPage());
			}
		};
		setAbortHandler(thirdPageRedirect);

		tester.addRequestHeader("Origin", "http://example.com/");
		csrfListener.setConflictingOriginAction(CsrfAction.ABORT);

		tester.clickLink("link");

		// have to check manually, as the assert checks the error code (which is not set due to our
		// custom handler)

		if (!aborted)
			throw new AssertionError("Request was not aborted");

		tester.assertRenderedPage(ThirdPage.class);
	}

	/** Tests whether a different port, but same scheme and hostname is considered a conflict. */
	@Test
	public void differentPortOriginAborted()
	{
		tester.addRequestHeader("Origin", "http://localhost:8080");
		csrfListener.setConflictingOriginAction(CsrfAction.ABORT);

		tester.clickLink("link");

		assertConflictingOriginsRequestAborted();
	}

	/** Tests whether a different scheme, but same port and hostname is considered a conflict. */
	@Test
	public void differentSchemeOriginAborted()
	{
		tester.addRequestHeader("Origin", "https://localhost");
		csrfListener.setConflictingOriginAction(CsrfAction.ABORT);

		tester.clickLink("link");

		assertConflictingOriginsRequestAborted();
	}

	/** Tests whether only the hostname is considered when matching the Origin header. */
	@Test
	public void longerOriginAllowed()
	{
		tester.addRequestHeader("Origin", "http://localhost/supercalifragilisticexpialidocious");
		csrfListener.setConflictingOriginAction(CsrfAction.ABORT);

		tester.clickLink("link");

		assertOriginsMatched();
		tester.assertRenderedPage(SecondPage.class);
	}

	/** Tests whether AJAX Links are checked through the CSRF listener */
	@Test
	public void simulatedCsrfAttackThroughAjaxIsPrevented()
	{
		csrfListener.setConflictingOriginAction(CsrfAction.ABORT);

		// first render a page in the user's session
		tester.addRequestHeader("Origin", "http://localhost");
		tester.startPage(ThirdPage.class);

		assertOriginsNotChecked();
		tester.assertRenderedPage(ThirdPage.class);

		// then click on a link from another external page
		tester.addRequestHeader("Origin", "http://attacker.com/");
		tester.clickLink("link", true);

		assertConflictingOriginsRequestAborted();
	}

	/** Tests whether AJAX Links are checked through the CSRF listener */
	@Test
	public void simulatedCsrfAttackIsSuppressed()
	{
		csrfListener.setConflictingOriginAction(CsrfAction.SUPPRESS);

		// first render a page in the user's session
		tester.addRequestHeader("Origin", "http://localhost");
		tester.startPage(ThirdPage.class);

		assertOriginsNotChecked();
		tester.assertRenderedPage(ThirdPage.class);

		// then click on a link from another external page
		tester.addRequestHeader("Origin", "http://attacker.com/");
		tester.clickLink("link", true);

		assertConflictingOriginsRequestSuppressed();
		tester.assertRenderedPage(ThirdPage.class);
	}

	/** Tests whether form submits are checked through the CSRF listener */
	@Test
	public void simulatedCsrfAttackOnFormIsSuppressed()
	{
		csrfListener.setConflictingOriginAction(CsrfAction.SUPPRESS);

		// first render a page in the user's session
		tester.addRequestHeader("Origin", "http://localhost");
		tester.startPage(ThirdPage.class);

		assertOriginsNotChecked();
		tester.assertRenderedPage(ThirdPage.class);

		// then click on a link from another external page
		tester.addRequestHeader("Origin", "http://attacker.com/");
		tester.submitForm("form");

		assertConflictingOriginsRequestSuppressed();
		tester.assertRenderedPage(ThirdPage.class);
	}

	/*
	 * Infrastructure code for these test cases starts here.
	 */

	/** The listener under test */
	private CsrfPreventionRequestCycleListener csrfListener;

	/** Flag for enabling/disabling the CSRF listener */
	private boolean csrfEnabled = true;

	/** Flag for enabling/disabling the page check of the CSRF listener */
	private boolean checkPage = true;

	/** Value for reporting the error code when the request was aborted */
	private int errorCode = 400;

	/** Value for reporting the error message when the request was aborted */
	private String errorMessage = "BAD REQUEST";

	/** Checks for asserting the functionality of the CSRF listener */
	private boolean matched, whitelisted, aborted, allowed, suppressed;

	/**
	 * Manner to override the default check whether the current request handler should be checked
	 * for CSRF attacks.
	 */
	private Predicate<IRequestHandler> customRequestHandlerCheck;

	/**
	 * Handlers for specific tests (ensures that the listener calls the right handler in the right
	 * circumstance.
	 */
	private Runnable abortHandler, allowHandler, suppressHandler, matchedHandler, whitelistHandler;

	private void setErrorCode(int errorCode)
	{
		this.errorCode = errorCode;
		csrfListener.setErrorCode(errorCode);
	}

	private void setCustomRequestHandlerCheck(Predicate<IRequestHandler> check)
	{
		this.customRequestHandlerCheck = check;
	}

	private void setErrorMessage(String errorMessage)
	{
		this.errorMessage = errorMessage;
		csrfListener.setErrorMessage(errorMessage);
	}

	private void setAbortHandler(Runnable abortHandler)
	{
		this.abortHandler = abortHandler;
	}

	private void setAllowHandler(Runnable allowHandler)
	{
		this.allowHandler = allowHandler;
	}

	private void setSuppressHandler(Runnable suppressHandler)
	{
		this.suppressHandler = suppressHandler;
	}

	private void setWhitelistHandler(Runnable whitelistHandler)
	{
		this.whitelistHandler = whitelistHandler;
	}

	private void setMatchedHandler(Runnable matchedHandler)
	{
		this.matchedHandler = matchedHandler;
	}

	/**
	 * Asserts that the origins were checked, and found matching.
	 */
	private void assertOriginsMatched()
	{
		if (!matched)
			throw new AssertionError("Origins were not matched");
	}

	/**
	 * Asserts that the origins were not checked, because the origin was on the whitelist.
	 */
	private void assertOriginsWhitelisted()
	{
		if (!whitelisted)
			throw new AssertionError("Origins were not whitelisted");
	}

	/**
	 * Asserts that the origins were checked, found conflicting, had an action "ABORTED" and returns
	 * a HTTP error.
	 */
	private void assertConflictingOriginsRequestAborted()
	{
		if (!aborted)
			throw new AssertionError("Request was not aborted");

		assertThat("Response error code", tester.getLastResponse().getStatus(), is(errorCode));
		assertThat("Response error message", tester.getLastResponse().getErrorMessage(),
			is(errorMessage));
	}

	/**
	 * Asserts that the origins were checked, found conflicting and had an action "SUPPRESS".
	 */
	private void assertConflictingOriginsRequestSuppressed()
	{
		if (!suppressed)
			throw new AssertionError("Request was not suppressed");
	}

	/**
	 * Asserts that the origins were checked, found conflicting and had an action "ALLOWED".
	 */
	private void assertConflictingOriginsRequestAllowed()
	{
		if (!allowed)
			throw new AssertionError("Request was not allowed");
	}

	/**
	 * Asserts that the origins were checked and found non-conflicting.
	 */
	private void assertOriginsCheckedButNotConflicting()
	{
		if (aborted)
			throw new AssertionError("Origin was checked and aborted");
		if (suppressed)
			throw new AssertionError("Origin was checked and suppressed");
		if (allowed)
			throw new AssertionError("Origin was checked and allowed");
		if (whitelisted)
			throw new AssertionError("Origin was whitelisted");
		if (!matched)
			throw new AssertionError("Origin was not checked");
	}

	/**
	 * Asserts that no check was performed at all.
	 */
	private void assertOriginsNotChecked()
	{
		if (aborted)
			throw new AssertionError("Request was checked and aborted");
		if (suppressed)
			throw new AssertionError("Request was checked and suppressed");
		if (allowed)
			throw new AssertionError("Request was checked and allowed");
		if (whitelisted)
			throw new AssertionError("Origin was whitelisted");
		if (matched)
			throw new AssertionError("Origin was checked and matched");
	}

	private final class MockCsrfPreventionRequestCycleListener extends
		CsrfPreventionRequestCycleListener
	{
		@Override
		protected boolean isEnabled()
		{
			return csrfEnabled;
		}

		@Override
		protected boolean isChecked(IRequestHandler handler)
		{
			if (customRequestHandlerCheck != null)
				return customRequestHandlerCheck.apply(handler);

			return super.isChecked(handler);
		}

		@Override
		protected boolean isChecked(IRequestablePage targetedPage)
		{
			return checkPage;
		}

		@Override
		protected void onAborted(HttpServletRequest containerRequest, String origin,
			IRequestablePage page)
		{
			aborted = true;
			if (abortHandler != null)
				abortHandler.run();
		}

		@Override
		protected void onAllowed(HttpServletRequest containerRequest, String origin,
			IRequestablePage page)
		{
			allowed = true;
			if (allowHandler != null)
				allowHandler.run();
		}

		@Override
		protected void onSuppressed(HttpServletRequest containerRequest, String origin,
			IRequestablePage page)
		{
			suppressed = true;
			if (suppressHandler != null)
				suppressHandler.run();
		}

		@Override
		protected void onMatchingOrigin(HttpServletRequest containerRequest, String origin,
			IRequestablePage page)
		{
			matched = true;
			if (matchedHandler != null)
				matchedHandler.run();
		}

		@Override
		protected void onWhitelisted(HttpServletRequest containerRequest, String origin,
			IRequestablePage page)
		{
			whitelisted = true;
			if (whitelistHandler != null)
				whitelistHandler.run();
		}
	}

	// Remove when migration to Java 8 is completed
	private interface Predicate<T>
	{
		boolean apply(T t);
	}
}
