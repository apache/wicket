/*
 * $Id: DefaultRequestTargetResolverStrategy.java,v 1.4 2005/12/30 21:47:05
 * jonathanlocke Exp $ $Revision$ $Date: 2006-03-21 02:33:42 +0100 (di,
 * 21 mrt 2006) $
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.request.compound;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.IRequestTarget;
import wicket.RequestCycle;
import wicket.protocol.http.request.WebExternalResourceRequestTarget;
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
		IRequestTarget mounted = requestCycle.getProcessor().getRequestCodingStrategy()
				.targetForRequest(requestParameters);
		if (mounted != null)
		{
			// the path was mounted, so return that directly
			return mounted;
		}

		final String path = requestParameters.getPath();

		// see whether this request points to a bookmarkable page
		if (requestParameters.getBookmarkablePageClass() != null)
		{
			return resolveBookmarkablePage(requestCycle, requestParameters);
		}
		// See whether this request points to a rendered page
		else if (requestParameters.getComponentPath() != null)
		{
			return resolveRenderedPage(requestCycle, requestParameters);
		}
		// see whether this request points to a shared resource
		else if (requestParameters.getResourceKey() != null)
		{
			return resolveSharedResource(requestCycle, requestParameters);
		}
		// see whether this request points to the home page
		else if (Strings.isEmpty(path) || ("/".equals(path)))
		{
			return resolveHomePageTarget(requestCycle, requestParameters);
		}

		// if we get here, we have no regconized Wicket target, and thus
		// regard this as a external (non-wicket) resource request on
		// this server
		return resolveExternalResource(requestCycle);
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
}
