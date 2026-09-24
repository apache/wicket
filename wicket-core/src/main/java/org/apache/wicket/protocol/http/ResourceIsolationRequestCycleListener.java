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

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.wicket.MetaDataKey;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.core.request.handler.IPageRequestHandler;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.protocol.http.IResourceIsolationPolicy.RequestType;
import org.apache.wicket.protocol.http.IResourceIsolationPolicy.ResourceIsolationOutcome;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.IRequestHandlerDelegate;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.cycle.IRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.http.flow.AbortWithHttpErrorCodeException;
import org.apache.wicket.util.lang.Classes;
import org.apache.wicket.util.string.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This {@link RequestCycle} listener ensures resource isolation, adding a layer of protection for
 * modern browsers that prevent <em>Cross-Site Request Forgery</em> attacks.
 * <p>
 * It uses the {@link FetchMetadataResourceIsolationPolicy} and
 * {@link OriginResourceIsolationPolicy} by default and can be customized with additional
 * {@link IResourceIsolationPolicy}s.
 * <p>
 * URL paths that are intended to be used cross-site can be excempted from these policies.
 * <p>
 * Learn more about Fetch Metadata and resource isolation at
 * <a href="https://web.dev/fetch-metadata">https://web.dev/fetch-metadata/</a>
 *
 * @author Santiago Diaz - saldiaz@google.com
 * @author Ecenaz Jen Ozmen - ecenazo@google.com
 */
public class ResourceIsolationRequestCycleListener implements IRequestCycleListener
{
	private static final Logger log = LoggerFactory
		.getLogger(ResourceIsolationRequestCycleListener.class);

	public static final String ERROR_MESSAGE = "The request was blocked by a resource isolation policy";

	/**
	 * The action to perform when the outcome of the resource isolation policy is DISALLOWED or
	 * UNKNOWN. 
	 */
	public enum CsrfAction
	{
		/** Aborts the request and throws an exception when a CSRF request is detected. */
		ABORT {
			@Override
			public String toString()
			{
				return "aborted";
			}
			
			@Override
			void apply(ResourceIsolationRequestCycleListener listener, HttpServletRequest request,
				IRequestablePage page)
			{
				listener.abortHandler(request, page);
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
			
			@Override
			void apply(ResourceIsolationRequestCycleListener listener, HttpServletRequest request,
				IRequestablePage page)
			{
				listener.suppressHandler(request, page);
			}
		},

		/** Detects a CSRF request, logs it and allows the request to continue. */
		ALLOW {
			@Override
			public String toString()
			{
				return "allowed";
			}
			
			@Override
			void apply(ResourceIsolationRequestCycleListener listener, HttpServletRequest request,
				IRequestablePage page)
			{
				listener.allowHandler(request, page);
			}
		};

		abstract void apply(ResourceIsolationRequestCycleListener listener,
			HttpServletRequest request, IRequestablePage page);
	}

	/**
	 * Action to perform when no resource isolation policy can determine the validity of the
	 * request.
	 */
	private CsrfAction unknownOutcomeAction = CsrfAction.ABORT;

	/**
	 * Action to perform when {@link ResourceIsolationOutcome#DISALLOWED} is reported by a
	 * resource isolation policy.
	 */
	private CsrfAction disallowedOutcomeAction = CsrfAction.ABORT;

	/**
	 * The error code to report when the action to take for a CSRF request is
	 * {@link CsrfAction#ABORT}. Default {@code 403 FORBIDDEN}.
	 */
	private int errorCode = jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN;

	/**
	 * The error message to report when the action to take for a CSRF request is {@code ERROR}.
	 * Default {@code "The request was blocked by a resource isolation policy"}.
	 */
	private String errorMessage = ERROR_MESSAGE;

	private final Set<String> exemptedPaths = new HashSet<>();
	
	private final List<IResourceIsolationPolicy> resourceIsolationPolicies = new ArrayList<>();

	/**
	 * The key {@link #recordOrigin(HttpServletRequest)} records the request method under. Not a
	 * valid header name, so it cannot collide with one.
	 */
	protected static final String RECORDED_METHOD = ":method";

	/** The headers {@link #recordOrigin(HttpServletRequest)} records by default. */
	private static final List<String> RECORDED_HEADERS = asList(
		FetchMetadataResourceIsolationPolicy.SEC_FETCH_SITE_HEADER,
		FetchMetadataResourceIsolationPolicy.SEC_FETCH_MODE_HEADER,
		FetchMetadataResourceIsolationPolicy.SEC_FETCH_DEST_HEADER, WebRequest.HEADER_ORIGIN,
		WebRequest.HEADER_REFERER);

	/** What {@link #recordOrigin(HttpServletRequest)} recorded, carried with a destination. */
	private static final MetaDataKey<Map<String, String>> ORIGIN = new MetaDataKey<>()
	{
		private static final long serialVersionUID = 1L;
	};

	/**
	 * Create a new listener with the given policies. If no policies are given,
	 * {@link FetchMetadataResourceIsolationPolicy} and {@link OriginResourceIsolationPolicy} will
	 * be used. The policies are checked in order. The first outcome that's not
	 * {@link ResourceIsolationOutcome#UNKNOWN} will be used.
	 * 
	 * @param policies
	 *            the policies to check requests against.
	 */
	public ResourceIsolationRequestCycleListener(IResourceIsolationPolicy... policies)
	{
		this.resourceIsolationPolicies.addAll(asList(policies));
		if (policies.length == 0)
		{
			this.resourceIsolationPolicies.addAll(asList(new FetchMetadataResourceIsolationPolicy(),
				new OriginResourceIsolationPolicy()));
		}
	}

	/**
	 * Sets the action when none of the resource isolation policies can come to an outcome. Default
	 * {@code ABORT}.
	 *
	 * @param action
	 *            the alternate action
	 *
	 * @return this (for chaining)
	 */
	public ResourceIsolationRequestCycleListener setUnknownOutcomeAction(CsrfAction action)
	{
		this.unknownOutcomeAction = action;
		return this;
	}

	/**
	 * Sets the action when a request is disallowed by a resource isolation policy. Default is
	 * {@code ABORT}.
	 *
	 * @param action
	 *            the alternate action
	 *
	 * @return this
	 */
	public ResourceIsolationRequestCycleListener setDisallowedOutcomeAction(CsrfAction action)
	{
		this.disallowedOutcomeAction = action;
		return this;
	}

	/**
	 * Modifies the HTTP error code in the exception when a disallowed request is detected.
	 *
	 * @param errorCode
	 *            the alternate HTTP error code, default {@code 403 FORBIDDEN}
	 *
	 * @return this
	 */
	public ResourceIsolationRequestCycleListener setErrorCode(int errorCode)
	{
		this.errorCode = errorCode;
		return this;
	}

	/**
	 * Modifies the HTTP message in the exception when a disallowed request is detected.
	 *
	 * @param errorMessage
	 *            the alternate message
	 *
	 * @return this
	 */
	public ResourceIsolationRequestCycleListener setErrorMessage(String errorMessage)
	{
		this.errorMessage = errorMessage;
		return this;
	}

	public void addExemptedPaths(String... exemptions)
	{
		Arrays.stream(exemptions).filter(e -> !Strings.isEmpty(e)).forEach(exemptedPaths::add);
	}

	@Override
	public void onBeginRequest(RequestCycle cycle)
	{
		HttpServletRequest containerRequest = (HttpServletRequest)cycle.getRequest()
			.getContainerRequest();

		log.debug("Processing request to: {}", containerRequest.getPathInfo());

		if (isEnabled())
		{
			// Record where this request came from while that is still known. Should the application
			// interrupt this request and remember its destination to be resumed after a sign in,
			// the request that resumes it will have been redirected from inside this application
			// and will say so: it carries the origin that resumed it, not the origin that chose the
			// URL. This record is what lets that request be judged on the latter.
			RestartResponseAtInterceptPageException.setInterceptMetaData(ORIGIN,
				recordOrigin(containerRequest));
		}
	}

	/**
	 * Records what the isolation policies need in order to tell where a request came from. Called
	 * for every request, and remembered with the destination whenever a request is interrupted and
	 * its destination kept to be resumed later.
	 * <p>
	 * Override to record what a custom {@link IResourceIsolationPolicy} reads. Record an entry for
	 * every header consulted, with a {@code null} value where the header was absent: a name the
	 * record does not mention at all is answered by the request that resumes the destination, which
	 * is never what a header deciding provenance should fall back to. Keys are matched ignoring
	 * case, and the record is held in the session, so its values have to be serializable.
	 *
	 * @param request
	 *            the request being handled
	 * @return the recorded properties, keyed by header name, with the request method under
	 *         {@link #RECORDED_METHOD}
	 */
	protected Map<String, String> recordOrigin(HttpServletRequest request)
	{
		Map<String, String> origin = new HashMap<>();
		for (String header : RECORDED_HEADERS)
		{
			origin.put(header, request.getHeader(header));
		}
		origin.put(RECORDED_METHOD, request.getMethod());
		return origin;
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
	 * (form submits, link clicks, AJAX events) are checked.
	 *
	 * @param handler
	 *            the handler that is currently processing
	 * @return true when resource isolation should be checked for this {@code handler}
	 */
	protected boolean isChecked(IRequestHandler handler)
	{
		return handler instanceof IPageRequestHandler
			&& !(handler instanceof RenderPageRequestHandler);
	}

	/**
	 * Determines what the request does with the targeted page, so that the policies can distinguish
	 * a render - which may legitimately be the result of a top-level navigation from another site -
	 * from a listener invocation, which must never come from another site.
	 * <p>
	 * Note that {@link #isChecked(IRequestHandler)} excludes renders by default, so policies only
	 * see {@link RequestType#RENDER} when that method is overridden to extend resource isolation to
	 * page renders.
	 *
	 * @param handler
	 *            the handler that is currently processing
	 * @return the type of request
	 */
	protected RequestType getRequestType(IRequestHandler handler)
	{
		return handler instanceof RenderPageRequestHandler ? RequestType.RENDER
			: RequestType.LISTENER;
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
		if (isChecked(handler))
		{
			IPageRequestHandler pageRequestHandler = (IPageRequestHandler)handler;
			IRequestablePage targetedPage = pageRequestHandler.getPage();
			HttpServletRequest containerRequest = (HttpServletRequest)cycle.getRequest()
				.getContainerRequest();

			if (!isChecked(targetedPage))
			{
				if (log.isDebugEnabled())
				{
					log.debug("Targeted page {} was opted out of resource isolation, allowed",
						targetedPage.getClass().getName());
				}
				return;
			}

			String pathInfo = containerRequest.getPathInfo();
			if (exemptedPaths.contains(pathInfo))
			{
				if (log.isDebugEnabled())
				{
					log.debug("Allowing request to {} because it matches an exempted path",
						new Object[] { pathInfo });
				}
				return;
			}

			RequestType requestType = getRequestType(handler);

			ResourceIsolationOutcome outcome = outcomeFor(containerRequest, targetedPage,
				requestType);

			Map<String, String> origin = RestartResponseAtInterceptPageException
				.getInterceptMetaData(ORIGIN);
			if (origin != null)
			{
				// This request resumes a destination that an earlier request chose. It carries the
				// origin that resumed it - this application's own, because signing in redirects
				// within it - so its own headers say nothing about where the URL came from. Judge
				// the request that did choose it as well, and keep the stricter of the two.
				outcome = stricter(outcome,
					outcomeFor(new RecordedOriginRequest(containerRequest, origin), targetedPage,
						requestType));
			}

			if (ResourceIsolationOutcome.DISALLOWED.equals(outcome))
			{
				disallowedOutcomeAction.apply(this, containerRequest, targetedPage);
			}
			else if (ResourceIsolationOutcome.UNKNOWN.equals(outcome))
			{
				unknownOutcomeAction.apply(this, containerRequest, targetedPage);
			}
		}
		else
		{
			if (log.isTraceEnabled())
				log.trace("Resolved handler {} is not checked, no CSRF check performed",
					handler.getClass().getName());
		}
	}

	/**
	 * Allow isolation policy to add headers.
	 * 
	 * @see IResourceIsolationPolicy#setHeaders(HttpServletResponse)
	 */
	@Override
	public void onEndRequest(RequestCycle cycle)
	{
		if (cycle.getResponse() instanceof WebResponse)
		{
			WebResponse webResponse = (WebResponse)cycle.getResponse();
			if (webResponse.isHeaderSupported())
			{
				for (IResourceIsolationPolicy resourceIsolationPolicy : resourceIsolationPolicies)
				{
					resourceIsolationPolicy
						.setHeaders((HttpServletResponse)webResponse.getContainerResponse());
				}
			}
		}
	}

	/**
	 * Allow the execution of the listener in the request because the outcome results in
	 * {@link CsrfAction#ALLOW}.
	 *
	 * @param request
	 *            the request
	 * @param page
	 *            the page that is targeted with this request
	 */
	protected void allowHandler(HttpServletRequest request, IRequestablePage page)
	{
		log.info("Possible CSRF attack, request URL: {}, action: allowed", request.getRequestURL());
	}

	/**
	 * Suppress the execution of the listener in the request because the outcome results in
	 * {@link CsrfAction#SUPPRESS}.
	 *
	 * @param request
	 *            the request
	 * @param page
	 *            the page that is targeted with this request
	 */
	protected void suppressHandler(HttpServletRequest request, IRequestablePage page)
	{
		log.info("Possible CSRF attack, request URL: {}, action: suppressed",
			request.getRequestURL());
		throw new RestartResponseException(page);
	}

	/**
	 * Abort the request because the outcome results in {@link CsrfAction#ABORT}.
	 *
	 * @param request
	 *            the request
	 * @param page
	 *            the page that is targeted with this request
	 */
	protected void abortHandler(HttpServletRequest request, IRequestablePage page)
	{
		log.info("Possible CSRF attack, request URL: {}, action: aborted with error {} {}",
			request.getRequestURL(), errorCode, errorMessage);
		throw new AbortWithHttpErrorCodeException(errorCode, errorMessage);
	}

	/**
	 * The outcome the policies reach for {@code request}: the first policy that does not answer
	 * {@link ResourceIsolationOutcome#UNKNOWN} decides.
	 */
	private ResourceIsolationOutcome outcomeFor(HttpServletRequest request,
		IRequestablePage targetedPage, RequestType requestType)
	{
		for (IResourceIsolationPolicy policy : resourceIsolationPolicies)
		{
			ResourceIsolationOutcome outcome = policy.isRequestAllowed(request, targetedPage,
				requestType);
			if (ResourceIsolationOutcome.DISALLOWED.equals(outcome))
			{
				log.debug("Isolation policy {} has rejected a request to {}",
					Classes.simpleName(policy.getClass()), request.getPathInfo());
				return outcome;
			}
			else if (ResourceIsolationOutcome.ALLOWED.equals(outcome))
			{
				return outcome;
			}
		}
		return ResourceIsolationOutcome.UNKNOWN;
	}

	private static ResourceIsolationOutcome stricter(ResourceIsolationOutcome one,
		ResourceIsolationOutcome other)
	{
		if (ResourceIsolationOutcome.DISALLOWED.equals(one) ||
			ResourceIsolationOutcome.DISALLOWED.equals(other))
		{
			return ResourceIsolationOutcome.DISALLOWED;
		}
		if (ResourceIsolationOutcome.UNKNOWN.equals(one) ||
			ResourceIsolationOutcome.UNKNOWN.equals(other))
		{
			return ResourceIsolationOutcome.UNKNOWN;
		}
		return ResourceIsolationOutcome.ALLOWED;
	}

	private static IRequestHandler unwrap(IRequestHandler handler)
	{
		while (handler instanceof IRequestHandlerDelegate)
		{
			handler = ((IRequestHandlerDelegate)handler).getDelegateHandler();
		}
		return handler;
	}

	/**
	 * Presents the request that was interrupted, so that the policies judge the origin that chose a
	 * resumed URL rather than the origin that resumed it.
	 * <p>
	 * Whatever the record mentions is answered from the record alone, its absences included: a
	 * resumed request always carries this application's own origin, so falling through to it would
	 * hand back the very answer this class exists to avoid. Everything else is answered by the
	 * resuming request, whose target scheme, host and port are the same ones the interrupted
	 * request was compared against.
	 */
	private static class RecordedOriginRequest extends HttpServletRequestWrapper
	{
		private final Map<String, String> origin;

		private RecordedOriginRequest(HttpServletRequest request, Map<String, String> origin)
		{
			super(request);

			// header names are matched ignoring case, whatever recordOrigin() returned
			this.origin = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			this.origin.putAll(origin);
		}

		@Override
		public String getMethod()
		{
			return origin.containsKey(RECORDED_METHOD) ? origin.get(RECORDED_METHOD)
				: super.getMethod();
		}

		@Override
		public String getHeader(String name)
		{
			return origin.containsKey(name) ? origin.get(name) : super.getHeader(name);
		}

		@Override
		public Enumeration<String> getHeaders(String name)
		{
			if (origin.containsKey(name))
			{
				String value = origin.get(name);
				return Collections.enumeration(
					value == null ? Collections.emptyList() : Collections.singletonList(value));
			}
			return super.getHeaders(name);
		}

		@Override
		public Enumeration<String> getHeaderNames()
		{
			Set<String> names = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
			for (Enumeration<String> live = super.getHeaderNames(); live.hasMoreElements();)
			{
				String name = live.nextElement();
				if (origin.containsKey(name) == false)
				{
					names.add(name);
				}
			}
			for (Map.Entry<String, String> recorded : origin.entrySet())
			{
				if (recorded.getValue() != null && RECORDED_METHOD.equals(recorded.getKey()) == false)
				{
					names.add(recorded.getKey());
				}
			}
			return Collections.enumeration(names);
		}
	}
}
