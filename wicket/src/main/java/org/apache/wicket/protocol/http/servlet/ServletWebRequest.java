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
package org.apache.wicket.protocol.http.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.protocol.http.RequestUtils;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.IWritableRequestParameters;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.Url.QueryParameter;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.util.io.Streams;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.string.PrependingStringBuffer;
import org.apache.wicket.util.string.StringValue;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.upload.FileItemFactory;
import org.apache.wicket.util.upload.FileUploadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link WebRequest} subclass that wraps a {@link HttpServletRequest} object.
 * 
 * @author Matej Knopp
 */
public class ServletWebRequest extends WebRequest
{
	private final HttpServletRequest httpServletRequest;

	private final Url url;

	private final String filterPrefix;

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
		this(httpServletRequest, filterPrefix, null);
	}

	/**
	 * Construct.
	 * 
	 * @param httpServletRequest
	 * 
	 * @param filterPrefix
	 *            contentPath + filterPath, used to extract the actual {@link Url}
	 * 
	 * @param url
	 */
	public ServletWebRequest(HttpServletRequest httpServletRequest, String filterPrefix, Url url)
	{
		Args.notNull(httpServletRequest, "httpServletRequest");
		Args.notNull(filterPrefix, "filterPrefix");

		this.httpServletRequest = httpServletRequest;
		this.filterPrefix = filterPrefix;

		if (url != null)
		{
			this.url = url;
		}
		else
		{
			this.url = getUrl(httpServletRequest, filterPrefix);
		}
	}

	private Url getUrl(HttpServletRequest request, String filterPrefix)
	{
		if (filterPrefix.length() > 0 && !filterPrefix.endsWith("/"))
		{
			filterPrefix += "/";
		}
		StringBuilder url = new StringBuilder();
		String uri = request.getRequestURI();
		uri = Strings.stripJSessionId(uri);
		final int start = request.getContextPath().length() + filterPrefix.length() + 1;
		url.append(uri.substring(start));

		String query = request.getQueryString();
		if (!Strings.isEmpty(query))
		{
			url.append("?");
			url.append(query);
		}

		return Url.parse(Strings.stripJSessionId(url.toString()), getCharset());
	}


	/**
	 * Returns the prefix of Wicket filter (without the leading /)
	 * 
	 * @return Wicket filter prefix
	 */
	public String getFilterPrefix()
	{
		return filterPrefix;
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
	public List<Cookie> getCookies()
	{
		Cookie[] cookies = httpServletRequest.getCookies();
		List<Cookie> result = (cookies == null) ? Collections.<Cookie> emptyList()
			: Arrays.asList(cookies);
		return Collections.unmodifiableList(result);
	}


	@Override
	public Locale getLocale()
	{
		return httpServletRequest.getLocale();
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

	private Map<String, List<StringValue>> postParameters = null;

	private static boolean isMultiPart(ServletRequest request)
	{
		String contentType = request.getContentType();
		return contentType != null && contentType.toLowerCase().contains("multipart");
	}

	@SuppressWarnings("unchecked")
	protected Map<String, List<StringValue>> generatePostParameters()
	{
		// Do not attempt to parse multipart request
		if (isMultiPart(getHttpServletRequest()))
		{
			return Collections.emptyMap();
		}

		Map<String, List<StringValue>> postParameters = new HashMap<String, List<StringValue>>();
		try
		{
			final BufferedReader reader = getHttpServletRequest().getReader();
			final String value = Streams.readString(reader);

			if (!Strings.isEmpty(value))
			{
				final Url url = Url.parse("?" + value, getCharset());
				for (final QueryParameter q : url.getQueryParameters())
				{
					List<StringValue> list = postParameters.get(q.getName());
					if (list == null)
					{
						list = new ArrayList<StringValue>();
						postParameters.put(q.getName(), list);
					}
					list.add(StringValue.valueOf(q.getValue()));
				}
			}
		}
		catch (final IOException e)
		{
			logger.warn(
				"Error parsing request body for post parameters; Fallback to ServletRequest#getParameters().",
				e);
			for (final String name : (List<String>)Collections.list(getHttpServletRequest().getParameterNames()))
			{
				List<StringValue> list = postParameters.get(name);
				if (list == null)
				{
					list = new ArrayList<StringValue>();
					postParameters.put(name, list);
				}
				for (final String value : getHttpServletRequest().getParameterValues(name))
				{
					list.add(StringValue.valueOf(value));
				}
			}
		}
		return postParameters;
	}

	private Map<String, List<StringValue>> getPostRequestParameters()
	{
		if (postParameters == null)
		{
			postParameters = generatePostParameters();
		}
		return postParameters;
	}

	private final IRequestParameters postRequestParameters = new IWritableRequestParameters()
	{
		public void reset()
		{
			getPostRequestParameters().clear();
		}

		public void setParameterValues(String key, List<StringValue> values)
		{
			getPostRequestParameters().put(key, values);
		}

		public Set<String> getParameterNames()
		{
			return Collections.unmodifiableSet(getPostRequestParameters().keySet());
		}

		public StringValue getParameterValue(String name)
		{
			List<StringValue> values = getPostRequestParameters().get(name);
			if (values == null || values.isEmpty())
			{
				return StringValue.valueOf((String)null);
			}
			else
			{
				return values.iterator().next();
			}
		}

		public List<StringValue> getParameterValues(String name)
		{
			List<StringValue> values = getPostRequestParameters().get(name);
			if (values != null)
			{
				values = Collections.unmodifiableList(values);
			}
			return values;
		}
	};

	@Override
	public IRequestParameters getPostParameters()
	{
		return postRequestParameters;
	}

	@Override
	public Url getUrl()
	{
		return new Url(url);
	}

	@Override
	public ServletWebRequest requestWithUrl(Url url)
	{
		return new ServletWebRequest(httpServletRequest, filterPrefix, url)
		{
			@Override
			public IRequestParameters getPostParameters()
			{
				// don't parse post parameters again
				return ServletWebRequest.this.getPostParameters();
			}
		};
	}

	/**
	 * Creates multipart web request from this request.
	 * 
	 * @param maxSize
	 * @return multipart request
	 * @throws FileUploadException
	 */
	public MultipartServletWebRequest newMultipartWebRequest(Bytes maxSize)
		throws FileUploadException
	{
		return new MultipartServletWebRequestImpl(getHttpServletRequest(), filterPrefix, maxSize);
	}

	/**
	 * Creates multipart web request from this request.
	 * 
	 * @param maxSize
	 * @param factory
	 * @return multipart request
	 * @throws FileUploadException
	 */
	public MultipartServletWebRequest newMultipartWebRequest(Bytes maxSize, FileItemFactory factory)
		throws FileUploadException
	{
		return new MultipartServletWebRequestImpl(getHttpServletRequest(), filterPrefix, maxSize,
			factory);
	}

	private static final Logger logger = LoggerFactory.getLogger(ServletWebRequest.class);

	@Override
	public String getPrefixToContextPath()
	{
		PrependingStringBuffer buffer = new PrependingStringBuffer();
		Url filterPrefixUrl = Url.parse(filterPrefix, getCharset());
		for (int i = 0; i < filterPrefixUrl.getSegments().size() - 1; ++i)
		{
			buffer.prepend("../");
		}
		return buffer.toString();
	}

	@Override
	public Charset getCharset()
	{
		return RequestUtils.getCharset(httpServletRequest);
	}

}
