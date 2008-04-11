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
package org.apache.wicket.request.target.basic;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.IRequestTarget;
import org.apache.wicket.PageParameters;
import org.apache.wicket.protocol.http.UnitTestSettings;
import org.apache.wicket.protocol.http.request.WebRequestCodingStrategy;
import org.apache.wicket.request.RequestParameters;
import org.apache.wicket.request.target.coding.AbstractRequestTargetUrlCodingStrategy;
import org.apache.wicket.request.target.coding.BookmarkablePageRequestTargetUrlCodingStrategy;
import org.apache.wicket.request.target.coding.WebRequestEncoder;
import org.apache.wicket.request.target.component.IBookmarkablePageRequestTarget;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.apache.wicket.util.value.ValueMap;

/**
 * Request coding strategy that uses a simple URI by putting the remaining path in the <tt>uri</tt>
 * page parameter. Override the decode() method to return the appropriate request target, calling
 * getURI(requestParameters) to get requested uri. Note that this request coding strategy takes
 * other page parameters from the query string directly, it does not use hierarchical path for
 * parameters.
 * 
 * @author <a href="mailto:jbq@apache.org">Jean-Baptiste Quenot</a>
 */
public class URIRequestTargetUrlCodingStrategy extends AbstractRequestTargetUrlCodingStrategy
{
	protected static final String URI = "uri";

	/**
	 * @see AbstractRequestTargetUrlCodingStrategy#AbstractRequestTargetUrlCodingStrategy(String)
	 */
	public URIRequestTargetUrlCodingStrategy(String mountPath)
	{
		super(mountPath);
	}

	/**
	 * Get the remaining path after mount point.
	 * 
	 * @param requestParameters
	 *            request parameters provided to the decode() method
	 * @return the URI
	 */
	public PageParameters decodeParameters(RequestParameters requestParameters)
	{
		final String parametersFragment = requestParameters.getPath().substring(
				getMountPath().length());
		return new PageParameters(decodeParameters(parametersFragment, requestParameters
				.getParameters()));
	}

	/**
	 * Does nothing
	 * 
	 * @see org.apache.wicket.request.target.coding.IRequestTargetUrlCodingStrategy#decode(org.apache.wicket.request.RequestParameters)
	 */
	public IRequestTarget decode(RequestParameters requestParameters)
	{
		return null;
	}

	/**
	 * Copied from {@link BookmarkablePageRequestTargetUrlCodingStrategy#encode(IRequestTarget)}
	 * without pageMapName field
	 * 
	 * @see org.apache.wicket.request.target.coding.IRequestTargetUrlCodingStrategy#encode(org.apache.wicket.IRequestTarget)
	 */
	public CharSequence encode(IRequestTarget requestTarget)
	{
		if (!(requestTarget instanceof IBookmarkablePageRequestTarget))
		{
			throw new IllegalArgumentException("This encoder can only be used with " +
					"instances of " + IBookmarkablePageRequestTarget.class.getName());
		}
		final AppendingStringBuffer url = new AppendingStringBuffer(40);
		url.append(getMountPath());
		final IBookmarkablePageRequestTarget target = (IBookmarkablePageRequestTarget)requestTarget;

		PageParameters pageParameters = target.getPageParameters();
		String pagemap = target.getPageMapName();
		if (pagemap != null)
		{
			if (pageParameters == null)
			{
				pageParameters = new PageParameters();
			}
			pageParameters.put(WebRequestCodingStrategy.PAGEMAP, WebRequestCodingStrategy
					.encodePageMapName(pagemap));
		}
		appendParameters(url, pageParameters);
		return url;
	}

	/**
	 * Always returns false
	 * 
	 * @see org.apache.wicket.request.target.coding.IRequestTargetUrlCodingStrategy#matches(org.apache.wicket.IRequestTarget)
	 */
	public boolean matches(IRequestTarget requestTarget)
	{
		return false;
	}

	/**
	 * Gets the encoded URL for the request target. The "uri" parameter is appended first with a
	 * slash, then the remaining parameters are handled like in
	 * {@link org.apache.wicket.request.target.coding.QueryStringUrlCodingStrategy#appendParameters(org.apache.wicket.util.string.AppendingStringBuffer, java.util.Map)}
	 * 
	 * @param url
	 *            the relative reference URL
	 * @param parameters
	 *            parameter names mapped to parameter values
	 */
	protected void appendParameters(AppendingStringBuffer url, Map parameters)
	{

		if (parameters.get(URI) != null)
		{
			url.append("/").append(parameters.get(URI));
		}
		if (!url.endsWith("/"))
		{
			url.append("/");
		}

		// Copied from QueryStringUrlCodingStrategy
		if (parameters != null && parameters.size() > 0)
		{
			final Iterator entries;
			if (UnitTestSettings.getSortUrlParameters())
			{
				entries = new TreeMap(parameters).entrySet().iterator();
			}
			else
			{
				entries = parameters.entrySet().iterator();
			}
			WebRequestEncoder encoder = new WebRequestEncoder(url);
			while (entries.hasNext())
			{
				Map.Entry entry = (Entry)entries.next();

				if (entry.getKey().equals(URI))
				{
					// Ignore "uri" parameter already handled
					continue;
				}
				if (entry.getValue() != null)
				{
					encoder.addValue(entry.getKey().toString(), entry.getValue());
				}
			}
		}
	}

	/**
	 * Decodes parameters object from the provided url fragment
	 * 
	 * @param urlFragment
	 *            fragment of the url after the decoded path and before the query string
	 * @param urlParameters
	 *            query string parameters
	 * @return Parameters created from the url fragment and query string
	 */
	protected ValueMap decodeParameters(String urlFragment, Map urlParameters)
	{
		// Hack off any leading slash
		if (urlFragment.startsWith("/"))
		{
			urlFragment = urlFragment.substring(1);
		}

		ValueMap parameters = new ValueMap();
		parameters.add(URI, urlFragment);

		if (urlParameters != null)
		{
			parameters.putAll(urlParameters);
		}

		return parameters;
	}

	/**
	 * Calls decodeParameters() and retrieves the <tt>uri</tt> parameter. If you need to access
	 * multiple parameters in the request, call {@link #decodeParameters(RequestParameters)}
	 * directly.
	 * 
	 * <p>
	 * <b>NOTE. </b> the returned URI is kept URL-encoded as per the
	 * {@link HttpServletRequest#getRequestURI()} specification
	 * </p>
	 * 
	 * @param requestParameters
	 * @return
	 */
	protected String getURI(RequestParameters requestParameters)
	{
		return decodeParameters(requestParameters).getString(URI);
	}
}
