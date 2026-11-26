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

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import org.apache.wicket.Application;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.flow.RedirectToUrlException;
import org.apache.wicket.request.handler.resource.ResourceReferenceRequestHandler;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.http.flow.AbortWithHttpErrorCodeException;
import org.apache.wicket.util.cookies.CookieDefaults.SameSite;
import org.apache.wicket.util.lang.Args;

import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;

/**
 * Binds Wicket sessions to the device they were signed in on, following the
 * <a href="https://w3c.github.io/webappsec-dbsc/">Device Bound Session Credentials</a> draft
 * (DBSC).
 * <p>
 * The servlet container's session cookie is long-lived and can be replayed from any machine once
 * malware has copied it. A bound session needs a second, short-lived cookie as well. The browser
 * only gets a new one from the refresh endpoint after it has signed a server challenge with a
 * private key it keeps on the device, so a copied session cookie stops working once the bound
 * cookie expires.
 * <p>
 * Install it in {@link WebApplication#init()} and start the binding once the user has signed in
 * and the session id was renewed, as {@code AuthenticatedWebSession#signIn} does:
 *
 * <pre>
 * protected void init()
 * {
 * 	super.init();
 * 	new DeviceBoundSessions().install(this);
 * }
 *
 * // after a successful sign-in
 * DeviceBoundSessions.get().start();
 * </pre>
 *
 * <p>
 * What it protects and what it does not:
 * <ul>
 * <li>It protects the requests Wicket handles for a session the browser has registered. Once a
 * session is bound, such a request without a valid bound cookie goes to
 * {@link #onMissingCredential(RequestCycle, MissingCredential)}, which ends the session.</li>
 * <li>Browsers without DBSC ignore the registration, and their sessions keep working unbound, as
 * does every session in the short time between {@link #start()} and the registration.</li>
 * <li>It does not stop malware that is running on the device, which can use the browser's key
 * there.</li>
 * <li>The bound cookie has to be sent wherever the session cookie is: its {@code SameSite} must be
 * at least as permissive as the session cookie's, or cross-site requests that carry the session
 * cookie miss it.</li>
 * <li>A WebSocket connection is checked once, when it opens, with the bound cookie its handshake
 * carried. Its messages are accepted for as long as it stays open, also after that cookie has
 * expired, and so are the messages of a connection opened before the binding was enforced.</li>
 * <li>{@link Session#replaceSession()} drops the binding along with the rest of the session's
 * metadata. The browser's next refresh is told to end its DBSC session; call {@link #start()}
 * again to bind the new session.</li>
 * </ul>
 * <p>
 * This module is experimental, and so is the draft it follows.
 *
 * @since 12.0.0
 */
public class DeviceBoundSessions
{
	static final String REGISTRATION_HEADER = "Secure-Session-Registration";

	static final String CHALLENGE_HEADER = "Secure-Session-Challenge";

	static final String RESPONSE_HEADER = "Secure-Session-Response";

	static final String SESSION_ID_HEADER = "Sec-Secure-Session-Id";

	static final String SKIPPED_HEADER = "Secure-Session-Skipped";

	static final String FETCH_SITE_HEADER = "Sec-Fetch-Site";

	private static final String ACCEPTED_ATTRIBUTE = DeviceBoundSessions.class.getName() +
		".accepted";

	static final MetaDataKey<DeviceBinding> BINDING = new MetaDataKey<>()
	{
		private static final long serialVersionUID = 1L;
	};

	static final MetaDataKey<DeviceBinding.Expiring> PENDING_REGISTRATION = new MetaDataKey<>()
	{
		private static final long serialVersionUID = 1L;
	};

	private static final MetaDataKey<DeviceBoundSessions> INSTANCE = new MetaDataKey<>()
	{
		private static final long serialVersionUID = 1L;
	};

	private static final SecureRandom RANDOM = new SecureRandom();

	private String registrationPath = "dbsc/register";

	private String refreshPath = "dbsc/refresh";

	private String cookieName = "wicket-dbsc";

	private SameSite cookieSameSite = SameSite.Lax;

	private List<String> allowedRefreshInitiators = List.of();

	private Duration cookieLifetime = Duration.ofMinutes(10);

	private Duration challengeLifetime = Duration.ofMinutes(2);

	private Duration gracePeriod = Duration.ofSeconds(30);

	private Clock clock = Clock.systemUTC();

	private boolean installed;

	/**
	 * @return the instance installed in the current application
	 * @throws IllegalStateException
	 *             if none is installed
	 */
	public static DeviceBoundSessions get()
	{
		DeviceBoundSessions instance = Application.get().getMetaData(INSTANCE);
		if (instance == null)
		{
			throw new IllegalStateException("DeviceBoundSessions is not installed in application " +
				Application.get().getName());
		}
		return instance;
	}

	/**
	 * Mounts the registration and refresh endpoints and starts checking the bound cookie of bound
	 * sessions. Configure this instance before installing it: its setters throw once it is
	 * installed.
	 *
	 * @param application
	 *            the application
	 * @return this
	 * @throws IllegalStateException
	 *             if the application has one installed already
	 */
	public DeviceBoundSessions install(WebApplication application)
	{
		Args.notNull(application, "application");
		if (application.getMetaData(INSTANCE) != null)
		{
			throw new IllegalStateException(
				"DeviceBoundSessions is already installed in application " + application.getName());
		}
		application.mountResource(registrationPath,
			new EndpointReference(EndpointReference.REGISTRATION));
		application.mountResource(refreshPath, new EndpointReference(EndpointReference.REFRESH));
		application.getRequestCycleListeners().add(new BoundCredentialCheck(this));
		application.setMetaData(INSTANCE, this);
		installed = true;
		return this;
	}

	/**
	 * Asks the browser to bind the current session to the device. Call it once the user has signed
	 * in, after {@link Session#changeSessionId()} or {@link Session#replaceSession()}.
	 * <p>
	 * It binds the session if it is temporary and adds the {@code Secure-Session-Registration}
	 * header to the current response, whether that is a page, an Ajax response or a redirect. A
	 * browser with DBSC then creates a key pair and registers it with the registration endpoint. A
	 * browser without it ignores the header. Nothing happens if the session is bound to a device
	 * already.
	 * <p>
	 * A {@link org.apache.wicket.request.flow.ResetResponseException} thrown after this method,
	 * such as a {@link RestartResponseException}, resets the response and drops the header, and
	 * the session stays unbound. Continue the sign-in with
	 * {@link org.apache.wicket.Component#setResponsePage(Class)} or
	 * {@link org.apache.wicket.Component#continueToOriginalDestination()}, which keep it, or call
	 * this method from the page the sign-in continues to.
	 *
	 * @throws IllegalStateException
	 *             if the current response is not a {@link WebResponse}
	 */
	public void start()
	{
		Session session = Session.get();
		if (session.getMetaData(BINDING) != null)
		{
			return;
		}
		Response response = RequestCycle.get().getResponse();
		if (!(response instanceof WebResponse webResponse))
		{
			throw new IllegalStateException("Cannot add a header to " + response);
		}
		session.bind();
		String challenge = newToken(32);
		session.setMetaData(PENDING_REGISTRATION,
			new DeviceBinding.Expiring(challenge, now().plus(challengeLifetime)));
		webResponse.addHeader(REGISTRATION_HEADER,
			"(" + DbscProof.ES256 + " " + DbscProof.RS256 + ");path=" +
				StructuredFields.string(endpointPath(EndpointReference.REGISTRATION)) +
				";challenge=" + StructuredFields.string(challenge));
	}

	/**
	 * @param session
	 *            a session
	 * @return whether a browser has registered a key for the session
	 */
	public boolean isBound(Session session)
	{
		return Args.notNull(session, "session").getMetaData(BINDING) != null;
	}

	/**
	 * Called for a request on a bound session that carries no valid bound cookie, once the browser
	 * has presented a valid one or the grace period has passed. A copied session cookie is one
	 * reason, but not the only one. The browser holding the key sends such a request too:
	 * <ul>
	 * <li>for a request another site started, for instance a link in a webmail, after the bound
	 * cookie has expired. The browser only refreshes the cookie first for requests from the
	 * application's own origin and from the hosts in
	 * {@link #setAllowedRefreshInitiators(String...)}, see
	 * {@link MissingCredential#isCrossSite()};</li>
	 * <li>when it skipped a refresh, because the refresh endpoint failed or it ran out of signing
	 * operations, see {@link MissingCredential#getSkippedRefresh()};</li>
	 * <li>when it has given up its DBSC session while the server still holds the binding, after a
	 * lost registration response or an error from the refresh endpoint.</li>
	 * </ul>
	 * <p>
	 * It invalidates the session and redirects the browser to the page the request came from,
	 * which then starts without a session: an application with a sign-in page asks the user to
	 * sign in again. The request does not reach its page, behavior or resource. For a session
	 * whose cookies were copied, the first request from the copy ends the session, which signs the
	 * user out.
	 * <p>
	 * Override it to log the attempt, to answer in another way or to tell the cases above apart.
	 * Stop the request by throwing, for instance an {@link AbortWithHttpErrorCodeException} with
	 * 403 to reject it and keep the session. A rejected request does not end the session, so its
	 * browser gets 403 until the session times out, its sign-in page included. An override that
	 * redirects has to invalidate the session as well: the redirected request is checked like any
	 * other, and without a bound cookie it is redirected again.
	 *
	 * @param cycle
	 *            the request cycle
	 * @param credential
	 *            what the request carried
	 */
	protected void onMissingCredential(RequestCycle cycle, MissingCredential credential)
	{
		Session.get().invalidate();
		throw new RedirectToUrlException("/" + cycle.getUrlRenderer().getBaseUrl());
	}

	/**
	 * @return the path the registration endpoint is mounted at
	 */
	public String getRegistrationPath()
	{
		return registrationPath;
	}

	/**
	 * @param registrationPath
	 *            the path to mount the registration endpoint at, {@code dbsc/register} by default
	 * @return this
	 */
	public DeviceBoundSessions setRegistrationPath(String registrationPath)
	{
		checkNotInstalled();
		this.registrationPath = Args.notEmpty(registrationPath, "registrationPath");
		return this;
	}

	/**
	 * @return the path the refresh endpoint is mounted at
	 */
	public String getRefreshPath()
	{
		return refreshPath;
	}

	/**
	 * @param refreshPath
	 *            the path to mount the refresh endpoint at, {@code dbsc/refresh} by default
	 * @return this
	 */
	public DeviceBoundSessions setRefreshPath(String refreshPath)
	{
		checkNotInstalled();
		this.refreshPath = Args.notEmpty(refreshPath, "refreshPath");
		return this;
	}

	/**
	 * @return the name of the bound cookie
	 */
	public String getCookieName()
	{
		return cookieName;
	}

	/**
	 * @param cookieName
	 *            the name of the bound cookie, {@code wicket-dbsc} by default
	 * @return this
	 */
	public DeviceBoundSessions setCookieName(String cookieName)
	{
		checkNotInstalled();
		this.cookieName = Args.notEmpty(cookieName, "cookieName");
		return this;
	}

	/**
	 * @return the {@code SameSite} attribute of the bound cookie
	 */
	public SameSite getCookieSameSite()
	{
		return cookieSameSite;
	}

	/**
	 * @param cookieSameSite
	 *            the {@code SameSite} attribute of the bound cookie, {@code Lax} by default. It has
	 *            to be at least as permissive as the session cookie's.
	 * @return this
	 */
	public DeviceBoundSessions setCookieSameSite(SameSite cookieSameSite)
	{
		checkNotInstalled();
		this.cookieSameSite = Args.notNull(cookieSameSite, "cookieSameSite");
		return this;
	}

	/**
	 * @return the hosts whose pages may make the browser refresh the bound cookie
	 */
	public List<String> getAllowedRefreshInitiators()
	{
		return allowedRefreshInitiators;
	}

	/**
	 * Lists the other sites whose requests the browser holds back until it has refreshed the bound
	 * cookie, as {@code allowed_refresh_initiators} of the session instructions. A request another
	 * site started after the bound cookie has expired otherwise arrives without it, and goes to
	 * {@link #onMissingCredential(RequestCycle, MissingCredential)}. Requests from the
	 * application's own origin are always refreshed.
	 * <p>
	 * The browser waits for the refresh before it sends a listed site's request, which tells that
	 * site how long the refresh took. List only sites you trust with that.
	 *
	 * @param hostPatterns
	 *            host patterns, none by default: a host such as {@code mail.example.com},
	 *            {@code *.example.com} for its subdomains, or {@code *} for every host
	 * @return this
	 */
	public DeviceBoundSessions setAllowedRefreshInitiators(String... hostPatterns)
	{
		checkNotInstalled();
		Args.notNull(hostPatterns, "hostPatterns");
		for (String hostPattern : hostPatterns)
		{
			Args.notEmpty(hostPattern, "hostPattern");
		}
		allowedRefreshInitiators = List.of(hostPatterns);
		return this;
	}

	/**
	 * @return how long a bound cookie lives
	 */
	public Duration getCookieLifetime()
	{
		return cookieLifetime;
	}

	/**
	 * @param cookieLifetime
	 *            how long a bound cookie lives, 10 minutes by default. Cookies copied from the
	 *            browser keep working at most this long, plus the grace period.
	 * @return this
	 */
	public DeviceBoundSessions setCookieLifetime(Duration cookieLifetime)
	{
		checkNotInstalled();
		this.cookieLifetime = positive(cookieLifetime, "cookieLifetime");
		return this;
	}

	/**
	 * @return how long a proof may answer a challenge sent on its own
	 */
	public Duration getChallengeLifetime()
	{
		return challengeLifetime;
	}

	/**
	 * @param challengeLifetime
	 *            how long a proof may answer a challenge sent on its own, with a registration or
	 *            with a refresh that had none, 2 minutes by default. The challenge sent along with
	 *            a bound cookie is kept by the browser for the refresh that follows when the cookie
	 *            expires, so it lives at least as long as the cookie plus the grace period. A
	 *            proof for an expired challenge gets a new challenge in return.
	 * @return this
	 */
	public DeviceBoundSessions setChallengeLifetime(Duration challengeLifetime)
	{
		checkNotInstalled();
		this.challengeLifetime = positive(challengeLifetime, "challengeLifetime");
		return this;
	}

	/**
	 * @return how long requests may arrive without a valid bound cookie
	 */
	public Duration getGracePeriod()
	{
		return gracePeriod;
	}

	/**
	 * @param gracePeriod
	 *            30 seconds by default. A newly bound session accepts requests without a bound
	 *            cookie until the browser presents one, or for this long, for the requests that
	 *            were sent before the registration finished. An expired bound cookie is accepted
	 *            this much longer, for the requests in flight while it expired.
	 * @return this
	 */
	public DeviceBoundSessions setGracePeriod(Duration gracePeriod)
	{
		checkNotInstalled();
		Args.notNull(gracePeriod, "gracePeriod");
		Args.isFalse(gracePeriod.isNegative(), "gracePeriod must not be negative");
		this.gracePeriod = gracePeriod;
		return this;
	}

	void setClock(Clock clock)
	{
		this.clock = clock;
	}

	Instant now()
	{
		return clock.instant();
	}

	/**
	 * Hands the request to {@link #onMissingCredential(RequestCycle, MissingCredential)} if its
	 * session is bound and it carries no valid bound cookie.
	 */
	void checkCredential(RequestCycle cycle)
	{
		if (!Session.exists())
		{
			return;
		}
		DeviceBinding binding = Session.get().getMetaData(BINDING);
		if (binding == null)
		{
			return;
		}
		WebRequest request = (WebRequest)cycle.getRequest();
		HttpServletRequest servletRequest = request
			.getContainerRequest() instanceof HttpServletRequest container ? container : null;
		String sessionIdentifier = binding.getSessionIdentifier();
		// a WebSocket connection keeps the request of its handshake for all its messages
		if (servletRequest != null &&
			sessionIdentifier.equals(servletRequest.getAttribute(ACCEPTED_ATTRIBUTE)))
		{
			return;
		}
		Instant now = now();
		String value = boundCookieValue(request);
		if (binding.acceptsCredential(value, now) || !binding.isArmed(now))
		{
			if (servletRequest != null)
			{
				servletRequest.setAttribute(ACCEPTED_ATTRIBUTE, sessionIdentifier);
			}
			return;
		}
		onMissingCredential(cycle,
			new MissingCredential(value != null,
				"cross-site".equals(request.getHeader(FETCH_SITE_HEADER)),
				StructuredFields.tokenFor(request.getHeaders(SKIPPED_HEADER),
					"session_identifier", sessionIdentifier)));
	}

	/**
	 * @return the value of the bound cookie, also from the {@code Cookie} header of a WebSocket
	 *         request, which reports no cookies
	 */
	private String boundCookieValue(WebRequest request)
	{
		Cookie cookie = request.getCookie(cookieName);
		if (cookie != null)
		{
			return cookie.getValue();
		}
		for (String header : request.getHeaders("Cookie"))
		{
			for (String pair : header.split(";"))
			{
				int separator = pair.indexOf('=');
				if (separator > 0 && pair.substring(0, separator).trim().equals(cookieName))
				{
					return pair.substring(separator + 1).trim();
				}
			}
		}
		return null;
	}

	/**
	 * Creates a binding for a key the browser has registered.
	 */
	DeviceBinding newBinding(DbscProof proof, Instant now) throws DbscProof.InvalidProofException
	{
		return new DeviceBinding(newToken(16), proof.embeddedKey(), proof.algorithm(),
			now.plus(gracePeriod));
	}

	/**
	 * Sets a new bound cookie and sends the challenge for the next refresh along with it.
	 */
	void issueCredential(DeviceBinding binding, WebResponse response, Instant now)
	{
		String value = newToken(32);
		Duration credentialLifetime = cookieLifetime.plus(gracePeriod);
		binding.issueCredential(value, now.plus(credentialLifetime));
		Cookie cookie = new Cookie(cookieName, value);
		cookie.setPath(cookiePath());
		cookie.setSecure(true);
		cookie.setHttpOnly(true);
		cookie.setMaxAge((int)cookieLifetime.toSeconds());
		cookie.setAttribute("SameSite", cookieSameSite.name());
		response.addCookie(cookie);
		sendChallenge(binding, response, now,
			credentialLifetime.compareTo(challengeLifetime) > 0 ? credentialLifetime
				: challengeLifetime);
	}

	/**
	 * Adds a new challenge for the next proof to the response.
	 */
	void sendChallenge(DeviceBinding binding, WebResponse response, Instant now)
	{
		sendChallenge(binding, response, now, challengeLifetime);
	}

	private void sendChallenge(DeviceBinding binding, WebResponse response, Instant now,
		Duration lifetime)
	{
		String challenge = newToken(32);
		binding.addChallenge(challenge, now.plus(lifetime));
		response.setHeader(CHALLENGE_HEADER, StructuredFields.string(challenge) + ";id=" +
			StructuredFields.string(binding.getSessionIdentifier()));
	}

	/**
	 * @return the JSON session instructions for a binding
	 */
	String instructions(DeviceBinding binding)
	{
		JSONObject scope = new JSONObject().put("include_site", false);
		String applicationPath = applicationPath();
		if (!applicationPath.isEmpty())
		{
			scope.put("scope_specification", new JSONArray().put(scopeRule("exclude", "/"))
				.put(scopeRule("include", applicationPath)));
		}
		JSONObject credential = new JSONObject().put("type", "cookie")
			.put("name", cookieName)
			.put("attributes",
				"Path=" + cookiePath() + "; Secure; HttpOnly; SameSite=" + cookieSameSite.name());
		JSONObject instructions = new JSONObject()
			.put("session_identifier", binding.getSessionIdentifier())
			.put("refresh_url", endpointPath(EndpointReference.REFRESH))
			.put("scope", scope)
			.put("credentials", new JSONArray().put(credential));
		if (!allowedRefreshInitiators.isEmpty())
		{
			instructions.put("allowed_refresh_initiators",
				new JSONArray(allowedRefreshInitiators));
		}
		return instructions.toString();
	}

	private static JSONObject scopeRule(String type, String path)
	{
		return new JSONObject().put("type", type).put("domain", "*").put("path", path);
	}

	private static String cookiePath()
	{
		String applicationPath = applicationPath();
		return applicationPath.isEmpty() ? "/" : applicationPath;
	}

	/**
	 * @return the absolute path of an endpoint, the way the application's mappers render it
	 */
	private static String endpointPath(String endpoint)
	{
		Url url = RequestCycle.get()
			.mapUrlFor(new ResourceReferenceRequestHandler(new EndpointReference(endpoint)));
		return applicationPath() + "/" + url;
	}

	/**
	 * @return the path the application is served at, the context path followed by the filter path,
	 *         or the empty string for an application at the root
	 */
	private static String applicationPath()
	{
		Request request = RequestCycle.get().getRequest();
		return request.getContextPath() + request.getFilterPath();
	}

	static String newToken(int bytes)
	{
		byte[] token = new byte[bytes];
		RANDOM.nextBytes(token);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(token);
	}

	private void checkNotInstalled()
	{
		if (installed)
		{
			throw new IllegalStateException(
				"DeviceBoundSessions cannot be configured once it is installed");
		}
	}

	private static Duration positive(Duration duration, String name)
	{
		Args.notNull(duration, name);
		Args.isFalse(duration.isNegative() || duration.isZero(), name + " must be positive");
		return duration;
	}
}
