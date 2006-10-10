/*
 * $Id: SharedResourceRequestTarget.java,v 1.3 2005/12/30 20:20:17 jonathanlocke
 * Exp $ $Revision$ $Date$
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
package wicket.request.target.resource;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Application;
import wicket.RequestCycle;
import wicket.Resource;
import wicket.Response;
import wicket.SharedResources;
import wicket.WicketRuntimeException;
import wicket.application.IClassResolver;
import wicket.markup.html.PackageResource;
import wicket.protocol.http.WebResponse;
import wicket.request.RequestParameters;

/**
 * Default implementation of {@link ISharedResourceRequestTarget}. Target that
 * denotes a shared {@link wicket.Resource}.
 * 
 * @author Eelco Hillenius
 */
public class SharedResourceRequestTarget implements ISharedResourceRequestTarget
{
	/** Logging object */
	private static final Log log = LogFactory.getLog(SharedResourceRequestTarget.class);

	private final RequestParameters requestParameters;

	/**
	 * Construct.
	 * 
	 * @param requestParameters
	 *            the request parameters
	 */
	public SharedResourceRequestTarget(RequestParameters requestParameters)
	{
		this.requestParameters = requestParameters;
		if (requestParameters == null)
		{
			throw new IllegalArgumentException("requestParameters may not be null");
		}
		else if (requestParameters.getResourceKey() == null)
		{
			throw new IllegalArgumentException("requestParameters.getResourceKey() "
					+ "may not be null");
		}
	}

	/**
	 * @see wicket.IRequestTarget#detach(wicket.RequestCycle)
	 */
	public void detach(RequestCycle requestCycle)
	{
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof SharedResourceRequestTarget)
		{
			SharedResourceRequestTarget that = (SharedResourceRequestTarget)obj;
			return getRequestParameters().getResourceKey().equals(
					that.getRequestParameters().getResourceKey());
		}
		return false;
	}

	/**
	 * @see wicket.IRequestTarget#getLock(RequestCycle)
	 */
	public Object getLock(RequestCycle requestCycle)
	{
		return null;
	}

	/**
	 * @see wicket.request.target.resource.ISharedResourceRequestTarget#getRequestParameters()
	 */
	public final RequestParameters getRequestParameters()
	{
		return requestParameters;
	}

	/**
	 * @see wicket.request.target.resource.ISharedResourceRequestTarget#getResourceKey()
	 */
	public final String getResourceKey()
	{
		return requestParameters.getResourceKey();
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		int result = "SharedResourceRequestTarget".hashCode();
		result += getRequestParameters().getResourceKey().hashCode();
		return 17 * result;
	}

	/**
	 * Respond by looking up the shared resource and delegating the actual
	 * response to that resource.
	 * 
	 * @see wicket.IRequestTarget#respond(wicket.RequestCycle)
	 */
	public void respond(RequestCycle requestCycle)
	{
		Application application = requestCycle.getApplication();
		SharedResources sharedResources = application.getSharedResources();
		final String resourceKey = getRequestParameters().getResourceKey();
		Resource resource = sharedResources.get(resourceKey);

		// try to lazily register
		if (resource == null)
		{
			int ix = resourceKey.indexOf('/');
			if (ix != -1)
			{
				String className = resourceKey.substring(0, ix);
				IClassResolver resolver = application.getApplicationSettings().getClassResolver();
				Class scope = null;
				try
				{
					scope = resolver.resolveClass(className);
					String path = resourceKey.substring(ix + 1);

					PackageResource packageResource = PackageResource.get(scope, path);
					if (sharedResources.get(resourceKey) == null)
					{
						sharedResources.add(resourceKey, packageResource);
					}
					resource = packageResource;
				}
				catch (Exception e)
				{
					// besides logging, ignore exception; after this an error
					// will be returned that the resource could not be retrieved
					log.error("unable to lazily register shared resource " + resourceKey
							+ ", exception=" + e.getMessage());
				}
			}
		}

		// if resource is still null, it doesn't exist
		if (resource == null)
		{
			Response response = requestCycle.getResponse();
			if (response instanceof WebResponse)
			{
				((WebResponse)response).getHttpServletResponse().setStatus(
						HttpServletResponse.SC_NOT_FOUND);
				log.error("shared resource " + resourceKey + " not found");
				return;
			}
			else
			{
				throw new WicketRuntimeException("shared resource " + resourceKey + " not found");
			}
		}

		// set request parameters if there are any
		if (requestParameters != null)
		{
			resource.setParameters(requestParameters.getParameters());
		}

		// let the resource handle the request
		resource.onResourceRequested();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "[SharedResourceRequestTarget@" + hashCode() + ", resourceKey="
				+ getRequestParameters().getResourceKey() + "]";
	}
}
