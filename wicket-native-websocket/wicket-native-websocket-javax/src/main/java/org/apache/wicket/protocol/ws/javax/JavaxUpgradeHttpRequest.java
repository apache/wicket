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
package org.apache.wicket.protocol.ws.javax;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;
import javax.websocket.Session;

import org.apache.wicket.util.string.StringValue;

/**
 * An artificial HttpServletRequest with data collected from the
 * available WebSocket Session and from the HandshakeRequest
 */
public class JavaxUpgradeHttpRequest implements HttpServletRequest
{
	private final HttpSession httpSession;
	private final String queryString;
	private final Principal userPrincipal;
	private final String requestUri;
	private final Map<String, String[]> parametersMap;
	private final Map<String, List<String>> headers;
	private final String contextPath = ""; // artificial

	public JavaxUpgradeHttpRequest(final Session session)
	{
		Map<String, Object> userProperties = session.getUserProperties();
		this.httpSession = (HttpSession) userProperties.get("session");
		this.headers = (Map<String, List<String>>) userProperties.get("headers");
		this.queryString = (String) userProperties.get("queryString");
		this.userPrincipal = (Principal) userProperties.get("userPrincipal");
		this.requestUri = userProperties.get("requestURI").toString();

		this.parametersMap = new HashMap<>();

		Map<String, List<String>> parameters = (Map<String, List<String>>) userProperties.get("parameterMap");
		for (Map.Entry<String, List<String>> entry : parameters.entrySet())
		{
			String name = entry.getKey();
			List<String> value = entry.getValue();
			parametersMap.put(name, value.toArray(new String[value.size()]));
		}
	}

	@Override
	public String getAuthType()
	{
		return null;
	}

	@Override
	public Cookie[] getCookies()
	{
		return new Cookie[0];
	}

	@Override
	public long getDateHeader(String name)
	{
		String headerValue = getHeader(name);
		return StringValue.valueOf(headerValue).toLong();
	}

	@Override
	public String getHeader(String name)
	{
		String value = null;
		if (headers != null)
		{
			List<String> headerValues = headers.get(name);
			if (headerValues.isEmpty() == false)
			{
				value = headerValues.get(0);
			}
		}
		return value;
	}

	@Override
	public Enumeration<String> getHeaders(String name)
	{
		Enumeration<String> values = null;
		if (headers != null)
		{
			List<String> headerValues = headers.get(name);
			if (headerValues.isEmpty() == false)
			{
				final Iterator<String> iterator = headerValues.iterator();
				values = new Enumeration<String>()
				{
					@Override
					public boolean hasMoreElements()
					{
						return iterator.hasNext();
					}

					@Override
					public String nextElement()
					{
						return iterator.next();
					}
				};
			}
		}
		return values;
	}

	@Override
	public Enumeration<String> getHeaderNames()
	{
		Enumeration<String> names = null;
		if (headers != null)
		{
			Set<String> headerNames = headers.keySet();
			if (headerNames.isEmpty() == false)
			{
				final Iterator<String> iterator = headerNames.iterator();
				names = new Enumeration<String>()
				{
					@Override
					public boolean hasMoreElements()
					{
						return iterator.hasNext();
					}

					@Override
					public String nextElement()
					{
						return iterator.next();
					}
				};
			}
		}
		return names;
	}

	@Override
	public int getIntHeader(String name)
	{
		String headerValue = getHeader(name);
		return StringValue.valueOf(headerValue).toInt();
	}

	@Override
	public String getMethod()
	{
		return null;
	}

	@Override
	public String getPathInfo()
	{
		return null;
	}

	@Override
	public String getPathTranslated()
	{
		return null;
	}

	@Override
	public String getContextPath()
	{
		return contextPath;
	}

	@Override
	public String getQueryString()
	{
		return queryString;
	}

	@Override
	public String getRemoteUser()
	{
		return null;
	}

	@Override
	public boolean isUserInRole(String role)
	{
		return false;
	}

	@Override
	public Principal getUserPrincipal()
	{
		return userPrincipal;
	}

	@Override
	public String getRequestedSessionId()
	{
		return null;
	}

	@Override
	public String getRequestURI()
	{
		return requestUri;
	}

	@Override
	public StringBuffer getRequestURL()
	{
		return null;
	}

	@Override
	public String getServletPath()
	{
		return null;
	}

	@Override
	public HttpSession getSession(boolean create)
	{
		return httpSession;
	}

	@Override
	public HttpSession getSession()
	{
		return httpSession;
	}

	@Override
	public String changeSessionId()
	{
		return null;
	}

	@Override
	public boolean isRequestedSessionIdValid()
	{
		return true;
	}

	@Override
	public boolean isRequestedSessionIdFromCookie()
	{
		return true;
	}

	@Override
	public boolean isRequestedSessionIdFromURL()
	{
		return false;
	}

	@Override
	public boolean isRequestedSessionIdFromUrl()
	{
		return false;
	}

	@Override
	public boolean authenticate(HttpServletResponse response) throws IOException, ServletException
	{
		return false;
	}

	@Override
	public void login(String username, String password) throws ServletException
	{
	}

	@Override
	public void logout() throws ServletException
	{
	}

	@Override
	public Collection<Part> getParts() throws IOException, ServletException
	{
		return null;
	}

	@Override
	public Part getPart(String name) throws IOException, ServletException
	{
		return null;
	}

	@Override
	public <T extends HttpUpgradeHandler> T upgrade(Class<T> tClass) throws IOException, ServletException
	{
		return null;
	}

	@Override
	public Object getAttribute(String name)
	{
		return null;
	}

	@Override
	public Enumeration<String> getAttributeNames()
	{
		return new Enumeration<String>()
		{
			@Override
			public boolean hasMoreElements()
			{
				return false;
			}

			@Override
			public String nextElement()
			{
				return null;
			}
		};
	}

	@Override
	public String getCharacterEncoding()
	{
		return null;
	}

	@Override
	public void setCharacterEncoding(String env) throws UnsupportedEncodingException
	{
	}

	@Override
	public int getContentLength()
	{
		return 0;
	}

	@Override
	public long getContentLengthLong()
	{
		return 0;
	}

	@Override
	public String getContentType()
	{
		return null;
	}

	@Override
	public ServletInputStream getInputStream() throws IOException
	{
		return null;
	}

	@Override
	public String getParameter(String name)
	{
		String[] values = parametersMap.get(name);
		return values != null ? values[0] : null;
	}

	@Override
	public Enumeration<String> getParameterNames()
	{
		final Iterator<String> iterator = parametersMap.keySet().iterator();
		return new Enumeration<String>()
		{
			@Override
			public boolean hasMoreElements()
			{
				return iterator.hasNext();
			}

			@Override
			public String nextElement()
			{
				return iterator.next();
			}
		};
	}

	@Override
	public String[] getParameterValues(String name)
	{
		return parametersMap.get(name);
	}

	@Override
	public Map<String, String[]> getParameterMap()
	{
		return parametersMap;
	}

	@Override
	public String getProtocol()
	{
		return null;
	}

	@Override
	public String getScheme()
	{
		return null;
	}

	@Override
	public String getServerName()
	{
		return null;
	}

	@Override
	public int getServerPort()
	{
		return 0;
	}

	@Override
	public BufferedReader getReader() throws IOException
	{
		return null;
	}

	@Override
	public String getRemoteAddr()
	{
		return null;
	}

	@Override
	public String getRemoteHost()
	{
		return null;
	}

	@Override
	public void setAttribute(String name, Object o)
	{
	}

	@Override
	public void removeAttribute(String name)
	{
	}

	@Override
	public Locale getLocale()
	{
		return null;
	}

	@Override
	public Enumeration<Locale> getLocales()
	{
		return null;
	}

	@Override
	public boolean isSecure()
	{
		return false;
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String path)
	{
		return null;
	}

	@Override
	public String getRealPath(String path)
	{
		return null;
	}

	@Override
	public int getRemotePort()
	{
		return 0;
	}

	@Override
	public String getLocalName()
	{
		return null;
	}

	@Override
	public String getLocalAddr()
	{
		return null;
	}

	@Override
	public int getLocalPort()
	{
		return 0;
	}

	@Override
	public ServletContext getServletContext()
	{
		return null;
	}

	@Override
	public AsyncContext startAsync() throws IllegalStateException
	{
		return null;
	}

	@Override
	public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException
	{
		return null;
	}

	@Override
	public boolean isAsyncStarted()
	{
		return false;
	}

	@Override
	public boolean isAsyncSupported()
	{
		return false;
	}

	@Override
	public AsyncContext getAsyncContext()
	{
		return null;
	}

	@Override
	public DispatcherType getDispatcherType()
	{
		return null;
	}
}
