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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.Cookie;

import org.apache.wicket.Application;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.response.filter.IResponseFilter;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.apache.wicket.util.time.Time;

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
	public void writeMetaData(WebResponse response)
	{
		for (Action action : actions)
		{
			if (action instanceof MetaDataAction)
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

	private static abstract class Action implements Comparable<Action>
	{
		protected abstract void invoke(WebResponse response);

		public int compareTo(Action o)
		{
			return 0;
		}
	}

	/**
	 * Actions not related directly to the content of the response, eg setting cookies, headers.
	 * 
	 * @author igor
	 */
	private static abstract class MetaDataAction extends Action
	{
		@Override
		public int compareTo(Action o)
		{
			// write first in response
			return -1;
		}
	}

	private static class WriteCharSequenceAction extends Action
	{
		private final StringBuilder builder = new StringBuilder(4096);

		public WriteCharSequenceAction()
		{

		}

		public void append(CharSequence sequence)
		{
			builder.append(sequence);
		}

		@Override
		protected void invoke(WebResponse response)
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
			response.write(responseBuffer);
		}

		@Override
		public int compareTo(Action o)
		{
			// needs to be invoked after set header actions
			return 1;
		}
	}

	private static class WriteDataAction extends Action
	{
		private final ByteArrayOutputStream stream = new ByteArrayOutputStream();

		public WriteDataAction()
		{

		}

		public void append(byte data[])
		{
			try
			{
				stream.write(data);
			}
			catch (IOException e)
			{
				throw new WicketRuntimeException(e);
			}
		}

		public void append(byte data[], int offset, int length)
		{
			try
			{
				stream.write(data, offset, length);
			}
			catch (Exception e)
			{
				throw new WicketRuntimeException(e);
			}
		}

		@Override
		protected void invoke(WebResponse response)
		{
			writeStream(response, stream);
		}

		@Override
		public int compareTo(Action o)
		{
			// needs to be invoked after set header actions
			return 1;
		}
	}

	private static class CloseAction extends Action
	{
		@Override
		protected void invoke(WebResponse response)
		{
			response.close();
		}
	}

	private static class AddCookieAction extends MetaDataAction
	{
		private final Cookie cookie;

		public AddCookieAction(Cookie cookie)
		{
			this.cookie = cookie;
		}

		@Override
		protected void invoke(WebResponse response)
		{
			response.addCookie(cookie);
		}
	}

	private static class ClearCookieAction extends MetaDataAction
	{
		private final Cookie cookie;

		public ClearCookieAction(Cookie cookie)
		{
			this.cookie = cookie;
		}

		@Override
		protected void invoke(WebResponse response)
		{
			response.clearCookie(cookie);
		}
	}

	private static class SetHeaderAction extends MetaDataAction
	{
		private final String name;
		private final String value;

		public SetHeaderAction(String name, String value)
		{
			this.name = name;
			this.value = value;
		}

		@Override
		protected void invoke(WebResponse response)
		{
			response.setHeader(name, value);
		}
	}

	private static class AddHeaderAction extends MetaDataAction
	{
		private final String name;
		private final String value;

		public AddHeaderAction(String name, String value)
		{
			this.name = name;
			this.value = value;
		}

		@Override
		protected void invoke(WebResponse response)
		{
			response.addHeader(name, value);
		}
	}

	private static class SetDateHeaderAction extends MetaDataAction
	{
		private final String name;
		private final Time value;

		public SetDateHeaderAction(String name, Time value)
		{
			this.name = name;
			this.value = Args.notNull(value, "value");
		}

		@Override
		protected void invoke(WebResponse response)
		{
			response.setDateHeader(name, value);
		}
	}

	private static class SetContentLengthAction extends MetaDataAction
	{
		private final long contentLength;

		public SetContentLengthAction(long contentLength)
		{
			this.contentLength = contentLength;
		}

		@Override
		protected void invoke(WebResponse response)
		{
			response.setContentLength(contentLength);
		}
	}

	private static class SetContentTypeAction extends MetaDataAction
	{
		private final String contentType;

		public SetContentTypeAction(String contentType)
		{
			this.contentType = contentType;
		}

		@Override
		protected void invoke(WebResponse response)
		{
			response.setContentType(contentType);
		}
	}

	private static class SetStatusAction extends MetaDataAction
	{
		private final int sc;

		public SetStatusAction(int sc)
		{
			this.sc = sc;
		}

		@Override
		protected void invoke(WebResponse response)
		{
			response.setStatus(sc);
		}
	}

	private static class SendErrorAction extends Action
	{
		private final int sc;
		private final String msg;

		public SendErrorAction(int sc, String msg)
		{
			this.sc = sc;
			this.msg = msg;
		}

		@Override
		protected void invoke(WebResponse response)
		{
			response.sendError(sc, msg);
		}
	}

	private static class SendRedirectAction extends Action
	{
		private final String url;

		public SendRedirectAction(String url)
		{
			this.url = url;
		}

		@Override
		protected void invoke(WebResponse response)
		{
			response.sendRedirect(url);
		}
	}

	private static class FlushAction extends Action
	{
		@Override
		protected void invoke(WebResponse response)
		{
			response.flush();
		}
	}

	private final List<Action> actions = new ArrayList<Action>();
	private WriteCharSequenceAction charSequenceAction;
	private WriteDataAction dataAction;

	@Override
	public void reset()
	{
		super.reset();
		actions.clear();
		charSequenceAction = null;
		dataAction = null;
	}

	@Override
	public void addCookie(Cookie cookie)
	{
		actions.add(new AddCookieAction(cookie));
	}

	@Override
	public void clearCookie(Cookie cookie)
	{
		actions.add(new ClearCookieAction(cookie));
	}

	@Override
	public void setContentLength(long length)
	{
		actions.add(new SetContentLengthAction(length));
	}

	@Override
	public void setContentType(String mimeType)
	{
		actions.add(new SetContentTypeAction(mimeType));
	}

	@Override
	public void setDateHeader(String name, Time date)
	{
		actions.add(new SetDateHeaderAction(name, date));
	}

	@Override
	public void setHeader(String name, String value)
	{
		actions.add(new SetHeaderAction(name, value));
	}

	@Override
	public void addHeader(String name, String value)
	{
		actions.add(new AddHeaderAction(name, value));
	}

	@Override
	public void write(CharSequence sequence)
	{
		if (dataAction != null)
		{
			throw new IllegalStateException(
				"Can't call write(CharSequence) after write(byte[]) has been called.");
		}

		if (charSequenceAction == null)
		{
			charSequenceAction = new WriteCharSequenceAction();
			actions.add(charSequenceAction);
		}
		charSequenceAction.append(sequence);
	}

	/**
	 * Returns the text already written to this response.
	 * 
	 * @return text
	 */
	public CharSequence getText()
	{
		if (dataAction != null)
		{
			throw new IllegalStateException("write(byte[]) has already been called.");
		}
		if (charSequenceAction != null)
		{
			return charSequenceAction.builder;
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
		if (dataAction != null)
		{
			throw new IllegalStateException("write(byte[]) has already been called.");
		}
		if (charSequenceAction != null)
		{
			charSequenceAction.builder.setLength(0);
		}
		write(text);
	}

	@Override
	public void write(byte[] array)
	{
		if (charSequenceAction != null)
		{
			throw new IllegalStateException(
				"Can't call write(byte[]) after write(CharSequence) has been called.");
		}
		if (dataAction == null)
		{
			dataAction = new WriteDataAction();
			actions.add(dataAction);
		}
		dataAction.append(array);
	}

	@Override
	public void write(byte[] array, int offset, int length)
	{
		if (charSequenceAction != null)
		{
			throw new IllegalStateException(
				"Can't call write(byte[]) after write(CharSequence) has been called.");
		}
		if (dataAction == null)
		{
			dataAction = new WriteDataAction();
			actions.add(dataAction);
		}
		dataAction.append(array, offset, length);
	}

	@Override
	public void sendRedirect(String url)
	{
		actions.add(new SendRedirectAction(url));
	}

	@Override
	public void setStatus(int sc)
	{
		actions.add(new SetStatusAction(sc));
	}

	@Override
	public void sendError(int sc, String msg)
	{
		actions.add(new SendErrorAction(sc, msg));
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
			if (action instanceof SendRedirectAction)
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public void flush()
	{
		actions.add(new FlushAction());
	}

	private static final void writeStream(final Response response, ByteArrayOutputStream stream)
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
		if (charSequenceAction != null)
		{
			toString = charSequenceAction.builder.toString();
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
