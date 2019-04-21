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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import javax.servlet.http.Cookie;
import org.apache.wicket.Application;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.response.filter.IResponseFilter;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.string.AppendingStringBuffer;

/**
 * Subclass of {@link WebResponse} that buffers the actions and performs those on another response.
 * 
 * @see #writeTo(WebResponse)
 * 
 * @author Matej Knopp
 */
public class BufferedWebResponse extends WebResponse implements IMetaDataBufferingWebResponse
{
	private final WebResponse originalResponse;

	/**
	 * Construct.
	 * 
	 * @param originalResponse
	 */
	public BufferedWebResponse(WebResponse originalResponse)
	{
		// if original response had some metadata set
		// we should transfer it to the current response
		if (originalResponse instanceof IMetaDataBufferingWebResponse)
		{
			((IMetaDataBufferingWebResponse)originalResponse).writeMetaData(this);
		}
		this.originalResponse = originalResponse;
	}

	/**
	 * transfer cookie operations (add, clear) to given web response
	 * 
	 * @param response
	 *            web response that should receive the current cookie operation
	 */
	@Override
	public void writeMetaData(WebResponse response)
	{
		for (Action action : actions)
		{
			if (action.getType() == ActionType.HEADER)
				action.invoke(response);
		}
	}


	@Override
	public String encodeURL(CharSequence url)
	{
		if (originalResponse != null)
		{
			return originalResponse.encodeURL(url);
		}
		else
		{
			return url != null ? url.toString() : null;
		}
	}

	@Override
	public String encodeRedirectURL(CharSequence url)
	{
		if (originalResponse != null)
		{
			return originalResponse.encodeRedirectURL(url);
		}
		else
		{
			return url != null ? url.toString() : null;
		}
	}

	private enum ActionType {
		/**
		 * Actions not related directly to the content of the response, eg setting cookies, headers.
		 */
		HEADER,
		REDIRECT,
		NORMAL,
		/**
		 * Actions directly related to the data of the response, eg writing output, etc.
		 */
		DATA;

		protected final Action action(Consumer<WebResponse> action) {
			return new Action(this, action);
		}
	}

	private static final class Action implements Comparable<Action>
	{
		private final ActionType type;
		private final Consumer<WebResponse> action;

		private Action(ActionType type, Consumer<WebResponse> action)
		{
			this.type = type;
			this.action = action;
		}

		protected final void invoke(WebResponse response)
		{
			action.accept(response);
		}

		protected final ActionType getType()
		{
			return type;
		}

		@Override
		public int compareTo(Action o)
		{
			return getType().ordinal() - o.getType().ordinal();
		}
	}

	private final List<Action> actions = new ArrayList<Action>();
	private StringBuilder charSequenceBuilder;
	private ByteArrayOutputStream dataStream;

	@Override
	public void reset()
	{
		super.reset();
		actions.clear();
		charSequenceBuilder = null;
		dataStream = null;
	}

	@Override
	public void addCookie(Cookie cookie)
	{
		actions.add(ActionType.HEADER.action(res -> res.addCookie(cookie)));
	}

	@Override
	public void clearCookie(Cookie cookie)
	{
		actions.add(ActionType.HEADER.action(res -> res.clearCookie(cookie)));
	}

	@Override
	public void setContentLength(long length)
	{
		actions.add(ActionType.HEADER.action(res -> res.setContentLength(length)));
	}

	@Override
	public void setContentType(String mimeType)
	{
		actions.add(ActionType.HEADER.action(res -> res.setContentType(mimeType)));
	}

	@Override
	public void setDateHeader(String name, Instant date)
	{
		Args.notNull(date, "date");
		actions.add(ActionType.HEADER.action(res -> res.setDateHeader(name, date)));
	}

	@Override
	public void setHeader(String name, String value)
	{
		actions.add(ActionType.HEADER.action(res -> res.setHeader(name, value)));
	}

	@Override
	public void addHeader(String name, String value)
	{
		actions.add(ActionType.HEADER.action(res -> res.addHeader(name, value)));
	}

	@Override
	public void disableCaching()
	{
		actions.add(ActionType.HEADER.action(WebResponse::disableCaching));
	}

	@Override
	public void write(CharSequence sequence)
	{
		if (dataStream != null)
		{
			throw new IllegalStateException(
				"Can't call write(CharSequence) after write(byte[]) has been called.");
		}

		if (charSequenceBuilder == null)
		{
			StringBuilder builder = new StringBuilder(4096);
			charSequenceBuilder = builder;
			actions.add(ActionType.DATA.action(res ->
			{
				AppendingStringBuffer responseBuffer = new AppendingStringBuffer(builder);

				List<IResponseFilter> responseFilters = Application.get()
						.getRequestCycleSettings()
						.getResponseFilters();

				if (responseFilters != null)
				{
					for (IResponseFilter filter : responseFilters)
					{
						responseBuffer = filter.filter(responseBuffer);
					}
				}
				res.write(responseBuffer);
			}));
		}
		charSequenceBuilder.append(sequence);
	}

	/**
	 * Returns the text already written to this response.
	 * 
	 * @return text
	 */
	public CharSequence getText()
	{
		if (dataStream != null)
		{
			throw new IllegalStateException("write(byte[]) has already been called.");
		}
		if (charSequenceBuilder != null)
		{
			return charSequenceBuilder;
		}
		else
		{
			return null;
		}
	}

	/**
	 * Replaces the text in this response
	 * 
	 * @param text
	 */
	public void setText(CharSequence text)
	{
		if (dataStream != null)
		{
			throw new IllegalStateException("write(byte[]) has already been called.");
		}
		if (charSequenceBuilder != null)
		{
			charSequenceBuilder.setLength(0);
		}
		write(text);
	}

	@Override
	public void write(byte[] array)
	{
		write(array, 0, array.length);
	}

	@Override
	public void write(byte[] array, int offset, int length)
	{
		if (charSequenceBuilder != null)
		{
			throw new IllegalStateException(
				"Can't call write(byte[]) after write(CharSequence) has been called.");
		}
		if (dataStream == null)
		{
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			dataStream = stream;
			actions.add(ActionType.DATA.action(res -> writeStream(res, stream)));
		}
		dataStream.write(array, offset, length);
	}

	@Override
	public void sendRedirect(String url)
	{
		actions.add(ActionType.REDIRECT.action(res -> res.sendRedirect(url)));
	}

	@Override
	public void setStatus(int sc)
	{
		actions.add(ActionType.HEADER.action(res -> res.setStatus(sc)));
	}

	@Override
	public void sendError(int sc, String msg)
	{
		actions.add(ActionType.NORMAL.action(res -> res.sendError(sc, msg)));
	}

	/**
	 * Writes the content of the buffer to the specified response. Also sets the properties and and
	 * headers.
	 * 
	 * @param response
	 */
	public void writeTo(final WebResponse response)
	{
		Args.notNull(response, "response");

		Collections.sort(actions);

		for (Action action : actions)
		{
			action.invoke(response);
		}
	}

	@Override
	public boolean isRedirect()
	{
		for (Action action : actions)
		{
			if (action.getType() == ActionType.REDIRECT)
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public void flush()
	{
		actions.add(ActionType.NORMAL.action(WebResponse::flush));
	}

	private static void writeStream(final Response response, ByteArrayOutputStream stream)
	{
		final boolean copied[] = { false };
		try
		{
			// try to avoid copying the array
			stream.writeTo(new OutputStream()
			{
				@Override
				public void write(int b) throws IOException
				{

				}

				@Override
				public void write(byte[] b, int off, int len) throws IOException
				{
					if (off == 0 && len == b.length)
					{
						response.write(b);
						copied[0] = true;
					}
				}
			});
		}
		catch (IOException e1)
		{
			throw new WicketRuntimeException(e1);
		}
		if (copied[0] == false)
		{
			response.write(stream.toByteArray());
		}
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		final String toString;
		if (charSequenceBuilder != null)
		{
			toString = charSequenceBuilder.toString();
		}
		else
		{
			toString = super.toString();
		}
		return toString;
	}

	@Override
	public Object getContainerResponse()
	{
		return originalResponse.getContainerResponse();
	}
}
