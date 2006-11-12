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
package wicket.protocol.http;

import javax.servlet.http.HttpServletResponse;

import wicket.WicketRuntimeException;
import wicket.util.string.AppendingStringBuffer;

/**
 * Subclass of WebResponse which buffers output and any redirection.
 * 
 * @author Jonathan Locke
 */
public class BufferedWebResponse extends WebResponse
{
	/** URL to redirect to when response is flushed, if any */
	private String redirectURL;

	/** Buffer to hold page */
	private AppendingStringBuffer buffer = new AppendingStringBuffer(4096);

	/**
	 * Constructor for testing harness.
	 */
	BufferedWebResponse()
	{
	}

	/**
	 * Package private constructor.
	 * 
	 * @param httpServletResponse
	 *            The servlet response object
	 */
	public BufferedWebResponse(final HttpServletResponse httpServletResponse)
	{
		super(httpServletResponse);
	}

	/**
	 * Flushes the response buffer by doing a redirect or writing out the
	 * buffer. NOTE: The servlet container will close the response output
	 * stream.
	 */
	@Override
	public void close()
	{
		// If a redirection was specified
		if (redirectURL != null)
		{
			// actually redirect
			super.redirect(redirectURL);
		}
		else
		{
			// Write the buffer to the response stream
			if (buffer.length() != 0)
			{
				super.write(buffer);
			}
		}
	}

	/**
	 * @see wicket.Response#reset()
	 */
	@Override
	public void reset()
	{
		redirectURL = null;
		buffer.clear();
	}
	
	/**
	 * Saves url to redirect to when buffered response is flushed.
	 * Implementations should encode the URL to make sure cookie-less operation
	 * is supported in case clients forgot.
	 * 
	 * @param url
	 *            The URL to redirect to
	 */
	@Override
	public final void redirect(final String url)
	{
		if (redirectURL != null)
		{
			throw new WicketRuntimeException("Already redirecting to '" + redirectURL
					+ "'. Cannot redirect more than once");
		}
		// encode to make sure no caller forgot this
		this.redirectURL = encodeURL(url).toString();
	}

	/**
	 * Writes string to response output.
	 * 
	 * @param string
	 *            The string to write
	 */
	@Override
	public void write(final CharSequence string)
	{
		buffer.append(string);
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API.
	 */
	public final void filter()
	{
		if (redirectURL == null && buffer.length() != 0)
		{
			this.buffer = filter(buffer);

		}
	}
	
	/**
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return buffer.toString();
	}
}