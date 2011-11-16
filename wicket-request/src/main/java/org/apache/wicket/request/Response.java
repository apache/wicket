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
package org.apache.wicket.request;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.wicket.util.lang.Args;

/**
 * Abstract base class for different implementations of response writing.
 * <p>
 * The implementation may not support calling both {@link #write(byte[])} and
 * {@link #write(CharSequence)} on the same {@link Response} instance.
 * 
 * @author Matej Knopp
 * @author igor.vaynberg
 */
public abstract class Response
{
	/**
	 * Writes the {@link CharSequence} to output.
	 * 
	 * @param sequence
	 * @throws IllegalStateException
	 *             if {@link #write(byte[])} has already been called on this instance
	 */
	public abstract void write(CharSequence sequence);

	/**
	 * Writes the buffer to output.
	 * 
	 * @param array
	 *            the data.
	 * @throws IllegalStateException
	 *             if {@link #write(CharSequence)} has already been called on this instance
	 */
	public abstract void write(byte[] array);

	/**
	 * Writes the buffer to output.
	 * 
	 * @param array
	 *            the data.
	 * @param offset
	 *            the start offset in the data.
	 * @param length
	 *            the number of bytes to write.
	 * 
	 * @throws IllegalStateException
	 *             if {@link #write(CharSequence)} has already been called on this instance
	 * @since 1.5.1
	 */
	public abstract void write(byte[] array, int offset, int length);

	/**
	 * Closes the response
	 */
	public void close()
	{
	}


	/**
	 * Encodes the specified URL by including the session ID in it, or, if encoding is not needed,
	 * returns the URL unchanged.
	 * 
	 * @param url
	 * @return encoded URL
	 */
	public abstract String encodeURL(CharSequence url);

	/**
	 * Called when the Response needs to reset itself. Subclasses can empty there buffer or build up
	 * state.
	 */
	public void reset()
	{
	}

	/**
	 * Provides access to the low-level container response object that implementaion of this
	 * {@link Response} delegate to. This allows users to access features provided by the container
	 * response but not by generalized Wicket {@link Response} objects.
	 * 
	 * @return low-level container response object, or {@code null} if none
	 */
	public abstract Object getContainerResponse();

	/**
	 * Returns an {@link OutputStream} suitable for writing binary data in the response. The servlet
	 * container does not encode the binary data.
	 * 
	 * <p>
	 * Calling flush() on the OutputStream commits the response.
	 * </p>
	 * <p>
	 * This method returns an output stream that delegates to {@link #write(byte[])},
	 * {@link #write(byte[], int, int)}, and {@link #close()} methods of this response instance
	 * </p>
	 * 
	 * @return output stream
	 */
	public OutputStream getOutputStream()
	{
		return new StreamAdapter(this);
	}

	private static class StreamAdapter extends OutputStream
	{
		private final Response response;

		public StreamAdapter(Response response)
		{
			Args.notNull(response, "response");
			this.response = response;
		}

		@Override
		public void write(int b) throws IOException
		{
			response.write(new byte[] { (byte)b });
		}

		@Override
		public void write(byte[] b) throws IOException
		{
			response.write(b);
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException
		{
			response.write(b, off, len);
		}

		@Override
		public void close() throws IOException
		{
			super.close();
			response.close();
		}
	}

}
