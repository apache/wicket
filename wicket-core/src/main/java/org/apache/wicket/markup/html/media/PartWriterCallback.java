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
package org.apache.wicket.markup.html.media;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.protocol.http.servlet.ResponseIOException;
import org.apache.wicket.request.resource.AbstractResource.WriteCallback;
import org.apache.wicket.request.resource.IResource.Attributes;
import org.apache.wicket.util.io.Streams;
import org.apache.wicket.util.resource.IResourceStream;

/**
 * Used to read a part of the package resource stream and write it to the output stream of the
 * response.
 *
 * @author Tobias Soloschenko
 *
 */
public class PartWriterCallback extends WriteCallback
{
	private final IResourceStream resourceStream;

	private final Long startbyte;

	private Long endbyte;

	private int bufferSize;

	/**
	 * Creates a part writer callback.<br>
	 * <br>
	 * Reads a part of the given resource stream. If the startbyte parameter is not null the
	 * number of bytes are skipped till the stream is read. If the endbyte is not null the stream is
	 * read till endbyte, else to the end of the whole stream. If startbyte and endbyte is null the
	 * whole stream is read.
	 *
	 * @param resourceStream
	 *            the resource stream to read
	 * @param startbyte
	 *            the start position to read from (if not null the number of bytes are skipped till
	 *            the stream is read)
	 * @param endbyte
	 *            the end position to read to (if not null the stream is going to be read till
	 *            endbyte, else to the end of the whole stream)
	 */
	public PartWriterCallback(IResourceStream resourceStream, Long startbyte,
		Long endbyte)
	{
		this.resourceStream = resourceStream;
		this.startbyte = startbyte;
		this.endbyte = endbyte;
	}

	/**
	 * Writes the data
	 *
	 * @param attributes
	 *            the attributes to get the output stream of the response
	 */
	@Override
	public void writeData(Attributes attributes) throws IOException
	{
		try
		{
			InputStream inputStream = resourceStream.getInputStream();
			OutputStream outputStream = attributes.getResponse().getOutputStream();
			byte[] buffer = new byte[getBufferSize()];

			if (startbyte != null || endbyte != null)
			{
				// skipping the first bytes which are
				// requested to be skipped by the client
				if (startbyte != null)
				{
					inputStream.skip(startbyte);
				}

				// If there are no end bytes given read the whole stream till the end
				if (endbyte == null)
				{
					endbyte = resourceStream.length().bytes();
				}

				long totalBytes = 0;
				int actualReadBytes;

				while ((actualReadBytes = inputStream.read(buffer)) != -1)
				{
					totalBytes = totalBytes + buffer.length;
					long lowerBuffer = endbyte - totalBytes;
					if (lowerBuffer <= 0)
					{
						buffer = resizeArray(buffer, actualReadBytes);
						outputStream.write(buffer);
						break;
					}
					else
					{
						outputStream.write(buffer);
					}
				}
			}
			else
			{
				Streams.copy(inputStream, outputStream, getBufferSize());
			}
		}
		catch (ResponseIOException e)
		{
			// the client has closed the connection and
			// doesn't read the stream further on
			// (in tomcats
			// org.apache.catalina.connector.ClientAbortException)
			// we ignore this case
		}
		catch (Exception e)
		{
			throw new WicketRuntimeException(
				"A problem occurred while writing the buffer to the output stream.", e);
		}
	}

	/**
	 * Reallocates an array with a new size, and copies the contents of the old array to the new
	 * array.
	 *
	 * @param oldArray
	 *            the old array, to be reallocated.
	 * @param newSize
	 *            the new array size.
	 * @return A new array with the same contents.
	 */
	@SuppressWarnings("rawtypes")
	private static byte[] resizeArray(byte[] oldArray, int newSize)
	{
		int oldSize = oldArray.length;
		byte[] newArray = new byte[newSize];
		int minLength = Math.min(oldSize, newSize);
		if (minLength > 0)
		{
			System.arraycopy(oldArray, 0, newArray, 0, minLength);
		}
		return newArray;
	}

	/**
	 * Sets the buffer size used to send the data to the client
	 *
	 * @return the buffer size used to send the data to the client (default is 4096)
	 */
	public int getBufferSize()
	{
		return bufferSize > 0 ? bufferSize : 4096;
	}

	/**
	 * Sets the buffer size used to send the data to the client
	 *
	 * @param bufferSize
	 *            the buffer size used to send the data to the client
	 */
	public void setBufferSize(int bufferSize)
	{
		this.bufferSize = bufferSize;
	}

}
