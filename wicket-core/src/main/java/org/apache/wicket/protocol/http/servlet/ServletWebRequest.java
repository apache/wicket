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
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.lang.Checks;
import org.apache.wicket.util.string.PrependingStringBuffer;
import org.apache.wicket.util.string.StringValue;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.core.util.string.UrlUtils;
import org.apache.wicket.util.time.Time;
import org.apache.wicket.util.upload.FileItemFactory;
import org.apache.wicket.util.upload.FileUploadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link WebRequest} subclass that wraps a {@link HttpServletRequest} object.
 * 
 * @author Matej Knopp
 * @author Juergen Donnerstag
 * @author Igor Vaynberg
 */
public class ServletWebRequest extends WebRequest
{
	private static final Logger LOG = LoggerFactory.getLogger(ServletWebRequest.class);

	private final HttpServletRequest httpServletRequest;

	private final Url url;

	private final String filterPrefix;

	private final ErrorAttributes errorAttributes;

	private final ForwardAttributes forwardAttributes;

	/**
	 * Construct.
	 * 
	 * @param httpServletRequest
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
	 * @param filterPrefix
	 *            contentPath + filterPath, used to extract the actual {@link Url}
	 * @param url
	 */
	public ServletWebRequest(HttpServletRequest httpServletRequest, String filterPrefix, Url url)
	{
		Args.notNull(httpServletRequest, "httpServletRequest");
		Args.notNull(filterPrefix, "filterPrefix");

		this.httpServletRequest = httpServletRequest;

		errorAttributes = ErrorAttributes.of(httpServletRequest, filterPrefix);

		forwardAttributes = ForwardAttributes.of(httpServletRequest, filterPrefix);

		if (forwardAttributes != null || errorAttributes != null)
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Setting filterPrefix('{}') to '' because there is either an error or a forward. {}, {}",
						new Object[] {filterPrefix, forwardAttributes, errorAttributes});
			}
			// the filter prefix is not needed when the current request is internal
			// see WICKET-4387
			this.filterPrefix = "";
		} else
		{
			this.filterPrefix = filterPrefix;
		}

		if (url != null)
		{
			this.url = url;
		}
		else
		{
			this.url = getContextRelativeUrl(httpServletRequest.getRequestURI(), filterPrefix);
		}
	}

	/**
	 * Returns base url without context or filter mapping.
	 * <p>
	 * Example: if current url is
	 * 
	 * <pre>
	 * http://localhost:8080/context/filter/mapping/wicket/bookmarkable/com.foo.Page?1&id=2
	 * </pre>
	 * 
	 * the base url is <em>wicket/bookmarkable/com.foo.Page</em>
	 * </p>
	 * 
	 * @see org.apache.wicket.request.Request#getClientUrl()
	 */
	@Override
	public Url getClientUrl()
	{
		if (errorAttributes != null && !Strings.isEmpty(errorAttributes.getRequestUri()))
		{
			String problematicURI = Url.parse(errorAttributes.getRequestUri(), getCharset())
				.toString();
			return getContextRelativeUrl(problematicURI, filterPrefix);
		}
		else if (forwardAttributes != null && !Strings.isEmpty(forwardAttributes.getRequestUri()))
		{
			String forwardURI = Url.parse(forwardAttributes.getRequestUri(), getCharset())
				.toString();
			return getContextRelativeUrl(forwardURI, filterPrefix);
		}
		else if (!isAjax())
		{
			return getContextRelativeUrl(httpServletRequest.getRequestURI(), filterPrefix);
		}
		else
		{
			String base = getHeader(HEADER_AJAX_BASE_URL);

			if (base == null)
			{
				base = getRequestParameters().getParameterValue(PARAM_AJAX_BASE_URL).toString(null);
			}

			Checks.notNull(base, "Current ajax request is missing the base url header or parameter");

			return setParameters(Url.parse(base, getCharset()));
		}
	}

	private Url setParameters(Url url)
	{
		url.setPort(httpServletRequest.getServerPort());
		url.setHost(httpServletRequest.getServerName());
		url.setProtocol(httpServletRequest.getScheme());
		return url;
	}

	private Url getContextRelativeUrl(String uri, String filterPrefix)
	{
		if (filterPrefix.length() > 0 && !filterPrefix.endsWith("/"))
		{
			filterPrefix += "/";
		}
		StringBuilder url = new StringBuilder();
		uri = Strings.stripJSessionId(uri);
		String contextPath = httpServletRequest.getContextPath();

		if (LOG.isDebugEnabled())
		{
			LOG.debug("Calculating context relative path from: context path '{}', filterPrefix '{}', uri '{}'",
					new Object[] {contextPath, filterPrefix, uri});
		}

		final int start = contextPath.length() + filterPrefix.length() + 1;
		if (uri.length() > start)
		{
			url.append(uri.substring(start));
		}

		if (errorAttributes == null)
		{
			String query = httpServletRequest.getQueryString();
			if (!Strings.isEmpty(query))
			{
				url.append('?');
				url.append(query);
			}
		}

		return setParameters(Url.parse(url.toString(), getCharset()));
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
	public Time getDateHeader(String name)
	{
		try
		{
			long value = httpServletRequest.getDateHeader(name);

			if (value == -1)
			{
				return null;
			}

			return Time.millis(value);
		}
		catch (IllegalArgumentException e)
		{
			// per spec thrown if the header contains a value that cannot be converted to a date
			return null;
		}
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

	protected Map<String, List<StringValue>> generatePostParameters()
	{
		Map<String, List<StringValue>> postParameters = new HashMap<String, List<StringValue>>();

		IRequestParameters queryParams = getQueryParameters();

		@SuppressWarnings("unchecked")
		Map<String, String[]> params = getContainerRequest().getParameterMap();
		for (Map.Entry<String, String[]> param : params.entrySet())
		{
			final String name = param.getKey();
			final String[] values = param.getValue();

			if (name != null && values != null)
			{
				// build a mutable list of query params that have the same name as the post param
				List<StringValue> queryValues = queryParams.getParameterValues(name);
				if (queryValues == null)
				{
					queryValues = Collections.emptyList();
				}
				else
				{
					queryValues = new ArrayList<StringValue>(queryValues);
				}

				// the list that will contain accepted post param values
				List<StringValue> postValues = new ArrayList<StringValue>();

				for (String value : values)
				{
					StringValue val = StringValue.valueOf(value);
					if (queryValues.contains(val))
					{
						// if a query param with this value exists remove it and continue
						queryValues.remove(val);
					}
					else
					{
						// there is no query param with this value, assume post
						postValues.add(val);
					}
				}

				if (!postValues.isEmpty())
				{
					postParameters.put(name, postValues);
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
		@Override
		public void reset()
		{
			getPostRequestParameters().clear();
		}

		@Override
		public void setParameterValues(String key, List<StringValue> values)
		{
			getPostRequestParameters().put(key, values);
		}

		@Override
		public Set<String> getParameterNames()
		{
			return Collections.unmodifiableSet(getPostRequestParameters().keySet());
		}

		@Override
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

		@Override
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
	public ServletWebRequest cloneWithUrl(Url url)
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
	 * @param upload
	 *            upload identifier for {@link UploadInfo}
	 * @return multipart request
	 * @throws FileUploadException
	 */
	public MultipartServletWebRequest newMultipartWebRequest(Bytes maxSize, String upload)
		throws FileUploadException
	{
		return new MultipartServletWebRequestImpl(getContainerRequest(), filterPrefix, maxSize,
			upload);
	}

	/**
	 * Creates multipart web request from this request.
	 * 
	 * @param maxSize
	 * @param upload
	 *            upload identifier for {@link UploadInfo}
	 * @param factory
	 * @return multipart request
	 * @throws FileUploadException
	 */
	public MultipartServletWebRequest newMultipartWebRequest(Bytes maxSize, String upload,
		FileItemFactory factory) throws FileUploadException
	{
		return new MultipartServletWebRequestImpl(getContainerRequest(), filterPrefix, maxSize,
			upload, factory);
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

	@Override
	public HttpServletRequest getContainerRequest()
	{
		return httpServletRequest;
	}

	@Override
	public String getContextPath()
	{
		return UrlUtils.normalizePath(httpServletRequest.getContextPath());
	}

	@Override
	public String getFilterPath()
	{
		return UrlUtils.normalizePath(filterPrefix);
	}

	@Override
	public boolean shouldPreserveClientUrl()
	{
		return (errorAttributes != null && !Strings.isEmpty(errorAttributes.getRequestUri()) || forwardAttributes != null &&
			!Strings.isEmpty(forwardAttributes.getRequestUri()));
	}
}
