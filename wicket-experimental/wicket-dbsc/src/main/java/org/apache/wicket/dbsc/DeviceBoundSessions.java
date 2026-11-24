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

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.wicket.Application;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.http.flow.AbortWithHttpErrorCodeException;
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
 * {@link #onMissingCredential(RequestCycle)}, which rejects it.</li>
 * <li>Browsers without DBSC ignore the registration, and their sessions keep working unbound, as
 * does every session in the short time between {@link #start()} and the registration.</li>
 * <li>It does not stop malware that is running on the device, which can use the browser's key
 * there.</li>
 * <li>The bound cookie has to be sent wherever the session cookie is: its {@code SameSite} must be
 * at least as permissive as the session cookie's, or cross-site requests that carry the session
 * cookie are rejected.</li>
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

	private String cookieSameSite = "Lax";

	private Duration cookieLifetime = Duration.ofMinutes(10);

	private Duration challengeLifetime = Duration.ofMinutes(2);

	private Duration gracePeriod = Duration.ofSeconds(30);

	private Clock clock = Clock.systemUTC();

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
	 * sessions. Configure this instance before installing it.
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
				StructuredFields.string(absolutePath(registrationPath)) + ";challenge=" +
				StructuredFields.string(challenge));
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
	 * has presented a valid one or the grace period has passed. Such a request comes from a browser
	 * that does not hold the session's key: a copied session cookie, or a browser that skipped a
	 * refresh because the refresh endpoint failed.
	 * <p>
	 * It rejects the request with 403 Forbidden and leaves the session alone. Override it to
	 * invalidate the session as well, or to log the attempt. Throw to stop the request.
	 *
	 * @param cycle
	 *            the request cycle
	 */
	protected void onMissingCredential(RequestCycle cycle)
	{
		throw new AbortWithHttpErrorCodeException(HttpServletResponse.SC_FORBIDDEN);
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
		this.cookieName = Args.notEmpty(cookieName, "cookieName");
		return this;
	}

	/**
	 * @return the {@code SameSite} attribute of the bound cookie
	 */
	public String getCookieSameSite()
	{
		return cookieSameSite;
	}

	/**
	 * @param cookieSameSite
	 *            the {@code SameSite} attribute of the bound cookie, {@code Lax} by default. It has
	 *            to be at least as permissive as the session cookie's.
	 * @return this
	 */
	public DeviceBoundSessions setCookieSameSite(String cookieSameSite)
	{
		this.cookieSameSite = Args.notEmpty(cookieSameSite, "cookieSameSite");
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
	 *            how long a bound cookie lives, 10 minutes by default. A copied session cookie
	 *            works at most this long, plus the grace period.
	 * @return this
	 */
	public DeviceBoundSessions setCookieLifetime(Duration cookieLifetime)
	{
		this.cookieLifetime = positive(cookieLifetime, "cookieLifetime");
		return this;
	}

	/**
	 * @return how long a proof may answer a challenge
	 */
	public Duration getChallengeLifetime()
	{
		return challengeLifetime;
	}

	/**
	 * @param challengeLifetime
	 *            how long a proof may answer a challenge, 2 minutes by default. A proof for an
	 *            expired challenge gets a new challenge in return.
	 * @return this
	 */
	public DeviceBoundSessions setChallengeLifetime(Duration challengeLifetime)
	{
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
	 * Rejects the request if its session is bound and it carries no valid bound cookie.
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
		Instant now = now();
		Cookie cookie = ((WebRequest)cycle.getRequest()).getCookie(cookieName);
		if (binding.acceptsCredential(cookie == null ? null : cookie.getValue(), now) ||
			!binding.isArmed(now))
		{
			return;
		}
		onMissingCredential(cycle);
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
	 * Sets a new bound cookie and sends a challenge for the next refresh along with it.
	 */
	void issueCredential(DeviceBinding binding, WebResponse response, Instant now)
	{
		String value = newToken(32);
		binding.issueCredential(value, now.plus(cookieLifetime).plus(gracePeriod));
		Cookie cookie = new Cookie(cookieName, value);
		cookie.setPath(cookiePath());
		cookie.setSecure(true);
		cookie.setHttpOnly(true);
		cookie.setMaxAge((int)cookieLifetime.toSeconds());
		cookie.setAttribute("SameSite", cookieSameSite);
		response.addCookie(cookie);
		sendChallenge(binding, response, now);
	}

	/**
	 * Adds a new challenge for the next proof to the response.
	 */
	void sendChallenge(DeviceBinding binding, WebResponse response, Instant now)
	{
		String challenge = newToken(32);
		binding.addChallenge(challenge, now.plus(challengeLifetime));
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
				"Path=" + cookiePath() + "; Secure; HttpOnly; SameSite=" + cookieSameSite);
		return new JSONObject().put("session_identifier", binding.getSessionIdentifier())
			.put("refresh_url", absolutePath(refreshPath))
			.put("scope", scope)
			.put("credentials", new JSONArray().put(credential))
			.toString();
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

	private static String absolutePath(String mountPath)
	{
		return applicationPath() + "/" + mountPath;
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

	private static Duration positive(Duration duration, String name)
	{
		Args.notNull(duration, name);
		Args.isFalse(duration.isNegative() || duration.isZero(), name + " must be positive");
		return duration;
	}
}
