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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.core.request.handler.IPageRequestHandler;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.protocol.http.IResourceIsolationPolicy.ResourceIsolationOutcome;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.IRequestHandlerDelegate;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.cycle.IRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
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
	private int errorCode = javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;

	/**
	 * The error message to report when the action to take for a CSRF request is {@code ERROR}.
	 * Default {@code "The request was blocked by a resource isolation policy"}.
	 */
	private String errorMessage = ERROR_MESSAGE;

	private final Set<String> exemptedPaths = new HashSet<>();
	
	private final List<IResourceIsolationPolicy> resourceIsolationPolicies = new ArrayList<>();

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

			for (IResourceIsolationPolicy policy : resourceIsolationPolicies)
			{
				ResourceIsolationOutcome outcome = policy
					.isRequestAllowed(containerRequest, targetedPage);
				if (ResourceIsolationOutcome.DISALLOWED.equals(outcome))
				{
					log.debug("Isolation policy {} has rejected a request to {}",
						Classes.simpleName(policy.getClass()), pathInfo);
					disallowedOutcomeAction.apply(this, containerRequest, targetedPage);
					return;
				}
				else if (ResourceIsolationOutcome.ALLOWED.equals(outcome))
				{
					return;
				}
			}
			unknownOutcomeAction.apply(this, containerRequest, targetedPage);
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

	private static IRequestHandler unwrap(IRequestHandler handler)
	{
		while (handler instanceof IRequestHandlerDelegate)
		{
			handler = ((IRequestHandlerDelegate)handler).getDelegateHandler();
		}
		return handler;
	}
}
