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

import javax.servlet.http.Cookie;

import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.apache.wicket.util.time.Time;
import org.atmosphere.cpr.AtmosphereResponse;

/**
 * Internal response class to wrap {@code AtmosphereResponse} to a {@link WebResponse}.
 * 
 * @author papegaaij
 */
class AtmosphereWebResponse extends WebResponse
{
	private AtmosphereResponse response;
	private final AppendingStringBuffer out;
	private boolean redirect;

	/**
	 * Construct.
	 * 
	 * @param response
	 */
	AtmosphereWebResponse(AtmosphereResponse response)
	{
		this.response = response;
		out = new AppendingStringBuffer(128);
	}

	@Override
	public void addCookie(Cookie cookie)
	{
		response.addCookie(cookie);
	}

	@Override
	public void clearCookie(Cookie cookie)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void setHeader(String name, String value)
	{
		response.setHeader(name, value);
	}

	@Override
	public void addHeader(String name, String value)
	{
		response.addHeader(name, value);
	}

	@Override
	public void setDateHeader(String name, Time date)
	{
		response.setDateHeader(name, date.getMilliseconds());
	}

	@Override
	public void setContentLength(long length)
	{
		response.setContentLength((int)length);
	}

	@Override
	public void setContentType(String mimeType)
	{
		response.setContentType(mimeType);
	}

	@Override
	public void setStatus(int sc)
	{
		response.setStatus(sc);
	}

	@Override
	public void sendError(int sc, String msg)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String encodeRedirectURL(CharSequence url)
	{
		return response.encodeRedirectUrl(url.toString());
	}

	@Override
	public void sendRedirect(String url)
	{
		out.clear();
		out.append("<ajax-response><redirect><![CDATA[" + url + "]]></redirect></ajax-response>");
		redirect = true;
	}

	@Override
	public void write(byte[] array)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void write(byte[] array, int offset, int length)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String encodeURL(CharSequence url)
	{
		return response.encodeURL(url.toString());
	}

	@Override
	public Object getContainerResponse()
	{
		return response;
	}

	@Override
	public boolean isRedirect()
	{
		return false;
	}

	@Override
	public void reset()
	{
		out.clear();
	}

	@Override
	public void flush()
	{
	}

	@Override
	public void write(CharSequence sequence)
	{
		if (!redirect)
			out.append(sequence);
	}

	/**
	 * @return The internal buffer directly as a {@link CharSequence}
	 */
	public CharSequence getBuffer()
	{
		return out;
	}

	@Override
	public String toString()
	{
		return out.toString();
	}
}
