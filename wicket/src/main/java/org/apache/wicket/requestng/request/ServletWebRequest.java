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
package org.apache.wicket.requestng.request;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.requestng.RequestParameters;
import org.apache.wicket.requestng.Url;
import org.apache.wicket.util.string.StringValue;
import org.apache.wicket.util.string.Strings;

/**
 * {@link WebRequest} subclass that wraps a {@link HttpServletRequest} object.
 * 
 * @author Matej Knopp
 */
public class ServletWebRequest extends WebRequest
{
	private final HttpServletRequest httpServletRequest;

	private final Url url;
	
	/**
	 * Construct.
	 * 
	 * @param httpServletRequest
	 * 
	 * @param filterPrefix
	 *            contentPath + filterPath, used to extract the actual {@link Url}
	 */
	public ServletWebRequest(HttpServletRequest httpServletRequest, String filterPrefix)
	{
		if (httpServletRequest == null)
		{
			throw new IllegalArgumentException("Argument 'httpServletRequest' may not be null.");
		}
		if (filterPrefix == null)
		{
			throw new IllegalArgumentException("Argument 'filterPrefix' may not be null.");
		}
		this.url = getUrl(httpServletRequest, filterPrefix);
		this.httpServletRequest = httpServletRequest;
	}
	
	private Url getUrl(HttpServletRequest request, String filterPrefix)
	{
		if (!filterPrefix.endsWith("/"))
		{
			filterPrefix += "/";
		}
		StringBuilder url = new StringBuilder();
		url.append(Strings.stripJSessionId(request.getRequestURI().substring(filterPrefix.length())));
		
		String query = request.getQueryString();
		if (!Strings.isEmpty(query))
		{
			url.append("?");
			url.append(query);
		}
		
		return Url.parse(url.toString());
	}

	/**
	 * Returns the wrapped {@link HttpServletRequest} instance.
	 * 
	 * @return {@link HttpServletRequest} instance.
	 */
	public final HttpServletRequest getHttpServletRequest()
	{
		return httpServletRequest;
	}

	@Override
	public Cookie[] getCookies()
	{
		return httpServletRequest.getCookies();
	}

	@Override
	public long getDateHeader(String name)
	{
		return httpServletRequest.getDateHeader(name);
	}

	@Override
	public String getHeader(String name)
	{
		return httpServletRequest.getHeader(name);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getHeaders(String name)
	{
		List<String> result = new ArrayList<String>();
		Enumeration<String> e = httpServletRequest.getHeaders(name);
		while (e.hasMoreElements())
		{
			result.add(e.nextElement());
		}
		return Collections.unmodifiableList(result);
	}

	@Override
	public RequestParameters getRequestParameters()
	{
		return new RequestParameters()
		{
			@SuppressWarnings("unchecked")
			public Set<String> getParameterNames()
			{
				Set<String> result = new HashSet<String>();
				Enumeration<String> e = httpServletRequest.getParameterNames();
				while (e.hasMoreElements())
				{
					result.add(e.nextElement());
				}
				return Collections.unmodifiableSet(result);
			}

			public StringValue getParameterValue(String name)
			{
				return StringValue.valueOf(httpServletRequest.getParameter(name));
			}

			public List<StringValue> getParameterValues(String name)
			{
				String values[] = httpServletRequest.getParameterValues(name);
				if (values == null)
				{
					return null;
				}
				else
				{
					List<StringValue> result = new ArrayList<StringValue>();
					for (String s : values)
					{
						result.add(StringValue.valueOf(s));
					}
					return Collections.unmodifiableList(result);
				}
			}
		};
	}

	@Override
	public Url getUrl()
	{
		return new Url(url);
	}

}
