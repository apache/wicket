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

import java.io.IOException;
import java.nio.charset.Charset;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.protocol.http.RequestUtils;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.util.lang.Args;

/**
 * WebResponse that wraps a {@link ServletWebResponse}.
 * 
 * @author Matej Knopp
 */
public class ServletWebResponse extends WebResponse
{
	private final HttpServletResponse httpServletResponse;
	private final HttpServletRequest httpServletRequest;

	/**
	 * Construct.
	 * 
	 * @param httpServletRequest
	 * @param httpServletResponse
	 */
	public ServletWebResponse(HttpServletRequest httpServletRequest,
		HttpServletResponse httpServletResponse)
	{
		Args.notNull(httpServletRequest, "httpServletRequest");
		Args.notNull(httpServletResponse, "httpServletResponse");

		this.httpServletResponse = httpServletResponse;
		this.httpServletRequest = httpServletRequest;
	}

	/**
	 * Returns the wrapped response
	 * 
	 * @return wrapped response
	 */
	public final HttpServletResponse getHttpServletResponse()
	{
		return httpServletResponse;
	}

	@Override
	public void addCookie(Cookie cookie)
	{
		httpServletResponse.addCookie(cookie);
	}

	@Override
	public void clearCookie(Cookie cookie)
	{
		cookie.setMaxAge(0);
		cookie.setValue(null);
		addCookie(cookie);
	}

	@Override
	public void setContentLength(long length)
	{
		httpServletResponse.addHeader("Content-Length", Long.toString(length));
	}

	@Override
	public void setContentType(String mimeType)
	{
		httpServletResponse.setContentType(mimeType);
	}

	@Override
	public void setDateHeader(String name, long date)
	{
		httpServletResponse.setDateHeader(name, date);
	}

	@Override
	public void setHeader(String name, String value)
	{
		httpServletResponse.setHeader(name, value);
	}

	@Override
	public void write(CharSequence sequence)
	{
		try
		{
			httpServletResponse.getWriter().append(sequence);
		}
		catch (IOException e)
		{
			throw new WicketRuntimeException(e);
		}
	}

	@Override
	public void write(byte[] array)
	{
		try
		{
			httpServletResponse.getOutputStream().write(array);
		}
		catch (IOException e)
		{
			throw new WicketRuntimeException(e);
		}
	}

	@Override
	public void setStatus(int sc)
	{
		httpServletResponse.setStatus(sc);
	}

	@Override
	public void sendError(int sc, String msg)
	{
		try
		{
			if (msg == null)
			{
				httpServletResponse.sendError(sc);
			}
			else
			{
				httpServletResponse.sendError(sc, msg);
			}
		}
		catch (IOException e)
		{
			throw new WicketRuntimeException(e);
		}
	}

	@Override
	public String encodeURL(CharSequence url)
	{
		if (url != null)
		{
			if (url.length() > 0 && url.charAt(0) == '?')
			{
				// there is a bug in apache tomcat 5.5 where tomcat doesn't put sessionid to url
				// when the URL starts with '?'. So we prepend the URL with ./ and remove it
				// afterwards (unless some container prepends session id before './' or mangles
				// the URL otherwise

				String encoded = httpServletResponse.encodeURL("./" + url.toString());
				if (encoded.startsWith("./"))
				{
					return encoded.substring(2);
				}
				else
				{
					return encoded;
				}
			}
			else
			{
				return httpServletResponse.encodeURL(url.toString());
			}
		}
		// FIXME NULL? Really?
		return httpServletResponse.encodeURL(null);
	}

	private String getAbsolutePrefix()
	{
		String port = "";
		if (("http".equals(httpServletRequest.getScheme()) && httpServletRequest.getServerPort() != 80) ||
			("https".equals(httpServletRequest.getScheme()) && httpServletRequest.getServerPort() != 443))
		{
			port = ":" + httpServletRequest.getServerPort();
		}
		return httpServletRequest.getScheme() + "://" + httpServletRequest.getServerName() + port;
	}

	private String getAbsoluteURL(String url)
	{
		if (url.startsWith("http://") || url.startsWith("https://"))
		{
			return url;
		}
		else if (url.startsWith("/"))
		{
			return getAbsolutePrefix() + url;
		}
		else
		{
			Charset charset = RequestUtils.getCharset(httpServletRequest);
			Url current = Url.parse(httpServletRequest.getRequestURI(), charset);
			Url append = Url.parse(url, charset);
			current.concatSegments(append.getSegments());
			Url result = new Url(current.getSegments(), append.getQueryParameters());
			return getAbsolutePrefix() + result.toString();
		}
	}

	private boolean redirect = false;

	@Override
	public void sendRedirect(String url)
	{
		sendRedirect(url, false);
	}

	private void sendRedirect(String url, boolean cacheable)
	{
		redirect = true;
		url = getAbsoluteURL(url);
		url = httpServletResponse.encodeRedirectURL(url);

		try
		{
			// proxies eventually cache '302 temporary redirect' responses:
			// for most wicket use cases this is fatal since redirects are
			// usually highly dynamic and can not be statically mapped
			// to a request url in general
			if (cacheable == false)
				RequestUtils.disableCaching(this);

			httpServletResponse.sendRedirect(url);
		}
		catch (IOException e)
		{
			throw new WicketRuntimeException(e);
		}
	}

	@Override
	public boolean isRedirect()
	{
		return redirect;
	}

	@Override
	public void flush()
	{
		try
		{
			httpServletResponse.flushBuffer();
		}
		catch (IOException e)
		{
			throw new WicketRuntimeException(e);
		}
	}

	@Override
	public void reset()
	{
		super.reset();
		httpServletResponse.reset();
		redirect = false;
	}
}
