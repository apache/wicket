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
package org.apache.wicket;

import java.util.Map;

import org.apache.wicket.request.target.resource.ResourceStreamRequestTarget;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.time.Time;
import org.apache.wicket.util.value.IValueMap;
import org.apache.wicket.util.value.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A Resource is something that implements IResourceListener and provides a getResourceStream()
 * method which returns the raw IResourceStream to be rendered back to the client browser.
 * <p>
 * Resources themselves do not currently have URLs. Instead, they are referred to by components that
 * have URLs.
 * <p>
 * Resources can be shared throughout an application by adding them to Application with
 * addResource(Class scope, String name) or addResource(String name). A resource added in such a way
 * is a named resource and is accessible throughout the application via
 * Application.getResource(Class scope, String name) or Application.getResource(String name). The
 * ResourceReference class enables easy access to such resources in a way that is light on clusters.
 * <p>
 * While resources can be shared between components, it is important to emphasize that components
 * <i>cannot </i> be shared among containers. For example, you can create a button image resource
 * with new DefaultButtonImageResource(...) and store that in the Application with addResource().
 * You can then assign that logical resource via ResourceReference to several ImageButton
 * components. While the button image resource can be shared between components like this, the
 * ImageButton components in this example are like all other components in Wicket and cannot be
 * shared.
 * 
 * @see SharedResources
 * @author Jonathan Locke
 * @author Johan Compagner
 * @author Gili Tzabari
 * @author Igor Vaynberg
 */
public abstract class Resource implements IResourceListener
{
	private static final long serialVersionUID = 1L;

	/** Logger */
	private static final Logger log = LoggerFactory.getLogger(Resource.class);

	/** True if this resource can be cached */
	private boolean cacheable;

	/**
	 * ThreadLocal to keep any parameters associated with the request for this resource
	 */
	private static final ThreadLocal<IValueMap> parameters = new ThreadLocal<IValueMap>();

	/**
	 * Constructor
	 */
	protected Resource()
	{
		// By default all resources are cacheable
		cacheable = true;
	}

	/**
	 * @return Gets the resource to render to the requester
	 */
	public abstract IResourceStream getResourceStream();

	/**
	 * @return boolean True or False if this resource is cacheable
	 */
	public final boolean isCacheable()
	{
		return cacheable;
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL IT.
	 * 
	 * Called when a resource is requested.
	 */
	public final void onResourceRequested()
	{
		try
		{
			// Get request cycle
			final RequestCycle cycle = RequestCycle.get();

			// Fetch resource from subclass if necessary
			IResourceStream resourceStream = init();

			// Get servlet response to use when responding with resource
			final Response response = cycle.getResponse();

			// FIXME WICKET-385 Move HTTP caching features out of org.apache.wicket.Resource
			if (isCacheable())
			{
				response.setLastModifiedTime(resourceStream.lastModifiedTime());
			}
			else
			{
				response.setLastModifiedTime(Time.valueOf(-1));
			}
			configureResponse(response);

			cycle.setRequestTarget(new ResourceStreamRequestTarget(resourceStream));
		}
		finally
		{
			// Really really really make sure parameters are cleared
			parameters.set(null);
		}
	}

	/**
	 * Should this resource be cacheable, so will it set the last modified and the some cache
	 * headers in the response.
	 * 
	 * @param cacheable
	 *            boolean if the lastmodified and cache headers must be set.
	 * @return this
	 */
	public final Resource setCacheable(boolean cacheable)
	{
		this.cacheable = cacheable;
		return this;
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT USE IT!
	 * 
	 * @param parameters
	 *            Map of query parameters that parameterize this resource
	 */
	public final void setParameters(final Map<String, ?> parameters)
	{
		if (parameters == null)
		{
			Resource.parameters.set(null);
		}
		else
		{
			Resource.parameters.set(new ValueMap(parameters));
		}
	}

	/**
	 * Allows implementations to do configure the response, like setting headers etc.
	 * 
	 * @param response
	 *            the response
	 */
	protected void configureResponse(final Response response)
	{
	}

	/**
	 * @return Any query parameters associated with the request for this resource
	 */
	protected ValueMap getParameters()
	{
		if (parameters.get() == null)
		{
			return new ValueMap(RequestCycle.get()
				.getRequest()
				.getRequestParameters()
				.getParameters());
		}
		return (ValueMap)parameters.get();
	}

	/**
	 * Sets any loaded resource to null, thus forcing a reload on the next request.
	 */
	protected void invalidate()
	{
	}

	/**
	 * Set resource field by calling subclass
	 * 
	 * @return The resource stream for the current request
	 */
	private final IResourceStream init()
	{
		IResourceStream stream = getResourceStream();

		if (stream == null)
		{
			throw new WicketRuntimeException("Could not get resource stream");
		}
		return stream;
	}
}
