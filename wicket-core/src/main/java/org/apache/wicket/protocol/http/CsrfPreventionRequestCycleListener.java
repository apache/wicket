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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.core.request.handler.IPageRequestHandler;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.IRequestHandlerDelegate;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.cycle.IRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.flow.AbortWithHttpErrorCodeException;
import org.apache.wicket.util.lang.Checks;
import org.apache.wicket.util.string.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Prevents CSRF attacks on Wicket components by checking the {@code Origin} and {@code Referer}
 * HTTP headers for cross domain requests. By default only checks requests that try to perform an
 * action on a component, such as a form submit, or link click.
 * <p>
 * <h3>Installation</h3>
 * <p>
 * You can enable this CSRF prevention filter by adding it to the request cycle listeners in your
 * {@link WebApplication#init() application's init method}:
 *
 * <pre>
 * &#064;Override
 * protected void init()
 * {
 * 	// ...
 * 	getRequestCycleListeners().add(new CsrfPreventionRequestCycleListener());
 * 	// ...
 * }
 * </pre>
 * <p>
 * <h3>Configuration</h3>
 * <p>
 * When the {@code Origin} or {@code Referer} HTTP header is present but doesn't match the requested
 * URL this listener will by default throw a HTTP error ( {@code 400 BAD REQUEST}) and abort the
 * request. You can {@link #setConflictingOriginAction(CsrfAction) configure} this specific action.
 * <p>
 * A missing {@code Origin} and {@code Referer} HTTP header is handled as if it were a bad request
 * and rejected. You can {@link #setNoOriginAction(CsrfAction) configure the specific action} to a
 * different value, suppressing or allowing the request when the HTTP headers are missing.
 * <p>
 * When the {@code Origin} HTTP header is present and has the value {@code null} it is considered to
 * be from a "privacy-sensitive" context and will trigger the no origin action. You can customize
 * what happens in those actions by overriding the respective {@code onXXXX} methods.
 * <p>
 * When you want to accept certain cross domain request from a range of hosts, you can
 * {@link #addAcceptedOrigin(String) whitelist those domains}.
 * <p>
 * You can {@link #isEnabled() enable or disable} this listener by overriding {@link #isEnabled()}.
 * <p>
 * You can {@link #isChecked(IRequestablePage) customize} whether a particular page should be
 * checked for CSRF requests. For example you can skip checking pages that have a
 * {@code @NoCsrfCheck} annotation, or only those pages that extend your base secure page class. For
 * example:
 *
 * <pre>
 * &#064;Override
 * protected boolean isChecked(IRequestablePage requestedPage)
 * {
 * 	return requestedPage instanceof SecurePage;
 * }
 * </pre>
 * <p>
 * You can also tweak the request handlers that are checked. The CSRF prevention request cycle
 * listener checks only action handlers, not render handlers. Override
 * {@link #isChecked(IRequestHandler)} to customize this behavior.
 * </p>
 * <p>
 * You can customize the default actions that are performed by overriding the event handlers for
 * them:
 * <ul>
 * <li>{@link #onWhitelisted(HttpServletRequest, String, IRequestablePage)} when an origin was
 * whitelisted</li>
 * <li>{@link #onMatchingOrigin(HttpServletRequest, String, IRequestablePage)} when an origin was
 * matching</li>
 * <li>{@link #onAborted(HttpServletRequest, String, IRequestablePage)} when an origin was in
 * conflict and the request should be aborted</li>
 * <li>{@link #onAllowed(HttpServletRequest, String, IRequestablePage)} when an origin was in
 * conflict and the request should be allowed</li>
 * <li>{@link #onSuppressed(HttpServletRequest, String, IRequestablePage)} when an origin was in
 * conflict and the request should be suppressed</li>
 * </ul>
 */
public class CsrfPreventionRequestCycleListener implements IRequestCycleListener
{
	private static final Logger log = LoggerFactory
		.getLogger(CsrfPreventionRequestCycleListener.class);

	/**
	 * The action to perform when a missing or conflicting source URI is detected.
	 */
	public enum CsrfAction {
		/** Aborts the request and throws an exception when a CSRF request is detected. */
		ABORT {
			@Override
			public String toString()
			{
				return "aborted";
			}
		},

		/**
		 * Ignores the action of a CSRF request, and just renders the page it was targeted against.
		 */
		SUPPRESS {
			@Override
			public String toString()
			{
				return "suppressed";
			}
		},

		/** Detects a CSRF request, logs it and allows the request to continue. */
		ALLOW {
			@Override
			public String toString()
			{
				return "allowed";
			}
		},
	}

	/**
	 * Action to perform when no Origin header is present in the request.
	 */
	private CsrfAction noOriginAction = CsrfAction.ABORT;

	/**
	 * Action to perform when a conflicting Origin header is found.
	 */
	private CsrfAction conflictingOriginAction = CsrfAction.ABORT;

	/**
	 * The error code to report when the action to take for a CSRF request is
	 * {@link CsrfAction#ABORT}. Default {@code 400 BAD REQUEST}.
	 */
	private int errorCode = javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;

	/**
	 * The error message to report when the action to take for a CSRF request is {@code ERROR}.
	 * Default {@code "Origin does not correspond to request"}.
	 */
	private String errorMessage = "Origin does not correspond to request";

	/**
	 * A white list of accepted origins (host names/domain names) presented as
	 * &lt;domainname&gt;.&lt;TLD&gt;. The domain part can contain subdomains.
	 */
	private Collection<String> acceptedOrigins = new ArrayList<>();

	/**
	 * Sets the action when no Origin header is present in the request. Default {@code ALLOW}.
	 *
	 * @param action
	 *            the alternate action
	 *
	 * @return this (for chaining)
	 */
	public CsrfPreventionRequestCycleListener setNoOriginAction(CsrfAction action)
	{
		this.noOriginAction = action;
		return this;
	}

	/**
	 * Sets the action when a conflicting Origin header is detected. Default is {@code ERROR}.
	 *
	 * @param action
	 *            the alternate action
	 *
	 * @return this
	 */
	public CsrfPreventionRequestCycleListener setConflictingOriginAction(CsrfAction action)
	{
		this.conflictingOriginAction = action;
		return this;
	}

	/**
	 * Modifies the HTTP error code in the exception when a conflicting Origin header is detected.
	 *
	 * @param errorCode
	 *            the alternate HTTP error code, default {@code 400 BAD REQUEST}
	 *
	 * @return this
	 */
	public CsrfPreventionRequestCycleListener setErrorCode(int errorCode)
	{
		this.errorCode = errorCode;
		return this;
	}

	/**
	 * Modifies the HTTP message in the exception when a conflicting Origin header is detected.
	 *
	 * @param errorMessage
	 *            the alternate message
	 *
	 * @return this
	 */
	public CsrfPreventionRequestCycleListener setErrorMessage(String errorMessage)
	{
		this.errorMessage = errorMessage;
		return this;
	}

	/**
	 * Adds an origin (host name/domain name) to the white list. An origin is in the form of
	 * &lt;domainname&gt;.&lt;TLD&gt;, and can contain a subdomain. Every Origin header that matches
	 * a domain from the whitelist is accepted and not checked any further for CSRF issues.
	 *
	 * E.g. when {@code example.com} is in the white list, this allows requests from (i.e. with an
	 * {@code Origin:} header containing) {@code example.com} and {@code blabla.example.com} but
	 * rejects requests from {@code blablaexample.com} and {@code example2.com}.
	 *
	 * @param acceptedOrigin
	 *            the acceptable origin
	 * @return this
	 */
	public CsrfPreventionRequestCycleListener addAcceptedOrigin(String acceptedOrigin)
	{
		Checks.notNull("acceptedOrigin", acceptedOrigin);

		// strip any leading dot characters
		final int len = acceptedOrigin.length();
		int i = 0;
		while (i < len && acceptedOrigin.charAt(i) == '.')
		{
			i++;
		}
		acceptedOrigins.add(acceptedOrigin.substring(i));
		return this;
	}

	@Override
	public void onBeginRequest(RequestCycle cycle)
	{
		if (log.isDebugEnabled())
		{
			HttpServletRequest containerRequest = (HttpServletRequest)cycle.getRequest()
				.getContainerRequest();
			log.debug("Request Source URI: {}", getSourceUri(containerRequest));
		}
	}

	/**
	 * Dynamic override for enabling/disabling the CSRF detection. Might be handy for specific
	 * tenants in a multi-tenant application. When false, the CSRF detection is not performed for
	 * the running request. Default {@code true}
	 *
	 * @return {@code true} when the CSRF checks need to be performed.
	 */
	protected boolean isEnabled()
	{
		return true;
	}

	/**
	 * Override to limit whether the request to the specific page should be checked for a possible
	 * CSRF attack.
	 *
	 * @param targetedPage
	 *            the page that is the target for the action
	 * @return {@code true} when the request to the page should be checked for CSRF issues.
	 */
	protected boolean isChecked(IRequestablePage targetedPage)
	{
		return true;
	}

	/**
	 * Override to change the request handler types that are checked. Currently only action handlers
	 * (form submits, link clicks, AJAX events) are checked for a matching Origin HTTP header.
	 *
	 * @param handler
	 *            the handler that is currently processing
	 * @return true when the Origin HTTP header should be checked for this {@code handler}
	 */
	protected boolean isChecked(IRequestHandler handler)
	{
		return handler instanceof IPageRequestHandler &&
			!(handler instanceof RenderPageRequestHandler);
	}

	/**
	 * Unwraps the handler if it is a {@code IRequestHandlerDelegate} down to the deepest nested
	 * handler.
	 *
	 * @param handler
	 *            The handler to unwrap
	 * @return the deepest handler that does not implement {@code IRequestHandlerDelegate}
	 */
	protected IRequestHandler unwrap(IRequestHandler handler)
	{
		while (handler instanceof IRequestHandlerDelegate)
			handler = ((IRequestHandlerDelegate)handler).getDelegateHandler();
		return handler;
	}

	@Override
	public void onRequestHandlerResolved(RequestCycle cycle, IRequestHandler handler)
	{
		if (!isEnabled())
		{
			log.trace("CSRF listener is disabled, no checks performed");
			return;
		}

		handler = unwrap(handler);

		// check if the request is targeted at a page
		if (isChecked(handler))
		{
			IPageRequestHandler prh = (IPageRequestHandler)handler;
			IRequestablePage targetedPage = prh.getPage();
			HttpServletRequest containerRequest = (HttpServletRequest)cycle.getRequest()
				.getContainerRequest();
			String sourceUri = getSourceUri(containerRequest);

			// Check if the page should be CSRF protected
			if (isChecked(targetedPage))
			{
				// if so check the Origin HTTP header
				checkRequest(containerRequest, sourceUri, targetedPage);
			}
			else
			{
				if (log.isDebugEnabled())
				{
					log.debug("Targeted page {} was opted out of the CSRF origin checks, allowed",
							targetedPage.getClass().getName());
				}
				allowHandler(containerRequest, sourceUri, targetedPage);
			}
		}
		else
		{
			if (log.isTraceEnabled())
				log.trace(
					"Resolved handler {} doesn't target an action on a page, no CSRF check performed",
					handler.getClass().getName());
		}
	}

	/**
	 * Resolves the source URI from the request headers ({@code Origin} or {@code Referer}).
	 *
	 * @param containerRequest
	 *            the current container request
	 * @return the normalized source URI.
	 */
	protected String getSourceUri(HttpServletRequest containerRequest)
	{
		String sourceUri = containerRequest.getHeader(WebRequest.HEADER_ORIGIN);
		if (Strings.isEmpty(sourceUri))
		{
			sourceUri = containerRequest.getHeader(WebRequest.HEADER_REFERER);
		}
		return normalizeUri(sourceUri);
	}

	/**
	 * Performs the check of the {@code Origin} or {@code Referer} header that is targeted at the
	 * {@code page}.
	 *
	 * @param request
	 *            the current container request
	 * @param sourceUri
	 *            the source URI
	 * @param page
	 *            the page that is the target of the request
	 */
	protected void checkRequest(HttpServletRequest request, String sourceUri, IRequestablePage page)
	{
		if (sourceUri == null || sourceUri.isEmpty())
		{
			log.debug("Source URI not present in request, {}", noOriginAction);
			switch (noOriginAction)
			{
				case ALLOW :
					allowHandler(request, sourceUri, page);
					break;
				case SUPPRESS :
					suppressHandler(request, sourceUri, page);
					break;
				case ABORT :
					abortHandler(request, sourceUri, page);
					break;
			}
			return;
		}
		sourceUri = sourceUri.toLowerCase(Locale.ROOT);

		// if the origin is a know and trusted origin, don't check any further but allow the request
		if (isWhitelistedHost(sourceUri))
		{
			whitelistedHandler(request, sourceUri, page);
			return;
		}

		// check if the origin HTTP header matches the request URI
		if (!isLocalOrigin(request, sourceUri))
		{
			log.debug("Source URI conflicts with request origin, {}", conflictingOriginAction);
			switch (conflictingOriginAction)
			{
				case ALLOW :
					allowHandler(request, sourceUri, page);
					break;
				case SUPPRESS :
					suppressHandler(request, sourceUri, page);
					break;
				case ABORT :
					abortHandler(request, sourceUri, page);
					break;
			}
		}
		else
		{
			matchingOrigin(request, sourceUri, page);
		}
	}

	/**
	 * Checks whether the domain part of the {@code sourceUri} ({@code Origin} or {@code Referer}
	 * header) is whitelisted.
	 *
	 * @param sourceUri
	 *            the contents of the {@code Origin} or {@code Referer} HTTP header
	 * @return {@code true} when the source domain was whitelisted
	 */
	protected boolean isWhitelistedHost(final String sourceUri)
	{
		try
		{
			final String sourceHost = new URI(sourceUri).getHost();
			if (Strings.isEmpty(sourceHost))
				return false;
			for (String whitelistedOrigin : acceptedOrigins)
			{
				if (sourceHost.equalsIgnoreCase(whitelistedOrigin) ||
					sourceHost.endsWith("." + whitelistedOrigin))
				{
					log.trace("Origin {} matched whitelisted origin {}, request accepted",
						sourceUri, whitelistedOrigin);
					return true;
				}
			}
		}
		catch (URISyntaxException e)
		{
			log.debug("Origin: {} not parseable as an URI. Whitelisted-origin check skipped.",
				sourceUri);
		}

		return false;
	}

	/**
	 * Checks whether the {@code Origin} HTTP header of the request matches where the request came
	 * from.
	 *
	 * @param containerRequest
	 *            the current container request
	 * @param originHeader
	 *            the contents of the {@code Origin} HTTP header
	 * @return {@code true} when the origin of the request matches the {@code Origin} HTTP header
	 */
	protected boolean isLocalOrigin(HttpServletRequest containerRequest, String originHeader)
	{
		// Make comparable strings from Origin and Location
		String origin = normalizeUri(originHeader);
		if (origin == null)
			return false;

		String request = getTargetUriFromRequest(containerRequest);
		if (request == null)
			return false;

		return origin.equalsIgnoreCase(request);
	}

	/**
	 * Creates a RFC-6454 comparable URI from the {@code uri} string.
	 *
	 * @param uri
	 *            the contents of the Origin or Referer HTTP header
	 * @return only the scheme://host[:port] part, or {@code null} when the URI string is not
	 *         compliant
	 */
	protected final String normalizeUri(String uri)
	{
		// the request comes from a privacy sensitive context, flag as non-local origin. If
		// alternative action is required, an implementor can override any of the onAborted,
		// onSuppressed or onAllowed and implement such needed action.

		if (Strings.isEmpty(uri) || "null".equals(uri))
			return null;

		StringBuilder target = new StringBuilder();

		try
		{
			URI originUri = new URI(uri);
			String scheme = originUri.getScheme();
			if (scheme == null)
			{
				return null;
			}
			else
			{
				scheme = scheme.toLowerCase(Locale.ROOT);
			}

			target.append(scheme);
			target.append("://");

			String host = originUri.getHost();
			if (host == null)
			{
				return null;
			}
			target.append(host);

			int port = originUri.getPort();
			boolean portIsSpecified = port != -1;
			boolean isAlternateHttpPort = "http".equals(scheme) && port != 80;
			boolean isAlternateHttpsPort = "https".equals(scheme) && port != 443;

			if (portIsSpecified && (isAlternateHttpPort || isAlternateHttpsPort))
			{
				target.append(':');
				target.append(port);
			}
			return target.toString();
		}
		catch (URISyntaxException e)
		{
			log.debug("Invalid URI provided: {}, marked conflicting", uri);
			return null;
		}
	}

	/**
	 * Creates a RFC-6454 comparable URI from the {@code request} requested resource.
	 *
	 * @param request
	 *            the incoming request
	 * @return only the scheme://host[:port] part, or {@code null} when the origin string is not
	 *         compliant
	 */
	protected final String getTargetUriFromRequest(HttpServletRequest request)
	{
		// Build scheme://host:port from request
		StringBuilder target = new StringBuilder();
		String scheme = request.getScheme();
		if (scheme == null)
		{
			return null;
		}
		else
		{
			scheme = scheme.toLowerCase(Locale.ROOT);
		}
		target.append(scheme);
		target.append("://");

		String host = request.getServerName();
		if (host == null)
		{
			return null;
		}
		target.append(host);

		int port = request.getServerPort();
		if ("http".equals(scheme) && port != 80 || "https".equals(scheme) && port != 443)
		{
			target.append(':');
			target.append(port);
		}

		return target.toString();
	}

	/**
	 * Handles the case where an origin is in the whitelist. Default action is to allow the
	 * whitelisted origin.
	 *
	 * @param request
	 *            the request
	 * @param origin
	 *            the contents of the {@code Origin} HTTP header
	 * @param page
	 *            the page that is targeted with this request
	 */
	protected void whitelistedHandler(HttpServletRequest request, String origin,
		IRequestablePage page)
	{
		onWhitelisted(request, origin, page);
		if (log.isDebugEnabled())
		{
			log.debug("CSRF Origin {} was whitelisted, allowed for page {}", origin,
				page.getClass().getName());
		}
	}

	/**
	 * Called when the origin was available in the whitelist. Override this method to implement your
	 * own custom action.
	 *
	 * @param request
	 *            the request
	 * @param origin
	 *            the contents of the {@code Origin} HTTP header
	 * @param page
	 *            the page that is targeted with this request
	 */
	protected void onWhitelisted(HttpServletRequest request, String origin, IRequestablePage page)
	{
	}

	/**
	 * Handles the case where an origin was checked and matched the request origin. Default action
	 * is to allow the whitelisted origin.
	 *
	 * @param request
	 *            the request
	 * @param origin
	 *            the contents of the {@code Origin} HTTP header
	 * @param page
	 *            the page that is targeted with this request
	 */
	protected void matchingOrigin(HttpServletRequest request, String origin,
		IRequestablePage page)
	{
		onMatchingOrigin(request, origin, page);
		if (log.isDebugEnabled())
		{
			log.debug("CSRF Origin {} matched requested resource, allowed for page {}", origin,
				page.getClass().getName());
		}
	}

	/**
	 * Called when the origin HTTP header matched the request. Override this method to implement
	 * your own custom action.
	 *
	 * @param request
	 *            the request
	 * @param origin
	 *            the contents of the {@code Origin} HTTP header
	 * @param page
	 *            the page that is targeted with this request
	 */
	protected void onMatchingOrigin(HttpServletRequest request, String origin,
		IRequestablePage page)
	{
	}

	/**
	 * Handles the case where an Origin HTTP header was not present or did not match the request
	 * origin, and the corresponding action ({@link #noOriginAction} or
	 * {@link #conflictingOriginAction}) is set to {@code ALLOW}.
	 *
	 * @param request
	 *            the request
	 * @param origin
	 *            the contents of the {@code Origin} HTTP header, may be {@code null} or empty
	 * @param page
	 *            the page that is targeted with this request
	 */
	protected void allowHandler(HttpServletRequest request, String origin,
		IRequestablePage page)
	{
		onAllowed(request, origin, page);
		log.info("Possible CSRF attack, request URL: {}, Origin: {}, action: allowed",
			request.getRequestURL(), origin);
	}

	/**
	 * Override this method to customize the case where an Origin HTTP header was not present or did
	 * not match the request origin, and the corresponding action ({@link #noOriginAction} or
	 * {@link #conflictingOriginAction}) is set to {@code ALLOW}.
	 *
	 * @param request
	 *            the request
	 * @param origin
	 *            the contents of the {@code Origin} HTTP header, may be {@code null} or empty
	 * @param page
	 *            the page that is targeted with this request
	 */
	protected void onAllowed(HttpServletRequest request, String origin, IRequestablePage page)
	{
	}

	/**
	 * Handles the case where an Origin HTTP header was not present or did not match the request
	 * origin, and the corresponding action ({@link #noOriginAction} or
	 * {@link #conflictingOriginAction}) is set to {@code SUPPRESS}.
	 *
	 * @param request
	 *            the request
	 * @param origin
	 *            the contents of the {@code Origin} HTTP header, may be {@code null} or empty
	 * @param page
	 *            the page that is targeted with this request
	 */
	protected void suppressHandler(HttpServletRequest request, String origin,
		IRequestablePage page)
	{
		onSuppressed(request, origin, page);
		log.info("Possible CSRF attack, request URL: {}, Origin: {}, action: suppressed",
			request.getRequestURL(), origin);
		throw new RestartResponseException(page);
	}

	/**
	 * Override this method to customize the case where an Origin HTTP header was not present or did
	 * not match the request origin, and the corresponding action ({@link #noOriginAction} or
	 * {@link #conflictingOriginAction}) is set to {@code SUPPRESSED}.
	 *
	 * @param request
	 *            the request
	 * @param origin
	 *            the contents of the {@code Origin} HTTP header, may be {@code null} or empty
	 * @param page
	 *            the page that is targeted with this request
	 */
	protected void onSuppressed(HttpServletRequest request, String origin, IRequestablePage page)
	{
	}

	/**
	 * Handles the case where an Origin HTTP header was not present or did not match the request
	 * origin, and the corresponding action ({@link #noOriginAction} or
	 * {@link #conflictingOriginAction}) is set to {@code ABORT}.
	 *
	 * @param request
	 *            the request
	 * @param origin
	 *            the contents of the {@code Origin} HTTP header, may be {@code null} or empty
	 * @param page
	 *            the page that is targeted with this request
	 */
	protected void abortHandler(HttpServletRequest request, String origin,
		IRequestablePage page)
	{
		onAborted(request, origin, page);
		log.info(
			"Possible CSRF attack, request URL: {}, Origin: {}, action: aborted with error {} {}",
			request.getRequestURL(), origin, errorCode, errorMessage);
		throw new AbortWithHttpErrorCodeException(errorCode, errorMessage);
	}

	/**
	 * Override this method to customize the case where an Origin HTTP header was not present or did
	 * not match the request origin, and the corresponding action ({@link #noOriginAction} or
	 * {@link #conflictingOriginAction}) is set to {@code ABORTED}.
	 *
	 * @param request
	 *            the request
	 * @param origin
	 *            the contents of the {@code Origin} HTTP header, may be {@code null} or empty
	 * @param page
	 *            the page that is targeted with this request
	 */
	protected void onAborted(HttpServletRequest request, String origin, IRequestablePage page)
	{
	}
}
