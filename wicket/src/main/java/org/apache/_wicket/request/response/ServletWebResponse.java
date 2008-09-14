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
package org.apache._wicket.request.response;

import java.io.IOException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.WicketRuntimeException;

/**
 * WebResponse that wraps a {@link ServletWebResponse}.
 * @author Matej Knopp
 */
public class ServletWebResponse extends WebResponse
{
	private final HttpServletResponse httpServletResponse;

	/**
	 * Construct.

	 * @param httpServletResponse
	 */
	public ServletWebResponse(HttpServletResponse httpServletResponse)
	{
		if (httpServletResponse == null)
		{
			throw new IllegalArgumentException("Argument 'httpServletResponse' may not be null.");
		}
		this.httpServletResponse = httpServletResponse;
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
		httpServletResponse.setContentLength((int)length);
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
	public String encodeURL(String url)
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
		return httpServletResponse.encodeURL(url);
	}
	
	@Override
	public void sendRedirect(String url)
	{
		url = httpServletResponse.encodeRedirectURL(url);
		try
		{
			httpServletResponse.sendRedirect(url);
		}
		catch (IOException e)
		{
			throw new WicketRuntimeException(e);
		}
	}
}
