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
 * Response that keeps headers in buffers until the first content is written.
 * 
 * This is necessary to get {@link #reset()} working without removing the JSESSIONID cookie. When
 * {@link HttpServletResponse#reset()} is called it removes all cookies, including the JSESSIONID
 * cookie - see <a href="https://issues.apache.org/bugzilla/show_bug.cgi?id=26183">Bug 26183</a>.
 * 
 * Calling {@link #reset()} on this response clears the buffered meta data, if there is already any
 * content written it throws {@link IllegalStateException}.
 * 
 * @author Matej Knopp
 */
class HeaderBufferingWebResponse extends WebResponse implements IMetaDataBufferingWebResponse
{
	private final WebResponse originalResponse;

	/**
	 * Buffer of meta data.
	 */
	private final BufferedWebResponse bufferedResponse;

	public HeaderBufferingWebResponse(WebResponse originalResponse)
	{
		this.originalResponse = originalResponse;

		bufferedResponse = new BufferedWebResponse(originalResponse);
	}

	private boolean buffering = true;

	private void stopBuffering()
	{
		if (buffering)
		{
			bufferedResponse.writeTo(originalResponse);
			buffering = false;
		}
	}

	/**
	 * The response used for meta data.
	 * 
	 * @return buffered response if nothing was written yet, the original response otherwise
	 */
	private WebResponse getMetaResponse()
	{
		if (buffering)
		{
			return bufferedResponse;
		}
		else
		{
			return originalResponse;
		}
	}

	@Override
	public void addCookie(Cookie cookie)
	{
		getMetaResponse().addCookie(cookie);
	}

	@Override
	public void clearCookie(Cookie cookie)
	{
		getMetaResponse().clearCookie(cookie);
	}

	@Override
	public void flush()
	{
		stopBuffering();

		originalResponse.flush();
	}

	@Override
	public boolean isRedirect()
	{
		return getMetaResponse().isRedirect();
	}

	@Override
	public void sendError(int sc, String msg)
	{
		getMetaResponse().sendError(sc, msg);
	}

	@Override
	public void sendRedirect(String url)
	{
		getMetaResponse().sendRedirect(url);
	}

	@Override
	public void setContentLength(long length)
	{
		getMetaResponse().setContentLength(length);
	}

	@Override
	public void setContentType(String mimeType)
	{
		getMetaResponse().setContentType(mimeType);
	}

	@Override
	public void setDateHeader(String name, Time date)
	{
		Args.notNull(date, "date");
		getMetaResponse().setDateHeader(name, date);
	}

	@Override
	public void setHeader(String name, String value)
	{
		getMetaResponse().setHeader(name, value);
	}

	@Override
	public void addHeader(String name, String value)
	{
		getMetaResponse().addHeader(name, value);
	}

	@Override
	public void setStatus(int sc)
	{
		getMetaResponse().setStatus(sc);
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
		stopBuffering();

		originalResponse.write(sequence);
	}

	@Override
	public void write(byte[] array)
	{
		stopBuffering();

		originalResponse.write(array);
	}


	@Override
	public void write(byte[] array, int offset, int length)
	{
		stopBuffering();

		originalResponse.write(array, offset, length);
	}

	@Override
	public void reset()
	{
		if (buffering)
		{
			// still buffering so just reset the buffer of meta data
			bufferedResponse.reset();
		}
		else
		{
			// the original response is never reset (see class javadoc)
			throw new IllegalStateException("Response is no longer buffering!");
		}
	}

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
