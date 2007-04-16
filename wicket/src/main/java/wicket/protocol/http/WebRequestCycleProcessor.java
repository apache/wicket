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
package wicket.protocol.http;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import wicket.Application;
import wicket.IRequestTarget;
import wicket.Page;
import wicket.PageParameters;
import wicket.RequestCycle;
import wicket.Session;
import wicket.protocol.http.request.WebRequestCodingStrategy;
import wicket.protocol.http.servlet.AbortWithWebErrorCodeException;
import wicket.request.AbstractRequestCycleProcessor;
import wicket.request.IRequestCodingStrategy;
import wicket.request.RequestParameters;
import wicket.request.target.component.BookmarkableListenerInterfaceRequestTarget;
import wicket.request.target.component.IBookmarkablePageRequestTarget;
import wicket.util.string.Strings;

/**
 * Default request processor implementation for normal web applications.
 * 
 * @author eelcohillenius
 */
public class WebRequestCycleProcessor extends AbstractRequestCycleProcessor
{
	/** log. */
	private static final Logger log = LoggerFactory.getLogger(AbstractRequestCycleProcessor.class);

	/**
	 * Construct.
	 */
	public WebRequestCycleProcessor()
	{
	}

	/**
	 * @see wicket.request.IRequestCycleProcessor#resolve(wicket.RequestCycle,
	 *      wicket.request.RequestParameters)
	 */
	public IRequestTarget resolve(final RequestCycle requestCycle,
			final RequestParameters requestParameters)
	{
		// first, see whether we can find any mount
		IRequestCodingStrategy requestCodingStrategy = requestCycle.getProcessor()
				.getRequestCodingStrategy();
		IRequestTarget mounted = requestCodingStrategy.targetForRequest(requestParameters);
		if (mounted != null)
		{
			if (mounted instanceof IBookmarkablePageRequestTarget)
			{
				IBookmarkablePageRequestTarget bookmarkableTarget = (IBookmarkablePageRequestTarget)mounted;
				// the path was mounted, so return that directly
				if (requestParameters.getComponentPath() != null
						&& requestParameters.getInterfaceName() != null)
				{
					final String componentPath = requestParameters.getComponentPath();
					final Page<?> page = Session.get().getPage(requestParameters.getPageMapName(),
							componentPath, requestParameters.getVersionNumber());

					if (page != null && page.getClass() == bookmarkableTarget.getPageClass())
					{
						return resolveListenerInterfaceTarget(requestCycle, page, componentPath,
								requestParameters.getInterfaceName(), requestParameters);
					}
					else
					{
						PageParameters params = new PageParameters(requestParameters
								.getParameters());
						return new BookmarkableListenerInterfaceRequestTarget(requestParameters
								.getPageMapName(), bookmarkableTarget.getPageClass(), params,
								requestParameters.getComponentPath(), requestParameters
										.getInterfaceName());
					}
				}
			}
			return mounted;
		}

		IRequestTarget target = null;
		final String path = requestParameters.getPath();

		// see whether this request points to a bookmarkable page
		if (requestParameters.getBookmarkablePageClass() != null)
		{
			target = resolveBookmarkablePage(requestCycle, requestParameters);
		}
		// See whether this request points to a rendered page
		else if (requestParameters.getComponentPath() != null)
		{
			// TODO in 1.2/1.3 this looks completely different!!
			// checks for: requestParameters.isOnlyProcessIfPathActive()
			target = resolveRenderedPage(requestCycle, requestParameters);
		}
		// see whether this request points to a shared resource
		else if (requestParameters.getResourceKey() != null)
		{
			target = resolveSharedResource(requestCycle, requestParameters);
		}
		// see whether this request points to the home page
		else if (Strings.isEmpty(path) || ("/".equals(path)))
		{
			target = resolveHomePageTarget(requestCycle, requestParameters);
		}

		if (target != null)
		{
			if (Application.get().getSecuritySettings().getEnforceMounts()
					&& requestCodingStrategy.pathForTarget(target) != null)
			{
				String msg = "Direct access not allowed for mounted targets";
				// the target was mounted, but we got here via another path
				// : deny the request
				log.error(msg + " [request=" + requestCycle.getRequest() + ",target=" + target
						+ ",session=" + Session.get() + "]");
				throw new AbortWithWebErrorCodeException(HttpServletResponse.SC_FORBIDDEN, msg);
			}
			return target;
		}
		else
		{
			// if we get here, we have no regconized Wicket target, and thus
			// regard this as a external (non-wicket) resource request on
			// this server
			return resolveExternalResource(requestCycle);
		}
	}


	/**
	 * @see wicket.request.AbstractRequestCycleProcessor#newRequestCodingStrategy()
	 */
	@Override
	protected IRequestCodingStrategy newRequestCodingStrategy()
	{
		return new WebRequestCodingStrategy();
	}

}
