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
package org.apache.wicket.util.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

import org.apache.wicket.util.lang.Args;

/**
 * Utilities methods for working with input and output streams.
 * 
 * @author Jonathan Locke
 * @author Igor Vaynberg
 */
public final class Streams
{
	/**
	 * Writes the input stream to the output stream. Input is done without a Reader object, meaning
	 * that the input is copied in its raw form. After it is copied it will close the streams.
	 * 
	 * @param in
	 *            The input stream
	 * @param out
	 *            The output stream
	 * @return Number of bytes copied from one stream to the other
	 * @throws IOException
	 */
	public static int copyAndClose(final InputStream in, final OutputStream out) throws IOException
	{
		try
		{
			return copy(in, out);
		}
		finally
		{
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(out);
		}
	}

	/**
	 * Writes the input stream to the output stream. Input is done without a Reader object, meaning
	 * that the input is copied in its raw form.
	 * 
	 * @param in
	 *            The input stream
	 * @param out
	 *            The output stream
	 * @return Number of bytes copied from one stream to the other
	 * @throws IOException
	 */
	public static int copy(final InputStream in, final OutputStream out) throws IOException
	{
		return copy(in, out, 4096);
	}

	/**
	 * Writes the input stream to the output stream. Input is done without a Reader object, meaning
	 * that the input is copied in its raw form.
	 * 
	 * @param in
	 *            The input stream
	 * @param out
	 *            The output stream
	 * @param bufSize
	 *            The buffer size. A good value is 4096.
	 * @return Number of bytes copied from one stream to the other
	 * @throws IOException
	 */
	public static int copy(final InputStream in, final OutputStream out, final int bufSize)
		throws IOException
	{
		if (bufSize <= 0)
		{
			throw new IllegalArgumentException("The parameter 'bufSize' must not be <= 0");
		}

		final byte[] buffer = new byte[bufSize];
		int bytesCopied = 0;
		while (true)
		{
			int byteCount = in.read(buffer, 0, buffer.length);
			if (byteCount <= 0)
			{
				break;
			}
			out.write(buffer, 0, byteCount);
			bytesCopied += byteCount;
		}
		return bytesCopied;
	}

	/**
	 * Loads properties from an XML input stream into the provided properties object.
	 * 
	 * @param properties
	 *            The object to load the properties into
	 * @param inputStream
	 * @throws IOException
	 *             When the input stream could not be read from
	 */
	public static void loadFromXml(final Properties properties, final InputStream inputStream)
		throws IOException
	{
		if (properties == null)
		{
			throw new IllegalArgumentException("properties must not be null");
		}
		if (inputStream == null)
		{
			throw new IllegalArgumentException("inputStream must not be null");
		}

		properties.loadFromXML(inputStream);
	}

	/**
	 * Sets the connection to a URL as non-caching and returns the input stream.
	 *
	 * @param url
	 *      the url to read from
	 * @return the input stream for this url
	 * @throws IOException when a connection cannot be opened
	 */
	public static InputStream readNonCaching(final URL url) throws IOException
	{
		Args.notNull(url, "url");

		URLConnection urlConnection = url.openConnection();
		urlConnection.setUseCaches(false);
		InputStream inputStream = urlConnection.getInputStream();
		return inputStream;
	}

	/**
	 * Reads a stream as a string.
	 * 
	 * @param in
	 *            The input stream
	 * @return The string
	 * @throws IOException
	 */
	public static String readString(final InputStream in) throws IOException
	{
		return readString(new BufferedReader(new InputStreamReader(in)));
	}

	/**
	 * Reads a string using a character encoding.
	 * 
	 * @param in
	 *            The input
	 * @param encoding
	 *            The character encoding of the input data
	 * @return The string
	 * @throws IOException
	 */
	public static String readString(final InputStream in, final CharSequence encoding)
		throws IOException
	{
		return readString(new BufferedReader(new InputStreamReader(in, encoding.toString())));
	}

	/**
	 * Reads all input from a reader into a string.
	 * 
	 * @param in
	 *            The input
	 * @return The string
	 * @throws IOException
	 */
	public static String readString(final Reader in) throws IOException
	{
		final StringBuilder buffer = new StringBuilder(2048);
		int value;

		while ((value = in.read()) != -1)
		{
			buffer.append((char)value);
		}

		return buffer.toString();
	}

	/**
	 * Private to prevent instantiation.
	 */
	private Streams()
	{
	}
}
