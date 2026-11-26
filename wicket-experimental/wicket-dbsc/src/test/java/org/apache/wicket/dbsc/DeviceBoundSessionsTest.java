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
package org.apache.wicket.dbsc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.wicket.Page;
import org.apache.wicket.core.request.mapper.CryptoMapper;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.mock.MockHttpServletResponse;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.flow.AbortWithHttpErrorCodeException;
import org.apache.wicket.request.resource.ByteArrayResource;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.cookies.CookieDefaults.SameSite;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;

class DeviceBoundSessionsTest
{
	private static final Pattern REGISTRATION = Pattern
		.compile("\\(ES256 RS256\\);path=\"([^\"]+)\";challenge=\"([^\"]+)\"");

	private static final Pattern CHALLENGE = Pattern.compile("\"([^\"]+)\";id=\"([^\"]+)\"");

	private TestClock clock;

	private DeviceBoundSessions deviceBoundSessions;

	private WicketTester tester;

	@BeforeEach
	void setUp()
	{
		StartPage.AJAX_CLICKS.set(0);
		SocketPage.RECEIVED.clear();
		start(new DeviceBoundSessions());
	}

	private void start(DeviceBoundSessions instance)
	{
		start(instance, application -> {
		});
	}

	private void start(DeviceBoundSessions instance, Consumer<WebApplication> configuration)
	{
		start(instance, configuration, false);
	}

	/**
	 * @param configuration
	 *            runs after the instance was installed
	 * @param atRoot
	 *            whether the application is served at the root, rather than at
	 *            {@code /context/servlet}
	 */
	private void start(DeviceBoundSessions instance, Consumer<WebApplication> configuration,
		boolean atRoot)
	{
		clock = new TestClock(Instant.parse("2026-11-24T10:00:00Z"));
		deviceBoundSessions = instance;
		deviceBoundSessions.setClock(clock);
		tester = new WicketTester(new MockApplication()
		{
			@Override
			protected void init()
			{
				super.init();
				deviceBoundSessions.install(this);
				mountResource("resource", new ResourceReference("resource")
				{
					private static final long serialVersionUID = 1L;

					@Override
					public IResource getResource()
					{
						return new ByteArrayResource("text/plain", new byte[] { 'x' });
					}
				});
				configuration.accept(this);
			}

			@Override
			public Class<? extends Page> getHomePage()
			{
				return StartPage.class;
			}

			@Override
			public WebRequest newWebRequest(HttpServletRequest servletRequest, String filterPath)
			{
				return atRoot ? super.newWebRequest(new AtRoot(servletRequest), "")
					: super.newWebRequest(servletRequest, filterPath);
			}
		});
	}

	@AfterEach
	void tearDown()
	{
		tester.destroy();
	}

	@Test
	void startAddsRegistrationHeaderToTheRedirectAfterTheClick()
	{
		tester.startPage(StartPage.class);
		tester.setFollowRedirects(false);
		tester.clickLink("start");

		MockHttpServletResponse response = tester.getLastResponse();
		assertTrue(response.isRedirect());
		Matcher registration = REGISTRATION.matcher(response.getHeader(
			DeviceBoundSessions.REGISTRATION_HEADER));
		assertTrue(registration.matches());
		assertEquals(tester.getRequest().getContextPath() + tester.getRequest().getServletPath() +
			"/dbsc/register", registration.group(1));
		assertFalse(tester.getSession().isTemporary());
	}

	@Test
	void registrationBindsTheSessionToAnEcKey() throws Exception
	{
		TestBrowser browser = TestBrowser.es256();

		JSONObject instructions = register(browser.registrationProof(start()));

		assertEquals(200, tester.getLastResponse().getStatus());
		assertEquals("no-store", tester.getLastResponse().getHeader("Cache-Control"));
		assertTrue(deviceBoundSessions.isBound(tester.getSession()));
		assertNotNull(instructions.getString("session_identifier"));
		assertEquals(tester.getRequest().getContextPath() + tester.getRequest().getServletPath() +
			"/dbsc/refresh", instructions.getString("refresh_url"));
		JSONObject scope = instructions.getJSONObject("scope");
		assertFalse(scope.getBoolean("include_site"));
		JSONArray rules = scope.getJSONArray("scope_specification");
		assertEquals("exclude", rules.getJSONObject(0).getString("type"));
		assertEquals("/", rules.getJSONObject(0).getString("path"));
		assertEquals("include", rules.getJSONObject(1).getString("type"));
		assertEquals(cookiePath(), rules.getJSONObject(1).getString("path"));
		JSONObject credential = instructions.getJSONArray("credentials").getJSONObject(0);
		assertEquals("cookie", credential.getString("type"));
		assertEquals("wicket-dbsc", credential.getString("name"));
		assertEquals("Path=" + cookiePath() + "; Secure; HttpOnly; SameSite=Lax",
			credential.getString("attributes"));

		Cookie cookie = boundCookie(tester.getLastResponse());
		assertNotNull(cookie);
		assertTrue(cookie.getSecure());
		assertTrue(cookie.isHttpOnly());
		assertEquals("Lax", cookie.getAttribute("SameSite"));
		assertEquals(cookiePath(), cookie.getPath());
		assertEquals(600, cookie.getMaxAge());

		Matcher challenge = CHALLENGE
			.matcher(tester.getLastResponse().getHeader(DeviceBoundSessions.CHALLENGE_HEADER));
		assertTrue(challenge.matches());
		assertEquals(instructions.getString("session_identifier"), challenge.group(2));
	}

	@Test
	void registrationAcceptsAnRsaKeyCarriedInThePayload() throws Exception
	{
		TestBrowser browser = TestBrowser.rs256();

		register(browser.registrationProofWithKeyInPayload(start()));

		assertEquals(200, tester.getLastResponse().getStatus());
		assertTrue(deviceBoundSessions.isBound(tester.getSession()));
	}

	@Test
	void registrationRejectsAnotherChallenge() throws Exception
	{
		start();

		register(TestBrowser.es256().registrationProof("not the challenge"));

		assertEquals(403, tester.getLastResponse().getStatus());
		assertFalse(deviceBoundSessions.isBound(tester.getSession()));
	}

	@Test
	void registrationRejectsAnExpiredChallenge() throws Exception
	{
		String challenge = start();
		clock.advance(deviceBoundSessions.getChallengeLifetime());

		register(TestBrowser.es256().registrationProof(challenge));

		assertEquals(403, tester.getLastResponse().getStatus());
		assertFalse(deviceBoundSessions.isBound(tester.getSession()));
	}

	@Test
	void registrationRejectsAnUnsignedProof()
	{
		register(TestBrowser.unsignedProof(start()));

		assertEquals(400, tester.getLastResponse().getStatus());
		assertFalse(deviceBoundSessions.isBound(tester.getSession()));
	}

	@Test
	void registrationRejectsAProofSignedByAnotherKey() throws Exception
	{
		register(TestBrowser.es256().registrationProofSignedBy(TestBrowser.es256(), start()));

		assertEquals(400, tester.getLastResponse().getStatus());
		assertFalse(deviceBoundSessions.isBound(tester.getSession()));
	}

	@Test
	void registrationRejectsARequestWithoutProof()
	{
		start();

		post(deviceBoundSessions.getRegistrationPath());

		assertEquals(400, tester.getLastResponse().getStatus());
	}

	@Test
	void registrationHappensOnce() throws Exception
	{
		TestBrowser browser = TestBrowser.es256();
		String proof = browser.registrationProof(start());
		register(proof);
		String sessionIdentifier = sessionIdentifier();

		register(proof);

		assertEquals(403, tester.getLastResponse().getStatus());
		assertEquals(sessionIdentifier, sessionIdentifier());
	}

	@Test
	void startDoesNothingOnABoundSession() throws Exception
	{
		register(TestBrowser.es256().registrationProof(start()));
		tester.startPage(StartPage.class);
		tester.setFollowRedirects(false);

		tester.clickLink("start");

		assertNull(tester.getLastResponse().getHeader(DeviceBoundSessions.REGISTRATION_HEADER));
	}

	@Test
	void endpointsTakePostOnly()
	{
		tester.getRequest().setMethod("GET");
		tester.executeUrl(deviceBoundSessions.getRefreshPath());

		assertEquals(405, tester.getLastResponse().getStatus());
		assertEquals("POST", tester.getLastResponse().getHeader("Allow"));
	}

	@Test
	void unboundSessionNeedsNoBoundCookie()
	{
		start();
		tester.startPage(StartPage.class);

		tester.assertRenderedPage(StartPage.class);
	}

	@Test
	void boundSessionEndsOnRequestWithoutBoundCookie() throws Exception
	{
		register(TestBrowser.es256().registrationProof(start()));
		tester.startPage(StartPage.class);
		tester.assertRenderedPage(StartPage.class);

		withoutBoundCookie();
		tester.setFollowRedirects(false);
		tester.startPage(StartPage.class);

		assertSessionEnded();
	}

	@Test
	void endedSessionLeavesTheBrowserOnThePageItAskedFor() throws Exception
	{
		register(TestBrowser.es256().registrationProof(start()));
		tester.startPage(StartPage.class);

		withoutBoundCookie();
		tester.startPage(StartPage.class);

		tester.assertRenderedPage(StartPage.class);
		assertFalse(deviceBoundSessions.isBound(tester.getSession()));
	}

	@Test
	void boundSessionEndsOnUnknownBoundCookie() throws Exception
	{
		register(TestBrowser.es256().registrationProof(start()));
		tester.startPage(StartPage.class);

		withoutBoundCookie();
		tester.getRequest().addCookie(new Cookie(deviceBoundSessions.getCookieName(), "guessed"));
		tester.setFollowRedirects(false);
		tester.startPage(StartPage.class);

		assertSessionEnded();
	}

	@Test
	void newlyBoundSessionWaitsForTheFirstBoundCookieUntilTheGracePeriodEnds() throws Exception
	{
		register(TestBrowser.es256().registrationProof(start()));

		withoutBoundCookie();
		tester.startPage(StartPage.class);
		tester.assertRenderedPage(StartPage.class);

		clock.advance(deviceBoundSessions.getGracePeriod());
		withoutBoundCookie();
		tester.setFollowRedirects(false);
		tester.startPage(StartPage.class);
		assertSessionEnded();
	}

	@Test
	void missingCredentialHandlingCanRejectAndKeepTheSession() throws Exception
	{
		tester.destroy();
		start(new DeviceBoundSessions()
		{
			@Override
			protected void onMissingCredential(RequestCycle cycle, MissingCredential credential)
			{
				throw new AbortWithHttpErrorCodeException(HttpServletResponse.SC_FORBIDDEN);
			}
		});
		register(TestBrowser.es256().registrationProof(start()));
		tester.startPage(StartPage.class);

		withoutBoundCookie();
		tester.startPage(StartPage.class);

		assertEquals(403, tester.getLastResponse().getStatus());
		assertTrue(deviceBoundSessions.isBound(tester.getSession()));
	}

	@Test
	void missingCredentialSaysWhatTheRequestCarried() throws Exception
	{
		tester.destroy();
		AtomicReference<MissingCredential> missing = new AtomicReference<>();
		start(new DeviceBoundSessions()
		{
			@Override
			protected void onMissingCredential(RequestCycle cycle, MissingCredential credential)
			{
				missing.set(credential);
				throw new AbortWithHttpErrorCodeException(HttpServletResponse.SC_FORBIDDEN);
			}
		});
		register(TestBrowser.es256().registrationProof(start()));
		tester.startPage(StartPage.class);

		withoutBoundCookie();
		tester.getRequest().addCookie(new Cookie(deviceBoundSessions.getCookieName(), "stale"));
		tester.getRequest().setHeader(DeviceBoundSessions.FETCH_SITE_HEADER, "cross-site");
		tester.getRequest().setHeader(DeviceBoundSessions.SKIPPED_HEADER,
			"unreachable;session_identifier=\"other\", quota_exceeded;session_identifier=" +
				quoted(sessionIdentifier()));
		tester.startPage(StartPage.class);

		assertTrue(missing.get().isCookiePresent());
		assertTrue(missing.get().isCrossSite());
		assertEquals("quota_exceeded", missing.get().getSkippedRefresh());

		withoutBoundCookie();
		tester.startPage(StartPage.class);

		assertFalse(missing.get().isCookiePresent());
		assertFalse(missing.get().isCrossSite());
		assertNull(missing.get().getSkippedRefresh());
	}

	@Test
	void expiredBoundCookieEndsTheSession() throws Exception
	{
		register(TestBrowser.es256().registrationProof(start()));
		tester.startPage(StartPage.class);
		Cookie cookie = boundCookie(tester.getRequest());

		clock.advance(
			deviceBoundSessions.getCookieLifetime().plus(deviceBoundSessions.getGracePeriod()));
		withoutBoundCookie();
		tester.getRequest().addCookie(cookie);
		tester.setFollowRedirects(false);
		tester.startPage(StartPage.class);

		assertSessionEnded();
	}

	@Test
	void previousBoundCookieWorksUntilItExpires() throws Exception
	{
		TestBrowser browser = TestBrowser.es256();
		register(browser.registrationProof(start()));
		Cookie previous = boundCookie(tester.getLastResponse());
		clock.advance(Duration.ofMinutes(5));
		refresh(sessionIdentifier(), browser.refreshProof(challenge()));
		assertEquals(200, tester.getLastResponse().getStatus());

		withoutBoundCookie();
		tester.getRequest().addCookie(previous);
		tester.startPage(StartPage.class);
		tester.assertRenderedPage(StartPage.class);

		clock.advance(Duration.ofMinutes(5).plus(deviceBoundSessions.getGracePeriod()));
		withoutBoundCookie();
		tester.getRequest().addCookie(previous);
		tester.setFollowRedirects(false);
		tester.startPage(StartPage.class);
		assertSessionEnded();
	}

	@Test
	void ajaxRequestNeedsTheBoundCookie() throws Exception
	{
		register(TestBrowser.es256().registrationProof(start()));
		tester.startPage(StartPage.class);

		tester.clickLink("ajax", true);
		assertEquals(1, StartPage.AJAX_CLICKS.get());

		withoutBoundCookie();
		tester.setFollowRedirects(false);
		tester.clickLink("ajax", true);

		assertEquals(1, StartPage.AJAX_CLICKS.get());
		assertSessionEnded();
	}

	@Test
	void resourceRequestNeedsTheBoundCookie() throws Exception
	{
		register(TestBrowser.es256().registrationProof(start()));
		tester.startPage(StartPage.class);

		tester.executeUrl("resource");
		assertEquals("x", tester.getLastResponseAsString());

		withoutBoundCookie();
		tester.setFollowRedirects(false);
		tester.executeUrl("resource");

		assertSessionEnded();
	}

	@Test
	void refreshWithoutProofGetsAChallenge() throws Exception
	{
		register(TestBrowser.es256().registrationProof(start()));
		String sessionIdentifier = sessionIdentifier();

		refresh(sessionIdentifier, null);

		assertEquals(403, tester.getLastResponse().getStatus());
		Matcher challenge = CHALLENGE
			.matcher(tester.getLastResponse().getHeader(DeviceBoundSessions.CHALLENGE_HEADER));
		assertTrue(challenge.matches());
		assertEquals(sessionIdentifier, challenge.group(2));
		assertEquals("DENY", tester.getLastResponse().getHeader("X-Frame-Options"));
	}

	@Test
	void refreshWithProofIssuesANewBoundCookie() throws Exception
	{
		TestBrowser browser = TestBrowser.es256();
		register(browser.registrationProof(start()));
		String sessionIdentifier = sessionIdentifier();
		String oldValue = boundCookie(tester.getLastResponse()).getValue();
		refresh(sessionIdentifier, null);
		String challenge = challenge();

		clock.advance(Duration.ofMinutes(1));
		refresh(sessionIdentifier, browser.refreshProof(challenge));

		assertEquals(200, tester.getLastResponse().getStatus());
		Cookie cookie = boundCookie(tester.getLastResponse());
		assertNotEquals(oldValue, cookie.getValue());
		assertNotNull(tester.getLastResponse().getHeader(DeviceBoundSessions.CHALLENGE_HEADER));

		withoutBoundCookie();
		tester.getRequest().addCookie(cookie);
		tester.startPage(StartPage.class);
		tester.assertRenderedPage(StartPage.class);
	}

	@Test
	void refreshUsesTheChallengeSentWithTheLastCookie() throws Exception
	{
		TestBrowser browser = TestBrowser.es256();
		register(browser.registrationProof(start()));

		refresh(sessionIdentifier(), browser.refreshProof(challenge()));

		assertEquals(200, tester.getLastResponse().getStatus());
	}

	@Test
	void refreshAfterTheCookieExpiredUsesTheChallengeSentWithIt() throws Exception
	{
		TestBrowser browser = TestBrowser.es256();
		register(browser.registrationProof(start()));
		String challenge = challenge();

		clock.advance(deviceBoundSessions.getCookieLifetime());
		refresh(sessionIdentifier(), browser.refreshProof(challenge));

		assertEquals(200, tester.getLastResponse().getStatus());
		assertNotNull(boundCookie(tester.getLastResponse()));
	}

	@Test
	void refreshAfterTheCookieAndGracePeriodExpiredGetsAChallenge() throws Exception
	{
		register(TestBrowser.es256().registrationProof(start()));
		tester.startPage(StartPage.class);

		clock.advance(Duration.ofHours(1));
		refresh(sessionIdentifier(), null);

		assertEquals(403, tester.getLastResponse().getStatus());
		assertNotNull(tester.getLastResponse().getHeader(DeviceBoundSessions.CHALLENGE_HEADER));
		assertTrue(deviceBoundSessions.isBound(tester.getSession()));
	}

	@Test
	void endpointsAreFoundBehindCryptoMapper() throws Exception
	{
		tester.destroy();
		start(new DeviceBoundSessions(), application -> application.setRootRequestMapper(
			new CryptoMapper(application.getRootRequestMapper(), application)));
		TestBrowser browser = TestBrowser.es256();
		tester.startPage(StartPage.class);
		tester.setFollowRedirects(false);
		tester.clickLink("start");
		tester.setFollowRedirects(true);
		Matcher registration = REGISTRATION.matcher(
			tester.getLastResponse().getHeader(DeviceBoundSessions.REGISTRATION_HEADER));
		assertTrue(registration.matches());

		post(relative(registration.group(1)), DeviceBoundSessions.RESPONSE_HEADER,
			quoted(browser.registrationProof(registration.group(2))));
		assertEquals(200, tester.getLastResponse().getStatus());
		JSONObject instructions = new JSONObject(tester.getLastResponse().getDocument());
		String refreshPath = relative(instructions.getString("refresh_url"));
		tester.startPage(StartPage.class);

		clock.advance(Duration.ofHours(1));
		post(refreshPath, DeviceBoundSessions.SESSION_ID_HEADER, quoted(sessionIdentifier()));
		assertEquals(403, tester.getLastResponse().getStatus());
		String challenge = challenge();
		post(refreshPath, DeviceBoundSessions.SESSION_ID_HEADER, quoted(sessionIdentifier()),
			DeviceBoundSessions.RESPONSE_HEADER, quoted(browser.refreshProof(challenge)));

		assertEquals(200, tester.getLastResponse().getStatus());
		assertNotNull(boundCookie(tester.getLastResponse()));
	}

	@Test
	void refreshAnswersAChallengeOnce() throws Exception
	{
		TestBrowser browser = TestBrowser.es256();
		register(browser.registrationProof(start()));
		String sessionIdentifier = sessionIdentifier();
		String proof = browser.refreshProof(challenge());
		refresh(sessionIdentifier, proof);

		refresh(sessionIdentifier, proof);

		assertEquals(403, tester.getLastResponse().getStatus());
	}

	@Test
	void refreshWithAnExpiredChallengeGetsANewOne() throws Exception
	{
		TestBrowser browser = TestBrowser.es256();
		register(browser.registrationProof(start()));
		String sessionIdentifier = sessionIdentifier();
		refresh(sessionIdentifier, null);
		String challenge = challenge();

		clock.advance(deviceBoundSessions.getChallengeLifetime());
		refresh(sessionIdentifier, browser.refreshProof(challenge));

		assertEquals(403, tester.getLastResponse().getStatus());
		assertNotEquals(challenge, challenge());
	}

	@Test
	void refreshSignedByAnotherKeyIsRefused() throws Exception
	{
		register(TestBrowser.es256().registrationProof(start()));

		refresh(sessionIdentifier(), TestBrowser.es256().refreshProof(challenge()));

		assertEquals(400, tester.getLastResponse().getStatus());
		assertNull(boundCookie(tester.getLastResponse()));
	}

	@Test
	void refreshWithAnotherAlgorithmIsRefused() throws Exception
	{
		register(TestBrowser.es256().registrationProof(start()));

		refresh(sessionIdentifier(), TestBrowser.rs256().refreshProof(challenge()));

		assertEquals(400, tester.getLastResponse().getStatus());
	}

	@Test
	void refreshOfAnUnknownSessionEndsIt() throws Exception
	{
		register(TestBrowser.es256().registrationProof(start()));

		refresh("unknown", null);

		assertEquals(200, tester.getLastResponse().getStatus());
		assertFalse(new JSONObject(tester.getLastResponse().getDocument()).getBoolean("continue"));
	}

	@Test
	void refreshOfAnInvalidatedSessionEndsIt() throws Exception
	{
		register(TestBrowser.es256().registrationProof(start()));
		String sessionIdentifier = sessionIdentifier();
		tester.startPage(StartPage.class);
		tester.clickLink("invalidate");

		refresh(sessionIdentifier, null);

		assertEquals(200, tester.getLastResponse().getStatus());
		assertFalse(new JSONObject(tester.getLastResponse().getDocument()).getBoolean("continue"));
	}

	@Test
	void startBindsAReplacedSession() throws Exception
	{
		tester.startPage(StartPage.class);
		tester.setFollowRedirects(false);
		tester.clickLink("replaceAndStart");
		tester.setFollowRedirects(true);
		Matcher registration = REGISTRATION.matcher(
			tester.getLastResponse().getHeader(DeviceBoundSessions.REGISTRATION_HEADER));
		assertTrue(registration.matches());

		register(TestBrowser.es256().registrationProof(registration.group(2)));

		assertEquals(200, tester.getLastResponse().getStatus());
		assertTrue(deviceBoundSessions.isBound(tester.getSession()));
	}

	@Test
	void replacingTheSessionEndsTheBinding() throws Exception
	{
		register(TestBrowser.es256().registrationProof(start()));
		String sessionIdentifier = sessionIdentifier();
		tester.startPage(StartPage.class);

		tester.clickLink("replace");
		assertFalse(deviceBoundSessions.isBound(tester.getSession()));
		refresh(sessionIdentifier, null);
		assertFalse(new JSONObject(tester.getLastResponse().getDocument()).getBoolean("continue"));

		register(TestBrowser.es256().registrationProof(start()));
		assertTrue(deviceBoundSessions.isBound(tester.getSession()));
		assertNotEquals(sessionIdentifier, sessionIdentifier());
	}

	@Test
	void refreshWithoutSessionIdentifierIsRefused()
	{
		post(deviceBoundSessions.getRefreshPath());

		assertEquals(400, tester.getLastResponse().getStatus());
	}

	@Test
	void installsOncePerApplication()
	{
		assertThrows(IllegalStateException.class,
			() -> new DeviceBoundSessions().install(tester.getApplication()));
	}

	@Test
	void cannotBeConfiguredOnceInstalled()
	{
		assertThrows(IllegalStateException.class,
			() -> deviceBoundSessions.setRefreshPath("auth/refresh"));
		assertThrows(IllegalStateException.class,
			() -> deviceBoundSessions.setCookieName("other"));
		assertThrows(IllegalStateException.class,
			() -> deviceBoundSessions.setCookieSameSite(SameSite.Strict));
		assertThrows(IllegalStateException.class,
			() -> deviceBoundSessions.setGracePeriod(Duration.ZERO));
		assertEquals("dbsc/refresh", deviceBoundSessions.getRefreshPath());
	}

	@Test
	void endpointPathMayStartWithASlash() throws Exception
	{
		tester.destroy();
		start(new DeviceBoundSessions().setRegistrationPath("/auth/register")
			.setRefreshPath("/auth/refresh"));

		String challenge = start();
		Matcher registration = REGISTRATION.matcher(
			tester.getLastResponse().getHeader(DeviceBoundSessions.REGISTRATION_HEADER));
		assertTrue(registration.matches());
		assertEquals(cookiePath() + "/auth/register", registration.group(1));

		post(relative(registration.group(1)), DeviceBoundSessions.RESPONSE_HEADER,
			quoted(TestBrowser.es256().registrationProof(challenge)));
		assertEquals(200, tester.getLastResponse().getStatus());
		assertEquals(cookiePath() + "/auth/refresh",
			new JSONObject(tester.getLastResponse().getDocument()).getString("refresh_url"));
	}

	@Test
	void instructionsNameTheAllowedRefreshInitiators() throws Exception
	{
		tester.destroy();
		start(new DeviceBoundSessions().setAllowedRefreshInitiators("mail.example.com",
			"*.example.org"));

		JSONObject instructions = register(TestBrowser.es256().registrationProof(start()));

		JSONArray initiators = instructions.getJSONArray("allowed_refresh_initiators");
		assertEquals(2, initiators.length());
		assertEquals("mail.example.com", initiators.getString(0));
		assertEquals("*.example.org", initiators.getString(1));
	}

	@Test
	void applicationAtTheRootIsScopedToTheWholeOrigin() throws Exception
	{
		tester.destroy();
		start(new DeviceBoundSessions(), application -> {
		}, true);

		tester.startPage(StartPage.class);
		tester.setFollowRedirects(false);
		tester.clickLink("start");
		tester.setFollowRedirects(true);
		Matcher registration = REGISTRATION.matcher(
			tester.getLastResponse().getHeader(DeviceBoundSessions.REGISTRATION_HEADER));
		assertTrue(registration.matches());
		assertEquals("/dbsc/register", registration.group(1));

		JSONObject instructions = register(
			TestBrowser.es256().registrationProof(registration.group(2)));

		assertEquals("/dbsc/refresh", instructions.getString("refresh_url"));
		assertFalse(instructions.getJSONObject("scope").has("scope_specification"));
		assertEquals("Path=/; Secure; HttpOnly; SameSite=Lax", instructions
			.getJSONArray("credentials")
			.getJSONObject(0)
			.getString("attributes"));
		assertEquals("/", boundCookie(tester.getLastResponse()).getPath());
	}

	@Test
	void webSocketOpenedWithTheBoundCookieOutlivesIt() throws Exception
	{
		register(TestBrowser.es256().registrationProof(start()));
		Cookie cookie = boundCookie(tester.getLastResponse());
		Page page = tester.startPage(SocketPage.class);
		TestSocket socket = new TestSocket(tester, page,
			cookie.getName() + "=" + cookie.getValue());
		socket.onOpen(null);

		clock.advance(Duration.ofHours(1));
		socket.onMessage("hello");

		assertTrue(socket.isOpen());
		assertEquals(1, SocketPage.RECEIVED.size());
		assertEquals("hello", SocketPage.RECEIVED.get(0));
		assertTrue(deviceBoundSessions.isBound(tester.getSession()));
	}

	@Test
	void webSocketOpenedWithoutTheBoundCookieEndsTheSession() throws Exception
	{
		register(TestBrowser.es256().registrationProof(start()));
		Page page = tester.startPage(SocketPage.class);
		TestSocket socket = new TestSocket(tester, page, "JSESSIONID=copied");
		socket.onOpen(null);

		socket.onMessage("hello");

		assertTrue(SocketPage.RECEIVED.isEmpty());
		assertFalse(socket.isOpen());
		assertTrue(socket.sent.stream().anyMatch(message -> message.contains("<redirect>")));
	}

	@Test
	void registrationWithoutStartIsRefusedBeforeItsKeyIsRead() throws Exception
	{
		tester.startPage(StartPage.class);

		register(TestBrowser.es256().registrationProofSignedBy(TestBrowser.es256(), "anything"));

		assertEquals(403, tester.getLastResponse().getStatus());
		assertFalse(deviceBoundSessions.isBound(tester.getSession()));
	}

	/**
	 * Clicks the start link and returns the registration challenge.
	 */
	private String start()
	{
		tester.startPage(StartPage.class);
		tester.setFollowRedirects(false);
		tester.clickLink("start");
		tester.setFollowRedirects(true);
		Matcher registration = REGISTRATION.matcher(
			tester.getLastResponse().getHeader(DeviceBoundSessions.REGISTRATION_HEADER));
		assertTrue(registration.matches());
		return registration.group(2);
	}

	private JSONObject register(String proof)
	{
		post(deviceBoundSessions.getRegistrationPath(), DeviceBoundSessions.RESPONSE_HEADER,
			quoted(proof));
		String document = tester.getLastResponse().getDocument();
		return document.isEmpty() ? null : new JSONObject(document);
	}

	private void refresh(String sessionIdentifier, String proof)
	{
		if (proof == null)
		{
			post(deviceBoundSessions.getRefreshPath(), DeviceBoundSessions.SESSION_ID_HEADER,
				quoted(sessionIdentifier));
		}
		else
		{
			post(deviceBoundSessions.getRefreshPath(), DeviceBoundSessions.SESSION_ID_HEADER,
				quoted(sessionIdentifier), DeviceBoundSessions.RESPONSE_HEADER, quoted(proof));
		}
	}

	private void post(String path, String... headers)
	{
		tester.getRequest().setMethod("POST");
		for (int i = 0; i < headers.length; i += 2)
		{
			tester.getRequest().setHeader(headers[i], headers[i + 1]);
		}
		tester.executeUrl(path);
	}

	private String sessionIdentifier()
	{
		return tester.getSession().getMetaData(DeviceBoundSessions.BINDING).getSessionIdentifier();
	}

	/**
	 * @return the challenge the last response sent
	 */
	private String challenge()
	{
		Matcher challenge = CHALLENGE
			.matcher(tester.getLastResponse().getHeader(DeviceBoundSessions.CHALLENGE_HEADER));
		assertTrue(challenge.matches());
		return challenge.group(1);
	}

	private void withoutBoundCookie()
	{
		Cookie[] cookies = tester.getRequest().getCookies();
		if (cookies == null)
		{
			return;
		}
		tester.getRequest().setCookies(Arrays.stream(cookies)
			.filter(cookie -> !cookie.getName().equals(deviceBoundSessions.getCookieName()))
			.toArray(Cookie[]::new));
	}

	private Cookie boundCookie(MockHttpServletResponse response)
	{
		return response.getCookies()
			.stream()
			.filter(cookie -> cookie.getName().equals(deviceBoundSessions.getCookieName()))
			.findFirst()
			.orElse(null);
	}

	private Cookie boundCookie(jakarta.servlet.http.HttpServletRequest request)
	{
		return Arrays.stream(request.getCookies())
			.filter(cookie -> cookie.getName().equals(deviceBoundSessions.getCookieName()))
			.findFirst()
			.orElse(null);
	}

	private String cookiePath()
	{
		return tester.getRequest().getContextPath() + tester.getRequest().getServletPath();
	}

	/**
	 * @return an absolute path the application rendered, relative to the application
	 */
	private String relative(String absolutePath)
	{
		assertTrue(absolutePath.startsWith(cookiePath() + "/"), absolutePath);
		return absolutePath.substring(cookiePath().length() + 1);
	}

	private void assertSessionEnded()
	{
		assertTrue(tester.getLastResponse().isRedirect());
		tester.setFollowRedirects(true);
		tester.startPage(StartPage.class);
		assertFalse(deviceBoundSessions.isBound(tester.getSession()));
	}

	/**
	 * Serves the application at the root: no context path and no filter path.
	 */
	private static final class AtRoot extends HttpServletRequestWrapper
	{
		AtRoot(HttpServletRequest request)
		{
			super(request);
		}

		@Override
		public String getContextPath()
		{
			return "";
		}

		@Override
		public String getServletPath()
		{
			return "";
		}

		@Override
		public String getRequestURI()
		{
			String prefix = super.getContextPath() + super.getServletPath();
			String uri = super.getRequestURI();
			return uri.startsWith(prefix) ? uri.substring(prefix.length()) : uri;
		}
	}

	private static String quoted(String value)
	{
		return StructuredFields.string(value);
	}
}
