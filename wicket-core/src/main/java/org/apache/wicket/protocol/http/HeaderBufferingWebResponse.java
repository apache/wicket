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
package org.apache.wicket.protocol.http;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.time.Time;

/**
 * Response that keeps headers in buffers but writes the content directly to the response.
 * 
 * This is necessary to get {@link #reset()} working without removing the JSESSIONID cookie. When
 * {@link HttpServletResponse#reset()} is called it removes all cookies, including the JSESSIONID
 * cookie.
 * 
 * Calling {@link #reset()} on this response only clears the buffered headers. If there is any
 * content written to response it throws {@link IllegalStateException}.
 * 
 * @author Matej Knopp
 */
class HeaderBufferingWebResponse extends WebResponse implements IMetaDataBufferingWebResponse
{
	private final WebResponse originalResponse;
	private final BufferedWebResponse bufferedResponse;

	public HeaderBufferingWebResponse(WebResponse originalResponse)
	{
		this.originalResponse = originalResponse;
		bufferedResponse = new BufferedWebResponse(originalResponse);
	}

	private boolean bufferedWritten = false;

	private void writeBuffered()
	{
		if (!bufferedWritten)
		{
			bufferedResponse.writeTo(originalResponse);
			bufferedWritten = true;
		}
	}

	private void checkHeader()
	{
		if (bufferedWritten)
		{
			throw new IllegalStateException("Header was already written to response!");
		}
	}

	@Override
	public void addCookie(Cookie cookie)
	{
		checkHeader();
		bufferedResponse.addCookie(cookie);
	}

	@Override
	public void clearCookie(Cookie cookie)
	{
		checkHeader();
		bufferedResponse.clearCookie(cookie);
	}

	private boolean flushed = false;

	@Override
	public void flush()
	{
		if (!bufferedWritten)
		{
			bufferedResponse.writeTo(originalResponse);
			bufferedResponse.reset();
		}
		originalResponse.flush();
		flushed = true;
	}

	@Override
	public boolean isRedirect()
	{
		return bufferedResponse.isRedirect();
	}

	@Override
	public void sendError(int sc, String msg)
	{
		checkHeader();
		bufferedResponse.sendError(sc, msg);
	}

	@Override
	public void sendRedirect(String url)
	{
		checkHeader();
		bufferedResponse.sendRedirect(url);
	}

	@Override
	public void setContentLength(long length)
	{
		checkHeader();
		bufferedResponse.setContentLength(length);
	}

	@Override
	public void setContentType(String mimeType)
	{
		checkHeader();
		bufferedResponse.setContentType(mimeType);
	}

	@Override
	public void setDateHeader(String name, Time date)
	{
		Args.notNull(date, "date");
		checkHeader();
		bufferedResponse.setDateHeader(name, date);
	}

	@Override
	public void setHeader(String name, String value)
	{
		checkHeader();
		bufferedResponse.setHeader(name, value);
	}

	@Override
	public void addHeader(String name, String value)
	{
		checkHeader();
		bufferedResponse.addHeader(name, value);
	}

	@Override
	public void setStatus(int sc)
	{
		bufferedResponse.setStatus(sc);
	}

	@Override
	public String encodeURL(CharSequence url)
	{
		return originalResponse.encodeURL(url);
	}

	@Override
	public String encodeRedirectURL(CharSequence url)
	{
		return originalResponse.encodeRedirectURL(url);
	}

	@Override
	public void write(CharSequence sequence)
	{
		writeBuffered();
		originalResponse.write(sequence);
	}

	@Override
	public void write(byte[] array)
	{
		writeBuffered();
		originalResponse.write(array);
	}


	@Override
	public void write(byte[] array, int offset, int length)
	{
		writeBuffered();
		originalResponse.write(array, offset, length);
	}

	@Override
	public void reset()
	{
		if (flushed)
		{
			throw new IllegalStateException("Response has already been flushed!");
		}
		bufferedResponse.reset();
		bufferedWritten = false;
	}

	@Override
	public void writeMetaData(WebResponse webResponse)
	{
		bufferedResponse.writeMetaData(webResponse);
	}

	@Override
	public Object getContainerResponse()
	{
		return originalResponse.getContainerResponse();
	}
}
