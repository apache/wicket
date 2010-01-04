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

import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.Application;
import org.apache.wicket.IRequestHandler;
import org.apache.wicket.Request;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.Session;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.protocol.http.request.InvalidUrlException;
import org.apache.wicket.protocol.http.request.WebRequestCodingStrategy;
import org.apache.wicket.protocol.http.servlet.AbortWithWebErrorCodeException;
import org.apache.wicket.request.AbstractRequestCycleProcessor;
import org.apache.wicket.request.IRequestCodingStrategy;
import org.apache.wicket.request.ObsoleteRequestParameters;
import org.apache.wicket.request.target.basic.EmptyAjaxRequestHandler;
import org.apache.wicket.request.target.component.BookmarkablePageRequestTarget;
import org.apache.wicket.util.string.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default request processor implementation for normal web applications.
 * 
 * @author eelcohillenius
 */
public class WebRequestCycleProcessor extends AbstractRequestCycleProcessor
{
	private static final Logger log = LoggerFactory.getLogger(WebRequestCycleProcessor.class);

	/**
	 * Construct.
	 */
	public WebRequestCycleProcessor()
	{
	}

	/**
	 * @see org.apache.wicket.request.IRequestCycleProcessor#resolve(org.apache.wicket.RequestCycle,
	 *      org.apache.wicket.request.ObsoleteRequestParameters)
	 */
	public IRequestHandler resolve(final RequestCycle requestCycle,
		final ObsoleteRequestParameters requestParameters)
	{
		try
		{
			IRequestCodingStrategy requestCodingStrategy = requestCycle.getProcessor()
				.getRequestCodingStrategy();

			final String path = requestParameters.getPath();
			IRequestHandler target = null;

			// See whether this request points to a bookmarkable page
			if (requestParameters.getBookmarkablePageClass() != null)
			{
				target = resolveBookmarkablePage(requestCycle, requestParameters);
			}
			// See whether this request points to a rendered page
			else if (requestParameters.getComponentPath() != null)
			{
				// marks whether or not we will be processing this request
				int processRequest = 0; // 0 == process, 1 == page expired, 2 == not active page
				// anymore
				if (processRequest == 0)
				{
					try
					{
						target = resolveRenderedPage(requestCycle, requestParameters);
					}
					catch (IgnoreAjaxRequestException e)
					{
						target = EmptyAjaxRequestHandler.getInstance();
					}
				}
				else
				{
					Request request = requestCycle.getRequest();
					if (request instanceof WebRequest && ((WebRequest)request).isAjax() &&
						processRequest == 2)
					{
						// if processRequest is false in an ajax request just have an empty ajax
						// target
						target = EmptyAjaxRequestHandler.getInstance();
					}
					else
					{
						throw new PageExpiredException("Request cannot be processed");
					}
				}
			}
			// See whether this request points to a shared resource
			else if (requestParameters.getResourceKey() != null)
			{
				target = resolveSharedResource(requestCycle, requestParameters);
			}
			// See whether this request points to the home page
			else if (Strings.isEmpty(path) || ("/".equals(path)))
			{
				target = resolveHomePageTarget(requestCycle, requestParameters);
			}

			// NOTE we are doing the mount check as the last item, so that it will
			// only be executed when everything else fails. This enables URLs like
			// /foo/bar/?wicket:bookmarkablePage=my.Page to be resolved, where
			// is either a valid mount or a non-valid mount. I (Eelco) am not
			// absolutely sure this is a great way to go, but it seems to have been
			// established as the default way of doing things. If we ever want to
			// tighten the algorithm up, it should be combined by going back to
			// unmounted paths so that requests with Wicket parameters like
			// 'bookmarkablePage' are always created and resolved in the same
			// fashion. There is a test for this in UrlMountingTest.
			if (target == null)
			{
				// still null? check for a mount
				target = requestCodingStrategy.targetForRequest(requestParameters);

				if (target == null && requestParameters.getComponentPath() != null)
				{
					// If the target is still null and there was a component path
					// then the Page could not be located in the session
					throw new PageExpiredException(
						"Cannot find the rendered page in session [pagemap=" +
							requestParameters.getPageMapName() + ",componentPath=" +
							requestParameters.getComponentPath() + ",versionNumber=" +
							requestParameters.getVersionNumber() + "]");
				}
			}
			else
			{
				// a target was found, but not by looking up a mount. check whether
				// this is allowed
				if (Application.get().getSecuritySettings().getEnforceMounts() &&
					requestCodingStrategy.pathForTarget(target) != null)
				{

					// we make an excepion if the homepage itself was mounted, see WICKET-1898
					boolean homepage = false;
					if (target instanceof BookmarkablePageRequestTarget)
					{
						final BookmarkablePageRequestTarget bt = (BookmarkablePageRequestTarget)target;
						if (bt.getPageClass().equals(Application.get().getHomePage()))
						{
							homepage = true;
						}
					}

					if (!homepage)
					{
						String msg = "Direct access not allowed for mounted targets";
						// the target was mounted, but we got here via another path
						// : deny the request
						log.error(msg + " [request=" + requestCycle.getRequest() + ",target=" +
							target + ",session=" + Session.get() + "]");
						throw new AbortWithWebErrorCodeException(HttpServletResponse.SC_FORBIDDEN,
							msg);
					}
				}
			}

			// (WICKET-1356) in case no target was found, return null here. RequestCycle will deal
			// with
			// it
			// possible letting wicket filter to pass the request down the filter chain
			/*
			 * if (target == null) { // if we get here, we have no recognized Wicket target, and
			 * thus // regard this as a external (non-wicket) resource request on // this server
			 * return resolveExternalResource(requestCycle); }
			 */

			return target;
		}
		catch (WicketRuntimeException e)
		{
			// we need to let page expired exception sift through instead of covering it up

			if (e instanceof PageExpiredException)
			{
				throw e;
			}
			else if (e.getCause() instanceof PageExpiredException)
			{
				throw e;
			}
			else
			{
				throw new InvalidUrlException(e);
			}
		}

	}

	/**
	 * @see org.apache.wicket.request.AbstractRequestCycleProcessor#newRequestCodingStrategy()
	 */
	@Override
	protected IRequestCodingStrategy newRequestCodingStrategy()
	{
		return new WebRequestCodingStrategy();
	}
}
