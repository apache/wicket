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
import java.util.Map.Entry;

import org.apache.wicket.IRequestTarget;
import org.apache.wicket.PageParameters;
import org.apache.wicket.request.RequestParameters;
import org.apache.wicket.request.target.coding.AbstractRequestTargetUrlCodingStrategy;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.value.ValueMap;

/**
 * Request coding strategy that uses a simple URI by putting the remaining path
 * in the <tt>uri</tt> page parameter. Override the decode() method to return
 * the appropriate request target, calling getURI(requestParameters) to get
 * requested uri. Note that this request coding strategy takes other page
 * parameters from the query string directly, it does not use hierarchical path
 * for parameters.
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
	 * @see wicket.request.target.coding.IRequestTargetUrlCodingStrategy#decode(wicket.request.RequestParameters)
	 */
	public IRequestTarget decode(RequestParameters requestParameters)
	{
		return null;
	}

	/**
	 * Does nothing
	 * 
	 * @see wicket.request.target.coding.IRequestTargetUrlCodingStrategy#encode(wicket.IRequestTarget)
	 */
	public CharSequence encode(IRequestTarget requestTarget)
	{
		return null;
	}

	/**
	 * Does nothing
	 * 
	 * @see wicket.request.target.coding.IRequestTargetUrlCodingStrategy#matches(wicket.IRequestTarget)
	 */
	public boolean matches(IRequestTarget requestTarget)
	{
		return false;
	}

	/**
	 * Gets the encoded URL for the request target. Typically, the result will
	 * be prepended with a protocol specific prefix. In a servlet environment,
	 * the prefix concatenates the context path and the servlet path, for
	 * example "mywebapp/myservlet".
	 * 
	 * @param url
	 *            the relative reference URL
	 * @param parameters
	 *            parameter names mapped to parameter values
	 */
	protected void appendParameters(AppendingStringBuffer url, Map parameters)
	{

		if (parameters != null && parameters.size() > 0)
		{
			boolean firstParam = true;
			Iterator entries = parameters.entrySet().iterator();

			while (entries.hasNext())
			{
				Map.Entry entry = (Entry)entries.next();

				if (entry.getValue() != null)
				{
					String escapedValue = urlEncode(entry.getValue().toString());

					if (!Strings.isEmpty(escapedValue))
					{
						if (entry.getKey().equals(URI))
						{
							url.append("/").append(escapedValue);
						}
						else
						{
							if (firstParam)
							{
								url.append("?"); /* Begin query string. */
								firstParam = false;
							}
							else
							{
								/*
								 * Separate new key=value(s) pair from previous
								 * pair with an ampersand.
								 */
								url.append("&");
							}

							/* Append key=value(s) pair. */
							url.append(entry.getKey());
							url.append("=");
							url.append(escapedValue);
						}
					}
				}
			}
		}
	}

	/**
	 * Decodes parameters object from the provided url fragment
	 * 
	 * @param urlFragment
	 *            fragment of the url after the decoded path and before the
	 *            query string
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

		if (urlFragment.length() == 0)
		{
			return new ValueMap();
		}

		ValueMap parameters = new ValueMap();
		parameters.add(URI, urlFragment);

		if (urlParameters != null)
		{
			parameters.putAll(urlParameters);
		}

		return parameters;
	}
	
	protected String getURI(RequestParameters requestParameters) {
		return decodeParameters(requestParameters).getString(URI);
	}
}
