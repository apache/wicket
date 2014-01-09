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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * See <a href="http://code.google.com/p/xebia-france/wiki/XForwardedFilter">XForwardedFilter</a>
 * 
 * @author Juergen Donnerstag
 */
public class XForwardedRequestWrapper extends HttpServletRequestWrapper
{
	private SimpleDateFormat[] dateFormats = new SimpleDateFormat[] {
			new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US),
			new SimpleDateFormat("EEEEEE, dd-MMM-yy HH:mm:ss zzz", Locale.US),
			new SimpleDateFormat("EEE MMMM d HH:mm:ss yyyy", Locale.US) };

	private Map<String, List<String>> headers;

	private String remoteAddr;

	private String remoteHost;

	private String scheme;

	private boolean secure;

	private int serverPort;

	/**
	 * Construct.
	 * 
	 * @param request
	 */
	@SuppressWarnings("unchecked")
	public XForwardedRequestWrapper(final HttpServletRequest request)
	{
		super(request);

		remoteAddr = request.getRemoteAddr();
		remoteHost = request.getRemoteHost();
		scheme = request.getScheme();
		secure = request.isSecure();
		serverPort = request.getServerPort();

		headers = new HashMap<String, List<String>>();
		Enumeration<?> headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements())
		{
			String header = (String)headerNames.nextElement();
			headers.put(header, Collections.list(request.getHeaders(header)));
		}
	}

	/**
	 * @see javax.servlet.http.HttpServletRequestWrapper#getDateHeader(java.lang.String)
	 */
	@Override
	public long getDateHeader(final String name)
	{
		String value = getHeader(name);
		if (value == null)
		{
			return -1;
		}

		Date date = null;
		for (int i = 0; ((i < dateFormats.length) && (date == null)); i++)
		{
			DateFormat dateFormat = dateFormats[i];
			try
			{
				date = dateFormat.parse(value);
			}
			catch (Exception ignored)
			{
				; // noop
			}
		}

		if (date == null)
		{
			throw new IllegalArgumentException(value);
		}
		else
		{
			return date.getTime();
		}
	}

	/**
	 * @see javax.servlet.http.HttpServletRequestWrapper#getHeader(java.lang.String)
	 */
	@Override
	public String getHeader(final String name)
	{
		Map.Entry<String, List<String>> header = getHeaderEntry(name);
		if (header == null || header.getValue() == null || header.getValue().isEmpty())
		{
			return null;
		}
		else
		{
			return header.getValue().get(0);
		}
	}

	/**
	 * 
	 * @param name
	 * @return The map entry for 'name'
	 */
	private Map.Entry<String, List<String>> getHeaderEntry(final String name)
	{
		for (Map.Entry<String, List<String>> entry : headers.entrySet())
		{
			if (entry.getKey().equalsIgnoreCase(name))
			{
				return entry;
			}
		}
		return null;
	}

	/**
	 * @see javax.servlet.http.HttpServletRequestWrapper#getHeaderNames()
	 */
	@Override
	public Enumeration<String> getHeaderNames()
	{
		return Collections.enumeration(headers.keySet());
	}

	/**
	 * @see javax.servlet.http.HttpServletRequestWrapper#getHeaders(java.lang.String)
	 */
	@Override
	public Enumeration<String> getHeaders(final String name)
	{
		Map.Entry<String, List<String>> header = getHeaderEntry(name);
		if (header == null || header.getValue() == null)
		{
			return Collections.enumeration(Collections.<String>emptyList());
		}
		else
		{
			return Collections.enumeration(header.getValue());
		}
	}

	/**
	 * @see javax.servlet.http.HttpServletRequestWrapper#getIntHeader(java.lang.String)
	 */
	@Override
	public int getIntHeader(final String name)
	{
		String value = getHeader(name);
		if (value == null)
		{
			return -1;
		}
		else
		{
			return Integer.parseInt(value);
		}
	}

	/**
	 * @see javax.servlet.ServletRequestWrapper#getRemoteAddr()
	 */
	@Override
	public String getRemoteAddr()
	{
		return remoteAddr;
	}

	/**
	 * @see javax.servlet.ServletRequestWrapper#getRemoteHost()
	 */
	@Override
	public String getRemoteHost()
	{
		return remoteHost;
	}

	/**
	 * @see javax.servlet.ServletRequestWrapper#getScheme()
	 */
	@Override
	public String getScheme()
	{
		return scheme;
	}

	/**
	 * @see javax.servlet.ServletRequestWrapper#getServerPort()
	 */
	@Override
	public int getServerPort()
	{
		return serverPort;
	}

	/**
	 * @see javax.servlet.ServletRequestWrapper#isSecure()
	 */
	@Override
	public boolean isSecure()
	{
		return secure;
	}

	/**
	 * @param name
	 */
	public void removeHeader(final String name)
	{
		Map.Entry<String, List<String>> header = getHeaderEntry(name);
		if (header != null)
		{
			headers.remove(header.getKey());
		}
	}

	/**
	 * 
	 * @param name
	 * @param value
	 */
	public void setHeader(final String name, final String value)
	{
		List<String> values = Arrays.asList(value);
		Map.Entry<String, List<String>> header = getHeaderEntry(name);
		if (header == null)
		{
			headers.put(name, values);
		}
		else
		{
			header.setValue(values);
		}
	}

	/**
	 * 
	 * @param remoteAddr
	 */
	public void setRemoteAddr(final String remoteAddr)
	{
		this.remoteAddr = remoteAddr;
	}

	/**
	 * 
	 * @param remoteHost
	 */
	public void setRemoteHost(final String remoteHost)
	{
		this.remoteHost = remoteHost;
	}

	/**
	 * 
	 * @param scheme
	 */
	public void setScheme(final String scheme)
	{
		this.scheme = scheme;
	}

	/**
	 * 
	 * @param secure
	 */
	public void setSecure(final boolean secure)
	{
		this.secure = secure;
	}

	/**
	 * 
	 * @param serverPort
	 */
	public void setServerPort(final int serverPort)
	{
		this.serverPort = serverPort;
	}
}
