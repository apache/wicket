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
import static org.apache.wicket.protocol.http.ResourceIsolationPolicy.SEC_FETCH_DEST_HEADER;
import static org.apache.wicket.protocol.http.ResourceIsolationPolicy.SEC_FETCH_MODE_HEADER;
import static org.apache.wicket.protocol.http.ResourceIsolationPolicy.SEC_FETCH_SITE_HEADER;
import static org.apache.wicket.protocol.http.ResourceIsolationPolicy.VARY_HEADER;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.apache.wicket.core.request.handler.IPageRequestHandler;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
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
 * The Fetch Metadata Request Cycle Listener is Wicket's implementation of Fetch Metadata. This adds
 * a layer of protection for modern browsers that prevents Cross-Site Request Forgery attacks.
 *
 * This request listener uses the {@link DefaultResourceIsolationPolicy} by default and can be
 * customized with additional Resource Isolation Policies.
 *
 * This listener can be configured to add exempted URL paths that are intended to be used
 * cross-site.
 *
 * Learn more about Fetch Metadata and resource isolation at
 * <a href="https://web.dev/fetch-metadata/">https://web.dev/fetch-metadata/</a>
 *
 * @author Santiago Diaz - saldiaz@google.com
 * @author Ecenaz Jen Ozmen - ecenazo@google.com
 */
public class FetchMetadataRequestCycleListener implements IRequestCycleListener
{

	private static final Logger log = LoggerFactory
		.getLogger(FetchMetadataRequestCycleListener.class);
	public static final int ERROR_CODE = 403;
	public static final String ERROR_MESSAGE = "Forbidden";
	public static final String VARY_HEADER_VALUE = SEC_FETCH_DEST_HEADER + ", "
		+ SEC_FETCH_SITE_HEADER + ", " + SEC_FETCH_MODE_HEADER;

	private final Set<String> exemptedPaths = new HashSet<>();
	private final List<ResourceIsolationPolicy> resourceIsolationPolicies = new ArrayList<>();

	public FetchMetadataRequestCycleListener(ResourceIsolationPolicy... additionalPolicies)
	{
		this.resourceIsolationPolicies.addAll(
			asList(new DefaultResourceIsolationPolicy(), new OriginBasedResourceIsolationPolicy()));

		this.resourceIsolationPolicies.addAll(asList(additionalPolicies));
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

	@Override
	public void onRequestHandlerResolved(RequestCycle cycle, IRequestHandler handler)
	{
		handler = unwrap(handler);
		IPageRequestHandler pageRequestHandler = getPageRequestHandler(handler);
		if (pageRequestHandler == null)
		{
			return;
		}

		IRequestablePage targetedPage = pageRequestHandler.getPage();
		HttpServletRequest containerRequest = (HttpServletRequest)cycle.getRequest()
			.getContainerRequest();

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

		for (ResourceIsolationPolicy resourceIsolationPolicy : resourceIsolationPolicies)
		{
			if (!resourceIsolationPolicy.isRequestAllowed(containerRequest, targetedPage))
			{
				log.debug("Isolation policy {} has rejected a request to {}",
					Classes.simpleName(resourceIsolationPolicy.getClass()), pathInfo);
				throw new AbortWithHttpErrorCodeException(ERROR_CODE, ERROR_MESSAGE);
			}
		}
	}

	@Override
	public void onEndRequest(RequestCycle cycle)
	{
		// set vary headers to avoid caching responses processed by Fetch Metadata
		// caching these responses may return 403 responses to legitimate requests
		// or defeat the protection
		if (cycle.getResponse() instanceof WebResponse)
		{
			WebResponse webResponse = (WebResponse)cycle.getResponse();
			if (webResponse.isHeaderSupported())
			{
				webResponse.addHeader(VARY_HEADER, VARY_HEADER_VALUE);
			}
		}
	}

	private static IRequestHandler unwrap(IRequestHandler handler)
	{
		while (handler instanceof IRequestHandlerDelegate)
		{
			handler = ((IRequestHandlerDelegate)handler).getDelegateHandler();
		}
		return handler;
	}

	private IPageRequestHandler getPageRequestHandler(IRequestHandler handler)
	{
		boolean isPageRequestHandler = handler instanceof IPageRequestHandler
			&& !(handler instanceof RenderPageRequestHandler);
		return isPageRequestHandler ? (IPageRequestHandler)handler : null;
	}
}
