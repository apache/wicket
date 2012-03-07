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
package org.apache.wicket.request.target.resource;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.Application;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.Resource;
import org.apache.wicket.Response;
import org.apache.wicket.SharedResources;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.application.IClassResolver;
import org.apache.wicket.markup.html.PackageResource;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.request.RequestParameters;
import org.apache.wicket.util.string.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Default implementation of {@link ISharedResourceRequestTarget}. Target that
 * denotes a shared {@link org.apache.wicket.Resource}.
 * 
 * @author Eelco Hillenius
 */
public class SharedResourceRequestTarget implements ISharedResourceRequestTarget
{
	/** Logging object */
	private static final Logger log = LoggerFactory.getLogger(SharedResourceRequestTarget.class);

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
	 * @see org.apache.wicket.IRequestTarget#detach(org.apache.wicket.RequestCycle)
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
	 * @see org.apache.wicket.request.target.resource.ISharedResourceRequestTarget#getRequestParameters()
	 */
	public final RequestParameters getRequestParameters()
	{
		return requestParameters;
	}

	/**
	 * @see org.apache.wicket.request.target.resource.ISharedResourceRequestTarget#getResourceKey()
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
	 * @see org.apache.wicket.IRequestTarget#respond(org.apache.wicket.RequestCycle)
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
				Class<?> scope = null;
				try
				{
					// First try to match mounted resources.
					scope = Application.get().getSharedResources().getAliasClass(className);

					// If that fails, resolve it as a fully qualified class
					// name.
					if (scope == null)
					{
						scope = resolver.resolveClass(className);
						
						if(scope.getPackage() == null) // do not accept default package as scope
						{
							scope = null;
						}
					}

					// get path component of resource key, replace '..' with
					// escape sequence to
					// prevent crippled urls in browser
					final CharSequence escapeString = application.getResourceSettings()
							.getParentFolderPlaceholder();

					String path = resourceKey.substring(ix + 1);
					if (Strings.isEmpty(escapeString) == false)
					{
						path = path.replace(escapeString, "..");
					}

					if (PackageResource.exists(scope, path, null, null))
					{
						resource = PackageResource.get(scope, path);
					}
				}
				catch (Exception e)
				{
					// besides logging, ignore exception; after this an error
					// will be returned that the resource could not be retrieved
					log.error("unable to lazily register shared resource " + resourceKey, e);
				}
			}
		}

		// if resource is still null, it doesn't exist
		if (resource == null)
		{
			String msg = "shared resource " + resourceKey + " not found or not allowed access";
			log.info(msg);

			Response response = requestCycle.getResponse();
			if (response instanceof WebResponse)
			{
				try
				{
					((WebResponse)response).getHttpServletResponse().sendError(
							HttpServletResponse.SC_NOT_FOUND);
				}
				catch (IOException e)
				{
					throw new WicketRuntimeException("Error sending 404 error to client", e);
				}
				return;
			}
			else
			{
				throw new WicketRuntimeException(msg);
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
