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
package org.apache.wicket.protocol.http.portlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * Portlet Action specific response wrapper.
 * 
 * <p>
 * Overrides many of the {@link PortletServletResponseWrapper} with no-op methods as they are not
 * applicable in Action response situations because a response is not actually going to be sent to
 * the client - the response actually sent to the client is wrapped in
 * {@link PortletRenderServletResponseWrapper}.
 * 
 * @see PortletServletResponseWrapper
 * @author Ate Douma
 */
public class PortletActionServletResponseWrapper extends PortletServletResponseWrapper
{

	public PortletActionServletResponseWrapper(HttpServletResponse response,
		WicketResponseState responseState)
	{
		super(response, responseState);
	}

	@Override
	public void addCookie(Cookie cookie)
	{
	}

	@Override
	public void addDateHeader(String s, long l)
	{
	}

	@Override
	public void addHeader(String s, String s1)
	{
	}

	@Override
	public void addIntHeader(String s, int i)
	{
	}

	@Override
	public String encodeUrl(String s)
	{
		return s;
	}

	@Override
	public String encodeURL(String s)
	{
		return s;
	}

	@Override
	public void flushBuffer() throws IOException
	{
	}

	@Override
	public int getBufferSize()
	{
		return 0;
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException
	{
		return null;
	}

	@Override
	public PrintWriter getWriter() throws IOException
	{
		return null;
	}

	@Override
	public boolean isCommitted()
	{
		return false;
	}

	@Override
	public void reset()
	{
	}

	@Override
	public void resetBuffer()
	{
	}

	@Override
	public void setBufferSize(int i)
	{
	}

	public void setCharacterEncoding(String charset)
	{
	}

	@Override
	public void setContentLength(int i)
	{
	}

	@Override
	public void setContentType(String s)
	{
	}

	@Override
	public void setDateHeader(String s, long l)
	{
	}

	@Override
	public void setHeader(String s, String s1)
	{
	}

	@Override
	public void setIntHeader(String s, int i)
	{
	}

	@Override
	public void setLocale(Locale locale)
	{
	}
}
