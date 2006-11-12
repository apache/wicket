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
package wicket.request.compound;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Application;
import wicket.IRequestTarget;
import wicket.RequestCycle;
import wicket.Session;
import wicket.protocol.http.request.WebErrorCodeResponseTarget;
import wicket.protocol.http.request.WebExternalResourceRequestTarget;
import wicket.request.IRequestCodingStrategy;
import wicket.request.RequestParameters;
import wicket.request.target.resource.SharedResourceRequestTarget;
import wicket.util.string.Strings;

/**
 * Default target resolver strategy. It tries to lookup any registered mount
 * with {@link wicket.request.IRequestCodingStrategy} and in case no mount was
 * found, it uses the {@link wicket.request.RequestParameters} object for
 * default resolving.
 * 
 * @author Eelco Hillenius
 * @author Igor Vaynberg
 * @author Jonathan Locke
 */
public class DefaultRequestTargetResolverStrategy extends AbstractRequestTargetResolverStrategy
{
	/** log. */
	private static final Log log = LogFactory.getLog(DefaultRequestTargetResolverStrategy.class);

	/**
	 * Construct.
	 */
	public DefaultRequestTargetResolverStrategy()
	{
	}

	/**
	 * @see wicket.request.compound.IRequestTargetResolverStrategy#resolve(wicket.RequestCycle,
	 *      wicket.request.RequestParameters)
	 */
	public final IRequestTarget resolve(final RequestCycle requestCycle,
			final RequestParameters requestParameters)
	{
		// first, see whether we can find any mount
		IRequestCodingStrategy requestCodingStrategy = requestCycle.getProcessor()
				.getRequestCodingStrategy();
		IRequestTarget mounted = requestCodingStrategy.targetForRequest(requestParameters);
		if (mounted != null)
		{
			// the path was mounted, so return that directly
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
				target = new WebErrorCodeResponseTarget(HttpServletResponse.SC_FORBIDDEN, msg);
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
	 * Resolves to an external resource.
	 * 
	 * @param requestCycle
	 *            The current request cycle
	 * @return The external resource request target
	 */
	protected IRequestTarget resolveExternalResource(RequestCycle requestCycle)
	{
		// Get the relative URL we need for loading the resource from
		// the servlet context
		// NOTE: we NEED to put the '/' in front as otherwise some versions
		// of application servers (e.g. Jetty 5.1.x) will fail for requests
		// like '/mysubdir/myfile.css'
		final String url = '/' + requestCycle.getRequest().getRelativeURL();
		return new WebExternalResourceRequestTarget(url);
	}

	/**
	 * Resolves to a shared resource target.
	 * 
	 * @param requestCycle
	 *            the current request cycle
	 * @param requestParameters
	 *            the request parameters object
	 * @return the shared resource as a request target
	 */
	protected IRequestTarget resolveSharedResource(final RequestCycle requestCycle,
			final RequestParameters requestParameters)
	{
		return new SharedResourceRequestTarget(requestParameters);
	}
}
