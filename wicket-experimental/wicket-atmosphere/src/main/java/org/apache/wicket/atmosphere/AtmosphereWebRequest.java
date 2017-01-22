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
package org.apache.wicket.atmosphere;

import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.wicket.protocol.http.RequestUtils;
import org.apache.wicket.protocol.http.servlet.MultipartServletWebRequest;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.Url;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.time.Time;

/**
 * Internal request to signal the processing of an event. This request will be mapped by
 * {@link AtmosphereRequestMapper} to an {@link AtmosphereRequestHandler}. The response will be
 * written to the client of a suspended connection.
 * 
 * @author papegaaij
 */
class AtmosphereWebRequest extends ServletWebRequest
{
	private final ServletWebRequest wrappedRequest;

	private final PageKey pageKey;

	private final Iterator<EventSubscription> subscriptions;

	private final AtmosphereEvent event;

	AtmosphereWebRequest(ServletWebRequest wrappedRequest, PageKey pageKey,
		Iterator<EventSubscription> subscriptions, AtmosphereEvent event)
	{
		super(wrappedRequest.getContainerRequest(), wrappedRequest.getFilterPrefix());
		this.wrappedRequest = wrappedRequest;
		this.pageKey = pageKey;
		this.subscriptions = subscriptions;
		this.event = event;
	}

	public PageKey getPageKey()
	{
		return pageKey;
	}

	public Iterator<EventSubscription> getSubscriptions()
	{
		return subscriptions;
	}

	public AtmosphereEvent getEvent()
	{
		return event;
	}

	@Override
	public List<Cookie> getCookies()
	{
		return wrappedRequest.getCookies();
	}

	@Override
	public List<String> getHeaders(String name)
	{
		return wrappedRequest.getHeaders(name);
	}

	@Override
	public String getHeader(String name)
	{
		return wrappedRequest.getHeader(name);
	}

	@Override
	public Time getDateHeader(String name)
	{
		return wrappedRequest.getDateHeader(name);
	}

	@Override
	public Url getUrl()
	{
		return wrappedRequest.getUrl();
	}

	@Override
	public Url getClientUrl()
	{
		return wrappedRequest.getClientUrl();
	}

	@Override
	public Locale getLocale()
	{
		return wrappedRequest.getLocale();
	}

	@Override
	public Charset getCharset()
	{
		// called from the super constructor, when wrappedRequest is still null
		if (wrappedRequest == null)
			return RequestUtils.getCharset(super.getContainerRequest());
		return wrappedRequest.getCharset();
	}

	@Override
	public Cookie getCookie(String cookieName)
	{
		return wrappedRequest.getCookie(cookieName);
	}

	@Override
	public int hashCode()
	{
		return wrappedRequest.hashCode();
	}

	@Override
	public Url getOriginalUrl()
	{
		return wrappedRequest.getOriginalUrl();
	}

	@Override
	public IRequestParameters getQueryParameters()
	{
		return wrappedRequest.getQueryParameters();
	}

	@Override
	public IRequestParameters getRequestParameters()
	{
		return wrappedRequest.getRequestParameters();
	}

	@Override
	public boolean equals(Object obj)
	{
		return wrappedRequest.equals(obj);
	}

	@Override
	public String getFilterPrefix()
	{
		return wrappedRequest.getFilterPrefix();
	}

	@Override
	public String toString()
	{
		return wrappedRequest.toString();
	}

	@Override
	public IRequestParameters getPostParameters()
	{
		return wrappedRequest.getPostParameters();
	}

	@Override
	public ServletWebRequest cloneWithUrl(Url url)
	{
		return wrappedRequest.cloneWithUrl(url);
	}

	@Override
	public MultipartServletWebRequest newMultipartWebRequest(Bytes maxSize, String upload)
		throws FileUploadException
	{
		return wrappedRequest.newMultipartWebRequest(maxSize, upload);
	}

	@Override
	public MultipartServletWebRequest newMultipartWebRequest(Bytes maxSize, String upload,
		FileItemFactory factory) throws FileUploadException
	{
		return wrappedRequest.newMultipartWebRequest(maxSize, upload, factory);
	}

	@Override
	public String getPrefixToContextPath()
	{
		return wrappedRequest.getPrefixToContextPath();
	}

	@Override
	public HttpServletRequest getContainerRequest()
	{
		return wrappedRequest.getContainerRequest();
	}

	@Override
	public String getContextPath()
	{
		return wrappedRequest.getContextPath();
	}

	@Override
	public String getFilterPath()
	{
		return wrappedRequest.getFilterPath();
	}

	@Override
	public boolean shouldPreserveClientUrl()
	{
		return wrappedRequest.shouldPreserveClientUrl();
	}

	@Override
	public boolean isAjax()
	{
		return true;
	}
}
