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

import java.util.Locale;
import java.util.Map;

import org.apache.wicket.ng.request.IRequestParameters;
import org.apache.wicket.ng.request.Url;
import org.apache.wicket.ng.request.parameter.CombinedRequestParametersAdapter;
import org.apache.wicket.ng.request.parameter.EmptyRequestParameters;
import org.apache.wicket.ng.request.parameter.UrlRequestParametersAdapter;
import org.apache.wicket.request.IRequestCodingStrategy;
import org.apache.wicket.request.IRequestCycleProcessor;
import org.apache.wicket.request.ObsoleteRequestParameters;


/**
 * Base class for page request implementations allowing access to request parameters. A Request has
 * a URL and a parameter map. You can retrieve the URL of the request with getURL(). The entire
 * parameter map can be retrieved via getParameterMap(). Individual parameters can be retrieved via
 * getParameter(String). If multiple values are available for a given parameter, they can be
 * retrieved via getParameters(String).
 * 
 * @author Jonathan Locke
 */
public abstract class Request
{
	/** the type safe request parameters object for this request. */
	private ObsoleteRequestParameters requestParameters;

	/**
	 * Construct.
	 */
	public Request()
	{
	}

	/**
	 * An implementation of this method is only required if a subclass wishes to support sessions
	 * via URL rewriting. This default implementation simply returns the URL String it is passed.
	 * 
	 * @param url
	 *            The URL to decode
	 * @return The decoded url
	 */
	public Url decodeURL(final Url url)
	{
		return url;
	}

	/**
	 * @return The locale for this request
	 */
	public abstract Locale getLocale();

	/**
	 * Gets a given (query) parameter by name.
	 * 
	 * @param key
	 *            Parameter name
	 * @return Parameter value
	 */
	public abstract String getParameter(final String key);

	/**
	 * Gets a map of (query) parameters sent with the request.
	 * 
	 * @return Map of parameters
	 */
	public abstract Map<String, String[]> getParameterMap();

	/**
	 * Gets an array of multiple parameters by name.
	 * 
	 * @param key
	 *            Parameter name
	 * @return Parameter values
	 */
	public abstract String[] getParameters(final String key);

	/**
	 * @return Path info for request
	 */
	public abstract String getPath();

	/**
	 * Gets a prefix to make this relative to the context root.
	 * <p>
	 * For example, if your context root is http://server.com/myApp/ and the request is for
	 * /myApp/mountedPage/, then the prefix returned might be "../../".
	 * <p>
	 * For a particular technology, this might return either an absolute prefix or a relative one.
	 * 
	 * @return Prefix relative to this request required to back up to context root.
	 */
	public abstract String getRelativePathPrefixToContextRoot();

	/**
	 * Gets a prefix to make this relative to the Wicket Servlet/Filter.
	 * <p>
	 * For example, if your context root is http://server.com/myApp/ and the request is for
	 * /myApp/mountedPage/, then the prefix returned might be "../".
	 * <p>
	 * For a particular technology, this might return either an absolute prefix or a relative one.
	 * 
	 * @return Prefix relative to this request required to back up to context root.
	 */
	public abstract String getRelativePathPrefixToWicketHandler();


	/**
	 * Sets request parameters. Should only be used when one request is being created as a
	 * replacement for another.
	 * 
	 * @param requestParameters
	 * @deprecated wicket-ng
	 */
	@Deprecated
	public final void setObsoleteRequestParameters(ObsoleteRequestParameters requestParameters)
	{
		this.requestParameters = requestParameters;
	}

	/**
	 * Gets the request parameters object using the instance of {@link IRequestCodingStrategy} of
	 * the provided request cycle processor.
	 * 
	 * @return the request parameters object
	 * @deprecated wicket-ng
	 */
	@Deprecated
	public final ObsoleteRequestParameters getObsoleteRequestParameters()
	{
		// reused cached parameters
		if (requestParameters != null)
		{
			return requestParameters;
		}

		// get the request encoder to decode the request parameters
		IRequestCycleProcessor processor = RequestCycle.get().getProcessor();
		final IRequestCodingStrategy encoder = processor.getRequestCodingStrategy();
		if (encoder == null)
		{
			throw new WicketRuntimeException("request encoder must be not-null (provided by " +
				processor + ")");
		}

		// decode the request parameters into a strongly typed parameters
		// object that is to be used by the target resolving
		try
		{
			requestParameters = encoder.decode(this);
		}
		catch (RuntimeException re)
		{
			// do set the parameters as it was parsed.
			// else the error page will also error again (infinite loop)
			requestParameters = new ObsoleteRequestParameters();
			throw re;
		}

		if (requestParameters == null)
		{
			throw new WicketRuntimeException("request parameters must be not-null (provided by " +
				encoder + ")");
		}
		return requestParameters;
	}

	/**
	 * Retrieves the relative URL of this request for local use. This is relative to the context
	 * root.
	 * 
	 * @return The relative request URL for local use
	 */
	public abstract Url getUrl();

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "Request[url=" + getUrl() + "]";
	}

	/**
	 * Returns the query string (part after ?) of this request.
	 * 
	 * @return request query string
	 */
	public abstract String getQueryString();

	/**
	 * @return POST request parameters for this request.
	 */
	public IRequestParameters getPostRequestParameters()
	{
		return EmptyRequestParameters.INSTANCE;
	}

	/**
	 * @return GET request parameters for this request.
	 */
	public IRequestParameters getGetRequestParameters()
	{
		return new UrlRequestParametersAdapter(getUrl());
	}

	/**
	 * @return all request parameters for this request (both POST and GET parameters)
	 */
	public IRequestParameters getRequestParameters()
	{
		return new CombinedRequestParametersAdapter(getGetRequestParameters(),
			getPostRequestParameters());
	}

	/**
	 * Returns request with specified URL and same POST parameters as this request.
	 * 
	 * @param url
	 *            Url instance
	 * @return request with specified URL.
	 */
	public Request requestWithUrl(final Url url)
	{
		final Request delegate = this;

		return new Request()
		{
			@Override
			public Url getUrl()
			{
				return url;
			}

			@Override
			public IRequestParameters getPostRequestParameters()
			{
				return delegate.getPostRequestParameters();
			}

			@Override
			public Locale getLocale()
			{
				return delegate.getLocale();
			}

			@Override
			public String getParameter(String key)
			{
				return delegate.getParameter(key);
			}

			@Override
			public Map<String, String[]> getParameterMap()
			{
				return delegate.getParameterMap();
			}

			@Override
			public String[] getParameters(String key)
			{
				return delegate.getParameters(key);
			}

			@Override
			public String getPath()
			{
				return delegate.getPath();
			}

			@Override
			public String getQueryString()
			{
				return delegate.getQueryString();
			}

			@Override
			public String getRelativePathPrefixToContextRoot()
			{
				return delegate.getRelativePathPrefixToContextRoot();
			}

			@Override
			public String getRelativePathPrefixToWicketHandler()
			{
				return delegate.getRelativePathPrefixToWicketHandler();
			}
		};
	}
}