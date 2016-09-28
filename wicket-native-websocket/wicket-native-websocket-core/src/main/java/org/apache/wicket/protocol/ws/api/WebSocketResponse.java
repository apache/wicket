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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.http.Cookie;

import org.apache.wicket.request.Response;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.util.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link Response} used to cache the written data to the web socket client
 * when Wicket thread locals are available.
 *
 * When the thread locals are not available then you can write directly to the {@link IWebSocketConnection}
 * taken from {@link org.apache.wicket.protocol.ws.api.registry.IWebSocketConnectionRegistry}.
 * In this case the response wont be cached.
 *
 * @since 6.0
 */
public class WebSocketResponse extends WebResponse
{
	private static final Logger LOG = LoggerFactory.getLogger(WebSocketResponse.class);
	
	private final IWebSocketConnection connection;

	private StringBuilder text;

	private ByteArrayOutputStream binary;

	private boolean isRedirect = false;

	public WebSocketResponse(final IWebSocketConnection conn)
	{
		this.connection = conn;
	}

	@Override
	public void write(CharSequence sequence)
	{
		if (text == null)
		{
			text = new StringBuilder();
		}
		text.append(sequence);
	}

	@Override
	public void write(byte[] array)
	{
		write(array, 0, array.length);
	}

	@Override
	public void write(byte[] array, int offset, int length)
	{
		if (binary == null)
		{
			binary = new ByteArrayOutputStream();
		}
		binary.write(array, offset, length);
	}

	@Override
	public void close()
	{
		if (connection.isOpen())
		{
			try
			{
				if (text != null)
				{
					connection.sendMessage(text.toString());
					text = null;
				}
				else if (binary != null)
				{
					byte[] bytes = binary.toByteArray();
					connection.sendMessage(bytes, 0, bytes.length);
					binary.close();
					binary = null;
				}

			} catch (IOException iox)
			{
				LOG.error("An error occurred while writing response to WebSocket client.", iox);
			}
		}
		super.close();
	}

	@Override
	public void reset()
	{
		text = null;
		if (binary != null)
		{
			try
			{
				binary.close();
			} catch (IOException iox)
			{
				LOG.error("An error occurred while resetting the binary content", iox);
			}
			binary = null;
		}
		isRedirect = false;
		super.reset();
	}

	@Override
	public String encodeURL(CharSequence url)
	{
		return url.toString();
	}

	@Override
	public final IWebSocketConnection getContainerResponse()
	{
		return connection;
	}

	@Override
	public void addCookie(Cookie cookie)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void clearCookie(Cookie cookie)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void setHeader(String name, String value)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void addHeader(String name, String value)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void setDateHeader(String name, Time date)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void setContentLength(long length)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void setContentType(String mimeType)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void setStatus(int sc)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void sendError(int sc, String msg)
	{
		LOG.warn("An HTTP error response in WebSocket communication would not be processed by the browser! " +
				"If you need to send the error code and message to the client then configure custom WebSocketResponse " +
				"via WebSocketSettings#newWebSocketResponse() factory method and override #sendError() method to write " +
				"them in an appropriate format for your application. " +
				"The ignored error code is '{}' and the message: '{}'.", sc, msg);
	}

	@Override
	public String encodeRedirectURL(CharSequence url)
	{
		return url.toString();
	}

	@Override
	public void sendRedirect(String url)
	{
		isRedirect = true;
		url = encodeRedirectURL(url);

		String ajaxRedirect = "<ajax-response><redirect><![CDATA[" + url + "]]></redirect></ajax-response>";
		write(ajaxRedirect);
	}

	@Override
	public boolean isRedirect()
	{
		return isRedirect;
	}

	@Override
	public void flush()
	{
	}
}
