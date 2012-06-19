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
package org.apache.wicket.protocol.ws.api;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.protocol.http.RequestUtils;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.util.lang.Generics;
import org.apache.wicket.util.time.Time;

/**
 * A {@link WebRequest} implementation used for the lifecycle of a web socket
 * connection. It keeps a copy of the HttpServletRequest provided by the web container
 * during the creation of the web socket connection (the http upgrade).
 *
 * @since 6.0
 */
public class WebSocketRequest extends WebRequest
{
	private final HttpServletRequest request;

	/**
	 * Constructor.
	 *
	 * @param req
	 *      the copy of the HttpServletRequest used for the upgrade of the HTTP protocol
	 */
	public WebSocketRequest(HttpServletRequest req)
	{
		this.request = req;
	}

	@Override
	public List<Cookie> getCookies()
	{
		List<Cookie> cookies = Arrays.asList(request.getCookies());
		return cookies;
	}

	@Override
	public List<String> getHeaders(String name)
	{
		Enumeration<String> headers = request.getHeaders(name);
		List<String> h = Generics.newArrayList();
		while (headers.hasMoreElements())
		{
			h.add(headers.nextElement());
		}
		
		return h;
	}

	@Override
	public String getHeader(String name)
	{
		return request.getHeader(name);
	}

	@Override
	public Time getDateHeader(String name)
	{
		long dateHeader = request.getDateHeader(name);
		return Time.millis(dateHeader);
	}

	@Override
	public Url getUrl()
	{
		return null;
	}

	@Override
	public Url getClientUrl()
	{
		return null;
	}

	@Override
	public Locale getLocale()
	{
		return request.getLocale();
	}

	@Override
	public Charset getCharset()
	{
		return RequestUtils.getCharset(request);
	}

	@Override
	public Object getContainerRequest()
	{
		return request;
	}

	@Override
	public boolean isAjax()
	{
		return true;
	}
}
