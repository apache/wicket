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
package org.apache.wicket.ng.request;

import org.apache.wicket.ng.request.parameter.CombinedRequestParametersAdapter;
import org.apache.wicket.ng.request.parameter.EmptyRequestParameters;
import org.apache.wicket.ng.request.parameter.UrlRequestParametersAdapter;

/**
 * Request object.
 * 
 * @author Matej Knopp
 */
public abstract class Request
{
	/**
	 * Returns the URL for this request. URL is relative to Wicket filter path.
	 * 
	 * @return Url instance
	 */
	public abstract Url getUrl();

	/**
	 * @return POST request parameters for this request.
	 */
	public RequestParameters getPostRequestParameters()
	{
		return EmptyRequestParameters.INSTANCE;
	}

	/**
	 * @return GET request parameters for this request.
	 */
	public RequestParameters getGetRequestParameters()
	{
		return new UrlRequestParametersAdapter(getUrl());
	}

	/**
	 * @return all request parameters for this request (both POST and GET parameters)
	 */
	public RequestParameters getRequestParameters()
	{
		return new CombinedRequestParametersAdapter(getGetRequestParameters(), getPostRequestParameters());
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
		return new Request()
		{
			@Override
			public Url getUrl()
			{
				return url;
			}

			@Override
			public RequestParameters getPostRequestParameters()
			{
				return Request.this.getPostRequestParameters();
			}
		};
	}

}
