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
package org.apache.wicket.request;

import java.nio.charset.Charset;
import java.util.Locale;

import org.apache.wicket.request.parameter.CombinedRequestParametersAdapter;
import org.apache.wicket.request.parameter.EmptyRequestParameters;
import org.apache.wicket.request.parameter.UrlRequestParametersAdapter;

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
	 * Returns the url against which the client, usually the browser, will resolve relative urls in
	 * the rendered markup. If the client is a browser this is the url in the browser's address bar.
	 * 
	 * <p>
	 * Under normal circumstances the client and request ({@link #getUrl()}) urls are the same.
	 * Handling an Ajax request, however, is a good example of when they may be different.
	 * Technologies such as XHR are free to request whatever url they wish <strong>without
	 * modifying</strong> the client url; however, any produced urls will be evaluated by the client
	 * against the client url - not against the request url.
	 * </p>
	 * <p>
	 * Lets take a simple example: <br>
	 * 
	 * Suppose we are on a client detail page. This page contains an Ajax link which opens a list of
	 * client's orders. Each order has a link that goes to the order detail page.
	 * 
	 * The client detail page is located at
	 * 
	 * <pre>
	 * /detail/customer/15
	 * </pre>
	 * 
	 * and the order detail page is located at
	 * 
	 * <pre>
	 * /order/22
	 * </pre>
	 * 
	 * The Ajax link which renders the detail section is located at
	 * 
	 * <pre>
	 *  /detail/wicket?page 3
	 * </pre>
	 * 
	 * Lets run through the execution and see what happens when the XHR request is processed:
	 * 
	 * <pre>
	 * request 1: /details/customer/15
	 * client url: details/customer/15 (the url in the browser's address bar)
	 * request url: details/customer/15
	 * Wicket renders relative Ajax details anchor as ../../wicket/page?3  
	 * 
	 * ../../wicket/page?3 resolved against current client url details/customer/15 yields:
	 * request 2: /wicket/page?3
	 * client url: customer/15 (unchanged since XHRs requests dont change it)
	 * request url: wicket/ajax/page?3
	 * 
	 * now Wicket has to render a relative url to /details/order/22. If Wicket renders 
	 * it against the request url it will be: ../order/22, and later evaluated on the
	 * client will result in /customer/order/22 which is incorrect.
	 * </pre>
	 * 
	 * This is why implementations of {@link Request} must track the client url, so that relative
	 * urls can be rendered against the same url they will be evaluated against on the client -
	 * which is not always the same as the request url. For example, Wicket's Ajax implementation
	 * always sends the current client url in a header along with the XHR request so that Wicket can
	 * correctly render relative urls against it.
	 * </p>
	 * 
	 * @return client url
	 */
	public abstract Url getClientUrl();

	/**
	 * In case this request has been created using {@link #cloneWithUrl(Url)}, this method should
	 * return the original URL.
	 * 
	 * @return original URL
	 */
	public Url getOriginalUrl()
	{
		return getUrl();
	}

	/**
	 * @return POST request parameters for this request.
	 */
	public IRequestParameters getPostParameters()
	{
		return EmptyRequestParameters.INSTANCE;
	}

	/**
	 * @return GET request parameters for this request.
	 */
	public IRequestParameters getQueryParameters()
	{
		return new UrlRequestParametersAdapter(getUrl());
	}

	/**
	 * @return all request parameters for this request (both POST and GET parameters)
	 */
	public IRequestParameters getRequestParameters()
	{
		return new CombinedRequestParametersAdapter(getQueryParameters(), getPostParameters());
	}

	/**
	 * Returns locale for this request.
	 * 
	 * @return locale
	 */
	public abstract Locale getLocale();

	/**
	 * Returns request with specified URL and same POST parameters as this request.
	 * 
	 * @param url
	 *            Url instance
	 * @return request with specified URL.
	 */
	public Request cloneWithUrl(final Url url)
	{
		return new Request()
		{
			@Override
			public Url getUrl()
			{
				return url;
			}

			@Override
			public Url getOriginalUrl()
			{
				return Request.this.getOriginalUrl();
			}

			@Override
			public Locale getLocale()
			{
				return Request.this.getLocale();
			}

			@Override
			public IRequestParameters getPostParameters()
			{
				return Request.this.getPostParameters();
			}

			@Override
			public Charset getCharset()
			{
				return Request.this.getCharset();
			}

			@Override
			public Url getClientUrl()
			{
				return Request.this.getClientUrl();
			}

			@Override
			public Object getContainerRequest()
			{
				return Request.this.getContainerRequest();
			}
		};
	}

	/**
	 * Returns prefix from Wicket Filter mapping to context path. This method does not take the
	 * actual URL into account.
	 * <p>
	 * For example if Wicket filter is mapped to hello/* this method should return ../ regardless of
	 * actual URL (after Wicket filter)
	 * 
	 * @return prefix to context path for this request.
	 * 
	 */
	public String getPrefixToContextPath()
	{
		return "";
	}

	/**
	 * Returns the context path or an empty string if the application is running under root context.
	 * Returned path, unless an empty string, will always start with a slash and will never end with
	 * a slash.
	 * 
	 * @return context path
	 */
	public String getContextPath()
	{
		return "";
	}

	/**
	 * Returns the path to which wicket Filter is mapped or an empty string if the filter is mapped
	 * to {@code /*}. Returned path, unless an empty string, will always start with a slash and will
	 * never end with a slash.
	 * 
	 * @return filter path
	 */
	public String getFilterPath()
	{
		return "";
	}

	/**
	 * Gets charset of the request
	 * 
	 * @return request charset
	 */
	public abstract Charset getCharset();

	/**
	 * Provides access to the low-level container request object that implementaion of this
	 * {@link Request} delegate to. This allows users to access features provided by the container
	 * requests but not by generalized Wicket {@link Request} objects.
	 * 
	 * @return low-level container request object, or {@code null} if none
	 */
	public abstract Object getContainerRequest();
}
